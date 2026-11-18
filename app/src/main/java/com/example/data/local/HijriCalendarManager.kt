package com.example.data.local

import android.content.Context
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.HijriCalendarWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class HijriCalendarUiState(
    val hijriDay: Int = 1,
    val hijriMonth: Int = 1,
    val hijriYear: Int = 1448,
    val hijriMonthName: String = "محرم",
    val dayName: String = "",
    val gregorianDateFormatted: String = "",
    val manualOffsetDays: Int = 0,
    val isManualAdjusted: Boolean = false,
    val isSyncedFromRemote: Boolean = false,
    val isCriticalWindow: Boolean = false,
    val lastFetchedMillis: Long = 0L,
    val remoteLastUpdated: String = "",
    val sourceLabel: String = "",
    val isRefreshing: Boolean = false,
    val lastSyncError: String? = null
)

object HijriCalendarManager {
    private const val BASE_HIJRI_URL = "https://raw.githubusercontent.com/moshraheem-sudo/hijri/main/hijri.json"
    private const val PREFS_NAME = "hijri_calendar_prefs"
    private const val UNIQUE_WORK_NAME = "hijri_calendar_auto_sync"

    private const val KEY_HAS_REMOTE = "has_remote_data"
    private const val KEY_REMOTE_DAY = "remote_hijri_day"
    private const val KEY_REMOTE_MONTH = "remote_hijri_month"
    private const val KEY_REMOTE_YEAR = "remote_hijri_year"
    private const val KEY_REMOTE_MONTH_NAME = "remote_hijri_month_name"
    private const val KEY_REMOTE_DAY_NAME = "remote_day_name"
    private const val KEY_REMOTE_GREGORIAN_DATE = "remote_gregorian_date"
    private const val KEY_REMOTE_ANCHOR_DATE = "remote_gregorian_anchor_date"
    private const val KEY_REMOTE_STATUS = "remote_status"
    private const val KEY_REMOTE_SOURCE = "remote_source"
    private const val KEY_REMOTE_LAST_UPDATED = "remote_last_updated"
    private const val KEY_LAST_FETCH_MILLIS = "last_fetch_millis"
    private const val KEY_LAST_FETCH_EPOCH_DAY = "last_fetch_epoch_day"
    private const val KEY_MANUAL_OFFSET_DAYS = "manual_offset_days"
    private const val KEY_SCHEDULED_CRITICAL = "scheduled_critical_mode"

    private val HIJRI_MONTH_NAMES_AR = listOf(
        "محرم",
        "صفر",
        "ربيع الأول",
        "ربيع الآخر",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوال",
        "ذو القعدة",
        "ذو الحجة"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow(HijriCalendarUiState())
    val state: StateFlow<HijriCalendarUiState> = _state.asStateFlow()

    fun buildHijriUrl(): String = "$BASE_HIJRI_URL?t=${System.currentTimeMillis()}"

    fun init(context: Context) {
        val computed = computeUiState(context, isRefreshing = false, error = null)
        _state.value = computed
        schedulePeriodicSync(context, computed.isCriticalWindow)
    }

    fun setManualOffset(context: Context, offsetDays: Int) {
        val clamped = offsetDays.coerceIn(-5, 5)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_MANUAL_OFFSET_DAYS, clamped)
            .apply()
        val updated = computeUiState(context, isRefreshing = _state.value.isRefreshing, error = null)
        _state.value = updated
        schedulePeriodicSync(context, updated.isCriticalWindow)
    }

    fun adjustManualOffsetBy(context: Context, delta: Int) {
        val current = getManualOffset(context)
        setManualOffset(context, current + delta)
    }

