package com.example.mobileproject.presentation.ui.screen.welcome

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.R

@SuppressLint("Range")
@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // LAYER GỐC: Toàn bộ nền màn hình là màu hồng Pastel thống nhất
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFE8E8)) // #FFEBEF
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {

        // ==========================================
        // KHÁT VỌNG SỬA LỖI 1: TẠO VIỀN KHUNG CHỮ NHẬT TRẮNG THEO ĐÚNG TỶ LỆ
        // ==========================================
        // Dựa trên kích thước Hình 3 (415x985) và Hình 4 (Toàn màn hình),
        // Thay vì làm Card nền trắng, ta dùng Box rỗng có Border màu trắng dày để tạo viền bo góc ôm xung quanh.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp) // Căn lề ngoài để viền trắng nằm lọt lòng màn hình hồng
                .border(
                    border = BorderStroke(width = 16.dp, color = Color.White), // Viền trắng siêu to bao quanh
                    shape = RoundedCornerShape(40.dp) // Bo góc lớn mềm mại đúng điệu Figma
                )
        )

        // LAYER NỘI DUNG CHÍNH: Xếp dọc từ trên xuống dưới nằm đè lên trên hệ thống viền
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp, vertical = 48.dp), // Padding rộng để chữ không chạm vào viền trắng vừa tạo
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // --- PHẦN 1: CỤM MASCOT & VẬT THỂ LAYER (ĐÃ SỬA THEO TỶ LỆ FIGMA CHUẨN) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp), // Tăng nhẹ không gian chứa để dải ruy băng tràn xuống thoải mái
                contentAlignment = Alignment.TopCenter
            ) {

                // 1. TRÁI TIM BÊN TRÁI (Cạnh tai Cáo) - Hình 5
                Image(
                    painter = painterResource(id = R.drawable.img_welcome_heart),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 0.dp, top = 130.dp) // Căn chỉnh vị trí snug bên tai cáo
                        .size(38.dp, 34.dp) // Tỷ lệ 41.27 x 37.25 từ Figma
                        .rotate(-28.52f), // Xoay đúng độ thiết kế
                    contentScale = ContentScale.Fit
                )

                // 2. TRÁI TIM TRÊN CAO (Giữa đầu Thỏ và Cáo) - Hình 6
                Image(
                    painter = painterResource(id = R.drawable.img_welcome_heart),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(start = 75.dp, top = 20.dp) // Đẩy sang phải để nằm trên đầu thỏ
                        .size(45.dp, 40.dp) // Tỷ lệ 50.78 x 45.83 từ Figma
                        .rotate(35.19f), // Xoay đúng độ thiết kế
                    contentScale = ContentScale.Fit
                )

                // 3. NHÂN VẬT CHÍNH (img_welcome_mascot) - Hình 2
                // Chiếm 85% chiều rộng màn hình (412/483)
                Image(
                    painter = painterResource(id = R.drawable.img_welcome_mascot),
                    contentDescription = "Mascot Couple",
                    modifier = Modifier
                        // Thay vì fillMaxWidth(1.4f), ta dùng requiredWidthIn hoặc tính theo % của màn hình
                        // Ép kích thước ảnh to hơn hẳn Box cha
                        .requiredWidth(340.dp) // Bạn tăng/giảm số dp này, ảnh ĐẢM BẢO sẽ to ra hoặc nhỏ lại lập tức
                        .height(251.dp)
                        .align(Alignment.TopCenter)
                        .padding(top = 50.dp),
                    contentScale = ContentScale.FillWidth
                )

                // 4. CỤM DẢI RUY BĂNG & CHỮ AFFINITY - ĐÃ KHỦNG BỐ TRÀN VIỀN
                // 4. CỤM DẢI RUY BĂNG & CHỮ AFFINITY - ĐÃ ÉP CỨNG ĐỘ RỘNG TRÀN VIỀN
                // Sử dụng Box định vị vị trí tổng, không dùng tỷ lệ phần trăm co giãn cho cha nữa
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 150.dp) // Điều chỉnh cao/thấp của dải băng so với mặt nhân vật tại đây
                        .wrapContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Dải ruy băng nền
                    Image(
                        painter = painterResource(id = R.drawable.img_welcome_ribbon),
                        contentDescription = null,
                        modifier = Modifier
                            // Ép dải ruy băng rộng hẳn ra ngoài viền trắng (Màn hình rộng khoảng 360dp, ta set 380dp)
                            .requiredWidth(380.dp)
                            .wrapContentHeight(),
                        contentScale = ContentScale.FillWidth
                    )

                    // Chữ Affinity nằm lọt lòng ruy băng
                    Image(
                        painter = painterResource(id = R.drawable.img_welcome_ribbon_text),
                        contentDescription = "Affinity",
                        modifier = Modifier
                            .requiredWidth(150.dp)
                            .wrapContentHeight()
                            // Thay vì dùng top, ta dùng bottom để kích chữ dịch ngược lên trên
                            .padding(bottom = 90.dp), // Bạn tăng số này lên (28.dp, 32.dp) nếu chữ vẫn muốn lên cao hơn
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // ====================================================================
// CỤM 2: HỘP CHỮ NHẬT TRẮNG BO GÓC CHỨA TEXT & BUTTON (ĐÃ FIX CO NGẮN LẠI)
// ====================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth(1f) // Độ rộng 415px / 483px tổng thể (~86%)
                    // ĐÃ SỬA: Thay đổi từ .height(450.dp) cố định sang wrapContentHeight()
                    // để hộp trắng tự động co ngắn và ôm khít nội dung theo trục dọc
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(32.dp)) // Bo góc 32dp cực kỳ mềm mại
                    .background(Color.White) // Nền màu trắng chuẩn chỉ
                    // ĐÃ SỬA: Tăng nhẹ padding vertical để hộp trắng có khoảng đệm trên/dưới cân đối khi co lại
                    .padding(vertical = 32.dp, horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                // Cột xếp dọc chứa toàn bộ nội dung chữ và nút bấm bên trong hộp trắng
                Column(
                    modifier = Modifier.wrapContentHeight(), // ĐÃ SỬA: Đối ứng theo tỷ lệ co giãn của cha
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // ĐÃ SỬA: Thay SpaceBetween bằng spacedBy(28.dp) để cố định khoảng cách vừa vặn giữa các phần tử,
                    // triệt tiêu hoàn toàn hiện tượng tự động giãn cách tạo mảng trống dư thừa.
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {

                    // 1. Title Section (Hình 3 - Chiều rộng 351px)
                    Column(
                        modifier = Modifier.fillMaxWidth(0.845f), // Tỷ lệ 351/415 lòng trong
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Welcome to your",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                            ),
                            color = Color(0xFF5C5254),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Affinity!",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                            ),
                            color = Color(0xFFFE8A8E), // Màu hồng Coral chuẩn
                            textAlign = TextAlign.Center
                        )
                    }

                    // 2. Description Text (Hình 4 - Chiều rộng 351px)
                    Text(
                        text = "Build your financial future and relationship, together. A private space designed for your shared journey.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                        ),
                        color = Color(0xFF6E6061),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.845f) // Tỷ lệ 351/415 lòng trong
                    )

                    // 3. Primary Button (Hình 5 & 6 - Rộng 252px, Cao 52px)
                    Button(
                        onClick = onGetStartedClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8A8E)),
                        shape = RoundedCornerShape(26.dp), // Dạng viên thuốc
                        modifier = Modifier
                            .fillMaxWidth(0.607f) // Tỷ lệ 252/415 chuẩn Figma
                            .height(52.dp) // Chiều cao cố định 52px
                    ) {
                        Text(
                            text = "GET STARTED!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                            )
                        )
                    }

                    // 4. Secondary Text Link (Hình 5 - Rộng 351px, Cao 32px)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.845f)
                            .height(32.dp)
                            .clickable { onLoginClick() },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                            ),
                            color = Color(0xFF9E9293)
                        )
                        Text(
                            text = "Log in",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                            ),
                            color = Color(0xFF5C5254)
                        )
                    }
                }
            }
        }
    }
}