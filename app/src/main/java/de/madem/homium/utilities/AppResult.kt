package de.madem.homium.utilities

import kotlinx.coroutines.flow.FlowCollector

sealed class AppResult<T>(open val data: T?) {
    data class Success<T>(override val data: T) : AppResult<T>(data)
    class Error<T>(val error: Throwable, data: T? = null) : AppResult<T>(data)
    class Loading<T>(data: T? = null) : AppResult<T>(data)
}

fun <T> T.toSuccessResult() = AppResult.Success(this)

fun <T> Throwable.toErrorLoading(data: T? = null) = AppResult.Error(this, data)

suspend fun <T> FlowCollector<AppResult<T>>.emitLoading(data: T? = null) {
    emit(AppResult.Loading(data))
}
