package com.example.mobileproject.presentation.ui.screen.memories

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.presentation.ui.components.place.PlaceAdapter
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.viewmodel.FavoriteUiState
import com.example.mobileproject.presentation.viewmodel.FavoriteViewModel
import com.example.mobileproject.presentation.viewmodel.HistoryUiState
import com.example.mobileproject.presentation.viewmodel.HistoryViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MemoriesFragment : Fragment(R.layout.fragment_memories) {

    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var placeAdapter: PlaceAdapter

    private var accessToken: String = ""
    private var currentTab: Int = TAB_FAVORITES
    private var latestFavoriteState: FavoriteUiState = FavoriteUiState()
    private var latestHistoryState: HistoryUiState = HistoryUiState()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteViewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]
        historyViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        accessToken = (activity as? HomeActivity)
            ?.intent
            ?.getStringExtra(HomeActivity.EXTRA_ACCESS_TOKEN)
            .orEmpty()

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val memoriesRecycler = view.findViewById<RecyclerView>(R.id.rvMemories)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val emptyView = view.findViewById<TextView>(R.id.tvEmpty)
        val clearHistoryButton = view.findViewById<MaterialButton>(R.id.btnClearHistory)

        placeAdapter = PlaceAdapter {
            (activity as? HomeActivity)?.switchTo(R.id.nav_explore)
        }

        memoriesRecycler.layoutManager = LinearLayoutManager(requireContext())
        memoriesRecycler.adapter = placeAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: TAB_FAVORITES
                renderCurrentTab(progressBar, emptyView, memoriesRecycler, clearHistoryButton)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        clearHistoryButton.setOnClickListener {
            historyViewModel.clearHistory(accessToken)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    favoriteViewModel.uiState.collect { state ->
                        latestFavoriteState = state
                        if (currentTab == TAB_FAVORITES) {
                            renderCurrentTab(progressBar, emptyView, memoriesRecycler, clearHistoryButton)
                        }
                    }
                }

                launch {
                    historyViewModel.uiState.collect { state ->
                        latestHistoryState = state
                        if (currentTab == TAB_HISTORY) {
                            renderCurrentTab(progressBar, emptyView, memoriesRecycler, clearHistoryButton)
                        }
                    }
                }
            }
        }

        favoriteViewModel.loadFavorites(accessToken)
        historyViewModel.loadHistory(accessToken)
    }

    private fun renderCurrentTab(
        progressBar: ProgressBar,
        emptyView: TextView,
        memoriesRecycler: RecyclerView,
        clearHistoryButton: MaterialButton,
    ) {
        val places: List<Place>
        val isLoading: Boolean

        if (currentTab == TAB_FAVORITES) {
            places = latestFavoriteState.favorites
            isLoading = latestFavoriteState.isLoading
            clearHistoryButton.visibility = View.GONE
        } else {
            places = latestHistoryState.history
            isLoading = latestHistoryState.isLoading
            clearHistoryButton.visibility = if (!isLoading && places.isNotEmpty()) View.VISIBLE else View.GONE
        }

        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        emptyView.visibility = if (!isLoading && places.isEmpty()) View.VISIBLE else View.GONE
        memoriesRecycler.visibility = if (places.isNotEmpty()) View.VISIBLE else View.GONE

        placeAdapter.submitList(places)
    }

    private companion object {
        const val TAB_FAVORITES = 0
        const val TAB_HISTORY = 1
    }
}
