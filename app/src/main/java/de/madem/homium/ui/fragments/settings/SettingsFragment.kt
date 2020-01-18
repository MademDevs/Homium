package de.madem.homium.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R
import de.madem.homium.databinding.FragmentSettingsBinding
import de.madem.homium.utilities.extensions.getSetting
import de.madem.homium.utilities.extensions.putSetting

class SettingsFragment : Fragment() {
    //viewmodel
    private lateinit var settingsViewModel: SettingsViewModel

    //binding
    private lateinit var binding: FragmentSettingsBinding

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
        binding.vibrationAllowed = getSetting(
            resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),
            Boolean::class
        ) ?: true

        with(binding.vibrationSwitch) {
            setOnCheckedChangeListener { compoundButton, checked ->
                putSetting(
                    resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),
                    checked
                )
            }
        }
    }


    //setup functions for shopping settings
    private fun setupShoppingSettings() {
        setupShoppingSortRadios()
        setupShoppingToInventoryRadios()
    }

    private fun setupShoppingSortRadios() {
        binding.shoppinglistSortId = getSetting(
            resources.getString(R.string.sharedpreference_settings_preferencekey_sortedShoppingRadioId),
            Int::class
        ) ?: R.id.radio_sort_normal

        with(binding.radioGroupSortShopping) {

            setOnCheckedChangeListener { _, _ ->
                putSetting(
                    resources.getString(R.string.sharedpreference_settings_preferencekey_sortedShoppingRadioId),
                    checkedRadioButtonId
                )
            }
        }
    }

    private fun setupShoppingToInventoryRadios() {
        binding.inventoryBehaviourQuestionId = getSetting(
            resources.getString(R.string.sharedpreference_settings_preferencekey_shoppingToInventory),
            Int::class
        ) ?: R.id.radio_check_question

        println(binding.inventoryBehaviourQuestionId == R.id.radioGroup_check_behaviour)
        println(binding.inventoryBehaviourQuestionId as Int)

        with(binding.radioGroupCheckBehaviour) {
            //check(checkedRadioId)

            setOnCheckedChangeListener { radioGroup, i ->
                putSetting(
                    resources.getString(R.string.sharedpreference_settings_preferencekey_shoppingToInventory),
                    checkedRadioButtonId
                )
            }
        }
    }


    //setup functions for speech assistent
    private fun setupSpeechAssistentSettings() {
        setupDeleteQuestionCheck()
    }

    private fun setupDeleteQuestionCheck() {
        binding.deleteQuestionAllowed = getSetting(
            resources.getString(R.string.sharedpreference_settings_preferencekey_deleteQuestionSpeechAssistentAllowed),
            Boolean::class
        ) ?: true

        with(binding.checkboxDeletequestionSpeech) {
            setOnCheckedChangeListener { _, isChecked ->
                putSetting(
                    resources.getString(R.string.sharedpreference_settings_preferencekey_deleteQuestionSpeechAssistentAllowed),
                    isChecked
                )
            }
        }
    }

}