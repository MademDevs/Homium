package de.madem.homium.utilities.backgroundtasks

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

open class CoroutineBackgroundTask<T> {

    //fields
    private var onPrepare : () -> Boolean = {true}
    private lateinit var backgroundAction : suspend CoroutineScope.() -> T
    private var onDone : (T) -> Unit = {}

    //functions
    fun onPrepare(function: () -> Boolean) : CoroutineBackgroundTask<T> {
        onPrepare = function
        return this
    }

    fun executeInBackground(function: suspend CoroutineScope.() -> T) : CoroutineBackgroundTask<T> {
        backgroundAction = function
        return this
    }

    fun onDone(function: (T) -> Unit) : CoroutineBackgroundTask<T> {
        onDone = function
        return this
    }

    open fun start(){
        if(this::backgroundAction.isInitialized){
            val allowedToStart = onPrepare.invoke()
            if(allowedToStart){
                GlobalScope.launch(IO) {
                    val result = backgroundAction.invoke(this)
                    withContext(Main){
                        onDone.invoke(result)
                    }
                }
            }

        }
    }

    open fun startInCoroutineScope(scope : CoroutineScope){
        if(this::backgroundAction.isInitialized){
            val allowedToStart = onPrepare.invoke()
            if(allowedToStart){
                scope.launch(IO) {
                    val result = backgroundAction.invoke(this)
                    withContext(Main){
                        onDone.invoke(result)
                    }
                }
            }


        }
    }
}