    fun getManualOffset(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_MANUAL_OFFSET_DAYS, 0)
            .coerceIn(-5, 5)
    }

    fun shouldAutoRefresh(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_HAS_REMOTE, false)) return true

        val current = computeUiState(context, isRefreshing = false, error = null)
        // آخر 3 أيام من الشهر (28، 29، 30) + أول يوم بعد الثلاثين (1): تحقق فوري في كل مرة
        if (current.isCriticalWindow) return true

        val todayEpoch = todayLocalEpochDay()
        val lastFetchEpochDay = prefs.getLong(KEY_LAST_FETCH_EPOCH_DAY, -1L)
        if (todayEpoch != lastFetchEpochDay) return true

        val lastFetchMillis = prefs.getLong(KEY_LAST_FETCH_MILLIS, 0L)
        return (System.currentTimeMillis() - lastFetchMillis) >= TimeUnit.MINUTES.toMillis(30)
    }

    suspend fun refreshFromRemote(
        context: Context,
        force: Boolean = false,
        resetManualOffset: Boolean = false
    ): Result<HijriCalendarUiState> {
        if (!force && !shouldAutoRefresh(context)) {
            val current = computeUiState(context, isRefreshing = false, error = null)
            _state.value = current
            return Result.success(current)
        }

        _state.value = _state.value.copy(isRefreshing = true, lastSyncError = null)

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(buildHijriUrl())
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val msg = "تعذر تحديث التقويم (رمز ${response.code})"
                        val fallback = computeUiState(context, isRefreshing = false, error = msg)
                        _state.value = fallback
                        return@withContext Result.failure(IllegalStateException(msg))
                    }

                    val body = response.body?.string().orEmpty()
                    if (body.isBlank()) {
                        val msg = "استجابة التقويم الهجري فارغة"
                        val fallback = computeUiState(context, isRefreshing = false, error = msg)
                        _state.value = fallback
                        return@withContext Result.failure(IllegalStateException(msg))
                    }

                    val json = JSONObject(body)
                    val hijriDay = json.optInt("hijri_day", 0)
                    val hijriMonth = json.optInt("hijri_month", 0)
                    val hijriYear = json.optInt("hijri_year", 0)
                    if (hijriDay !in 1..30 || hijriMonth !in 1..12 || hijriYear < 1400) {
                        val msg = "صيغة بيانات التقويم غير صالحة"
                        val fallback = computeUiState(context, isRefreshing = false, error = msg)
                        _state.value = fallback
                        return@withContext Result.failure(IllegalStateException(msg))
                    }

                    val monthName = json.optString("hijri_month_name", "")
                        .ifBlank { HIJRI_MONTH_NAMES_AR.getOrElse(hijriMonth - 1) { "" } }
                    val dayName = json.optString("day_name", "")
                    val gregorianDate = json.optString("gregorian_date", "")
                    val anchorDate = json.optString("gregorian_anchor_date", "")
                    val status = json.optString("status", "")
                    val source = json.optString("source", "")
                    val lastUpdated = json.optString("last_updated", "")

                    val now = System.currentTimeMillis()
                    val todayEpoch = todayLocalEpochDay()

                    val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putBoolean(KEY_HAS_REMOTE, true)
                        .putInt(KEY_REMOTE_DAY, hijriDay)
                        .putInt(KEY_REMOTE_MONTH, hijriMonth)
                        .putInt(KEY_REMOTE_YEAR, hijriYear)
                        .putString(KEY_REMOTE_MONTH_NAME, monthName)
                        .putString(KEY_REMOTE_DAY_NAME, dayName)
                        .putString(KEY_REMOTE_GREGORIAN_DATE, gregorianDate)
                        .putString(KEY_REMOTE_ANCHOR_DATE, anchorDate)
                        .putString(KEY_REMOTE_STATUS, status)
                        .putString(KEY_REMOTE_SOURCE, source)
                        .putString(KEY_REMOTE_LAST_UPDATED, lastUpdated)
                        .putLong(KEY_LAST_FETCH_MILLIS, now)
                        .putLong(KEY_LAST_FETCH_EPOCH_DAY, todayEpoch)

                    if (resetManualOffset) {
                        editor.putInt(KEY_MANUAL_OFFSET_DAYS, 0)
                    }
                    editor.apply()

                    val updated = computeUiState(context, isRefreshing = false, error = null)
                    _state.value = updated
                    schedulePeriodicSync(context, updated.isCriticalWindow)
                    Result.success(updated)
                }
            } catch (e: Exception) {
                val msg = "تعذر الاتصال برابط التقويم الهجري"
                val fallback = computeUiState(context, isRefreshing = false, error = msg)
                _state.value = fallback
                Result.failure(e)
            }
        }
    }

    fun schedulePeriodicSync(context: Context, isCriticalWindow: Boolean = computeUiState(context, false, null).isCriticalWindow) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val prevCritical = if (prefs.contains(KEY_SCHEDULED_CRITICAL)) {
                prefs.getBoolean(KEY_SCHEDULED_CRITICAL, false)
            } else {
                null
            }

            // آخر 3 أيام من الشهر (28، 29، 30) وأول يوم من الشهر (1): تحديث دوري كل ساعة للتأكد من الهلال
            // بقية الأيام: تحديث دوري مستمر كل 3 ساعات
            val repeatHours = if (isCriticalWindow) 1L else 3L
            val policy = if (prevCritical != isCriticalWindow) {
                ExistingPeriodicWorkPolicy.UPDATE
            } else {
                ExistingPeriodicWorkPolicy.KEEP
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<HijriCalendarWorker>(repeatHours, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                policy,
                request
            )
            prefs.edit().putBoolean(KEY_SCHEDULED_CRITICAL, isCriticalWindow).apply()
        } catch (_: Exception) {
        }
    }

    fun computeUiState(
        context: Context,
        isRefreshing: Boolean = false,
        error: String? = null
    ): HijriCalendarUiState {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasRemote = prefs.getBoolean(KEY_HAS_REMOTE, false)
        val manualOffset = getManualOffset(context)
        val todayEpoch = todayLocalEpochDay()

        var baseDay: Int
        var baseMonth: Int
        var baseYear: Int
        var baseMonthName: String
        var syncedFromRemote = false
        var remoteDayName = ""
        var remoteGregorianEpoch: Long? = null

        if (hasRemote) {
            val rDay = prefs.getInt(KEY_REMOTE_DAY, 1)
            val rMonth = prefs.getInt(KEY_REMOTE_MONTH, 1)
            val rYear = prefs.getInt(KEY_REMOTE_YEAR, 1448)
            val rMonthName = prefs.getString(KEY_REMOTE_MONTH_NAME, "").orEmpty()
            remoteDayName = prefs.getString(KEY_REMOTE_DAY_NAME, "").orEmpty()
            val rGreg = prefs.getString(KEY_REMOTE_GREGORIAN_DATE, "").orEmpty()
            val rAnchor = prefs.getString(KEY_REMOTE_ANCHOR_DATE, "").orEmpty()

            val refEpoch = parseEpochDay(rGreg)
            remoteGregorianEpoch = refEpoch
            val anchorEpoch = parseEpochDay(rAnchor)

            val candidateFromRef = if (refEpoch != null) rDay + (todayEpoch - refEpoch).toInt() else null
            val candidateFromAnchor = if (anchorEpoch != null) 1 + (todayEpoch - anchorEpoch).toInt() else null
            val candidateDay = candidateFromRef ?: candidateFromAnchor

            if (candidateDay != null && candidateDay in 1..30 && rMonth in 1..12) {
                baseDay = candidateDay
                baseMonth = rMonth
                baseYear = rYear
                baseMonthName = rMonthName.ifBlank { HIJRI_MONTH_NAMES_AR.getOrElse(rMonth - 1) { "" } }
                syncedFromRemote = true
            } else if (candidateDay != null && candidateDay in 31..59 && rMonth in 1..12) {
                baseDay = candidateDay - 30
                baseMonth = if (rMonth == 12) 1 else rMonth + 1
                baseYear = if (rMonth == 12) rYear + 1 else rYear
                baseMonthName = HIJRI_MONTH_NAMES_AR.getOrElse(baseMonth - 1) { "" }
                syncedFromRemote = true
            } else {
                val fallback = getUmmAlQuraTriple()
                baseDay = fallback.first
                baseMonth = fallback.second
                baseYear = fallback.third
                baseMonthName = HIJRI_MONTH_NAMES_AR.getOrElse(baseMonth - 1) { "" }
            }
        } else {
            val fallback = getUmmAlQuraTriple()
            baseDay = fallback.first
            baseMonth = fallback.second
            baseYear = fallback.third
            baseMonthName = HIJRI_MONTH_NAMES_AR.getOrElse(baseMonth - 1) { "" }
        }

        val (finalDay, finalMonth, finalYear) = shiftHijriDate(baseDay, baseMonth, baseYear, manualOffset)
        val finalMonthName = if (finalMonth == baseMonth && finalYear == baseYear) {
            baseMonthName
        } else {
            HIJRI_MONTH_NAMES_AR.getOrElse(finalMonth - 1) { "" }
        }

        val isCritical = (baseDay in 28..30 || baseDay == 1) || (finalDay in 28..30 || finalDay == 1)

        val resolvedDayName = if (
            syncedFromRemote &&
            remoteGregorianEpoch == todayEpoch &&
            manualOffset == 0 &&
            remoteDayName.isNotBlank()
        ) {
            remoteDayName
        } else {
            formatArabicDayName(Date())
        }

        val sourceRaw = prefs.getString(KEY_REMOTE_SOURCE, "").orEmpty()
        val sourceLabel = when {
            sourceRaw.contains("sistani", ignoreCase = true) -> "مكتب السيد السيستاني"
            sourceRaw.isNotBlank() -> sourceRaw.removePrefix("https://").removePrefix("http://").trimEnd('/')
            else -> ""
        }

        return HijriCalendarUiState(
            hijriDay = finalDay,
            hijriMonth = finalMonth,
            hijriYear = finalYear,
            hijriMonthName = finalMonthName,
            dayName = resolvedDayName,
            gregorianDateFormatted = formatArabicGregorian(Date()),
            manualOffsetDays = manualOffset,
            isManualAdjusted = manualOffset != 0,
            isSyncedFromRemote = syncedFromRemote,
            isCriticalWindow = isCritical,
            lastFetchedMillis = prefs.getLong(KEY_LAST_FETCH_MILLIS, 0L),
            remoteLastUpdated = prefs.getString(KEY_REMOTE_LAST_UPDATED, "").orEmpty(),
            sourceLabel = sourceLabel,
            isRefreshing = isRefreshing,
            lastSyncError = error
        )
    }

    private fun shiftHijriDate(day: Int, month: Int, year: Int, offset: Int): Triple<Int, Int, Int> {
        var d = day
        var m = month
        var y = year
        if (offset > 0) {
            repeat(offset) {
                d += 1
                if (d > 30) {
                    d = 1
                    m += 1
                    if (m > 12) {
                        m = 1
                        y += 1
                    }
                }
            }
        } else if (offset < 0) {
            repeat(-offset) {
                d -= 1
                if (d < 1) {
                    m -= 1
                    if (m < 1) {
                        m = 12
                        y -= 1
                    }
                    d = getIslamicMonthLength(y, m)
                }
            }
        }
        return Triple(d, m, y)
    }

    private fun getUmmAlQuraTriple(): Triple<Int, Int, Int> {
        val cal = IslamicCalendar(ULocale("@calendar=islamic-umalqura"))
        cal.timeInMillis = System.currentTimeMillis()
        val day = cal.get(IslamicCalendar.DAY_OF_MONTH).coerceIn(1, 30)
        val month = (cal.get(IslamicCalendar.MONTH) + 1).coerceIn(1, 12)
        val year = cal.get(IslamicCalendar.YEAR)
        return Triple(day, month, year)
    }

    private fun getIslamicMonthLength(year: Int, month1Based: Int): Int {
        return try {
            val cal = IslamicCalendar(ULocale("@calendar=islamic-umalqura"))
            cal.set(year, (month1Based - 1).coerceIn(0, 11), 1)
            cal.getActualMaximum(IslamicCalendar.DAY_OF_MONTH).coerceIn(29, 30)
        } catch (_: Exception) {
            30
        }
    }

    private fun parseEpochDay(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            val parsed = sdf.parse(dateStr.trim()) ?: return null
            TimeUnit.MILLISECONDS.toDays(parsed.time)
        } catch (_: Exception) {
            null
        }
    }

    private fun todayLocalEpochDay(): Long {
        val local = Calendar.getInstance()
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                local.get(Calendar.YEAR),
                local.get(Calendar.MONTH),
                local.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }
        return TimeUnit.MILLISECONDS.toDays(utc.timeInMillis)
    }

    private fun formatArabicDayName(date: Date): String {
        return try {
            SimpleDateFormat("EEEE", Locale("ar")).format(date)
        } catch (_: Exception) {
            ""
        }
    }

    private fun formatArabicGregorian(date: Date): String {
        return try {
            val raw = SimpleDateFormat("d MMMM yyyy", Locale("ar")).format(date)
            toArabicNumerals(raw)
        } catch (_: Exception) {
            ""
        }
    }

    fun toArabicNumerals(input: String): String {
        val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return input.map { if (it in '0'..'9') arabicNumerals[it - '0'] else it }.joinToString("")
    }

    fun formatLastFetchedRelative(lastFetchedMillis: Long): String {
        if (lastFetchedMillis <= 0L) return "لم يتم التحديث من الإنترنت بعد"
        val sdf = SimpleDateFormat("yyyy/MM/dd • hh:mm a", Locale("ar"))
        return toArabicNumerals(sdf.format(Date(lastFetchedMillis)))
    }
}
