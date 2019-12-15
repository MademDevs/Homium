package de.madem.homium.ui.fragments.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_SHOPPING
import de.madem.homium.managers.adapters.InventoryItemListAdapter
import de.madem.homium.ui.activities.inventoryedit.InventoryItemEditActivity
import de.madem.homium.ui.activities.shoppingitem.ShoppingItemEditActivity
import de.madem.homium.utilities.switchToActivity
import de.madem.homium.utilities.switchToActivityForResult

class InventoryFragment : Fragment() {

    //private lateinit var binding: ResultPro
    private lateinit var inventoryViewModel: InventoryViewModel
    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inventoryViewModel = ViewModelProviders.of(this).get(InventoryViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_inventory, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        registerRecyclerView()
        registerFloatingActionButton()
    }

    override fun onResume() {
        super.onResume()

        inventoryViewModel.reloadInventoryItems()
    }

    private fun registerRecyclerView() {
        val recyclerView = root.findViewById<RecyclerView>(R.id.rv_inventory)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val inventoryAdapter = InventoryItemListAdapter(this, inventoryViewModel.inventoryItems)

        inventoryAdapter.shortClickListener = { item, holder ->
            switchToActivity(InventoryItemEditActivity::class)
        }

        recyclerView.adapter = inventoryAdapter
    }


    private fun registerFloatingActionButton() {
        //floating action button
        val btnAddShoppingItem = root.findViewById<FloatingActionButton>(R.id.fab_add_inventory)

        btnAddShoppingItem.setOnClickListener {
            //implementing simple navigation to inventory item edit screen via intent
            switchToActivityForResult(0, InventoryItemEditActivity::class)
        }
    }


}