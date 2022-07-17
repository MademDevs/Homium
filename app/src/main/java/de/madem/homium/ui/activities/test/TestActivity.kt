package de.madem.homium.ui.activities.test

import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import de.madem.homium.R
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Units

class TestActivity : AppCompatActivity() {
    private val viewModel : TestViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        println("TESTACTIVITY: ONRESUME")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            viewModel.clearInventory()
        }

        findViewById<Button>(R.id.btn_dummy).setOnClickListener {
            val list = mutableListOf<InventoryItem>().apply {
                add(InventoryItem("Apfel", 1, Units.ITEM.getString(this@TestActivity), "Kühlschrank"))
                add(InventoryItem("Milch", 1, Units.LITRE.getString(this@TestActivity), "Kühlschrank"))
            }
            viewModel.insertInventoryItems(list)
        }

        findViewById<Button>(R.id.button_test_darkmode).setOnClickListener {
            val mode = AppCompatDelegate.getDefaultNightMode()
            if(mode == AppCompatDelegate.MODE_NIGHT_NO){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}