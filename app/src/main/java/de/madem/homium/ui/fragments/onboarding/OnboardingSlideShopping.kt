package de.madem.homium.ui.fragments.onboarding


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.madem.homium.R

/**
 * A simple [Fragment] subclass.
 */
class OnboardingSlideShopping : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_onboarding_slide_shopping, container, false)
    }


}
