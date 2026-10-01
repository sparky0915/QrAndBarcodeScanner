package com.example.barcodescanner.usecase

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.barcodescanner.BuildConfig
import com.example.barcodescanner.R
import com.example.barcodescanner.extension.unsafeLazy
import com.example.barcodescanner.model.SearchEngine
import com.google.zxing.BarcodeFormat

class Settings(private val context: Context) {

    companion object {
        const val THEME_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        const val THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO
        const val THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES

        // 主题色（Material You 风格）：
        // THEME_COLOR_DYNAMIC = 跟随系统动态取色（Monet，Android 12+）；
        // 其余为内置色板，由 res/values/styles.xml 里的 ThemeOverlay.Lj.* 提供 M3 色阶。
        const val THEME_COLOR_DYNAMIC = "dynamic"
        const val THEME_COLOR_BLUE = "blue"
        const val THEME_COLOR_TEAL = "teal"
        const val THEME_COLOR_GREEN = "green"
        const val THEME_COLOR_LIME = "lime"
        const val THEME_COLOR_AMBER = "amber"
        const val THEME_COLOR_ORANGE = "orange"
        const val THEME_COLOR_RED = "red"
        const val THEME_COLOR_PINK = "pink"
        const val THEME_COLOR_CUSTOM = "custom"

        /** 界面上色点展示顺序 */
        val THEME_COLOR_PRESETS = listOf(
            THEME_COLOR_BLUE,
            THEME_COLOR_TEAL,
            THEME_COLOR_GREEN,
            THEME_COLOR_LIME,
            THEME_COLOR_AMBER,
            THEME_COLOR_ORANGE,
            THEME_COLOR_RED,
            THEME_COLOR_PINK,
        )

        /**
         * 各预设色板对应的"种子色"。
         * 实际配色由 Material 的 content-based DynamicColors 按种子生成整套 M3 色阶
         * （HCT 算法，与系统 Monet 同源），所以界面上色点显示的是种子色，
         * 落到控件上的 primary 会是同色系的色调调整值 —— 这正是 M3 的正常表现。
         */
        @androidx.annotation.ColorInt
        fun themeColorSeedRes(themeColor: String): Int {
            return when (themeColor) {
                THEME_COLOR_TEAL -> R.color.lj_teal_primary
                THEME_COLOR_GREEN -> R.color.lj_green_primary
                THEME_COLOR_LIME -> R.color.lj_lime_primary
                THEME_COLOR_AMBER -> R.color.lj_amber_primary
                THEME_COLOR_ORANGE -> R.color.lj_orange_primary
                THEME_COLOR_RED -> R.color.lj_red_primary
                THEME_COLOR_PINK -> R.color.lj_pink_primary
                else -> R.color.lj_blue_primary
            }
        }

        /** 色点在界面上显示的预览色 */
        @androidx.annotation.ColorRes
        fun themeColorPreviewRes(themeColor: String): Int {
            return when (themeColor) {
                THEME_COLOR_TEAL -> R.color.lj_teal_primary
                THEME_COLOR_GREEN -> R.color.lj_green_primary
                THEME_COLOR_LIME -> R.color.lj_lime_primary
                THEME_COLOR_AMBER -> R.color.lj_amber_primary
                THEME_COLOR_ORANGE -> R.color.lj_orange_primary
                THEME_COLOR_RED -> R.color.lj_red_primary
                THEME_COLOR_PINK -> R.color.lj_pink_primary
                else -> R.color.lj_blue_primary
            }
        }

        private const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME"
        private var INSTANCE: Settings? = null

        fun getInstance(context: Context): Settings {
            return INSTANCE ?: Settings(context.applicationContext).apply { INSTANCE = this }
        }
    }

    private enum class Key {
        THEME,
        THEME_COLOR,
        THEME_COLOR_CUSTOM,
        INVERSE_BARCODE_COLORS,
        OPEN_LINKS_AUTOMATICALLY,
        COPY_TO_CLIPBOARD,
        SIMPLE_AUTO_FOCUS,
        FLASHLIGHT,
        VIBRATE,
        CONTINUOUS_SCANNING,
        CONFIRM_SCANS_MANUALLY,
        IS_BACK_CAMERA,
        SAVE_SCANNED_BARCODES_TO_HISTORY,
        SAVE_CREATED_BARCODES_TO_HISTORY,
        DO_NOT_SAVE_DUPLICATES,
        SEARCH_ENGINE,
        ERROR_REPORTS,
    }

