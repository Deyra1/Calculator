package com.example.calculator

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    
    private lateinit var broadcastReceiver: CalculatorBroadcastReceiver
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        
        // 获取传递过来的用户名
        val username = intent.getStringExtra("username") ?: "admin"
        
        // 设置用户名显示
        findViewById<TextView>(R.id.txtUsername).text = "用户：$username"
        
        // 返回按钮点击事件
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish() // 关闭当前Activity，返回上一个Activity
        }
        
        // 退出登录按钮点击事件
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // 返回登录界面
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
        }
        
        // 历史记录菜单项点击事件
        findViewById<LinearLayout>(R.id.menuItemHistory).setOnClickListener {
            Toast.makeText(this, "历史记录功能开发中", Toast.LENGTH_SHORT).show()
        }
        
        // 主题设置菜单项点击事件
        findViewById<LinearLayout>(R.id.menuItemTheme).setOnClickListener {
            Toast.makeText(this, "主题设置功能开发中", Toast.LENGTH_SHORT).show()
        }
        
        // 广播菜单项点击事件
        findViewById<LinearLayout>(R.id.menuItemBroadcast).setOnClickListener {
            // 发送一个自定义广播
            sendBroadcast(Intent("com.example.calculator.CUSTOM_ACTION").apply {
                putExtra("message", "这是一条来自计算器应用的广播消息")
                putExtra("timestamp", System.currentTimeMillis())
            })
            Toast.makeText(this, "广播已发送", Toast.LENGTH_SHORT).show()
        }
        
        // 关于我们菜单项点击事件
        findViewById<LinearLayout>(R.id.menuItemAbout).setOnClickListener {
            val aboutIntent = Intent(this, AboutActivity::class.java)
            startActivity(aboutIntent)
        }
        
        // 注册广播接收器
        broadcastReceiver = CalculatorBroadcastReceiver()
        val intentFilter = IntentFilter().apply {
            addAction("com.example.calculator.CUSTOM_ACTION")
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_POWER_CONNECTED)
        }
        registerReceiver(broadcastReceiver, intentFilter)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 注销广播接收器
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 