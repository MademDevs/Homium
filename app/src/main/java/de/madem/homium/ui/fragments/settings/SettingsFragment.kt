package de.madem.homium.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.getSetting
import de.madem.homium.utilities.putSetting

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        settingsViewModel =
            ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        //setup switch
        val vibrationSwitch: Switch = root.findViewById(R.id.vibrationSwitch)
        val vibrationEnabled : Boolean = getSetting<Boolean>(resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),Boolean::class) ?: true

        vibrationSwitch.isChecked = vibrationEnabled
        vibrationSwitch.setOnCheckedChangeListener { compoundButton, checked ->
            putSetting<Boolean>(resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),checked)
        }

        settingsViewModel.text.observe(this, Observer {
            //textView.text = it
        })
        return root
    }
}