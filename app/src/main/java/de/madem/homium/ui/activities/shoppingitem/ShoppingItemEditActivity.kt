package de.madem.homium.ui.activities.shoppingitem

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import de.madem.homium.R
import de.madem.homium.constants.INTENT_DATA_TRANSFER_EDIT_SHOPPING_ITEM_ID
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databinding.ActivityShoppingItemEditBinding
import de.madem.homium.models.*
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.*
import javax.inject.Inject

@AndroidEntryPoint
class ShoppingItemEditActivity : AppCompatActivity() {
    //fields
    private var binding: ActivityShoppingItemEditBinding? = null
    private val viewModel : ShoppingItemEditViewModel by viewModels()

    //TODO: switch to ViewModel to get rid of DB-Reference in UI-Controller
    @Inject
    lateinit var db : AppDatabase
    private lateinit var bigUnits : Array<String>
    private lateinit var smallUnits : Array<String>
    private var itemid: Int = -1

    companion object{
        private const val SAVEINSTANCESTATE_UNIT_INDEX = "sist_unit_index"
        private const val SAVEINSTANCESTATE_COUNT = "sist_count"

        private val LOG_TAG : String = ShoppingItemEditActivity::class.simpleName ?: ""

    }

    //ON CREATE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //getting data from ressources
        bigUnits = SMALL_UNITS_DATASET
        smallUnits = BIG_UNITS_AND_NON_CONVERTABLE_DATASET


        binding = ActivityShoppingItemEditBinding.inflate(layoutInflater).also {
            setContentView(it.root)
            initGuiComponents(it)
            setupViewModel(it)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)


        itemid = intent.getIntExtra(INTENT_DATA_TRANSFER_EDIT_SHOPPING_ITEM_ID, -1)
        if(itemid >= 0) {
            setShoppingItemToElements(itemid)
        }

        updateSpinnerOnItemSelected()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.notNull {
            //count value
            val numPickerCount = it.shoppingItemEditNumPickCount
            if(numPickerCount.isVisible){
                //outState.putString(SAVEINSTANCESTATE_COUNT, numPickerCount.displayedValues[numPickerCount.value])
            }
            else{
                outState.putString(SAVEINSTANCESTATE_COUNT, it.shoppingItemEditEditTxtCount.text.toString())
            }
        }

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //unit value
        binding.notNull {
            val numPickerCount = it.shoppingItemEditNumPickCount
            val editTextCount = it.shoppingItemEditEditTxtCount

            //count value
            val countValue = savedInstanceState.getString(SAVEINSTANCESTATE_COUNT) ?: smallUnits[0]

            if(bigUnits.contains(countValue)){
                //numPickerCount.displayedValues = bigUnits
                //numPickerCount.value = bigUnits.indexOf(countValue)
            }
            else if(smallUnits.contains(countValue)){
                //numPickerCount.displayedValues = smallUnits
                //numPickerCount.value = smallUnits.indexOf(countValue)
            }
            else{
                //numPickerCount.visibility = View.INVISIBLE
                editTextCount.setText(countValue)
                editTextCount.visibility = View.VISIBLE
            }
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
            .executeInBackground { db.shoppingDao().getShoppingItemByIdSync(id) }
            .onDone {
                binding.notNull { binding ->
                    val editTextCount = binding.shoppingItemEditEditTxtCount
                    val numPickerCount = binding.shoppingItemEditNumPickCount

                    //setting amount
                    if(editTextCount.isVisible){
                        editTextCount.text = Editable.Factory.getInstance().newEditable(it.count.toString())
                    }
                    else{
                        if(numPickerCount.displayedValues.contains(it.count.toString())){
                            //numPickerCount.value = numPickerCount.displayedValues.indexOf(it.count.toString())
                        }
                        else{
                            assignValueFromPickerToEditText(it.count.toString())
                        }

                    }
                }
            }.start()

    }

