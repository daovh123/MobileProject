package com.example.mobileproject.presentation.ui.screen.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.mobileproject.R
import com.example.mobileproject.presentation.seed.SeedDataProvider
import com.google.android.material.textview.MaterialTextView

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialTextView>(R.id.tvHomeSeed1).text = SeedDataProvider.homeHighlights[0]
        view.findViewById<MaterialTextView>(R.id.tvHomeSeed2).text = SeedDataProvider.homeHighlights[1]
        view.findViewById<MaterialTextView>(R.id.tvHomeSeed3).text = SeedDataProvider.homeHighlights[2]
    }
}
