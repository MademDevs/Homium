package de.madem.homium.ui.activities.main

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import androidx.room.RoomDatabase
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Product
import de.madem.homium.ui.activities.test.TestActivity
import de.madem.homium.utilities.switchToActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileReader


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_shopping,
                R.id.navigation_inventory,
                R.id.navigation_recipes,
                R.id.navigation_money,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        fillDatabase()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if( menu != null){
            menuInflater.inflate(R.menu.activity_main_actionbar_menu,menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId){
            R.id.main_actionbar_testezone -> {
                switchToActivity(TestActivity::class)
                return false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    //Onboarding -> filling some Data in Database
    private fun fillDatabase() {
        val db = AppDatabase.getInstance(this)
        GlobalScope.launch {
            db.itemDao().deleteAllProduct()
            //db.itemDao().insertProduct(
                //Product("Apfel", "kg", "2"),
                //Product("Ananas", "Stück", "1"),
                //Product("Brötchen", "Stück", "5"),
                //Product("Hackfleisch", "g", "500")
           // )

            var fileReader = BufferedReader(FileReader("ProductsCSV.csv"))
            var line = fileReader.readLine()
            while(line != null) {
                val splitted = line.split(";")
                db.itemDao().insertProduct(Product(splitted[0], splitted[1], splitted[2]))
                line = fileReader.readLine()
            }
            println(db.itemDao().getAllProduct())
        }
    }


}
