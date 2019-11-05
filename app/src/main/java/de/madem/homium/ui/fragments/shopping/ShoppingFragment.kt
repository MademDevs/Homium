package de.madem.homium.ui.fragments.shopping

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.madem.homium.R
import de.madem.homium.ui.activities.shoppingitem.ShoppingItemEditActivity
import de.madem.homium.ui.activities.test.TestActivity
import de.madem.homium.utilities.switchToActivity
import kotlinx.android.synthetic.main.fragment_shopping.*

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

        //floating action button
        val btnAddShoppingItem = root.findViewById<FloatingActionButton>(R.id.floatingActionButton_addShoppingItem)

        btnAddShoppingItem.setOnClickListener {
            //implementing simple navigation to shopping item edit screen via intent
            startActivity(Intent(activity,ShoppingItemEditActivity::class.java))
        }



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_test.setOnClickListener {
            switchToActivity(TestActivity::class)
        }
    }

}