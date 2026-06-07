package com.example.mobileproject.presentation.ui.screen.couple

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme
import com.example.mobileproject.presentation.viewmodel.ProfileViewModel

class CoupleConnectedActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ACCESS_TOKEN: String = "extra_access_token"
        const val EXTRA_RELATIONSHIP_START_DATE: String = "extra_relationship_start_date"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty()
        val relationshipStartDate = intent.getStringExtra(EXTRA_RELATIONSHIP_START_DATE).orEmpty()
        enableImmersiveMode()
        setContent {
            MobileProjectTheme {
                CoupleConnectedScreen(
                    accessToken = accessToken,
                    relationshipStartDate = relationshipStartDate,
                    onGoHome = {
                        startActivity(
                            Intent(this, HomeActivity::class.java)
                                .putExtra(HomeActivity.EXTRA_ACCESS_TOKEN, accessToken)
                        )
                        finish()
                    }
                )
            }
        }
    }

    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

@Composable
fun rememberBase64Bitmap(dataUrl: String?): android.graphics.Bitmap? {
    if (dataUrl.isNullOrBlank()) return null
    return remember(dataUrl) {
        try {
            val base64 = dataUrl.substringAfter(",", missingDelimiterValue = "")
            if (base64.isNotBlank()) {
                val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            null
        }
    }
}

fun formatRelationshipDate(isoDateStr: String): String {
    return try {
        // Try parsing ISO Instant (e.g. 2026-06-04T12:00:00Z)
        val instant = java.time.Instant.parse(isoDateStr)
        val localDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("d 'Tháng' M, yyyy", java.util.Locale("vi"))
        localDate.format(formatter)
    } catch (e: Exception) {
        try {
            // Fallback for simple yyyy-MM-dd
            val localDate = java.time.LocalDate.parse(isoDateStr)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("d 'Tháng' M, yyyy", java.util.Locale("vi"))
            localDate.format(formatter)
        } catch (ex: Exception) {
            isoDateStr.takeIf { it.isNotBlank() } ?: "12 Tháng 6, 2022"
        }
    }
}

@Composable
private fun CoupleConnectedScreen(
    accessToken: String,
    relationshipStartDate: String,
    onGoHome: () -> Unit,
) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val uiState by profileViewModel.uiState.collectAsState()

    LaunchedEffect(accessToken) {
        if (accessToken.isNotBlank()) {
            profileViewModel.ensureProfileLoaded(accessToken)
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val primaryPink = colorScheme.primary
    val lightPinkBg = colorScheme.primaryContainer
    val textColor = colorScheme.onSurface
    val grayText = colorScheme.onSurfaceVariant

    val myProfile = uiState.savedProfile
    val partnerProfile = uiState.partnerProfile

    val myName = myProfile?.nickName ?: myProfile?.fullName ?: "Bạn"
    val partnerName = partnerProfile?.nickName ?: partnerProfile?.fullName ?: "Nửa kia"

    val userBitmap = rememberBase64Bitmap(myProfile?.avatarUrl)
    val partnerBitmap = rememberBase64Bitmap(partnerProfile?.avatarUrl)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // White Card Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Overlapping circular profile pictures
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((-18).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User Circle
                            Surface(
                                modifier = Modifier.size(96.dp),
                                shape = CircleShape,
                                border = BorderStroke(3.dp, colorScheme.surfaceContainerLowest),
                                shadowElevation = 4.dp,
                                color = colorScheme.surfaceContainerLowest
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(lightPinkBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (userBitmap != null) {
                                        Image(
                                            bitmap = userBitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = myName.firstOrNull()?.uppercase() ?: "?",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryPink
                                        )
                                    }
                                }
                            }

                            // Partner Circle
                            Surface(
                                modifier = Modifier.size(96.dp),
                                shape = CircleShape,
                                border = BorderStroke(3.dp, colorScheme.surfaceContainerLowest),
                                shadowElevation = 4.dp,
                                color = colorScheme.surfaceContainerLowest
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(lightPinkBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (partnerBitmap != null) {
                                        Image(
                                            bitmap = partnerBitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = partnerName.firstOrNull()?.uppercase() ?: "?",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }

                        // Pink Heart Badge in the overlap center
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(colorScheme.surfaceContainerLowest, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(lightPinkBg, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    tint = primaryPink,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = stringResource(R.string.connected_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle
                    val defaultSubtitle = stringResource(R.string.connected_subtitle)
                    val displaySubtitle = if (myName.isNotBlank() && partnerName.isNotBlank() && myName != "Bạn" && partnerName != "Nửa kia") {
                        "$myName và $partnerName từ nay sẽ cùng nhau xây dựng tổ ấm tài chính của mình."
                    } else {
                        defaultSubtitle
                    }
                    Text(
                        text = displaySubtitle,
                        fontSize = 14.sp,
                        color = grayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // RELATIONSHIP STARTED Section
                    Text(
                        text = "NGÀY BẮT ĐẦU MỐI QUAN HỆ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = grayText,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date Pill
                    val dateValue = formatRelationshipDate(relationshipStartDate.takeIf { it.isNotBlank() } ?: uiState.coupleStatus?.startAt.orEmpty())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .background(lightPinkBg, shape = RoundedCornerShape(50.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = grayText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = dateValue,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f, fill = false),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = grayText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Milestones Subtext
                    Text(
                        text = "Chúng tôi sẽ dùng ngày này để kỷ niệm các cột mốc của hai bạn.",
                        fontSize = 11.sp,
                        color = grayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.85f),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Primary Button: Go to Home
                    Button(
                        onClick = onGoHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryPink,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Về Trang chủ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary text button: View Connection Settings
                    Text(
                        text = "Xem thiết lập kết nối",
                        color = grayText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable { /* Connection Settings */ }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            // Fox & Bunny couple sticker overlaying bottom left corner
            Image(
                painter = painterResource(R.drawable.sticker_6),
                contentDescription = null,
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = 10.dp, y = (-22).dp)
            )
        }
    }
}
