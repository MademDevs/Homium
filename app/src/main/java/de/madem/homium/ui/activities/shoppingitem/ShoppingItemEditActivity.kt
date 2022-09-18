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
import de.madem.homium.databinding.ActivityShoppingItemEditBinding
import de.madem.homium.errors.businesslogicerrors.DeletionFailedException
import de.madem.homium.errors.presentationerrors.ValidationException
import de.madem.homium.utilities.extensions.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingItemEditActivity : AppCompatActivity() {
    companion object {
        private val LOG_TAG: String =
            ShoppingItemEditActivity::class.simpleName ?: "ShoppingItemEditActivity"
    }

    private val viewModel: ShoppingItemEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityShoppingItemEditBinding.inflate(layoutInflater).also {
            setContentView(it.root)
            setupGuiComponents(it)
            setupViewModelObservers(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            menuInflater.inflate(R.menu.shoppingitem_edit_actionbar_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.shopping_item_edit_actionbar_confirm -> {
                viewModel.mergeShoppingItem().onCollect(this) { success ->
                    if (success) {
                        //TODO change to normal finish and reactive approach
                        finishWithBooleanResult("dataChanged", true, Activity.RESULT_OK)
                    }
                }
            }
            android.R.id.home -> finishWithBooleanResult("dataChanged", false, Activity.RESULT_OK)
        }
        return super.onOptionsItemSelected(item)
    }

    //region private functions
    private fun setupGuiComponents(binding: ActivityShoppingItemEditBinding) {
        setupActionbar()
        setupDeleteButton(binding)
        setupAutoCompleteTextView(binding)
        setupUnitPicker(binding)
        setupCountPicker(binding)
        setupEditTextCount(binding)
    }

    private fun setupActionbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun setupDeleteButton(binding: ActivityShoppingItemEditBinding) {
        binding.shoppingItemEditBtnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(R.string.shopping_item_delete_question)
                .setPositiveButton(R.string.answer_yes) { dialog, _ ->
                    viewModel.deleteShoppingItem().onCollect(this) { success ->
                        if (success) {
                            Toast.makeText(
                                this,
                                R.string.notification_delete_shoppingitem_sucess,
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                            //TODO exchange with reactive approach and just calling finish
                            finishWithBooleanResult("dataChanged", true, Activity.RESULT_OK)
                        }
                    }
                }
                .setNegativeButton(R.string.answer_no) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }
    }

    private fun setupAutoCompleteTextView(binding: ActivityShoppingItemEditBinding) {
        binding.shoppingItemEditAutoCmplTxtName.apply {
            addTextChangedListener { viewModel.setEditItemName(it?.toString() ?: "") }
            onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                val productName = parent.getItemAtPosition(position).toString()
                viewModel.loadProductByName(productName)
                hideKeyboard()
            }
        }
    }

    private fun setupUnitPicker(binding: ActivityShoppingItemEditBinding) {
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
    }

    private fun setupCountPicker(binding: ActivityShoppingItemEditBinding) {
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
    }

    private fun setupEditTextCount(binding: ActivityShoppingItemEditBinding) {
        binding.shoppingItemEditEditTxtCount.apply {
            setDistinctVisibility(View.GONE)
            setOnLongClickListener {
                hideKeyboard()
                viewModel.setCounterStateToInRangeType()
                true
            }
            addTextChangedListener {
                if (this.visibility == View.VISIBLE) {
                    viewModel.setCounterStateCustomWithValue(it?.toString() ?: "")
                }
            }
        }
    }

    private fun setupViewModelObservers(binding: ActivityShoppingItemEditBinding) {
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
            if (it in viewModel.units.indices) {
                binding.shoppingItemEditNumPickUnit.setDistinctValue(it)
            }
        }

        viewModel.counterState.onCollect(this) { cntState ->
            with(binding) {
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

        viewModel.errors.onCollect(this) {
            val errMsgResId = when (it) {
                is DeletionFailedException -> R.string.errormsg_delete_shopping_failed
                is ValidationException -> it.errMsgResId
                else -> R.string.error
            }
            Toast.makeText(this, errMsgResId, Toast.LENGTH_SHORT).show()
        }
    }
    //endregion
}
