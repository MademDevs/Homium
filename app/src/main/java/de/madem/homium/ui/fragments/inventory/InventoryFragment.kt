package de.madem.homium.ui.fragments.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.managers.adapters.InventoryItemListAdapter

class InventoryFragment : Fragment() {

    //private lateinit var binding: ResultPro
    private lateinit var inventoryViewModel: InventoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inventoryViewModel = ViewModelProviders.of(this).get(InventoryViewModel::class.java)

        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_inventory)
        recyclerView.layoutManager = LinearLayoutManager(context)

        inventoryViewModel.setDummyData()

        recyclerView.adapter = InventoryItemListAdapter(this, inventoryViewModel.inventoryItems)
    }

}