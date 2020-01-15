package de.madem.homium.ui.activities.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.databinding.FragmentPresentationStepBinding
import de.madem.homium.databinding.FragmentPresentationStepCardBinding
import de.madem.homium.utilities.inflater

class RecipePresentationStepFragment
    : Fragment() {

    var textToDisplay: MutableLiveData<String> = MutableLiveData("")

    private lateinit var binding: FragmentPresentationStepBinding

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, sis: Bundle?): View? {
        binding = FragmentPresentationStepBinding.inflate(inflater, parent, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = Adapter(viewLifecycleOwner, textToDisplay)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("text", textToDisplay.value)

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.getString("text")
            .takeIf { it?.isNotEmpty() ?: false }
            .also { textToDisplay.value = it }
    }


    private class Adapter(
        owner: LifecycleOwner, textLiveData: LiveData<String>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private var textToDisplay: String = ""

        init {
            textLiveData.observe(owner, Observer {
                textToDisplay = it
                notifyDataSetChanged()
            })
        }

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