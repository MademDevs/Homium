package de.madem.homium.ui.activities.shoppingitem

import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Product
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.utilities.CoroutineBackgroundTask
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
        val id = intent.getIntExtra("item", -1)
        if(id >= 0) {
            setShoppingItemToElements(id)
        }

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
            //TODO: Save a shopping item -> addToDatabaseIfPossible()
            R.id.shopping_item_edit_actionbar_confirm -> addToDatabaseIfPossible()
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)

    }

    fun setShoppingItemToElements(id: Int) {
        CoroutineBackgroundTask<ShoppingItem>()
            .executeInBackground { db.itemDao().getShoppingItemById(id) }
            .onDone {
                autoCmplTxtName.text = Editable.Factory.getInstance().newEditable(it.name)
            }
            .start()
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
            //getting data for picker
            val bigUnits = resources.getStringArray(R.array.big_units)
            val smallUnits = resources.getStringArray(R.array.small_units)

            numPickerCount.isSaveFromParentEnabled = false
            numPickerCount.isSaveEnabled = false
            numPickerCount.minValue = 0
            numPickerCount.maxValue = smallUnits.size-1
            numPickerCount.value = 0
            numPickerCount.displayedValues = smallUnits
            numPickerCount.setOnLongClickListener {
                numPickerCount.isVisible = false
                editTextCount.isVisible = true
                true
            }



            numPickerUnit.setOnValueChangedListener { npUnit, i, i2 ->
                println("UNIT: index: ${npUnit.value} value : ${npUnit.displayedValues[npUnit.value]}")

                when(npUnit.value) {
                    1, 3 -> {
                        numPickerCount.minValue = 0
                        numPickerCount.maxValue = bigUnits.size-1
                        numPickerCount.displayedValues = bigUnits
                    }
                    else -> {
                        numPickerCount.displayedValues = smallUnits
                        numPickerCount.minValue = 0
                        numPickerCount.maxValue = smallUnits.size-1
                    }
                }
            }
        } else {
            numPickerCount.isVisible = false
        }
        /*
         numPickerCount = findViewById<NumberPicker>(R.id.shopping_item_edit_numPick_count)
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            //getting data for picker
            val bigUnits = resources.getStringArray(R.array.big_units)
            val smallUnits = resources.getStringArray(R.array.small_units)

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

        */

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

    private fun getAmount(): Int? {
        if(numPickerCount.isVisible) {
            return numPickerCount.displayedValues[numPickerCount.value].toIntOrNull()
        } else {
            return editTextCount.text.toString().toIntOrNull()
        }
    }

    private fun getUnit(): String {
        return Units.stringValueArray(this)[numPickerUnit.value]
    }

    private fun getItemTitle(): String {
        return autoCmplTxtName.text.toString()
    }

    private fun addToDatabaseIfPossible() {

        //check if all input components are valid
        val title = getItemTitle()
        val amount = getAmount()
        val unit = getUnit()

        if(title.isNotBlank() && title.isNotEmpty() && amount != null){
            //all input components are valid -> creating object and put it into database via coroutine
            val item = ShoppingItem(title, amount, unit)

            GlobalScope.launch {
                db.itemDao().insertShopping(item)

            }
            finish()
        }
        else{
            Toast.makeText(this, resources.getString(R.string.errormsg_invalid_shoppingitem_parameters),Toast.LENGTH_LONG).show()
        }


    }
}
