package de.madem.homium.utilities

fun <T> T?.notNull(function: (T) -> Unit){
    if (this != null){
        function(this)
    }
}

fun <T> T?.applyNotNull(function: T.() -> Unit): T {
    if (this != null){
        function(this)
        return this
    }
    throw RuntimeException("Function parameter is null!")
}