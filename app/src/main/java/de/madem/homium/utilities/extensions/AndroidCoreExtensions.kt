package de.madem.homium.utilities.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import de.madem.homium.R
import de.madem.homium.constants.SHAREDPREFERENCE_NAMESPACEKEY_SETTINGS
import de.madem.homium.utilities.android_utilities.SearchViewHandler
import de.madem.homium.utilities.pictures.BitmapUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

fun <T : Any> Fragment.switchToActivity(clazz: KClass<T>) {
    startActivity(Intent(context, clazz.java))
}

fun <T : Any> Activity.switchToActivity(clazz: KClass<T>) {
    startActivity(Intent(this, clazz.java))
}

fun <T : Any> Fragment.switchToActivity(clazz: KClass<T>,optionFunc: (Intent) -> Unit) {
    val intent : Intent = Intent(context,clazz.java)
    optionFunc.invoke(intent)
    startActivity(intent)
}

fun <T : Any> Activity.switchToActivity(clazz: KClass<T>,optionFunc: (Intent) -> Unit) {
    val intent : Intent = Intent(this,clazz.java)
    optionFunc.invoke(intent)
    startActivity(intent)
}

fun <T : Any> Context.switchToActivity(clazz: KClass<T>,optionFunc: (Intent) -> Unit) {
    val intent : Intent = Intent(this,clazz.java)
    optionFunc.invoke(intent)
    startActivity(intent)
}

fun AppCompatActivity.hideKeyboard() {
    val view = this.currentFocus
    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
}

fun ImageView.setPictureFromPath(path: String?, reqWidth: Int = 400, reqHeight: Int = 400) {
    if(path.isNullOrEmpty()) {
        setImageResource(R.mipmap.empty_picture)
    } else {
        val bitmap = BitmapUtil.loadBitmapFromPath(path, reqWidth, reqHeight)

        setImageBitmap(bitmap)
    }
}


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
        SHAREDPREFERENCE_NAMESPACEKEY_SETTINGS,
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
        SHAREDPREFERENCE_NAMESPACEKEY_SETTINGS,
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

fun <T : Any> Activity.switchToActivityForResult(requestCode: Int, clazz: KClass<T>)
        = switchToActivityForResult(requestCode,clazz){}

fun <T : Any> Activity.switchToActivityForResult(requestCode: Int,clazz: KClass<T>,modifyIntent: (Intent)->Unit){
    val intent = Intent(this,clazz.java)
    modifyIntent.invoke(intent)
    startActivityForResult(intent, requestCode)
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

fun Activity.finishWithResultData(resultCode: Int,modifyIntent : (Intent)->Unit){
    val resultIntent = Intent()

    modifyIntent.invoke(resultIntent)
    setResult(resultCode, resultIntent)
    finish()
}

fun Context.toAppCompatActivityOrNull() : AppCompatActivity?{
    return if(this is AppCompatActivity) this as AppCompatActivity else null
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

/*
fun Bundle.putIngredient(key: String, value: RecipeEditActivity.Ingredient) {
    //creating bundle
    val bundle = bundleOf()
    //adding data to bundle
    bundle.putInt("id",value.id)
    bundle.putString("name",value.name)
    bundle.putInt("count",value.count)
    bundle.putString("unit",value.unit)

    //putting bundle
    this.putBundle(key, bundle)
}

 */



fun ViewGroup.inflater() = LayoutInflater.from(context)

fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner?, onUpdate: (T) -> Unit) {
    if (lifecycleOwner != null) {
        observe(lifecycleOwner, Observer(onUpdate))
    }
}

fun <T> LiveData<T>.applyAndObserver(lifecycleOwner: LifecycleOwner?, initialValue: T?, onUpdate: (T) -> Unit) {
    initialValue?.let { onUpdate(initialValue) }
    observe(lifecycleOwner, onUpdate)
}

fun Fragment?.whenSearchViewHandler(action: (SearchViewHandler) -> Unit){
    val s = this as? SearchViewHandler
    s.notNull {
        action.invoke(it)
    }
}

fun <T> Flow<T>.onCollect(viewLifecycleOwner: LifecycleOwner, onEmit: suspend (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        this@onCollect.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { onEmit(it) }
    }
}

fun EditText.setDistinctText(text: CharSequence){
    if(this.text.toString() != text.toString()) {
        this.setText(text)
    }
}

fun NumberPicker.setDistinctValue(value: Int) {
    if(this.value != value) {
        this.value = value
    }
}

fun NumberPicker.setDistinctDisplayedValues(values: Array<String>) {
    if(values === displayedValues || this.displayedValues.contentEquals(values)) {
        return
    }
    this.displayedValues = values
}

fun View.setDistinctVisibility(visibility: Int) {
    if(this.visibility != visibility) {
        this.visibility = visibility
    }
}