package de.madem.homium.managers

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main

class CoroutineBackgroundTask<T>() {

    private lateinit var backgroundAction : () -> T
    private var onDone : (T) -> Unit = {}

    fun executeInBackground(function: () -> T) : CoroutineBackgroundTask<T>{
        backgroundAction = function

        return this
    }

    fun onDone(function: (T) -> Unit) : CoroutineBackgroundTask<T>{
        onDone = function
        return this
    }

    fun start(){
        if(this::backgroundAction.isInitialized){
            GlobalScope.launch {
                val result = backgroundAction.invoke()
                withContext(Main){
                    onDone.invoke(result)
                }
            }
        }
    }
}