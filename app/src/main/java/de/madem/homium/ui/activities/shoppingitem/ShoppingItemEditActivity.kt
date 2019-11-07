package de.madem.homium.ui.activities.shoppingitem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import de.madem.homium.R
import de.madem.homium.models.Units

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
        editTxtQuantity = findViewById(R.id.shopping_item_edit_editTxt_quantity)
        spinnerUnits = findViewById(R.id.shopping_item_edit_spinner_unit)
        spinnerUnits.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,Units.stringValueArray(this))

    }
}
