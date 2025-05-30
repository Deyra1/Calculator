package com.example.calculator;

import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

import android.util.Log;

import java.util.Arrays;

public class BinaryConverterActivity extends AppCompatActivity {
    private static final String TAG = "BinaryConverter";
    
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

    // 数字按钮引用
    private Button[] numberButtons = new Button[10];
    // 十六进制按钮引用
    private Button[] hexButtons = new Button[6]; // A, B, C, D, E, F
    // 其他功能按钮引用
    private Button btnClear;
    private ImageButton btnDelete; // 修改为ImageButton类型
    private Button btnDot;
    private Button btnDoubleZero;
    private Button btnC_hex; // 添加C_hex按钮引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            // 读取并应用主题偏好设置
            SharedPreferences preferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
            int themeMode = preferences.getInt("selectedTheme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

            // 根据偏好设置应用主题
            AppCompatDelegate.setDefaultNightMode(themeMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binary_converter);

        // 初始化显示区域
            initializeViews();

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
        
            // 确保所有进制行都可见
            ensureRowsVisible();
        
        // 初始显示
        updateDisplay();
        
        // 设置主题色
            if (txtDecimalValue != null) {
        txtDecimalValue.setTextColor(themeColor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "初始化进制转换器失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // 创建简单的后备视图
            setContentView(createFallbackView());
        }
    }
    
    private void initializeViews() {
        try {
            txtBinaryValue = findViewById(R.id.txtBinaryValue);
            txtOctalValue = findViewById(R.id.txtOctalValue);
            txtDecimalValue = findViewById(R.id.txtDecimalValue);
            txtHexValue = findViewById(R.id.txtHexValue);
            
            if (txtBinaryValue == null || txtOctalValue == null || 
                txtDecimalValue == null || txtHexValue == null) {
                throw new IllegalStateException("无法找到必要的TextView视图");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw e; // 重新抛出异常，让外层捕获
        }
    }
    
    private View createFallbackView() {
        TextView fallbackView = new TextView(this);
        fallbackView.setText("进制转换器加载失败，请返回重试");
        fallbackView.setTextSize(20);
        fallbackView.setPadding(50, 50, 50, 50);
        fallbackView.setGravity(android.view.Gravity.CENTER);
        return fallbackView;
    }
    
    private void ensureRowsVisible() {
        try {
            if (txtBinaryValue == null || txtOctalValue == null || 
                txtDecimalValue == null || txtHexValue == null) {
                Log.e(TAG, "TextView references are null, cannot make rows visible");
                return;
            }
            
            View binaryRow = (View)txtBinaryValue.getParent();
            View octalRow = (View)txtOctalValue.getParent();
            View decimalRow = (View)txtDecimalValue.getParent();
            View hexRow = (View)txtHexValue.getParent();
            
            if (binaryRow != null) binaryRow.setVisibility(View.VISIBLE);
            if (octalRow != null) octalRow.setVisibility(View.VISIBLE);
            if (decimalRow != null) decimalRow.setVisibility(View.VISIBLE);
            if (hexRow != null) hexRow.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring rows visible: " + e.getMessage(), e);
        }
    }

    private void setupTopBar() {
        try {
            // 获取导航栏布局中的控件
            View navBar = findViewById(R.id.nav_bar);
            if (navBar == null) {
                Log.e(TAG, "Navigation bar not found");
                return;
            }
            
            // 设置左侧按钮点击事件
            ImageButton btnMenu = navBar.findViewById(R.id.btnMenu);
            ImageButton btnBack = navBar.findViewById(R.id.btnBack);
            
            // 在进制转换页面，显示菜单按钮，隐藏返回按钮
            if (btnMenu != null && btnBack != null) {
                btnMenu.setVisibility(View.VISIBLE);
                btnBack.setVisibility(View.GONE);
                
                btnMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 跳转到菜单页面
                        Intent menuIntent = new Intent(BinaryConverterActivity.this, MenuActivity.class);
                        // 传递当前登录的用户名
                        menuIntent.putExtra("username", getIntent().getStringExtra("username") != null ? 
                            getIntent().getStringExtra("username") : "admin");
                        startActivity(menuIntent);
                    }
                });
            } else {
                Log.w(TAG, "Left buttons not found");
            }
            
            // 设置选项卡样式
            TextView tabBasic = navBar.findViewById(R.id.tabBasic);
            TextView tabBinary = navBar.findViewById(R.id.tabBinary);
            
            if (tabBasic != null && tabBinary != null) {
                // 设置当前页面标签样式
                tabBinary.setTextColor(getResources().getColor(R.color.colorPrimary));
                tabBinary.setTypeface(null, android.graphics.Typeface.BOLD);
                
                // 设置其他标签样式
                tabBasic.setTextColor(getResources().getColor(android.R.color.darker_gray));
                tabBasic.setTypeface(null, android.graphics.Typeface.NORMAL);
                
                // 设置标签点击事件
                tabBasic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 处理基础计算器标签的点击
                        Intent calculatorIntent = new Intent(BinaryConverterActivity.this, CalculatorActivity.class);
                        // 传递当前登录的用户名
                        calculatorIntent.putExtra("username", getIntent().getStringExtra("username") != null ? 
                            getIntent().getStringExtra("username") : "admin");
                        calculatorIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(calculatorIntent);
                        finish();
                    }
                });
                
                // 进制转换标签点击不需要处理，因为已经在进制转换页面
            }
            
            // 隐藏语音输入和静音按钮
            ImageButton btnMute = navBar.findViewById(R.id.btnMute);
            ImageButton btnVoiceInput = navBar.findViewById(R.id.btnVoiceInput);
            
            if (btnMute != null) {
                btnMute.setVisibility(View.GONE);
            }
            
            if (btnVoiceInput != null) {
                btnVoiceInput.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up top bar: " + e.getMessage(), e);
        }
    }

    private void setupNumberButtons() {
        try {
        // 数字0-9
        for (int i = 0; i <= 9; i++) {
            final int num = i;
            int id = getResources().getIdentifier("btn" + i, "id", getPackageName());
                numberButtons[i] = findViewById(id);
                if (numberButtons[i] == null) {
                    Log.e(TAG, "Button with id btn" + i + " not found!");
                } else {
                    numberButtons[i].setOnClickListener(v -> {
                        try {
                            onNumberPressed(num);
                        } catch (Exception e) {
                            Log.e(TAG, "Error handling number button click: " + e.getMessage(), e);
                        }
                    });
            }
        }

        // 十六进制按钮A-F
            char[] hexChars = {'A', 'B', 'C', 'D', 'E', 'F'};
            for (int i = 0; i < 6; i++) {
                char c = hexChars[i];
                final char hex = c;
                int id = getResources().getIdentifier("btn" + c, "id", getPackageName());
                // 特殊处理C按钮，因为它有特殊的ID
                if (c == 'C') {
                    id = R.id.btnC_hex;
                }
                hexButtons[i] = findViewById(id);
                if (hexButtons[i] == null) {
                    Log.e(TAG, "Button with id btn" + c + " not found!");
                } else {
                    hexButtons[i].setOnClickListener(v -> {
                        try {
                            onHexPressed(hex);
                        } catch (Exception e) {
                            Log.e(TAG, "Error handling hex button click: " + e.getMessage(), e);
                        }
                    });
                }
            }

        // 双零按钮
        btnDoubleZero = findViewById(R.id.btnDoubleZero);
        if (btnDoubleZero != null) {
            btnDoubleZero.setOnClickListener(v -> {
                try {
                    // 在二进制模式下，00 等同于 0
                    if (inputBase == 2) {
                        if (newInput || currentInput.toString().equals("0")) {
                            currentInput = new StringBuilder("0");
                        } else {
                            currentInput.append("0");
                        }
                    } else {
                        // 其他进制正常处理00
                        if (newInput || currentInput.toString().equals("0")) {
                            currentInput = new StringBuilder("0");
                        } else {
                            currentInput.append("00");
                        }
                    }
                    updateDisplay();
                } catch (Exception e) {
                    Log.e(TAG, "Error handling double zero button click: " + e.getMessage(), e);
                }
            });
        }

        // 小数点按钮
            btnDot = findViewById(R.id.btnDot);
            if (btnDot == null) {
                Log.e(TAG, "Button with id btnDot not found!");
            } else {
            btnDot.setOnClickListener(v -> {
                try {
                    // 如果当前输入中没有小数点，则添加小数点
                    if (!currentInput.toString().contains(".")) {
                        if (newInput) {
                            currentInput = new StringBuilder("0.");
                            newInput = false;
                        } else {
                            currentInput.append(".");
                        }
                        updateDisplay();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling dot button click: " + e.getMessage(), e);
                }
            });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up number buttons: " + e.getMessage(), e);
        }
    }

    private void setupFunctionButtons() {
        try {
        // 清除按钮
            btnClear = findViewById(R.id.btnClear);
            if (btnClear == null) {
                Log.e(TAG, "Button with id btnClear not found!");
            } else {
            btnClear.setOnClickListener(v -> {
                    try {
                currentInput = new StringBuilder("0");
                newInput = true;
                updateDisplay();
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling clear button click: " + e.getMessage(), e);
                    }
            });
        }

        // 删除按钮
            btnDelete = findViewById(R.id.btnDelete);
            if (btnDelete == null) {
                Log.e(TAG, "Button with id btnDelete not found!");
            } else {
            // 设置删除按钮的颜色为主题色
            btnDelete.setColorFilter(themeColor);
            btnDelete.setOnClickListener(v -> {
                    try {
                if (currentInput.length() > 0) {
                    if (currentInput.length() == 1) {
                        currentInput = new StringBuilder("0");
                        newInput = true;
                    } else {
                        currentInput.deleteCharAt(currentInput.length() - 1);
                        // 如果删除后只剩下小数点，则删除小数点
                        if (currentInput.toString().equals(".")) {
                            currentInput = new StringBuilder("0");
                            newInput = true;
                        }
                        // 如果删除后以小数点结尾，保留小数点
                        // 如果删除后为空，则设为0
                        if (currentInput.length() == 0) {
                            currentInput = new StringBuilder("0");
                            newInput = true;
                        }
                    }
                    updateDisplay();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling delete button click: " + e.getMessage(), e);
                }
            });
            }
            
            // 获取C_hex按钮引用
            btnC_hex = findViewById(R.id.btnC_hex);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up function buttons: " + e.getMessage(), e);
        }
    }

    private void setupInputAreaClick() {
        try {
        // 设置四个进制显示区域的点击事件
        View binaryRow = findViewById(R.id.layoutBinaryRow);
        View octalRow = findViewById(R.id.layoutOctalRow);  
        View decimalRow = findViewById(R.id.layoutDecimalRow);
            View hexRow = findViewById(R.id.layoutHexRow);
        
            if (binaryRow != null) {
                binaryRow.setOnClickListener(v -> {
                    try {
                        setActiveInput(2);
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting binary as active input: " + e.getMessage(), e);
                    }
                });
            }
            
            if (octalRow != null) {
                octalRow.setOnClickListener(v -> {
                    try {
                        setActiveInput(8);
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting octal as active input: " + e.getMessage(), e);
                    }
                });
            }
            
            if (decimalRow != null) {
                decimalRow.setOnClickListener(v -> {
                    try {
                        setActiveInput(10);
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting decimal as active input: " + e.getMessage(), e);
                    }
                });
            }
            
            if (hexRow != null) {
                hexRow.setOnClickListener(v -> {
                    try {
                        setActiveInput(16);
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting hex as active input: " + e.getMessage(), e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up input area clicks: " + e.getMessage(), e);
        }
    }
    
    private void setActiveInput(int base) {
        try {
        // 更新输入进制
        inputBase = base;
            
            // 检查TextView是否为null
            if (txtBinaryValue == null || txtOctalValue == null || 
                txtDecimalValue == null || txtHexValue == null) {
                Log.e(TAG, "TextView references are null in setActiveInput");
                return;
            }
        
        // 更新输入字段颜色
        txtBinaryValue.setTextColor(base == 2 ? themeColor : defaultTextColor);
        txtOctalValue.setTextColor(base == 8 ? themeColor : defaultTextColor);
        txtDecimalValue.setTextColor(base == 10 ? themeColor : defaultTextColor);
        txtHexValue.setTextColor(base == 16 ? themeColor : defaultTextColor);
        
        // 更新活跃视图引用
        switch (base) {
            case 2:
                activeInputView = txtBinaryValue;
                break;
            case 8:
                activeInputView = txtOctalValue;
                break;
            case 16:
                activeInputView = txtHexValue;
                break;
            default: // 10
                activeInputView = txtDecimalValue;
                break;
        }
        
        // 确保所有进制行都可见
        ensureRowsVisible();
        
        // 切换进制时总是重置输入值为0
        currentInput = new StringBuilder("0");
        newInput = true;
        
        // 更新按钮状态
        updateButtonStates();
        
        // 更新UI显示
        updateDisplay();
        } catch (Exception e) {
            Log.e(TAG, "Error setting active input: " + e.getMessage(), e);
        }
    }

    // 更新按钮状态
    private void updateButtonStates() {
        try {
        // 根据当前输入进制启用或禁用相应按钮
        for (int i = 0; i <= 9; i++) {
                Button btn = numberButtons[i];
            if (btn != null) {
                boolean enabled = true;
                if (inputBase == 2 && i > 1) enabled = false; // 二进制只允许0和1
                if (inputBase == 8 && i > 7) enabled = false; // 八进制只允许0-7
                
                btn.setEnabled(enabled);
                btn.setAlpha(enabled ? 1.0f : 0.5f);
            }
        }
        
        // 十六进制字符按钮
            for (int i = 0; i < 6; i++) {
                Button btn = hexButtons[i];
            if (btn != null) {
                boolean enabled = inputBase == 16; // 只有十六进制才能使用A-F
                btn.setEnabled(enabled);
                btn.setAlpha(enabled ? 1.0f : 0.5f);
            }
        }
        
        // 特殊处理C_hex按钮（如果不是通过hexButtons数组处理的话）
        if (btnC_hex != null && !Arrays.asList(hexButtons).contains(btnC_hex)) {
            boolean enabled = inputBase == 16;
            btnC_hex.setEnabled(enabled);
            btnC_hex.setAlpha(enabled ? 1.0f : 0.5f);
        }
        
        // 双零按钮在所有模式下都可用
        if (btnDoubleZero != null) {
            btnDoubleZero.setEnabled(true);
            btnDoubleZero.setAlpha(1.0f);
        }
        
        // 清除和删除按钮始终可用
            Button btnClear = this.btnClear;
        if (btnClear != null) {
            btnClear.setEnabled(true);
            btnClear.setAlpha(1.0f);
        }
        
            ImageButton btnDelete = this.btnDelete;
        if (btnDelete != null) {
            btnDelete.setEnabled(true);
            btnDelete.setAlpha(1.0f);
        }
        
        // 小数点按钮启用（进制转换也应该支持小数点输入）
            Button btnDot = this.btnDot;
        if (btnDot != null) {
            btnDot.setEnabled(true);
            btnDot.setAlpha(1.0f);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating button states: " + e.getMessage(), e);
        }
    }

    private void updateDisplay() {
        try {
            // 检查TextView是否为null
            if (txtBinaryValue == null || txtOctalValue == null || 
                txtDecimalValue == null || txtHexValue == null) {
                Log.e(TAG, "TextView references are null in updateDisplay");
                return;
            }
            
            // 处理带小数点的情况
            String inputStr = currentInput.toString();
            
            try {
                if (inputStr.contains(".")) {
                    // 将当前输入转换为十进制值
                    double decimalValue;
                    
                    // 根据当前输入进制转换为十进制
                    if (inputBase == 10) {
                        // 十进制直接解析
                        decimalValue = Double.parseDouble(inputStr);
                    } else {
                        // 其他进制需要分别处理整数部分和小数部分
                        String[] parts = inputStr.split("\\.");
                        String intPart = parts[0];
                        String fracPart = parts.length > 1 ? parts[1] : "";
                        
                        // 整数部分转换为十进制
                        int intValue = Integer.parseInt(intPart, inputBase);
                        
                        // 小数部分转换为十进制
                        double fracValue = 0;
                        for (int i = 0; i < fracPart.length(); i++) {
                            int digit = Character.digit(fracPart.charAt(i), inputBase);
                            if (digit < 0) {
                                throw new NumberFormatException("Invalid digit for base " + inputBase);
                            }
                            fracValue += digit * Math.pow(inputBase, -(i + 1));
                        }
                        
                        decimalValue = intValue + fracValue;
                    }
                    
                    // 将十进制值转换为各种进制
                    // 二进制表示
                    String binaryStr = convertDecimalToBase(decimalValue, 2);
                    
                    // 八进制表示
                    String octalStr = convertDecimalToBase(decimalValue, 8);
                    
                    // 十六进制表示
                    String hexStr = convertDecimalToBase(decimalValue, 16).toUpperCase();
                    
                    // 更新显示
                    txtBinaryValue.setText(binaryStr);
                    txtOctalValue.setText(octalStr);
                    txtDecimalValue.setText(Double.toString(decimalValue));
                    txtHexValue.setText(hexStr);
                } else {
                    // 整数情况，进行正常的进制转换
                    int intValue = Integer.parseInt(inputStr, inputBase);
                    
                    // 更新所有进制的值
                    txtBinaryValue.setText(Integer.toBinaryString(intValue));
                    txtOctalValue.setText(Integer.toOctalString(intValue));
                    txtDecimalValue.setText(Integer.toString(intValue));
                    txtHexValue.setText(Integer.toHexString(intValue).toUpperCase());
                }
            } catch (NumberFormatException e) {
                // 处理超出整数范围的值
                Log.e(TAG, "Number format error: " + e.getMessage());
                currentInput = new StringBuilder("0");
                newInput = true;
                
                // 更新所有进制的值为0
                txtBinaryValue.setText("0");
                txtOctalValue.setText("0");
                txtDecimalValue.setText("0");
                txtHexValue.setText("0");
            }
            
            // 确保所有进制行都可见
            ensureRowsVisible();
            
            // 恢复当前输入框的主题色
            txtBinaryValue.setTextColor(inputBase == 2 ? themeColor : defaultTextColor);
            txtOctalValue.setTextColor(inputBase == 8 ? themeColor : defaultTextColor);
            txtDecimalValue.setTextColor(inputBase == 10 ? themeColor : defaultTextColor);
            txtHexValue.setTextColor(inputBase == 16 ? themeColor : defaultTextColor);
            
            // 启用或禁用按钮
            updateButtonStates();
            
        } catch (Exception e) {
            // 处理任何其他异常
            Log.e(TAG, "Error updating display: " + e.getMessage(), e);
            currentInput = new StringBuilder("0");
            newInput = true;
            
            // 确保TextView不为null
            if (txtBinaryValue != null) txtBinaryValue.setText("0");
            if (txtOctalValue != null) txtOctalValue.setText("0");
            if (txtDecimalValue != null) txtDecimalValue.setText("0");
            if (txtHexValue != null) txtHexValue.setText("0");
            
            // 更新按钮状态
            updateButtonStates();
        }
    }
    
    /**
     * 将十进制小数转换为指定进制的字符串表示
     * @param decimalValue 十进制小数值
     * @param targetBase 目标进制
     * @return 目标进制的字符串表示
     */
    private String convertDecimalToBase(double decimalValue, int targetBase) {
        // 分离整数部分和小数部分
        int intPart = (int) decimalValue;
        double fracPart = decimalValue - intPart;
        
        // 转换整数部分
        String intString;
        switch (targetBase) {
            case 2:
                intString = Integer.toBinaryString(intPart);
                break;
            case 8:
                intString = Integer.toOctalString(intPart);
                break;
            case 16:
                intString = Integer.toHexString(intPart);
                break;
            default:
                intString = Integer.toString(intPart);
        }
        
        // 如果没有小数部分，直接返回整数部分
        if (fracPart == 0) {
            return intString;
        }
        
        // 转换小数部分（最多保留10位小数）
        StringBuilder fracString = new StringBuilder(".");
        for (int i = 0; i < 10 && fracPart > 0; i++) {
            fracPart *= targetBase;
            int digit = (int) fracPart;
            if (digit < 10) {
                fracString.append(digit);
            } else {
                // 对于十六进制，10-15转换为A-F
                fracString.append((char) ('A' + digit - 10));
            }
            fracPart -= digit;
        }
        
        return intString + fracString.toString();
    }

    private void onNumberPressed(int number) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error handling number press: " + e.getMessage(), e);
        }
    }

    private void onHexPressed(char hexChar) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error handling hex press: " + e.getMessage(), e);
        }
    }
} 