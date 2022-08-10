package de.madem.homium.ui.activities.shoppingitem

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
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
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
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
    private var itemid: Int = -1

    companion object{
        private val LOG_TAG : String = ShoppingItemEditActivity::class.simpleName ?: ""
    }

    //ON CREATE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingItemEditBinding.inflate(layoutInflater).also {
            setContentView(it.root)
            initGuiComponents(it)
            setupViewModel(it)
        }

        itemid = intent.getIntExtra(INTENT_DATA_TRANSFER_EDIT_SHOPPING_ITEM_ID, -1)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
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

    //private functions
    private fun initGuiComponents(binding: ActivityShoppingItemEditBinding){
        //init Actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        //init delete button
        binding.shoppingItemEditBtnDelete.setOnClickListener{
            AlertDialog.Builder(this)
                .setMessage(R.string.shopping_item_delete_question)
                .setPositiveButton(R.string.answer_yes) { dialog, _ ->
                    CoroutineBackgroundTask<Unit>()
                        .executeInBackground {
                            if(itemid >= 0) {
                                //TODO move to vm
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
        binding.shoppingItemEditAutoCmplTxtName.addTextChangedListener {
            viewModel.setEditItemName(it?.toString() ?: "")
        }

        binding.shoppingItemEditAutoCmplTxtName.apply {
            onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                val productName = parent.getItemAtPosition(position).toString()
                viewModel.loadProductByName(productName)
                hideKeyboard()
            }
        }

        //init numberpicker
        val units = viewModel.units.map { resources.getString(it.resourceId) }.toTypedArray()
        binding.shoppingItemEditNumPickUnit.apply {
            isSaveFromParentEnabled = false
            isSaveEnabled = false
            minValue = units.indices.first
            maxValue = units.indices.last
            setDistinctDisplayedValues(units)
            setOnValueChangedListener { npUnit, _, newIdx ->
                println("UNIT: index: ${npUnit.value} value : ${npUnit.displayedValues[npUnit.value]}")
                viewModel.setSelectedUnitByIndex(newIdx)
            }
        }

        binding.shoppingItemEditNumPickCount.apply {
            isSaveFromParentEnabled = false
            isSaveEnabled = false
            setOnLongClickListener {
                viewModel.setCounterStateToCustomType()
                true
            }
            setOnValueChangedListener { _, _, newVal ->
                viewModel.setCounterStateInRangeSelectedIndex(newVal)
            }
        }

        binding.shoppingItemEditEditTxtCount.apply {
            setDistinctVisibility(View.GONE)
            setOnLongClickListener {
                hideKeyboard()
                viewModel.setCounterStateToInRangeType()
                true
            }
            addTextChangedListener {
                if(this.visibility == View.VISIBLE){
                    viewModel.setCounterStateCustomWithValue(it?.toString() ?: "")
                }
            }
        }
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

    //TODO move to vm
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
                        shoppingItemEditEditTxtCount.setDistinctVisibility(View.INVISIBLE)
                        shoppingItemEditNumPickCount.setDistinctVisibility(View.VISIBLE)
                        val dataset = cntState.dataset
                        shoppingItemEditNumPickCount.apply {
                            minValue = dataset.indices.first
                            maxValue = dataset.indices.last
                            setDistinctDisplayedValues(dataset)
                            setDistinctValue(cntState.selectedIndex)
                        }
                    }
                    is ShoppingCounterState.Custom -> {
                        shoppingItemEditNumPickCount.setDistinctVisibility(View.INVISIBLE)

                        shoppingItemEditEditTxtCount.setDistinctVisibility(View.VISIBLE)
                        shoppingItemEditEditTxtCount.setDistinctText(cntState.value)
                    }
                }
            }
        }

        viewModel.allProducts.onCollect(this) { products ->
            val productNameList = products.map { it.name }
            binding.shoppingItemEditAutoCmplTxtName.setAdapter(
                ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    productNameList
                )
            )
        }
    }
}
