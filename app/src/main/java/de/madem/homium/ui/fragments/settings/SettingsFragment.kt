package de.madem.homium.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R
import de.madem.homium.utilities.getSetting
import de.madem.homium.utilities.putSetting

class SettingsFragment : Fragment() {
    //viewmodel
    private lateinit var settingsViewModel: SettingsViewModel

    //GUI components
    private lateinit var vibrationSwitch : Switch
    private lateinit var radioGroupSortShopping : RadioGroup
    private lateinit var checkBoxDeleteQuestionSpeech : CheckBox

    //functions
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root : View = inflater.inflate(R.layout.fragment_settings, container, false)

        //setup general settings
        setupGeneralSettings(root)

        //setup shopping settings
        setupShoppingSettings(root)

        //setup speech assistent settings
        setupSpeechAssistentSettings(root)


        return root
    }

    override fun onPause() {
        super.onPause()
        putSetting<Boolean>(resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),vibrationSwitch.isChecked)
    }



    //setup functions for general settings
    private fun setupGeneralSettings(root: View){
        setupVibrationSwitch(root)
    }

    private fun setupVibrationSwitch(root: View){
        //setup switch
        vibrationSwitch = root.findViewById(R.id.vibrationSwitch)
        val vibrationEnabled : Boolean = getSetting<Boolean>(resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),Boolean::class) ?: true


        vibrationSwitch.isChecked = vibrationEnabled
        vibrationSwitch.setOnCheckedChangeListener { compoundButton, checked ->
            //putSetting<Boolean>(resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),checked)
        }
    }





    //setup functions for shopping settings
    private fun setupShoppingSettings(root: View){
        setupShoppingSortRadios(root)
    }

    private fun setupShoppingSortRadios(root : View){
        radioGroupSortShopping = root.findViewById<RadioGroup>(R.id.radioGroup_sort_shopping)
        var checkedRadioId : Int = getSetting<Int>(resources.getString(R.string.sharedpreference_settings_preferencekey_sortedShoppingRadioId),Int::class) ?: 0

        if(checkedRadioId == 0){
            checkedRadioId = R.id.radio_sort_normal
        }

        radioGroupSortShopping.check(checkedRadioId)

        radioGroupSortShopping.setOnCheckedChangeListener { radioGroup, i ->
            putSetting<Int>(resources.getString(R.string.sharedpreference_settings_preferencekey_sortedShoppingRadioId),radioGroupSortShopping.checkedRadioButtonId)
        }

    }


    //setup functions for speech assistent
    private fun setupSpeechAssistentSettings(root: View){
        setupDeleteQuestionCheck(root)
    }

    private fun setupDeleteQuestionCheck(root: View){
        checkBoxDeleteQuestionSpeech = root.findViewById(R.id.checkbox_deletequestion_speech)
        val questionAllowed = getSetting(resources.getString(R.string.sharedpreference_settings_preferencekey_deleteQuestionSpeechAssistentAllowed),
            Boolean::class) ?: true

        checkBoxDeleteQuestionSpeech.isChecked = questionAllowed

        checkBoxDeleteQuestionSpeech.setOnCheckedChangeListener { _, isChecked ->
            putSetting<Boolean>(resources.getString(R.string.sharedpreference_settings_preferencekey_deleteQuestionSpeechAssistentAllowed),isChecked)
        }
    }



}