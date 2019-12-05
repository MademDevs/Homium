package de.madem.homium.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
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

//fragment toast
fun Fragment.showToastShort(string: String) = context.showToastShort(string)
fun Fragment.showToastLong(string: String) = context.showToastLong(string)

fun Fragment.showToastShort(resource: Int, vararg arguments: Any) =
    context.showToast(Toast.LENGTH_SHORT, resource, arguments)

fun Fragment.showToastLong(resource: Int, vararg arguments: Any) =
    context.showToast(Toast.LENGTH_LONG, resource, arguments)

//activity/conext toast
fun Context?.showToastShort(resource: Int, vararg arguments: Any) =
    showToast(Toast.LENGTH_SHORT, resource, arguments)

fun Context?.showToastLong(resource: Int, vararg arguments: Any) =
    showToast(Toast.LENGTH_LONG, resource, arguments)

fun Context?.showToastShort(string: String) {
    showToast(Toast.LENGTH_SHORT, R.string.dummy, string)
}

fun Context?.showToastLong(string: String) {
    showToast(Toast.LENGTH_LONG, R.string.dummy, string)
}

private fun Context?.showToast(duration: Int, resource: Int, vararg arguments: Any) = notNull {
    Toast.makeText(it, it.getString(resource, arguments), duration).show()
}

fun <T : Any> Context.getSetting(key: String, type: KClass<T>): T? {
    //getting shared preferences
    val prefs = this.getSharedPreferences(
        resources.getString(R.string.sharedprefernce_namespacekey_settings),
        Context.MODE_PRIVATE
    )

    //getting setting depending on type
    return when (type) {
        Int::class -> prefs.getInt(key, 0) as T
        Boolean::class -> prefs.getBoolean(key, false) as T
        Float::class -> prefs.getFloat(key, 0f) as T
        Double::class -> prefs.getFloat(key, 0f).toDouble() as T
        Long::class -> prefs.getLong(key, 0) as T
        String::class -> prefs.getString(key, "") as T
        else -> null
    }
}

fun <T : Any> Context.putSetting(key: String, value: T): Boolean {
    //getting shared preferences
    val prefs = this.getSharedPreferences(
        resources.getString(R.string.sharedprefernce_namespacekey_settings),
        Context.MODE_PRIVATE
    )

    var result = false

    prefs.edit(false) {
        result = when (value) {
            is String -> {
                putString(key, value)
                commit()
            }
            is Boolean -> {
                putBoolean(key, value)
                commit()
            }
            is Int -> {
                putInt(key, value)
                commit()
            }
            is Float -> {
                putFloat(key, value)
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

//settings for fragment
fun <T : Any> Fragment.putSetting(key: String, value: T): Boolean {
    var result = false

    this.context.notNull {
        it.putSetting(key, value)
        result = true
    }

    return result
}

fun <T : Any> Fragment.getSetting(key: String, type: KClass<T>): T? {

    var result: T? = null

    this.context.notNull {
        result = it.getSetting(key, type)
    }

    return result
}


fun <T : Any> Fragment.switchToActivityForResult(requestCode: Int, clazz: KClass<T>) {
    startActivityForResult(Intent(context, clazz.java), requestCode)
}

fun Activity.finishWithBooleanResult(key: String, value: Boolean, resultCode: Int) {
    val resultIntent = Intent()
    resultIntent.putExtra(key, value)

    setResult(resultCode, resultIntent)
    finish()
}


fun Activity.vibrate() = vibrateInContext()
fun Fragment.vibrate() = context?.vibrateInContext()

private fun Context.vibrateInContext() {
    val vib = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
        vib?.vibrate(30)
    } else {
        vib?.vibrate(VibrationEffect.createOneShot(30, 10))
    }
}