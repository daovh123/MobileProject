package com.example.mobileproject.presentation.ui.screen.explore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private data class AreaOption(
        val labelResId: Int,
        val query: String,
    )

    private lateinit var exploreViewModel: ExploreViewModel
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }

    private var currentLocationLat: Double? = null
    private var currentLocationLng: Double? = null
    private var lastHandledRandomSuggestionToken: Long = -1

    private val predefinedAreaOptions = listOf(
        AreaOption(R.string.explore_area_ha_noi, "ha noi"),
        AreaOption(R.string.explore_area_ho_chi_minh, "ho chi minh"),
        AreaOption(R.string.explore_area_da_nang, "da nang"),
        AreaOption(R.string.explore_area_can_tho, "can tho"),
        AreaOption(R.string.explore_area_hai_phong, "hai phong"),
        AreaOption(R.string.explore_area_nha_trang, "nha trang"),
        AreaOption(R.string.explore_area_hue, "hue"),
        AreaOption(R.string.explore_area_vung_tau, "vung tau"),
        AreaOption(R.string.explore_area_da_lat, "da lat"),
        AreaOption(R.string.explore_area_quy_nhon, "quy nhon"),
        AreaOption(R.string.explore_area_bien_hoa, "bien hoa"),
        AreaOption(R.string.explore_area_buon_ma_thuot, "buon ma thuot"),
        AreaOption(R.string.explore_area_phan_thiet, "phan thiet"),
        AreaOption(R.string.explore_area_long_xuyen, "long xuyen"),
        AreaOption(R.string.explore_area_thai_nguyen, "thai nguyen"),
        AreaOption(R.string.explore_area_nam_dinh, "nam dinh"),
    )

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            fetchCurrentLocation()
        } else {
            exploreViewModel.onCurrentLocationUnavailable(getString(R.string.explore_location_permission_denied))
            view?.findViewById<Chip>(R.id.chipNearMe)?.isChecked = false
            Toast.makeText(
                requireContext(),
                getString(R.string.explore_location_permission_denied),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

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
        val filterToggleButton = view.findViewById<MaterialButton>(R.id.btnExploreFilterToggle)
        val advancedFiltersLayout = view.findViewById<View>(R.id.llExploreAdvancedFilters)
        val typeGroup = view.findViewById<ChipGroup>(R.id.cgExploreType)
        val typeAllChip = view.findViewById<Chip>(R.id.chipTypeAll)
        val typeFoodChip = view.findViewById<Chip>(R.id.chipTypeFood)
        val typeDrinkChip = view.findViewById<Chip>(R.id.chipTypeDrink)
        val nearMeChip = view.findViewById<Chip>(R.id.chipNearMe)
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
        syncAreaSuggestions(emptyList())
        areaInput.keyListener = null
        areaInput.dropDownAnchor = areaInput.id
        areaInput.dropDownHorizontalOffset = 0
        areaInput.setOnClickListener {
            refreshDropdownBounds(areaInput)
            areaInput.showDropDown()
        }
        areaInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                refreshDropdownBounds(areaInput)
                areaInput.showDropDown()
            }
        }
        areaInput.setOnItemClickListener { _, _, position, _ ->
            val selected = areaAdapter.getItem(position).orEmpty()
            exploreViewModel.onProvinceChanged(normalizeProvinceSelection(selected))
        }

        filterToggleButton.setOnClickListener {
            setAdvancedFiltersExpanded(
                expanded = !advancedFiltersLayout.isVisible,
                advancedFiltersLayout = advancedFiltersLayout,
                filterToggleButton = filterToggleButton,
            )
        }
        setAdvancedFiltersExpanded(
            expanded = false,
            advancedFiltersLayout = advancedFiltersLayout,
            filterToggleButton = filterToggleButton,
        )

        ratingAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            buildRatingOptions(),
        )
        ratingInput.setAdapter(ratingAdapter)
        ratingInput.keyListener = null
        ratingInput.dropDownAnchor = ratingInput.id
        ratingInput.dropDownHorizontalOffset = 0
        ratingInput.setOnClickListener {
            refreshDropdownBounds(ratingInput)
            ratingInput.showDropDown()
        }
        ratingInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                refreshDropdownBounds(ratingInput)
                ratingInput.showDropDown()
            }
        }
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

        randomButton.setOnClickListener {
            exploreViewModel.onQueryChanged(searchInput.text?.toString().orEmpty())
            exploreViewModel.onProvinceChanged(normalizeProvinceSelection(areaInput.text?.toString().orEmpty()))
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
            if (isChecked) {
                setAdvancedFiltersExpanded(
                    expanded = true,
                    advancedFiltersLayout = advancedFiltersLayout,
                    filterToggleButton = filterToggleButton,
                )
                ensureCurrentLocation()
            }
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
        exploreViewModel.onProvinceChanged(normalizeProvinceSelection(province))
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
        progressBar: LinearProgressIndicator,
        messageText: MaterialTextView,
        retryButton: MaterialButton,
    ) {
        progressBar.isVisible = state.isLoading || state.isRandomLoading
        placeAdapter.submitList(state.places)

        syncAreaSuggestions(state.availableProvinces)

        val selectedProvinceLabel = provinceLabelForQuery(state.selectedProvince)
        if (!areaInput.hasFocus() && areaInput.text?.toString().orEmpty() != selectedProvinceLabel) {
            areaInput.setText(selectedProvinceLabel, false)
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

            if (state.randomSuggestionToken != lastHandledRandomSuggestionToken) {
                lastHandledRandomSuggestionToken = state.randomSuggestionToken
                showPlaceDetail(randomSuggestion)
            }
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

    private fun syncAreaSuggestions(@Suppress("UNUSED_PARAMETER") areas: List<String>) {
        val normalizedAreas = buildAreaDisplayOptions()
        val current = (0 until areaAdapter.count).mapNotNull { areaAdapter.getItem(it) }
        if (current == normalizedAreas) {
            return
        }
        areaAdapter.clear()
        areaAdapter.addAll(normalizedAreas)
        areaAdapter.notifyDataSetChanged()
    }

    private fun normalizeProvinceSelection(raw: String): String {
        val value = raw.trim()
        if (value.isBlank()) {
            return ""
        }
        if (value == getString(R.string.explore_all_areas_option)) {
            return ""
        }

        val normalizedSelection = value.lowercase(Locale.ROOT)
        return predefinedAreaOptions.firstOrNull { getString(it.labelResId) == value }?.query
            ?: normalizedSelection
    }

    private fun provinceLabelForQuery(query: String): String {
        if (query.isBlank()) {
            return getString(R.string.explore_all_areas_option)
        }

        val normalizedQuery = query.lowercase(Locale.ROOT)
        return predefinedAreaOptions.firstOrNull { it.query == normalizedQuery }
            ?.let { getString(it.labelResId) }
            ?: query
    }

    private fun buildAreaDisplayOptions(): List<String> {
        return buildList {
            add(getString(R.string.explore_all_areas_option))
            predefinedAreaOptions.forEach { option ->
                add(getString(option.labelResId))
            }
        }
    }

    private fun setAdvancedFiltersExpanded(
        expanded: Boolean,
        advancedFiltersLayout: View,
        filterToggleButton: MaterialButton,
    ) {
        advancedFiltersLayout.isVisible = expanded
        if (expanded) {
            view?.findViewById<AutoCompleteTextView>(R.id.actExploreArea)?.let(::refreshDropdownBounds)
            view?.findViewById<AutoCompleteTextView>(R.id.actExploreMinRating)?.let(::refreshDropdownBounds)
        }
        filterToggleButton.text = if (expanded) {
            getString(R.string.explore_filter_toggle_hide)
        } else {
            getString(R.string.explore_filter_toggle_show)
        }
    }

    private fun refreshDropdownBounds(input: AutoCompleteTextView) {
        val inputWidth = input.width
        val parentWidth = (input.parent as? View)?.width ?: 0
        val targetWidth = when {
            inputWidth > 0 -> inputWidth
            parentWidth > 0 -> parentWidth
            else -> ViewGroup.LayoutParams.WRAP_CONTENT
        }

        input.dropDownWidth = targetWidth
        input.dropDownHorizontalOffset = 0
    }

    private fun ensureCurrentLocation() {
        val permissionState = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun fetchCurrentLocation() {
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location == null) {
                    onLocationUnavailable(getString(R.string.explore_location_unavailable))
                    return@addOnSuccessListener
                }

                currentLocationLat = location.latitude
                currentLocationLng = location.longitude
                exploreViewModel.onCurrentLocationUpdated(location.latitude, location.longitude)
            }
            .addOnFailureListener {
                onLocationUnavailable(getString(R.string.explore_location_unavailable))
            }
    }

    private fun onLocationUnavailable(message: String) {
        currentLocationLat = null
        currentLocationLng = null
        exploreViewModel.onCurrentLocationUnavailable(message)
        view?.findViewById<Chip>(R.id.chipNearMe)?.isChecked = false
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun buildRatingOptions(): List<String> {
        return listOf(
            getString(R.string.explore_rating_1_option),
            getString(R.string.explore_rating_2_option),
            getString(R.string.explore_rating_3_option),
            getString(R.string.explore_rating_4_option),
            getString(R.string.explore_rating_5_option),
        )
    }

    private fun minRatingFromPosition(position: Int): Int? {
        return when (position) {
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
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
        val imageView = dialogView.findViewById<ImageView>(R.id.ivPlaceDetailImage)
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
        imageView.load(place.imageUrl?.takeIf { it.isNotBlank() }) {
            crossfade(true)
            placeholder(R.drawable.bg_place_image_placeholder)
            error(R.drawable.bg_place_image_placeholder)
            fallback(R.drawable.bg_place_image_placeholder)
        }
        openingText.text = getString(
            R.string.explore_detail_opening_badge_format,
            valueOrUpdating(place.openHours),
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
        val currentLocationValue = coordinatesOrUpdating(currentLocationLat, currentLocationLng)
        coordinatesText.append(
            "\n" + detailLine(R.string.explore_detail_current_location, currentLocationValue),
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
        val origin = if (currentLocationLat != null && currentLocationLng != null) {
            "${currentLocationLat},${currentLocationLng}"
        } else {
            null
        }

        if (place.lat != null && place.lng != null) {
            return if (origin != null) {
                "https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(origin)}&destination=${place.lat},${place.lng}&travelmode=driving"
            } else {
                "https://www.google.com/maps/search/?api=1&query=${place.lat},${place.lng}"
            }
        }

        val directUrl = normalizedValue(place.googleMapsUrl)
        if (directUrl != null && origin == null) {
            return directUrl
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

        return if (origin != null) {
            "https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(origin)}&destination=${Uri.encode(query)}&travelmode=driving"
        } else {
            "https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"
        }
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
