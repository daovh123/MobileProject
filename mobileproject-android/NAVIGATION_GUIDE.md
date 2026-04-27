# Navigation System - Material Design 3

## Tổng quan

Hệ thống navigation mới được xây dựng tuân theo tiêu chuẩn **Material Design 3** với các đặc điểm chính:

✅ **Consistency**: Tất cả navigation items được quản lý tập trung từ `NavigationConfig`
✅ **Reusable**: Component `AppNavigationBar` có thể được sử dụng ở bất kỳ đâu
✅ **Animated**: Smooth color transitions khi chuyển giữa tabs
✅ **Accessible**: Đầy đủ content descriptions cho accessibility

---

## Cấu trúc

### 1. `NavigationConfig.kt`
**Tập trung quản lý tất cả navigation items**

```kotlin
data class NavigationItem(
    val route: String,              // "home", "explore", etc.
    val labelRes: Int,              // R.string.page_home
    val icon: ImageVector,          // LucideHome, LucideCompass, ...
    val contentDescriptionRes: Int, // R.string.cd_home
)

object NavigationConfig {
    val navigationItems = listOf(...)
    fun getItemByRoute(route: String): NavigationItem?
}
```

**Lợi ích:**
- Dễ dàng thêm/xóa/sửa items
- Tất cả icon, label, route đều được định nghĩa ở một nơi
- Tránh duplicate code

### 2. `AppNavigationBar.kt`
**Component Material Design 3 reusable**

```kotlin
@Composable
fun AppNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavigationItem> = NavigationConfig.navigationItems,
)
```

**Features:**
- ✨ Animated color transitions (animateColorAsState)
- 📏 Rounded rectangular background (Material Design 3)
- 📱 Proper padding & elevation
- 🎨 Uses app's color scheme

### 3. `HomeScaffold.kt` (Updated)
**Sử dụng AppNavigationBar component**

```kotlin
bottomBar = {
    if (!isChatRoute && !isProfileRoute) {
        AppNavigationBar(
            currentRoute = currentRoute,
            onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )
    }
}
```

---

## Sử dụng

### Cách 1: Thêm mục mới vào navigation

**File: `NavigationConfig.kt`**

```kotlin
object NavigationConfig {
    val navigationItems = listOf(
        // ... existing items ...
        NavigationItem(
            route = "newScreen",
            labelRes = R.string.new_screen,
            icon = LucideNewIcon,
            contentDescriptionRes = R.string.cd_new_screen,
        ),
    )
}
```

**Tự động sync:**
- ✅ Hiện trong navigation bar
- ✅ Route được define centralized
- ✅ Icon & label được sync

### Cách 2: Sử dụng AppNavigationBar trong custom screen

```kotlin
@Composable
fun MyCustomScreen() {
    val navController = rememberNavController()
    val currentRoute = ... // Get từ navController
    
    Column {
        // Your content
        
        AppNavigationBar(
            currentRoute = currentRoute,
            onNavigate = { route ->
                navController.navigate(route)
            },
        )
    }
}
```

---

## Visual Design

### Selected State (Active Tab)
- **Background**: `primaryContainer` với alpha 0.9f
- **Icon Color**: `primary`
- **Icon Size**: 20dp
- **Text**: Hiển thị label với `labelMedium` style
- **Shape**: `extraLarge` rounded corners

### Unselected State
- **Icon Color**: `onSurfaceVariant`
- **Icon Size**: 24dp
- **No background**
- **Smooth animation** khi chuyển state

### Container
- **Background**: `surface` (alpha 0.98f)
- **Elevation**: 8dp (Material Design 3)
- **Border**: 1dp `outline` (alpha 0.12f)
- **Padding**: 12dp horizontal, 12dp vertical
- **Shape**: `extraLarge` rounded corners

---

## Material Design 3 Compliance

✅ **Navigation Bar**: Follows MD3 specs
✅ **Color System**: Using Material color scheme
✅ **Elevation**: Proper shadow elevation (8dp)
✅ **Animation**: Smooth transitions between states
✅ **Typography**: Using Material typography scales
✅ **Shapes**: Using Material shape scales (extraLarge)
✅ **Accessibility**: Complete content descriptions

---

## Mẹo & Best Practices

### 1. **Luôn sử dụng NavigationConfig**
```kotlin
// ✅ Good
val item = NavigationConfig.getItemByRoute(currentRoute)

// ❌ Tránh
val item = items.firstOrNull { it.route == route }
```

### 2. **Giữ route string sync với object**
```kotlin
// ✅ Good - centralized
object HomeRoutes {
    const val HOME: String = "home"
}
NavigationItem(route = "home", ...)

// ❌ Tránh - duplicate magic strings
navController.navigate("home")
```

### 3. **Luôn thêm content descriptions**
```kotlin
// ✅ Good
Icon(
    imageVector = item.icon,
    contentDescription = stringResource(item.contentDescriptionRes),
)

// ❌ Tránh
Icon(
    imageVector = item.icon,
    contentDescription = "Home", // Hard-coded string
)
```

### 4. **Sử dụng animations**
```kotlin
// ✅ The component handles this automatically
// Color transitions được animate smooth

// ❌ Tránh
// Không bỏ animations để save performance
```

---

## Adding New Icons

Nếu cần icon mới, thêm vào `LucideIcons.kt`:

```kotlin
// File: com/example/mobileproject/presentation/ui/icons/LucideIcons.kt
val LucideNewIcon: ImageVector
    get() = ...
```

Sau đó sử dụng trong `NavigationConfig`:

```kotlin
NavigationItem(
    route = "newFeature",
    labelRes = R.string.new_feature,
    icon = LucideNewIcon,  // ← Use the new icon
    contentDescriptionRes = R.string.cd_new_feature,
)
```

---

## Troubleshooting

### Navigation bar không update
- Check: `currentRoute` được pass đúng không?
- Check: Route trong `NavigationItem` khớp với actual route không?

### Icon không hiển thị
- Check: Icon vector được define trong `LucideIcons.kt` không?
- Check: Import đúng chưa?

### Animation không smooth
- Check: `animateColorAsState` có được call đúng không?
- Check: Composition restarting không liên tục?

---

## File Structure

```
presentation/ui/
├── navigation/
│   ├── NavigationConfig.kt      ← Central configuration
│   └── AppNavigationBar.kt      ← Reusable component
└── screen/app/compose/
    └── HomeScaffold.kt          ← Uses AppNavigationBar
```

---

## Summary

**Trước (Old System):**
- ❌ Navigation bar hardcoded trong HomeScaffold
- ❌ Items defined locally, khó sync
- ❌ Duplicate icon/label definitions

**Sau (New System):**
- ✅ Centralized `NavigationConfig`
- ✅ Reusable `AppNavigationBar` component
- ✅ Easy to add/modify items
- ✅ Material Design 3 compliant
- ✅ Smooth animations & transitions
- ✅ Better accessibility support

---

**Made with Material Design 3 ✨**
