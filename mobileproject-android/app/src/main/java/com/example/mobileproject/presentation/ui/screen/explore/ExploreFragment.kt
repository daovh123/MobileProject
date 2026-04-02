package com.example.mobileproject.presentation.ui.screen.explore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.presentation.ui.components.place.PlaceAdapter
import com.example.mobileproject.presentation.viewmodel.ExplorePlaceType
import com.example.mobileproject.presentation.viewmodel.ExploreUiState
import com.example.mobileproject.presentation.viewmodel.ExploreViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private lateinit var exploreViewModel: ExploreViewModel
    private val placeAdapter = PlaceAdapter { place ->
        showPlaceDetail(place)
    }
    private lateinit var areaAdapter: ArrayAdapter<String>
    private lateinit var ratingAdapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exploreViewModel = ViewModelProvider(this)[ExploreViewModel::class.java]

        val searchInput = view.findViewById<TextInputEditText>(R.id.etExploreQuery)
        val searchButton = view.findViewById<MaterialButton>(R.id.btnExploreSearch)
        val areaInput = view.findViewById<AutoCompleteTextView>(R.id.actExploreArea)
        val randomButton = view.findViewById<MaterialButton>(R.id.btnExploreRandom)
        val typeGroup = view.findViewById<ChipGroup>(R.id.cgExploreType)
        val typeAllChip = view.findViewById<Chip>(R.id.chipTypeAll)
        val typeFoodChip = view.findViewById<Chip>(R.id.chipTypeFood)
        val typeDrinkChip = view.findViewById<Chip>(R.id.chipTypeDrink)
        val nearMeChip = view.findViewById<Chip>(R.id.chipNearMe)
        val openNowChip = view.findViewById<Chip>(R.id.chipOpenNow)
        val ratingInput = view.findViewById<AutoCompleteTextView>(R.id.actExploreMinRating)
        val radiusInput = view.findViewById<TextInputEditText>(R.id.etExploreRadius)
        val randomText = view.findViewById<MaterialTextView>(R.id.tvExploreRandomResult)
        val progressBar = view.findViewById<LinearProgressIndicator>(R.id.progressExplore)
        val messageText = view.findViewById<MaterialTextView>(R.id.tvExploreMessage)
        val retryButton = view.findViewById<MaterialButton>(R.id.btnExploreRetry)
        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvExplorePlaces)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = placeAdapter

        areaAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            mutableListOf(),
        )
        areaInput.setAdapter(areaAdapter)

        ratingAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            buildRatingOptions(),
        )
        ratingInput.setAdapter(ratingAdapter)
        ratingInput.setText(getString(R.string.explore_rating_all_option), false)

        searchButton.setOnClickListener {
            submitSearch(
                query = searchInput.text?.toString().orEmpty(),
                province = areaInput.text?.toString().orEmpty(),
            )
        }

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                submitSearch(
                    query = searchInput.text?.toString().orEmpty(),
                    province = areaInput.text?.toString().orEmpty(),
                )
                true
            } else {
                false
            }
        }

        areaInput.doAfterTextChanged {
            exploreViewModel.onProvinceChanged(it?.toString().orEmpty())
        }

        randomButton.setOnClickListener {
            exploreViewModel.onQueryChanged(searchInput.text?.toString().orEmpty())
            exploreViewModel.onProvinceChanged(areaInput.text?.toString().orEmpty())
            exploreViewModel.randomPlace()
        }

        typeGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedType = when (checkedIds.firstOrNull()) {
                R.id.chipTypeFood -> ExplorePlaceType.FOOD
                R.id.chipTypeDrink -> ExplorePlaceType.DRINK
                else -> ExplorePlaceType.ALL
            }
            exploreViewModel.onTypeChanged(selectedType)
        }

        nearMeChip.setOnCheckedChangeListener { _, isChecked ->
            exploreViewModel.onNearMeChanged(isChecked)
        }

        openNowChip.setOnCheckedChangeListener { _, isChecked ->
            exploreViewModel.onOpenNowChanged(isChecked)
        }

        ratingInput.setOnItemClickListener { _, _, position, _ ->
            exploreViewModel.onMinRatingChanged(minRatingFromPosition(position))
        }

        radiusInput.doAfterTextChanged {
            exploreViewModel.onRadiusChanged(it?.toString().orEmpty())
        }

        retryButton.setOnClickListener {
            exploreViewModel.retry()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                exploreViewModel.uiState.collect { state ->
                    renderState(
                        state = state,
                        areaInput = areaInput,
                        ratingInput = ratingInput,
                        radiusInput = radiusInput,
                        randomText = randomText,
                        typeAllChip = typeAllChip,
                        typeFoodChip = typeFoodChip,
                        typeDrinkChip = typeDrinkChip,
                        nearMeChip = nearMeChip,
                        openNowChip = openNowChip,
                        progressBar = progressBar,
                        messageText = messageText,
                        retryButton = retryButton,
                    )
                }
            }
        }
    }

    private fun submitSearch(query: String, province: String) {
        exploreViewModel.onQueryChanged(query)
        exploreViewModel.onProvinceChanged(province)
        exploreViewModel.search()
    }

    private fun renderState(
        state: ExploreUiState,
        areaInput: AutoCompleteTextView,
        ratingInput: AutoCompleteTextView,
        radiusInput: TextInputEditText,
        randomText: MaterialTextView,
        typeAllChip: Chip,
        typeFoodChip: Chip,
        typeDrinkChip: Chip,
        nearMeChip: Chip,
        openNowChip: Chip,
        progressBar: LinearProgressIndicator,
        messageText: MaterialTextView,
        retryButton: MaterialButton,
    ) {
        progressBar.isVisible = state.isLoading || state.isRandomLoading
        placeAdapter.submitList(state.places)

        syncAreaSuggestions(state.availableProvinces)

        if (!areaInput.hasFocus() && areaInput.text?.toString().orEmpty() != state.selectedProvince) {
            areaInput.setText(state.selectedProvince, false)
        }

        val isAll = state.selectedType == ExplorePlaceType.ALL
        val isFood = state.selectedType == ExplorePlaceType.FOOD
        val isDrink = state.selectedType == ExplorePlaceType.DRINK
        if (typeAllChip.isChecked != isAll) {
            typeAllChip.isChecked = isAll
        }
        if (typeFoodChip.isChecked != isFood) {
            typeFoodChip.isChecked = isFood
        }
        if (typeDrinkChip.isChecked != isDrink) {
            typeDrinkChip.isChecked = isDrink
        }
        if (nearMeChip.isChecked != state.nearMeOnly) {
            nearMeChip.isChecked = state.nearMeOnly
        }
        if (openNowChip.isChecked != state.openNowOnly) {
            openNowChip.isChecked = state.openNowOnly
        }
        val minRatingLabel = ratingLabel(state.selectedMinRating)
        if (!ratingInput.hasFocus() && ratingInput.text?.toString().orEmpty() != minRatingLabel) {
            ratingInput.setText(minRatingLabel, false)
        }
        if (!radiusInput.hasFocus() && radiusInput.text?.toString().orEmpty() != state.radiusKmInput) {
            radiusInput.setText(state.radiusKmInput)
        }

        val randomSuggestion = state.randomSuggestion
        if (randomSuggestion == null) {
            randomText.isVisible = false
        } else {
            val areaLabel = randomSuggestion.province
                ?.takeIf { it.isNotBlank() }
                ?: randomSuggestion.district
                    ?.takeIf { it.isNotBlank() }
                ?: getString(R.string.explore_updating)

            randomText.isVisible = true
            randomText.text = getString(
                R.string.explore_random_result_format,
                valueOrUpdating(randomSuggestion.name),
                areaLabel,
            )
        }

        if (state.isLoading || state.isRandomLoading) {
            messageText.isVisible = false
            retryButton.isVisible = false
            return
        }

        val errorMessage = state.errorMessage
        if (!errorMessage.isNullOrBlank()) {
            messageText.isVisible = true
            messageText.text = getString(R.string.explore_error_message, errorMessage)
            retryButton.isVisible = true
            return
        }

        if (state.places.isEmpty()) {
            messageText.isVisible = true
            messageText.text = getString(R.string.explore_empty_message)
            retryButton.isVisible = false
            return
        }

        messageText.isVisible = false
        retryButton.isVisible = false
    }

    private fun syncAreaSuggestions(areas: List<String>) {
        val current = (0 until areaAdapter.count).mapNotNull { areaAdapter.getItem(it) }
        if (current == areas) {
            return
        }
        areaAdapter.clear()
        areaAdapter.addAll(areas)
        areaAdapter.notifyDataSetChanged()
    }

    private fun buildRatingOptions(): List<String> {
        return listOf(
            getString(R.string.explore_rating_all_option),
            getString(R.string.explore_rating_1_option),
            getString(R.string.explore_rating_2_option),
            getString(R.string.explore_rating_3_option),
            getString(R.string.explore_rating_4_option),
            getString(R.string.explore_rating_5_option),
        )
    }

    private fun minRatingFromPosition(position: Int): Int? {
        return when (position) {
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 4
            5 -> 5
            else -> null
        }
    }

    private fun ratingLabel(minRating: Int?): String {
        return when (minRating) {
            1 -> getString(R.string.explore_rating_1_option)
            2 -> getString(R.string.explore_rating_2_option)
            3 -> getString(R.string.explore_rating_3_option)
            4 -> getString(R.string.explore_rating_4_option)
            5 -> getString(R.string.explore_rating_5_option)
            else -> getString(R.string.explore_rating_all_option)
        }
    }

    private fun showPlaceDetail(place: Place) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_place_detail, null)
        dialog.setContentView(dialogView)

        val closeButton = dialogView.findViewById<ImageButton>(R.id.btnPlaceDetailClose)
        val titleText = dialogView.findViewById<MaterialTextView>(R.id.tvPlaceDetailTitle)
        val metaText = dialogView.findViewById<MaterialTextView>(R.id.tvPlaceDetailMeta)
        val openingText = dialogView.findViewById<MaterialTextView>(R.id.tvPlaceDetailOpening)
        val priceText = dialogView.findViewById<MaterialTextView>(R.id.tvPlaceDetailPrice)
        val directionButton = dialogView.findViewById<MaterialButton>(R.id.btnPlaceDirection)
        val summaryText = dialogView.findViewById<MaterialTextView>(R.id.tvPlaceDetailSummary)
        val ratingText = dialogView.findViewById<MaterialTextView>(R.id.tvPlaceDetailRating)
        val addressText = dialogView.findViewById<MaterialTextView>(R.id.tvPlaceDetailAddress)
        val coordinatesText = dialogView.findViewById<MaterialTextView>(R.id.tvPlaceDetailCoordinates)

        val location = normalizedValue(place.province)
            ?: normalizedValue(place.district)
            ?: getString(R.string.explore_updating)

        titleText.text = valueOrUpdating(place.name)
        metaText.text = getString(
            R.string.explore_detail_meta_format,
            valueOrUpdating(place.effectiveTag),
            location,
        )
        openingText.text = getString(
            R.string.explore_detail_opening_badge_format,
            valueOrUpdating(place.openingHours),
        )
        priceText.text = getString(
            R.string.explore_detail_price_badge_format,
            valueOrUpdating(place.priceRange),
        )
        summaryText.text = buildOverviewText(place)
        ratingText.text = getString(
            R.string.explore_detail_rating_badge_format,
            ratingOrUpdating(place.rating, place.reviewCount),
        )
        addressText.text = buildAddressText(place)
        coordinatesText.text = detailLine(
            R.string.explore_detail_coordinates,
            coordinatesOrUpdating(place.lat, place.lng),
        )

        val mapsUrl = resolveGoogleMapsUrl(place)
        directionButton.isEnabled = mapsUrl != null
        directionButton.alpha = if (mapsUrl == null) 0.6f else 1f

        directionButton.setOnClickListener {
            val url = mapsUrl
            if (url == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.explore_detail_no_map_data),
                    Toast.LENGTH_SHORT,
                ).show()
                return@setOnClickListener
            }

            openGoogleMaps(url)
        }

        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
    }

    private fun detailLine(labelRes: Int, value: String): String {
        return getString(R.string.explore_detail_line_format, getString(labelRes), value)
    }

    private fun buildOverviewText(place: Place): String {
        val lines = mutableListOf<String>()

        normalizedValue(place.effectiveTag)?.let {
            lines.add(detailLine(R.string.explore_detail_effective_tag, it))
        }
        normalizedValue(place.category)?.let {
            lines.add(detailLine(R.string.explore_detail_category, it))
        }
        normalizedValue(place.mealType)?.let {
            lines.add(detailLine(R.string.explore_detail_meal_type, it))
        }
        normalizedValue(place.imageUrl)?.let {
            lines.add(detailLine(R.string.explore_detail_image_url, getString(R.string.explore_detail_available)))
        }

        return if (lines.isEmpty()) {
            getString(R.string.explore_detail_summary_empty)
        } else {
            lines.joinToString("\n")
        }
    }

    private fun buildAddressText(place: Place): String {
        val parts = listOf(
            normalizedValue(place.address),
            normalizedValue(place.district),
            normalizedValue(place.province),
        ).filterNotNull()

        return if (parts.isEmpty()) {
            getString(R.string.explore_updating)
        } else {
            parts.joinToString(", ")
        }
    }

    private fun resolveGoogleMapsUrl(place: Place): String? {
        val directUrl = normalizedValue(place.googleMapsUrl)
        if (directUrl != null) {
            return directUrl
        }

        if (place.lat != null && place.lng != null) {
            return "https://www.google.com/maps/search/?api=1&query=${place.lat},${place.lng}"
        }

        val query = listOf(
            normalizedValue(place.name),
            normalizedValue(place.address),
            normalizedValue(place.district),
            normalizedValue(place.province),
        ).filterNotNull().joinToString(" ")

        if (query.isBlank()) {
            return null
        }

        return "https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"
    }

    private fun openGoogleMaps(url: String) {
        val uri = Uri.parse(url)
        val packageManager = requireContext().packageManager

        val mapsAppIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)

        when {
            mapsAppIntent.resolveActivity(packageManager) != null -> startActivity(mapsAppIntent)
            fallbackIntent.resolveActivity(packageManager) != null -> startActivity(fallbackIntent)
            else -> Toast.makeText(
                requireContext(),
                getString(R.string.explore_detail_no_map_app),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun normalizedValue(value: String?): String? {
        return value?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun valueOrUpdating(value: String?): String {
        return normalizedValue(value) ?: getString(R.string.explore_updating)
    }

    private fun ratingOrUpdating(rating: Double?, reviewCount: Int?): String {
        if (rating == null || reviewCount == null) {
            return getString(R.string.explore_updating)
        }
        return getString(R.string.explore_rating_format, rating, reviewCount)
    }

    private fun coordinatesOrUpdating(lat: Double?, lng: Double?): String {
        if (lat == null || lng == null) {
            return getString(R.string.explore_updating)
        }
        return getString(R.string.explore_coordinates_format, lat, lng)
    }
}
