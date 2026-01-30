package com.seeway.xiaoxinapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.seeway.xiaoxinapp.databinding.ItemNavBinding

class NavItemAdapter(
    private val items: List<NavItem>
) : RecyclerView.Adapter<NavItemAdapter.NavItemViewHolder>() {

    inner class NavItemViewHolder(private val binding: ItemNavBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NavItem) {
            binding.tvName.text = item.name
            val cardView = binding.root as com.google.android.material.card.MaterialCardView
            try {
                cardView.setCardBackgroundColor(Color.parseColor(item.color))
            } catch (e: Exception) {
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        android.R.color.darker_gray
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavItemViewHolder {
        val binding = ItemNavBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NavItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NavItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