    private val sharedPreferences by unsafeLazy {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    var theme: Int
        get() = get(Key.THEME, THEME_SYSTEM)
        set(value) {
            set(Key.THEME, value)
            applyTheme(value)
        }

    val isDarkTheme: Boolean
        get() = theme == THEME_DARK || (theme == THEME_SYSTEM && isSystemDarkModeEnabled())

    /**
     * 主题色。默认 blue（即原 App 的蓝色强调色，保证与旧版观感一致）。
     * 改这个值后需要让界面重建（Activity.recreate()）才会生效。
     */
    var themeColor: String
        get() = get(Key.THEME_COLOR, THEME_COLOR_BLUE)
        set(value) = set(Key.THEME_COLOR, value)

    /** 自定义取色（THEME_COLOR == "custom" 时生效），存 ARGB 整数 */
    var themeColorCustom: Int
        get() = get(Key.THEME_COLOR_CUSTOM, Color.parseColor("#1685A9"))
        set(value) = set(Key.THEME_COLOR_CUSTOM, value)

    /** 当前主题色对应的种子色；THEME_COLOR_DYNAMIC 无种子（跟随系统壁纸） */
    @get:androidx.annotation.ColorInt
    val themeColorSeed: Int
        get() = if (themeColor == THEME_COLOR_CUSTOM) {
            themeColorCustom
        } else {
            ContextCompat.getColor(context, themeColorSeedRes(themeColor))
        }

    var areBarcodeColorsInversed: Boolean
        get() = get(Key.INVERSE_BARCODE_COLORS, false)
        set(value) = set(Key.INVERSE_BARCODE_COLORS, value)

    val barcodeContentColor: Int
        get() = when  {
            isDarkTheme && areBarcodeColorsInversed -> Color.WHITE
            else -> Color.BLACK
        }

    val barcodeBackgroundColor: Int
        get() = when {
            isDarkTheme && areBarcodeColorsInversed.not() -> Color.WHITE
            else -> Color.TRANSPARENT
        }

    var openLinksAutomatically: Boolean
        get() = get(Key.OPEN_LINKS_AUTOMATICALLY, false)
        set(value) = set(Key.OPEN_LINKS_AUTOMATICALLY, value)

    var copyToClipboard: Boolean
        get() = get(Key.COPY_TO_CLIPBOARD, true)
        set(value) = set(Key.COPY_TO_CLIPBOARD, value)

    var simpleAutoFocus: Boolean
        get() = get(Key.SIMPLE_AUTO_FOCUS, false)
        set(value) = set(Key.SIMPLE_AUTO_FOCUS, value)

    var flash: Boolean
        get() = get(Key.FLASHLIGHT, false)
        set(value) = set(Key.FLASHLIGHT, value)

    var vibrate: Boolean
        get() = get(Key.VIBRATE, true)
        set(value) = set(Key.VIBRATE, value)

    var continuousScanning: Boolean
        get() = get(Key.CONTINUOUS_SCANNING, false)
        set(value) = set(Key.CONTINUOUS_SCANNING, value)

    var confirmScansManually: Boolean
        get() = get(Key.CONFIRM_SCANS_MANUALLY, false)
        set(value) = set(Key.CONFIRM_SCANS_MANUALLY, value)

    var isBackCamera: Boolean
        get() = get(Key.IS_BACK_CAMERA, true)
        set(value) = set(Key.IS_BACK_CAMERA, value)

    var saveScannedBarcodesToHistory: Boolean
        get() = get(Key.SAVE_SCANNED_BARCODES_TO_HISTORY, true)
        set(value) = set(Key.SAVE_SCANNED_BARCODES_TO_HISTORY, value)

    var saveCreatedBarcodesToHistory: Boolean
        get() = get(Key.SAVE_CREATED_BARCODES_TO_HISTORY, true)
        set(value) = set(Key.SAVE_CREATED_BARCODES_TO_HISTORY, value)

    var doNotSaveDuplicates: Boolean
        get() = get(Key.DO_NOT_SAVE_DUPLICATES, false)
        set(value) = set(Key.DO_NOT_SAVE_DUPLICATES, value)

    var searchEngine: SearchEngine
        get() = get(Key.SEARCH_ENGINE, SearchEngine.NONE)
        set(value) = set(Key.SEARCH_ENGINE, value)

    var areErrorReportsEnabled: Boolean
        get() = get(Key.ERROR_REPORTS, BuildConfig.ERROR_REPORTS_ENABLED_BY_DEFAULT)
        set(value) {
            set(Key.ERROR_REPORTS, value)
            Logger.isEnabled = value
        }

    fun isFormatSelected(format: BarcodeFormat): Boolean {
        return sharedPreferences.getBoolean(format.name, true)
    }

    fun setFormatSelected(format: BarcodeFormat, isSelected: Boolean) {
        sharedPreferences.edit()
            .putBoolean(format.name, isSelected)
            .apply()
    }

    fun reapplyTheme() {
        applyTheme(theme)
    }

    private fun get(key: Key, default: Int): Int {
        return sharedPreferences.getInt(key.name, default)
    }

    private fun set(key: Key, value: Int) {
        return sharedPreferences.edit()
            .putInt(key.name, value)
            .apply()
    }

    private fun get(key: Key, default: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key.name, default)
    }

    private fun set(key: Key, value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(key.name, value)
            .apply()
    }

    private fun get(key: Key, default: SearchEngine = SearchEngine.NONE): SearchEngine {
        val rawValue = sharedPreferences.getString(key.name, null) ?: default.name
        return SearchEngine.valueOf(rawValue)
    }

    private fun set(key: Key, value: SearchEngine) {
        sharedPreferences.edit()
            .putString(key.name, value.name)
            .apply()
    }

    private fun get(key: Key, default: String): String {
        return sharedPreferences.getString(key.name, default) ?: default
    }

    private fun set(key: Key, value: String) {
        sharedPreferences.edit()
            .putString(key.name, value)
            .apply()
    }

    private fun applyTheme(theme: Int) {
        when (theme) {
            AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(theme)
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }

    private fun isSystemDarkModeEnabled(): Boolean {
        val mode = context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        return mode == Configuration.UI_MODE_NIGHT_YES
    }
}