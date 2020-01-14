package de.madem.homium.ui.activities.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.databinding.FragmentPresentationStepBinding
import de.madem.homium.databinding.FragmentPresentationStepCardBinding
import de.madem.homium.utilities.inflater

class RecipePresentationStepFragment
    : Fragment() {

    lateinit var textToDisplay: String

    private lateinit var binding: FragmentPresentationStepBinding

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, sis: Bundle?): View? {
        binding = FragmentPresentationStepBinding.inflate(inflater, parent, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) = with(binding) {
        super.onActivityCreated(savedInstanceState)

        recyclerView.adapter = Adapter(textToDisplay)
    }

    private class Adapter(
        private val textToDisplay: String
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private class ViewHolder(
            val binding: FragmentPresentationStepCardBinding
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = FragmentPresentationStepCardBinding.inflate(
                parent.inflater(), parent, false
            )

            return ViewHolder(binding)
        }

        override fun getItemCount() = 1

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.binding){
            cardText.text = textToDisplay
        }

    }

}