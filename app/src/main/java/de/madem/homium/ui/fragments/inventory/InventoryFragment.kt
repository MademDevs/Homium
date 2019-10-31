package de.madem.homium.ui.fragments.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R

class InventoryFragment : Fragment() {

    private lateinit var inventoryViewModel: InventoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inventoryViewModel =
            ViewModelProviders.of(this).get(InventoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_inventory, container, false)
        val textView: TextView = root.findViewById(R.id.text_inventory)
        inventoryViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}