package com.example.mediapipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import io.flutter.plugin.platform.PlatformView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class CustomImageView(
    private val context: Context,
    id: Int,
    creationParams: Map<String, Any>?,
    private val activity: MainActivity
) : PlatformView {

    private val constrainLayout: ConstraintLayout = ConstraintLayout(context)
    private val previewView: PreviewView = PreviewView(context)
    private val overlayView = OverlayView(context, null)

    private val baseOperationsBuilder = BaseOptions.builder().setModelAssetPath("hand_landmarker.task")
    private val baseOptions = baseOperationsBuilder.build()

    private val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
        .setBaseOptions(baseOptions)
        .setMinHandDetectionConfidence(0.5f)
        .setMinTrackingConfidence(0.5f)
        .setMinHandPresenceConfidence(0.5f)
        .setNumHands(2)
        .setRunningMode(RunningMode.LIVE_STREAM)

    private val options = optionsBuilder.build()
    private var handLandmarker: HandLandmarker? = null

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        handLandmarker = HandLandmarker.createFromOptions(context, options)

        constrainLayout.id = View.generateViewId()
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        constrainLayout.layoutParams = layoutParams

        previewView.id = View.generateViewId()
        constrainLayout.addView(previewView)

        overlayView.id = View.generateViewId()
        constrainLayout.addView(overlayView)

        startCamera()

        activity.lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                startCamera()
            }
        }
    }

    override fun getView(): View {
        return constrainLayout
    }

    override fun dispose() {
        cameraExecutor.shutdown()
        handLandmarker?.close()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, { imageProxy ->
                    processImageProxy(imageProxy)
                })
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(activity as LifecycleOwner, cameraSelector, preview, imageAnalyzer)
        } catch (e: Exception) {
            Log.e("CustomImageView", "Use case binding failed", e)
        }
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val bitmap = mediaImage.toBitmap()
        val mpImage = BitmapImageBuilder(bitmap)(rotationDegrees).build()

        val result = handLandmarker?.detect(mpImage)
        result?.let {
            overlayView.setResults(it, bitmap.height, bitmap.width)
        }

        imageProxy.close()
    }

    private fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
