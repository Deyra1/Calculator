package com.example.calculator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 计算器应用的广播接收器
 * 用于接收系统广播和自定义广播
 */
class CalculatorBroadcastReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // 接收自定义广播
            "com.example.calculator.CUSTOM_ACTION" -> {
                val message = intent.getStringExtra("message") ?: "无消息内容"
                val timestamp = intent.getLongExtra("timestamp", 0L)
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                
                Toast.makeText(
                    context, 
                    "收到广播: $message\n时间: $time", 
                    Toast.LENGTH_LONG
                ).show()
            }
            
            // 接收电池电量低广播
            Intent.ACTION_BATTERY_LOW -> {
                Toast.makeText(
                    context,
                    "电池电量低，请连接充电器",
                    Toast.LENGTH_LONG
                ).show()
            }
            
            // 接收充电器连接广播
            Intent.ACTION_POWER_CONNECTED -> {
                Toast.makeText(
                    context,
                    "充电器已连接",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
} 