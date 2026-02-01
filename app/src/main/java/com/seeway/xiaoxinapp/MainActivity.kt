package com.seeway.xiaoxinapp

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.seeway.xiaoxinapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Nav items
    private lateinit var navHome: LinearLayout
    private lateinit var navMusic: LinearLayout
    private lateinit var navNotifications: LinearLayout
    private lateinit var navSettings: LinearLayout

    // Nav icons
    private lateinit var navHomeIcon: ImageView
    private lateinit var navMusicIcon: ImageView
    private lateinit var navNotificationsIcon: ImageView
    private lateinit var navSettingsIcon: ImageView

    // Nav labels
    private lateinit var navHomeLabel: TextView
    private lateinit var navMusicLabel: TextView
    private lateinit var navNotificationsLabel: TextView
    private lateinit var navSettingsLabel: TextView

    private val navItemDrawables = mapOf(
        R.id.nav_home to Pair(R.drawable.ic_sidebar_home, R.drawable.ic_sidebar_home_selected),
        R.id.nav_music to Pair(R.drawable.ic_sidebar_music, R.drawable.ic_sidebar_music_selected),
        R.id.nav_notifications to Pair(R.drawable.ic_sidebar_bell, R.drawable.ic_sidebar_bell_selected),
        R.id.nav_settings to Pair(R.drawable.ic_sidebar_settings, R.drawable.ic_sidebar_settings_selected)
    )

    private var selectedNavId: Int = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge before setContentView
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide status bar and navigation bar (immersive mode)
        hideSystemBars()

        // Handle insets for edge-to-edge
        setupEdgeToEdge()

        setupNavigation()
    }

    private fun setupEdgeToEdge() {
        // Consume insets without applying padding - content extends to edges
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            insets
        }
    }

    private fun hideSystemBars() {
        // Hide system bars using modern API
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun setupNavigation() {
        // Initialize nav items
        navHome = binding.navHome
        navMusic = binding.navMusic
        navNotifications = binding.navNotifications
        navSettings = binding.navSettings

        // Initialize icons
        navHomeIcon = binding.navHomeIcon
        navMusicIcon = binding.navMusicIcon
        navNotificationsIcon = binding.navNotificationsIcon
        navSettingsIcon = binding.navSettingsIcon

        // Initialize labels
        navHomeLabel = binding.navHomeLabel
        navMusicLabel = binding.navMusicLabel
        navNotificationsLabel = binding.navNotificationsLabel
        navSettingsLabel = binding.navSettingsLabel

        // Set click listeners
        navHome.setOnClickListener {
            navigateTo(R.id.navigation_home)
            selectedNavId = R.id.nav_home
        }
        navMusic.setOnClickListener {
            navigateTo(R.id.navigation_explore)
            selectedNavId = R.id.nav_music
        }
        navNotifications.setOnClickListener {
            navigateTo(R.id.navigation_tools)
            selectedNavId = R.id.nav_notifications
        }
        navSettings.setOnClickListener {
            navigateTo(R.id.navigation_profile)
            selectedNavId = R.id.nav_settings
        }

        // Wait for the NavHostFragment to be created before accessing NavController
        binding.root.post {
            try {
                val navController = findNavController(R.id.nav_host_fragment)
                val appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.navigation_home,
                        R.id.navigation_explore,
                        R.id.navigation_tools,
                        R.id.navigation_profile
                    )
                )

                // Update selected nav item on destination change
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    updateSelectedNavItem(destination.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateTo(destinationId: Int) {
        try {
            val navController = findNavController(R.id.nav_host_fragment)

            // Map nav item IDs to fragment IDs
            val fragmentId = when (selectedNavId) {
                R.id.nav_home -> R.id.navigation_home
                R.id.nav_music -> R.id.navigation_explore
                R.id.nav_notifications -> R.id.navigation_tools
                R.id.nav_settings -> R.id.navigation_profile
                else -> destinationId
            }

            navController.navigate(fragmentId)
            updateSelectedNavItem(fragmentId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateSelectedNavItem(navItemId: Int) {
        selectedNavId = when (navItemId) {
            R.id.navigation_home -> R.id.nav_home
            R.id.navigation_explore -> R.id.nav_music
            R.id.navigation_tools -> R.id.nav_notifications
            R.id.navigation_profile -> R.id.nav_settings
            else -> R.id.nav_home
        }

        // Reset all nav items
        resetNavItem(navHome, navHomeIcon, navHomeLabel, R.id.nav_home)
        resetNavItem(navMusic, navMusicIcon, navMusicLabel, R.id.nav_music)
        resetNavItem(navNotifications, navNotificationsIcon, navNotificationsLabel, R.id.nav_notifications)
        resetNavItem(navSettings, navSettingsIcon, navSettingsLabel, R.id.nav_settings)

        // Set selected nav item
        val selectedItem = when (navItemId) {
            R.id.navigation_home -> navHome
            R.id.navigation_explore -> navMusic
            R.id.navigation_tools -> navNotifications
            R.id.navigation_profile -> navSettings
            else -> navHome
        }

        val navId = selectedNavId

        selectedItem.background = ContextCompat.getDrawable(this, R.drawable.bg_sidebar_item_selected)

        val (icon, label) = when (navId) {
            R.id.nav_home -> Pair(navHomeIcon, navHomeLabel)
            R.id.nav_music -> Pair(navMusicIcon, navMusicLabel)
            R.id.nav_notifications -> Pair(navNotificationsIcon, navNotificationsLabel)
            R.id.nav_settings -> Pair(navSettingsIcon, navSettingsLabel)
            else -> Pair(navHomeIcon, navHomeLabel)
        }

        val selectedDrawable = navItemDrawables[navId]?.second ?: R.drawable.ic_sidebar_home_selected
        icon.setImageResource(selectedDrawable)
        label.setTextColor(ContextCompat.getColor(this, R.color.blue_600))
    }

    private fun resetNavItem(
        layout: LinearLayout,
        icon: ImageView,
        label: TextView,
        navId: Int
    ) {
        layout.background = null
        val defaultDrawable = navItemDrawables[navId]?.first ?: R.drawable.ic_sidebar_home
        icon.setImageResource(defaultDrawable)
        label.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}
