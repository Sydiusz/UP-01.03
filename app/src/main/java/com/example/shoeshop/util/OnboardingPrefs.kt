package com.example.shoeshop.util

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "onboarding_prefs"
private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

fun isOnboardingCompleted(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
}

fun setOnboardingCompleted(context: Context, completed: Boolean) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit {
        putBoolean(KEY_ONBOARDING_COMPLETED, completed)
    }
}
