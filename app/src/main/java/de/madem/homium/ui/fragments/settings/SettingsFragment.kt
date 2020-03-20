package de.madem.homium.ui.fragments.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R
import de.madem.homium.application.HomiumApplication
import de.madem.homium.application.HomiumSettings
import de.madem.homium.constants.*
import de.madem.homium.databinding.FragmentSettingsBinding
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.getSetting
import de.madem.homium.utilities.extensions.notNull
import de.madem.homium.utilities.extensions.putSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import java.lang.ref.WeakReference

class SettingsFragment : Fragment() {
    //viewmodel
    private lateinit var settingsViewModel: SettingsViewModel

    //binding
    private lateinit var binding: FragmentSettingsBinding

    //reference for backgroundwork
    private lateinit var contextRef : WeakReference<Context>

    //functions
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_settings, container, false
        )


        binding.lifecycleOwner = this@SettingsFragment

        contextRef = WeakReference<Context>(this.context?: HomiumApplication.appContext!!)
        //setup general settings
        setupGeneralSettings()

        //setup shopping settings
        setupShoppingSettings()

        //setup general settings
        setupInventorySettings()

        //setup speech assistent settings
        setupSpeechAssistentSettings()


        return binding.root
    }

    //setup functions for general settings
    private fun setupGeneralSettings() {
        setupVibrationSwitch()
        setupAppTheme()
    }

    private fun setupVibrationSwitch() {
        //setup switch
        CoroutineBackgroundTask<Boolean>().executeInBackground {

            /*contextRef.get()?.getSetting(
                SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_VIBRATION_ENABLED,
                Boolean::class
            ) ?: true*/
            HomiumSettings.vibrationEnabled
        }.onDone {
            binding.vibrationAllowed = it

            with(binding.vibrationSwitch) {
                setOnCheckedChangeListener { compoundButton, checked ->
                    /*putSetting(
                        SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_VIBRATION_ENABLED,
                        checked
                    )*/
                    HomiumSettings.vibrationEnabled = checked
                }
            }
        }.start()
    }

    private fun setupAppTheme(){
        CoroutineBackgroundTask<Pair<Int,Int>>().executeInBackground {
            val id = when(HomiumSettings.appTheme){
                AppCompatDelegate.MODE_NIGHT_NO -> R.id.radio_app_theme_light
                AppCompatDelegate.MODE_NIGHT_YES -> R.id.radio_app_theme_dark
                else -> R.id.radio_app_theme_system
            }

            return@executeInBackground Pair<Int,Int>(HomiumSettings.appTheme,id)
        }.onDone {
          with(binding.radioGroupAppTheme){
              binding.appThemeRadioId = it.second

              setOnCheckedChangeListener { _, checkedId ->
                  val theme = when(checkedId){
                      R.id.radio_app_theme_light -> AppCompatDelegate.MODE_NIGHT_NO
                      R.id.radio_app_theme_dark -> AppCompatDelegate.MODE_NIGHT_YES
                      else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                  }

                  if(HomiumSettings.appTheme != theme){
                      AppCompatDelegate.setDefaultNightMode(theme)
                      HomiumSettings.appTheme = theme
                  }
              }
          }
        }.start()
    }


    //setup functions for shopping settings
    private fun setupShoppingSettings() {
        setupShoppingSortRadios()
    }

    private fun setupShoppingSortRadios() {
        CoroutineBackgroundTask<Int>().executeInBackground {
            /*val setting = contextRef.get()?.getSetting(
                SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_SORT,
                String::class
            ) ?: SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_NORMAL*/
            val setting = HomiumSettings.shoppingSort

            return@executeInBackground if(setting == SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_REVERSED)
                R.id.radio_sort_reversed
            else
                R.id.radio_sort_normal
        }.onDone {
            binding.shoppinglistSortId = it
            with(binding.radioGroupSortShopping) {

                setOnCheckedChangeListener { _, id ->

                    val sortVal = if(id == R.id.radio_sort_reversed)
                        SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_REVERSED
                    else
                        SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_NORMAL

                    /*putSetting(
                        SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_SORT,
                        sortVal
                    )
                     */
                    HomiumSettings.shoppingSort = sortVal
                }
            }
        }.start()



    }

    //setup for inventory settings
    private fun setupInventorySettings(){
        setupShoppingToInventoryRadios()
    }

    private fun setupShoppingToInventoryRadios() {
        CoroutineBackgroundTask<Int>().executeInBackground {
            /*contextRef.get()?.getSetting(
                SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_TO_INVENTORY,
                Int::class
            ) ?: R.id.radio_check_question*/
            HomiumSettings.shoppingToInventory
        }.onDone {
            binding.inventoryBehaviourQuestionId = it

            with(binding.radioGroupCheckBehaviour) {
                //check(checkedRadioId)

                setOnCheckedChangeListener { radioGroup, i ->
                    /*putSetting(
                        SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_TO_INVENTORY,
                        checkedRadioButtonId
                    )*/
                    HomiumSettings.shoppingToInventory = checkedRadioButtonId
                }
            }
        }.start()
    }


    //setup functions for speech assistent
    private fun setupSpeechAssistentSettings() {
        setupDeleteQuestionCheck()
    }

    private fun setupDeleteQuestionCheck() {
        CoroutineBackgroundTask<Boolean>().executeInBackground {
            /*contextRef.get()?.getSetting(
                SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_DELETE_QUESTION_SPEECH_ASSISTENT_ALLOWED,
                Boolean::class
            ) ?: true*/
            HomiumSettings.speechAssistantDeleteQuestion
        }.onDone {
            binding.deleteQuestionAllowed = it

            with(binding.checkboxDeletequestionSpeech) {
                setOnCheckedChangeListener { _, isChecked ->
                    /*putSetting(
                        SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_DELETE_QUESTION_SPEECH_ASSISTENT_ALLOWED,
                        isChecked
                    )*/
                    HomiumSettings.speechAssistantDeleteQuestion = isChecked
                }
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        HomiumSettings.saveSettings(context ?: HomiumApplication.appContext)
    }

}