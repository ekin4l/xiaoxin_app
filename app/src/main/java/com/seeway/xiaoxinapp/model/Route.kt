package com.seeway.xiaoxinapp.model

import com.amap.api.services.route.DriveRouteResult
import com.amap.api.services.route.DriveStep

/**
 * Route plan data model
 */
data class Route(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val distance: Int = 0, // in meters
    val duration: Int = 0, // in seconds
    val trafficLights: Int = 0,
    val routeType: RouteType = RouteType.RECOMMENDED,
    val polyline: List<LatLng> = emptyList(),
    val steps: List<RouteStep> = emptyList(),
    val tollFee: Int = 0, // in yuan
    val tollDistance: Int = 0 // toll road distance in meters
) {
    enum class RouteType {
        RECOMMENDED,      // Recommended route
        FASTEST,          // Fastest route
        SHORTEST,         // Shortest distance
        NO_HIGHWAY,       // Avoid highways
        ECONOMICAL        // Fuel efficient
    }

    /**
     * Get formatted distance string
     */
    fun getFormattedDistance(): String {
        return when {
            distance < 1000 -> "${distance}米"
            else -> "${distance / 1000}.${(distance % 1000) / 100}公里"
        }
    }

    /**
     * Get formatted duration string
     */
    fun getFormattedDuration(): String {
        val hours = duration / 3600
        val minutes = (duration % 3600) / 60

        return when {
            hours > 0 -> "${hours}小时${minutes}分钟"
            minutes > 0 -> "${minutes}分钟"
            else -> "1分钟"
        }
    }

    /**
     * Get route type display name
     */
    fun getRouteTypeName(): String {
        return when (routeType) {
            RouteType.RECOMMENDED -> "推荐"
            RouteType.FASTEST -> "最快"
            RouteType.SHORTEST -> "最短"
            RouteType.NO_HIGHWAY -> "少高速"
            RouteType.ECONOMICAL -> "经济"
        }
    }

    data class LatLng(
        val latitude: Double,
        val longitude: Double
    )

    data class RouteStep(
        val instruction: String,
        val distance: Int,
        val duration: Int,
        val action: String = ""
    )

    companion object {
        /**
         * Create mock routes for testing
         */
        fun createMockRoutes(): List<Route> {
            return listOf(
                Route(
                    id = "1",
                    name = "推荐路线",
                    description = "经由京藏高速",
                    distance = 15000,
                    duration = 1200,
                    trafficLights = 8,
                    routeType = RouteType.RECOMMENDED,
                    tollFee = 25,
                    tollDistance = 8000,
                    steps = listOf(
                        RouteStep("沿当前道路向东行驶", 500, 60, "直行"),
                        RouteStep("右转进入京藏高速", 200, 30, "右转"),
                        RouteStep("沿京藏高速行驶", 8000, 600, "高速"),
                        RouteStep("从出口驶出", 300, 40, "出口"),
                        RouteStep("到达目的地", 100, 20, "到达")
                    )
                ),
                Route(
                    id = "2",
                    name = "最快路线",
                    description = "经由北四环",
                    distance = 14500,
                    duration = 1080,
                    trafficLights = 12,
                    routeType = RouteType.FASTEST,
                    tollFee = 15,
                    tollDistance = 5000,
                    steps = listOf(
                        RouteStep("沿当前道路向东行驶", 500, 60, "直行"),
                        RouteStep("左转进入北四环", 300, 40, "左转"),
                        RouteStep("沿北四环行驶", 10000, 720, "环路"),
                        RouteStep("右转进入辅路", 200, 30, "右转"),
                        RouteStep("到达目的地", 100, 20, "到达")
                    )
                ),
                Route(
                    id = "3",
                    name = "最短路线",
                    description = "城市道路",
                    distance = 12000,
                    duration = 1800,
                    trafficLights = 25,
                    routeType = RouteType.SHORTEST,
                    tollFee = 0,
                    tollDistance = 0,
                    steps = listOf(
                        RouteStep("沿当前道路向东行驶", 2000, 300, "直行"),
                        RouteStep("右转进入主干道", 100, 20, "右转"),
                        RouteStep("沿主干道行驶", 8000, 1200, "直行"),
                        RouteStep("左转进入目的地道路", 200, 30, "左转"),
                        RouteStep("到达目的地", 100, 20, "到达")
                    )
                )
            )
        }
    }
}
