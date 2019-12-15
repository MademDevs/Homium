package de.madem.homium.databases

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.madem.homium.models.Ingredients
import de.madem.homium.models.Recipe
import de.madem.homium.models.ShoppingItem

@Dao
interface RecipeDao {

    @Insert
    fun insertRecipe(vararg item: Recipe)

    @Query("DELETE FROM recipe")
    fun deleteAllRecipe()

    @Query("SELECT * FROM recipe")
    fun getAllRecipe(): List<Recipe>

    @Query("SELECT * FROM recipe WHERE uid = :id")
    fun getRecipeById(id: Int): Recipe

    //TODO: Queries for Recipe and Ingredients!

    @Insert
    fun insertIngredient(vararg item: Ingredients)

    @Query("SELECT * FROM ingredients WHERE uuid = :id")
    fun getIngredientById(id: Int) : Ingredients?


}