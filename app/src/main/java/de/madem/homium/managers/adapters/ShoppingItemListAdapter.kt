package de.madem.homium.managers.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.models.ShoppingItem

private const val quantityUnitTemplate = "quantity unit"

class ShoppingItemListAdapter(private val data : MutableList<ShoppingItem>) : RecyclerView.Adapter<ShoppingItemListAdapter.ShoppingItemViewHolder>() {

    //fields
    private var clickListener : (position:Int, view: View) -> Unit = { pos, item ->
        //nothing to do here :D
    }

    private var longClickListener : (position:Int, view: View) -> Boolean = { pos, item ->
        //nothing to do here
        false
    }

    //Setter for onClickListener
    fun setOnItemClickListener(function : (Int, View) -> Unit){
        this.clickListener = function
    }

    fun setOnItemLongClickListener(function: (Int, View) -> Boolean){
        this.longClickListener = function
    }

    //functions
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shoppingitem_listitem,parent,false)
        return ShoppingItemViewHolder(view,clickListener,longClickListener)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ShoppingItemViewHolder, position: Int) {
        val shoppingItem = data[position]

        holder.txtTitle.text = shoppingItem.name
        holder.txtQuantityUnit.text = quantityUnitTemplate.replace("quantity",shoppingItem.count.toString()).replace("unit",shoppingItem.unit)
    }


    //View Holder class
    class ShoppingItemViewHolder(itemView : View, clickListener: (Int, View) -> Unit, longClickListener: (Int, View) -> Boolean) : RecyclerView.ViewHolder(itemView){

        //fields
        val txtTitle : TextView = itemView.findViewById(R.id.txtView_shoppingitemList_title)
        val txtQuantityUnit : TextView = itemView.findViewById(R.id.txtView_shoppingitemList_quantity_unit)

        init{

            //setting clicklistener for view --> clicklistener for view triggers internal clicklistener
            itemView.setOnClickListener {
                val adapterPosition = adapterPosition

                if (adapterPosition != RecyclerView.NO_POSITION){
                    clickListener.invoke(adapterPosition, it)
                }
            }

            //setting longclicklistener for view --> longclicklistener for view triggers internal longclicklistener
            itemView.setOnLongClickListener {
                val adapterPosition = adapterPosition

                if(adapterPosition != RecyclerView.NO_POSITION){
                    return@setOnLongClickListener longClickListener.invoke(adapterPosition, it)
                }
                else{
                    return@setOnLongClickListener false
                }
            }
        }

    }
}