package com.example.mediapipe
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.mediapipe.R
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: HandLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()

    var resultChangeListener: ResultChangeListener? = null
    private var scaleFactor: Float = 1f
    private var imageWidth: Float = 0.5f
    private var imageHeight: Float = 0.5f
    var result : String = "Let's Play"


    init {
        initPaints()
    }

    fun clear() {
        results = null
        linePaint.reset()
        pointPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { handLandmarkerResult ->
            var handDetected = false
            for (landmark in handLandmarkerResult.landmarks()) {
                val point_x = mutableListOf<Float>()
                val point_y = mutableListOf<Float>()
                for (normalizedLandmark in landmark) {

                    val x = normalizedLandmark.x() * imageWidth * scaleFactor
                    val y = normalizedLandmark.y() * imageHeight * scaleFactor

                    point_x.add(x)
                    point_y.add(y)
                    handDetected = true
                }

                Log.d("Point_x","${point_x}")
                Log.d("Point_y","${point_y}")
                val temp = findHandGesture(point_x,point_y)

                if(temp != result)
                {
                    result = temp
                    resultChangeListener?.onResultChanged(result)
                }

                for (normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }

                HandLandmarker.HAND_CONNECTIONS.forEach {
                    canvas.drawLine(
                        landmark.get(it!!.start())
                            .x() * imageWidth * scaleFactor,
                        landmark.get(it.start())
                            .y() * imageHeight * scaleFactor,
                        landmark.get(it.end())
                            .x() * imageWidth * scaleFactor,
                        landmark.get(it.end())
                            .y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }

            }
            if (!handDetected && result != "Let's Play") {
                result = "Let's Play"
                resultChangeListener?.onResultChanged(result)
            }


        }
    }

    fun setResults(
        handLandmarkerResults: HandLandmarkerResult,
        imageHeight: Float,
        imageWidth: Float,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = handLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
    }

    private fun findHandGesture(positions_X: List<Float>, positions_Y: List<Float>): String {
        val dist = positions_X.indices.map { i ->
            (positions_X[i] - positions_X[0]) * (positions_X[i] - positions_X[0]) +
                    (positions_Y[i] - positions_Y[0]) * (positions_Y[i] - positions_Y[0])
        }

        if (dist[6] > dist[8] && dist[10] > dist[12] && dist[14] > dist[16] && dist[18] > dist[20]) {
            return "Stone"
        } else if (dist[8] > dist[6] && dist[12] > dist[10] && dist[18] > dist[20] && dist[14] > dist[16]) {
            return "Scissor"
        } else if (dist[8] > dist[6] && dist[12] > dist[10] && dist[16] > dist[14] && dist[20] > dist[18]) {
            return "Paper"
        }
        return ""
    }

    interface ResultChangeListener {
        fun onResultChanged(result: String)
    }
}
