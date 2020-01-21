package de.madem.homium.managers.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.models.InventoryItem
import de.madem.homium.utilities.InventoryItemAmountClassifier

class InventoryItemListAdapter(val owner: LifecycleOwner, liveData: LiveData<List<InventoryItem>>)
    : RecyclerView.Adapter<InventoryItemListAdapter.InventoryItemViewHolder>() {

    companion object {
        private const val quantityUnitTemplate = "quantity unit"
    }

    var data = liveData.value ?: listOf()

    init {
        liveData.observe(owner, Observer { list ->
            data = list
            notifyDataSetChanged()
        })
    }

    //fields
    var shortClickListener : (item: InventoryItem, holder: InventoryItemViewHolder) -> Unit = { _, _ ->
        //nothing to do here :D
    }

    var longClickListener : (item: InventoryItem, holder: InventoryItemViewHolder ) -> Boolean = { _, _ ->
        //nothing to do here
        false
    }

    //functions
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_inventory_item,parent,false)
        return InventoryItemViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(viewHolder: InventoryItemViewHolder, position: Int) {
        val inventoryItem = data[position]

        with(viewHolder) {
            //set text and quantity

            txtTitle.text = inventoryItem.name
            txtQuantityUnit.text = quantityUnitTemplate
                .replace("quantity",inventoryItem.count.toString())
                .replace("unit",inventoryItem.unit)
            txtLocation.text = inventoryItem.location

            val classifier = InventoryItemAmountClassifier.byInventoryItem(inventoryItem)
            viewStock.setBackgroundColor(classifier.color)

            //set click handler
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    shortClickListener(inventoryItem, this)
                }
            }

            //set click handler
            itemView.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    longClickListener(inventoryItem, this)
                } else {
                    false
                }
            }
        }
    }


    //View Holder class
    class InventoryItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        //fields
        val txtTitle : TextView = itemView.findViewById(R.id.tv_name)
        val txtQuantityUnit : TextView = itemView.findViewById(R.id.tv_quantityunit)
        val txtLocation : TextView = itemView.findViewById(R.id.tv_location)
        val viewStock: View = itemView.findViewById(R.id.view_stock)

    }
}