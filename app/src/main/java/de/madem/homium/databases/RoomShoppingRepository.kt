package de.madem.homium.databases

import de.madem.homium.errors.businesslogicerrors.DeletionFailedException
import de.madem.homium.errors.businesslogicerrors.InsertFailedException
import de.madem.homium.errors.businesslogicerrors.ShoppingItemNotFoundException
import de.madem.homium.errors.businesslogicerrors.UpdateFailedException
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.repositories.ShoppingRepository
import de.madem.homium.utilities.AppResult
import de.madem.homium.utilities.toSuccessResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/**
 * This is a concrete implementation of [ShoppingRepository] based on Room
 * */
class RoomShoppingRepository(
    private val shoppingDao: ShoppingDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ShoppingRepository {
    override fun getShoppingItemById(id: Int): Flow<AppResult<ShoppingItem>> = shoppingDao
        .getShoppingItemById(id)
        .map {
            if(it == null) {
                return@map AppResult.Error(ShoppingItemNotFoundException())
            }
            return@map AppResult.Success(it)
        }
        .onStart {
            emit(AppResult.Loading())
        }
        .flowOn(dispatcher)

    override suspend fun deleteShoppingItemById(id: Int): AppResult<Unit> {
        return withContext(dispatcher) {
            try {
                shoppingDao.deleteShoppingItemById(id).toSuccessResult()
            }
            catch (ex: Exception) {
                AppResult.Error(DeletionFailedException("ShoppingItem", "id = $id"))
            }
        }
    }

    override suspend fun updateShoppingItemById(id: Int, name: String, count: Int, unit: Units) : AppResult<Unit> {
        return withContext(dispatcher) {
            try {
                shoppingDao.updateShoppingItemById(id, name, count, unit).toSuccessResult()
            }
            catch (ex: Exception) {
                AppResult.Error(UpdateFailedException("ShoppingItem", "id = $id"))
            }
        }
    }

    override suspend fun insertShoppingItem(item: ShoppingItem) : AppResult<Unit> {
        return withContext(dispatcher) {
            try {
                shoppingDao.insertShoppingItem(item).toSuccessResult()
            }
            catch (ex: Exception) {
                val itemStr = item.toString()
                AppResult.Error(InsertFailedException("ShoppingItem", itemStr))
            }
        }
    }
}