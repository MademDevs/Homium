package de.madem.homium.ui.activities.shoppingitem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import de.madem.homium.R
import de.madem.homium.ui.activities.test.TestActivity

class ShoppingItemEditActivity : AppCompatActivity() {

    //TEST DATA
    private val smallProductTestData = listOf<String>("Apple","Milk","Bread","Sausage","Cheese")

    //GUI Components
    private lateinit var btnDelete : Button
    private lateinit var autoCmplTxtName : AutoCompleteTextView
    private lateinit var editTxtQuantity : EditText
    private lateinit var spinnerUnits : Spinner


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
            //TODO: Save a shopping item
            R.id.shopping_item_edit_actionbar_confirm -> Toast.makeText(this,resources.getString(R.string.notification_edited_shoppingitem_sucess),Toast.LENGTH_SHORT).show()
        }

        return super.onOptionsItemSelected(item)

    }

    //private fuctions
    private fun initGuiComponents(){
        btnDelete = findViewById(R.id.shopping_item_edit_btn_delete)
        btnDelete.setOnClickListener{
            Toast.makeText(this,resources.getString(R.string.notification_delete_shoppingitem_sucess),Toast.LENGTH_SHORT).show()
        }

        autoCmplTxtName = findViewById(R.id.shopping_item_edit_autoCmplTxt_name)
        autoCmplTxtName.setAdapter(ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,smallProductTestData))

        val units2: Array<String> = arrayOf("Stück", "gramm")
        val numPickerCount = findViewById<NumberPicker>(R.id.numPickerCount)
        numPickerCount.minValue = 1
        numPickerCount.maxValue = 20
        numPickerCount.value = 1


        val numPickerUnit = findViewById<NumberPicker>(R.id.numPickerUnit)
        numPickerUnit.minValue = 0
        numPickerUnit.maxValue = 1
        numPickerUnit.displayedValues = units2
        numPickerUnit.value = 0

        val grammUnits = arrayOf("50", "100", "150", "200", "250", "300", "350", "400", "450", "500", "600", "700", "800", "900", "1000")
        val grammUnits2 = arrayOf("50", "100", "150", "200", "250", "300")
        numPickerUnit.setOnValueChangedListener { np, i, i2 ->
            when(np.value) {
                0 -> {numPickerCount.minValue = 1; numPickerCount.maxValue = 20}
                1 -> {numPickerCount.minValue = 0; numPickerCount.maxValue = 5; numPickerCount.displayedValues = grammUnits2}
            }
        }


        //spinnerUnits.adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,resources.getStringArray(R.array.dummy_units))
    }
}
