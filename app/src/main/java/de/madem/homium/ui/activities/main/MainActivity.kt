package de.madem.homium.ui.activities.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.madem.homium.BuildConfig
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_SPEECH
import de.madem.homium.exceptions.SpeechRecognitionException
import de.madem.homium.speech.SpeechAssistent
import de.madem.homium.speech.startSpeechRecognition
import de.madem.homium.ui.activities.about.AboutActivity
import de.madem.homium.ui.activities.test.TestActivity
import de.madem.homium.ui.dialogs.RecipeImportDialogListener
import de.madem.homium.utilities.RecipeImporter
import de.madem.homium.utilities.extensions.showToastLong
import de.madem.homium.utilities.extensions.switchToActivity
import de.madem.homium.utilities.extensions.whenSearchViewHandler
import java.lang.Exception
import java.util.*


class MainActivity : AppCompatActivity(), RecipeImportDialogListener by RecipeImporter() {

    //fields
    private var speechAssistent : SpeechAssistent? = null

    //oncreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //nav controller for bottom navigation
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_shopping,
                R.id.navigation_inventory,
                R.id.navigation_recipes,
                R.id.navigation_money,
                R.id.navigation_settings
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //instanciating speech assistent
        speechAssistent = SpeechAssistent(this)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if( menu != null){
            menuInflater.inflate(R.menu.activity_main_actionbar_menu,menu)
        }

        if (!BuildConfig.DEBUG) {
            menu?.findItem(R.id.main_actionbar_testezone)?.isVisible = false
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //close search view
        closeSearchViewIfExisting()
        //handle item
        return when(item.itemId){
            R.id.main_actionbar_testezone -> {
                switchToActivity(TestActivity::class)
                false
            }
            R.id.main_actionbar_speech_assistent -> {
                try {
                    startSpeechRecognition(REQUEST_CODE_SPEECH, Locale.GERMAN)
                }
                catch (ex : SpeechRecognitionException){
                    AlertDialog.Builder(this)
                        .setTitle(R.string.error)
                        .setMessage(R.string.errormsg_unknown_error_with_speech_assistent)
                        .setPositiveButton(android.R.string.ok){_,_ -> }
                        .show()
                }
                catch(ex : Exception){
                    showToastLong(R.string.errormsg_unknown_error_with_speech_assistent)
                }
                false
            }
            R.id.main_actionbar_about -> {
                switchToActivity(AboutActivity::class)
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){

            if(requestCode == REQUEST_CODE_SPEECH && data != null){
                val resultOfSpeechRecognition = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""

                if(resultOfSpeechRecognition.isNotEmpty() && resultOfSpeechRecognition.isNotBlank()){
                    //calling speech assistent in try catch to ensure that system does not crash no matter what happens
                    try{
                        speechAssistent?.executeCommand(command = resultOfSpeechRecognition)
                    }
                    catch(ex : Exception){
                       ex.printStackTrace()
                    }

                }
            }

        }
    }

    private fun closeSearchViewIfExisting(){
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager?.primaryNavigationFragment.whenSearchViewHandler {
            it.closeSearchView()
        }
    }
}
