package de.madem.homium.ui.fragments.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R
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

        contextRef = WeakReference<Context>(this.context)
        //setup general settings
        setupGeneralSettings()

        //setup shopping settings
        setupShoppingSettings()

        //setup speech assistent settings
        setupSpeechAssistentSettings()


        return binding.root
    }

    //setup functions for general settings
    private fun setupGeneralSettings() {
        setupVibrationSwitch()
    }

    private fun setupVibrationSwitch() {
        //setup switch


        CoroutineBackgroundTask<Boolean>().executeInBackground {

            contextRef.get()?.getSetting(
                resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),
                Boolean::class
            ) ?: true
        }.onDone {
            binding.vibrationAllowed = it

            with(binding.vibrationSwitch) {
                setOnCheckedChangeListener { compoundButton, checked ->
                    putSetting(
                        resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),
                        checked
                    )
                }
            }
        }.start()



    }


    //setup functions for shopping settings
    private fun setupShoppingSettings() {
        setupShoppingSortRadios()
        setupShoppingToInventoryRadios()
    }

    private fun setupShoppingSortRadios() {
        CoroutineBackgroundTask<Int>().executeInBackground {
            contextRef.get()?.getSetting(
                resources.getString(R.string.sharedpreference_settings_preferencekey_sortedShoppingRadioId),
                Int::class
            ) ?: R.id.radio_sort_normal
        }.onDone {
            binding.shoppinglistSortId = it
            with(binding.radioGroupSortShopping) {

                setOnCheckedChangeListener { _, id ->
                    putSetting(
                        resources.getString(R.string.sharedpreference_settings_preferencekey_sortedShoppingRadioId),
                        id
                    )
                }
            }
        }.start()



    }

    private fun setupShoppingToInventoryRadios() {
        CoroutineBackgroundTask<Int>().executeInBackground {
            contextRef.get()?.getSetting(
                resources.getString(R.string.sharedpreference_settings_preferencekey_shoppingToInventory),
                Int::class
            ) ?: R.id.radio_check_question
        }.onDone {
            binding.inventoryBehaviourQuestionId = it

            with(binding.radioGroupCheckBehaviour) {
                //check(checkedRadioId)

                setOnCheckedChangeListener { radioGroup, i ->
                    putSetting(
                        resources.getString(R.string.sharedpreference_settings_preferencekey_shoppingToInventory),
                        checkedRadioButtonId
                    )
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
            contextRef.get()?.getSetting(
                resources.getString(R.string.sharedpreference_settings_preferencekey_deleteQuestionSpeechAssistentAllowed),
                Boolean::class
            ) ?: true
        }.onDone {
            binding.deleteQuestionAllowed = it

            with(binding.checkboxDeletequestionSpeech) {
                setOnCheckedChangeListener { _, isChecked ->
                    putSetting(
                        resources.getString(R.string.sharedpreference_settings_preferencekey_deleteQuestionSpeechAssistentAllowed),
                        isChecked
                    )
                }
            }
        }.start()
    }

}