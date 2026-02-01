package com.seeway.xiaoxinapp.ui.home

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.seeway.xiaoxinapp.R
import com.seeway.xiaoxinapp.adapter.POIAdapter
import com.seeway.xiaoxinapp.databinding.FragmentHomeBinding
import com.seeway.xiaoxinapp.model.POI
import com.seeway.xiaoxinapp.model.Route
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var aMap: AMap? = null
    private lateinit var poiAdapter: POIAdapter
    private val poiList = mutableListOf<POI>()

    // UI State
    private var isSearchMode = false
    private var isNavigating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            binding.mapView.onCreate(savedInstanceState)
            initMap()
            setupSearch()
            setupMapControls()
            setupAIAssistant()
            setupPOIList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initMap() {
        try {
            aMap = binding.mapView.map

            // Set map type
            aMap?.mapType = AMap.MAP_TYPE_NORMAL

            // Configure location style
            val myLocationStyle = MyLocationStyle()
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
            myLocationStyle.interval(2000)
            aMap?.myLocationStyle = myLocationStyle

            aMap?.isMyLocationEnabled = true

            // Configure UI settings
            aMap?.uiSettings?.isMyLocationButtonEnabled = false
            aMap?.uiSettings?.isZoomControlsEnabled = false
            aMap?.uiSettings?.isCompassEnabled = true

            // Disable indoor map
            aMap?.showIndoorMap(false)

            // Set initial camera position to Beijing
            val beijing = LatLng(39.9042, 116.4074)
            aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(beijing, 12f))

            // Set up map click listener
            aMap?.setOnMapClickListener {
                // Hide search results when clicking on map
                hideSearchResults()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupSearch() {
        val searchInput = binding.searchInput

        // Configure search input
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchInput.text.toString())
                true
            } else {
                false
            }
        }

        // Search button
        binding.btnSearch.setOnClickListener {
            performSearch(searchInput.text.toString())
        }
    }

    private fun setupMapControls() {
        // Zoom in
        binding.btnZoomIn.setOnClickListener {
            aMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }

        // Zoom out
        binding.btnZoomOut.setOnClickListener {
            aMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }

        // Layers (placeholder for now)
        binding.btnLayers.setOnClickListener {
            // TODO: Show layer selection dialog
        }

        // Locate to current position
        binding.btnLocate.setOnClickListener {
            aMap?.myLocation?.let { location ->
                val myLatLng = LatLng(location.latitude, location.longitude)
                aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16f))
            }
        }
    }

    private fun setupAIAssistant() {
        binding.voiceAssistantBtn.setOnClickListener {
            // TODO: Show AI assistant overlay
            startVoiceSearch()
        }
    }

    private fun setupPOIList() {
        poiAdapter = POIAdapter(
            onItemClick = { poi ->
                onPOIClicked(poi)
            },
            onRouteClick = { poi ->
                onRouteClicked(poi)
            }
        )

        binding.poiResultsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = poiAdapter
        }

        // Close button
        binding.btnCloseResults.setOnClickListener {
            hideSearchResults()
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return

        // Show search results panel
        binding.poiResultsPanel.visibility = View.VISIBLE

        // TODO: Replace with actual AMap POI search
        // For now, use mock data
        val mockPOIs = POI.createMockPOIs()
        poiList.clear()
        poiList.addAll(mockPOIs)
        poiAdapter.submitList(mockPOIs)

        // Update title
        binding.poiResultsTitle.text = "\"$query\" 的搜索结果"

        // Hide keyboard
        binding.searchInput.clearFocus()
    }

    private fun startVoiceSearch() {
        // TODO: Integrate voice recognition
        // For now, just show a placeholder
        binding.searchInput.hint = "正在听..."
        binding.searchInput.postDelayed({
            binding.searchInput.hint = getString(R.string.search_hint)
        }, 2000)
    }

    private fun onPOIClicked(poi: POI) {
        // Move camera to POI location
        val latLng = LatLng(poi.latitude, poi.longitude)
        aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

        // Hide search results
        hideSearchResults()

        // TODO: Add marker and show POI info
    }

    private fun onRouteClicked(poi: POI) {
        // Hide search results
        hideSearchResults()

        // TODO: Show route planning bottom sheet
        // For now, just show a toast or log
        showRouteSelection(poi)
    }

    private fun showRouteSelection(poi: POI) {
        // TODO: Implement route selection bottom sheet
        // For now, just move camera to show the route
        val mockRoutes = Route.createMockRoutes()
        if (mockRoutes.isNotEmpty()) {
            binding.poiResultsTitle.text = "到 ${poi.name} 的路线"
            binding.poiResultsPanel.visibility = View.VISIBLE
        }
    }

    private fun hideSearchResults() {
        binding.poiResultsPanel.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        try {
            binding.mapView.onResume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            binding.mapView.onPause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            binding.mapView.onDestroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            binding.mapView.onSaveInstanceState(outState)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAMap(): AMap? = aMap
}
