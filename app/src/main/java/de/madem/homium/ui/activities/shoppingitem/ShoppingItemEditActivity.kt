package de.madem.homium.ui.activities.shoppingitem

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import de.madem.homium.R
import de.madem.homium.constants.BIG_UNITS_VALUES
import de.madem.homium.constants.SMALL_UNITS_VALUES
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Product
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.finishWithBooleanResult
import de.madem.homium.utilities.extensions.hideKeyboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShoppingItemEditActivity : AppCompatActivity() {

    //GUI Components
    private lateinit var btnDelete : Button
    private lateinit var autoCmplTxtName : AutoCompleteTextView
    private lateinit var numPickerCount: NumberPicker
    private lateinit var numPickerUnit: NumberPicker
    private lateinit var editTextCount: EditText


    //fields
    //TODO: switch to ViewModel to get rid of DB-Reference in UI-Controller
    @Inject
    lateinit var db : AppDatabase
    private lateinit var bigUnits : Array<String>
    private lateinit var smallUnits : Array<String>
    private var itemid: Int = -1

    companion object{
        private const val SAVEINSTANCESTATE_UNIT_INDEX = "sist_unit_index";
        private const val SAVEINSTANCESTATE_COUNT = "sist_count";

    }

    //ON CREATE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_item_edit)


        //getting data from ressources
        bigUnits = BIG_UNITS_VALUES
        smallUnits = SMALL_UNITS_VALUES


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)


        initGuiComponents()
        itemid = intent.getIntExtra("item", -1)
        if(itemid >= 0) {
            btnDelete.isVisible = true
            setShoppingItemToElements(itemid)
            supportActionBar?.title = resources.getString(R.string.screentitle_edit_shoppingitem_edit)
        } else {
            supportActionBar?.title = resources.getString(R.string.screentitle_edit_shopppingitem_add)
            btnDelete.isVisible = false
        }

        updateSpinnerOnItemSelected()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //unit index
        outState.putInt(SAVEINSTANCESTATE_UNIT_INDEX, numPickerUnit.value)

        //count value
        if(numPickerCount.isVisible){
            outState.putString(SAVEINSTANCESTATE_COUNT, numPickerCount.displayedValues[numPickerCount.value])
        }
        else{
            outState.putString(SAVEINSTANCESTATE_COUNT, editTextCount.text.toString())
        }

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //unit value
        numPickerUnit.value = savedInstanceState.getInt(SAVEINSTANCESTATE_UNIT_INDEX)

        //count value
        val countValue = savedInstanceState.getString(SAVEINSTANCESTATE_COUNT) ?: smallUnits[0]

        if(bigUnits.contains(countValue)){
            numPickerCount.displayedValues = bigUnits
            numPickerCount.value = bigUnits.indexOf(countValue)
        }
        else if(smallUnits.contains(countValue)){
            numPickerCount.displayedValues = smallUnits
            numPickerCount.value = smallUnits.indexOf(countValue)
        }
        else{
            numPickerCount.isVisible = false
            editTextCount.setText(countValue)
            editTextCount.isVisible = true;
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
            R.id.shopping_item_edit_actionbar_confirm ->  addOrUpdateToDatabaseIfPossible()
            android.R.id.home -> finishWithBooleanResult("dataChanged",false, Activity.RESULT_OK)
        }

        return super.onOptionsItemSelected(item)

    }

    private fun setShoppingItemToElements(id: Int) {
        CoroutineBackgroundTask<ShoppingItem>()
            .executeInBackground { db.itemDao().getShoppingItemById(id) }
            .onDone {
                //setting name
                autoCmplTxtName.text = Editable.Factory.getInstance().newEditable(it.name)

                //setting unit
                numPickerUnit.value = Units.stringValueArray(this).indexOf(it.unit)

                    //setting amount
                    if(editTextCount.isVisible){
                        editTextCount.text = Editable.Factory.getInstance().newEditable(it.count.toString())
                    }
                    else{
                        setValuesForNumPickerCount(numPickerUnit)

                        if(numPickerCount.displayedValues.contains(it.count.toString())){
                            numPickerCount.value = numPickerCount.displayedValues.indexOf(it.count.toString())
                        }
                        else{
                            assignValueFromPickerToEditText(it.count.toString())
                        }

                    }
                }
                    .start()

            }

    //private fuctions
    private fun setSpinnerDefaultValues(name: String) {
        CoroutineBackgroundTask<Product>()
            .executeInBackground { db.itemDao().getProductsByName(name)[0] }
            .onDone {

                //setting unit
                numPickerUnit.value = Units.stringValueArray(this).indexOf(it.unit)

                //setting amount
                if(editTextCount.isVisible){
                    editTextCount.text = Editable.Factory.getInstance().newEditable(it.amount)
                }
                else{
                    setValuesForNumPickerCount(numPickerUnit)

                    if(numPickerCount.displayedValues.contains(it.amount)){
                        numPickerCount.value = numPickerCount.displayedValues.indexOf(it.amount)
                    }
                    else{
                        assignValueFromPickerToEditText(it.amount)
                    }

                }
            }
            .start()

    }
    private fun updateSpinnerOnItemSelected() {
        autoCmplTxtName.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            setSpinnerDefaultValues(selectedItem)
            hideKeyboard()
        }
    }


    private fun initGuiComponents(){

        //init delete button
        btnDelete = findViewById(R.id.shopping_item_edit_btn_delete)
        btnDelete.setOnClickListener{

            AlertDialog.Builder(this)
                .setMessage(R.string.shopping_item_delete_question)
                .setPositiveButton(R.string.answer_yes) { dialog, _ ->
                    CoroutineBackgroundTask<Unit>()
                        .executeInBackground {
                            if(itemid >= 0) {
                                db.itemDao().deleteShoppingItemById(itemid)
                            }
                        }
                        .onDone {
                            Toast.makeText(this,resources.getString(R.string.notification_delete_shoppingitem_sucess),Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            finishWithBooleanResult("dataChanged",true, Activity.RESULT_OK)
                        }
                        .start()
                }
                .setNegativeButton(R.string.answer_no) { dialog, _ ->
                    dialog.dismiss()
                }.show()

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
            numPickerCount.isSaveFromParentEnabled = false
            numPickerCount.isSaveEnabled = false
            numPickerCount.minValue = 0
            numPickerCount.maxValue = smallUnits.size-1
            numPickerCount.value = 0
            numPickerCount.displayedValues = smallUnits
            numPickerCount.setOnLongClickListener {
                assignValueFromPickerToEditText(numPickerCount.displayedValues[numPickerCount.value])

            }

            numPickerUnit.setOnValueChangedListener { npUnit, i, i2 ->
                println("UNIT: index: ${npUnit.value} value : ${npUnit.displayedValues[npUnit.value]}")
                setValuesForNumPickerCount(npUnit)
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

    private fun setValuesForNumPickerCount(numberPickerUnit: NumberPicker){
        when(numberPickerUnit.value) {
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

    private fun assignValueFromPickerToEditText(value : String) : Boolean{
        numPickerCount.isVisible = false
        editTextCount.isVisible = true
        editTextCount.text = Editable.Factory.getInstance().newEditable(value)
        return true
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

    private fun addOrUpdateToDatabaseIfPossible() {

        //check if all input components are valid
        val title = getItemTitle()
        val amount = getAmount()
        val unit = getUnit()

        if(title.isNotBlank() && title.isNotEmpty() && amount != null){
            //all input components are valid -> creating object and put it into database via coroutine
            val item = ShoppingItem(title, amount, unit)

            CoroutineBackgroundTask<Unit>()
                .executeInBackground {
                if(itemid >= 0){
                    db.itemDao().updateShoppingItemById(itemid, title, amount, unit)
                }
                else{
                    db.itemDao().insertShopping(item)
                }
            }.onDone {
                finishWithBooleanResult("dataChanged",true, Activity.RESULT_OK)
            }.start()

        }
        else{
            Toast.makeText(this, resources.getString(R.string.errormsg_invalid_parameters),Toast.LENGTH_LONG).show()
        }

        

    }
}
