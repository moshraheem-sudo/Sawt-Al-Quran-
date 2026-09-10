package com.example.data.local

import android.content.Context

object NotificationSettingsManager {
    private const val PREF_NAME = "quran_notification_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_NOTIFICATIONS_PER_HOUR = "notifications_per_hour"

    fun areNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    /**
     * Number of notifications per hour (Range: 1 to 20). Default is 2 (every 30 minutes).
     */
    fun getNotificationsPerHour(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_NOTIFICATIONS_PER_HOUR, 2).coerceIn(1, 20)
    }

    fun setNotificationsPerHour(context: Context, count: Int) {
        val validCount = count.coerceIn(1, 20)
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_NOTIFICATIONS_PER_HOUR, validCount).apply()
    }

    /**
     * Calculates the interval in minutes based on notifications per hour.
     * E.g.:
     * 20 -> 3 minutes
     * 15 -> 4 minutes
     * 12 -> 5 minutes
     * 10 -> 6 minutes
     * 6  -> 10 minutes
     * 4  -> 15 minutes
     * 3  -> 20 minutes
     * 2  -> 30 minutes
     * 1  -> 60 minutes
     */
    fun getIntervalMinutes(context: Context): Int {
        val count = getNotificationsPerHour(context)
        return when (count) {
            20 -> 3
            19 -> 3
            18 -> 3
            17 -> 3
            16 -> 3
            15 -> 4
            14 -> 4
            13 -> 4
            12 -> 5
            11 -> 5
            10 -> 6
            9  -> 6
            8  -> 7
            7  -> 8
            6  -> 10
            5  -> 12
            4  -> 15
            3  -> 20
            2  -> 30
            1  -> 60
            else -> (60 / count).coerceAtLeast(3)
        }
    }

    fun getFrequencyDescription(count: Int): String {
        return when (count) {
            20 -> "20 إشعاراً في الساعة (إشعار كل 3 دقائق)"
            15 -> "15 إشعاراً في الساعة (إشعار كل 4 دقائق)"
            12 -> "12 إشعاراً في الساعة (إشعار كل 5 دقائق)"
            10 -> "10 إشعارات في الساعة (إشعار كل 6 دقائق)"
            6  -> "6 إشعارات في الساعة (إشعار كل 10 دقائق)"
            4  -> "4 إشعارات في الساعة (إشعار كل 15 دقيقة)"
            3  -> "3 إشعارات في الساعة (إشعار كل 20 دقيقة)"
            2  -> "إشعاران في الساعة (إشعار كل 30 دقيقة)"
            1  -> "إشعار واحد في الساعة (إشعار كل 60 دقيقة)"
            else -> {
                val mins = (60 / count).coerceAtLeast(3)
                "$count إشعارات في الساعة (تقريباً كل $mins دقائق)"
            }
        }
    }
}
