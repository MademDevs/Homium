package de.madem.homium.managers.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.models.RecipeIngredient
import java.util.*

class IngredientsAdapter(private val cntxt : Context, private var data : MutableList<RecipeIngredient>)
    : RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {

    var deleteButtonClickListener : (Int) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(cntxt).inflate(R.layout.recipe_edit_ingredient,parent,
            false)
        return IngredientViewHolder(view = view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val element = data[position]
        holder.name.text = element.name
        holder.amount.text = element.count.toString()
        holder.unit.text = element.unit
        holder.deleteBtn.setOnClickListener {
            deleteButtonClickListener.invoke(holder.adapterPosition)
            notifyItemRemoved(position)
        }

    }

    fun setData(data : MutableList<RecipeIngredient>){
        this.data = data
        notifyDataSetChanged()
    }

    class IngredientViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val amount: TextView = view.findViewById(R.id.ingredient_amount_txt)
        val unit: TextView = view.findViewById(R.id.ingredient_unit_txt)
        val name: TextView = view.findViewById(R.id.ingredient_name_txt)
        val deleteBtn: ImageButton = view.findViewById(R.id.ingredient_btn_delete)
    }
}