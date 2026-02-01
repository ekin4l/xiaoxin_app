package com.seeway.xiaoxinapp.model

import com.amap.api.services.core.PoiItem

/**
 * POI (Point of Interest) data model
 */
data class POI(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val distance: Int = 0, // in meters
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: POIType = POIType.OTHER,
    val rating: Float = 0f,
    val phoneNumber: String? = null,
    val website: String? = null,
    val imageUrl: String? = null
) {
    enum class POIType {
        RESTAURANT,
        HOTEL,
        ATTRACTION,
        SHOPPING,
        GAS_STATION,
        PARKING,
        HOSPITAL,
        BANK,
        OTHER
    }

    /**
     * Get formatted distance string
     */
    fun getFormattedDistance(): String {
        return when {
            distance < 1000 -> "${distance}m"
            else -> "${distance / 1000}.${(distance % 1000) / 100}km"
        }
    }

    /**
     * Get duration estimate (walking: 5min per km)
     */
    fun getWalkingDuration(): Int {
        return (distance / 1000 * 5).toInt()
    }

    /**
     * Get duration estimate (driving: 2min per km)
     */
    fun getDrivingDuration(): Int {
        return (distance / 1000 * 2).toInt()
    }

    companion object {
        /**
         * Create POI from AMap PoiItem
         */
        fun fromPoiItem(poiItem: PoiItem): POI {
            return POI(
                id = poiItem.poiId,
                name = poiItem.title,
                address = poiItem.snippet ?: "",
                distance = poiItem.distance,
                latitude = poiItem.latLonPoint.latitude,
                longitude = poiItem.latLonPoint.longitude,
                type = parsePOIType(poiItem.typeDes),
                rating = 0f // AMap doesn't provide rating in basic POI
            )
        }

        /**
         * Create mock POI for testing
         */
        fun createMockPOIs(): List<POI> {
            return listOf(
                POI(
                    id = "1",
                    name = "故宫博物院",
                    address = "北京市东城区景山前街4号",
                    distance = 3500,
                    latitude = 39.9163,
                    longitude = 116.3972,
                    type = POIType.ATTRACTION,
                    rating = 4.8f
                ),
                POI(
                    id = "2",
                    name = "天安门广场",
                    address = "北京市东城区西长安街",
                    distance = 3200,
                    latitude = 39.9042,
                    longitude = 116.3976,
                    type = POIType.ATTRACTION,
                    rating = 4.7f
                ),
                POI(
                    id = "3",
                    name = "颐和园",
                    address = "北京市海淀区新建宫门路19号",
                    distance = 15000,
                    latitude = 40.0005,
                    longitude = 116.2755,
                    type = POIType.ATTRACTION,
                    rating = 4.9f
                ),
                POI(
                    id = "4",
                    name = "北京首都国际机场",
                    address = "北京市顺义区首都机场路",
                    distance = 28000,
                    latitude = 40.0799,
                    longitude = 116.6031,
                    type = POIType.OTHER,
                    rating = 4.5f
                ),
                POI(
                    id = "5",
                    name = "北京西站",
                    address = "北京市丰台区莲花池东路",
                    distance = 8500,
                    latitude = 39.8936,
                    longitude = 116.3218,
                    type = POIType.OTHER,
                    rating = 4.3f
                )
            )
        }

        private fun parsePOIType(typeDes: String?): POIType {
            return when (typeDes?.lowercase()) {
                "餐饮服务", "美食" -> POIType.RESTAURANT
                "住宿服务" -> POIType.HOTEL
                "旅游景点", "风景名胜" -> POIType.ATTRACTION
                "购物服务", "商场" -> POIType.SHOPPING
                "汽车服务", "加油站" -> POIType.GAS_STATION
                "停车场" -> POIType.PARKING
                "医疗保健", "医院" -> POIType.HOSPITAL
                "金融保险", "银行" -> POIType.BANK
                else -> POIType.OTHER
            }
        }
    }
}
