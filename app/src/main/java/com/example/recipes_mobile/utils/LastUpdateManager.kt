package com.example.recipes_mobile.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object LastUpdateManager {
    private const val PREF_NAME = "last_update_prefs"
    private const val KEY_LAST_UPDATED = "key_last_updated"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getLastUpdated(context: Context): Long {
        return getPreferences(context).getLong(KEY_LAST_UPDATED, 0L)
    }

    fun setLastUpdated(context: Context, timestamp: Long) {
        getPreferences(context).edit().putLong(KEY_LAST_UPDATED, timestamp).apply()
        Log.d("test time", "updated time: ${getPreferences(context).getLong(KEY_LAST_UPDATED, 0L)} ")
    }
}
