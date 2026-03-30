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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var topAppBar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView

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
            when (item.itemId) {
                R.id.nav_home -> {
                    showFragment(
                        fragmentTag = "home",
                        titleRes = R.string.page_home,
                        fragmentProvider = { HomeFragment() },
                    )
                    true
                }

                R.id.nav_wallet -> {
                    showFragment(
                        fragmentTag = "wallet",
                        titleRes = R.string.page_wallet,
                        fragmentProvider = { WalletFragment() },
                    )
                    true
                }

                R.id.nav_explore -> {
                    showFragment(
                        fragmentTag = "explore",
                        titleRes = R.string.page_explore,
                        fragmentProvider = { ExploreFragment() },
                    )
                    true
                }

                R.id.nav_memories -> {
                    showFragment(
                        fragmentTag = "memories",
                        titleRes = R.string.page_memories,
                        fragmentProvider = { MemoriesFragment() },
                    )
                    true
                }

                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun showFragment(
        fragmentTag: String,
        titleRes: Int,
        fragmentProvider: () -> Fragment,
    ) {
        topAppBar.title = getString(titleRes)

        val fragment = supportFragmentManager.findFragmentByTag(fragmentTag) ?: fragmentProvider()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment, fragmentTag)
            .commit()
    }
}
