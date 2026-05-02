package com.example.mobileproject.presentation.ui.navigation

/**
 * Quick reference for the Navigation system
 *
 * CURRENT STRUCTURE:
 * - AppNavigationBar.kt   — Reusable bottom navigation bar component
 * - NavigationConfig.kt   — Centralized item definitions (route, icon, label, a11y)
 * - HomeScaffold.kt       — Scaffold wrapper; hosts NavHost and calls AppNavigationBar
 *                           Route constants live in the HomeRoutes object inside this file
 */

/**
 * USAGE EXAMPLE 1: In HomeScaffold
 * 
 * bottomBar = {
 *     if (!isChatRoute && !isProfileRoute) {
 *         AppNavigationBar(
 *             currentRoute = currentRoute,
 *             onNavigate = { route ->
 *                 navController.navigate(route) {
 *                     popUpTo(navController.graph.findStartDestination().id) {
 *                         saveState = true
 *                     }
 *                     launchSingleTop = true
 *                     restoreState = true
 *                 }
 *             },
 *         )
 *     }
 * }
 */

/**
 * USAGE EXAMPLE 2: Add a new navigation item
 * 
 * Location: NavigationConfig.kt
 * 
 * object NavigationConfig {
 *     val navigationItems = listOf(
 *         NavigationItem(
 *             route = "home",
 *             labelRes = R.string.page_home,
 *             icon = LucideHome,
 *             contentDescriptionRes = R.string.cd_home,
 *         ),
 *         // ... other items ...
 *         NavigationItem(
 *             route = "newFeature",         // ← Add new item here
 *             labelRes = R.string.feature,
 *             icon = LucideNewIcon,
 *             contentDescriptionRes = R.string.cd_new_feature,
 *         ),
 *     )
 * }
 * 
 * That's it! It will automatically:
 * - Show in navigation bar
 * - Work with navController
 * - Display correct icon & label
 * - Have proper styling
 */

/**
 * STYLE DETAILS - Material Design 3
 * 
 * SELECTED STATE:
 * - Background: primaryContainer (90% alpha)
 * - Icon: primary color, 20dp
 * - Label: Displayed with labelMedium style
 * - Shape: extraLarge rounded corners
 * 
 * UNSELECTED STATE:
 * - Icon: onSurfaceVariant color, 24dp
 * - No background
 * - Smooth color animation when selected
 * 
 * CONTAINER:
 * - Background: surface (98% alpha)
 * - Elevation: 8dp
 * - Border: outline color with 12% alpha
 * - Padding: 12dp horizontal, 12dp vertical
 * - Rounded: extraLarge corners
 */

/**
 * ANIMATION & PERFORMANCE
 * 
 * The component uses:
 * - animateColorAsState for smooth transitions
 * - Proper recomposition handling
 * - No expensive operations in hot path
 * 
 * Animation details:
 * - Label "NavigationItemIconColor" for debugging
 * - Default animation spec (smooth, ~300ms)
 * - Better UX than instant color changes
 */

/**
 * ACCESSIBILITY
 * 
 * Every icon has:
 * - contentDescription from string resource
 * - Proper tint colors for contrast
 * - Semantic meaning (navigation item)
 * 
 * Screen readers will announce:
 * "Home, button, not selected" (for unselected)
 * "Home, button, selected" (for selected)
 */

/**
 * FILE ORGANIZATION
 * 
 * com/example/mobileproject/presentation/ui/
 * ├── navigation/
 * │   ├── NavigationConfig.kt
 * │   └── AppNavigationBar.kt
 * │
 * └── screen/app/compose/
 *     └── HomeScaffold.kt
 */
