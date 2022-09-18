package de.madem.homium.utilities.backgroundtasks

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface DebounceCoroutine<T> {
    val debounceMillis: ULong
    suspend fun startDebounced(input: T)
}

private class DebounceCoroutineImpl<T> (
    override val debounceMillis: ULong,
    private val onNewDebounceJobStarted: suspend () -> Unit = { },
    private val onDebounceJobCompleted: suspend () -> Unit = { },
    private val onExecute: suspend (T) -> Unit
) : DebounceCoroutine<T> {
    private var debounceJob: Job = Job()

    override suspend fun startDebounced(input: T) = coroutineScope {
        if (debounceJob.isActive) {
            debounceJob.cancel()
        }

        debounceJob = launch {
            onNewDebounceJobStarted()

            delay(debounceMillis.toLong())
            onExecute.invoke(input)

            onDebounceJobCompleted()
        }
    }
}



fun <T> debounce(
    debounceMillis: ULong, onNewDebounceJobStarted: suspend () -> Unit = { },
    onDebounceJobCompleted: suspend () -> Unit = { },
    onExecute: suspend (T) -> Unit
) : DebounceCoroutine<T> = DebounceCoroutineImpl(
    debounceMillis = debounceMillis, onDebounceJobCompleted = onDebounceJobCompleted,
    onNewDebounceJobStarted = onNewDebounceJobStarted, onExecute = onExecute
)
