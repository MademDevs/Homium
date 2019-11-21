package de.madem.homium.ui.activities.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.madem.homium.R
import de.madem.homium.managers.DatabaseInitializer
import de.madem.homium.ui.activities.test.TestActivity
import de.madem.homium.utilities.getSetting
import de.madem.homium.utilities.putSetting
import de.madem.homium.utilities.switchToActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dataBaseInitialized : Boolean = getSetting<Boolean>(resources.getString(R.string.sharedpreference_settings_preferencekey_databaseInitialized),Boolean::class) ?: false

        if(!dataBaseInitialized){
            //init database (should be done in onboarding later)
            DatabaseInitializer(applicationContext) {
                Toast.makeText(this,"Init Database",Toast.LENGTH_SHORT).show()
                putSetting(resources.getString(R.string.sharedpreference_settings_preferencekey_databaseInitialized),true)
            }

        }



        //nav controller for bottom navigation
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





}
