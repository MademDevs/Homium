package com.andreklein.myapplication.ui.last

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.andreklein.myapplication.R

class LastFragment : Fragment() {

    private lateinit var lastViewModel: LastViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lastViewModel =
            ViewModelProviders.of(this).get(LastViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_last, container, false)
        val textView: TextView = root.findViewById(R.id.text_last)
        lastViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}