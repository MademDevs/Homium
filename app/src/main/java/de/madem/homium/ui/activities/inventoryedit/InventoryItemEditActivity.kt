package de.madem.homium.ui.activities.inventoryedit

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Product
import de.madem.homium.models.Units
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.*

class InventoryItemEditActivity : AppCompatActivity() {

    //GUI Components
    private lateinit var btnDelete: Button
    private lateinit var autoCmplTxtName: AutoCompleteTextView
    private lateinit var autoCmplTxtLocation: AutoCompleteTextView
    private lateinit var numPickerCount: NumberPicker
    private lateinit var numPickerUnit: NumberPicker
    private lateinit var editTextCount: EditText


    //fields
    private val db = AppDatabase.getInstance()
    private lateinit var bigUnits: Array<String>
    private lateinit var smallUnits: Array<String>
    private var itemid: Int = -1

    //ON CREATE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_item_edit)

        //getting data from ressources
        bigUnits = resources.getStringArray(R.array.big_units)
        smallUnits = resources.getStringArray(R.array.small_units)

        //init action bar
        initActionbar()

        initGuiComponents()
        itemid = intent.getIntExtra("item", -1)

        if (itemid >= 0) {
            btnDelete.isVisible = true
            setInventoryItemToView(itemid)
        } else {
            btnDelete.isVisible = false
        }

