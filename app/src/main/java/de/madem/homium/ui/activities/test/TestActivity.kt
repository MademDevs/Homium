package de.madem.homium.ui.activities.test

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.DatabaseInitializer
import de.madem.homium.models.ShoppingItem
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.coroutines.*

class TestActivity : AppCompatActivity() {

    private lateinit var list: List<ShoppingItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = this

        setContentView(R.layout.activity_test)

        val btn = findViewById<Button>(R.id.button)
        val listView = findViewById<ListView>(R.id.listView)
        list = listOf()

        btn.setOnClickListener {

            //Code for getting Products
            GlobalScope.launch {
                val dao = AppDatabase.getInstance(context).itemDao()
                list = dao.getAllShopping()
            }
            //
            listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        }

        btn_initdb.setOnClickListener {
            DatabaseInitializer(applicationContext) {
                Toast.makeText(this, "Datenbank initialisiert!", Toast.LENGTH_SHORT).show()
            }
        }

    }


}