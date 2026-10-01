package com.example.barcodescanner.usecase

import android.graphics.Bitmap
import com.example.barcodescanner.extension.orZero
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.schedulers.Schedulers

object BarcodeImageScanner {
    private var bitmapBuffer: IntArray? = null

    fun parse(image: Bitmap): Single<Result> {
        return Single
            .create<Result> { emitter ->
                parse(image, emitter)
            }
            .subscribeOn(Schedulers.newThread())
    }

    private fun parse(image: Bitmap, emitter: SingleEmitter<Result>) {
        try {
            emitter.onSuccess(tryParse(image))
        } catch (ex: Exception) {
            Logger.log(ex)
            emitter.onError(ex)
        }
    }

    private fun tryParse(image: Bitmap): Result {
        val width = image.width
        val height = image.height
        val size = width * height

        // 保留原有缓冲区复用逻辑，但改用非空局部变量：
        // compileSdk 34 的 android.jar 带 @NonNull 注解，Bitmap.getPixels 的参数已是非空 IntArray，
        // 不能再直接传可空字段（老代码在旧版 Kotlin/AGP 下靠"平台类型"蒙混过关）
        val buffer = bitmapBuffer?.takeIf { it.size >= size }
            ?: IntArray(size).also { bitmapBuffer = it }

        image.getPixels(buffer, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, buffer)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val reader = MultiFormatReader()
        return reader.decode(bitmap)
    }
}