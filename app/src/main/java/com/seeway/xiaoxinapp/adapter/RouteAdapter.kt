package com.seeway.xiaoxinapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.seeway.xiaoxinapp.databinding.ItemRouteSelectorBinding
import com.seeway.xiaoxinapp.model.Route

/**
 * Adapter for route selection
 */
class RouteAdapter(
    private val onItemClick: (Route) -> Unit,
    private var selectedRouteId: String = ""
) : ListAdapter<Route, RouteAdapter.RouteViewHolder>(RouteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemRouteSelectorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(getItem(position), selectedRouteId)
    }

    fun setSelectedRouteId(routeId: String) {
        selectedRouteId = routeId
        notifyDataSetChanged()
    }

    inner class RouteViewHolder(
        private val binding: ItemRouteSelectorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(route: Route, selectedId: String) {
            val isSelected = route.id == selectedId

            binding.apply {
                // Set selected state
                root.isSelected = isSelected
                // Note: stroke is set in XML via app:strokeWidth and app:strokeColor

                // Set route type badge
                tvRouteType.text = route.getRouteTypeName()
                val context = root.context
                when (route.routeType) {
                    Route.RouteType.RECOMMENDED -> {
                        tvRouteType.setBackgroundResource(android.R.color.transparent)
                        tvRouteType.setTextColor(context.getColor(com.seeway.xiaoxinapp.R.color.blue_600))
                    }
                    Route.RouteType.FASTEST -> {
                        tvRouteType.setBackgroundResource(android.R.color.transparent)
                        tvRouteType.setTextColor(context.getColor(com.seeway.xiaoxinapp.R.color.success))
                    }
                    Route.RouteType.SHORTEST -> {
                        tvRouteType.setBackgroundResource(android.R.color.transparent)
                        tvRouteType.setTextColor(context.getColor(com.seeway.xiaoxinapp.R.color.warning))
                    }
                    else -> {
                        tvRouteType.setBackgroundResource(android.R.color.transparent)
                        tvRouteType.setTextColor(context.getColor(com.seeway.xiaoxinapp.R.color.text_secondary))
                    }
                }

                // Set route name and description
                tvRouteName.text = route.name
                tvRouteDescription.text = route.description

                // Set distance and duration
                tvRouteDistance.text = route.getFormattedDistance()
                tvRouteDuration.text = route.getFormattedDuration()

                // Set traffic lights count
                tvRouteTrafficLights.text = "${route.trafficLights}个红绿灯"

                // Set toll info
                if (route.tollFee > 0) {
                    tvRouteToll.visibility = android.view.View.VISIBLE
                    tvRouteToll.text = "收费 ${route.tollFee}元"
                } else {
                    tvRouteToll.visibility = android.view.View.GONE
                }

                // Set selected indicator
                ivSelected.visibility = if (isSelected) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Set click listener
                root.setOnClickListener {
                    onItemClick(route)
                    setSelectedRouteId(route.id)
                }
            }
        }
    }

    private class RouteDiffCallback : DiffUtil.ItemCallback<Route>() {
        override fun areItemsTheSame(oldItem: Route, newItem: Route): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Route, newItem: Route): Boolean {
            return oldItem == newItem
        }
    }
}
