package com.example.notificc

import android.content.Context
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

object NotificationStorage {
    private const val PREF_NAME = "notification_prefs"
    private const val KEY_HISTORY = "notification_history"

    fun saveNotification(context: Context, entry: NotificationEntry) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentList = getNotificationHistory(context).toMutableList()
        currentList.add(0, entry) // terbaru di atas

        val json = Gson().toJson(currentList)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun getNotificationHistory(context: Context): List<NotificationEntry> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<NotificationEntry>>() {}.type
                Gson().fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun clearNotificationHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
