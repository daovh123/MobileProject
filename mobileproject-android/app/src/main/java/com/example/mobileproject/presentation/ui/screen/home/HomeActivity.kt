package com.example.mobileproject.presentation.ui.screen.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobileproject.R
import com.example.mobileproject.presentation.viewmodel.ProductViewModel
import com.example.mobileproject.presentation.ui.screen.explore.ExploreFragment
import com.example.mobileproject.presentation.ui.screen.memories.MemoriesFragment
import com.example.mobileproject.presentation.ui.screen.settings.SettingsFragment
import com.example.mobileproject.presentation.ui.screen.wallet.WalletFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.addCallback
import dagger.hilt.android.AndroidEntryPoint
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    private lateinit var topAppBar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navItems: Map<Int, NavItem>

    private var currentMenuItemId: Int = R.id.nav_home
    private var accessToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty()
        if (accessToken.isBlank()) {
            accessToken = authSessionStore.load()?.token.orEmpty()
        }
        navItems = createNavItems(accessToken)

        ViewModelProvider(this)[ProductViewModel::class.java]

        topAppBar = findViewById(R.id.topAppBar)
        bottomNav = findViewById(R.id.bottomNav)

        topAppBar.menu.clear()
        topAppBar.inflateMenu(R.menu.menu_top_app_bar)

        topAppBar.setNavigationOnClickListener {
            bottomNav.selectedItemId = R.id.nav_settings
        }

        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notifications -> true
                else -> false
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            switchTo(item.itemId)
            true
        }

        onBackPressedDispatcher.addCallback(this) {
            if (currentMenuItemId != R.id.nav_home) {
                bottomNav.selectedItemId = R.id.nav_home
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

        val startMenuItemId = savedInstanceState?.getInt(KEY_SELECTED_NAV_ITEM_ID) ?: R.id.nav_home
        bottomNav.selectedItemId = startMenuItemId
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_SELECTED_NAV_ITEM_ID, currentMenuItemId)
        super.onSaveInstanceState(outState)
    }

    private fun switchTo(menuItemId: Int) {
        val navItem = navItems[menuItemId] ?: return

        topAppBar.title = getString(navItem.titleRes)
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        navItems.values.forEach { item ->
            fragmentManager.findFragmentByTag(item.fragmentTag)?.let(transaction::hide)
        }

        val fragment = fragmentManager.findFragmentByTag(navItem.fragmentTag)
            ?: navItem.fragmentProvider().also {
                transaction.add(R.id.fragmentContainer, it, navItem.fragmentTag)
            }
        transaction.show(fragment)
        transaction.commit()

        currentMenuItemId = menuItemId
    }

    private data class NavItem(
        val fragmentTag: String,
        val titleRes: Int,
        val fragmentProvider: () -> Fragment,
    )

    private fun createNavItems(accessToken: String): Map<Int, NavItem> = mapOf(
        R.id.nav_home to NavItem(
            fragmentTag = "home",
            titleRes = R.string.page_home,
            fragmentProvider = { HomeFragment.newInstance(accessToken) },
        ),
        R.id.nav_wallet to NavItem(
            fragmentTag = "wallet",
            titleRes = R.string.page_wallet,
            fragmentProvider = { WalletFragment() },
        ),
        R.id.nav_explore to NavItem(
            fragmentTag = "explore",
            titleRes = R.string.page_explore,
            fragmentProvider = { ExploreFragment() },
        ),
        R.id.nav_memories to NavItem(
            fragmentTag = "memories",
            titleRes = R.string.page_memories,
            fragmentProvider = { MemoriesFragment() },
        ),
        R.id.nav_settings to NavItem(
            fragmentTag = "settings",
            titleRes = R.string.settings_title,
            fragmentProvider = { SettingsFragment.newInstance(accessToken) },
        ),
    )

    companion object {
        const val EXTRA_ACCESS_TOKEN: String = "extra_access_token"
        const val KEY_SELECTED_NAV_ITEM_ID: String = "selected_nav_item_id"
    }
}
