package de.madem.homium.utilities

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

fun <T : Any> Fragment.switchToActivity(clazz: KClass<T>) {
    startActivity(Intent(context, clazz.java))
}

fun <T : Any> Activity.switchToActivity(clazz: KClass<T>) {
    startActivity(Intent(this, clazz.java))
}