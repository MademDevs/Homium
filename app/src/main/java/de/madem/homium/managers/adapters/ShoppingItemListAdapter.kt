package de.madem.homium.managers.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.models.ShoppingItem

class ShoppingItemListAdapter(owner: LifecycleOwner, liveData: MutableLiveData<List<ShoppingItem>>)
    : RecyclerView.Adapter<ShoppingItemListAdapter.ShoppingItemViewHolder>(), Filterable {

    companion object {
        private const val quantityUnitTemplate = "quantity unit"
    }



    //data
    var data = liveData.value?.toMutableList() ?: mutableListOf()
    private var dataBackup = MutableList(data.size){ data[it] }

    //filter
    private val filter = object : Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val resultList = mutableListOf<ShoppingItem>()

            if(constraint == null || constraint.isEmpty() || constraint.isBlank()){
                resultList.addAll(dataBackup)
            }
            else{
                val filterArgs = constraint.toString().toLowerCase().trim().split(" ")
                dataBackup.forEach { item ->
                    val name = item.name.trim().toLowerCase()
                    val unit = item.unit.getString().trim().toLowerCase()
                    val cnt = item.count.toString()
                    var matches = true
                    for(arg in filterArgs) {
                        if(!(name.contains(arg)
                            || unit.contains(arg)
                            || arg == cnt)){
                            matches = false
                            break
                        }
                    }
                    if(matches){
                        resultList.add(item)
                    }

                }
            }

            return FilterResults().apply {
                values = resultList
            }
        }

        override fun publishResults(constraint: CharSequence?, result: FilterResults?) {
            data.clear()
            val displayedItems : List<ShoppingItem> = result?.values as? List<ShoppingItem> ?: listOf()
            data.addAll(displayedItems)
            notifyDataSetChanged()
        }
    }
    var isReadyForFiltering = false
        private set(value) {
            field = value
        }



    init {
        liveData.observe(owner, Observer { list ->
            isReadyForFiltering = false
            data = list.toMutableList()
            dataBackup = MutableList(data.size){ data[it] }
            notifyDataSetChanged()
        })
    }

    //fields
    var shortClickListener : (item: ShoppingItem, holder: ShoppingItemViewHolder) -> Unit = { _, _ ->
        //nothing to do here :D
    }

    var longClickListener : (item: ShoppingItem, holder: ShoppingItemViewHolder ) -> Boolean = { _, _ ->
        //nothing to do here
        false
    }

    //functions
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shoppingitem_listitem,parent,false)
        return ShoppingItemViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(viewHolder: ShoppingItemViewHolder, position: Int) {
        val shoppingItem = data[position]

        with(viewHolder) {
            //set text and quantity
            txtTitle.text = shoppingItem.name
            txtQuantityUnit.text = quantityUnitTemplate
                .replace("quantity",shoppingItem.count.toString())
                .replace("unit",shoppingItem.unit.getString())

            //set view checked
            applyCheck(shoppingItem.checked)

            //set click handler
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    shortClickListener(shoppingItem, this)
                }
            }

            //set click handler
            itemView.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    longClickListener(shoppingItem, this)
                } else {
                    false
                }
            }
        }
    }


    //View Holder class
    class ShoppingItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        //fields
        val txtTitle : TextView = itemView.findViewById(R.id.txtView_shoppingitemList_title)
        val txtQuantityUnit : TextView = itemView.findViewById(R.id.txtView_shoppingitemList_quantity_unit)
        private val imgViewIcon : ImageView = itemView.findViewById(R.id.imgView_shoppingitemList_icon)

        fun applyCheck(isChecked: Boolean) {
            val resId = if(isChecked) R.drawable.ic_check_circle_green else R.drawable.ic_circle
            imgViewIcon.setImageResource(resId)
        }

    }

    override fun onViewAttachedToWindow(holder: ShoppingItemViewHolder) {
        super.onViewAttachedToWindow(holder)
        isReadyForFiltering = true
    }

    //interface functions
    override fun getFilter(): Filter {
        return filter
    }
}