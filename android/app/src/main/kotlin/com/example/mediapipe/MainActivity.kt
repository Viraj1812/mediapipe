package com.example.mediapipe

import android.view.LayoutInflater
import android.view.View
import androidx.camera.view.PreviewView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hand_gesture_demo.CameraController
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall

import io.flutter.plugin.common.MethodChannel
class MainActivity: FlutterFragmentActivity() {
    private lateinit var methodChannelResult: MethodChannel.Result

//    private val getContent = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri : Uri? ->
//        lifecycleScope.launch {
//            withContext(Dispatchers.IO){
//                uri?.let { mediaUri ->
//                    methodChannelResult.success("$mediaUri")
//                }
//            }
//        }
//    }

//    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
//        flutterEngine.platformViewsController
//            .registry
//            .registerViewFactory(
//                "customLiveStreamView", CustomLiveStreamViewFactory(this))
//
//        super.configureFlutterEngine(flutterEngine)
//    }

//    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
//
//        flutterEngine.platformViewsController
//            .registry
//            .registerViewFactory(
//            "customImageView", CustomImageViewFactory(this))
//
//
//        MethodChannel(
//            flutterEngine.dartExecutor.binaryMessenger,
//            "pickImagePlatform"
//        ).setMethodCallHandler{ call, result ->
//            methodChannelResult = result
//            when (call.method){
//                "pickImage" -> {
//                    getContent.launch(arrayOf("image/*"))
//                }
//
//                else -> {
//                    result.notImplemented()
//                }
//            }
//        }
//
//        super.configureFlutterEngine(flutterEngine)

//    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
//        super.configureFlutterEngine(flutterEngine)
//        // INIT THE LAYOUT VIEW
//        val view: View = LayoutInflater.from(this).inflate(R.layout.camera_view, null)
//        flutterEngine
//            .platformViewsController
//            .registry
//            .registerViewFactory("<camera_view>", CameraViewFactory(view))
//        // HANDLE THE CAMERA FUNCTIONS
//        val cameraChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "CameraController")
//        val cameraPreview: PreviewView = view.findViewById(R.id.camera_view)
//        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
//        val cameraController = CameraViewController(this, this, cameraPreview)
//        val cameraManager = CameraManager()
//        cameraChannel.setMethodCallHandler { call: MethodCall, result: MethodChannel.Result ->
//            cameraManager.handle(call,result, cameraController)
//        }
//    }

    private lateinit var cameraController: CameraController

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Inflate the layout view
        val view: View = LayoutInflater.from(this).inflate(R.id.camera_view_layout, null)

        // Register the view factory
        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory("<camera_view>", CameraViewFactory(this, ViewModelProvider(this)[MainViewModel::class.java]))

        // Handle the camera functions
        val cameraChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "CameraController")
        val cameraPreview: PreviewView = view.findViewById(R.id.camera_view)
        val overlayView: OverlayView = view.findViewById(R.id.overlay)
        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Initialize CameraController
        cameraController = CameraController(this, this, cameraPreview, overlayView, viewModel)
//        val cameraManager = CameraManager()
//        cameraChannel.setMethodCallHandler { call: MethodCall, result: MethodChannel.Result ->
//            cameraManager.handle(call,result, cameraController)
//        }
    }




    override fun onDestroy() {
        super.onDestroy()
        cameraController.release()
    }

}

