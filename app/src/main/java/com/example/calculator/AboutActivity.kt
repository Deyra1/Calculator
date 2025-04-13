package com.example.calculator

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        
        // 设置返回按钮点击事件
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish() // 关闭当前Activity
        }
    }
} 