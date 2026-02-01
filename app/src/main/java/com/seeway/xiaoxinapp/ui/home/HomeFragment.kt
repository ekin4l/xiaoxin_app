package com.seeway.xiaoxinapp.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
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
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import android.widget.Toast
import android.content.Intent
import com.seeway.xiaoxinapp.R
import com.seeway.xiaoxinapp.adapter.POIAdapter
import com.seeway.xiaoxinapp.databinding.FragmentHomeBinding
import com.seeway.xiaoxinapp.model.POI
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment(), PoiSearch.OnPoiSearchListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var aMap: AMap? = null
    private var poiSearch: PoiSearch? = null
    private lateinit var poiAdapter: POIAdapter
    private val poiList = mutableListOf<POI>()
    private var currentSearchQuery = ""

    // Markers on map
    private val poiMarkers = mutableListOf<Marker>()

    // Location client
    private var locationClient: AMapLocationClient? = null
    private val locationListener = AMapLocationListener { location ->
        onLocationChanged(location)
    }
    private var firstLocationReceived = false
    private var latestLocation: AMapLocation? = null

    // UI State
    private var isSearchMode = false
    private var isNavigating = false
    private var isAIOverlayVisible = false

    // AI Handler
    private val handler = Handler(Looper.getMainLooper())

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

            // Configure location style - blue arrow with direction (no auto-follow)
            val myLocationStyle = MyLocationStyle()
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
            myLocationStyle.interval(2000)
            myLocationStyle.strokeColor(android.graphics.Color.BLUE)
            myLocationStyle.radiusFillColor(android.graphics.Color.argb(50, 0, 0, 255))
            aMap?.myLocationStyle = myLocationStyle

            aMap?.isMyLocationEnabled = true

            // Configure UI settings
            aMap?.uiSettings?.isMyLocationButtonEnabled = false
            aMap?.uiSettings?.isZoomControlsEnabled = false
            aMap?.uiSettings?.isCompassEnabled = false

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

            // Start location
            startLocation()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startLocation() {
        try {
            // Initialize location client
            locationClient = AMapLocationClient(requireContext())

            // Configure location option
            val locationOption = AMapLocationClientOption()
            locationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            locationOption.isOnceLocation = false
            locationOption.isNeedAddress = true
            locationOption.interval = 2000

            locationClient?.setLocationOption(locationOption)
            locationClient?.setLocationListener(locationListener)
            locationClient?.startLocation()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onLocationChanged(location: AMapLocation?) {
        // Save latest location
        if (location != null && location.errorCode == 0) {
            latestLocation = location
        }

        if (location != null) {
            if (location.errorCode == 0) {
                // Successfully got location
                val latLng = LatLng(location.latitude, location.longitude)

                // Move camera to current location on first successful location
                if (!firstLocationReceived) {
                    // Get current zoom level and increase by 1
                    val currentZoom = aMap?.cameraPosition?.zoom ?: 16f
                    val newZoom = currentZoom + 1f

                    aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, newZoom))
                    firstLocationReceived = true

                    Toast.makeText(
                        requireContext(),
                        "首次定位成功！移动到当前位置",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                // Location error
                val errorInfo = when (location.errorCode) {
                    1 -> "定位失败，请检查定位权限"
                    2 -> "定位失败，请检查网络连接"
                    3 -> "定位失败，请检查设备设置"
                    else -> "定位失败，错误码: ${location.errorCode}, 错误信息: ${location.errorInfo}"
                }
                Toast.makeText(
                    requireContext(),
                    errorInfo,
                    Toast.LENGTH_SHORT
                ).show()
            }
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
            latestLocation?.let { location ->
                if (location.errorCode == 0) {
                    val myLatLng = LatLng(location.latitude, location.longitude)
                    aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16f))
                } else {
                    Toast.makeText(
                        requireContext(),
                        "尚未获取到定位信息",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } ?: Toast.makeText(
                requireContext(),
                "尚未获取到定位信息",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupAIAssistant() {
        // Find the button directly from the root view
        val voiceBtn = binding.root.findViewById<CardView>(R.id.voice_assistant_btn)
        val aiOverlay = binding.root.findViewById<View>(R.id.ai_agent_overlay)

        voiceBtn?.setOnClickListener {
            // Toggle overlay visibility
            if (aiOverlay != null) {
                if (aiOverlay.visibility == View.VISIBLE) {
                    aiOverlay.visibility = View.GONE
                } else {
                    aiOverlay.visibility = View.VISIBLE
                    aiOverlay.bringToFront()
                }
            }
        }

        // Setup AI overlay views after layout
        binding.root.post {
            setupAIOverlayViews()
        }
    }

    private fun setupAIOverlayViews() {
        // Close AI overlay
        val btnClose = binding.root.findViewById<View>(R.id.btn_close_ai)
        btnClose?.setOnClickListener {
            hideAIAgentOverlay()
        }

        // Suggestion buttons (now TextView)
        val suggestion1 = binding.root.findViewById<TextView>(R.id.ai_suggestion_1)
        suggestion1?.setOnClickListener {
            handleAIInput("帮我找找附近停车方便的咖啡馆")
        }

        val suggestion2 = binding.root.findViewById<TextView>(R.id.ai_suggestion_2)
        suggestion2?.setOnClickListener {
            handleAIInput("推荐一下周边的亲子乐园")
        }

        val suggestion3 = binding.root.findViewById<TextView>(R.id.ai_suggestion_3)
        suggestion3?.setOnClickListener {
            handleAIInput("避开拥堵，去三里屯怎么走？")
        }

        // Send button
        val btnSend = binding.root.findViewById<ImageButton>(R.id.btn_ai_send)
        btnSend?.setOnClickListener {
            val input = binding.root.findViewById<EditText>(R.id.ai_input)
            val text = input?.text.toString()
            if (text.isNotBlank()) {
                handleAIInput(text)
            }
        }

        // Input enter key
        val aiInput = binding.root.findViewById<EditText>(R.id.ai_input)
        aiInput?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val text = aiInput.text.toString()
                if (text.isNotBlank()) {
                    handleAIInput(text)
                }
                true
            } else {
                false
            }
        }
    }

    private fun showAIAgentOverlay() {
        isAIOverlayVisible = true
        val overlay = binding.root.findViewById<View>(R.id.ai_agent_overlay)
        overlay?.visibility = View.VISIBLE
        overlay?.alpha = 0f
        overlay?.animate()?.alpha(1f)?.setDuration(300)?.start()

        // Reset to suggestions view
        binding.root.findViewById<View>(R.id.ai_suggestions_view)?.visibility = View.VISIBLE
        binding.root.findViewById<View>(R.id.ai_thinking_view)?.visibility = View.GONE
        binding.root.findViewById<View>(R.id.ai_result_view)?.visibility = View.GONE

        // Clear input
        binding.root.findViewById<EditText>(R.id.ai_input)?.text?.clear()
    }

    private fun hideAIAgentOverlay() {
        isAIOverlayVisible = false
        val overlay = binding.root.findViewById<View>(R.id.ai_agent_overlay)
        overlay?.animate()?.alpha(0f)?.setDuration(200)?.withEndAction {
            overlay?.visibility = View.GONE
        }?.start()
    }

    private fun handleAIInput(input: String) {
        // Show thinking state
        binding.root.findViewById<View>(R.id.ai_suggestions_view)?.visibility = View.GONE
        binding.root.findViewById<View>(R.id.ai_thinking_view)?.visibility = View.VISIBLE
        binding.root.findViewById<View>(R.id.ai_result_view)?.visibility = View.GONE

        // Update thinking text
        val thinkingText = binding.root.findViewById<TextView>(R.id.ai_thinking_text)
        thinkingText?.text = "\"$input\""

        // Clear input
        binding.root.findViewById<EditText>(R.id.ai_input)?.text?.clear()

        // Simulate AI processing
        handler.postDelayed({
            // Show result
            showAIResult(input)
        }, 2500)
    }

    private fun showAIResult(input: String) {
        binding.root.findViewById<View>(R.id.ai_thinking_view)?.visibility = View.GONE
        binding.root.findViewById<View>(R.id.ai_result_view)?.visibility = View.VISIBLE

        val resultText = when {
            input.contains("乐园") || input.contains("公园") || input.contains("亲子") ->
                "已为您找到周边最受好评的 2 家亲子乐园，生态环境优美，非常适合周末游玩。"
            input.contains("咖啡馆") || input.contains("咖啡") ->
                "已为您找到周边停车位最充足的 3 家咖啡馆，这就为您展示详情。"
            input.contains("三里屯") || input.contains("拥堵") ->
                "已为您规划避开拥堵的最佳路线，预计节省 15 分钟，这就开始导航。"
            else ->
                "已为您找到相关结果，这就为您展示详情。"
        }

        binding.root.findViewById<TextView>(R.id.ai_result_text)?.text = "\"$resultText\""

        // Auto-hide and show results
        handler.postDelayed({
            hideAIAgentOverlay()
            // Show search results with relevant POIs
            performSearch(if (input.contains("咖啡")) "咖啡" else if (input.contains("乐园") || input.contains("公园")) "公园" else input)
        }, 3000)
    }

    private fun setupPOIList() {
        poiAdapter = POIAdapter(
            onItemClick = { poi, position ->
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

        // Save search query for later use
        currentSearchQuery = query

        // Initialize PoiSearch if not already initialized
        if (poiSearch == null) {
            poiSearch = PoiSearch(requireContext(), null)
            poiSearch?.setOnPoiSearchListener(this)
        }

        // Create search query
        val queryObj = PoiSearch.Query(query, "", "")
        queryObj.pageSize = 20
        queryObj.pageNum = 0

        poiSearch?.query = queryObj

        // Get current location for search center
        aMap?.myLocation?.let { location ->
            val latLonPoint = LatLonPoint(location.latitude, location.longitude)
            queryObj.location = latLonPoint
        }

        // Start search
        poiSearch?.searchPOIAsyn()

        // Hide keyboard
        binding.searchInput.clearFocus()
    }

    // PoiSearch.OnPoiSearchListener implementation
    override fun onPoiSearched(result: PoiResult?, rCode: Int) {
        if (rCode == 1000) {
            val pois = result?.pois
            if (pois != null && pois.isNotEmpty()) {
                val count = pois.size
                val firstPoiName = pois[0].title

                // Show toast with result count and first POI name
                Toast.makeText(
                    requireContext(),
                    "找到 $count 个结果，第一条: $firstPoiName",
                    Toast.LENGTH_SHORT
                ).show()

                // Show search results panel
                binding.poiResultsPanel.visibility = View.VISIBLE

                // Hide search bar
                binding.searchBarContainer.visibility = View.GONE

                // Convert PoiItem to POI
                val convertedPOIs = pois.map { poiItem ->
                    POI(
                        id = poiItem.poiId ?: "",
                        name = poiItem.title,
                        address = poiItem.snippet ?: "",
                        distance = poiItem.distance,
                        latitude = poiItem.latLonPoint?.latitude ?: 0.0,
                        longitude = poiItem.latLonPoint?.longitude ?: 0.0
                    )
                }

                poiList.clear()
                poiList.addAll(convertedPOIs)
                poiAdapter.submitList(convertedPOIs)

                // Select first item
                if (convertedPOIs.isNotEmpty()) {
                    poiAdapter.setSelectedPosition(0)
                }

                // Clear existing markers and add new ones
                clearPOIMarkers()
                addPOIMarkers(convertedPOIs)

                // Adjust camera to show all markers
                adjustCameraToShowAllPOIs(convertedPOIs)

                // Update title
                binding.poiResultsTitle.text = "\"$currentSearchQuery\" 的搜索结果"
            } else {
                Toast.makeText(
                    requireContext(),
                    "未找到相关结果",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "搜索失败，错误码: $rCode",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onPoiItemSearched(item: PoiItem?, rCode: Int) {
        // Not used for batch search
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

        // Start navigation activity
        startNaviActivity(poi)
    }

    private fun startNaviActivity(poi: POI) {
        // Get current location
        latestLocation?.let { startLoc ->
            if (startLoc.errorCode == 0) {
                val intent = Intent(requireContext(), com.seeway.xiaoxinapp.NaviActivity::class.java)
                intent.putExtra(com.seeway.xiaoxinapp.NaviActivity.EXTRA_START_LAT, startLoc.latitude)
                intent.putExtra(com.seeway.xiaoxinapp.NaviActivity.EXTRA_START_LON, startLoc.longitude)
                intent.putExtra(com.seeway.xiaoxinapp.NaviActivity.EXTRA_END_LAT, poi.latitude)
                intent.putExtra(com.seeway.xiaoxinapp.NaviActivity.EXTRA_END_LON, poi.longitude)
                intent.putExtra(com.seeway.xiaoxinapp.NaviActivity.EXTRA_END_NAME, poi.name)

                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "尚未获取到当前位置，无法开始导航",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } ?: Toast.makeText(
            requireContext(),
            "尚未获取到当前位置，无法开始导航",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun hideSearchResults() {
        binding.poiResultsPanel.visibility = View.GONE

        // Show search bar
        binding.searchBarContainer.visibility = View.VISIBLE

        // Clear POI markers
        clearPOIMarkers()
    }

    private fun clearPOIMarkers() {
        poiMarkers.forEach { it.remove() }
        poiMarkers.clear()
    }

    private fun addPOIMarkers(pois: List<POI>) {
        pois.forEach { poi ->
            val latLng = LatLng(poi.latitude, poi.longitude)
            val marker = aMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(poi.name)
                    .snippet(poi.address)
            )
            marker?.let { poiMarkers.add(it) }
        }
    }

    private fun adjustCameraToShowAllPOIs(pois: List<POI>) {
        if (pois.isEmpty()) return

        val boundsBuilder = com.amap.api.maps.model.LatLngBounds.Builder()
        pois.forEach { poi ->
            boundsBuilder.include(LatLng(poi.latitude, poi.longitude))
        }

        try {
            val bounds = boundsBuilder.build()
            val padding = 200 // padding in pixels
            aMap?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, padding)
            )
        } catch (e: Exception) {
            // If only one POI or bounds calculation fails, just move to first POI
            val firstPoi = pois[0]
            val latLng = LatLng(firstPoi.latitude, firstPoi.longitude)
            aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
        }
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
            // Stop location
            locationClient?.stopLocation()
            locationClient?.onDestroy()
            locationClient = null

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
