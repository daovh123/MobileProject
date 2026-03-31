package com.example.mobileproject.presentation.ui.screen.memories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.mobileproject.R
import com.example.mobileproject.presentation.seed.SeedDataProvider
import com.google.android.material.textview.MaterialTextView

class MemoriesFragment : Fragment(R.layout.fragment_memories) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialTextView>(R.id.tvMemoriesGreeting).text =
            getString(R.string.memories_greeting, SeedDataProvider.memoryGreetingName)
        view.findViewById<MaterialTextView>(R.id.tvMemoriesSeed2).text = SeedDataProvider.memoryMoments[0]
        view.findViewById<MaterialTextView>(R.id.tvMemoriesMonth).text = SeedDataProvider.memoryMonth
        view.findViewById<MaterialTextView>(R.id.tvMemoriesEventTitle).text = SeedDataProvider.memoryEventTitle
        view.findViewById<MaterialTextView>(R.id.tvMemoriesEventSubtitle).text = SeedDataProvider.memoryEventSubtitle
        view.findViewById<MaterialTextView>(R.id.tvPartnerName).text = SeedDataProvider.partnerName
        view.findViewById<MaterialTextView>(R.id.tvPartnerMeta).text = SeedDataProvider.partnerMeta
    }
}
