package com.example.ui.screens.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HijriCalendarManager
import com.example.data.local.NotificationSettingsManager
import com.example.notification.NotificationScheduler
import com.example.notification.AyahNotificationHelper
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.worker.AyahWorker

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ThemeManager
import com.example.data.remote.AppReleaseInfo
import com.example.data.remote.AppUpdateManager
import com.example.data.remote.DownloadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

enum class UpdateStatus {
    IDLE,
    CHECKING,
    AVAILABLE,
    DOWNLOADING,
    DOWNLOADED,
    UP_TO_DATE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    initialExpandHijri: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Hijri Calendar State
    var isHijriExpanded by remember { mutableStateOf(initialExpandHijri) }
    val hijriState by HijriCalendarManager.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        HijriCalendarManager.init(context)
    }

    // App Theme State
    var isThemeCardExpanded by remember { mutableStateOf(false) }

    // 1. Quran App Update State
    val quranCurrentVersion = AppUpdateManager.QURAN_CURRENT_VERSION
    var quranUpcomingVersion by remember { mutableStateOf(quranCurrentVersion) }
    var quranUpdateInfo by remember { mutableStateOf<AppReleaseInfo?>(null) }
    var isQuranUpdateExpanded by remember { mutableStateOf(false) }
    var quranUpdateStatus by remember { mutableStateOf(UpdateStatus.IDLE) }
    var quranDownloadProgress by remember { mutableStateOf(0f) }
    var quranDownloadedMB by remember { mutableStateOf(0f) }
    var quranTotalMB by remember { mutableStateOf(0f) }
    var quranSpeedMBs by remember { mutableStateOf(0f) }
    var quranDownloadedApkFile by remember { mutableStateOf<File?>(null) }
    var quranDownloadJob by remember { mutableStateOf<Job?>(null) }

    var isNourUpdateExpanded by remember { mutableStateOf(false) }
    var isDeveloperCardExpanded by remember { mutableStateOf(false) }
    var isAppInfoExpanded by remember { mutableStateOf(false) }
    var isNotificationsExpanded by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(NotificationSettingsManager.areNotificationsEnabled(context)) }
    var notificationsPerHour by remember { mutableIntStateOf(NotificationSettingsManager.getNotificationsPerHour(context)) }

    var hasPostNotifPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }
    var canScheduleExactAlarm by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.canScheduleExactAlarms() == true
        )
    }
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isIgnoringBatteryOptimizations(context.packageName) == true
            } else true
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPostNotifPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                canScheduleExactAlarm = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.canScheduleExactAlarms() == true
                isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isIgnoringBatteryOptimizations(context.packageName) == true
                } else true
                if (NotificationSettingsManager.areNotificationsEnabled(context)) {
                    NotificationScheduler.ensureAlarmScheduledIfNeeded(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 2. Nour Al-Itrah App Update State
    var nourUpcomingVersion by remember { mutableStateOf("") }
    var nourUpdateInfo by remember { mutableStateOf<AppReleaseInfo?>(null) }
    var nourUpdateStatus by remember { mutableStateOf(UpdateStatus.IDLE) }
    var nourDownloadProgress by remember { mutableStateOf(0f) }
    var nourDownloadedMB by remember { mutableStateOf(0f) }
    var nourTotalMB by remember { mutableStateOf(0f) }
    var nourSpeedMBs by remember { mutableStateOf(0f) }
    var nourDownloadedApkFile by remember { mutableStateOf<File?>(null) }
    var nourDownloadJob by remember { mutableStateOf<Job?>(null) }


    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.statusBars
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "إعدادات التطبيق",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        val isRtl = androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(if (isRtl) Alignment.CenterEnd else Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // HIJRI CALENDAR CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isHijriExpanded = !isHijriExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "التقويم الهجري",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${HijriCalendarManager.toArabicNumerals(hijriState.hijriDay.toString())} ${hijriState.hijriMonthName}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isHijriExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isHijriExpanded) "طي" else "توسيع",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isHijriExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            val dayText = HijriCalendarManager.toArabicNumerals(hijriState.hijriDay.toString())
                                            val yearText = HijriCalendarManager.toArabicNumerals(hijriState.hijriYear.toString())
                                            val dayPrefix = if (hijriState.dayName.isNotBlank()) "${hijriState.dayName} • " else ""
                                            Text(
                                                text = "$dayPrefix$dayText ${hijriState.hijriMonthName} $yearText هـ",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "${hijriState.gregorianDateFormatted} م",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val statusDesc = buildString {
                                                append(
                                                    when {
                                                        hijriState.isManualAdjusted -> "تعديل يدوي (${if (hijriState.manualOffsetDays > 0) "+" else ""}${HijriCalendarManager.toArabicNumerals(hijriState.manualOffsetDays.toString())} يوم)"
                                                        hijriState.isCriticalWindow -> "وضع تحقق الهلال التلقائي (أيام ٢٨–٣٠ وأول الشهر)"
                                                        hijriState.isSyncedFromRemote -> "مُحدّث تلقائياً من الرابط"
                                                        else -> "حساب محلي (بانتظار التحديث)"
                                                    }
                                                )
                                                if (hijriState.sourceLabel.isNotBlank()) {
                                                    append(" • ${hijriState.sourceLabel}")
                                                }
                                            }
                                            Text(
                                                text = statusDesc,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "آخر تحديث: ${HijriCalendarManager.formatLastFetchedRelative(hijriState.lastFetchedMillis)}",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    Text(
                                        text = "تعديل التقويم يدوياً (تقديم أو تأخير بالأيام):",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilledTonalIconButton(
                                            onClick = {
                                                HijriCalendarManager.adjustManualOffsetBy(context, -1)
                                            },
                                            enabled = hijriState.manualOffsetDays > -5,
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .testTag("hijri_minus_day_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "إنقاص يوم"
                                            )
                                        }

                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                        ) {
                                            val offsetLabel = when {
                                                hijriState.manualOffsetDays == 0 -> "تلقائي (٠ يوم)"
                                                hijriState.manualOffsetDays > 0 -> "+${HijriCalendarManager.toArabicNumerals(hijriState.manualOffsetDays.toString())} يوم"
                                                else -> "${HijriCalendarManager.toArabicNumerals(hijriState.manualOffsetDays.toString())} يوم"
                                            }
                                            Text(
                                                text = offsetLabel,
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        FilledTonalIconButton(
                                            onClick = {
                                                HijriCalendarManager.adjustManualOffsetBy(context, +1)
                                            },
                                            enabled = hijriState.manualOffsetDays < 5,
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .testTag("hijri_plus_day_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "زيادة يوم"
                                            )
                                        }
                                    }

                                    if (hijriState.manualOffsetDays != 0) {
                                        TextButton(
                                            onClick = {
                                                HijriCalendarManager.setManualOffset(context, 0)
                                                Toast.makeText(context, "تمت استعادة التقويم التلقائي", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            Text(
                                                text = "إرجاع للتقويم التلقائي (٠)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val result = HijriCalendarManager.refreshFromRemote(
                                                    context = context,
                                                    force = true,
                                                    resetManualOffset = true
                                                )
                                                if (result.isSuccess) {
                                                    Toast.makeText(context, "✅ تم تحديث التقويم الهجري من الرابط", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "تعذر التحديث من الرابط، تحقق من الإنترنت", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        enabled = !hijriState.isRefreshing,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("hijri_update_from_url_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        if (hijriState.isRefreshing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("جاري التحديث من الرابط...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("تحديث التقويم الآن عبر الرابط", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }

                                    hijriState.lastSyncError?.let { err ->
                                        Text(
                                            text = err,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.error,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "يتم تحديث التقويم تلقائياً وبشكل دائم من الرابط المعتمد، مع تكثيف التحقق التلقائي في آخر ٣ أيام من الشهر وأول يوم بعد الثلاثين لضمان دقة رؤية الهلال.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // NOTIFICATIONS CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isNotificationsExpanded = !isNotificationsExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "إعدادات الإشعارات",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (isNotificationsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isNotificationsExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            AnimatedVisibility(visible = isNotificationsExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Master Switch Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "تفعيل باقة إشعارات الآيات",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (notificationsEnabled) NotificationSettingsManager.getFrequencyDescription(notificationsPerHour) else "الإشعارات متوقفة حالياً",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (notificationsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Switch(
                                            checked = notificationsEnabled,
                                            onCheckedChange = { enabled ->
                                                notificationsEnabled = enabled
                                                NotificationSettingsManager.setNotificationsEnabled(context, enabled)
                                                if (enabled) {
                                                    NotificationScheduler.schedule(context, forceWorkUpdate = true)
                                                    Toast.makeText(context, "تم تفعيل الإشعارات: ${NotificationSettingsManager.getFrequencyDescription(notificationsPerHour)}", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    NotificationScheduler.cancel(context)
                                                    Toast.makeText(context, "تم إيقاف الإشعارات", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }

                                    if (notificationsEnabled) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                        // Frequency Header & Counter
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "وتيرة التذكير بالآيات في الساعة",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                ) {
                                                    Text(
                                                        text = "$notificationsPerHour إشعار / ساعة",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "الوتيرة الحالية: ${NotificationSettingsManager.getFrequencyDescription(notificationsPerHour)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        // Frequency Slider (1 to 20 notifications per hour)
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Slider(
                                                value = notificationsPerHour.toFloat(),
                                                onValueChange = { newValue ->
                                                    notificationsPerHour = newValue.toInt()
                                                },
                                                onValueChangeFinished = {
                                                    NotificationSettingsManager.setNotificationsPerHour(context, notificationsPerHour)
                                                    NotificationScheduler.schedule(context, forceWorkUpdate = true)
                                                    Toast.makeText(context, "تم حفظ الوتيرة: ${NotificationSettingsManager.getFrequencyDescription(notificationsPerHour)}", Toast.LENGTH_SHORT).show()
                                                },
                                                valueRange = 1f..20f,
                                                steps = 18,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "١ (كل ساعة)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "١٠ (كل ٦ دقائق)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "٢٠ (كل ٣ دقائق)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Quick Presets in 2-Column Spacious Grid
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "خيارات جاهزة وسريعة:",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            
                                            val presetPairs = listOf(
                                                (20 to "كل ٣ دقائق") to (12 to "كل ٥ دقائق"),
                                                (6 to "كل ١٠ دقائق") to (4 to "كل ربع ساعة"),
                                                (2 to "كل نصف ساعة") to (1 to "كل ساعة")
                                            )

                                            presetPairs.forEach { (firstPreset, secondPreset) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    // Item 1
                                                    PresetCard(
                                                        count = firstPreset.first,
                                                        timeDesc = firstPreset.second,
                                                        isSelected = (notificationsPerHour == firstPreset.first),
                                                        modifier = Modifier.weight(1f),
                                                        onClick = {
                                                            notificationsPerHour = firstPreset.first
                                                            NotificationSettingsManager.setNotificationsPerHour(context, firstPreset.first)
                                                            NotificationScheduler.schedule(context, forceWorkUpdate = true)
                                                            Toast.makeText(context, "تم اختيار: ${firstPreset.first} في الساعة (${firstPreset.second})", Toast.LENGTH_SHORT).show()
                                                        }
                                                    )
                                                    // Item 2
                                                    PresetCard(
                                                        count = secondPreset.first,
                                                        timeDesc = secondPreset.second,
                                                        isSelected = (notificationsPerHour == secondPreset.first),
                                                        modifier = Modifier.weight(1f),
                                                        onClick = {
                                                            notificationsPerHour = secondPreset.first
                                                            NotificationSettingsManager.setNotificationsPerHour(context, secondPreset.first)
                                                            NotificationScheduler.schedule(context, forceWorkUpdate = true)
                                                            Toast.makeText(context, "تم اختيار: ${secondPreset.first} في الساعة (${secondPreset.second})", Toast.LENGTH_SHORT).show()
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Test Immediate Notification Button
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val success = AyahNotificationHelper.showRandomAyahNotification(context, isTest = true)
                                                    if (success) {
                                                        Toast.makeText(context, "✅ تم إرسال إشعار قرآني تجريبي فوري", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "تعذر إرسال الإشعار، يرجى التأكد من صلاحية الإشعارات", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("🔔 إرسال إشعار تجريبي فوري الآن", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Background & Post-Reboot Reliability Controls
                                        val needsAction = !hasPostNotifPermission || !canScheduleExactAlarm || !isIgnoringBatteryOptimizations
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (needsAction) {
                                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                                            } else {
                                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                if (needsAction) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (needsAction) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = if (needsAction) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = if (needsAction) {
                                                            "لضمان استمرار الإشعارات بعد إطفاء الهاتف وإعادة تشغيله، يُنصح بتفعيل الصلاحيات التالية:"
                                                        } else {
                                                            "تعمل الإشعارات بأعلى موثوقية في الخلفية وتستأنف تلقائياً بعد إطفاء الهاتف أو إعادة تشغيله."
                                                        },
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        lineHeight = 18.sp
                                                    )
                                                }

                                                if (!hasPostNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                                            }
                                                            runCatching { context.startActivity(intent) }
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .testTag("open_notification_permission_button"),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("تفعيل إذن الإشعارات من النظام", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                if (!canScheduleExactAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                                data = Uri.parse("package:${context.packageName}")
                                                            }
                                                            runCatching { context.startActivity(intent) }.onFailure {
                                                                val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                                    data = Uri.parse("package:${context.packageName}")
                                                                }
                                                                runCatching { context.startActivity(fallback) }
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .testTag("open_exact_alarm_settings_button"),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("السماح بالتنبيهات الدقيقة (المنبهات والتذكيرات)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                if (!isIgnoringBatteryOptimizations && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            val batteryIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                                            runCatching { context.startActivity(batteryIntent) }.onFailure {
                                                                val appDetailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                                    data = Uri.parse("package:${context.packageName}")
                                                                }
                                                                runCatching { context.startActivity(appDetailsIntent) }
                                                            }
                                                            Toast.makeText(
                                                                context,
                                                                "اختر (صوت القرآن) ثم (بلا قيود) وفعّل التشغيل التلقائي إن وجد",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .testTag("open_battery_optimization_settings_button"),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Icon(Icons.Default.BatterySaver, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("استثناء من توفير البطارية (بلا قيود)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                                        // CARD 1: App Theme Selection Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isThemeCardExpanded = !isThemeCardExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "مظهر وثيمات التطبيق",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(ThemeManager.currentThemeMode.primaryColor)
                                            )
                                            Text(
                                                text = if (ThemeManager.currentThemeMode.isDark) "داكن" else "فاتح",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = if (isThemeCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isThemeCardExpanded) "طي" else "توسيع",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isThemeCardExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "اختر الثيم والمظهر المفضل للواجهة والقراءة:",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    val modes = AppThemeMode.entries
                                    modes.chunked(2).forEach { rowModes ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowModes.forEach { mode ->
                                                val isSelected = ThemeManager.currentThemeMode == mode
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { ThemeManager.setTheme(context, mode) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    border = BorderStroke(
                                                        if (isSelected) 2.dp else 1.dp,
                                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(CircleShape)
                                                                .background(mode.backgroundColor)
                                                                .border(1.dp, mode.primaryColor, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(7.dp)
                                                                    .clip(CircleShape)
                                                                    .background(mode.primaryColor)
                                                            )
                                                        }

                                                        Text(
                                                            text = mode.titleAr.split(" ").firstOrNull() ?: mode.titleAr,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(1f),
                                                            maxLines = 1
                                                        )

                                                        if (isSelected) {
                                                            Icon(
                                                                imageVector = Icons.Default.CheckCircle,
                                                                contentDescription = "محدد",
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            if (rowModes.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // CARD 2: Quran App Update Card (تحديث تطبيق صوت القرءان)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isQuranUpdateExpanded = !isQuranUpdateExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تحديث تطبيق صوت القرءان",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector = if (isQuranUpdateExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isQuranUpdateExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            AnimatedVisibility(visible = isQuranUpdateExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "الحالي: $quranCurrentVersion",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text(
                                                text = "القادم: $quranUpcomingVersion",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    when (quranUpdateStatus) {
                                        UpdateStatus.IDLE -> {
                                            Text(
                                                text = "اضغط للبحث عن تحديثات أحدث لتطبيق صوت القرءان عبر GitHub API",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        quranUpdateStatus = UpdateStatus.CHECKING
                                                        val result = AppUpdateManager.checkQuranUpdate()
                                                        val info = result.getOrNull()
                                                        if (info != null) {
                                                            quranUpdateInfo = info
                                                            quranUpcomingVersion = info.tagName
                                                            if (info.isNewerAvailable) {
                                                                quranUpdateStatus = UpdateStatus.AVAILABLE
                                                            } else {
                                                                quranUpdateStatus = UpdateStatus.UP_TO_DATE
                                                            }
                                                        } else {
                                                            quranUpdateStatus = UpdateStatus.UP_TO_DATE
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "افحص عن تحديثات",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }

                                        UpdateStatus.CHECKING -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "جاري الفحص عن إصدارات أحدث...",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        UpdateStatus.AVAILABLE -> {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "🎉 تم العثور على إصدار جديد أحدث ($quranUpcomingVersion)!",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Text(
                                                        text = quranUpdateInfo?.releaseNotes ?: "يتضمن التحديث الجديد تحسينات في التلاوات والأداء وإصلاحات عامة.",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    quranDownloadJob = coroutineScope.launch {
                                                        quranUpdateStatus = UpdateStatus.DOWNLOADING
                                                        quranDownloadProgress = 0f
                                                        quranDownloadedMB = 0f

                                                        val downloadUrl = quranUpdateInfo?.downloadUrl ?: AppUpdateManager.QURAN_DIRECT_APK
                                                        val cleanVersionTag = quranUpcomingVersion.trim().removePrefix("v").removePrefix("V")
                                                        val fileName = "Sawt_AL-Quran_v$cleanVersionTag.apk"

                                                        AppUpdateManager.downloadApkFile(
                                                            context = context,
                                                            downloadUrl = downloadUrl,
                                                            fileName = fileName,
                                                            onProgress = { state ->
                                                                when (state) {
                                                                    is DownloadState.Progress -> {
                                                                        quranDownloadProgress = state.progressPercent / 100f
                                                                        quranDownloadedMB = state.downloadedBytes / (1024f * 1024f)
                                                                        quranTotalMB = state.totalBytes / (1024f * 1024f)
                                                                        quranSpeedMBs = state.speedMBs
                                                                    }
                                                                    is DownloadState.Completed -> {
                                                                        quranDownloadedApkFile = state.apkFile
                                                                        quranUpdateStatus = UpdateStatus.DOWNLOADED
                                                                        installApk(context, state.apkFile)
                                                                    }
                                                                    is DownloadState.Error -> {
                                                                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                                                                        quranUpdateStatus = UpdateStatus.AVAILABLE
                                                                    }
                                                                    else -> {}
                                                                }
                                                            }
                                                        )
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.GetApp,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "تحميل وتثبيت التحديث الآن",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }

                                            TextButton(
                                                onClick = {
                                                    AppUpdateManager.postponeQuranVersion(context, quranUpcomingVersion)
                                                    quranUpdateStatus = UpdateStatus.UP_TO_DATE
                                                    Toast.makeText(context, "تم تأجيل التحديث", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Text(
                                                    text = "تأجيل التحديث",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        UpdateStatus.DOWNLOADING -> {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "جاري تحميل تحديث صوت القرءان...",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "${(quranDownloadProgress * 100).toInt()}%",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                LinearProgressIndicator(
                                                    progress = { quranDownloadProgress },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .clip(RoundedCornerShape(4.dp)),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = String.format(Locale.US, "السرعة: %.1f ميغابايت/ث", quranSpeedMBs),
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = String.format(Locale.US, "%.1f / %.1f ميغابايت", quranDownloadedMB, quranTotalMB),
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                // Stop Download Button (زر إيقاف التحميل)
                                                OutlinedButton(
                                                    onClick = {
                                                        quranDownloadJob?.cancel()
                                                        quranDownloadJob = null
                                                        quranUpdateStatus = UpdateStatus.AVAILABLE
                                                        Toast.makeText(context, "تم إيقاف تحميل تحديث صوت القرءان", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error
                                                    ),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Cancel,
                                                        contentDescription = "إيقاف",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "إيقاف التحميل",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        UpdateStatus.DOWNLOADED -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "✅ اكتمل تحميل الملف التحديث بنجاح!",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.Center
                                                )
                                                Button(
                                                    onClick = {
                                                        quranDownloadedApkFile?.let { apk ->
                                                            installApk(context, apk)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.SystemUpdate,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "تثبيت تحديث التطبيق الآن",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }

                                        UpdateStatus.UP_TO_DATE -> {
                                            Text(
                                                text = "تطبيقك محدّث بأحدث إصدار متوفر بالفعل ($quranCurrentVersion)",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // CARD 3: Nour Al-Itrah App (تطبيق نور العترة)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isNourUpdateExpanded = !isNourUpdateExpanded
                                        if (isNourUpdateExpanded && nourUpdateStatus == UpdateStatus.IDLE) {
                                            coroutineScope.launch {
                                                nourUpdateStatus = UpdateStatus.CHECKING
                                                val result = AppUpdateManager.checkNourUpdate()
                                                val info = result.getOrNull()
                                                if (info != null) {
                                                    nourUpdateInfo = info
                                                    nourUpcomingVersion = info.tagName
                                                    if (info.isNewerAvailable) {
                                                        nourUpdateStatus = UpdateStatus.AVAILABLE
                                                    } else {
                                                        nourUpdateStatus = UpdateStatus.UP_TO_DATE
                                                    }
                                                } else {
                                                    nourUpdateStatus = UpdateStatus.UP_TO_DATE
                                                }
                                            }
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تحديث تطبيق نور العترة",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector = if (isNourUpdateExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isNourUpdateExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            AnimatedVisibility(visible = isNourUpdateExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(
                                            text = if (nourUpcomingVersion.isNotEmpty()) "الإصدار المتوفر: $nourUpcomingVersion" else "الإصدار المتوفر: جاري التحقق...",
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    when (nourUpdateStatus) {
                                        UpdateStatus.IDLE -> {
                                            Text(
                                                text = "اضغط للبحث عن تحديثات أحدث لتطبيق نور العترة عبر GitHub API",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        nourUpdateStatus = UpdateStatus.CHECKING
                                                        val result = AppUpdateManager.checkNourUpdate()
                                                        val info = result.getOrNull()
                                                        if (info != null) {
                                                            nourUpdateInfo = info
                                                            nourUpcomingVersion = info.tagName
                                                            if (info.isNewerAvailable) {
                                                                nourUpdateStatus = UpdateStatus.AVAILABLE
                                                            } else {
                                                                nourUpdateStatus = UpdateStatus.UP_TO_DATE
                                                            }
                                                        } else {
                                                            nourUpdateStatus = UpdateStatus.UP_TO_DATE
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "افحص عن تحديثات",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }

                                        UpdateStatus.CHECKING -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "جاري الفحص عن إصدارات أحدث...",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        UpdateStatus.AVAILABLE -> {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "🎉 تم العثور على إصدار جديد أحدث ($nourUpcomingVersion)!",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Text(
                                                        text = nourUpdateInfo?.releaseNotes ?: "يتضمن التحديث الجديد تحسينات في التلاوات والأداء وإصلاحات عامة.",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    nourDownloadJob = coroutineScope.launch {
                                                        nourUpdateStatus = UpdateStatus.DOWNLOADING
                                                        nourDownloadProgress = 0f
                                                        nourDownloadedMB = 0f

                                                        val downloadUrl = nourUpdateInfo?.downloadUrl ?: AppUpdateManager.NOUR_DIRECT_APK
                                                        val fileName = "Noor_Al-Atra_v${nourUpcomingVersion}.apk"

                                                        AppUpdateManager.downloadApkFile(
                                                            context = context,
                                                            downloadUrl = downloadUrl,
                                                            fileName = fileName,
                                                            onProgress = { state ->
                                                                when (state) {
                                                                    is DownloadState.Progress -> {
                                                                        nourDownloadProgress = state.progressPercent / 100f
                                                                        nourDownloadedMB = state.downloadedBytes / (1024f * 1024f)
                                                                        nourTotalMB = state.totalBytes / (1024f * 1024f)
                                                                        nourSpeedMBs = state.speedMBs
                                                                    }
                                                                    is DownloadState.Completed -> {
                                                                        nourDownloadedApkFile = state.apkFile
                                                                        nourUpdateStatus = UpdateStatus.DOWNLOADED
                                                                        installApk(context, state.apkFile)
                                                                    }
                                                                    is DownloadState.Error -> {
                                                                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                                                                        nourUpdateStatus = UpdateStatus.AVAILABLE
                                                                    }
                                                                    else -> {}
                                                                }
                                                            }
                                                        )
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.GetApp,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "تحميل وتثبيت التحديث الآن",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }

                                            TextButton(
                                                onClick = {
                                                    AppUpdateManager.postponeNourVersion(context, nourUpcomingVersion)
                                                    nourUpdateStatus = UpdateStatus.UP_TO_DATE
                                                    Toast.makeText(context, "تم تأجيل التحديث", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Text(
                                                    text = "تأجيل التحديث",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        UpdateStatus.DOWNLOADING -> {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "جاري تحميل تحديث نور العترة...",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "${(nourDownloadProgress * 100).toInt()}%",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                LinearProgressIndicator(
                                                    progress = { nourDownloadProgress },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .clip(RoundedCornerShape(4.dp)),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = String.format(Locale.US, "السرعة: %.1f ميغابايت/ث", nourSpeedMBs),
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = String.format(Locale.US, "%.1f / %.1f ميغابايت", nourDownloadedMB, nourTotalMB),
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                // Stop Download Button (زر إيقاف التحميل)
                                                OutlinedButton(
                                                    onClick = {
                                                        nourDownloadJob?.cancel()
                                                        nourDownloadJob = null
                                                        nourUpdateStatus = UpdateStatus.AVAILABLE
                                                        Toast.makeText(context, "تم إيقاف تحميل تحديث نور العترة", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error
                                                    ),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Cancel,
                                                        contentDescription = "إيقاف",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "إيقاف التحميل",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        UpdateStatus.DOWNLOADED -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "✅ اكتمل تحميل الملف التحديث بنجاح!",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.Center
                                                )
                                                Button(
                                                    onClick = {
                                                        nourDownloadedApkFile?.let { apk ->
                                                            installApk(context, apk)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.SystemUpdate,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "تثبيت تطبيق نور العترة الآن",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }

                                        UpdateStatus.UP_TO_DATE -> {
                                            Text(
                                                text = "الإصدار المتوفر حالياً على الرابط هو ($nourUpcomingVersion)",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                                        // CARD 4: Developer Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isDeveloperCardExpanded = !isDeveloperCardExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "قسم المطور",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (isDeveloperCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isDeveloperCardExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AnimatedVisibility(visible = isDeveloperCardExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "المطور المهندس محمد شهيد",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(
                                                    android.content.Intent.ACTION_VIEW,
                                                    android.net.Uri.parse("https://wa.me/9647831452279")
                                                )
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "تعذر فتح واتساب", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = androidx.compose.ui.graphics.Color(0xFF25D366),
                                            contentColor = androidx.compose.ui.graphics.Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Chat,
                                                contentDescription = "واتساب",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "واتساب: 9647831452279+",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(AppUpdateManager.QURAN_REPO_URL)
                                                )
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "تعذر فتح رابط GitHub", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.testTag("developer_github_repo_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = "GitHub",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "مستودع التطبيق على GitHub",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // App Info & Copyright Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "عن التطبيق",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                    Text(
                                        text = "تطبيق صوت القرآن",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "تطبيق القرآن الكريم الصوتي للبث والتحميل والاستماع لمشاهير القراء",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "الإصدار: $quranCurrentVersion",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "جميع الحقوق محفوظة © تطبيق صوت القرآن",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "2026",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                        }
                    }

                    // Extra Bottom Spacing outside cards to keep entire dialog scrollable above Android navigation bar
                    Spacer(modifier = Modifier.height(100.dp))
                }
        }
    }
}
}

@Composable
private fun PresetCard(
    count: Int,
    timeDesc: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (count == 1) "إشعار واحد / ساعة" else if (count == 2) "إشعاران / ساعة" else "$count إشعارات / ساعة",
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeDesc,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

