package de.madem.homium.ui.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R

class ShoppingFragment : Fragment() {

    private lateinit var shoppingViewModel: ShoppingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        shoppingViewModel =
            ViewModelProviders.of(this).get(ShoppingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_shopping, container, false)
        val textView: TextView = root.findViewById(R.id.text_shopping)
        shoppingViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}