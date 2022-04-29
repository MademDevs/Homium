package de.madem.homium.ui.activities.test

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Units
import de.madem.homium.speech.SpeechAssistent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            GlobalScope.launch(IO) {
                AppDatabase.getInstance().inventoryDao().clearInventory()
            }
        }

        findViewById<Button>(R.id.btn_dummy).setOnClickListener {
            GlobalScope.launch(IO) {
                val list = mutableListOf<InventoryItem>().apply {
                    add(InventoryItem("Apfel", 1, Units.ITEM.getString(this@TestActivity), "Kühlschrank"))
                    add(InventoryItem("Milch", 1, Units.LITRE.getString(this@TestActivity), "Kühlschrank"))
                }

                for (item in list) {
                    AppDatabase.getInstance().inventoryDao().insertInventoryItems(item)
                }
            }
        }



        /*
        speechAssistent = SpeechAssistent(this)

        findViewById<Button>(R.id.btn_TestSpeechAssistent).apply {
            setOnClickListener {
                this@TestActivity.startSpeechRecognition(REQUEST_CODE_SPEECH, Locale.GERMAN)
            }
        }

        txtTextSpeechAssistent = findViewById(R.id.txt_TestSpeechAssistent)

         */

        findViewById<Button>(R.id.button_test_darkmode).setOnClickListener {
            val mode = AppCompatDelegate.getDefaultNightMode();
            if(mode == AppCompatDelegate.MODE_NIGHT_NO){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }


    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
    }*/



}