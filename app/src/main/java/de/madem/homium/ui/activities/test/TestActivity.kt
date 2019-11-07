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


        val numPickerCount = findViewById<NumberPicker>(R.id.numPickerCount)
        numPickerCount.minValue = 1
        numPickerCount.maxValue = 10

        val numPickerUnit = findViewById<NumberPicker>(R.id.numPickerUnit)
        numPickerUnit.minValue = 0
        numPickerUnit.maxValue = 4
        numPickerUnit.displayedValues = UNITS

        numPickerUnit.setOnValueChangedListener { np, i, i2 ->
            when(np.value) {
                0 -> {numPickerCount.minValue = 1; numPickerCount.maxValue = 20; numPickerCount.value = 1}
                1 -> {numPickerCount.minValue = 2; numPickerCount.maxValue = 10; numPickerCount.value = 2}
                2 -> {numPickerCount.minValue = 50; numPickerCount.maxValue = 200; numPickerCount.value = 50}
                3 -> {numPickerCount.minValue = 3; numPickerCount.maxValue = 5; numPickerCount.value = 3}
                4 -> {numPickerCount.minValue = 4; numPickerCount.maxValue = 100; numPickerCount.value = 4}
            }
        }


    }

}