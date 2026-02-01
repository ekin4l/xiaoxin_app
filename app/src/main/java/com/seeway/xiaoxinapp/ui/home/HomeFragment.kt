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

            // Configure location style
            val myLocationStyle = MyLocationStyle()
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
            myLocationStyle.interval(2000)
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
