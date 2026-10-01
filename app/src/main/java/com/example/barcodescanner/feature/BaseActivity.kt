package com.example.barcodescanner.feature

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.barcodescanner.di.settings
import com.example.barcodescanner.di.rotationHelper
import com.example.barcodescanner.usecase.Settings
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // ① 主题色（Material You）：必须在 super.onCreate() / setContentView() 之前应用，
        //    否则视图已经按旧主题 inflate 出来了。改主题色后界面需要重建（recreate）才生效。
        applyThemeColor()

        super.onCreate(savedInstanceState)

        // ② 修复（HyperOS/MIUI 底部栏"分层"）：
        //
        // 上游的布局逻辑本来就是对的 —— 让内容铺到导航栏下方（LAYOUT_HIDE_NAVIGATION），
        // 再由各页面用 extension/WindowsInsets.kt 的 applySystemWindowInsets() 把导航栏高度
        // 补成 padding（Activity 根部 / app_bar_layout / bottom_navigation_view 等）。
        // 所以这里保持原行为，不能改成"根布局统一 insets"，否则会与逐 View 处理叠加、挤坏布局。
        //
        // 分层的真正原因在"颜色"上：
        //   1) 主题里 android:navigationBarColor 被设成不透明色 → 已改为透明（values/styles.xml）
        //   2) MIUI/HyperOS 默认开启"导航栏对比度强制"，给手势条垫一层半透明底衬 → 下面关掉
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        rotationHelper.lockCurrentOrientationIfNeeded(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        applyStatusBarColor()
    }

    /**
     * ① 状态栏与标题栏同色（都用 M3 的 app bar 容器色 colorSurfaceContainer），
     *    两块连成一体，视觉上更沉浸。
     *
     * ⚠️ 必须在这里（onPostCreate，视图已 inflate）并从**视图**取色，不能从 window.decorView 取：
     *    AppCompat 给视图挂的是带 DynamicColors 覆盖层的 ContextThemeWrapper，而 window.decorView
     *    持有的是另一个 theme 实例、不含覆盖层 —— 用它取 colorSurfaceContainer 会拿到 M3 基线色。
     *    2026-10-01 实测：状态栏 #F3EDF7（基线淡紫）而标题栏 #E7F1E3（绿色种子推导），明显不一致。
     */
    fun applyStatusBarColor() {
        val anchor = findViewById<View>(android.R.id.content) ?: window.decorView
        @Suppress("DEPRECATION")
        window.statusBarColor = MaterialColors.getColor(
            anchor,
            com.google.android.material.R.attr.colorSurfaceContainer
        )
    }

    /**
     * 沉浸式页面（相机扫描页）用：状态栏透明，让内容（相机预览）铺到状态栏下。
     * 离开该页面时调用 [applyStatusBarColor] 还原成与标题栏同色的主题色。
     */
    fun applyTransparentStatusBar() {
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun applyThemeColor() {
        // 三条路径统一走 Material 的 DynamicColors：
        //   · 跟随系统 → Monet 取壁纸配色
        //   · 预设色/自定义色 → content-based：按种子色在本地算出整套 M3 色阶（HCT，
        //     与系统 Monet 同源），因此标题栏、卡片等 surface 系列颜色也会跟着主题走
        val builder = DynamicColorsOptions.Builder()
        if (settings.themeColor != Settings.THEME_COLOR_DYNAMIC) {
            builder.setContentBasedSource(settings.themeColorSeed)
        }
        DynamicColors.applyToActivityIfAvailable(this, builder.build())
    }
}
