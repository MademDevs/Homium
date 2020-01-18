package de.madem.homium.ui.activities.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.databinding.FragmentPresentationStepBinding
import de.madem.homium.databinding.FragmentPresentationStepCardBinding
import de.madem.homium.utilities.extensions.applyAndObserver
import de.madem.homium.utilities.extensions.inflater

private typealias StepBinding = FragmentPresentationStepBinding
private typealias StepCardBinding = FragmentPresentationStepCardBinding
private typealias StepViewModel = RecipePresentationStepViewModel

private typealias RVVH = RecyclerView.ViewHolder
private typealias RVAdapter<T> = RecyclerView.Adapter<T>

class RecipePresentationStepFragment : Fragment() {

    companion object {
        fun with(text: String) = RecipePresentationStepFragment()
            .apply {
                arguments = bundleOf(
                    Pair("text", text)
                )
            }
    }

    private lateinit var viewModel: StepViewModel
    private lateinit var binding: StepBinding

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, sis: Bundle?): View? {
        //init data binding
        binding = StepBinding.inflate(inflater, parent, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //init view model
        viewModel = ViewModelProviders.of(this).get(StepViewModel::class.java).apply {

            //get text from bundle and add it to view model
            textToDisplay.value = arguments?.getString("text") ?: ""
        }
    }

    override fun onViewCreated(view: View, sis: Bundle?) {
        super.onViewCreated(view, sis)

        binding.recyclerView.adapter = Adapter(viewModel.textToDisplay)
    }

    private class Adapter(var textToDisplay: LiveData<String>) : RVAdapter<Adapter.ViewHolder>() {

        private class ViewHolder(val binding: StepCardBinding) : RVVH(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = StepCardBinding.inflate(
                parent.inflater(), parent, false
            )

            return ViewHolder(binding)
        }

        override fun getItemCount() = 1

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.binding) {
            textToDisplay.applyAndObserver(lifecycleOwner, textToDisplay.value) {
                cardText.text = it
            }
        }
    }

}