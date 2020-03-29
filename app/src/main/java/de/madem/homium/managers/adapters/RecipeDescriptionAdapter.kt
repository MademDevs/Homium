package de.madem.homium.managers.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.models.RecipeDescription

class RecipeDescriptionAdapter(private val context : Context, private var data : List<RecipeDescription>)
    : RecyclerView.Adapter<RecipeDescriptionAdapter.RecipeDescriptionViewHolder>() {

    //overridden functions
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeDescriptionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recipe_edit_description,parent,false)
        return RecipeDescriptionViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecipeDescriptionViewHolder, position: Int) {
        val element = data[position]
        holder.numberText.text = (position+1).toString()
        holder.descriptionText.setText(element.description)
        holder.descriptionText.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(eTxt: Editable?) {
                //syncing edittext with description
                element.description = eTxt.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //nothing to do here
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //nothing to do here
            }
        })
    }

    //view holder
    class RecipeDescriptionViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val numberText : TextView = view.findViewById(R.id.descr_count)
        val descriptionText : EditText = view.findViewById(R.id.descr_editTxt)
    }

    //functions
    fun descriptionTexts() : List<String> = data.map { it.description }

    fun setData(newData : List<RecipeDescription>){
        data = newData
        notifyItemInserted(data.lastIndex)
    }
}