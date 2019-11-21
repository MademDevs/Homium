package de.madem.homium.utilities

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class CoroutineBackgroundTask<T> {

    private lateinit var backgroundAction : CoroutineScope.() -> T
    private var onDone : (T) -> Unit = {}

    fun executeInBackground(function: CoroutineScope.() -> T) : CoroutineBackgroundTask<T> {
        backgroundAction = function
        return this
    }

    fun onDone(function: (T) -> Unit) : CoroutineBackgroundTask<T> {
        onDone = function
        return this
    }

    fun start(){
        if(this::backgroundAction.isInitialized){
            GlobalScope.launch(IO) {
                val result = backgroundAction.invoke(this)
                withContext(Main){
                    onDone.invoke(result)
                }
            }
        }
    }
}