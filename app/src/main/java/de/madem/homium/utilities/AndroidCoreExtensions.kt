package de.madem.homium.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import de.madem.homium.R
import kotlin.reflect.KClass

fun <T : Any> Fragment.switchToActivity(clazz: KClass<T>) {
    startActivity(Intent(context, clazz.java))
}

fun <T : Any> Activity.switchToActivity(clazz: KClass<T>) {
    startActivity(Intent(this, clazz.java))
}

fun <T : Any> Activity.getSetting(key : String, type : KClass<T>) : T?{
    //getting shared preferences
    val prefs = this.getSharedPreferences(resources.getString(R.string.sharedprefernce_namespacekey_settings),Context.MODE_PRIVATE)

    //getting setting depending on type
    return when(type){
        Int::class -> prefs.getInt(key,0) as T
        Boolean::class -> prefs.getBoolean(key,false) as T
        Float::class -> prefs.getFloat(key,0f) as T
        Double::class -> prefs.getFloat(key,0f).toDouble() as T
        Long::class -> prefs.getLong(key,0) as T
        String::class -> prefs.getString(key,"") as T
        else -> null
    }
}

fun <T : Any> Activity.putSetting(key : String, value : T) : Boolean{
    //getting shared preferences
    val prefs = this.getSharedPreferences(resources.getString(R.string.sharedprefernce_namespacekey_settings),Context.MODE_PRIVATE)

    var result : Boolean = false

    prefs.edit(false){
         result = when(value){
            is String -> {
                putString(key, value)
                commit()
            }
            is Boolean -> {
                putBoolean(key,value)
                commit()
            }
            is Int -> {
                putInt(key,value)
                commit()
            }
            is Float -> {
                putFloat(key,value)
                commit()
            }
            is Double -> {
                putFloat(key, value.toFloat())
                commit()
            }
            is Long -> {
                putLong(key, value)
                commit()
            }

            else -> false
        }
    }

    return result

}

fun Context.vibrate() {
    val vib = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O){
        vib?.vibrate(30)
    }
    else{
        vib?.vibrate(VibrationEffect.createOneShot(30,10))
    }
}