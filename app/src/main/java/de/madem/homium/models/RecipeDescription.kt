package de.madem.homium.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "descriptions")
data class RecipeDescription(
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "recipeId") var recipeID: Int,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
)