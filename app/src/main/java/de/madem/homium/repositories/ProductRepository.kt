package de.madem.homium.repositories

import de.madem.homium.models.Product
import de.madem.homium.utilities.AppResult
import kotlinx.coroutines.flow.Flow

//TODO Add Documentation to class and all functions
interface ProductRepository {
    fun getAllProducts(): Flow<AppResult<List<Product>>>
    fun getProductsByName(productName: String): Flow<AppResult<List<Product>>>
}