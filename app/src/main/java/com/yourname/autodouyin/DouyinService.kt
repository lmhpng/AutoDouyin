package com.yourname.autodouyin

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
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
                // 1. 向上滑动
                swipeUp()
                
                // 2. 等待 UI 加载出来 (稍微多等一点，让系统有时间读到广告标签)
                delay(Random.nextLong(1500, 2500))

                // 3. 智能检测全屏广告/团购/直播
                if (isAdOrLive()) {
                    // 如果发现是广告，直接跳过当前循环，马上滑走！
                    continue 
                }

                // 4. 你的 10% 概率双击点赞
                if (Random.nextFloat() < 0.10f) {
                    doubleTapLike()
                }

                // 5. 正常观看视频
                val watchTime = Random.nextLong(3000, 15000)
                delay(watchTime)

                // 6. 15% 概率休息
                if (Random.nextFloat() < 0.15f) {
                    delay(Random.nextLong(10000, 30000))
                }
            }
        }
    }

    // ========== “透视眼”功能：扫描屏幕文字 ==========
    private fun isAdOrLive(): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        // 遇到这些词直接滑走
        val badWords = listOf("广告", "跳过", "直播中", "点击进入", "去购买", "团购", "立即下载", "查看详情")
        return searchNodesForText(rootNode, badWords)
    }

    private fun searchNodesForText(node: AccessibilityNodeInfo?, keywords: List<String>): Boolean {
        if (node == null) return false
        
        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""
        
        for (word in keywords) {
            if (text.contains(word) || desc.contains(word)) {
                return true
            }
        }
        
        for (i in 0 until node.childCount) {
            if (searchNodesForText(node.getChild(i), keywords)) {
                return true
            }
        }
        return false
    }
    // ====================================================

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

        // 恢复为你原本专属的点赞区域 (右上角偏下)
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
