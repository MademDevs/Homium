package de.madem.homium.databases

import androidx.room.*
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.models.ShoppingItem

@Dao
interface RecipeDao {

    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): List<RecipeIngredient>

    @Insert
    fun insertRecipe(item: Recipe): Long

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

    @Query("SELECT * FROM ingredients WHERE uid = :id")
    fun getIngredientById(id: Int): RecipeIngredient

    @Update
    fun updateRecipe(vararg recipes: Recipe)

    @Update
    fun updateDescription(vararg descriptions: RecipeDescription)

    @Update
    fun updateIngredients(vararg ingredients: RecipeIngredient)

    @Delete
    fun deleteRecipe(vararg recipes: Recipe)

    @Query("DELETE FROM descriptions WHERE recipeId = :id")
    fun deleteDescriptionByRecipeId(id: Int)

    @Query("DELETE FROM ingredients WHERE recipeId = :id")
    fun deleteIngredientByRecipeId(id: Int)

    @Query("SELECT CASE WHEN name LIKE :name THEN 1 ELSE 0 END FROM recipe")
    fun containsRecipeWithName(name : String) : Boolean

    @Query("SELECT uid FROM recipe WHERE name LIKE :name")
    fun idOf(name: String) : Int
}