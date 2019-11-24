package de.madem.homium.utilities

fun <T> T?.notNull(function: (T) -> Unit){
    if (this != null){
        function(this)
    }
}