package com.yourname.autodouyin

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.*
import kotlin.random.Random

class DouyinService : AccessibilityService() {

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("ACTION")
        if (action == "START") {
            startAutoSwipe()
        } else if (action == "STOP") {
            stopAutoSwipe()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startAutoSwipe() {
        if (job?.isActive == true) return
        
        job = scope.launch {
            while (isActive) {
                swipeUp()
                delay(Random.nextLong(500, 1500))

                if (Random.nextFloat() < 0.05f) {
                    doubleTapLike()
                }

                val watchTime = Random.nextLong(3000, 15000)
                delay(watchTime)

                if (Random.nextFloat() < 0.15f) {
                    delay(Random.nextLong(10000, 30000))
                }
            }
        }
    }

    private fun stopAutoSwipe() {
        job?.cancel()
    }

    private fun swipeUp() {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()

        val startX = width / 2f
        val startY = height * 0.7f
        val endX = width / 2f
        val endY = height * 0.3f

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val duration = Random.nextLong(200, 500)
        val stroke = GestureDescription.StrokeDescription(path, 0, duration)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        
        dispatchGesture(gesture, null, null)
    }

    private fun doubleTapLike() {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()

        val x1 = width * Random.nextDouble(0.80, 0.95).toFloat()
        val y1 = height * Random.nextDouble(0.12, 0.20).toFloat()
        
        val path1 = Path().apply { moveTo(x1, y1) }
        val path2 = Path().apply { moveTo(x1 + Random.nextInt(-25, 25), y1 + Random.nextInt(-25, 25)) }

        val builder = GestureDescription.Builder()
        builder.addStroke(GestureDescription.StrokeDescription(path1, 0, 50))
        builder.addStroke(GestureDescription.StrokeDescription(path2, 150, 50))

        dispatchGesture(builder.build(), null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { stopAutoSwipe() }
    override fun onDestroy() {
        super.onDestroy()
        stopAutoSwipe()
    }
}
