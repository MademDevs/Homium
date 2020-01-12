package de.madem.homium.utilities

fun <T> T?.notNull(function: (T) -> Unit){
    if (this != null){
        function(this)
    }
}

fun <T> withNotNull(element: T?, function: T.() -> Unit) {
    if(element != null) {
        function(element)
    }
}

fun <T> T?.applyNotNull(function: T.() -> Unit): T {
    if (this != null){
        function(this)
        return this
    }
    throw RuntimeException("Function parameter is null!")
}

fun String.capitalizeEachWord() : String{
    return this.split(Regex(" ")).joinToString(" ") { it.capitalize() }
}

fun String.capitalizeEachWordExcept(vararg except: String) : String{
    return this.split(Regex(" ")).joinToString(" ") {
        if(!(except.contains(it))){
            it.capitalize()
        }
        else{
            it
        }
    }
}