package com.example.mobileproject.presentation.ui.screen.profile

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.R
import com.example.mobileproject.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerProfileScreen(
    accessToken: String,
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val primaryPink = Color(0xFFFE8A8E)
    val lightPinkBg = Color(0xFFFFF0F1)
    val textColor = Color(0xFF5C5254)
    val grayText = Color(0xFF8C7F7B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thông tin nửa kia",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        lightPinkBg,
                        Color.White,
                    )
                )
            )
    ) { innerPadding ->
        if (uiState.isLoadingCouple) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryPink)
            }
            return@Scaffold
        }

        val partner = uiState.partnerProfile
        if (partner == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.sticker_2),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp)
                    )
                    Text(
                        text = "Chưa kết nối với đối phương hoặc không tìm thấy dữ liệu.",
                        textAlign = TextAlign.Center,
                        color = grayText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            return@Scaffold
        }

        val displayName = partner.fullName?.takeIf { it.isNotBlank() }
            ?: partner.nickName?.takeIf { it.isNotBlank() }
            ?: partner.username.orEmpty()
        val username = partner.username.orEmpty()
        val nickname = partner.nickName ?: "Chưa đặt biệt danh"
        val fullName = partner.fullName ?: "Chưa cập nhật họ tên"
        val daysTogether = partner.daysTogether ?: 0L
        val anniversaryDate = partner.startAt ?: "Chưa cập nhật"

        val birthDateFormatted = partner.birthDate?.let {
            runCatching {
                val parts = it.split("-")
                if (parts.size == 3) {
                    "${parts[2]}/${parts[1]}/${parts[0]}"
                } else {
                    it
                }
            }.getOrDefault(it)
        } ?: "Chưa cập nhật"

        val genderText = when (partner.gender?.uppercase()) {
            "MALE" -> "Nam"
            "FEMALE" -> "Nữ"
            "OTHER" -> "Khác"
            else -> "Chưa cập nhật"
        }

        val phoneNumber = partner.phoneNumber ?: "Chưa cập nhật"

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // HERO AVATAR SECTION
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(4.dp, primaryPink, CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val partnerBitmap = uiState.partnerAvatarBitmap
                    if (partnerBitmap != null) {
                        Image(
                            bitmap = partnerBitmap.asImageBitmap(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(primaryPink.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "P",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = primaryPink
                            )
                        }
                    }
                }

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodyMedium,
                    color = grayText,
                    fontWeight = FontWeight.Medium
                )
            }

            // DETAILS ELEVATED CARD
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        DetailRow(
                            icon = Icons.Default.Person,
                            label = "Họ và tên",
                            value = fullName,
                            textColor = textColor,
                            grayText = grayText,
                            accentColor = primaryPink
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        DetailRow(
                            icon = Icons.Default.SentimentSatisfied,
                            label = "Biệt danh",
                            value = nickname,
                            textColor = textColor,
                            grayText = grayText,
                            accentColor = primaryPink
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        DetailRow(
                            icon = Icons.Default.Cake,
                            label = "Ngày sinh",
                            value = birthDateFormatted,
                            textColor = textColor,
                            grayText = grayText,
                            accentColor = primaryPink
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        DetailRow(
                            icon = Icons.Default.Wc,
                            label = "Giới tính",
                            value = genderText,
                            textColor = textColor,
                            grayText = grayText,
                            accentColor = primaryPink
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        DetailRow(
                            icon = Icons.Default.Phone,
                            label = "Số điện thoại",
                            value = phoneNumber,
                            textColor = textColor,
                            grayText = grayText,
                            accentColor = primaryPink
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        DetailRow(
                            icon = Icons.Default.Favorite,
                            label = "Số ngày bên nhau",
                            value = "$daysTogether ngày",
                            textColor = textColor,
                            grayText = grayText,
                            accentColor = primaryPink
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        DetailRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Ngày kết đôi",
                            value = anniversaryDate,
                            textColor = textColor,
                            grayText = grayText,
                            accentColor = primaryPink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    textColor: Color,
    grayText: Color,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(accentColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = grayText,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
