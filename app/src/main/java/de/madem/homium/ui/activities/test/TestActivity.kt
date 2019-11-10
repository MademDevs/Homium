package de.madem.homium.ui.activities.test

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import de.madem.homium.R
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {

    companion object {
        val UNITS: Array<String> = arrayOf("Stück", "Packung", "gramm", "l", "ml")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)



        val numPickerUnit = findViewById<NumberPicker>(R.id.numPickerUnit)
        numPickerUnit.minValue = 0
        numPickerUnit.maxValue = 4
        numPickerUnit.displayedValues = UNITS



    }

}