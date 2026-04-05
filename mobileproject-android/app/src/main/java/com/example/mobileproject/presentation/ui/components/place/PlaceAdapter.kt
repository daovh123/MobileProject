package com.example.mobileproject.presentation.ui.components.place

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.Place
import android.widget.ImageView
import com.google.android.material.textview.MaterialTextView

class PlaceAdapter(
    private val onPlaceClick: (Place) -> Unit = {},
) : ListAdapter<Place, PlaceAdapter.PlaceViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = getItem(position)
        holder.bind(place)
        holder.itemView.setOnClickListener { onPlaceClick(place) }
    }

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tagText: MaterialTextView = itemView.findViewById(R.id.tvPlaceTag)
        private val nameText: MaterialTextView = itemView.findViewById(R.id.tvPlaceName)
        private val addressText: MaterialTextView = itemView.findViewById(R.id.tvPlaceAddress)
        private val ratingText: MaterialTextView = itemView.findViewById(R.id.tvPlaceRating)
        private val openingHoursText: MaterialTextView = itemView.findViewById(R.id.tvPlaceOpeningHours)
        private val imageView: ImageView = itemView.findViewById(R.id.ivPlaceImage)

        fun bind(place: Place) {
            val context = itemView.context
            val fallback = context.getString(R.string.explore_updating)

            tagText.text = place.effectiveTag?.takeIf { it.isNotBlank() } ?: fallback
            nameText.text = place.name?.takeIf { it.isNotBlank() } ?: fallback
            addressText.text = place.address?.takeIf { it.isNotBlank() } ?: fallback

            ratingText.text = if (place.rating != null && place.reviewCount != null) {
                context.getString(R.string.explore_rating_format, place.rating, place.reviewCount)
            } else {
                context.getString(R.string.explore_rating_unknown)
            }

            openingHoursText.text = place.openHours?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.explore_open_hours_unknown)

            imageView.load(place.imageUrl?.takeIf { it.isNotBlank() }) {
                crossfade(true)
                placeholder(R.drawable.bg_place_image_placeholder)
                error(R.drawable.bg_place_image_placeholder)
                fallback(R.drawable.bg_place_image_placeholder)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem == newItem
        }
    }
}