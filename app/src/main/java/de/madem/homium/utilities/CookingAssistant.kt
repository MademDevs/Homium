package de.madem.homium.utilities

import android.content.Context
import de.madem.homium.databases.AppDatabase
import java.lang.ref.WeakReference

/*
 * This class is made for cooking elements in Homium and deals with certain operations in Database.
 *
 * */


class CookingAssistant(private val contextReference: WeakReference<Context>) {
    val recipeDao = AppDatabase.getInstance().recipeDao()
    val inventoryDao = AppDatabase.getInstance().recipeDao()
}