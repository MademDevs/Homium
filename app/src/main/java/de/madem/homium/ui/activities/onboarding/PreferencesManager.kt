package de.madem.homium.ui.activities.onboarding

import android.content.Context
import android.content.SharedPreferences


//for checking if app is started for the first time
//otherwise onboardingactivity will start everytime the app is started
@Deprecated("Functionality has been replaced by extension functions")
class PreferencesManager(context: Context) {
    private val preferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        preferences = context.getSharedPreferences(PREFERENCE_NAME, PRIVATE_MODE)
        editor = preferences.edit()
    }

    fun isFirstRun() = preferences.getBoolean(FIRST_TIME, true)

    fun setFirstRun() {
        editor.putBoolean(FIRST_TIME, false).commit()
        editor.commit()
    }

    companion object {
        private const val PRIVATE_MODE = 0
        private const val PREFERENCE_NAME = "configuration"
        private const val FIRST_TIME = "isFirstRun"
    }
}