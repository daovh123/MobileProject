package com.example.mobileproject.presentation.ui.screen.explore

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.mobileproject.R
import com.example.mobileproject.presentation.seed.SeedDataProvider
import com.google.android.material.textview.MaterialTextView

class ExploreFragment : Fragment(R.layout.fragment_explore) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialTextView>(R.id.tvExploreSeed1).text = SeedDataProvider.exploreIdeas[0]
        view.findViewById<MaterialTextView>(R.id.tvExploreSeed2).text = SeedDataProvider.exploreIdeas[1]
        view.findViewById<MaterialTextView>(R.id.tvExploreSeed3).text = SeedDataProvider.exploreIdeas[2]
    }
}
