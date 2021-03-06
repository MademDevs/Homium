package de.madem.homium.ui.activities.ingredient

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.view.isVisible
import de.madem.homium.R
import de.madem.homium.constants.*
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Product
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.models.Units
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.finishWithResultData
import de.madem.homium.utilities.extensions.hideKeyboard
import de.madem.homium.utilities.extensions.notNull
import de.madem.homium.utilities.extensions.showToastShort

class IngredientEditActivity : AppCompatActivity() {

    //fields
    private var ingredient : RecipeIngredient? = null

    private lateinit var valuesForCountPicker : List<String>
    private lateinit var valuesForUnitPicker : List<String>

    private lateinit var bigUnits : Array<String>
    private lateinit var smallUnits : Array<String>

    private var currentPickerCountMode = MODE_COUNT_VIA_PICKER

    //gui components
    private lateinit var txtIngredientName : AutoCompleteTextView
    private lateinit var  pickerCount : NumberPicker
    private lateinit var pickerUnit : NumberPicker
    private lateinit var editTxtCount : EditText

    //database
    private val dao = AppDatabase.getInstance().recipeDao()

    //companion
    companion object{

        //modes
        private const val MODE_COUNT_VIA_PICKER = 1
        private const val MODE_COUNT_VIA_TEXT = 2

        //Codes for save instance state
        private const val INSTANCE_STATE_KEY_COUNT_PICKER_VALUE = "COUNT_PICKER_VALUE"
        private const val INSTANCE_STATE_KEY_UNIT_PICKER_VALUE = "UNIT_PICKER_VALUE"
        private const val INSTANCE_STATE_KEY_COUNT_PICKER_DATA = "COUNT_PICKER_DATA"
        private const val INSTANCE_STATE_KEY_COUNT_PICKER_MODE = "COUNT_PICKER_MODE"
        private const val INSTANCE_STATE_KEY_COUNT_TXT_TEXT = "COUNT_TXT_TEXT"

    }


    //lifecycle functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredient_edit)

        //setup big and small units
        bigUnits = BIG_UNITS_VALUES
        smallUnits = SMALL_UNITS_VALUES

        //setup Actionbar
        setupActionbar()

        //getting Ingredient
        getIngredientFromDatabase(intent.getIntExtra(INTENT_DATA_TRANSFER_EDIT_INGREDIENT_ID,-1))

