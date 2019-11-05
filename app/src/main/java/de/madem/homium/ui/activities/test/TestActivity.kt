package de.madem.homium.ui.activities.test

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import de.madem.homium.R
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {

    companion object {
        val COUNTRIES = arrayOf("Belgium", "France", "Italy", "Germany", "Spain")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)

        val adapter = ArrayAdapter(
            this, android.R.layout.simple_dropdown_item_1line, COUNTRIES
        )
        tvauto_test.setAdapter(adapter)
    }

}