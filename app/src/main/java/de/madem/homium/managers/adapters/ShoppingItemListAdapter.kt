package de.madem.homium.managers.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.databinding.ShoppingitemListitemBinding
import de.madem.homium.models.ShoppingItem


class ShoppingItemListAdapter(
    private val onItemClick: (item: ShoppingItem, vh: ShoppingItemViewHolder) -> Unit = {_, _ ->},
    private val onItemLongClick: (item: ShoppingItem, vh: ShoppingItemViewHolder) -> Boolean = { _,_ -> false }
) : ListAdapter<ShoppingItem, ShoppingItemListAdapter.ShoppingItemViewHolder>(itemCallback) {

    companion object {
        //Diff Callback
        private val itemCallback = object: DiffUtil.ItemCallback<ShoppingItem>() {
            override fun areItemsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean {
                return newItem == oldItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ShoppingitemListitemBinding.inflate(inflater, parent, false)
        return ShoppingItemViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ShoppingItemViewHolder, position: Int) {
        val shoppingItem = getItem(position)

        with(viewHolder) {
            //set text and quantity
            txtTitle.text = shoppingItem.name

            val quantityText = "${shoppingItem.count} ${shoppingItem.unit.getString()}"
            txtQuantityUnit.text = quantityText

            //set view checked
            applyCheckState(shoppingItem.checked)

            //set click handler
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(shoppingItem, viewHolder)
                }
            }

            //set click handler
            itemView.setOnLongClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemLongClick(shoppingItem, viewHolder)
                } else {
                    false
                }
            }
        }
    }

    //TODO Also Move this to ViewModel
    fun getItemAtPosition(position: Int): ShoppingItem = getItem(position)

    //View Holder class
    class ShoppingItemViewHolder(binding: ShoppingitemListitemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        //fields
        val txtTitle: TextView = binding.txtViewShoppingitemListTitle
        val txtQuantityUnit: TextView = binding.txtViewShoppingitemListQuantityUnit
        private val imgViewIcon: ImageView = binding.imgViewShoppingitemListIcon

        fun applyCheckState(isChecked: Boolean) {
            val resId = if (isChecked) R.drawable.ic_check_circle_green else R.drawable.ic_circle
            imgViewIcon.setImageResource(resId)
        }

    }
}