        //setup other layout components
        setupNameEditText()
        setupPickers(savedInstanceState?.getInt(INSTANCE_STATE_KEY_COUNT_PICKER_VALUE) ?: 0,
            savedInstanceState?.getInt(INSTANCE_STATE_KEY_UNIT_PICKER_VALUE) ?: 0,
            savedInstanceState?.getStringArray(INSTANCE_STATE_KEY_COUNT_PICKER_DATA) ?: smallUnits)
        currentPickerCountMode = savedInstanceState?.getInt(INSTANCE_STATE_KEY_COUNT_PICKER_MODE) ?: currentPickerCountMode
        setupCountEditText(text = savedInstanceState?.getString(INSTANCE_STATE_KEY_COUNT_TXT_TEXT))
        setupDeleteButton()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu.notNull {
            menuInflater.inflate(R.menu.activity_ingredients_edit_actionbar_menu,it)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.ingredients_edit_actionbar_confirm -> {
                val validInput = getValidComponentValuesIfPossible()

                if(validInput != null){
                    finishWithResultData(Activity.RESULT_OK){intent ->
                        with(intent){
                            putExtra(INTENT_DATA_TRANSFER_EDIT_INGREDIENT_NAME,validInput.first)
                            putExtra(INTENT_DATA_TRANSFER_EDIT_INGREDIENT_COUNT,validInput.second)
                            putExtra(INTENT_DATA_TRANSFER_EDIT_INGREDIENT_UNIT,validInput.third)
                        }
                    }
                    true
                }
                else{
                    showToastShort(R.string.errormsg_invalid_parameters)
                    false
                }
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putInt(INSTANCE_STATE_KEY_COUNT_PICKER_VALUE,pickerCount.value)
        outState.putInt(INSTANCE_STATE_KEY_UNIT_PICKER_VALUE,pickerUnit.value)
        outState.putStringArray(INSTANCE_STATE_KEY_COUNT_PICKER_DATA,valuesForCountPicker.toTypedArray())
        outState.putInt(INSTANCE_STATE_KEY_COUNT_PICKER_MODE,currentPickerCountMode)
        outState.putString(INSTANCE_STATE_KEY_COUNT_TXT_TEXT,editTxtCount.text.toString())

        super.onSaveInstanceState(outState)
    }

    //setup functions
    private fun setupNameEditText(){
        txtIngredientName = findViewById(R.id.ingredient_edit_autoCmplTxt_name)

        CoroutineBackgroundTask<List<Product>>()
            .executeInBackground {
                val result = AppDatabase.getInstance().itemDao().getAllProduct()
                return@executeInBackground result
            }.onDone {result ->
                val productNameList = result.map { it.name }
                txtIngredientName.setAdapter(ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, productNameList))

            }.start()

        txtIngredientName.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            setSpinnerDefaultValues(selectedItem)
            hideKeyboard()
        }
    }

    private fun setupActionbar(){
        with(supportActionBar){
            this?.setHomeButtonEnabled(true)
            this?.setDisplayHomeAsUpEnabled(true)
            title = resources.getString(R.string.screentitle_edit_ingredient_add)
        }
    }

    private fun setupCountEditText(text : String?){
        editTxtCount = findViewById(R.id.ingredient_edit_editTxt_count)

        if (ingredient != null){
            if(valuesForCountPicker.contains(ingredient?.count.toString())){
                val count = ingredient?.count ?: 0
                changeCountMode(to = MODE_COUNT_VIA_PICKER,currentCount = count)
            }
            else{
                changeCountMode(to = MODE_COUNT_VIA_TEXT,currentCount = 0)
                editTxtCount.text = Editable.Factory.getInstance().newEditable(ingredient?.count.toString())
            }
        }
        else{
            if(currentPickerCountMode == MODE_COUNT_VIA_TEXT){
                changeCountMode(MODE_COUNT_VIA_TEXT,null)
            }
            else{
                changeCountMode(MODE_COUNT_VIA_PICKER,null)
            }


        }

        text.notNull {
            editTxtCount.text = Editable.Factory.getInstance().newEditable(it)
        }


        editTxtCount.setOnLongClickListener {
            changeCountMode(to = MODE_COUNT_VIA_PICKER, currentCount = editTxtCount.text.toString().toIntOrNull())
            return@setOnLongClickListener true
        }
    }

    private fun setupPickers(countValue : Int , unitValue : Int, countPickerValues : Array<String>){
        //getting values for pickers
        valuesForCountPicker = countPickerValues.toList()
        valuesForUnitPicker = Units.stringValueArray(this).toList()

        //getting pickers as gui components
        pickerCount = findViewById(R.id.ingredient_edit_numPick_count)
        pickerUnit = findViewById(R.id.ingredient_edit_numPick_unit)

        //assigning data to picker
        with(pickerCount){
            displayedValues = valuesForCountPicker.toTypedArray()
            minValue = 0
            maxValue = valuesForCountPicker.lastIndex
            value = countValue
        }

        with(pickerUnit){
            displayedValues = valuesForUnitPicker.toTypedArray()
            minValue = 0
            maxValue = valuesForUnitPicker.lastIndex
            value = unitValue
        }

        //setting event listener
        pickerCount.setOnLongClickListener {
            changeCountMode(to = MODE_COUNT_VIA_TEXT,currentCount = getCount())
            true
        }

        pickerUnit.setOnValueChangedListener { numberPicker, _, _ ->
            println("PICKER UNIT ON VALUE CHANGED")
            val currentVal = pickerUnit.displayedValues[numberPicker.value]

            valuesForCountPicker = if(currentVal == Units.GRAM.getString(this) || currentVal == Units.MILLILITRE.getString(this)){
                bigUnits.toList()
            } else{
                smallUnits.toList()
            }

            pickerCount.displayedValues = valuesForCountPicker.toTypedArray()
        }

    }

    private fun setupDeleteButton(){
        findViewById<Button>(R.id.ingredient_btn_delete).apply {

            if(ingredient == null){
                this.isEnabled = false
                this.isVisible = false
            }
            else{
                this.isEnabled = true
                this.isVisible = true
            }

            setOnClickListener {
                ingredient.notNull {
                    //TODO: IMPLEMENT CODE FOR DELETING INGREDIENT
                }
            }


        }
    }

    private fun changeCountMode(to: Int, currentCount : Int?){
        when(to){
            MODE_COUNT_VIA_PICKER -> {
                pickerCount.isVisible = true
                editTxtCount.isVisible = false

                if(currentCount != null && valuesForCountPicker.contains(currentCount.toString())){
                    pickerCount.value = valuesForCountPicker.indexOf(currentCount.toString())
                }

                currentPickerCountMode = to

            }
            MODE_COUNT_VIA_TEXT -> {
                pickerCount.isVisible = false
                editTxtCount.isVisible = true

                currentCount.notNull {
                    editTxtCount.text = Editable.Factory.getInstance().newEditable(currentCount.toString())
                }

                currentPickerCountMode = to
            }
        }
    }

    private fun getIngredientFromDatabase(id : Int){

        if(id >= 0){
           CoroutineBackgroundTask<RecipeIngredient>()
               .executeInBackground{
                dao.getIngredientById(id)
           }.onDone { result ->
                result.notNull {
                    ingredient = it
                    assignIngredientValuesToGuiComponents()
                }
           }
        }
    }

    private fun assignIngredientValuesToGuiComponents(){
        ingredient.notNull {
            supportActionBar?.title = resources.getString(R.string.screentitle_edit_ingredient_edit)
            //TODO: Implement logic for assinging data of an existing ingredient to the gui components
        }
    }

    private fun getValidComponentValuesIfPossible() : Triple<String,Int,String>?{
        //check name
        val name = txtIngredientName.text.toString()
        if(name.isEmpty() || name.isBlank()){
            return null
        }

        //check count
        val count = getCount() ?: return null

        //check unit
        val unit = getUnit()
        return Triple(name,count,unit)
    }

    private fun getCount(): Int? {
        if(pickerCount.isVisible) {
            return pickerCount.displayedValues[pickerCount.value].toIntOrNull()
        } else {
            return editTxtCount.text.toString().toIntOrNull()
        }
    }

    private fun getUnit(): String {
        return Units.stringValueArray(this)[pickerUnit.value]
    }

    private fun setSpinnerDefaultValues(name: String) {
        CoroutineBackgroundTask<Product>()
            .executeInBackground { AppDatabase.getInstance().itemDao().getProductsByName(name)[0] }
            .onDone {

                //setting unit
                pickerUnit.value = Units.stringValueArray(this).indexOf(it.unit)

                //setting amount
                if(currentPickerCountMode == MODE_COUNT_VIA_TEXT){
                    editTxtCount.text = Editable.Factory.getInstance().newEditable(it.amount)
                }
                else{
                    setValuesForNumPickerCount(pickerUnit)

                    if(pickerCount.displayedValues.contains(it.amount)){
                        pickerCount.value = pickerCount.displayedValues.indexOf(it.amount)
                    }
                    else{
                        changeCountMode(MODE_COUNT_VIA_TEXT,it.amount.toIntOrNull() ?: 1)
                        //assignValueFromPickerToEditText(it.amount)
                    }

                }
            }
            .start()

    }

    private fun setValuesForNumPickerCount(numberPickerUnit: NumberPicker){
        when(numberPickerUnit.value) {
            1, 3 -> {
                pickerCount.minValue = 0
                pickerCount.maxValue = bigUnits.size-1
                pickerCount.displayedValues = bigUnits
                valuesForCountPicker = bigUnits.toList()
            }
            else -> {
                pickerCount.displayedValues = smallUnits
                pickerCount.minValue = 0
                pickerCount.maxValue = smallUnits.size-1
                valuesForCountPicker = smallUnits.toList()
            }
        }
    }



}
