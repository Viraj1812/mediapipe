package com.example.mediapipe

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity: FlutterFragmentActivity() {
    private lateinit var methodChannelResult: MethodChannel.Result

    private val getContent = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri : Uri? ->
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                uri?.let { mediaUri ->
                    methodChannelResult.success("$mediaUri")
                }
            }
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {

        flutterEngine.platformViewsController
            .registry
            .registerViewFactory(
            "customImageView", CustomImageViewFactory(this))


        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "pickImagePlatform"
        ).setMethodCallHandler{ call, result ->
            methodChannelResult = result
            when (call.method){
                "pickImage" -> {
                    getContent.launch(arrayOf("image/*"))
                }

                else -> {
                    result.notImplemented()
                }
            }
        }

        super.configureFlutterEngine(flutterEngine)

    }

}
