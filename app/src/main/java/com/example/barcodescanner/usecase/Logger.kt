package com.example.barcodescanner.usecase

import android.util.Log
import com.example.barcodescanner.BuildConfig

/**
 * 本地日志。
 *
 * 原实现把异常上报到 Sentry（upstream 作者的 DSN）。本 fork 已整体移除 Sentry SDK：
 * ① 不应把使用者机器的崩溃数据发到第三方项目；② Sentry 的 SentryInitProvider 会在
 * 进程启动时自动初始化，缺少 DSN 会直接抛 IllegalArgumentException（实测闪退元凶）。
 * 现在只写本机 logcat，TAG 保持包名便于过滤：adb logcat -s com.example.barcodescanner
 */
object Logger {
    private const val TAG = "com.example.barcodescanner"

    var isEnabled = BuildConfig.ERROR_REPORTS_ENABLED_BY_DEFAULT

    fun log(error: Throwable) {
        if (isEnabled) {
            Log.e(TAG, error.message ?: error.toString(), error)
        }
    }
}
