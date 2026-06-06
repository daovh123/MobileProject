# PHÂN TÍCH CHI TIẾT CẤU TRÚC MÀN HÌNH ANDROID — LOVE WALLET

> **Mục tiêu:** Phân tích từng màn hình trong ứng dụng Android: cấu trúc UI, các component ghép lại ra sao, tại sao thiết kế như vậy.

---

## MỤC LỤC

1. [Hệ thống Design System & Theme](#1-hệ-thống-design-system--theme)
2. [Hệ thống Navigation](#2-hệ-thống-navigation)
3. [Hệ thống Icon](#3-hệ-thống-icon)
4. [Shared Components (UI Kit)](#4-shared-components-ui-kit)
5. [Wallet Components](#5-wallet-components)
6. [Welcome Screen](#6-welcome-screen)
7. [Login Screen](#7-login-screen)
8. [Register Screen](#8-register-screen)
9. [Personal Info Screen](#9-personal-info-screen)
10. [Couple Connect Screen](#10-couple-connect-screen)
11. [Couple Connected Screen](#11-couple-connected-screen)
12. [Home Screen (Dashboard)](#12-home-screen-dashboard)
13. [Home Scaffold (Main Container)](#13-home-scaffold-main-container)
14. [Home Top Bar](#14-home-top-bar)
15. [Wallet Screen](#15-wallet-screen)
16. [Top-Up Screen](#16-top-up-screen)
17. [Top-Up QR Screen](#17-top-up-qr-screen)
18. [Top-Up Bank Redirect Screen](#18-top-up-bank-redirect-screen)
19. [Transfer Money Screen](#19-transfer-money-screen)
20. [QR Scanner Screen](#20-qr-scanner-screen)
21. [Recent Transactions Screen](#21-recent-transactions-screen)
22. [Saving Goals Screen](#22-saving-goals-screen)
23. [Future Goals Screen](#23-future-goals-screen)
24. [Add Saving Goal Screen](#24-add-saving-goal-screen)
25. [Add Future Goal Screen](#25-add-future-goal-screen)
26. [Explore Screen](#26-explore-screen)
27. [Memories Screen](#27-memories-screen)
28. [Capture Moment Screen](#28-capture-moment-screen)
29. [Chat Screen](#29-chat-screen)
30. [Profile Screen](#30-profile-screen)
31. [Profile Edit Screen](#31-profile-edit-screen)
32. [Add Expense Screen](#32-add-expense-screen)
33. [Analytics Screen](#33-analytics-screen)
34. [Notification Screen](#34-notification-screen)
35. [Settings Screen](#35-settings-screen)
36. [Settings Detail Screens](#36-settings-detail-screens)
37. [Map Share Screen](#37-map-share-screen)

---

## 1. HỆ THỐNG DESIGN SYSTEM & THEME

### 1.1. Font: Plus Jakarta Sans

**File:** `theme/Theme.kt`

```
Font Family: Plus Jakarta Sans (Google Fonts)
Provider: com.google.android.gms.fonts
```

**Typography Scale:**
| Style | Weight | Size | Line Height | Letter Spacing | Dùng cho |
|---|---|---|---|---|---|
| displayLarge | SemiBold | 38sp | 46sp | -1.0sp | Hero text (số tiền lớn) |
| displayMedium | SemiBold | 32sp | 40sp | -0.6sp | Balance card amount |
| displaySmall | SemiBold | 26sp | 34sp | -0.4sp | Section hero |
| headlineLarge | SemiBold | 26sp | 34sp | -0.3sp | Screen titles |
| headlineMedium | SemiBold | 22sp | 30sp | -0.3sp | Card titles |
| headlineSmall | SemiBold | 18sp | 26sp | -0.2sp | Sub-section titles |
| titleLarge | SemiBold | 20sp | 28sp | 0sp | Card headers |
| titleMedium | SemiBold | 16sp | 24sp | 0sp | List item titles |
| titleSmall | Medium | 14sp | 20sp | 0sp | Labels |
| bodyLarge | Normal | 16sp | 24sp | 0sp | Body text |
| bodyMedium | Normal | 14sp | 20sp | 0sp | Description text |
| bodySmall | Normal | 12sp | 16sp | 0sp | Caption, timestamp |
| labelLarge | SemiBold | 14sp | 20sp | 0.1sp | Button text |
| labelMedium | Medium | 12sp | 16sp | 0.4sp | Chip labels |
| labelSmall | Medium | 11sp | 16sp | 0.4sp | Badge text |

**Tại sao chọn Plus Jakarta Sans?**
- Modern geometric sans-serif, giống Airbnb/San Francisco
- Letter-spacing âm cho display sizes → cảm giác premium, tight
- SemiBold weight cho headings → strong visual hierarchy
- Normal weight cho body → readable, comfortable

### 1.2. Shape System

```
extraSmall: 10.dp  → Chips, small badges
small:      14.dp  → Input fields, small cards
medium:     16.dp  → Standard cards
large:      24.dp  → Large cards, bottom sheets
extraLarge: 28.dp  → Hero cards, balance card
```

**Tại sao dùng rounded corners lớn?**
- Friendly, approachable aesthetic (phù hợp couples app)
- Consistent với Material 3 design language
- Tạo cảm giác soft, romantic

### 1.3. Color Palette

**Light Mode:**
| Role | Hex | Dùng cho |
|---|---|---|
| primary | `#FF385C` | Buttons, links, accents (Airbnb Rausch pink) |
| onPrimary | `#FFFFFF` | Text on primary |
| primaryContainer | `#FFF1F3` | Light pink surfaces |
| background/surface | `#FFFBFA` | Cream white background |
| surfaceContainerLow | `#FFF7F8` | Subtle surface variation |
| surfaceContainer | `#FFF1F3` | Card backgrounds |
| surfaceContainerHigh | `#FFE8EC` | Elevated surfaces |
| secondary | `#6B7B8D` | Muted text, icons |
| tertiary | `#FF6B81` | Accent pink, income color |
| error | `#EF4444` | Error states, expense color |
| outline | `#BFB0B3` | Borders |

**Dark Mode (Warm Dark):**
| Role | Hex | Dùng cho |
|---|---|---|
| primary | `#FF7A8F` | Soft warm pink |
| background/surface | `#1A1614` | Warm dark brown (NOT cold gray) |
| surfaceContainer | `#2A2523` | Dark surface layers |
| secondary | `#C4B8CC` | Muted text |
| error | `#FCA5A5` | Light error |

**Tại sao "Warm Dark" thay vì gray?**
- Tạo cảm giác "ấm cúng về đêm" — phù hợp romantic app
- Brown undertones thay vì blue/gray → ít harsh hơn
- Text dùng `#F5EDE8` (warm off-white) thay vì pure white → dễ nhìn hơn

### 1.4. Extended Theme

**File:** `theme/AppExtendedTheme.kt`

Cung cấp thêm colors beyond Material 3 spec:

| Color | Light | Dark | Dùng cho |
|---|---|---|---|
| gradientStart/End | `#FF6B8A`→`#FF385C` | `#FF7A8F`→`#CC2D4A` | Hero gradients |
| cardGradientStart/End | `#FF7A92`→`#FF385C` | `#D4607A`→`#8C1A32` | Balance card gradient |
| glassBackground | `#FFFFFF@88%` | `#1A1614@90%` | Glassmorphism surfaces |
| glassBorder | `#FFFFFF@45%` | `#F5EDE8@10%` | Glass border |
| heartPulse | `#FF385C` | `#FF7A8F` | Heart animations |
| successGreen | `#22C55E` | `#86EFAC` | Success states |
| warningAmber | `#F59E0B` | `#FBBF24` | Warning states |
| anniversaryGold | `#D4A574` | `#D4A574` | Anniversary highlights |

**Computed Brushes:**
- `primaryGradient`: LinearGradient từ gradientStart → gradientEnd
- `cardGradient`: LinearGradient cho balance cards
- `balanceCardGradient`: 3-stop gradient (cardGradientStart → cardGradientEnd → gradientEnd)

**Truy cập:** `AppTheme.extendedColors` trong Composable

---

## 2. HỆ THỐNG NAVIGATION

### 2.1. Navigation Config

**File:** `navigation/NavigationConfig.kt`

5 bottom nav items:

| Route | Label | Icon | Content Description |
|---|---|---|---|
| `home` | Trang chủ | LucideHome (Home) | Home |
| `explore` | Khám phá | LucideCompass (Explore) | Explore |
| `wallet` | Ví | LucideWallet (Wallet) | Wallet |
| `memories` | Kỷ niệm | LucideHeart (Favorite) | Memories |
| `profile` | Cá nhân | LucideUser (Person) | Profile |

### 2.2. App Navigation Bar

**File:** `navigation/AppNavigationBar.kt`

**Cấu trúc:**
```
Surface (shadowElevation=8dp, surface color)
  └── BoxWithConstraints
      └── Row (height=68dp)
          └── NavigationBarItemContent × 5
              └── Column (56dp touch target)
                  ├── Box (indicator pill nếu selected)
                  │   └── Surface (32×4dp, RoundedCornerShape(999dp), primary color)
                  ├── Icon (animateColorAsState: primary hoặc onSurfaceVariant)
                  └── Text (labelSmall, SemiBold nếu selected)
```

**Tại sao custom NavigationBar thay vì dùng M3 NavigationBar?**
- Kiểm soát hoàn toàn animation (indicator pill sliding)
- Custom indicator shape (32×4dp pill, không phải M3 indicator)
- Color animation với `animateColorAsState(tween(160ms))`
- Font weight animation (Normal → SemiBold)

### 2.3. Home Scaffold (NavHost Container)

**File:** `app/compose/HomeScaffold.kt`

**Cấu trúc:**
```
Scaffold
  ├── topBar: HomeTopBar (conditional visibility theo route)
  ├── bottomBar: AppNavigationBar (conditional visibility theo route)
  ├── snackbarHost: SnackbarHost (ChatInAppNotificationBus)
  └── content:
      └── Box
          ├── Decorative gradient circles (background)
          └── NavHost (startDestination = "home")
              ├── composable("home") → HomeScreen
              ├── composable("explore") → ExploreRoute
              ├── composable("wallet") → WalletScreen
              ├── composable("memories") → MemoriesScreen
              ├── composable("profile") → ProfileScreen
              ├── composable("profile_edit") → ProfileEditScreen
              ├── composable("chat") → ChatScreen
              ├── composable("add_expense") → AddExpenseScreen
              ├── composable("top_up") → TopUpScreen
              ├── composable("top_up_qr/{topUpId}") → TopUpQRScreen
              ├── composable("top_up_bank_redirect/{topUpId}") → TopUpBankRedirectScreen
              ├── composable("transfer_money") → TransferMoneyScreen
              ├── composable("recent_transactions") → RecentTransactionsScreen
              ├── composable("saving_goals") → SavingGoalsScreen
              ├── composable("future_goals") → FutureGoalsScreen
              ├── composable("add_saving_goal") → AddSavingGoalScreen
              ├── composable("add_future_goal") → AddFutureGoalScreen
              ├── composable("memories_capture") → CaptureMomentScreen
              ├── composable("notifications") → NotificationScreen
              ├── composable("settings") → SettingsScreen
              ├── composable("settings_privacy") → SettingsPrivacyScreen
              ├── composable("settings_notifications") → SettingsNotificationsScreen
              ├── composable("settings_appearance") → SettingsAppearanceScreen
              └── composable("settings_help") → SettingsHelpScreen
```

**Routes ẩn bottom/top bar:** chat, add_expense, top_up, top_up_qr/*, top_up_bank_redirect/*, transfer_money, recent_transactions, saving_goals, future_goals, add_saving_goal, add_future_goal, memories_capture, notifications, settings_privacy, settings_notifications, settings_appearance, settings_help

**Tại sao ẩn bars cho secondary screens?**
- Full-screen experience cho focused tasks
- Giảm visual clutter
- More space cho content

**BackHandler logic:**
1. Nếu có thể popBackStack → pop
2. Nếu đang ở home → finish activity
3. Otherwise → navigate về home

**Special integrations:**
- `ChatInAppNotificationBus`: Hiển thị snackbar khi nhận tin nhắn mới
- `NotificationRefreshBus`: Refresh notification count khi có sự kiện
- `ChatNotificationGate`: Suppress notifications khi đang ở chat screen

---

## 3. HỆ THỐNG ICON

### 3.1. Lucide Icons

**File:** `icons/LucideIcons.kt`

32 icon aliases map từ Material Icons Rounded:

```
LucideBell → Icons.Rounded.Notifications
LucideCamera → Icons.Rounded.PhotoCamera
LucideChevronRight → Icons.Rounded.ChevronRight
LucideClose → Icons.Rounded.Close
LucideDarkMode → Icons.Rounded.DarkMode
LucideCompass → Icons.Rounded.Explore
LucideEye → Icons.Rounded.Visibility
LucideEyeOff → Icons.Rounded.VisibilityOff
LucideEdit → Icons.Rounded.Edit
LucideHeart → Icons.Rounded.Favorite
LucideHome → Icons.Rounded.Home
LucideInfo → Icons.Rounded.Info
LucideImage → Icons.Rounded.Image
LucideLink → Icons.Rounded.Link
LucideLock → Icons.Rounded.Lock
LucideLogOut → Icons.AutoMirrored.Rounded.Logout
LucideMail → Icons.Rounded.Email
LucideMapPin → Icons.Rounded.Place
LucidePercent → Icons.Rounded.Percent
LucidePalette → Icons.Rounded.Palette
LucideSearch → Icons.Rounded.Search
LucideSend → Icons.Rounded.Send
LucideSettings → Icons.Rounded.Settings
LucideShare2 → Icons.Filled.Share
LucideShield → Icons.Rounded.Security
LucideSun → Icons.Rounded.LightMode
LucideSystemTheme → Icons.Rounded.AutoMode
LucideShoppingCart → Icons.Rounded.ShoppingCart
LucideUser → Icons.Rounded.Person
LucideRefreshCw → Icons.Rounded.Refresh
LucideReply → Icons.AutoMirrored.Rounded.Reply
LucideWallet → Icons.Rounded.AccountBalanceWallet
```

**Tại sao dùng alias thay vì Lucide library thật?**
- Không thêm dependency → giảm APK size
- Material Icons Rounded đã có sẵn, đủ dùng
- Consistent với Material Design ecosystem
- Dễ swap sang Lucide library sau nếu cần

### 3.2. Material Icons (Bottom Nav)

**File:** `icons/MaterialIcons.kt`

5 icons cho bottom navigation tabs:
```
Home → Icons.Filled.Home
Explore → Icons.Rounded.Explore
Wallet → Icons.Filled.Wallet
Heart → Icons.Filled.Favorite
Profile → Icons.Filled.Person
```

---

## 4. SHARED COMPONENTS (UI KIT)

### 4.1. App UI Kit

**File:** `components/core/AppUiKit.kt`

Các component nền tảng được dùng xuyên suốt app:

#### `AppScreenBackground`
```
Box
  ├── Modifier.background(verticalGradient: surface → surfaceContainerLow → surface@0.85f)
  ├── Decorative radial gradient orb (primary@0.1f, top-end corner)
  ├── Decorative radial gradient orb (tertiary@0.07f, bottom-start corner)
  └── content()
```
**Tại sao?** Tạo background nhất quán với subtle gradient + decorative orbs → visual depth, không nhàm chán.

#### `AppSurfaceCard`
```
Surface (tonalElevation=2dp, shadowElevation=8dp, large shape)
  └── content()
```
**Tại sao?** Consistent card styling trên toàn app. Tonal elevation tạo subtle depth, shadow tạo physical feel.

#### `AppSectionHeader`
```
Column
  ├── Text (titleLarge, Bold, onSurface)
  └── Text? (bodyMedium, onSurfaceVariant) — optional subtitle
```

#### `AppPrimaryButton`
```
Button (large shape, primary/onPrimary)
  └── Text (labelLarge)
```

#### `AppFormTextField`
```
OutlinedTextField (medium shape, custom colors)
  ├── Custom focus/unfocus border colors
  └── Supporting text, leading/trailing icons
```

#### `AppStateMessage`
```
Column (centered)
  ├── Text (titleMedium, onSurface)
  ├── Text (bodyMedium, onSurfaceVariant)
  └── TextButton? (optional action)
```

#### `AppMetricChip`
```
Surface (small shape, secondaryContainer@0.7f)
  └── Row
      ├── Text (labelMedium, onSecondaryContainer)
      └── Text (titleMedium, onSecondaryContainer)
```

#### `AppEmptyState`
```
Surface (large shape, surfaceContainerHigh)
  └── Column (centered, 24dp padding)
      ├── Text (titleMedium, onSurface)
      └── Text (bodyMedium, onSurfaceVariant)
```

### 4.2. Modern UI Components

**File:** `components/ModernUiComponents.kt`

#### `ModernGradientCard`
```
Surface (RoundedCornerShape(32dp))
  └── Box (Modifier.background(brush))
      └── content()
```

#### `AIInsightChip`
```
Surface (RoundedCornerShape(16dp), secondary@0.08f)
  └── Row
      ├── Icon (AutoAwesome, primary)
      └── Text (labelMedium, primary)
```

#### `SettingGroupCard`
```
Surface (RoundedCornerShape(28dp), surfaceContainerLowest)
  └── Column (20dp padding)
      └── content()
```

#### `SettingRow`
```
Row (clickable)
  ├── Surface (CircleShape, primary@0.12f, 40dp)
  │   └── Icon (primary)
  ├── Column
  │   ├── Text (titleSmall, Bold)
  │   └── Text? (bodySmall, onSurfaceVariant)
  └── trailing? (Switch, etc.)
```

#### `SquircleAvatar`
```
Surface (RoundedCornerShape(30dp))  ← "squircle" shape
  └── AsyncImage (Coil, 96dp default)
```
**Tại sao "squircle"?** Không tròn hoàn toàn, không vuông → unique visual identity, giống iOS app icons.

#### `SpendingDonutChart`
```
Canvas (200dp)
  ├── drawArc × N segments (Stroke style, 40f width, RoundCap)
  ├── Center text (selected percentage hoặc total)
  └── Clickable segments → onSelect(index)
```

#### `DiscoveryImageCard`
```
Card (RoundedCornerShape(24dp))
  └── Box
      ├── AsyncImage (fill)
      ├── Gradient overlay (Transparent → Black@0.6f)
      └── Column (bottom-aligned)
          ├── Text (titleMedium, White)
          ├── Text (bodySmall, White@0.8f)
          └── Row (rating badge)
```

#### `EmptyChatSuggestions`
```
LazyRow
  └── items → Surface (RoundedCornerShape(50dp), primary@0.08f)
              └── Text (labelMedium, primary)
```

#### `MomentCard`
```
Card (RoundedCornerShape(24dp))
  └── Column
      ├── AsyncImage (16:9 ratio)
      ├── Row
      │   ├── Text (titleMedium)
      │   ├── IconButton (pin toggle)
      │   └── Text (bodySmall, timestamp)
      └── footer?()
```

### 4.3. Auth Visuals

**File:** `components/auth/AuthVisuals.kt`

#### `AuthBackdrop`
```
Box (Modifier.background(linearGradient))
  ├── Dark mode: #1A1614 → #221E1C → #14110F
  ├── Light mode: #FFFBFA → #FFF1F3 → #FFE8EC
  ├── Decorative orb (primary@0.1f, top-end)
  ├── Decorative orb (secondary@0.09f, bottom-start)
  └── content()
```

#### `AuthBrandMark`
```
Surface (CircleShape, 80dp, Color.White@0.9f, shadowElevation=12dp)
  └── Text (heart emoji, headlineLarge)
  + Infinite pulse animation: scale 1.0 → 1.04, 1200ms
```

#### `AuthFormSurface`
```
Surface (RoundedCornerShape(28dp), Color.White@0.97f, shadowElevation=8dp)
  └── Column (24dp padding)
      └── content()
```
**Tại sao AuthFormSurface dùng Color.White cứng?** Tạo floating card effect trên backdrop, không phụ thuộc theme → consistent appearance trên cả light/dark.

### 4.4. Place Cards

**File:** `components/place/PlaceCards.kt`

#### `PlaceCard`
```
Card (RoundedCornerShape(24dp), surface@0.95f)
  └── Box
      ├── AsyncImage (360×250dp, với fallback system)
      ├── Gradient overlay (Transparent → Black@0.38f → Black@0.86f)
      ├── Top badges: Row
      │   ├── Surface (tag badge, primaryContainer@0.92f)
      │   └── Surface (rating badge, White@0.9f, star icon)
      └── Bottom info: Column
          ├── Text (titleMedium, White)
          ├── Text (bodySmall, White@0.85f, address)
          └── Text (labelSmall, open hours)
```

**Fallback image system:** Khi ảnh chính load lỗi → swap sang deterministic fallback URL dựa trên `placeId.hashCode()`.

#### `TrendingPlaceCard`
```
Card (RoundedCornerShape(22dp))
  └── Column
      ├── AsyncImage (238×168dp, với fallback)
      └── Column (12dp padding)
          ├── Text (titleSmall, Bold)
          ├── Text (bodySmall, secondary)
          └── Row
              ├── Icon (star, amber)
              └── Text (rating)
```

### 4.5. Monthly Calendar

**File:** `components/calendar/MonthlyCalendar.kt`

```
Card (RoundedCornerShape(20dp))
  └── Column
      ├── Text (month/year title)
      ├── Row (T2 T3 T4 T5 T6 T7 CN headers)
      └── Column (6 rows × 7 cols grid)
          └── CalendarDayCell × 42
              ├── Text (day number)
              └── Text? (note lines, primary color)
```

**Đặc biệt:** Tuần bắt đầu từ Thứ 2 (quy ước Việt Nam). Có `VietnamCalendarNotes` hardcode các ngày lễ VN (Tết, Valentine, 8/3, 30/4, 1/5, 2/9, 20/10, 20/11, 25/12).

### 4.6. Draggable Chat FAB

**File:** `components/core/DraggableChatFab.kt`

```
Surface (CircleShape, 56dp, primaryContainer, shadowElevation=12dp)
  └── Box
      ├── Text (chat icon label)
      └── Border (outlineVariant@0.18f)
```

**Draggable:** `pointerInput` + `detectDragGestures` → position saved via `rememberSaveable` (survives rotation). Clamped within container bounds.

---

## 5. WALLET COMPONENTS

### 5.1. Balance Section

**File:** `component/wallet/BalanceSection.kt`

```
ElevatedCard (extraLarge shape, 28dp, Color.Transparent container)
  └── Box (Modifier.background(extendedColors.balanceCardGradient))
      └── Column (24dp padding)
          ├── Text ("VÍ TÌNH YÊU", labelMedium, White@0.9f, letterSpacing=1.5sp)
          ├── Spacer(8dp)
          └── Text (formatted amount, displayMedium/40sp/Black weight, White)
```

**Tại sao dùng gradient cho balance card?**
- Hero element trên Wallet screen → cần nổi bật nhất
- Gradient hồng tạo cảm giác romantic, premium
- White text trên pink gradient → high contrast, readable

### 5.2. Goal Section

**File:** `component/wallet/GoalSection.kt`

```
Column
  ├── Row
  │   ├── Text ("Mục tiêu tiết kiệm", titleMedium, Bold)
  │   └── TextButton ("Xem tất cả")
  └── if (goals.isEmpty())
      │   Text ("Chưa có mục tiêu", onSurfaceVariant)
  └── else
      └── sortedGoals.take(2).map { GoalCard(it) }
```

**Sorting:** Achieved goals ở cuối, gần deadline lên trước.

### 5.3. Monthly Spending Card

**File:** `component/wallet/MonthlySpendingCard.kt`

```
Card (extraLarge shape, 28dp)
  └── Column
      ├── Row
      │   ├── Text ("Chi tiêu theo danh mục")
      │   └── Surface (month picker pill, RoundedCornerShape(14dp))
      │       └── Text (month name)
      ├── if (empty) Text ("Chưa có chi tiêu")
      └── else
          ├── Canvas (donut chart)
          │   ├── drawArc × N segments (Stroke 35f/50f selected)
          │   └── Center text (selected % hoặc total)
          └── LazyColumn (legend)
              └── items → Row (clickable)
                  ├── Surface (8dp circle, category color)
                  ├── Text (category name)
                  └── Text (percentage)
```

### 5.4. Recent Activity Section

**File:** `component/wallet/RecentActivitySection.kt`

```
Column
  ├── Row
  │   ├── Text ("Giao dịch gần đây")
  │   └── TextButton ("Xem tất cả")
  └── if (empty) Text ("Chưa có giao dịch")
  └── else transactions.take(5).map { TransactionItem(it, goals) }
```

### 5.5. Spending Trend Card

**File:** `component/wallet/SpendingTrendCard.kt`

```
Card (RoundedCornerShape(32dp))
  └── Column
      ├── Text ("Xu hướng chi tiêu")
      └── Canvas (line chart)
          ├── Path (income line, tertiary color, Stroke 4f)
          └── Path (expense line, primary color, Stroke 4f)
```

### 5.6. Transaction Item

**File:** `component/wallet/TransactionItem.kt`

```
Card (large shape)
  └── Row (clickable → showDetail dialog)
      ├── Surface (CircleShape, primaryContainer)
      │   └── Icon (category icon, primary)
      ├── Column
      │   ├── Text (category name hoặc goal name)
      │   └── Text (note + date, bodySmall, secondary)
      └── Text (amount, Bold)
          ├── Expense: error color (red)
          └── Income: tertiary color (green)
```

**Detail Dialog:**
```
Dialog
  └── Surface (extraLarge shape)
      └── Column
          ├── IconButton (close, top-right)
          ├── Icon (category, 64dp)
          ├── Text (amount, headlineLarge, Black weight)
          ├── HorizontalDivider
          ├── DetailRow × 5 (category, type, date, time, note)
          └── TextButton (close)
```

---

## 6. WELCOME SCREEN

**File:** `welcome/WelcomeScreen.kt`

### Cấu trúc UI
```
BoxWithConstraints
  └── Box (background: cream/pink)
      └── Box (BorderStroke, decorative frame)
          └── Column (scrollable when maxHeight < 760dp)
              ├── Box (image area)
              │   ├── Image (rabbit illustration)
              │   ├── Image (heart decorations)
              │   └── Image (ribbon)
              └── Box (card surface, bottom)
                  └── Column (centered, 32dp padding)
                      ├── Text ("Love Wallet", displaySmall)
                      ├── Text ("Cùng nhau quản lý tài chính", bodyLarge)
                      ├── Spacer(24dp)
                      ├── Button ("Bắt đầu", primary, full-width)
                      └── Row
                          ├── Text ("Đã có tài khoản?")
                          └── TextButton ("Đăng nhập")
```

### Components sử dụng
- `BoxWithConstraints` → responsive layout
- `Image` → illustrations (rabbit, heart, ribbon)
- `Button` → primary CTA
- `TextButton` → secondary action
- `BorderStroke` → decorative frame

### Tại sao thiết kế như vậy?
- **Onboarding pattern:** Illustration + title + CTA → standard mobile onboarding
- **BoxWithConstraints:** Scroll khi màn hình nhỏ (keyboard đẩy lên)
- **Illustration-driven:** Hình ảnh chiếm >50% screen → emotional connection
- **2 CTA rõ ràng:** "Bắt đầu" (primary) + "Đăng nhập" (secondary)

---

## 7. LOGIN SCREEN

**File:** `login/LoginActivity.kt`

### Cấu trúc UI
```
BoxWithConstraints
  └── Box (AuthBackdrop gradient)
      ├── Image (rabbit, top-left decorative)
      ├── Image (fox, top-right decorative)
      └── Column (scrollable, centered)
          └── Surface (AuthFormSurface — floating white card)
              └── Column (24dp padding)
                  ├── Text ("Chào mừng trở lại!", headlineMedium)
                  ├── Text ("Đăng nhập để tiếp tục", bodyMedium, secondary)
                  ├── AnimatedVisibility (registration success)
                  │   └── Surface (green tinted)
                  │       └── Text ("Đăng ký thành công!")
                  ├── LoginInputField (email)
                  │   └── OutlinedTextField + LucideMail icon
                  ├── LoginInputField (password)
                  │   └── OutlinedTextField + LucideLock icon + eye toggle
                  ├── Row (forgot password link, right-aligned)
                  ├── Button ("Đăng nhập", full-width, primary)
                  ├── AnimatedVisibility (error)
                  │   └── Surface (error tinted)
                  │       └── Text (error message, error color)
                  └── Row
                      ├── Text ("Chưa có tài khoản?")
                      └── TextButton ("Tạo tài khoản")
```

### Components sử dụng
- `AuthBackdrop`, `AuthFormSurface` (từ AuthVisuals.kt)
- `LoginInputField` (custom, wrapping OutlinedTextField)
- `AnimatedVisibility` → show/hide error, success messages
- `PasswordVisualTransformation` → ẩn/hiện password
- `KeyboardActions(onDone)` → auto-submit
- `LaunchedEffect(uiState.authSession)` → navigate on success

### State management
```
AuthViewModel → uiState.collectAsState()
  ├── isLoading → Button loading state
  ├── errorMessage → AnimatedVisibility + error text
  └── authSession → LaunchedEffect → navigate
```

### Tại sao thiết kế như vậy?
- **Floating card pattern:** White card trên gradient backdrop → depth, focus
- **Decorative illustrations:** Rabbit + fox tạo brand identity, friendly feel
- **AnimatedVisibility cho errors:** Smooth appear/disappear, không jump layout
- **Auto-submit on keyboard done:** Tiện lợi, giảm thao tác

---

## 8. REGISTER SCREEN

**File:** `register/RegisterActivity.kt`

### Cấu trúc UI
```
BoxWithConstraints
  └── Box (AuthBackdrop)
      ├── Image (rabbit)
      ├── Image (fox)
      └── Column (scrollable)
          └── Surface (AuthFormSurface)
              └── Column
                  ├── Text ("Tạo tài khoản", headlineMedium)
                  ├── Text ("Bắt đầu hành trình together", bodyMedium)
                  ├── RegisterInputField (email + LucideMail)
                  ├── RegisterInputField (nickname + LucideUser)
                  ├── RegisterInputField (password + LucideLock + eye toggle)
                  ├── RegisterInputField (birthday + LucideInfo)
                  ├── RegisterInputField (phone + LucidePhone)
                  ├── Button ("Tạo tài khoản", full-width)
                  ├── AnimatedVisibility (error)
                  └── Row ("Đã có tài khoản? Đăng nhập")
```

### State management
```
AuthViewModel → uiState.collectAsState()
  ├── LaunchedEffect(authSession) → navigate to LoginActivity với prefilled email
  └── Manual validation: email format, password ≥ 6 chars, non-blank fields
```

### Tại sao gần giống Login?
- **Consistent visual language:** Cùng AuthBackdrop + AuthFormSurface
- **Form-driven:** 5 input fields → cần scroll → BoxWithConstraints
- **Separate Activity thay vì NavHost:** Vì register là pre-auth flow

---

## 9. PERSONAL INFO SCREEN

**File:** `profile/PersonalInfoActivity.kt`

### Cấu trúc UI
```
Box (AuthBackdrop)
  └── Column
      ├── AuthBrandMark (animated heart)
      └── AuthFormSurface
          └── ProfileFormContent
              ├── OutlinedTextField (fullName)
              ├── OutlinedTextField (nickName)
              ├── DatePickerDialog trigger (birthDate)
              ├── FilterChip × 3 (MALE / FEMALE / OTHER)
              └── Button ("Tiếp tục")
```

### Components tái sử dụng
- `AuthBackdrop` + `AuthBrandMark` + `AuthFormSurface` từ AuthVisuals.kt
- `ProfileFormContent` từ profile/ProfileFormContent.kt

### Tại sao là Activity riêng?
- **Pre-auth flow:** Sau register, cần hoàn thành profile trước khi vào app
- **Không thuộc NavHost:** Vì chưa authenticated đầy đủ
- **Submit pattern:** `submitRequested` flag → save → LaunchedEffect detects success → navigate to HomeActivity

---

## 10. COUPLE CONNECT SCREEN

**File:** `couple/CoupleConnectScreen.kt` + `couple/CoupleConnectActivity.kt`

### Cấu trúc UI (Glass Morphism version)
```
Box (gradient background + background image)
  └── LazyColumn
      ├── Box (hero section)
      │   ├── Image (background)
      │   ├── Gradient overlay
      │   └── Column (centered)
      │       ├── Text ("Kết nối cùng nhau")
      │       └── Text (subtitle)
      ├── Surface (glass morphism — glassBackground + glassBorder)
      │   └── Column
      │       ├── Text ("Mã của bạn")
      │       ├── Surface (code display, XXX-XXX format)
      │       │   └── Row
      │       │       ├── Text (couple code, headlineLarge)
      │       │       └── IconButton (copy)
      │       └── Text (expires in 15 min)
      ├── Surface (glass morphism)
      │   └── Column
      │       ├── Text ("Nhập mã partner")
      │       ├── OutlinedTextField (code input)
      │       └── Button ("Gửi yêu cầu")
      ├── AnimatedVisibility (incoming request)
      │   └── Surface (glass morphism)
      │       ├── Text ("Partner muốn kết nối!")
      │       ├── Button ("Chấp nhận")
      │       └── OutlinedButton ("Từ chối")
      └── Heart pulse animation (InfiniteTransition)
```

### State management
```
CoupleViewModel → uiState.collectAsState()
  ├── myCoupleCode → Hiển thị mã
  ├── incomingRequestId → Hiển thị accept/reject UI
  ├── paired → LaunchedEffect → navigate to CoupleConnectedActivity
  └── Polling: startPolling() mỗi 3 giây
```

### Tại sao dùng Glass Morphism?
- **Visual distinctiveness:** Khác biệt với các screen khác → special occasion feel
- **Romantic aesthetic:** Glass effect + gradient → dreamy, beautiful
- **Content hierarchy:** Glass panels nổi trên gradient background

---

## 11. COUPLE CONNECTED SCREEN

**File:** `couple/CoupleConnectedActivity.kt`

### Cấu trúc UI
```
Box (gradient background)
  └── Column (centered)
      ├── Text (celebration emoji, 64sp)
      ├── Text ("Đã kết nối!", headlineLarge)
      ├── Text (relationship start date)
      ├── Surface (days together display)
      └── Button ("Vào trang chủ")
```

**Tại sao đơn giản?**
- Celebration screen → focus vào emotion, không phải functionality
- Transition screen → user sẽ nhanh chóng chuyển sang Home

---

## 12. HOME SCREEN (DASHBOARD)

**File:** `home/HomeScreen.kt` (961 lines — màn hình phức tạp nhất)

### Cấu trúc UI
```
Box
  └── HomeContent
      └── Column (scrollable)
          ├── SharedBalanceCard
          │   └── ElevatedCard (gradient background)
          │       └── Column
          │           ├── Text ("Số dư chung", White@0.9f)
          │           ├── Text (balance, displayMedium, White)
          │           └── Row (3 action buttons)
          │               ├── SharedBalanceActionButton ("Nạp tiền", add icon)
          │               ├── SharedBalanceActionButton ("Rút tiền", remove icon)
          │               └── SharedBalanceActionButton ("Đóng góp", heart icon)
          │
          ├── DaysTogetherModernCard
          │   └── Surface (gradient)
          │       └── Row
          │           ├── Column
          │           │   ├── Text ("Bên nhau")
          │           │   └── Text (days + "ngày", displaySmall)
          │           └── Heart pulse animation (InfiniteTransition)
          │
          ├── Row (2 MiniMetricCards)
          │   ├── MiniMetricCard ("Mục tiêu", count, target icon)
          │   └── MiniMetricCard ("Thành công", achieved, check icon)
          │
          ├── GoalListSection ("Mục tiêu tương lai", future goals)
          │   ├── Text (title)
          │   └── goals.map { GoalCard(it) }
          │
          ├── GoalListSection ("Mục tiêu tiết kiệm", saving goals)
          │   ├── Text (title)
          │   └── goals.map { GoalCard(it) }
          │
          └── ElevatedCard (Pair/Map section)
              ├── if (!paired) → PairSection
              │   └── Button ("Kết nối với partner")
              └── if (paired) → Map section
                  ├── Switch ("Chia sẻ vị trí")
                  ├── AndroidView (osmdroid MapView)
                  │   └── Marker (partner location)
                  └── Text (distance calculation)
          │
          └── FAB overlay (expandable)
              └── Column
                  ├── SmallFloatingActionButton ("Thêm mục tiêu tiết kiệm")
                  ├── SmallFloatingActionButton ("Thêm mục tiêu tương lai")
                  └── FloatingActionButton (main, rotate animation)
```

### Components sử dụng
- `SharedBalanceCard` (inline composable)
- `DaysTogetherModernCard` (inline composable)
- `MiniMetricCard` (inline composable)
- `GoalListSection` → delegates to `GoalCard`
- `PairSection` (inline composable)
- `SharedBalanceActionButton` (inline composable)
- `GoalFabItem` (inline composable)
- `AndroidView` (osmdroid MapView)
- `ContributeGoalBottomSheet`
- `Switch`, `FloatingActionButton`, `SmallFloatingActionButton`
- `InfiniteTransition` (heart pulse animation)
- `animateFloatAsState` (FAB rotation)

### ViewModels (4!)
```
CoupleViewModel → coupleStatus, paired, daysTogether
WalletViewModel → wallet balance
GoalViewModel → saving goals, future goals
HomeMapShareViewModel → shareLocationEnabled
```

### Tại sao Home screen phức tạp như vậy?
- **Dashboard pattern:** Tổng hợp thông tin quan trọng nhất
- **4 ViewModels:** Home là aggregation point — cần data từ nhiều domain
- **Map integration:** Vị trí partner ngay trên home → quick access
- **Expandable FAB:** Ẩn secondary actions, chỉ hiện khi cần
- **Heart pulse animation:** Romantic element, visual delight

---

## 13. HOME SCAFFOLD

*Đã mô tả ở phần Navigation (#2.3)*

---

## 14. HOME TOP BAR

**File:** `app/compose/HomeTopBar.kt`

### Cấu trúc UI
```
CenterAlignedTopAppBar
  ├── navigationIcon:
  │   └── IconButton (back arrow — chỉ hiện ở secondary screens)
  ├── title:
  │   └── Text (route-dependent title)
  │       ├── "home" → "Love Wallet"
  │       ├── "wallet" → "Ví chung"
  │       ├── "explore" → "Khám phá"
  │       ├── "memories" → "Kỷ niệm"
  │       ├── "profile" → "Cá nhân"
  │       ├── "profile_edit" → "Chỉnh sửa"
  │       ├── "chat" → "Chat"
  │       └── ...
  └── actions:
      ├── IconButton? (user icon — on profile route)
      └── BadgedBox (notification bell)
          ├── IconButton (bell icon)
          │   + Bell shake animation (InfiniteTransition khi có unread)
          └── Badge (unread count, "9+" format)
```

### Tại sao bell shake animation?
- **Attention grabber:** Thông báo unread → cần user chú ý
- **Subtle:** Chỉ shake 1-2 lần, không liên tục → không annoying
- **Gamification element:** Tạo cảm giác "có gì mới"

---

## 15. WALLET SCREEN

**File:** `wallet/WalletScreen.kt`

### Cấu trúc UI
```
Box
  └── Scaffold
      └── LazyColumn
          ├── item { BalanceSection(balance) }
          ├── item { GoalSection(goals, onSeeAllClick) }
          ├── item { MonthlySpendingCard(spendingList, selectedMonth) }
          ├── item { RecentActivitySection(transactions, goals) }
          └── item { Spacer(80dp) }  // space for FAB
      └── Floating Action Button (expandable)
          ├── FAB option: "Thêm chi tiêu"
          ├── FAB option: "Xem phân tích"
          ├── FAB option: "Nạp tiền"
          └── FAB option: "Rút tiền"
  └── ContributeGoalBottomSheet (conditional)
```

### Components tái sử dụng
- `BalanceSection` → component/wallet/
- `GoalSection` → component/wallet/
- `MonthlySpendingCard` → component/wallet/
- `RecentActivitySection` → component/wallet/
- `ContributeGoalBottomSheet` → home/components/

### State management
```
WalletViewModel → uiState: WalletUiState
  ├── wallet → BalanceSection
  ├── categoryBreakdown → MonthlySpendingCard
  ├── recentTransactions → RecentActivitySection
  └── allTransactions → (available for full list)

SavingGoalViewModel → uiState
  └── goals → GoalSection, ContributeGoalBottomSheet
```

### Tại sao dùng LazyColumn?
- **Performance:** Chỉ render items đang visible
- **Scroll-aware:** Balance card scroll lên → tiết kiệm không gian
- **Mixed content:** Mỗi item type khác nhau → LazyColumn handles tốt

---

## 16. TOP-UP SCREEN

**File:** `wallet/TopUpScreen.kt`

### Cấu trúc UI (2-Step Wizard)

**Step 1: AmountStep**
```
Scaffold (TopAppBar: "Nạp tiền")
  └── Column (scrollable)
      ├── Text ("Nhập số tiền", headlineSmall)
      ├── BasicTextField (amount input, large font, responsive size)
      │   └── decorationBox: Text ("0", placeholder)
      ├── Row (quick amount chips: 50K, 100K, 200K, 500K)
      │   └── OutlinedButton × 4
      ├── Text (predicted balance preview)
      └── Button ("Tiếp theo", disabled if invalid)
```

**Step 2: BankSelectStep**
```
Column
  ├── Text ("Chọn ngân hàng", headlineSmall)
  ├── LazyColumn (44 banks)
  │   └── items → BankItem
  │       └── Row (clickable)
  │           ├── BankLogo (bank logo resource)
  │           ├── Column
  │           │   ├── Text (bank name)
  │           │   └── Text (bank abbreviation)
  │           └── RadioButton (selected state)
  └── Row
      ├── OutlinedButton ("Quay lại")
      └── Button ("Tạo yêu cầu")
```

### Transition
`AnimatedContent` với slide animation giữa 2 steps.

### State management
```
TopUpViewModel → uiState: TopUpUiState
  ├── amount → BasicTextField
  ├── currentStep → AnimatedContent switch
  ├── selectedBank → BankItem selection
  └── navigationEvents: SharedFlow
      ├── NavigateToQR(topUpId) → onNavigateToQR
      └── NavigateToBankRedirect(topUpId) → onNavigateToBankRedirect
```

### Tại sao 2-step wizard?
- **Cognitive load reduction:** Mỗi bước chỉ 1 quyết định
- **Amount trước, bank sau:** Logic flow — "nạp bao nhiêu" trước, "vào đâu" sau
- **Quick amount chips:** Giảm typing cho common amounts

---

## 17. TOP-UP QR SCREEN

**File:** `wallet/TopUpQRScreen.kt`

### Cấu trúc UI
```
Scaffold (TopAppBar: "Quét mã QR")
  └── Column (centered, scrollable)
      ├── AnimatedVisibility (loading)
      │   └── LoadingTopUpState
      │       └── CircularProgressIndicator
      ├── TopUpStatusBadge (PENDING/PAID/FAILED)
      │   └── Surface (color-coded badge)
      ├── Card (QR code display)
      │   └── Column
      │       ├── Image (QR bitmap from ZXing)
      │       ├── Text ("Quét để chuyển khoản")
      │       └── Text (amount, formatted)
      ├── TransferDetailsCard
      │   └── Column
      │       ├── TransferDetailRow ("Ngân hàng", bankName)
      │       ├── TransferDetailRow ("Số TK", accountNumber)
      │       ├── TransferDetailRow ("Chủ TK", accountName)
      │       ├── TransferDetailRow ("Nội dung", transferContent)
      │       └── TransferDetailRow ("Mã chuyển tiền", transferCode)
      ├── Text (countdown timer, mỗi giây)
      └── Button ("Đã chuyển khoản xong")
```

### Special patterns
- **QR generation:** ZXing `MultiFormatWriter.encode()` → `BitMatrix` → `Bitmap`
- **Status polling:** `LaunchedEffect(topUpId)` → poll mỗi 3 giây
- **Countdown timer:** `mutableIntStateOf` + `delay(1000L)` loop

---

## 18. TOP-UP BANK REDIRECT SCREEN

**File:** `wallet/TopUpBankRedirectScreen.kt`

### Cấu trúc UI
```
Scaffold
  └── Column (centered)
      ├── Phase 0: Loading
      │   └── CircularProgressIndicator
      ├── Phase 1: Waiting
      │   └── TransferSummary
      │       ├── BankLogo
      │       ├── Text ("Đang chờ xác nhận từ ngân hàng")
      │       └── CircularProgressIndicator
      └── Phase 2: Success
          └── Column
              ├── Icon (check, scale animation)
              ├── Text ("Nạp tiền thành công!")
              └── Button ("Về trang chủ")
```

### Animation
- Phase transitions tracked by `mutableIntStateOf`
- Checkmark: `animateFloatAsState` scale 0→1

---

## 19. TRANSFER MONEY SCREEN

**File:** `wallet/TransferMoneyScreen.kt`

### Cấu trúc UI
```
Scaffold (CenterAlignedTopAppBar: "Rút tiền")
  └── Column
      ├── if (form state)
      │   ├── OutlinedTextField (amount)
      │   ├── Surface (clickable → BankSelectionSheet)
      │   │   └── Row
      │   │       ├── BankLogo
      │   │       └── Text (selected bank name)
      │   ├── OutlinedTextField (account number)
      │   ├── OutlinedTextField (note, optional)
      │   └── Button ("Tạo yêu cầu rút tiền")
      │
      ├── if (waiting bank confirmation)
      │   └── WaitingBankCard
      │       ├── CircularProgressIndicator
      │       ├── Text ("Đang chờ xác nhận")
      │       └── Text (transfer code)
      │
      └── if (success)
          └── SuccessContent
              ├── Icon (check)
              ├── Text ("Rút tiền thành công!")
              └── TransferSummaryRow × N

  └── ModalBottomSheet (BankSelectionSheet)
      └── LazyColumn (44 banks)
          └── BankItem × 44
```

### State management
```
TransferMoneyViewModel → uiState
  ├── isSubmitting → Button loading
  ├── isWaitingBankConfirmation → WaitingBankCard
  ├── isSuccess → SuccessContent
  └── errorMessage → Snackbar
```

---

## 20. QR SCANNER SCREEN

**File:** `wallet/QRScannerScreen.kt`

### Cấu trúc UI
```
Scaffold (TopAppBar: "Quét mã QR")
  └── Column
      ├── Box (camera preview)
      │   └── AndroidView (CameraX PreviewView)
      │       + ImageAnalysis (ML Kit BarcodeScanning)
      ├── Row (bottom controls)
      │   ├── TextButton ("Nhập thủ công")
      │   └── IconButton (gallery pick)
      └── Text ("Hướng camera vào mã QR")
```

### Camera Integration
```
CameraX:
  ├── Preview → PreviewView
  └── ImageAnalysis → BarcodeScanning.getClient()
      └── onSuccess → onScanned(rawValue)

ML Kit:
  └── BarcodeScanning.process(InputImage)
      └── onSuccess → get(0).rawValue
```

### Tại sao dùng CameraX + ML Kit?
- **CameraX:** Modern camera API, lifecycle-aware, dễ dùng hơn Camera2
- **ML Kit:** On-device, free, fast barcode detection
- **Gallery fallback:** `GetContent()` contract → scan QR from image

---

## 21. RECENT TRANSACTIONS SCREEN

**File:** `wallet/RecentTransactionsScreen.kt`

### Cấu trúc UI
```
Scaffold (TopAppBar: "Giao dịch gần đây")
  └── Box
      ├── if (loading) CircularProgressIndicator
      ├── if (empty) Text ("Chưa có giao dịch")
      └── LazyColumn
          └── items → TransactionItem(transaction, goals)
```

**Đơn giản** — chỉ wrapper cho `TransactionItem` component với list.

---

## 22. SAVING GOALS SCREEN

**File:** `wallet/SavingGoalsScreen.kt`

### Cấu trúc UI
```
Scaffold (TopAppBar: "Mục tiêu tiết kiệm")
  └── Box
      ├── if (loading) CircularProgressIndicator
      ├── if (empty) Text ("Chưa có mục tiêu")
      └── LazyColumn
          └── items → GoalCard(goal)
              + sorted: achieved at bottom, near-deadline first
```

---

## 23. FUTURE GOALS SCREEN

**File:** `wallet/FutureGoalsScreen.kt`

### Cấu trúc UI
```
Scaffold (TopAppBar: "Mục tiêu tương lai")
  └── Box
      ├── if (loading) CircularProgressIndicator
      ├── if (empty) Text ("Chưa có mục tiêu")
      └── LazyColumn
          └── items (filterIsInstance<FutureGoal>())
              └── GoalCard(goal, onTaskToggle)
```

---

## 24. ADD SAVING GOAL SCREEN

**File:** `home/AddSavingGoalScreen.kt`

### Cấu trúc UI
```
Scaffold (TopAppBar: "Tạo mục tiêu tiết kiệm")
  └── Column (scrollable, 16dp padding)
      ├── Text ("Tên mục tiêu")
      ├── TextField (name)
      ├── Text ("Danh mục")
      ├── Row (CategoryCircle × 5: Travel, Tech, Home, Savings, Others)
      │   └── CategoryCircle
      │       └── Surface (CircleShape, 56dp)
      │           └── Icon (category icon)
      ├── Text ("Số tiền mục tiêu")
      ├── TextField (targetAmount, number keyboard)
      ├── Text ("Hạn chót")
      ├── Surface (clickable → DatePickerDialog)
      │   └── Text (formatted date hoặc "Chọn ngày")
      └── Button ("Tạo mục tiêu", full-width)
```

### State management
```
GoalViewModel → uiState
  ├── isCreating → Button loading
  ├── createSuccess → LaunchedEffect → navigateBack
  └── error → LaunchedEffect → Toast
```

---

## 25. ADD FUTURE GOAL SCREEN

**File:** `home/AddFutureGoalScreen.kt`

### Cấu trúc UI
```
Scaffold (TopAppBar: "Tạo mục tiêu tương lai")
  └── Column (scrollable)
      ├── Text ("Tên mục tiêu")
      ├── TextField (name)
      ├── Text ("Danh mục")
      ├── Row (FutureCategoryCircle × 5)
      ├── Text ("Hạn chót")
      ├── Surface (clickable → DatePickerDialog)
      ├── Text ("Công việc cần làm")
      ├── Column (dynamic task list)
      │   └── tasks.map { task ->
      │       └── Row
      │           ├── TextField (task content)
      │           └── IconButton (remove task)
      │   }
      ├── TextButton ("+ Thêm công việc")
      └── Button ("Tạo mục tiêu")
```

### Tại sao Add Saving và Add Future là screen riêng?
- **Complexity:** Future goals có dynamic task list → cần nhiều không gian
- **Navigation:** Có thể navigate từ Home hoặc từ Goals list
- **Dedicated focus:** User chỉ làm 1 việc → focused experience

---

## 26. EXPLORE SCREEN

**File:** `explore/ExploreScreen.kt` (1815 lines — lớn nhất)

### Cấu trúc UI
```
Scaffold
  └── LazyColumn
      ├── item { ExploreSearchBar }
      │   └── OutlinedTextField (search icon, trailing clear)
      │
      ├── item { ExploreTypeChipsRow }
      │   └── LazyRow
      │       └── FilterChip × 5 (Tất cả, Quán ăn, Quán nước, Giải trí, Địa điểm)
      │
      ├── item { ExploreAdvancedFilters }
      │   └── Column
      │       ├── DropdownSelector ("Khu vực", provinces list)
      │       ├── DropdownSelectorByIndex ("Đánh giá tối thiểu", 1-5 stars)
      │       ├── Row (Near me toggle + Radius input)
      │       └── Button ("Tìm kiếm")
      │
      ├── item { Random suggestion card }
      │   └── Surface (clickable → randomPlace())
      │       └── Row
      │           ├── Icon (dice)
      │           └── Text ("Gợi ý ngẫu nhiên")
      │
      ├── item { ExploreBudgetPlannerCard }
      │   └── Card
      │       ├── Text ("Lập kế hoạch khám phá")
      │       ├── SegmentedButton (budget source: Ví / Thủ công)
      │       ├── if (wallet) Text (wallet balance)
      │       ├── if (manual) TextField (budget input)
      │       ├── TextField (số người)
      │       ├── TextField (số điểm dừng)
      │       └── Button ("Tạo kế hoạch")
      │
      ├── item { Trending section }
      │   ├── Text ("Đang thịnh hành")
      │   └── LazyRow
      │       └── TrendingPlaceCard × N
      │
      ├── items { search results }
      │   └── PlaceCard × N (clickable → PlaceDetailBottomSheet)
      │
      ├── item { loading indicator (for paging) }
      │
      └── item { Explore plan results (if generated) }
          └── Column
              └── planItems.map { ExploreBudgetPlanItemCard }
                  └── Card
                      ├── Row (stop order + experience type badge)
                      ├── AsyncImage (place image)
                      ├── Text (place name, Bold)
                      ├── Text (estimated cost)
                      ├── Text (reason)
                      └── TextButton ("Mở Google Maps")
  └── PlaceDetailBottomSheet (ModalBottomSheet)
      └── Column
          ├── AsyncImage (large image)
          ├── Text (place name, headlineMedium)
          ├── Row (rating + review count)
          ├── Text (address)
          ├── Text (open hours)
          ├── Text (price range)
          ├── Row
          │   ├── IconButton (favorite toggle)
          │   ├── IconButton (share to chat)
          │   └── IconButton (open Google Maps)
          └── Button ("Đóng")
```

### ViewModels (3!)
```
ExploreViewModel → search, filters, places, trending, explore plan, wallet balance
FavoriteViewModel → favoriteIds, toggleFavorite
HistoryViewModel → recordView
```

### Tại sao Explore screen lớn như vậy?
- **Multi-feature screen:** Search + Filter + Trending + Random + Budget Planner + Results
- **Complex interactions:** Paging, location permission, multiple filter types
- **LazyColumn với mixed items:** Mỗi item type khác nhau
- **Bottom sheet cho details:** Không cần navigate sang screen mới

---

## 27. MEMORIES SCREEN

**File:** `memories/MemoriesScreen.kt`

### Cấu trúc UI
```
Column (AppScreenBackground)
  └── Column (scrollable)
      ├── PartnerSection
      │   └── Card (clickable → expand)
      │       ├── AsyncImage (partner avatar)
      │       ├── Text (partner name)
      │       ├── Text ("X ngày bên nhau")
      │       └── AnimatedVisibility (expanded)
      │           └── Column (full partner info)
      │
      ├── RecentMomentsPreviewCard
      │   └── Card
      │       ├── Text ("Khoảnh khắc gần đây")
      │       ├── if (empty) EmptyMomentTile
      │       │   └── Surface (clickable → onOpenCapture)
      │       │       └── Icon (camera) + Text ("Chụp ảnh đầu tiên")
      │       └── else Row (moment photos, max 3)
      │           └── AsyncImage (moment image, Coil)
      │
      ├── MemoriesCalendarCard
      │   └── Card
      │       ├── MonthlyCalendarCard (year, month, notesByDay)
      │       └── if (nearestReminder)
      │           └── Surface (reminder badge)
      │               └── Text ("X ngày nữa: Valentine")
      │
      └── SpecialDaysBottomSheet (ModalBottomSheet)
          └── Column
              └── specialDays.map { day ->
                  └── Row
                      ├── Icon (calendar)
                      ├── Text (day name)
                      └── Text (days until)
              }
```

### State management
```
MemoriesViewModel → uiState
  ├── moments → RecentMomentsPreviewCard
  ├── partnerProfile → PartnerSection
  └── relationshipStartAt → days calculation
```

---

## 28. CAPTURE MOMENT SCREEN

**File:** `memories/CaptureMomentScreen.kt`

### Cấu trúc UI
```
LazyColumn
  ├── item { Camera preview }
  │   └── Box
  │       ├── AndroidView (CameraX PreviewView)
  │       └── IconButton (flip camera, top-right)
  │
  ├── item { Capture controls }
  │   └── Row
  │       ├── IconButton (gallery pick)
  │       ├── Surface (CircleShape, 72dp, capture button)
  │       │   └── IconButton (camera icon)
  │       └── IconButton (flash toggle)
  │
  ├── item { Title input }
  │   └── OutlinedTextField ("Thêm tiêu đề...")
  │
  ├── item { Post button }
  │   └── Button ("Đăng khoảnh khắc")
  │
  └── item { Moments grid (bottom sheet style) }
      └── LazyVerticalGrid (GridCells.Fixed(4))
          └── items → AsyncImage (moment thumbnail)
```

### Camera Integration
```
CameraX:
  ├── Preview → PreviewView
  └── ImageCapture → takePicture()
      → OnImageSavedCallback
      → Bitmap compression → Base64
      → apiService.createMoment(title, base64Image)
```

---

## 29. CHAT SCREEN

**File:** `chat/ChatScreen.kt` (1693 lines — phức tạp nhất)

### Cấu trúc UI
```
Column
  ├── ChatTopBar
  │   └── Surface
  │       └── Row
  │           ├── IconButton (back)
  │           ├── AvatarImage (partner avatar, 40dp)
  │           ├── Column
  │           │   ├── Text (partner name, titleMedium)
  │           │   └── AnimatedVisibility (typing)
  │           │       └── TypingIndicatorBubble
  │           │           └── Row (3 dots, infinite bounce animation)
  │           └── IconButton (info)
  │
  ├── LazyColumn (messages list, reverse layout)
  │   └── items (GroupedMessage list)
  │       ├── DateSeparator
  │       │   └── Row
  │       │       ├── HorizontalDivider
  │       │       ├── Text (date label)
  │       │       └── HorizontalDivider
  │       │
  │       └── SwipeableReplyBubble
  │           └── SwipeToDismissBox (swipe → reply)
  │               └── ChatBubble
  │                   ├── if (!mine) AvatarImage (partner)
  │                   └── Column
  │                       ├── if (replyToId) QuotedMessagePreview
  │                       │   └── Surface (quoted message preview)
  │                       ├── Surface (bubble shape)
  │                       │   ├── if (explore plan card)
  │                       │   │   └── ExplorePlanChatCard
  │                       │   │       ├── AsyncImage (place)
  │                       │   │       ├── Text (place name)
  │                       │   │       ├── PlannerInfoPill × N
  │                       │   │       └── TextButton ("Mở Maps")
  │                       │   └── else
  │                       │       └── MessageTextWithLinks
  │                       │           └── ClickableText (AnnotatedString with URL annotations)
  │                       ├── Text (timestamp, bodySmall)
  │                       └── if (mine) read status icon
  │
  │   + PendingMessageBubble (for unsent messages)
  │       └── Row
  │           ├── Text (message text, alpha=0.6f)
  │           ├── if (SENDING) CircularProgressIndicator (small)
  │           └── if (FAILED) IconButton (retry)
  │
  ├── ReplyQuoteBar (if replying)
  │   └── Surface
  │       └── Row
  │           ├── Text (quoted message preview)
  │           └── IconButton (cancel reply)
  │
  └── ChatInputBar
      └── Surface (bottom bar)
          └── Row
              ├── IconButton (emoji quick send: ❤️)
              ├── OutlinedTextField (message input, weight=1f)
              ├── IconButton (send, primary color)
              └── MiniAiSuggestionRow?
                  └── Surface ("Gửi @MiniAI để trò chuyện với AI")
```

### Message Grouping
**File:** `chat/ChatMessageGrouping.kt`

Messages được group theo:
- Cùng sender
- Trong vòng 5 phút
- Position: SINGLE / FIRST / MIDDLE / LAST
- Avatar chỉ hiện ở FIRST hoặc SINGLE
- Timestamp chỉ hiện ở LAST hoặc SINGLE

### Special patterns
- **SwipeToDismissBox:** Vuốt phải → reply
- **combinedClickable:** Long press → reaction popup
- **AnnotatedString:** URLs clickable trong tin nhắn
- **ExplorePlanChatCard:** Rich card hiển thị kế hoạch khám phá
- **@MiniAI detection:** `findMentionContext()` → trigger AI response
- **Pending messages:** Hiển thị SENDING/FAILED state với retry

### State management
```
ChatViewModel → uiState
  ├── messages → LazyColumn
  ├── pendingMessages → PendingMessageBubble
  ├── isPartnerTyping → TypingIndicatorBubble
  ├── replyingToMessage → ReplyQuoteBar
  └── isConnected → connection status indicator
```

### Tại sao Chat screen phức tạp?
- **Rich messaging:** Text + explore plan cards + image links
- **Swipe-to-reply:** Native mobile UX pattern
- **Message grouping:** Giống WhatsApp/iMessage → professional feel
- **Typing indicators:** Real-time feedback
- **AI integration:** @MiniAI trigger
- **Pending state management:** Optimistic UI + retry

---

## 30. PROFILE SCREEN

**File:** `profile/ProfileScreen.kt`

### Cấu trúc UI
```
Scaffold (TopAppBar: "Cá nhân")
  └── Column (scrollable)
      ├── ProfileHeroCard
      │   └── Card
      │       ├── Box (gradient background)
      │       │   └── Column (centered)
      │       │       ├── SquircleAvatar (avatar bitmap, 96dp)
      │       │       ├── Text (display name, headlineSmall)
      │       │       └── Text (email, bodySmall, secondary)
      │       └── TextButton ("Chỉnh sửa", bottom-right)
      │
      ├── AnniversaryCard
      │   └── Card
      │       ├── if (!paired)
      │       │   └── Button ("Mời partner kết nối")
      │       └── if (paired)
      │           ├── Row
      │           │   ├── Column
      │           │   │   ├── Text ("Partner: @username")
      │           │   │   └── Text ("X ngày bên nhau")
      │           │   └── AsyncImage (partner avatar)
      │           └── if (anniversaryTomorrow)
      │               └── Surface (gold badge)
      │                   └── Text ("Ngày kỷ niệm sắp đến!")
      │
      ├── ProfileMenuCard ("Cài đặt")
      │   └── Column
      │       ├── ListItem (Theme selector)
      │       │   └── SingleChoiceSegmentedButtonRow
      │       │       ├── SegmentedButton ("Sáng")
      │       │       ├── SegmentedButton ("Tối")
      │       │       └── SegmentedButton ("Hệ thống")
      │       ├── ListItem ("Thông báo") → onNavigate
      │       ├── ListItem ("Quyền riêng tư") → onNavigate
      │       └── ListItem ("Trợ giúp") → onNavigate
      │
      └── ProfileMenuCard ("Tài khoản")
          └── Column
              ├── SwitchInfoRow ("Hiện trạng thái hoạt động")
              ├── SwitchInfoRow ("Cho phép tìm kiếm")
              └── ListItem ("Đăng xuất", error color)
```

### ViewModels (4!)
```
ProfileViewModel → profile, coupleStatus, partnerProfile, avatar
ThemeModeViewModel → themeMode
UserSettingsViewModel → 4 setting toggles
NotificationViewModel → (for notification screen navigation)
```

### Tại sao Profile screen phức tạp?
- **Multi-section:** Hero card + Anniversary + Settings + Account
- **4 ViewModels:** Profile là aggregation point cho user-related settings
- **Theme selector inline:** Không cần navigate → tiện lợi
- **Anniversary awareness:** Romantic element → gold badge highlight

---

## 31. PROFILE EDIT SCREEN

**File:** `profile/ProfileEditScreen.kt`

### Cấu trúc UI
```
Scaffold (TopAppBar: "Chỉnh sửa profile")
  └── Column (scrollable)
      ├── EditableAvatarCard
      │   └── Card (centered)
      │       ├── Box
      │       │   ├── SquircleAvatar (current avatar)
      │       │   └── IconButton (camera overlay → image picker)
      │       └── LazyRow (avatar frames)
      │           └── EditFrameItem × 6
      │               └── Surface (CircleShape, frame color border)
      │
      ├── ProfileEditFields
      │   ├── OutlinedTextField (nickName)
      │   ├── OutlinedTextField (email)
      │   └── Surface (clickable → DatePickerDialog)
      │       └── Text (birthDate)
      │
      └── Button ("Lưu thay đổi", disabled if !isDirty)
```

### Avatar upload flow
```
rememberLauncherForActivityResult(GetContent())
  → user picks image
  → contentResolver.openInputStream(uri)
  → readBytes()
  → profileViewModel.uploadAvatar(token, bytes, contentType)
  → apiService.uploadAvatar(multipart)
  → base64 data URL returned
  → decoded to Bitmap on Dispatchers.Default
```

---

## 32. ADD EXPENSE SCREEN

**File:** `add_expense/AddExpenseScreen.kt`

### Cấu trúc UI
```
Scaffold (TopAppBar: "Thêm chi tiêu")
  └── Box
      └── Column
          ├── AmountInput
          │   └── Card
          │       └── Column
          │           ├── Text ("Số tiền", labelMedium)
          │           └── BasicTextField (large font, responsive)
          │
          ├── CategorySelector
          │   └── Row
          │       ├── CategoryCircle (top 3 categories)
          │       │   └── Surface (CircleShape, 48dp)
          │       │       └── Icon (category icon)
          │       └── TextButton ("Thêm...")
          │
          ├── ExpenseFormFields
          │   ├── OutlinedTextField (note)
          │   └── Surface (clickable → DatePickerDialog)
          │       └── Row
          │           ├── Icon (calendar)
          │           └── Text (formatted date)
          │
          ├── AnimatedVisibility (AttachReceiptSection — placeholder)
          │
          └── Button ("Lưu chi tiêu", bottom overlay)
```

### CategoryBottomSheet (opened from "Thêm...")
```
ModalBottomSheet (max height 600dp)
  └── LazyColumn
      └── groups.map { group ->
          └── Column
              ├── Text (group name: "Thiết yếu" / "Lifestyle" / "Tài chính")
              └── group.categories.map { CategoryRowItem }
                  └── Row (clickable)
                      ├── Surface (CircleShape, icon bg)
                      │   └── Icon (category icon)
                      ├── Text (category name)
                      └── if (selected) Icon (check)
```

### 14 Categories grouped
- **Thiết yếu:** Ăn uống, Di chuyển, Gia dụng, Hóa đơn, Thuê nhà, Giáo dục
- **Lifestyle:** Hẹn hò, Quà tặng, Du lịch, Thú cưng, Giải trí
- **Tài chính:** Khẩn cấp, Đầu tư, Khác

---

## 33. ANALYTICS SCREEN

**File:** `analytics/AnalyticsScreen.kt`

### Cấu trúc UI
```
Scaffold
  └── LazyColumn
      ├── item { ChartCard("Chi tiêu theo danh mục") }
      │   └── PieChart
      │       └── Canvas
      │           └── drawArc × N segments
      │               (mỗi segment: startAngle, sweepAngle, color)
      │
      └── item { ChartCard("Xu hướng theo tháng") }
          └── BarChart
              └── Canvas
                  └── drawRect × 12 bars
                      (income = tertiary, expense = primary)
```

### Custom Canvas Drawing
- **PieChart:** `drawArc` với `Fill` style, mỗi category 1 color
- **BarChart:** `drawRect` với spacing, Y-axis scaled theo max value

---

## 34. NOTIFICATION SCREEN

**File:** `notification/NotificationScreen.kt`

### Cấu trúc UI
```
Scaffold (CenterAlignedTopAppBar: "Thông báo")
  └── Box
      ├── if (loading && empty) CircularProgressIndicator
      ├── if (empty && !loading) Text ("Không có thông báo")
      └── LazyColumn
          └── items → NotificationItem
              └── Surface (clickable → markRead)
                  └── Row
                      ├── Text (emoji icon theo type)
                      │   ├── 💰 PAYMENT
                      │   ├── 💸 TRANSACTION
                      │   ├── 🎯 GOAL
                      │   ├── 💬 CHAT_MESSAGE
                      │   ├── 📸 PARTNER_MEMORY
                      │   └── 🎉 SPECIAL_DAY
                      ├── Column
                      │   ├── Text (title, Bold nếu !read)
                      │   └── Text (body, bodySmall)
                      └── Text (relative time, labelSmall)
          + snapshotFlow for infinite scroll paging
```

### Paging
- `snapshotFlow { listState.layoutInfo }` detect khi scroll gần cuối
- Gọi `loadNextPage()` khi `lastVisibleItemIndex >= totalItems - 3`

---

## 35. SETTINGS SCREEN

**File:** `settings/SettingsScreen.kt`

### Cấu trúc UI
```
Column (AppScreenBackground)
  └── Column
      ├── AppSectionHeader ("Cài đặt")
      └── AppSurfaceCard
          └── Column
              ├── SettingsMenuItem ("Chỉnh sửa profile", user icon)
              ├── SettingsMenuItem ("Quyền riêng tư", shield icon)
              ├── SettingsMenuItem ("Thông báo", bell icon)
              ├── SettingsMenuItem ("Giao diện", palette icon)
              └── SettingsMenuItem ("Trợ giúp", info icon)
```

**SettingsMenuItem:**
```
ListItem
  ├── headlineContent: Text (title)
  ├── supportingContent: Text? (subtitle)
  ├── leadingContent: Icon
  └── trailingContent: Icon (chevronRight)
```

---

## 36. SETTINGS DETAIL SCREENS

**File:** `settings/SettingsDetailScreens.kt`

### SettingsPrivacyScreen
```
Column
  ├── AppSectionHeader ("Quyền riêng tư")
  ├── AppSurfaceCard
  │   └── Column
  │       ├── SwitchRow ("Hiện trạng thái hoạt động")
  │       └── SwitchRow ("Cho phép tìm kiếm theo email")
  └── Button ("Đăng xuất", error color)
```

### SettingsNotificationsScreen
```
Column
  ├── AppSectionHeader ("Thông báo")
  └── NotificationSettingsSection
      └── Column
          ├── NotifGroupToggle ("Tin nhắn chat", chat icon)
          ├── NotifGroupToggle ("Thanh toán", payment icon)
          ├── NotifGroupToggle ("Giao dịch", transaction icon)
          ├── NotifGroupToggle ("Mục tiêu", goal icon)
          └── NotifGroupToggle ("Kỷ niệm", memory icon)
```

### SettingsAppearanceScreen
```
Column
  ├── AppSectionHeader ("Giao diện")
  └── AppSurfaceCard
      └── SingleChoiceSegmentedButtonRow
          ├── SegmentedButton ("Sáng", sun icon)
          ├── SegmentedButton ("Tối", moon icon)
          └── SegmentedButton ("Hệ thống", system icon)
```

### SettingsHelpScreen
```
Column
  ├── AppSectionHeader ("Trợ giúp")
  └── AppSurfaceCard
      └── Column
          ├── ListItem ("Phiên bản", trailing: version number)
          ├── ListItem ("Điều khoản sử dụng") → open URL
          └── ListItem ("Chính sách bảo mật") → open URL
```

---

## 37. MAP SHARE SCREEN

**File:** `map/MapShareActivity.kt`

### Cấu trúc UI
```
Scaffold (TopAppBar: "Chia sẻ vị trí")
  └── AndroidView (osmdroid MapView)
      └── MapView
          ├── Marker (my location)
          └── Marker (partner location, nếu có)
```

### Integration
```
osmdroid:
  ├── MapView (AndroidView composable)
  ├── Marker (GeoPoint)
  ├── MapController.setCenter()
  └── Configuration.getInstance().userAgentValue

Location updates:
  ├── MapShareForegroundService → Broadcast location
  ├── BroadcastReceiver → receive updates
  └── MapWebSocketHandler → send to partner via WebSocket
```

### Tại sao dùng osmdroid?
- **Free:** Không cần API key như Google Maps
- **OpenStreetMap:** Community-driven, miễn phí
- **Offline capable:** Có thể cache tiles
- **Tùy biến cao:** Full control over markers, overlays

---

## TÓM TẮT PATTERNS TOÀN DIỆN

### ViewModel → Screen binding pattern
```kotlin
// Universal pattern across all screens:
val uiState by viewModel.uiState.collectAsState()

// Side effects:
LaunchedEffect(uiState.someValue) {
    if (uiState.someValue != null) {
        // navigate, show toast, etc.
    }
}
```

### Component reuse map
```
AuthBackdrop + AuthFormSurface ← Welcome, Login, Register, PersonalInfo
GoalCard ← Home, SavingGoals, FutureGoals, Wallet
ContributeGoalBottomSheet ← Home, Wallet
TransactionItem ← RecentTransactions, Wallet
BankLogo ← TopUp, TopUpQR, TransferMoney
AppScreenBackground ← Explore, Memories, Settings, CaptureMoment
AppSurfaceCard + AppSectionHeader ← Settings, SettingsDetail
PlaceCard ← Explore, Favorites, History
```

### Navigation pattern
- **Pre-auth screens:** Activity-based (Welcome → Login → Register → PersonalInfo → CoupleConnect)
- **Post-auth screens:** NavHost-based (HomeScaffold hosts 30 routes)
- **Standalone activities:** MapShare, Settings (legacy)

### Error handling pattern
```kotlin
// Pattern 1: Resource sealed class (most common)
when (resource) {
    is Resource.Loading -> show spinner
    is Resource.Success -> show data
    is Resource.Error -> show error message
}

// Pattern 2: runCatching (auth, couple, profile)
runCatching { repository.call() }
    .onSuccess { _uiState.update { it.copy(data = result) } }
    .onFailure { _uiState.update { it.copy(error = it.message) } }

// Pattern 3: Result type (favorite, history)
repository.call()
    .onSuccess { ... }
    .onFailure { ... }
```

### State management pattern
```kotlin
// Every ViewModel follows:
private val _uiState = MutableStateFlow(ScreenUiState())
val uiState: StateFlow<ScreenUiState> = _uiState.asStateFlow()

// Update via:
_uiState.update { it.copy(field = newValue) }
```
