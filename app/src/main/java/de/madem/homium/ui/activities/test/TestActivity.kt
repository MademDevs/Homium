package de.madem.homium.ui.activities.test

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_SPEECH
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.DatabaseInitializer
import de.madem.homium.models.ShoppingItem
import de.madem.homium.speech.SpeechAssistent
import de.madem.homium.speech.startSpeechRecognition
import de.madem.homium.utilities.showToastShort
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.coroutines.*
import java.util.*

class TestActivity : AppCompatActivity() {

    private lateinit var txtTextSpeechAssistent : TextView
    private var speechAssistent: SpeechAssistent? = null

    override fun onResume() {
        super.onResume()
        println("TESTACTIVITY: ONRESUME")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        speechAssistent = SpeechAssistent(this)

        findViewById<Button>(R.id.btn_TestSpeechAssistent).apply {
            setOnClickListener {
                this@TestActivity.startSpeechRecognition(REQUEST_CODE_SPEECH, Locale.GERMAN)
            }
        }

        txtTextSpeechAssistent = findViewById(R.id.txt_TestSpeechAssistent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){

            if(requestCode == REQUEST_CODE_SPEECH && data != null){
                val resultOfSpeechRecognition = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""

                if(resultOfSpeechRecognition.isNotEmpty()){
                    txtTextSpeechAssistent.text = resultOfSpeechRecognition
                    speechAssistent?.executeCommand(command = resultOfSpeechRecognition.toLowerCase())
                }
            }

        }
    }



}