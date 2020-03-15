package de.madem.homium.constants

//Big Unit Values

//Small Unit Values
val SMALL_UNITS_VALUES : Array<String> = Array<String>(15){(it+1).toString()};
val BIG_UNITS_VALUES : Array<String> = sequence<String> {
    var start = 50
    yield(start.toString())

    while(start < 500){
        start += 50
        yield(start.toString())
    }

    while(start < 1000){
        start += 100
        yield(start.toString())
    }

}.toList().toTypedArray()
