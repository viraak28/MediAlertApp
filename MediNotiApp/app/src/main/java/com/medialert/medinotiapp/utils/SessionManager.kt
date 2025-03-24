package com.medialert.medinotiapp.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun saveSession(userId: Int) {
        prefs.edit().putInt("userId", userId).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getInt("userId", -1) != -1
    }

    fun getUserId(): Int {
        return prefs.getInt("userId", -1)
    }
}

