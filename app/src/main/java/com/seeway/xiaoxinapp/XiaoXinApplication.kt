package com.seeway.xiaoxinapp

import android.app.Application
import android.content.Context
import com.amap.api.maps.MapsInitializer

class XiaoXinApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        setupPrivacyCompliance()
        context = applicationContext
    }

    private fun setupPrivacyCompliance() {
        // 地图SDK隐私合规设置
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
    }

    companion object {
        lateinit var context: Context
    }
}
