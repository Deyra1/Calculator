package com.example.calculator

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class CalculatorActivity : AppCompatActivity() {
    private lateinit var display: TextView
    private var currentInput = StringBuilder("0")
    private var currentOperation: String? = null
    private var firstOperand: Double? = null
    private var newNumber = true
    private lateinit var broadcastReceiver: CalculatorBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_calculator)
            
            // 初始化显示区域
            display = findViewById(R.id.display)
            
            // 初始化顶部功能区
            setupTopBar()
            
            // 初始化计算器按钮
            setupCalculatorButtons()
            
            // 初始化底部操作栏
            setupBottomBar()
            
            // 注册广播接收器
            broadcastReceiver = CalculatorBroadcastReceiver()
            val intentFilter = IntentFilter().apply {
                addAction("com.example.calculator.CUSTOM_ACTION")
                addAction(Intent.ACTION_BATTERY_LOW)
                addAction(Intent.ACTION_POWER_CONNECTED)
            }
            registerReceiver(broadcastReceiver, intentFilter)
        } catch (e: Exception) {
            // 如果布局加载失败，显示一个简单的后备视图
            setContentView(createFallbackView())
            Toast.makeText(this, "计算器加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
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
    
    private fun setupTopBar() {
        try {
            // 设置菜单按钮点击事件
            findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
                // 跳转到菜单页面
                val menuIntent = Intent(this, MenuActivity::class.java)
                // 传递当前登录的用户名
                menuIntent.putExtra("username", intent.getStringExtra("username") ?: "admin")
                startActivity(menuIntent)
            }
            
            // 设置选项卡点击事件
            setupTab(R.id.tabBasic, "基础")
            setupTab(R.id.tabScientific, "科学")
            setupTab(R.id.tabFraction, "分数")
            
            // 设置右侧功能按钮点击事件
            findViewById<ImageButton>(R.id.btnEdit)?.setOnClickListener {
                Toast.makeText(this, "编辑按钮点击", Toast.LENGTH_SHORT).show()
            }
            
            findViewById<ImageButton>(R.id.btnSettings)?.setOnClickListener {
                Toast.makeText(this, "设置按钮点击", Toast.LENGTH_SHORT).show()
            }
            
            findViewById<ImageButton>(R.id.btnMute)?.setOnClickListener {
                Toast.makeText(this, "静音按钮点击", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupTab(tabId: Int, tabName: String) {
        findViewById<TextView>(tabId)?.setOnClickListener {
            // 重置所有选项卡样式
            findViewById<TextView>(R.id.tabBasic)?.apply {
                setTextColor(resources.getColor(android.R.color.darker_gray))
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
            
            findViewById<TextView>(R.id.tabScientific)?.apply {
                setTextColor(resources.getColor(android.R.color.darker_gray))
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
            
            findViewById<TextView>(R.id.tabFraction)?.apply {
                setTextColor(resources.getColor(android.R.color.darker_gray))
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
            
            // 设置当前选项卡的样式
            findViewById<TextView>(tabId)?.apply {
                setTextColor(resources.getColor(android.R.color.white))
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            
            Toast.makeText(this, "$tabName 模式已选择", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupBottomBar() {
        try {
            // 设置"继续计算"按钮点击事件
            findViewById<Button>(R.id.btnContinueCalc)?.setOnClickListener {
                Toast.makeText(this, "继续计算", Toast.LENGTH_SHORT).show()
            }
            
            // 设置"清除记录"按钮点击事件
            findViewById<Button>(R.id.btnClearHistory)?.setOnClickListener {
                Toast.makeText(this, "已清除所有记录", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupCalculatorButtons() {
        try {
            // 数字按钮
            setupNumberButton(R.id.btn0, "0")
            setupNumberButton(R.id.btn1, "1")
            setupNumberButton(R.id.btn2, "2")
            setupNumberButton(R.id.btn3, "3")
            setupNumberButton(R.id.btn4, "4")
            setupNumberButton(R.id.btn5, "5")
            setupNumberButton(R.id.btn6, "6")
            setupNumberButton(R.id.btn7, "7")
            setupNumberButton(R.id.btn8, "8")
            setupNumberButton(R.id.btn9, "9")
            setupNumberButton(R.id.btnDot, ".")

            // 基本运算符
            setupOperationButton(R.id.btnPlus, "+")
            setupOperationButton(R.id.btnMinus, "-")
            setupOperationButton(R.id.btnMultiply, "×")
            setupOperationButton(R.id.btnEquals, "=")

            // 清除按钮
            findViewById<Button>(R.id.btnClear)?.setOnClickListener {
                currentInput = StringBuilder("0")
                currentOperation = null
                firstOperand = null
                display.text = "0"
                newNumber = true
            }
            
            // 删除按钮
            findViewById<Button>(R.id.btnDelete)?.setOnClickListener {
                if (currentInput.isNotEmpty()) {
                    if (currentInput.length == 1) {
                        currentInput = StringBuilder("0")
                    } else {
                        currentInput.deleteCharAt(currentInput.length - 1)
                    }
                    display.text = currentInput.toString()
                }
            }
            
            // 科学计算按钮
            findViewById<Button>(R.id.btnSin)?.setOnClickListener {
                Toast.makeText(this, "Sin功能待实现", Toast.LENGTH_SHORT).show()
            }
            
            findViewById<Button>(R.id.btnCos)?.setOnClickListener {
                Toast.makeText(this, "Cos功能待实现", Toast.LENGTH_SHORT).show()
            }
            
            findViewById<Button>(R.id.btnTan)?.setOnClickListener {
                Toast.makeText(this, "Tan功能待实现", Toast.LENGTH_SHORT).show()
            }
            
            findViewById<Button>(R.id.btnLn)?.setOnClickListener {
                Toast.makeText(this, "Ln功能待实现", Toast.LENGTH_SHORT).show()
            }
            
            findViewById<Button>(R.id.btnLog)?.setOnClickListener {
                Toast.makeText(this, "Log功能待实现", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "按钮初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun setupNumberButton(buttonId: Int, number: String) {
        try {
            findViewById<Button>(buttonId)?.setOnClickListener {
                if (newNumber || currentInput.toString() == "0") {
                    currentInput = StringBuilder(number)
                    newNumber = false
                } else {
                    currentInput.append(number)
                }
                display.text = currentInput.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupOperationButton(buttonId: Int, operation: String) {
        try {
            findViewById<Button>(buttonId)?.setOnClickListener {
                if (currentInput.isNotEmpty()) {
                    try {
                        val number = currentInput.toString().toDouble()
                        if (operation == "=") {
                            if (firstOperand != null && currentOperation != null) {
                                val result = performOperation(firstOperand!!, number, currentOperation!!)
                                display.text = formatResult(result)
                                currentInput = StringBuilder(display.text)
                                firstOperand = null
                                currentOperation = null
                                
                                // 将计算结果添加到历史记录（仅作为示例，未实际实现）
                                Toast.makeText(this, "计算结果已添加到历史记录", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            firstOperand = number
                            currentOperation = operation
                            newNumber = true
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "计算错误: ${e.message}", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performOperation(a: Double, b: Double, operation: String): Double {
        return when (operation) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            else -> b
        }
    }

    private fun formatResult(result: Double): String {
        return if (result == result.toInt().toDouble()) {
            result.toInt().toString()
        } else {
            // 限制小数位数以避免显示过长
            String.format("%.6f", result).trimEnd('0').trimEnd('.')
        }
    }
    
    // 创建一个简单的后备视图，以防主视图加载失败
    private fun createFallbackView(): TextView {
        val textView = TextView(this)
        textView.text = "计算器界面加载失败，请返回重试"
        textView.textSize = 20f
        textView.setPadding(50, 50, 50, 50)
        return textView
    }
} 