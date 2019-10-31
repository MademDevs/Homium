package de.madem.homium.ui.fragments.money

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R

class MoneyFragment : Fragment() {

    private lateinit var moneyViewModel: MoneyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        moneyViewModel =
            ViewModelProviders.of(this).get(MoneyViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_money, container, false)
        val textView: TextView = root.findViewById(R.id.text_money)
        moneyViewModel.text.observe(this, Observer {
            textView.text = it
        })
        //testcomment
        return root
    }
}