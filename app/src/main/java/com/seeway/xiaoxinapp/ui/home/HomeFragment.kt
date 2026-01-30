package com.seeway.xiaoxinapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.seeway.xiaoxinapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var aMap: AMap? = null

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initMap() {
        try {
            aMap = binding.mapView.map

            // 设置地图样式，使用更兼容的2D模式
            aMap?.mapType = AMap.MAP_TYPE_NORMAL

            val myLocationStyle = MyLocationStyle()
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
            myLocationStyle.interval(2000)
            aMap?.myLocationStyle = myLocationStyle

            aMap?.isMyLocationEnabled = true

            aMap?.uiSettings?.isMyLocationButtonEnabled = false
            aMap?.uiSettings?.isZoomControlsEnabled = true
            aMap?.uiSettings?.isCompassEnabled = true

            // 禁用一些可能不兼容的特性
            aMap?.showIndoorMap(false)

            val beijing = LatLng(39.9042, 116.4074)
            aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(beijing, 12f))

            binding.fabLocate.setOnClickListener {
                aMap?.myLocation?.let { location ->
                    val myLatLng = LatLng(location.latitude, location.longitude)
                    aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16f))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
