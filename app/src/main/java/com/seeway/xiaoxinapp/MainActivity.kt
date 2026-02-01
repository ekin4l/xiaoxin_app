package com.seeway.xiaoxinapp

import android.Manifest
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.seeway.xiaoxinapp.databinding.ActivityMainBinding
import com.amap.api.maps.MapsInitializer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Location permissions
    private val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val REQUEST_LOCATION_PERMISSION = 1001

    // Nav buttons
    private lateinit var navHome: ImageButton
    private lateinit var navMusic: ImageButton
    private lateinit var navNotifications: ImageButton
    private lateinit var navSettings: ImageButton

    private val navItemDrawables = mapOf(
        R.id.nav_home to Pair(R.drawable.ic_nav_home_normal, R.drawable.ic_nav_home),
        R.id.nav_music to Pair(R.drawable.ic_nav_music, R.drawable.ic_nav_music),
        R.id.nav_notifications to Pair(R.drawable.ic_nav_bell, R.drawable.ic_nav_bell),
        R.id.nav_settings to Pair(R.drawable.ic_nav_settings, R.drawable.ic_nav_settings)
    )

    private var selectedNavId: Int = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up AMap privacy agreement (required for AMap SDK)
        setupAMapPrivacy()

        // Enable edge-to-edge before setContentView
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide status bar and navigation bar (immersive mode)
        hideSystemBars()

        // Handle insets for edge-to-edge
        setupEdgeToEdge()

        // Request location permissions
        requestLocationPermissions()

        setupNavigation()
    }

    private fun setupAMapPrivacy() {
        // Set AMap privacy agreement (required for SDK to work)
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)

        // Initialize AMap with context
        MapsInitializer.initialize(this)
    }

    private fun requestLocationPermissions() {
        val missingPermissions = LOCATION_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), REQUEST_LOCATION_PERMISSION)
        } else {
            // Permissions already granted
            Toast.makeText(this, "定位权限已授予", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            val allGranted = grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Toast.makeText(this, "定位权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "定位权限被拒绝，无法使用定位功能", Toast.LENGTH_LONG).show()
            }
        }
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
        // Initialize nav buttons
        navHome = binding.navHome
        navMusic = binding.navMusic
        navNotifications = binding.navNotifications
        navSettings = binding.navSettings

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

        // Reset all nav items to normal state
        resetNavItem(navHome, R.id.nav_home)
        resetNavItem(navMusic, R.id.nav_music)
        resetNavItem(navNotifications, R.id.nav_notifications)
        resetNavItem(navSettings, R.id.nav_settings)

        // Set selected nav item
        val selectedItem = when (navItemId) {
            R.id.navigation_home -> navHome
            R.id.navigation_explore -> navMusic
            R.id.navigation_tools -> navNotifications
            R.id.navigation_profile -> navSettings
            else -> navHome
        }

        val navId = selectedNavId

        // Set selected background (blue with rounded corners)
        selectedItem.background = ContextCompat.getDrawable(this, R.drawable.bg_nav_item_selected)

        // Set selected icon (white for home)
        val selectedDrawable = navItemDrawables[navId]?.second ?: R.drawable.ic_nav_home
        selectedItem.setImageResource(selectedDrawable)
    }

    private fun resetNavItem(button: ImageButton, navId: Int) {
        // Set normal background (transparent with hover effect)
        button.background = ContextCompat.getDrawable(this, R.drawable.bg_nav_item_normal)
        // Set normal icon (gray)
        val defaultDrawable = navItemDrawables[navId]?.first ?: R.drawable.ic_nav_home_normal
        button.setImageResource(defaultDrawable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}
