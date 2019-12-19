package de.madem.homium.speech

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import de.madem.homium.exceptions.SpeechRecognitionException
import java.util.*

fun Activity.startSpeechRecognition(requestCode: Int, locale: Locale){
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString())

    if(intent.resolveActivity(packageManager) != null){
        startActivityForResult(intent,requestCode)
    }
    else {
        throw SpeechRecognitionException("intent.resolveActivity(packageManager) == null")
    }

}