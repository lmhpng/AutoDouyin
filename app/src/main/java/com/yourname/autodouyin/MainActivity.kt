package com.yourname.autodouyin

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 200, 50, 50)
        }

        val btnPermission = Button(this).apply {
            text = "1. 去开启无障碍服务权限"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        val btnToggle = Button(this).apply {
            text = "2. 启动/停止 刷视频"
            setOnClickListener {
                val intent = Intent(this@MainActivity, DouyinService::class.java)
                if (!isRunning) {
                    intent.putExtra("ACTION", "START")
                    startService(intent)
                    text = "运行中... (点击停止)"
                    isRunning = true
                    Toast.makeText(context, "已发送启动指令，请切回短视频APP", Toast.LENGTH_SHORT).show()
                } else {
                    intent.putExtra("ACTION", "STOP")
                    startService(intent)
                    text = "2. 启动/停止 刷视频"
                    isRunning = false
                }
            }
        }

        layout.addView(btnPermission)
        layout.addView(btnToggle)
        setContentView(layout)
    }
}
