package de.madem.homium.databases

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.madem.homium.models.Ingredients
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
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

    @Insert
    fun insertIngredient(vararg item: RecipeIngredient)

    @Insert
    fun insertDescription(vararg item: RecipeDescription)

    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId")
    fun getIngredientByRecipeId(recipeId: Int): List<RecipeIngredient>

    @Query("SELECT * FROM descriptions WHERE recipeId = :recipeId")
    fun getDescriptionByRecipeId(recipeId: Int): List<RecipeDescription>

    @Query("DELETE FROM ingredients")
    fun deleteAllIngredient()

    @Query("DELETE FROM descriptions")
    fun deleteAllDescription()

    //TODO: Queries for Recipe and Ingredients!

}