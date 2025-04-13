package com.example.calculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    // 定义固定的用户名和密码
    private val USERNAME = "admin"
    private val PASSWORD = "123"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_login)
            
            // 找到用户名和密码输入框以及登录按钮
            val usernameInput = findViewById<EditText>(R.id.usernameInput)
            val passwordInput = findViewById<EditText>(R.id.passwordInput)
            val loginButton = findViewById<Button>(R.id.loginButton)
            
            // 设置点击监听器
            loginButton.setOnClickListener {
                try {
                    val username = usernameInput.text.toString()
                    val password = passwordInput.text.toString()
                    
                    // 验证用户名和密码
                    if (username == USERNAME && password == PASSWORD) {
                        // 验证成功，跳转到计算器界面
                        val intent = Intent(this, CalculatorActivity::class.java)
                        // 传递用户名
                        intent.putExtra("username", username)
                        startActivity(intent)
                        Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show()
                    } else {
                        // 验证失败，显示错误信息
                        Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "登录失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            // 如果布局加载失败，创建一个简单的后备布局
            setContentView(createFallbackView())
            e.printStackTrace()
        }
    }
    
    // 创建一个简单的后备视图，以防主视图加载失败
    private fun createFallbackView(): Button {
        val button = Button(this)
        button.text = "进入计算器"
        button.setOnClickListener {
            try {
                val intent = Intent(this, CalculatorActivity::class.java)
                intent.putExtra("username", USERNAME)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "跳转失败: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
        return button
    }
} 