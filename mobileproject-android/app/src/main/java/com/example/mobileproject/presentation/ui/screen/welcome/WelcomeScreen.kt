/**
 * WelcomeScreen - Màn hình chào mừng đầu tiên của ứng dụng.
 *
 * Mục đích:
 * - Giới thiệu ứng dụng đến người dùng mới với hình ảnh mascot và ribbon.
 * - Cung cấp 2 lựa chọn: "Bắt đầu" (đăng ký) hoặc "Đăng nhập".
 *
 * Layout:
 * - Sử dụng [BoxWithConstraints] để phát hiện chiều cao màn hình, quyết định có cuộn hay không.
 * - Màn hình nhỏ (< 760dp) sẽ bật cuộn dọc và sắp xếp theo khoảng cách đều (spacedBy).
 * - Màn hình lớn sẽ căn giữa đều (SpaceEvenly) mà không cần cuộn.
 * - [Box] outer dùng làm nền primaryContainer với viền bo tròn tạo hiệu ứng "khung tranh".
 * - [Column] chứa hình ảnh mascot + ribbon ở trên, card nội dung (tiêu đề + nút) ở dưới.
 *
 * Được host bởi: [WelcomeActivity]
 *
 * Navigation:
 * - [onGetStartedClick] → chuyển đến màn hình đăng ký ([RegisterActivity]).
 * - [onLoginClick] → chuyển đến màn hình đăng nhập ([LoginActivity]).
 */
package com.example.mobileproject.presentation.ui.screen.welcome

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.R

/**
 * Composable chính của màn hình Welcome.
 *
 * Hiển thị hình ảnh mascot, ribbon chào mừng, tiêu đề ứng dụng và các nút điều hướng.
 * Không quan sát ViewModel - đây là màn hình tĩnh chỉ chịu trách nhiệm UI.
 *
 * @param onGetStartedClick Callback khi người dùng nhấn nút "Bắt đầu" → điều hướng đến đăng ký.
 * @param onLoginClick Callback khi người dùng nhấn "Đăng nhập" → điều hướng đến đăng nhập.
 * @param modifier Modifier tùy chỉnh cho toàn bộ màn hình.
 */
@SuppressLint("Range")
@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Lưu trạng thái cuộn để hỗ trợ màn hình nhỏ
    val scrollState = rememberScrollState()

    // BoxWithConstraints: phát hiện chiều cao thực tế của màn hình để quyết định layout responsive.
    // Màn hình nhỏ (< 760dp) sẽ bật scroll, màn hình lớn căn đều SpaceEvenly.
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        // Điều kiện responsive: bật cuộn khi màn hình quá nhỏ để chứa hết nội dung
        val enableScroll = maxHeight < 760.dp

        // Box outer: lớp nền primaryContainer bao toàn màn hình, có padding thanh hệ thống
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            // Viền trang trí: tạo hiệu ứng "khung tranh" với border 16dp bo tròn 40dp
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .border(
                        border = BorderStroke(
                            width = 16.dp,
                            color = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(40.dp)
                    )
            )

            // Column chính: chứa mascot + card nội dung.
            // Layout: Column (dọc) vì thứ tự các thành phần từ trên xuống dưới.
            // Khi màn hình nhỏ thì dùng spacedBy, khi lớn thì SpaceEvenly để lấp đầy.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (enableScroll) Modifier.verticalScroll(scrollState) else Modifier)
                    .padding(horizontal = 36.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (enableScroll) {
                    Arrangement.spacedBy(18.dp)
                } else {
                    Arrangement.SpaceEvenly
                }
            ) {
                // Vùng hình ảnh: chứa mascot, ribbon và các trái tim trang trí.
                // Chiều cao thay đổi tùy theo enableScroll để tối ưu không gian.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (enableScroll) 280.dp else 300.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Trái tim trang trí góc trái, xoay -28.52 độ tạo hiệu ứng tự nhiên
                    Image(
                        painter = painterResource(id = R.drawable.img_welcome_heart),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 130.dp)
                            .size(38.dp, 34.dp)
                            .rotate(-28.52f),
                        contentScale = ContentScale.Fit
                    )

                    Image(
                        painter = painterResource(id = R.drawable.img_welcome_heart),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(start = 75.dp, top = 20.dp)
                            .size(45.dp, 40.dp)
                            .rotate(35.19f),
                        contentScale = ContentScale.Fit
                    )

                    // Mascot chính: hình ảnh linh vật ứng dụng, đặt ở giữa trên cùng
                    Image(
                        painter = painterResource(id = R.drawable.img_welcome_mascot),
                        contentDescription = null,
                        modifier = Modifier
                            .requiredWidth(340.dp)
                            .height(251.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = 50.dp),
                        contentScale = ContentScale.FillWidth
                    )

                    // Ribbon + text: dải banner phía dưới mascot chứa slogan
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 150.dp)
                            .wrapContentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ribbon background
                        Image(
                            painter = painterResource(id = R.drawable.img_welcome_ribbon),
                            contentDescription = null,
                            modifier = Modifier
                                .requiredWidth(380.dp)
                                .wrapContentHeight(),
                            contentScale = ContentScale.FillWidth
                        )

                        Image(
                            painter = painterResource(id = R.drawable.img_welcome_ribbon_text),
                            contentDescription = null,
                            modifier = Modifier
                                .requiredWidth(150.dp)
                                .wrapContentHeight()
                                .padding(bottom = 90.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Card nội dung: chứa tiêu đề, phụ đề, nút "Bắt đầu" và link "Đăng nhập".
                // Sử dụng Box bo tròn 32dp với nền surface để tạo floating card effect.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(vertical = 28.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.wrapContentHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(0.845f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.welcome_title_line1),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.welcome_title_line2),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 32.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            text = stringResource(R.string.welcome_subtitle),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.845f)
                        )

                        Button(
                            onClick = onGetStartedClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(26.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.607f)
                                .height(52.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.welcome_get_started),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.845f)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onLoginClick() }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.welcome_already_have_account),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.welcome_login),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
