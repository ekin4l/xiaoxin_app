package com.seeway.xiaoxinapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.AMapNaviView
import com.seeway.xiaoxinapp.databinding.ActivityNaviBinding

/**
 * 导航Activity - 使用高德导航SDK进行应用内导航
 */
class NaviActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNaviBinding
    private var aMapNavi: AMapNavi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val endLat = intent.getDoubleExtra(EXTRA_END_LAT, 0.0)
        val endLon = intent.getDoubleExtra(EXTRA_END_LON, 0.0)
        val endName = intent.getStringExtra(EXTRA_END_NAME) ?: "目的地"
        val startLat = intent.getDoubleExtra(EXTRA_START_LAT, 0.0)
        val startLon = intent.getDoubleExtra(EXTRA_START_LON, 0.0)

        binding.tvNaviTitle.text = "导航到 $endName"
        binding.tvNaviInfo.text = "正在计算路线..."

        // 初始化导航视图
        binding.naviView.onCreate(savedInstanceState)

        // 初始化导航
        aMapNavi = AMapNavi.getInstance(this)

        // 添加导航监听器 - 使用最简实现
        aMapNavi?.addAMapNaviListener(object : com.amap.api.navi.AMapNaviListener {
            override fun onInitNaviSuccess() {
                // 初始化成功，计算路线
                if (startLat != 0.0 && startLon != 0.0) {
                    val startPoint = com.amap.api.navi.model.NaviLatLng(startLat, startLon)
                    val endPoint = com.amap.api.navi.model.NaviLatLng(endLat, endLon)
                    aMapNavi?.calculateDriveRoute(listOf(startPoint), listOf(endPoint), 0)
                } else {
                    Toast.makeText(this@NaviActivity, "无法获取当前位置", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onInitNaviFailure() {
                Toast.makeText(this@NaviActivity, "导航初始化失败", Toast.LENGTH_SHORT).show()
            }

            override fun onCalculateRouteSuccess(result: IntArray?) {
                // 路线计算成功，开始导航
                binding.naviLayout.visibility = View.VISIBLE
                aMapNavi?.startNavi(0)
                binding.tvNaviInfo.text = "导航中..."
            }

            override fun onCalculateRouteSuccess(p0: com.amap.api.navi.model.AMapCalcRouteResult?) {
                // 新版本API的回调
                binding.naviLayout.visibility = View.VISIBLE
                aMapNavi?.startNavi(0)
                binding.tvNaviInfo.text = "导航中..."
            }

            override fun onCalculateRouteFailure(p0: Int) {
                Toast.makeText(this@NaviActivity, "路线计算失败", Toast.LENGTH_SHORT).show()
                binding.tvNaviInfo.text = "路线计算失败"
            }

            override fun onCalculateRouteFailure(p0: com.amap.api.navi.model.AMapCalcRouteResult?) {
                Toast.makeText(this@NaviActivity, "路线计算失败", Toast.LENGTH_SHORT).show()
            }

            override fun onArriveDestination() {
                Toast.makeText(this@NaviActivity, "已到达目的地: $endName", Toast.LENGTH_LONG).show()
                binding.tvNaviInfo.text = "已到达目的地"
            }

            override fun onNaviInfoUpdate(p0: com.amap.api.navi.model.NaviInfo?) {
                p0?.let {
                    val distance = it.getPathRetainDistance()
                    val time = it.getPathRetainTime()
                    binding.tvNaviInfo.text = "剩余: ${distance}米, ${time}秒"
                }
            }

            override fun onStartNavi(p0: Int) {}

            // 其他必需方法的空实现
            override fun onTrafficStatusUpdate() {}
            override fun onLocationChange(p0: com.amap.api.navi.model.AMapNaviLocation?) {}
            override fun onReCalculateRouteForYaw() {}
            override fun onReCalculateRouteForTrafficJam() {}
            override fun onArrivedWayPoint(p0: Int) {}
            override fun onGpsOpenStatus(p0: Boolean) {}
            override fun updateCameraInfo(p0: Array<out com.amap.api.navi.model.AMapNaviCameraInfo>?) {}
            override fun updateIntervalCameraInfo(p0: com.amap.api.navi.model.AMapNaviCameraInfo?, p1: com.amap.api.navi.model.AMapNaviCameraInfo?, p2: Int) {}
            override fun onServiceAreaUpdate(p0: Array<out com.amap.api.navi.model.AMapServiceAreaInfo>?) {}
            override fun showCross(p0: com.amap.api.navi.model.AMapNaviCross?) {}
            override fun hideCross() {}
            override fun showModeCross(p0: com.amap.api.navi.model.AMapModelCross?) {}
            override fun hideModeCross() {}
            override fun showLaneInfo(p0: Array<out com.amap.api.navi.model.AMapLaneInfo>?, p1: ByteArray?, p2: ByteArray?) {}
            override fun showLaneInfo(p0: com.amap.api.navi.model.AMapLaneInfo?) {}
            override fun hideLaneInfo() {}
            override fun notifyParallelRoad(p0: Int) {}
            override fun OnUpdateTrafficFacility(p0: Array<out com.amap.api.navi.model.AMapNaviTrafficFacilityInfo>?) {}
            override fun OnUpdateTrafficFacility(p0: com.amap.api.navi.model.AMapNaviTrafficFacilityInfo?) {}
            override fun updateAimlessModeStatistics(p0: com.amap.api.navi.model.AimLessModeStat?) {}
            override fun updateAimlessModeCongestionInfo(p0: com.amap.api.navi.model.AimLessModeCongestionInfo?) {}
            override fun onPlayRing(p0: Int) {}
            override fun onNaviRouteNotify(p0: com.amap.api.navi.model.AMapNaviRouteNotifyData?) {}
            override fun onGpsSignalWeak(p0: Boolean) {}
            override fun onGetNavigationText(p0: Int, p1: String?) {}
            override fun onGetNavigationText(p0: String?) {}
            override fun onEndEmulatorNavi() {}
        })

        binding.btnExitNavi.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val EXTRA_START_LAT = "start_lat"
        const val EXTRA_START_LON = "start_lon"
        const val EXTRA_END_LAT = "end_lat"
        const val EXTRA_END_LON = "end_lon"
        const val EXTRA_END_NAME = "end_name"
    }

    override fun onResume() {
        super.onResume()
        binding.naviView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.naviView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.naviView.onDestroy()
        aMapNavi?.stopNavi()
        aMapNavi = null
        AMapNavi.destroy()
    }
}
