package com.example.mobileproject.presentation.ui.screen.wallet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.mobileproject.R
import com.example.mobileproject.presentation.seed.SeedDataProvider
import com.google.android.material.textview.MaterialTextView

class WalletFragment : Fragment(R.layout.fragment_wallet) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialTextView>(R.id.tvWalletSeed1).text = SeedDataProvider.walletActivities[0]
        view.findViewById<MaterialTextView>(R.id.tvWalletSeed2).text = SeedDataProvider.walletActivities[1]
        view.findViewById<MaterialTextView>(R.id.tvWalletSeed3).text = SeedDataProvider.walletActivities[2]
    }
}
