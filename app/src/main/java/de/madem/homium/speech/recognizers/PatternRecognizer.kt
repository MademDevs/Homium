package de.madem.homium.speech.recognizers

import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask

interface PatternRecognizer {

    fun matchingTask(command : String) : CoroutineBackgroundTask<Boolean>?

}