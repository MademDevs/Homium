package de.madem.homium.databases

import de.madem.homium.models.Product
import de.madem.homium.repositories.ProductRepository
import de.madem.homium.utilities.AppResult
import de.madem.homium.utilities.toErrorResult
import de.madem.homium.utilities.toSuccessResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class RoomProductRepository(
    private val productDao: ProductDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ProductRepository {
    override fun getAllProducts(): Flow<AppResult<List<Product>>> = productDao
        .getAllProducts()
        .map<List<Product>, AppResult<List<Product>>> { it.toSuccessResult() }
        .catch { emit(it.toErrorResult()) }
        .flowOn(dispatcher)


    override fun getProductsByName(productName: String): Flow<AppResult<List<Product>>> = productDao
        .getProductsByName(productName)
        .map<List<Product>, AppResult<List<Product>>> { it.toSuccessResult() }
        .catch { emit(it.toErrorResult()) }
        .flowOn(dispatcher)
}