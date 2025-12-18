package com.example.shoeshop.util

import android.content.Context

private const val PREFS_NAME = "recovery_prefs"
private const val KEY_EMAIL = "recovery_email"

fun saveUserEmail(context: Context, email: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_EMAIL, email).apply()
}
fun getUserEmail(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_EMAIL, "") ?: ""
}