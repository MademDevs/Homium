package de.madem.homium.utilities.extensions

import de.madem.homium.utilities.AppResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

/**
 * This extension forwards all Errors of this Flow into the given [channel]
 */
fun <T> Flow<AppResult<T>?>.forwardNullableErrors(channel: Channel<Throwable>) : Flow<AppResult<T>?> {
    return this.onEach {
        if(it is AppResult.Error) {
            channel.send(it.error)
        }
    }
}

/**
 * This extension forwards all Errors of this Flow into the given [channel]
 */
fun <T> Flow<AppResult<T>>.forwardErrors(channel: Channel<Throwable>) : Flow<AppResult<T>> {
    return this.onEach {
        if(it is AppResult.Error) {
            channel.send(it.error)
        }
    }
}