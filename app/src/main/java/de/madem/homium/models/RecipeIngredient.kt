package de.madem.homium.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class RecipeIngredient(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "unit") val unit: String = "",
    @ColumnInfo(name = "recipeId") var recipeId: Int,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
)