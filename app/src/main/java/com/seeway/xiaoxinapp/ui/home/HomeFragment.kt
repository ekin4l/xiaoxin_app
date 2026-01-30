package com.seeway.xiaoxinapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.seeway.xiaoxinapp.NavItem
import com.seeway.xiaoxinapp.NavItemAdapter
import com.seeway.xiaoxinapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        setupNavItems()
    }

    private fun setupNavItems() {
        val navItems = listOf(
            NavItem("搜索引擎", com.seeway.xiaoxinapp.R.drawable.ic_search, "#4285F4"),
            NavItem("社交媒体", com.seeway.xiaoxinapp.R.drawable.ic_social, "#1DA1F2"),
            NavItem("视频平台", com.seeway.xiaoxinapp.R.drawable.ic_video, "#FF0000"),
            NavItem("购物商城", com.seeway.xiaoxinapp.R.drawable.ic_shop, "#FF6900"),
            NavItem("新闻资讯", com.seeway.xiaoxinapp.R.drawable.ic_news, "#000000"),
            NavItem("音乐流媒体", com.seeway.xiaoxinapp.R.drawable.ic_music, "#1DB954"),
            NavItem("办公工具", com.seeway.xiaoxinapp.R.drawable.ic_work, "#4A90D9"),
            NavItem("学习平台", com.seeway.xiaoxinapp.R.drawable.ic_study, "#FFC107"),
            NavItem("游戏娱乐", com.seeway.xiaoxinapp.R.drawable.ic_game, "#9C27B0"),
            NavItem("出行导航", com.seeway.xiaoxinapp.R.drawable.ic_map, "#34A853"),
            NavItem("金融理财", com.seeway.xiaoxinapp.R.drawable.ic_finance, "#00BCD4"),
            NavItem("生活服务", com.seeway.xiaoxinapp.R.drawable.ic_life, "#FF9800")
        )

        val spanCount = if (resources.configuration.screenWidthDp >= 600) 4 else 3
        binding.rvNavItems.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.rvNavItems.adapter = NavItemAdapter(navItems)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
