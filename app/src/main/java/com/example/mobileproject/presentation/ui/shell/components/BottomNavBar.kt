package com.example.mobileproject.presentation.ui.shell.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.presentation.navigation.Routes

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    val selectedColor = Color(0xFF84CC16)
    val unselectedColor = Color(0xFF8B8B95)
    val sheetColor = Color.White
    val sheetShape: Shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)

    val sideItems = listOf(
        NavItem(Routes.Dashboard, "HOME", Icons.Outlined.Home),
        NavItem(Routes.Health, "HEALTH", Icons.Outlined.FavoriteBorder),
        NavItem(Routes.Activity, "ACTIVITY", Icons.Outlined.ShowChart), // Tương tự như nhịp tim
        NavItem(Routes.Treasury, "WEALTH", Icons.Outlined.AccountBalanceWallet),
    )
    val pawRoute = Routes.Mentor

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Soft green glow behind the navbar that bleeds downward.
        // Using unbounded layout so it doesn't stretch the container
        Box(
            modifier = Modifier
                .offset(y = (-50).dp)
                .size(150.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(selectedColor.copy(alpha = 0.35f), Color.Transparent),
                    ),
                    shape = CircleShape
                )
        )

        // The actual Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(sheetShape)
                .background(sheetColor)
                .navigationBarsPadding() // Optional: native support for insets
                .padding(horizontal = 24.dp)
                .padding(top = 10.dp, bottom = 32.dp)
                .height(64.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                SideNavItem(
                    icon = sideItems[0].icon,
                    label = sideItems[0].label,
                    selected = currentRoute == sideItems[0].route,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    onClick = { onNavigate(sideItems[0].route) },
                )
                SideNavItem(
                    icon = sideItems[1].icon,
                    label = sideItems[1].label,
                    selected = currentRoute == sideItems[1].route,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    onClick = { onNavigate(sideItems[1].route) },
                )

                Spacer(Modifier.width(60.dp)) // Space for center PAW

                SideNavItem(
                    icon = sideItems[2].icon,
                    label = sideItems[2].label,
                    selected = currentRoute == sideItems[2].route,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    onClick = { onNavigate(sideItems[2].route) },
                )
                SideNavItem(
                    icon = sideItems[3].icon,
                    label = sideItems[3].label,
                    selected = currentRoute == sideItems[3].route,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    onClick = { onNavigate(sideItems[3].route) },
                )
            }
        }

        // Center Paw Button (raised)
        Column(
            modifier = Modifier
                // We shift it up to accommodate the new increased bottom padding
                .offset(y = (-44).dp), 
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                onClick = { onNavigate(pawRoute) },
                modifier = Modifier
                    .size(68.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = selectedColor.copy(alpha = 0.5f),
                        ambientColor = selectedColor.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(selectedColor), // Always green in image
            ) {
                Icon(
                    imageVector = Icons.Filled.Pets, // The paw icon is filled white in image
                    contentDescription = "Paw",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "PAW",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.2).sp,
                color = selectedColor, // Always green
            )
        }
    }
}

@Composable
private fun SideNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val tint = if (selected) selectedColor else unselectedColor
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
                color = tint,
            )
        }
    }
}
