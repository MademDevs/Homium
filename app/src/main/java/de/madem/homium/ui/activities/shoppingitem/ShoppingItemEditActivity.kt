package de.madem.homium.ui.activities.shoppingitem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.view.isVisible
import de.madem.homium.R
import de.madem.homium.models.Units
import kotlinx.android.synthetic.main.activity_test.*

class ShoppingItemEditActivity : AppCompatActivity() {

    //TEST DATA
    private val smallProductTestData = listOf<String>("Apple","Milk","Bread","Sausage","Cheese")

    //GUI Components
    private lateinit var btnDelete : Button
    private lateinit var autoCmplTxtName : AutoCompleteTextView
    private lateinit var numPickerCount: NumberPicker
    private lateinit var numPickerUnit: NumberPicker
    private lateinit var editTextCount: EditText

    //ON CREATE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_item_edit)

        /*
        TODO: implement functionality for getting shoppingitem
        val shoppingitem = intent.getParcelableExtra<ShoppingItem>(resources.getString(R.string.data_transfer_intent_edit_shoppingitem))
        if (shoppingitem == null){
            //adding action
            supportActionBar?.title = resources.getString(R.string.screentitle_edit_shopppingitem_add)
        }
        else{
            supportActionBar?.title = resources.getString(R.string.screentitle_edit_shoppingitem_edit)
        }
         */

        initGuiComponents()

    }

    //optionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(menu != null){
            menuInflater.inflate(R.menu.shoppingitem_edit_actionbar_menu,menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            //TODO: Save a shopping item -> addToDatabase()
            R.id.shopping_item_edit_actionbar_confirm -> Toast.makeText(this,resources.getString(R.string.notification_edited_shoppingitem_sucess),Toast.LENGTH_SHORT).show()
        }

        return super.onOptionsItemSelected(item)

    }

    //private fuctions
    private fun initGuiComponents(){
        //init delete button
        btnDelete = findViewById(R.id.shopping_item_edit_btn_delete)
        btnDelete.setOnClickListener{
            Toast.makeText(this,resources.getString(R.string.notification_delete_shoppingitem_sucess),Toast.LENGTH_SHORT).show()
        }

        //init txt autocomplete
        autoCmplTxtName = findViewById(R.id.shopping_item_edit_autoCmplTxt_name)
        autoCmplTxtName.setAdapter(ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,smallProductTestData))

        //init numberpicker
        numPickerCount = findViewById<NumberPicker>(R.id.shopping_item_edit_numPick_count).also {
            it.isSaveFromParentEnabled = false
            it.isSaveEnabled = false
            it.minValue = 1
            it.maxValue = 20
            it.value = 1
            it.setOnLongClickListener {
                numPickerCount.isVisible = false
                editTextCount.isVisible = true
                true
            }
        }
        val numPickerCountStandardDisplay = numPickerCount.displayedValues

        val units = Units.stringValueArray(this)
        numPickerUnit = findViewById<NumberPicker>(R.id.shopping_item_edit_numPick_unit).also {
            it.isSaveFromParentEnabled = false
            it.isSaveEnabled = false
            it.minValue = 0
            it.maxValue = units.size-1
            it.displayedValues = units
            it.value = 0
        }

        editTextCount = findViewById<EditText>(R.id.shopping_item_edit_editTxt_count).also {
            it.isVisible = false
            it.setOnLongClickListener {
                editTextCount.isVisible = false
                numPickerCount.isVisible = true
                true
            }
        }

        val bigUnits = arrayOf("50", "100", "150", "200", "250", "300", "350", "400", "450", "500", "600", "700", "800", "900", "1000")
        numPickerUnit.setOnValueChangedListener { np, i, i2 ->
            when(np.value) {
                1, 3 -> {
                    numPickerCount.minValue = 0
                    numPickerCount.maxValue = 14
                    numPickerCount.displayedValues = bigUnits
                }
                else -> {
                    numPickerCount.displayedValues = numPickerCountStandardDisplay
                    numPickerCount.minValue = 1
                    numPickerCount.maxValue = 20
                }
            }
        }

    }

    private fun getAmount(): Double {
        if(numPickerCount.isVisible) {
            return numPickerCount.value.toDouble()
        } else {
            return editTextCount.text.toString().toDouble()
        }
    }

    private fun getUnit(): Units {
        return Units.valueOf(Units.stringValueArray(this)[numPickerUnit.value])
    }

    private fun getItem(): String {
        return autoCmplTxtName.text.toString()
    }

    private fun addToDatabase() {
        //TODO: Add item to Database
    }
}
