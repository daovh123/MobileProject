package com.example.mobileproject.presentation.ui.components.place

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.Place

@Composable
fun PlaceCard(
    place: Place,
    modifier: Modifier = Modifier,
    onClick: (Place) -> Unit = {},
) {
    val fallback = stringResource(R.string.explore_updating)
    val colorScheme = MaterialTheme.colorScheme
    val cardShape = RoundedCornerShape(24.dp)
    val locationText = place.address?.takeIf { it.isNotBlank() } ?: fallback
    val tagText = place.effectiveTag?.takeIf { it.isNotBlank() } ?: fallback
    val ratingText = if (place.rating != null && place.reviewCount != null) {
        stringResource(R.string.explore_rating_format, place.rating, place.reviewCount)
    } else {
        stringResource(R.string.explore_rating_unknown)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(cardShape)
            .clickable { onClick(place) },
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.26f)),
        shape = cardShape,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PlaceImagePlaceholder(modifier = Modifier.fillMaxSize())
            AsyncImage(
                model = place.imageUrl?.takeIf { it.isNotBlank() },
                contentDescription = place.name ?: fallback,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.38f),
                                Color.Black.copy(alpha = 0.86f),
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            ) {
                Surface(
                    color = colorScheme.primaryContainer.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.25f)),
                ) {
                    Text(
                        text = tagText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Surface(
                    color = colorScheme.secondaryContainer.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.22f)),
                ) {
                    Text(
                        text = ratingText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp, top = 10.dp),
            ) {
                Text(
                    text = place.name?.takeIf { it.isNotBlank() } ?: fallback,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.90f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.20f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = place.openHours?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.explore_open_hours_unknown),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun TrendingPlaceCard(
    place: Place,
    modifier: Modifier = Modifier,
    onClick: (Place) -> Unit = {},
) {
    val fallback = stringResource(R.string.explore_updating)
    val colorScheme = MaterialTheme.colorScheme
    val area = place.province?.takeIf { it.isNotBlank() }
        ?: place.district?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.explore_unknown_district)
    val tag = place.effectiveTag?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.explore_unknown_tag)
    val ratingText = if (place.rating != null && place.reviewCount != null) {
        stringResource(R.string.explore_rating_format, place.rating, place.reviewCount)
    } else {
        stringResource(R.string.explore_rating_unknown)
    }

    Card(
        modifier = modifier
            .width(238.dp)
            .aspectRatio(238f / 168f)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick(place) },
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.24f)),
        shape = RoundedCornerShape(22.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PlaceImagePlaceholder(modifier = Modifier.fillMaxSize())
            AsyncImage(
                model = place.imageUrl?.takeIf { it.isNotBlank() },
                contentDescription = place.name ?: fallback,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.36f),
                                Color.Black.copy(alpha = 0.82f),
                            )
                        )
                    )
            )

            Surface(
                color = colorScheme.secondaryContainer.copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            ) {
                Text(
                    text = ratingText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp, top = 10.dp),
            ) {
                Text(
                    text = place.name?.takeIf { it.isNotBlank() } ?: fallback,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.explore_tag_district_format, tag, area),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PlaceImagePlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(colorResource(R.color.md3_tertiary_container)), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(R.drawable.ic_restaurant_24),
            contentDescription = null,
            tint = colorResource(R.color.md3_on_tertiary_container),
            modifier = Modifier.size(44.dp),
        )
    }
}
