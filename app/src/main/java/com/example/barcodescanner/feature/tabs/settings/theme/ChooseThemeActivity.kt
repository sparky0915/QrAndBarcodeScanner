package com.example.barcodescanner.feature.tabs.settings.theme

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.example.barcodescanner.R
import com.example.barcodescanner.di.settings
import com.example.barcodescanner.extension.applySystemWindowInsets
import com.example.barcodescanner.extension.unsafeLazy
import com.example.barcodescanner.feature.BaseActivity
import com.example.barcodescanner.usecase.Settings
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.activity_choose_theme.*
import kotlinx.android.synthetic.main.dialog_color_picker.view.*

class ChooseThemeActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ChooseThemeActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val buttons by unsafeLazy {
        listOf(button_system_theme, button_light_theme, button_dark_theme)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_theme)
        supportEdgeToEdge()
        initToolbar()
        initThemeColor()
    }

    override fun onResume() {
        super.onResume()
        showInitialSettings()
        handleSettingsChanged()
    }

    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun initToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
    }

    // ---------- 主题色（Material You / 预设 / 自定义）----------

    private fun initThemeColor() {
        button_dynamic_color.isChecked = settings.themeColor == Settings.THEME_COLOR_DYNAMIC
        button_dynamic_color.setCheckedChangedListener { isChecked ->
            if (isChecked) {
                selectThemeColor(Settings.THEME_COLOR_DYNAMIC)
            }
        }
        buildColorDots()
        initCustomColor()
    }

    /** 8 个预设色点：单排、等分（layout_weight），选中的套一圈主题色描边 */
    private fun buildColorDots() {
        layout_theme_colors.removeAllViews()

        val dotSize = resources.getDimensionPixelSize(R.dimen.theme_color_dot_size)
        val innerSize = resources.getDimensionPixelSize(R.dimen.theme_color_dot_inner_size)
        val current = settings.themeColor

        Settings.THEME_COLOR_PRESETS.forEach { key ->
            val cell = FrameLayout(this)
            // 等分宽度：8 个色点单排铺满一行且始终居中，窄屏也不会被裁掉
            cell.layoutParams = LinearLayout.LayoutParams(0, dotSize, 1f)
            cell.isClickable = true
            cell.isFocusable = true
            cell.contentDescription = themeColorName(key)
            cell.setOnClickListener { selectThemeColor(key) }

            val dot = View(this)
            dot.layoutParams = FrameLayout.LayoutParams(innerSize, innerSize, Gravity.CENTER)
            dot.background = ContextCompat.getDrawable(this, R.drawable.theme_color_dot)
            dot.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, Settings.themeColorPreviewRes(key))
            )

            if (key == current) {
                cell.background = ContextCompat.getDrawable(this, R.drawable.theme_color_dot_ring)
            }

            cell.addView(dot)
            layout_theme_colors.addView(cell)
        }
    }

    private fun initCustomColor() {
        view_custom_color_swatch.backgroundTintList =
            ColorStateList.valueOf(settings.themeColorCustom)
        frame_custom_color.background = if (settings.themeColor == Settings.THEME_COLOR_CUSTOM) {
            ContextCompat.getDrawable(this, R.drawable.theme_color_dot_ring)
        } else {
            null
        }
        layout_custom_color.setOnClickListener { showColorPickerDialog() }
    }

    /**
     * 自定义取色。
     * Android / Material 都没有公开的系统取色控件（ColorPickerDialog 是隐藏的内部类），
     * 所以这里用原生 Slider 拼一个 HSV 取色器：色相 / 饱和度 / 明度 + Hex 输入框 + 实时预览。
     * 滑块与 Hex 框双向联动：拖滑块会更新色号，直接填色号也会反过来驱动滑块与预览。
     */
    private fun showColorPickerDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_color_picker, null)

        var picked = settings.themeColorCustom
        // 防止"滑块 → 写回 Hex → 又触发 Hex 监听 → 再改滑块"形成回环
        var syncing = false

        fun applyColorFromSliders() {
            picked = Color.HSVToColor(
                floatArrayOf(
                    view.slider_hue.value,
                    view.slider_saturation.value / 100f,
                    view.slider_lightness.value / 100f
                )
            )
            view.color_preview.backgroundTintList = ColorStateList.valueOf(picked)
            syncing = true
            view.input_color_hex.setText(hexOf(picked))
            syncing = false
        }

        val hsv = FloatArray(3)
        Color.colorToHSV(picked, hsv)
        // 说明：Slider 不设 stepSize 时为连续取值，可直接赋小数
        // （设过 stepSize=1 时非整数倍取值会抛 IllegalStateException，2026-10-01 踩过）
        syncing = true
        view.slider_hue.value = hsv[0].coerceIn(0f, 360f)
        view.slider_saturation.value = hsv[1] * 100f
        view.slider_lightness.value = hsv[2] * 100f
        syncing = false

        val onChange = Slider.OnChangeListener { _, _, _ ->
            if (!syncing) {
                applyColorFromSliders()
            }
        }
        listOf(view.slider_hue, view.slider_saturation, view.slider_lightness).forEach {
            it.addOnChangeListener(onChange)
        }

        // Hex 输入 → 反向驱动滑块与预览；非法输入不做任何事，等用户继续敲
        view.input_color_hex.doAfterTextChanged { text ->
            if (!syncing) {
                val parsed = parseHexColor(text?.toString())
                if (parsed != null) {
                    picked = parsed
                    val v = FloatArray(3)
                    Color.colorToHSV(parsed, v)
                    syncing = true
                    view.slider_hue.value = v[0].coerceIn(0f, 360f)
                    view.slider_saturation.value = v[1] * 100f
                    view.slider_lightness.value = v[2] * 100f
                    syncing = false
                    view.color_preview.backgroundTintList = ColorStateList.valueOf(parsed)
                    // 注意：此时不再回写输入框 —— HSV 往返取整会把用户填的色号改掉
                }
            }
        }

        syncing = true
        view.input_color_hex.setText(hexOf(picked))
        syncing = false
        view.color_preview.backgroundTintList = ColorStateList.valueOf(picked)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.activity_choose_theme_color_custom)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                settings.themeColorCustom = picked
                selectThemeColor(Settings.THEME_COLOR_CUSTOM)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun hexOf(color: Int): String = String.format("#%06X", 0xFFFFFF and color)

    /** 解析 #RRGGBB / RRGGBB，也接受 3 位缩写；非法返回 null */
    private fun parseHexColor(raw: String?): Int? {
        val hex = raw?.trim()?.removePrefix("#") ?: return null
        return try {
            when (hex.length) {
                6 -> Color.parseColor("#$hex")
                3 -> Color.parseColor("#${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}")
                else -> null
            }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * 改主题色。
     * Material 的 DynamicColors 只能在 Activity 创建时应用，已经 inflate 出来的旧界面不会自动换色，
     * 所以这里重启整个任务：先起主界面（清栈重建），再把主题页叠在上面，
     * 这样返回时设置页也是新配色，同时还能留在本页连续试色。
     */
    private fun selectThemeColor(key: String) {
        if (settings.themeColor == key) {
            return
        }
        settings.themeColor = key

        val mainIntent = Intent(this, com.example.barcodescanner.feature.tabs.BottomTabsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(mainIntent)
        startActivity(Intent(this, ChooseThemeActivity::class.java))
    }

    private fun themeColorName(key: String): String {
        return getString(
            when (key) {
                Settings.THEME_COLOR_TEAL -> R.string.activity_choose_theme_color_teal
                Settings.THEME_COLOR_GREEN -> R.string.activity_choose_theme_color_green
                Settings.THEME_COLOR_LIME -> R.string.activity_choose_theme_color_lime
                Settings.THEME_COLOR_AMBER -> R.string.activity_choose_theme_color_amber
                Settings.THEME_COLOR_ORANGE -> R.string.activity_choose_theme_color_orange
                Settings.THEME_COLOR_RED -> R.string.activity_choose_theme_color_red
                Settings.THEME_COLOR_PINK -> R.string.activity_choose_theme_color_pink
                else -> R.string.activity_choose_theme_color_blue
            }
        )
    }

    // ---------- 明暗模式（原有逻辑）----------

    private fun showInitialSettings() {
        val theme = settings.theme
        button_system_theme.isChecked = theme == Settings.THEME_SYSTEM
        button_light_theme.isChecked = theme == Settings.THEME_LIGHT
        button_dark_theme.isChecked = theme == Settings.THEME_DARK
    }

    private fun handleSettingsChanged() {
        button_system_theme.setCheckedChangedListener { isChecked ->
            if (isChecked.not()) {
                return@setCheckedChangedListener
            }

            uncheckOtherButtons(button_system_theme)
            settings.theme = Settings.THEME_SYSTEM
        }

        button_light_theme.setCheckedChangedListener { isChecked ->
            if (isChecked.not()) {
                return@setCheckedChangedListener
            }

            uncheckOtherButtons(button_light_theme)
            settings.theme = Settings.THEME_LIGHT
        }

        button_dark_theme.setCheckedChangedListener { isChecked ->
            if (isChecked.not()) {
                return@setCheckedChangedListener
            }

            uncheckOtherButtons(button_dark_theme)
            settings.theme = Settings.THEME_DARK
        }
    }

    private fun uncheckOtherButtons(checkedButton: View) {
        buttons.forEach { button ->
            if (checkedButton !== button) {
                button.isChecked = false
            }
        }
    }
}
