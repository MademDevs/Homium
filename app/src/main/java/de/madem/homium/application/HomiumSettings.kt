package de.madem.homium.application

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import de.madem.homium.R
import de.madem.homium.constants.*
import de.madem.homium.utilities.extensions.getSetting
import de.madem.homium.utilities.extensions.notNull
import de.madem.homium.utilities.extensions.putSetting

object HomiumSettings {
    var vibrationEnabled : Boolean = true
    var shoppingSort = SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_NORMAL
    var shoppingToInventory = R.id.radio_check_question
    var speechAssistantDeleteQuestion = true
    var appTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    fun initialize(context: Context){
        with(context){
            vibrationEnabled = getSetting(SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_VIBRATION_ENABLED,
                Boolean::class) ?: true

            shoppingSort = getSetting(SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_SORT,
                String::class) ?: SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_NORMAL

            shoppingToInventory = getSetting(
                SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_TO_INVENTORY, Int::class) ?: R.id.radio_check_question

            speechAssistantDeleteQuestion = getSetting(
                SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_DELETE_QUESTION_SPEECH_ASSISTENT_ALLOWED, Boolean::class) ?: true

            appTheme = getSetting(SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_APP_THEME,Int::class)
                ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

            if(appTheme == 0){
                appTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
    }

    fun saveSettings(context: Context? = HomiumApplication.appContext){
        context.notNull {
            with(it){
                putSetting<Boolean>(SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_VIBRATION_ENABLED,
                    vibrationEnabled)
                putSetting<String>(SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_SORT,
                    shoppingSort)

                putSetting<Int>(SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_TO_INVENTORY, shoppingToInventory)

                putSetting(
                    SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_DELETE_QUESTION_SPEECH_ASSISTENT_ALLOWED,
                    speechAssistantDeleteQuestion)

                putSetting(SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_APP_THEME, appTheme)
            }
        }
    }

    override fun toString(): String {
        return "HOMIUM SETTINGS:\nvibration enabled:$vibrationEnabled" +
                "\nShopping Sort: $shoppingSort\nshoppingToInventory$shoppingToInventory\n" +
                "speech assistent delete question allowed: $speechAssistantDeleteQuestion"+
                "\nTheme_ID: $appTheme"

    }
}