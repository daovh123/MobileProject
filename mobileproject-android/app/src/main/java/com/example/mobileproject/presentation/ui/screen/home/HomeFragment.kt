package com.example.mobileproject.presentation.ui.screen.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileproject.R
import com.example.mobileproject.presentation.seed.SeedDataProvider
import com.example.mobileproject.presentation.ui.screen.couple.CoupleConnectActivity
import com.example.mobileproject.presentation.viewmodel.CoupleUiState
import com.example.mobileproject.presentation.viewmodel.CoupleViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var coupleViewModel: CoupleViewModel
    private var accessToken: String = ""

    companion object {
        private const val ARG_ACCESS_TOKEN: String = "arg_access_token"

        fun newInstance(accessToken: String): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACCESS_TOKEN, accessToken)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coupleViewModel = ViewModelProvider(this)[CoupleViewModel::class.java]
        accessToken = arguments?.getString(ARG_ACCESS_TOKEN).orEmpty()

        view.findViewById<MaterialTextView>(R.id.tvHomeSeed1).text = SeedDataProvider.homeHighlights[0]
        view.findViewById<MaterialTextView>(R.id.tvHomeSeed2).text = SeedDataProvider.homeHighlights[1]
        view.findViewById<MaterialTextView>(R.id.tvHomeSeed3).text = SeedDataProvider.homeHighlights[2]

        val pairTitle = view.findViewById<MaterialTextView>(R.id.tvPairTitle)
        val pairSubtitle = view.findViewById<MaterialTextView>(R.id.tvPairSubtitle)
        val pairCode = view.findViewById<MaterialTextView>(R.id.tvPairMyCode)
        val pairHint = view.findViewById<MaterialTextView>(R.id.tvPairHint)
        val connectedCard = view.findViewById<View>(R.id.cardPairConnected)
        val partnerName = view.findViewById<MaterialTextView>(R.id.tvPairPartnerName)

        val daysTogetherCard = view.findViewById<View>(R.id.cardDaysTogether)
        val daysTogetherCount = view.findViewById<MaterialTextView>(R.id.tvDaysTogetherCount)
        val anniversaryHintCard = view.findViewById<View>(R.id.cardAnniversaryHint)

        val pairButton = view.findViewById<MaterialButton>(R.id.btnPairNow)
        pairButton.setOnClickListener {
            startActivity(
                Intent(requireContext(), CoupleConnectActivity::class.java)
                    .putExtra(CoupleConnectActivity.EXTRA_ACCESS_TOKEN, accessToken)
            )
        }

        if (accessToken.isBlank()) {
            renderNoSessionState(
                pairTitle = pairTitle,
                pairSubtitle = pairSubtitle,
                pairCode = pairCode,
                pairHint = pairHint,
                connectedCard = connectedCard,
                daysTogetherCard = daysTogetherCard,
                pairButton = pairButton,
            )
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                coupleViewModel.uiState.collect { state ->
                    renderPairState(
                        state = state,
                        pairTitle = pairTitle,
                        pairSubtitle = pairSubtitle,
                        pairCode = pairCode,
                        pairHint = pairHint,
                        connectedCard = connectedCard,
                        partnerName = partnerName,
                        daysTogetherCard = daysTogetherCard,
                        daysTogetherCount = daysTogetherCount,
                        anniversaryHintCard = anniversaryHintCard,
                        pairButton = pairButton,
                    )
                }
            }
        }

        coupleViewModel.loadStatus(accessToken)
    }

    override fun onResume() {
        super.onResume()
        if (accessToken.isNotBlank()) {
            coupleViewModel.loadStatus(accessToken)
        }
    }

    private fun renderNoSessionState(
        pairTitle: MaterialTextView,
        pairSubtitle: MaterialTextView,
        pairCode: MaterialTextView,
        pairHint: MaterialTextView,
        connectedCard: View,
        daysTogetherCard: View,
        pairButton: MaterialButton,
    ) {
        pairTitle.text = getString(R.string.home_pair_title)
        pairSubtitle.text = getString(R.string.home_pair_subtitle)
        pairCode.visibility = View.GONE
        connectedCard.visibility = View.GONE
        daysTogetherCard.visibility = View.GONE
        pairHint.visibility = View.VISIBLE
        pairHint.text = getString(R.string.home_pair_session_expired)
        pairHint.setTextColor(ContextCompat.getColor(requireContext(), R.color.auth_pink))
        pairButton.visibility = View.VISIBLE
        pairButton.isEnabled = false
        pairButton.text = getString(R.string.home_pair_button)
    }

    private fun renderPairState(
        state: CoupleUiState,
        pairTitle: MaterialTextView,
        pairSubtitle: MaterialTextView,
        pairCode: MaterialTextView,
        pairHint: MaterialTextView,
        connectedCard: View,
        partnerName: MaterialTextView,
        daysTogetherCard: View,
        daysTogetherCount: MaterialTextView,
        anniversaryHintCard: View,
        pairButton: MaterialButton,
    ) {
        if (state.paired) {
            pairTitle.text = getString(R.string.home_pair_connected_title)
            pairSubtitle.text = getString(R.string.home_pair_connected_subtitle)
            pairCode.visibility = View.GONE
            pairHint.visibility = View.GONE
            pairButton.visibility = View.GONE
            connectedCard.visibility = View.VISIBLE

            daysTogetherCard.visibility = View.VISIBLE
            val computedDaysTogether = state.daysTogether?.coerceAtLeast(1)
                ?: computeDaysTogetherFromStartAt(state.startAt)
                ?: 1L
            daysTogetherCount.text = computedDaysTogether.toString()

            val showHappyAnniversary = state.anniversaryTomorrow == true
            anniversaryHintCard.visibility = if (showHappyAnniversary) View.VISIBLE else View.GONE

            val partnerDisplayName = state.partnerUsername ?: getString(R.string.home_pair_partner_unknown)
            partnerName.text = getString(R.string.home_pair_partner_name, partnerDisplayName)
            return
        }

        pairTitle.text = getString(R.string.home_pair_title)
        pairSubtitle.text = getString(R.string.home_pair_subtitle)
        connectedCard.visibility = View.GONE
        daysTogetherCard.visibility = View.GONE
        pairButton.visibility = View.VISIBLE
        pairButton.isEnabled = !state.isLoading
        pairButton.text = if (state.isLoading) {
            getString(R.string.home_pair_loading_button)
        } else {
            getString(R.string.home_pair_button)
        }

        val myCode = state.myCoupleCode
        if (myCode.isNullOrBlank()) {
            pairCode.visibility = View.GONE
        } else {
            pairCode.visibility = View.VISIBLE
            pairCode.text = getString(R.string.home_pair_my_code, myCode)
        }

        val hintText = when {
            !state.errorMessage.isNullOrBlank() -> state.errorMessage
            state.outgoingStatus.equals("PENDING", ignoreCase = true) -> getString(R.string.home_pair_outgoing_pending)
            state.outgoingStatus.equals("REJECTED", ignoreCase = true) -> getString(R.string.home_pair_outgoing_rejected)
            !state.myCoupleCodeExpiresAt.isNullOrBlank() -> getString(
                R.string.home_pair_code_expiry,
                state.myCoupleCodeExpiresAt ?: "",
            )
            else -> null
        }

        if (hintText.isNullOrBlank()) {
            pairHint.visibility = View.GONE
            return
        }

        val hintColor = if (
            !state.errorMessage.isNullOrBlank() ||
            state.outgoingStatus.equals("REJECTED", ignoreCase = true)
        ) {
            R.color.auth_pink
        } else {
            R.color.md3_on_surface_variant
        }

        pairHint.visibility = View.VISIBLE
        pairHint.text = hintText
        pairHint.setTextColor(ContextCompat.getColor(requireContext(), hintColor))
    }

    private fun computeDaysTogetherFromStartAt(startAt: String?): Long? {
        val datePart = startAt?.takeIf { it.length >= 10 }?.substring(0, 10) ?: return null
        val parts = datePart.split("-")
        if (parts.size != 3) {
            return null
        }

        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].toIntOrNull() ?: return null

        val utc = TimeZone.getTimeZone("UTC")

        val startCal = GregorianCalendar(utc).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayCal = GregorianCalendar(utc).apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = todayCal.timeInMillis - startCal.timeInMillis
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis) + 1
        return days.coerceAtLeast(1)
    }
}