        updateSpinnerOnItemSelected()
    }

    private fun initActionbar() =
        withNotNull(supportActionBar) {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setTitle(
                if (itemid >= 0) R.string.screentitle_edit_inventoryitem_edit
                else R.string.screentitle_edit_inventoryitem_add
            )
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("unit", numPickerUnit.value)
        outState.putInt("count", numPickerCount.value)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        numPickerCount.value = savedInstanceState.getInt("count")
        numPickerUnit.value = savedInstanceState.getInt("unit")
    }

    //optionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            menuInflater.inflate(R.menu.inventoryitem_edit_actionbar_menu, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.inventory_item_edit_actionbar_confirm -> addOrUpdateToDatabaseIfPossible()
            android.R.id.home -> {
                finishWithBooleanResult("dataChanged", false, Activity.RESULT_OK)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setInventoryItemToView(id: Int) {
        CoroutineBackgroundTask<InventoryItem>()
            .executeInBackground { db.inventoryDao().fetchInventoryItemById(id) }
            .onDone {
                //setting name
                autoCmplTxtName.text = Editable.Factory.getInstance().newEditable(it.name)

                //set location
                autoCmplTxtLocation.text = Editable.Factory.getInstance().newEditable(it.location)

                //setting unit
                numPickerUnit.value = Units.stringValueArray(this).indexOf(it.unit)

                //setting amount
                if (editTextCount.isVisible) {
                    editTextCount.text =
                        Editable.Factory.getInstance().newEditable(it.count.toString())
                } else {
                    setValuesForNumPickerCount(numPickerUnit)

                    if (numPickerCount.displayedValues.contains(it.count.toString())) {
                        numPickerCount.value =
                            numPickerCount.displayedValues.indexOf(it.count.toString())
                    } else {
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
                if (editTextCount.isVisible) {
                    editTextCount.text = Editable.Factory.getInstance().newEditable(it.amount)
                } else {
                    setValuesForNumPickerCount(numPickerUnit)

                    if (numPickerCount.displayedValues.contains(it.amount)) {
                        numPickerCount.value = numPickerCount.displayedValues.indexOf(it.amount)
                    } else {
                        assignValueFromPickerToEditText(it.amount)
                    }

                }
            }
            .start()

    }

    private fun updateSpinnerOnItemSelected() {
        autoCmplTxtName.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                setSpinnerDefaultValues(selectedItem)
                hideKeyboard()
            }
    }


    private fun initGuiComponents() {
        //init delete button
        btnDelete = findViewById(R.id.inventory_item_button_delete)
        btnDelete.setOnClickListener {

            AlertDialog.Builder(this)
                .setMessage(R.string.inventory_edit_button_delete_question)
                .setPositiveButton(R.string.answer_yes) { dialog, _ ->
                    CoroutineBackgroundTask<Unit>()
                        .executeInBackground {
                            if (itemid >= 0) {
                                db.inventoryDao().deleteInventoryItemById(itemid)
                            }
                        }
                        .onDone {
                            showToastShort(R.string.notification_delete_inventoryitem_sucess)
                            dialog.dismiss()
                            finishWithBooleanResult("dataChanged", true, Activity.RESULT_OK)
                        }
                        .start()
                }
                .setNegativeButton(R.string.answer_no) { dialog, _ ->
                    dialog.dismiss()
                }.show()

        }

        //init txt autocomplete
        autoCmplTxtName = findViewById(R.id.inventory_item_edit_name)

        CoroutineBackgroundTask<List<Product>>()
            .executeInBackground {
            val result = db.itemDao().getAllProduct()
            return@executeInBackground result
        }.onDone { result ->
            val productNameList = result.map { it.name }
            autoCmplTxtName.setAdapter(
                ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    productNameList
                )
            )

        }.start()

        //init location
        val locations = arrayOf("Kühlschrank", "Speisekeller", "Gefrierschrank")

        autoCmplTxtLocation = findViewById(R.id.inventory_item_edit_location)
        autoCmplTxtLocation.setAdapter(
            ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, locations)
        )
        autoCmplTxtLocation.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                hideKeyboard()
            }

        //init numberpicker
        val units = Units.stringValueArray(this)
        numPickerUnit = findViewById<NumberPicker>(R.id.inventory_item_picker_unit).also {
            it.isSaveFromParentEnabled = false
            it.isSaveEnabled = false
            it.minValue = 0
            it.maxValue = units.size - 1
            it.displayedValues = units
            it.value = 0
        }


        numPickerCount = findViewById<NumberPicker>(R.id.inventory_item_picker_count)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            //getting data for picker


            numPickerCount.isSaveFromParentEnabled = false
            numPickerCount.isSaveEnabled = false
            numPickerCount.minValue = 0
            numPickerCount.maxValue = smallUnits.size - 1
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

        editTextCount = findViewById<EditText>(R.id.inventory_item_input_count).also {
            it.isVisible = false
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
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

    private fun setValuesForNumPickerCount(numberPickerUnit: NumberPicker) {
        when (numberPickerUnit.value) {
            1, 3 -> {
                numPickerCount.minValue = 0
                numPickerCount.maxValue = bigUnits.size - 1
                numPickerCount.displayedValues = bigUnits
            }
            else -> {
                numPickerCount.displayedValues = smallUnits
                numPickerCount.minValue = 0
                numPickerCount.maxValue = smallUnits.size - 1
            }
        }
    }

    private fun assignValueFromPickerToEditText(value: String): Boolean {
        numPickerCount.isVisible = false
        editTextCount.isVisible = true
        editTextCount.text = Editable.Factory.getInstance().newEditable(value)
        return true
    }

    private fun getAmount(): Int? {
        if (numPickerCount.isVisible) {
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

    private fun getLocation(): String {
        return autoCmplTxtLocation.text.toString()
    }

    private fun addOrUpdateToDatabaseIfPossible() {

        //check if all input components are valid
        val title = getItemTitle()
        val amount = getAmount()
        val unit = getUnit()
        val location = getLocation()

        if (title.isNotBlank() && title.isNotEmpty() && amount != null) {
            //all input components are valid -> creating object and put it into database via coroutine
            val item = InventoryItem(title, amount, unit, location)

            CoroutineBackgroundTask<Unit>()
                .executeInBackground {
                if (itemid >= 0) {
                    db.inventoryDao().updateInventoryItem(itemid, title, amount, unit, location)
                } else {
                    db.inventoryDao().insertInventoryItems(item)
                }
            }.onDone {
                finishWithBooleanResult("dataChanged", true, Activity.RESULT_OK)
            }.start()

        } else {
            showToastLong(R.string.errormsg_invalid_parameters)
        }
    }

}