    //private functions
    //TODO move this logic to ViewModel
    private fun setSpinnerDefaultValues(name: String) {
        CoroutineBackgroundTask<Product>()
            .executeInBackground { db.shoppingDao().getProductsByName(name)[0] }
            .onDone {
                binding.notNull { binding ->
                    //setting unit
                    binding.shoppingItemEditNumPickUnit.value = Units.stringValueArray(this).indexOf(it.unit)

                    //setting amount
                    val editTextCount = binding.shoppingItemEditEditTxtCount
                    val numPickerCount = binding.shoppingItemEditNumPickCount
                    if(editTextCount.isVisible){
                        editTextCount.text = Editable.Factory.getInstance().newEditable(it.amount)
                    }
                    else{
                        //TODO set values to picker
                        //setValuesForNumPickerCount(binding.shoppingItemEditNumPickUnit)

                        if(numPickerCount.displayedValues.contains(it.amount)){
                            numPickerCount.value = numPickerCount.displayedValues.indexOf(it.amount)
                        }
                        else{
                            assignValueFromPickerToEditText(it.amount)
                        }
                    }
                }
            }
            .start()

    }
    private fun updateSpinnerOnItemSelected() {
        binding?.shoppingItemEditAutoCmplTxtName?.notNull {
            it.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                setSpinnerDefaultValues(selectedItem)
                hideKeyboard()
            }
        }
    }


    private fun initGuiComponents(binding: ActivityShoppingItemEditBinding){
        //init delete button
        binding.shoppingItemEditBtnDelete.setOnClickListener{
            AlertDialog.Builder(this)
                .setMessage(R.string.shopping_item_delete_question)
                .setPositiveButton(R.string.answer_yes) { dialog, _ ->
                    CoroutineBackgroundTask<Unit>()
                        .executeInBackground {
                            if(itemid >= 0) {
                                db.shoppingDao().deleteShoppingItemById(itemid)
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
        CoroutineBackgroundTask<List<Product>>().executeInBackground {
            val result = db.shoppingDao().getAllProduct()
            return@executeInBackground result
        }.onDone {result ->
            val productNameList = result.map { it.name }
            binding.shoppingItemEditAutoCmplTxtName.setAdapter(
                ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    productNameList
                )
            )
        }.start()


        //init numberpicker
        val units = viewModel.units.map { resources.getString(it.resourceId) }.toTypedArray()
        val numPickerUnit = binding.shoppingItemEditNumPickUnit.apply {
            isSaveFromParentEnabled = false
            isSaveEnabled = false
            minValue = units.indices.first
            maxValue = units.indices.last
            displayedValues = units
        }

        val numPickerCount = binding.shoppingItemEditNumPickCount
        //getting data for picker
        numPickerCount.isSaveFromParentEnabled = false
        numPickerCount.isSaveEnabled = false
        //numPickerCount.minValue = 0
        //numPickerCount.maxValue = smallUnits.size-1
        //numPickerCount.value = 0
        //numPickerCount.displayedValues = smallUnits
        numPickerCount.setOnLongClickListener {
            assignValueFromPickerToEditText(numPickerCount.displayedValues[numPickerCount.value])
        }

        numPickerCount.setOnValueChangedListener { _, _, newVal ->
            viewModel.setCounterStateInRangeSelectedIndex(newVal)
        }

        numPickerUnit.setOnValueChangedListener { npUnit, _, newIdx ->
            println("UNIT: index: ${npUnit.value} value : ${npUnit.displayedValues[npUnit.value]}")
            viewModel.setSelectedUnitByIndex(newIdx)
        }

        binding.shoppingItemEditEditTxtCount.also { ed ->
            ed.visibility = View.GONE
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                ed.setOnLongClickListener {
                    ed.visibility = View.GONE
                    numPickerCount.visibility = View.VISIBLE
                    true
                }
            } else {
                ed.visibility = View.VISIBLE
            }
        }

        binding.shoppingItemEditAutoCmplTxtName.addTextChangedListener {
            viewModel.setEditItemName(it?.toString() ?: "")
        }
    }

    private fun assignValueFromPickerToEditText(value : String) : Boolean{
        binding?.shoppingItemEditNumPickCount?.visibility = View.INVISIBLE
        binding?.shoppingItemEditEditTxtCount?.visibility = View.VISIBLE
        binding?.shoppingItemEditEditTxtCount?.text = Editable.Factory.getInstance().newEditable(value)
        return true
    }

    private fun getAmount(): Int? = binding?.let {
        val numPickerCount = it.shoppingItemEditNumPickCount
        if(numPickerCount.isVisible) {
            return numPickerCount.displayedValues[numPickerCount.value].toIntOrNull()
        } else {
            return it.shoppingItemEditEditTxtCount.text.toString().toIntOrNull()
        }
    }

    private fun getUnit(): String {
        val numPickerUnit = binding?.shoppingItemEditNumPickUnit
        return Units.stringValueArray(this)[numPickerUnit?.value ?: 0]
    }

    @Deprecated("Transfer logic into ViewModel")
    private fun getItemTitle(): String {
        return binding?.shoppingItemEditAutoCmplTxtName?.text?.toString() ?: ""
    }

    private fun addOrUpdateToDatabaseIfPossible() {

        //check if all input components are valid
        val title = getItemTitle()
        val amount = getAmount()
        val unit = getUnit()

        if(title.isNotBlank() && title.isNotEmpty() && amount != null){
            //all input components are valid -> creating object and put it into database via coroutine
            val item = ShoppingItem(title, amount, Units.unitOf(unit) ?: Units.default)

            CoroutineBackgroundTask<Unit>()
                .executeInBackground {
                if(itemid >= 0){
                    db.shoppingDao().updateShoppingItemById(itemid, title, amount, unit)
                }
                else{
                    db.shoppingDao().insertShopping(item)
                }
            }.onDone {
                finishWithBooleanResult("dataChanged",true, Activity.RESULT_OK)
            }.start()

        }
        else{
            Toast.makeText(this, resources.getString(R.string.errormsg_invalid_parameters),Toast.LENGTH_LONG).show()
        }
    }

    private fun setupViewModel(binding: ActivityShoppingItemEditBinding) {
        viewModel.actionTitleResId.onCollect(this) { resId ->
            try {
                supportActionBar?.title = resources.getString(resId)
            } catch (ex: Resources.NotFoundException) {
                Log.e(LOG_TAG, ex.message, ex)
            }
        }

        viewModel.showDeleteButton.onCollect(this) {
            binding.shoppingItemEditBtnDelete.isVisible = it
        }

        viewModel.editItemName.onCollect(this) { title ->
            binding.shoppingItemEditAutoCmplTxtName.setDistinctText(title)
        }

        viewModel.selectedUnitIndex.onCollect(this) {
            if(it in viewModel.units.indices) {
                binding.shoppingItemEditNumPickUnit.setDistinctValue(it)
            }
        }

        viewModel.counterState.onCollect(this) { cntState ->
            with(binding){
                when (cntState) {
                    is ShoppingCounterState.InRange -> {
                        val dataset = cntState.dataset
                        shoppingItemEditNumPickCount.apply {
                            minValue = dataset.indices.first
                            maxValue = dataset.indices.last
                            displayedValues = dataset
                            setDistinctValue(cntState.selectedIndex)
                        }
                        //TODO Set visibility of EditText
                    }
                    is ShoppingCounterState.Custom -> {
                        //TODO handle Custom CounterStates
                    }
                }
            }
        }
    }
}
