package de.madem.homium.ui.activities.shoppingitem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.view.isVisible
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.CoroutineBackgroundTask
import de.madem.homium.models.Product
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.ui.activities.main.MainActivity
import de.madem.homium.utilities.switchToActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ShoppingItemEditActivity : AppCompatActivity() {

    //GUI Components
    private lateinit var btnDelete : Button
    private lateinit var autoCmplTxtName : AutoCompleteTextView
    private lateinit var numPickerCount: NumberPicker
    private lateinit var numPickerUnit: NumberPicker
    private lateinit var editTextCount: EditText
    private val db = AppDatabase.getInstance(this)

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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

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
            R.id.shopping_item_edit_actionbar_confirm -> addToDatabase().also { switchToActivity(MainActivity::class) }
            android.R.id.home -> finish()
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

        CoroutineBackgroundTask<List<Product>>().executeInBackground {
            val result = db.itemDao().getAllProduct()
            return@executeInBackground result
        }.onDone {result ->
            val productNameList = result.map { it.name }
            autoCmplTxtName.setAdapter(ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, productNameList))

        }.start()

        /*
        val productNameList = getProducts().map { it.name }

        var nameList = mutableListOf<String>()
        for (el in productNameList) {
            nameList.add(el.name)
        }


        autoCmplTxtName.setAdapter(ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, productNameList))
        */

        //init numberpicker
        val units = Units.stringValueArray(this)
        numPickerUnit = findViewById<NumberPicker>(R.id.shopping_item_edit_numPick_unit).also {
            it.isSaveFromParentEnabled = false
            it.isSaveEnabled = false
            it.minValue = 0
            it.maxValue = units.size-1
            it.displayedValues = units
            it.value = 0
        }


        numPickerCount = findViewById<NumberPicker>(R.id.shopping_item_edit_numPick_count)
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            numPickerCount.isSaveFromParentEnabled = false
            numPickerCount.isSaveEnabled = false
            numPickerCount.minValue = 1
            numPickerCount.maxValue = 20
            numPickerCount.value = 1
            numPickerCount.setOnLongClickListener {
                numPickerCount.isVisible = false
                editTextCount.isVisible = true
                true
            }
            val bigUnits = resources.getStringArray(R.array.big_units)
            val smallUnits = resources.getStringArray(R.array.small_units)
            numPickerUnit.setOnValueChangedListener { np, i, i2 ->
                when(np.value) {
                    1, 3 -> {
                        numPickerCount.minValue = 0
                        numPickerCount.maxValue = 14
                        numPickerCount.displayedValues = bigUnits
                    }
                    else -> {
                        numPickerCount.displayedValues = smallUnits
                        numPickerCount.minValue = 0
                        numPickerCount.maxValue = 14
                    }
                }
            }
        } else {
            numPickerCount.isVisible = false
        }

        editTextCount = findViewById<EditText>(R.id.shopping_item_edit_editTxt_count).also {
            it.isVisible = false
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                it.setOnLongClickListener {
                    editTextCount.isVisible = false
                    numPickerCount.isVisible = true
                    true
                }
            } else {
                it.isVisible = true
            }
        }

    }

    private fun getProducts():List<Product> {
        var list = listOf<Product>()
        GlobalScope.launch {
            list = db.itemDao().getAllProduct()
        }
        Toast.makeText(this, "Prdocts geladen ${list.size}",Toast.LENGTH_SHORT).show()
        return list
    }

    private fun getAmount(): Int {
        if(numPickerCount.isVisible) {
            return numPickerCount.value
        } else {
            return editTextCount.text.toString().toInt()
        }
    }

    private fun getUnit(): String {
        return Units.stringValueArray(this)[numPickerUnit.value]
    }

    private fun getItem(): String {
        return autoCmplTxtName.text.toString()
    }

    private fun addToDatabase() {
        val item = ShoppingItem(getItem(), getAmount(), getUnit())
        GlobalScope.launch { db.itemDao().insertShopping(item) }
    }
}
