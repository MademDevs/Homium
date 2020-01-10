package de.madem.homium.utilities

import android.content.Context
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.ShoppingItem
import java.lang.ref.WeakReference

/*
 * The class InventoryQuantityOperationInformation encapsulates an information for id,unit and quantity
 * returned after calculating with quantities (= Infomation what is the result of a quantity calculation with
 * a certain Inventoryitem)
 *
 * */
inline class InventoryQuantityOperationInformation(val info: Triple<Int,Int,String>)

/*
 * The class CookingAssistant is made for cooking elements in Homium and deals with certain operations in Database.
 *
 * */


class CookingAssistant(private val contextReference: WeakReference<Context>) {

    //fields
    private val recipeDao = AppDatabase.getInstance().recipeDao()
    private val inventoryDao = AppDatabase.getInstance().recipeDao()
    private val shoppingDao = AppDatabase.getInstance().itemDao()

    //public functions
    fun cookRecipe(recipe: Recipe){
        //analyze
        val analysisResult = analyzeRecipe(recipe)

        //decide whether to insert shopping items or to subtract from inventory after user decision
        //TODO: Implement logic for inserting shopping items
        //TODO: Implement logic for subtracting from inventory
    }


    //private functions
    private fun analyzeRecipe(recipe : Recipe) : List<ShoppingItem>{
        //TODO: create code for returning a list of shopping items to be set on shopping list or data to be subtracted from Inventory
        return emptyList()
    }

}