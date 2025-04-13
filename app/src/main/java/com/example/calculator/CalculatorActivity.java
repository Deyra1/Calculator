package com.example.calculator;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CalculatorActivity extends AppCompatActivity {
    private TextView display;
    private StringBuilder currentInput = new StringBuilder("0");
    private String currentOperation = null;
    private Double firstOperand = null;
    private boolean newNumber = true;
    private CalculatorBroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_calculator);
            
            // 初始化显示区域
            display = findViewById(R.id.display);
            
            // 初始化顶部功能区
            setupTopBar();
            
            // 初始化计算器按钮
            setupCalculatorButtons();
            
            // 初始化底部操作栏
            setupBottomBar();
            
            // 注册广播接收器
            broadcastReceiver = new CalculatorBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.example.calculator.CUSTOM_ACTION");
            intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
            intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            registerReceiver(broadcastReceiver, intentFilter);
        } catch (Exception e) {
            // 如果布局加载失败，显示一个简单的后备视图
            setContentView(createFallbackView());
            Toast.makeText(this, "计算器加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupTopBar() {
        try {
            // 设置菜单按钮点击事件
            ImageButton btnMenu = findViewById(R.id.btnMenu);
            if (btnMenu != null) {
                btnMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 跳转到菜单页面
                        Intent menuIntent = new Intent(CalculatorActivity.this, MenuActivity.class);
                        // 传递当前登录的用户名
                        menuIntent.putExtra("username", getIntent().getStringExtra("username") != null ? 
                            getIntent().getStringExtra("username") : "admin");
                        startActivity(menuIntent);
                    }
                });
            }
            
            // 设置选项卡点击事件
            setupTab(R.id.tabBasic, "基础");
            setupTab(R.id.tabScientific, "科学");
            setupTab(R.id.tabFraction, "分数");
            
            // 设置右侧功能按钮点击事件
            setupFunctionButton(R.id.btnEdit, "编辑按钮点击");
            setupFunctionButton(R.id.btnSettings, "设置按钮点击");
            setupFunctionButton(R.id.btnMute, "静音按钮点击");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupFunctionButton(int buttonId, final String message) {
        ImageButton button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(CalculatorActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void setupTab(int tabId, final String tabName) {
        TextView tabView = findViewById(tabId);
        if (tabView != null) {
            tabView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 重置所有选项卡样式
                    TextView tabBasic = findViewById(R.id.tabBasic);
                    if (tabBasic != null) {
                        tabBasic.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        tabBasic.setTextSize(18);
                        tabBasic.setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                    
                    TextView tabScientific = findViewById(R.id.tabScientific);
                    if (tabScientific != null) {
                        tabScientific.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        tabScientific.setTextSize(18);
                        tabScientific.setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                    
                    TextView tabFraction = findViewById(R.id.tabFraction);
                    if (tabFraction != null) {
                        tabFraction.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        tabFraction.setTextSize(18);
                        tabFraction.setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                    
                    // 设置当前选项卡的样式
                    TextView currentTab = findViewById(tabId);
                    if (currentTab != null) {
                        currentTab.setTextColor(getResources().getColor(android.R.color.white));
                        currentTab.setTextSize(18);
                        currentTab.setTypeface(null, android.graphics.Typeface.BOLD);
                    }
                    
                    Toast.makeText(CalculatorActivity.this, tabName + " 模式已选择", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void setupBottomBar() {
        try {
            // 设置"继续计算"按钮点击事件
            Button btnContinueCalc = findViewById(R.id.btnContinueCalc);
            if (btnContinueCalc != null) {
                btnContinueCalc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(CalculatorActivity.this, "继续计算", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            // 设置"清除记录"按钮点击事件
            Button btnClearHistory = findViewById(R.id.btnClearHistory);
            if (btnClearHistory != null) {
                btnClearHistory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(CalculatorActivity.this, "已清除所有记录", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupCalculatorButtons() {
        try {
            // 数字按钮
            setupNumberButton(R.id.btn0, "0");
            setupNumberButton(R.id.btn1, "1");
            setupNumberButton(R.id.btn2, "2");
            setupNumberButton(R.id.btn3, "3");
            setupNumberButton(R.id.btn4, "4");
            setupNumberButton(R.id.btn5, "5");
            setupNumberButton(R.id.btn6, "6");
            setupNumberButton(R.id.btn7, "7");
            setupNumberButton(R.id.btn8, "8");
            setupNumberButton(R.id.btn9, "9");
            setupNumberButton(R.id.btnDot, ".");

            // 基本运算符
            setupOperationButton(R.id.btnPlus, "+");
            setupOperationButton(R.id.btnMinus, "-");
            setupOperationButton(R.id.btnMultiply, "×");
            setupOperationButton(R.id.btnEquals, "=");

            // 清除按钮
            Button btnClear = findViewById(R.id.btnClear);
            if (btnClear != null) {
                btnClear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentInput = new StringBuilder("0");
                        currentOperation = null;
                        firstOperand = null;
                        display.setText("0");
                        newNumber = true;
                    }
                });
            }
            
            // 删除按钮
            Button btnDelete = findViewById(R.id.btnDelete);
            if (btnDelete != null) {
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentInput.length() > 0) {
                            if (currentInput.length() == 1) {
                                currentInput = new StringBuilder("0");
                            } else {
                                currentInput.deleteCharAt(currentInput.length() - 1);
                            }
                            display.setText(currentInput.toString());
                        }
                    }
                });
            }
            
            // 科学计算按钮
            setupScienceButton(R.id.btnSin, "Sin功能待实现");
            setupScienceButton(R.id.btnCos, "Cos功能待实现");
            setupScienceButton(R.id.btnTan, "Tan功能待实现");
            setupScienceButton(R.id.btnLn, "Ln功能待实现");
            setupScienceButton(R.id.btnLog, "Log功能待实现");
        } catch (Exception e) {
            Toast.makeText(this, "按钮初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setupScienceButton(int buttonId, final String message) {
        Button button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(CalculatorActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupNumberButton(int buttonId, final String number) {
        try {
            Button button = findViewById(buttonId);
            if (button != null) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (newNumber || currentInput.toString().equals("0")) {
                            currentInput = new StringBuilder(number);
                            newNumber = false;
                        } else {
                            currentInput.append(number);
                        }
                        display.setText(currentInput.toString());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupOperationButton(int buttonId, final String operation) {
        try {
            Button button = findViewById(buttonId);
            if (button != null) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentInput.length() > 0) {
                            try {
                                double number = Double.parseDouble(currentInput.toString());
                                if (operation.equals("=")) {
                                    if (firstOperand != null && currentOperation != null) {
                                        double result = performOperation(firstOperand, number, currentOperation);
                                        display.setText(formatResult(result));
                                        currentInput = new StringBuilder(display.getText());
                                        firstOperand = null;
                                        currentOperation = null;
                                        
                                        // 将计算结果添加到历史记录（仅作为示例，未实际实现）
                                        Toast.makeText(CalculatorActivity.this, "计算结果已添加到历史记录", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    firstOperand = number;
                                    currentOperation = operation;
                                    newNumber = true;
                                }
                            } catch (Exception e) {
                                Toast.makeText(CalculatorActivity.this, "计算错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double performOperation(double a, double b, String operation) {
        if (operation.equals("+")) {
            return a + b;
        } else if (operation.equals("-")) {
            return a - b;
        } else if (operation.equals("×")) {
            return a * b;
        } else {
            return b;
        }
    }

    private String formatResult(double result) {
        if (result == (int) result) {
            return String.valueOf((int) result);
        } else {
            // 限制小数位数以避免显示过长
            return String.format("%.6f", result).replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }
    
    // 创建一个简单的后备视图，以防主视图加载失败
    private TextView createFallbackView() {
        TextView textView = new TextView(this);
        textView.setText("计算器界面加载失败，请返回重试");
        textView.setTextSize(20);
        textView.setPadding(50, 50, 50, 50);
        return textView;
    }
} 