package de.madem.homium.databases

import androidx.room.Dao
import androidx.room.Query
import de.madem.homium.models.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM product")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM product WHERE name LIKE :productName")
    fun getProductsByName(productName: String): Flow<List<Product>>
}