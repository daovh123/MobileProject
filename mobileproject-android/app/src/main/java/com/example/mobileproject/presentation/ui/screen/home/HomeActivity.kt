package com.example.mobileproject.presentation.ui.screen.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobileproject.R
import com.example.mobileproject.presentation.viewmodel.ProductViewModel
import com.example.mobileproject.presentation.ui.screen.explore.ExploreFragment
import com.example.mobileproject.presentation.ui.screen.memories.MemoriesFragment
import com.example.mobileproject.presentation.ui.screen.wallet.WalletFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.addCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var topAppBar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView

    private var currentMenuItemId: Int = R.id.nav_home

    private val navItems: Map<Int, NavItem> = mapOf(
        R.id.nav_home to NavItem(
            fragmentTag = "home",
            titleRes = R.string.page_home,
            fragmentProvider = { HomeFragment() },
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
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ViewModelProvider(this)[ProductViewModel::class.java]

        topAppBar = findViewById(R.id.topAppBar)
        bottomNav = findViewById(R.id.bottomNav)

        setSupportActionBar(topAppBar)

        topAppBar.setNavigationOnClickListener {
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

    private companion object {
        const val KEY_SELECTED_NAV_ITEM_ID: String = "selected_nav_item_id"
    }
}
