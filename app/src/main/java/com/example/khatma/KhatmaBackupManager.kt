package com.example.khatma

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * مدير النسخ الاحتياطي الدائم لتقدم الختمة القرآنية في SharedPreferences
 * لضمان عدم ضياع تقدم الأجزاء حتى عند تحديث التطبيق أو إعادة تهيئة قاعدة البيانات.
 */
object KhatmaBackupManager {
    private const val TAG = "KhatmaBackupManager"
    private const val PREF_NAME = "quran_khatma_persistent_backup"
    private const val KEY_KHATMA_JSON = "khatma_progress_backup_json"
    private const val KEY_LAST_BACKUP_TIME = "last_backup_timestamp"

    fun hasBackup(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_KHATMA_JSON)
    }

    fun loadBackup(context: Context): List<KhatmaProgress> {
        val result = mutableListOf<KhatmaProgress>()
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_KHATMA_JSON, null) ?: return emptyList()
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val item = KhatmaProgress(
                    juzNumber = obj.getInt("juzNumber"),
                    lastReadPage = obj.optInt("lastReadPage", 0),
                    progressPercent = obj.optInt("progressPercent", 0),
                    isCompleted = obj.optBoolean("isCompleted", false),
                    lastUpdatedAt = obj.optLong("lastUpdatedAt", System.currentTimeMillis())
                )
                result.add(item)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading Khatma backup", e)
        }
        return result
    }

    fun saveBackup(context: Context, items: List<KhatmaProgress>) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            for (item in items) {
                val obj = JSONObject().apply {
                    put("juzNumber", item.juzNumber)
                    put("lastReadPage", item.lastReadPage)
                    put("progressPercent", item.progressPercent)
                    put("isCompleted", item.isCompleted)
                    put("lastUpdatedAt", item.lastUpdatedAt)
                }
                jsonArray.put(obj)
            }
            prefs.edit()
                .putString(KEY_KHATMA_JSON, jsonArray.toString())
                .putLong(KEY_LAST_BACKUP_TIME, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving Khatma backup", e)
        }
    }

    fun saveSingleJuz(context: Context, item: KhatmaProgress) {
        try {
            val current = loadBackup(context).toMutableList()
            val index = current.indexOfFirst { it.juzNumber == item.juzNumber }
            if (index >= 0) {
                current[index] = item
            } else {
                current.add(item)
            }
            saveBackup(context, current)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating single Juz in backup", e)
        }
    }

    fun clearBackup(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing Khatma backup", e)
        }
    }
}
