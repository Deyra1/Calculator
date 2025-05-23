package com.example.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class BinaryConverterActivity extends AppCompatActivity {
    private TextView txtBinaryValue;
    private TextView txtOctalValue;  
    private TextView txtDecimalValue;
    private TextView txtHexValue;
    
    // 当前输入和输出的进制
    private int inputBase = 10; // 默认输入进制为十进制
    private int outputBase = 2; // 默认输出进制为二进制
    
    private StringBuilder currentInput = new StringBuilder("0");
    private boolean newInput = true;
    
    // 主题颜色
    private final int themeColor = 0xFF69C261;
    private final int defaultTextColor = 0xFF000000;
    
    // 活跃输入区域
    private TextView activeInputView;
    private TextView activeOutputView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binary_converter);

        // 初始化显示区域
        txtBinaryValue = findViewById(R.id.txtBinaryValue);
        txtOctalValue = findViewById(R.id.txtOctalValue);
        txtDecimalValue = findViewById(R.id.txtDecimalValue);
        txtHexValue = findViewById(R.id.txtHexValue);

        // 设置顶部导航栏
        setupTopBar();

        // 设置数字按钮
        setupNumberButtons();

        // 设置功能按钮
        setupFunctionButtons();
        
        // 设置点击输入区域
        setupInputAreaClick();

        // 默认值
        inputBase = 10;  // 默认输入进制为十进制
        outputBase = 2;  // 默认输出进制为二进制
        activeInputView = txtDecimalValue;
        
        // 更新按钮状态
        updateButtonStates();
        
        // 设置所有进制行都可见
        View binaryRow = (View)txtBinaryValue.getParent();
        View octalRow = (View)txtOctalValue.getParent();
        View decimalRow = (View)txtDecimalValue.getParent();
        View hexRow = (View)txtHexValue.getParent();
        
        binaryRow.setVisibility(View.VISIBLE);
        octalRow.setVisibility(View.VISIBLE);
        decimalRow.setVisibility(View.VISIBLE);
        hexRow.setVisibility(View.VISIBLE);
        
        // 初始显示
        updateDisplay();
        
        // 设置主题色
        txtDecimalValue.setTextColor(themeColor);
    }

    private void setupTopBar() {
        try {
        // 设置返回按钮点击事件
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 返回上一页
                    finish();
                    }
                });
            }
            
            // 设置选项卡点击事件
            setupTab(R.id.tabBasic, "基础");
            setupTab(R.id.tabBinary, "进制转换");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTab(int tabId, final String tabName) {
        TextView tabView = findViewById(tabId);
        if (tabView != null) {
            tabView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 处理基础计算器标签的点击
                    if (tabId == R.id.tabBasic) {
                        Intent calculatorIntent = new Intent(BinaryConverterActivity.this, CalculatorActivity.class);
                        // 传递当前登录的用户名
                        calculatorIntent.putExtra("username", getIntent().getStringExtra("username") != null ? 
                            getIntent().getStringExtra("username") : "admin");
                        startActivity(calculatorIntent);
                        finish();
                        return;
                    }
                    
                    // 进制转换标签已经是当前页面，无需执行其他操作
                }
            });
        }
    }

    private void setupNumberButtons() {
        // 数字0-9
        for (int i = 0; i <= 9; i++) {
            final int num = i;
            int id = getResources().getIdentifier("btn" + i, "id", getPackageName());
            Button button = findViewById(id);
            if (button != null) {
                button.setOnClickListener(v -> onNumberPressed(num));
            }
        }

        // 十六进制按钮A-F
        for (char c = 'A'; c <= 'F'; c++) {
            final char hex = c;
            int id = getResources().getIdentifier("btn" + c, "id", getPackageName());
            Button button = findViewById(id);
            if (button != null) {
                button.setOnClickListener(v -> onHexPressed(hex));
                // 根据当前输入进制启用或禁用按钮
                button.setEnabled(inputBase == 16);
                button.setAlpha(inputBase == 16 ? 1.0f : 0.5f);
            }
        }

        // 00按钮
        Button btnDoubleZero = findViewById(R.id.btnDoubleZero);
        if (btnDoubleZero != null) {
            btnDoubleZero.setOnClickListener(v -> onDoubleZeroPressed());
        }

        // 小数点按钮
        Button btnDot = findViewById(R.id.btnDot);
        if (btnDot != null) {
            btnDot.setOnClickListener(v -> {
                // 进制转换不支持小数，不做任何操作也不显示Toast
            });
        }
    }

    private void setupFunctionButtons() {
        // 清除按钮
        Button btnClear = findViewById(R.id.btnClear);
        if (btnClear != null) {
            btnClear.setOnClickListener(v -> {
                currentInput = new StringBuilder("0");
                newInput = true;
                updateDisplay();
            });
        }

        // 删除按钮
        Button btnDelete = findViewById(R.id.btnDelete);
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                if (currentInput.length() > 0) {
                    if (currentInput.length() == 1) {
                        currentInput = new StringBuilder("0");
                        newInput = true;
                    } else {
                        currentInput.deleteCharAt(currentInput.length() - 1);
                    }
                    updateDisplay();
                }
            });
        }
    }

    private void setupInputAreaClick() {
        // 设置四个进制显示区域的点击事件
        View binaryRow = findViewById(R.id.layoutBinaryRow);
        View octalRow = findViewById(R.id.layoutOctalRow);  
        View decimalRow = findViewById(R.id.layoutDecimalRow);
        View hexRow = findViewById(R.id.layoutHexadecimalRow);
        
        binaryRow.setOnClickListener(v -> setActiveInput(2));
        octalRow.setOnClickListener(v -> setActiveInput(8));
        decimalRow.setOnClickListener(v -> setActiveInput(10));
        hexRow.setOnClickListener(v -> setActiveInput(16));
    }
    
    private void setActiveInput(int base) {
        // 更新输入进制
        inputBase = base;
        
        // 更新输入字段颜色
        txtBinaryValue.setTextColor(base == 2 ? themeColor : defaultTextColor);
        txtOctalValue.setTextColor(base == 8 ? themeColor : defaultTextColor);
        txtDecimalValue.setTextColor(base == 10 ? themeColor : defaultTextColor);
        txtHexValue.setTextColor(base == 16 ? themeColor : defaultTextColor);
        
        // 更新活跃视图引用
        switch (base) {
            case 2:
                activeInputView = txtBinaryValue;
                // 不需要改变输出进制
                break;
            case 8:
                activeInputView = txtOctalValue;
                // 不需要改变输出进制 
                break;
            case 16:
                activeInputView = txtHexValue;
                // 不需要改变输出进制
                break;
            default: // 10
                activeInputView = txtDecimalValue;
                // 不需要改变输出进制
                break;
        }
        
        // 确保所有进制行都可见
        View binaryRow = (View)txtBinaryValue.getParent();
        View octalRow = (View)txtOctalValue.getParent();
        View decimalRow = (View)txtDecimalValue.getParent();
        View hexRow = (View)txtHexValue.getParent();
        
        binaryRow.setVisibility(View.VISIBLE);
        octalRow.setVisibility(View.VISIBLE);
        decimalRow.setVisibility(View.VISIBLE);
        hexRow.setVisibility(View.VISIBLE);
        
        // 切换进制时总是重置输入值为0
        currentInput = new StringBuilder("0");
        newInput = true;
        
        // 更新按钮状态
        updateButtonStates();
        
        // 更新UI显示
        updateDisplay();
    }

    // 更新按钮状态
    private void updateButtonStates() {
        // 根据当前输入进制启用或禁用相应按钮
        for (int i = 0; i <= 9; i++) {
            Button btn = findViewById(getResources().getIdentifier("btn" + i, "id", getPackageName()));
            if (btn != null) {
                boolean enabled = true;
                if (inputBase == 2 && i > 1) enabled = false; // 二进制只允许0和1
                if (inputBase == 8 && i > 7) enabled = false; // 八进制只允许0-7
                
                btn.setEnabled(enabled);
                btn.setAlpha(enabled ? 1.0f : 0.5f);
            }
        }
        
        // 十六进制字符按钮
        for (char c = 'A'; c <= 'F'; c++) {
            Button btn = findViewById(getResources().getIdentifier("btn" + c, "id", getPackageName()));
            if (btn != null) {
                boolean enabled = inputBase == 16; // 只有十六进制才能使用A-F
                btn.setEnabled(enabled);
                btn.setAlpha(enabled ? 1.0f : 0.5f);
            }
        }
        
        // 00按钮始终可用
        Button btnDoubleZero = findViewById(R.id.btnDoubleZero);
        if (btnDoubleZero != null) {
            btnDoubleZero.setEnabled(true);
            btnDoubleZero.setAlpha(1.0f);
        }
        
        // 清除和删除按钮始终可用
        Button btnClear = findViewById(R.id.btnClear);
        if (btnClear != null) {
            btnClear.setEnabled(true);
            btnClear.setAlpha(1.0f);
        }
        
        Button btnDelete = findViewById(R.id.btnDelete);
        if (btnDelete != null) {
            btnDelete.setEnabled(true);
            btnDelete.setAlpha(1.0f);
        }
        
        // 小数点按钮禁用（因为进制转换不支持小数）
        Button btnDot = findViewById(R.id.btnDot);
        if (btnDot != null) {
            btnDot.setEnabled(false);
            btnDot.setAlpha(0.5f);
        }
    }

    private void updateDisplay() {
        try {
            // 将当前输入(基于inputBase)转换为整数值
            int value = Integer.parseInt(currentInput.toString(), inputBase);
            
            // 更新所有进制的值
            txtBinaryValue.setText(Integer.toBinaryString(value));
            txtOctalValue.setText(Integer.toOctalString(value));
            txtDecimalValue.setText(Integer.toString(value));
            txtHexValue.setText(Integer.toHexString(value).toUpperCase());
            
            // 确保所有进制行都可见（移除resetRowVisibility调用）
            View binaryRow = (View)txtBinaryValue.getParent();
            View octalRow = (View)txtOctalValue.getParent();
            View decimalRow = (View)txtDecimalValue.getParent();
            View hexRow = (View)txtHexValue.getParent();
            
            binaryRow.setVisibility(View.VISIBLE);
            octalRow.setVisibility(View.VISIBLE);
            decimalRow.setVisibility(View.VISIBLE);
            hexRow.setVisibility(View.VISIBLE);
            
            // 恢复当前输入框的主题色
            txtBinaryValue.setTextColor(inputBase == 2 ? themeColor : defaultTextColor);
            txtOctalValue.setTextColor(inputBase == 8 ? themeColor : defaultTextColor);
            txtDecimalValue.setTextColor(inputBase == 10 ? themeColor : defaultTextColor);
            txtHexValue.setTextColor(inputBase == 16 ? themeColor : defaultTextColor);
            
            // 启用或禁用按钮
            updateButtonStates();
            
        } catch (NumberFormatException e) {
            // 处理输入错误，不显示Toast
            currentInput = new StringBuilder("0");
            newInput = true;
            txtBinaryValue.setText("0");
            txtOctalValue.setText("0");
            txtDecimalValue.setText("0");
            txtHexValue.setText("0");
        }
    }

    private void onNumberPressed(int number) {
        // 检查按当前输入进制的限制
        if ((inputBase == 2 && number > 1) || 
            (inputBase == 8 && number > 7)) {
            // 超出范围的输入直接忽略，不显示Toast
            return;
        }

        if (newInput || currentInput.toString().equals("0")) {
            currentInput = new StringBuilder(Integer.toString(number));
            newInput = false;
        } else {
            currentInput.append(number);
        }
        updateDisplay();
    }

    private void onHexPressed(char hexChar) {
        // 十六进制字符只在十六进制模式有效
        if (inputBase != 16) {
            // 非十六进制模式直接忽略输入，不显示Toast
            return;
        }

        if (newInput || currentInput.toString().equals("0")) {
            currentInput = new StringBuilder(String.valueOf(hexChar));
            newInput = false;
        } else {
            currentInput.append(hexChar);
        }
        updateDisplay();
    }

    private void onDoubleZeroPressed() {
        if (newInput || currentInput.toString().equals("0")) {
            currentInput = new StringBuilder("0");
        } else {
            currentInput.append("00");
        }
        updateDisplay();
    }
} 