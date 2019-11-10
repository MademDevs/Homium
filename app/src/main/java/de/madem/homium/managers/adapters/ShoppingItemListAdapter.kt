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
    private var clickListener : OnItemClickListener = object : OnItemClickListener{
        override fun onItemClicked(position: Int) {
            //nothing to do here :D
        }
    }

    private var longClickListener : OnItemLongClickListener = object : OnItemLongClickListener{
        override fun onItemLongClicked(position: Int) : Boolean {
            //nothing to do here :D
            return false
        }
    }

    //Setter for clickListener
    fun setOnItemClickListener(clickListener : OnItemClickListener){
        this.clickListener = clickListener
    }

    fun setOnItemClickListener(function : (Int) -> Unit){
        this.clickListener = object : OnItemClickListener{
            override fun onItemClicked(position: Int) {
                function.invoke(position)
            }
        }
    }

    fun setOnItemLongClickListener(longClickListener: OnItemLongClickListener){
        this.longClickListener = longClickListener
    }

    fun setOnItemLongClickListener(function: (Int) -> Boolean){
        this.longClickListener = object : OnItemLongClickListener{
            override fun onItemLongClicked(position: Int): Boolean {
                return function.invoke(position)
            }
        }
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
    class ShoppingItemViewHolder(itemView : View, clickListener: OnItemClickListener, longClickListener: OnItemLongClickListener) : RecyclerView.ViewHolder(itemView){

        //fields
        val txtTitle : TextView = itemView.findViewById(R.id.txtView_shoppingitemList_title)
        val txtQuantityUnit : TextView = itemView.findViewById(R.id.txtView_shoppingitemList_quantity_unit)

        init{

            //setting clicklistener for view --> clicklistener for view triggers internal clicklistener
            itemView.setOnClickListener {
                val adapterPosition = adapterPosition

                if (adapterPosition != RecyclerView.NO_POSITION){
                    clickListener.onItemClicked(adapterPosition)
                }
            }

            //setting longclicklistener for view --> longclicklistener for view triggers internal longclicklistener
            itemView.setOnLongClickListener {
                val adapterPosition = adapterPosition

                if(adapterPosition != RecyclerView.NO_POSITION){
                    return@setOnLongClickListener longClickListener.onItemLongClicked(adapterPosition)
                }
                else{
                    return@setOnLongClickListener false
                }
            }
        }

    }

    //Adapters ClickListener Interfaces
    interface OnItemClickListener{
        fun onItemClicked(position: Int)
    }

    interface OnItemLongClickListener{
        fun onItemLongClicked(position : Int) : Boolean
    }
}