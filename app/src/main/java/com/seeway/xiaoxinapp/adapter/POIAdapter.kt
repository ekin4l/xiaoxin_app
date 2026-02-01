package com.seeway.xiaoxinapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.seeway.xiaoxinapp.databinding.ItemPoiResultBinding
import com.seeway.xiaoxinapp.model.POI

/**
 * Adapter for POI search results
 */
class POIAdapter(
    private val onItemClick: (POI) -> Unit,
    private val onRouteClick: (POI) -> Unit
) : ListAdapter<POI, POIAdapter.POIViewHolder>(POIDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): POIViewHolder {
        val binding = ItemPoiResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return POIViewHolder(binding)
    }

    override fun onBindViewHolder(holder: POIViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class POIViewHolder(
        private val binding: ItemPoiResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(poi: POI) {
            binding.apply {
                // Set POI name
                tvPoiName.text = poi.name

                // Set address
                tvPoiAddress.text = poi.address

                // Set distance
                tvPoiDistance.text = poi.getFormattedDistance()

                // Set duration estimate
                val walkingTime = poi.getWalkingDuration()
                tvPoiDuration.text = if (walkingTime < 60) {
                    "步行约${walkingTime}分钟"
                } else {
                    val hours = walkingTime / 60
                    val mins = walkingTime % 60
                    if (hours > 0) {
                        "步行约${hours}小时${mins}分钟"
                    } else {
                        "步行约${mins}分钟"
                    }
                }

                // Set type icon based on POI type
                ivPoiType.setImageResource(getTypeIcon(poi.type))

                // Set rating if available
                if (poi.rating > 0) {
                    tvPoiRating.visibility = android.view.View.VISIBLE
                    tvPoiRating.text = String.format("%.1f", poi.rating)
                } else {
                    tvPoiRating.visibility = android.view.View.GONE
                }

                // Set click listeners
                root.setOnClickListener { onItemClick(poi) }
                btnRoute.setOnClickListener { onRouteClick(poi) }
            }
        }

        private fun getTypeIcon(type: POI.POIType): Int {
            return when (type) {
                POI.POIType.RESTAURANT -> android.R.drawable.ic_menu_gallery
                POI.POIType.HOTEL -> android.R.drawable.ic_menu_gallery
                POI.POIType.ATTRACTION -> android.R.drawable.ic_menu_gallery
                POI.POIType.SHOPPING -> android.R.drawable.ic_menu_gallery
                POI.POIType.GAS_STATION -> android.R.drawable.ic_menu_gallery
                POI.POIType.PARKING -> android.R.drawable.ic_menu_gallery
                POI.POIType.HOSPITAL -> android.R.drawable.ic_menu_gallery
                POI.POIType.BANK -> android.R.drawable.ic_menu_gallery
                POI.POIType.OTHER -> android.R.drawable.ic_menu_info_details
            }
        }
    }

    private class POIDiffCallback : DiffUtil.ItemCallback<POI>() {
        override fun areItemsTheSame(oldItem: POI, newItem: POI): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: POI, newItem: POI): Boolean {
            return oldItem == newItem
        }
    }
}
