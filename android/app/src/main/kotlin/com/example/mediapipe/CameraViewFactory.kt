package com.example.mediapipe

import android.content.Context
import android.view.View
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.hand_gesture_demo.CameraController
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class CameraViewFactory(private val lifecycleOwner: LifecycleOwner, private val viewModel: MainViewModel) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        val creationParams = args as Map<String?, Any?>?
        val view = View.inflate(context, R.id.camera_view_layout, null)
        return CameraView(context!!, viewId, creationParams, view, lifecycleOwner, viewModel)
    }
}

internal class CameraView(
    context: Context,
    id: Int,
    creationParams: Map<String?, Any?>?,
    view: View,
    lifecycleOwner: LifecycleOwner,
    viewModel: MainViewModel
) : PlatformView {

    private val cameraPreview: PreviewView = view.findViewById(R.id.camera_view)
    private val overlayView: OverlayView = view.findViewById(R.id.overlay)
    private val cameraController: CameraController

    init {
        // Initialize the CameraController
        cameraController = CameraController(context, lifecycleOwner, cameraPreview, overlayView, viewModel)
        cameraController.startCamera()
    }

    override fun getView(): View {
        return cameraPreview
    }

    override fun dispose() {
        // Stop and release the camera resources when disposed
        cameraController.stopCamera()
        cameraController.release()
    }
}
