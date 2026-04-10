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
                // 第一步：向上滑到一个新视频
                swipeUp()
                
                // 第二步：雷打不动地花费 2 秒钟等待，让画面和文字完全加载出来
                delay(2000)

                // 第三步：判断是不是广告或者直播
                if (isAdOrLive()) {
                    // 如果有“广告”或“直播中”这些字词，直接进入下一次循环（即马上再执行一次上滑）
                    continue 
                }

                // ==========================================
                // 第四步：如果没有这些字词，说明是纯视频，开始执行观看和点赞策略
                // ==========================================
                
                // 执行 10% 概率的双击点赞策略
                if (Random.nextFloat() < 0.10f) {
                    doubleTapLike()
                }

                // 执行随机观看纯视频 3-15 秒钟
                val watchTime = Random.nextLong(3000, 15000)
                delay(watchTime)

                // 保留之前的随机休息策略：15% 的概率随机休息 10-30 秒，防封号
                if (Random.nextFloat() < 0.15f) {
                    delay(Random.nextLong(10000, 30000))
                }
            }
        }
    }

    // ========== 极简精准词库检测 ==========
    private fun isAdOrLive(): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        // 按照你的要求，严格缩减字词，只抓最明确的“广告”和“直播”，防止误伤正常视频
        val badWords = listOf("广告", "直播中")
        return searchNodesForText(rootNode, badWords)
    }

    private fun searchNodesForText(node: AccessibilityNodeInfo?, keywords: List<String>): Boolean {
        if (node == null) return false
        
        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""
        
        for (word in keywords) {
            // 只要包含敏感词，立刻判定为广告/直播
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

        // 依然保持你原本专属的右侧偏上点赞区域
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
