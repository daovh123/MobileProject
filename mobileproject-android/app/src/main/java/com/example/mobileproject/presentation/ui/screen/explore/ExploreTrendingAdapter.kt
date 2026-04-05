package com.example.mobileproject.presentation.ui.screen.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.Place
import com.google.android.material.textview.MaterialTextView

class ExploreTrendingAdapter(
    private val onPlaceClick: (Place) -> Unit,
) : ListAdapter<Place, ExploreTrendingAdapter.TrendingViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_explore_trending, parent, false)
        return TrendingViewHolder(view, onPlaceClick)
    }

    override fun onBindViewHolder(holder: TrendingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TrendingViewHolder(
        itemView: View,
        private val onPlaceClick: (Place) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val trendingImage: ImageView = itemView.findViewById(R.id.ivTrendingImage)
        private val trendingName: MaterialTextView = itemView.findViewById(R.id.tvTrendingName)
        private val trendingMeta: MaterialTextView = itemView.findViewById(R.id.tvTrendingMeta)
        private val trendingRating: MaterialTextView = itemView.findViewById(R.id.tvTrendingRating)

        fun bind(place: Place) {
            val context = itemView.context
            val unknown = context.getString(R.string.explore_updating)

            trendingName.text = place.name?.takeIf { it.isNotBlank() } ?: unknown

            val area = place.province?.takeIf { it.isNotBlank() }
                ?: place.district?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.explore_unknown_district)
            val tag = place.effectiveTag?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.explore_unknown_tag)
            trendingMeta.text = context.getString(R.string.explore_tag_district_format, tag, area)

            trendingRating.text = if (place.rating != null && place.reviewCount != null) {
                context.getString(R.string.explore_rating_format, place.rating, place.reviewCount)
            } else {
                context.getString(R.string.explore_rating_unknown)
            }

            trendingImage.load(place.imageUrl?.takeIf { it.isNotBlank() }) {
                crossfade(true)
                placeholder(R.drawable.bg_place_image_placeholder)
                error(R.drawable.bg_place_image_placeholder)
                fallback(R.drawable.bg_place_image_placeholder)
            }

            itemView.setOnClickListener { onPlaceClick(place) }
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
