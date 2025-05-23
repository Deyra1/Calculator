package com.example.calculator;

import static android.content.Context.RECEIVER_NOT_EXPORTED;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calculator.model.HistoryItem;
import com.example.calculator.utils.HistoryManager;

import java.util.List;
import java.util.Locale;
import java.util.HashMap;

public class CalculatorActivity extends AppCompatActivity {
    private TextView display;
    private TextView expressionDisplay;
    private StringBuilder currentInput = new StringBuilder("0");
    private StringBuilder currentExpression = new StringBuilder();
    private String currentOperation = null;
    private Double firstOperand = null;
    private boolean newNumber = true;
    private boolean isMuted = true; // 默认设置为静音
    private CalculatorBroadcastReceiver broadcastReceiver;
    private HistoryManager historyManager;
    private TextToSpeech textToSpeech;
    private boolean isTTSInitialized = false;
    private boolean paused = true; // Initialize paused to true

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 禁用模拟器OpenGL警告
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.opengl.EGL14.eglGetError(); // 清除可能的错误标志
        }
        
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_calculator);
            
            // 请求音频焦点
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.i("TTS", "Audio focus requested successfully.");
            } else {
                Log.e("TTS", "Failed to request audio focus.");
            }

            // 初始化TextToSpeech，延迟初始化
            initializeTextToSpeech();
            
            // 初始化显示区域
            display = findViewById(R.id.display);
            if (display == null) {
                throw new RuntimeException("无法找到display视图");
            }
            expressionDisplay = findViewById(R.id.expressionDisplay);
            if (expressionDisplay == null) {
                throw new RuntimeException("无法找到expressionDisplay视图");
            }
            
            // 初始化历史记录管理器
            historyManager = new HistoryManager(this);
            
            // 显示最新的历史记录
            displayLatestHistory();
            
            // 初始化顶部功能区
            setupTopBar();
            
            // 初始化计算器按钮
            setupCalculatorButtons();
            
            // 注册广播接收器
            broadcastReceiver = new CalculatorBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.example.calculator.CUSTOM_ACTION");
            intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
            intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            registerReceiver(broadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED);
        } catch (Exception e) {
            // 记录详细的错误信息
            String errorMsg = "计算器加载失败: " + e.getMessage() + "\n" +
                            "错误类型: " + e.getClass().getName() + "\n" +
                            "堆栈跟踪: " + android.util.Log.getStackTraceString(e);
            android.util.Log.e("CalculatorActivity", errorMsg);
            
            // 如果布局加载失败，显示一个简单的后备视图
            setContentView(createFallbackView());
            Toast.makeText(this, "计算器加载失败，请查看日志了解详情", Toast.LENGTH_LONG).show();
        }
    }
    
    private void initializeTextToSpeech() {
        try {
            if (textToSpeech != null) {
                textToSpeech.stop();
                textToSpeech.shutdown();
            }
            
            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        // 设置语言为中文
                        int result = textToSpeech.setLanguage(Locale.CHINESE);
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "不支持中文语音合成或缺少数据。");
                            isTTSInitialized = false;
                            paused = true;
                            if (result == TextToSpeech.LANG_MISSING_DATA) {
                                Log.e("TTS", "缺少中文语音数据，尝试启动下载界面...");
                                Intent installIntent = new Intent();
                                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                try {
                                    startActivity(installIntent);
                                } catch (Exception e) {
                                    Log.e("TTS", "启动语音数据安装界面失败: " + e.getMessage());
                                    Toast.makeText(getApplicationContext(), "无法启动语音数据安装界面，请检查系统TTS设置", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                             // 检查语音数据是否已下载
                            if (textToSpeech.isLanguageAvailable(Locale.CHINESE) >= TextToSpeech.LANG_AVAILABLE) {
                                Log.i("TTS", "中文语音数据已完整下载，TTS初始化成功");
                                isTTSInitialized = true;
                                paused = false; // 在初始化完成后将paused设置为false
                                // 可以在这里添加打开页面时的语音播报，但考虑到首页是计算器，可能不需要。
                                // 如果需要，可以取消注释speakOut("计算器已加载");
                                // speakOut("计算器已加载");
                            } else {
                                Log.e("TTS", "中文语音数据未完整下载。");
                                isTTSInitialized = false;
                                paused = true;
                            }
                        }
                    } else {
                        Log.e("TTS", "TextToSpeech引擎初始化失败，错误码：" + status);
                        isTTSInitialized = false;
                        paused = true;
                    }
                    // 更新UI以反映状态
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateMuteButtonState();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e("CalculatorActivity", "初始化语音引擎失败: " + e.getMessage());
            isTTSInitialized = false;
            paused = true;
            updateMuteButtonState();
        }
    }

    private void updateMuteButtonState() {
        try {
            final ImageButton btnMute = findViewById(R.id.btnMute);
            if (btnMute != null) {
                btnMute.post(new Runnable() {
                    @Override
                    public void run() {
                        btnMute.setImageResource(isMuted ? R.drawable.volume_off : R.drawable.volume_on);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("CalculatorActivity", "更新音量按钮状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示最新的历史记录
     */
    private void displayLatestHistory() {
        List<HistoryItem> historyList = historyManager.getHistoryList();
        if (!historyList.isEmpty()) {
            // 获取最新的一条历史记录
            HistoryItem latestItem = historyList.get(0);
            
            // 显示表达式和结果
            expressionDisplay.setText(latestItem.getExpression());
            display.setText(latestItem.getResult());
            
            // 更新当前输入
            currentInput = new StringBuilder(latestItem.getResult());
        } else {
            // 如果没有历史记录，显示默认值
            display.setText("0");
            expressionDisplay.setText("");
            currentInput = new StringBuilder("0");
        }
    }
    
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            try {
                textToSpeech.stop();
                textToSpeech.shutdown();
            } catch (Exception e) {
                Log.e("CalculatorActivity", "关闭语音引擎失败: " + e.getMessage());
            }
        }
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
            setupTab(R.id.tabBinary, "二进制转换");
            
            // 设置音量按钮点击事件
            setupVolumeButton();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupVolumeButton() {
        final ImageButton btnMute = findViewById(R.id.btnMute);
        if (btnMute != null) {
            btnMute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isTTSInitialized) {
                        // 如果TTS未初始化，尝试重新初始化
                        initializeTextToSpeech();
                        Toast.makeText(getApplicationContext(), "正在初始化语音系统...", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isMuted = !isMuted;
                    updateMuteButtonState();
                }
            });
            // 设置初始状态
            btnMute.setImageResource(R.drawable.volume_off);
        }
    }
    
    private void setupTab(int tabId, final String tabName) {
        TextView tabView = findViewById(tabId);
        if (tabView != null) {
            tabView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 处理二进制转换标签的点击
                    if (tabId == R.id.tabBinary) {
                        Intent binaryIntent = new Intent(CalculatorActivity.this, BinaryConverterActivity.class);
                        // 传递当前登录的用户名
                        binaryIntent.putExtra("username", getIntent().getStringExtra("username") != null ? 
                            getIntent().getStringExtra("username") : "admin");
                        startActivity(binaryIntent);
                        return;
                    }
                    
                    // 重置所有选项卡样式
                    TextView tabBasic = findViewById(R.id.tabBasic);
                    if (tabBasic != null) {
                        tabBasic.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        tabBasic.setTextSize(18);
                        tabBasic.setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                    
                    TextView tabBinary = findViewById(R.id.tabBinary);
                    if (tabBinary != null) {
                        tabBinary.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        tabBinary.setTextSize(18);
                        tabBinary.setTypeface(null, android.graphics.Typeface.NORMAL);
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
            setupOperationButton(R.id.btnDivide, "÷");
            setupOperationButton(R.id.btnEquals, "=");

            // 清除按钮
            Button btnClear = findViewById(R.id.btnClear);
            if (btnClear != null) {
                btnClear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentInput = new StringBuilder("0");
                        currentExpression = new StringBuilder();
                        currentOperation = null;
                        firstOperand = null;
                        display.setText("0");
                        expressionDisplay.setText("");
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
        } catch (Exception e) {
            Toast.makeText(this, "按钮初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
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
                        // 添加语音播报
                        speakResult(number);
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
                                        // 更新表达式显示
                                        currentExpression.append(currentInput);
                                        expressionDisplay.setText(currentExpression.toString());
                                        
                                        // 执行计算
                                        double result = performOperation(firstOperand, number, currentOperation);
                                        String formattedResult = formatResult(result);
                                        display.setText(formattedResult);
                                        currentInput = new StringBuilder(display.getText());
                                        
                                        // 保存到历史记录
                                        String fullExpression = currentExpression.toString();
                                        historyManager.saveHistory(fullExpression, formattedResult);
                                        
                                        // 语音播报"等于"
                                        speakResult("等于");
                                        
                                        // 在播报完"等于"后再播报结果
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                speakResult(formattedResult);
                                            }
                                        }, 800); // 延迟800毫秒，可以根据实际效果调整
                                        
                                        // 重置状态
                                        currentExpression = new StringBuilder();
                                        firstOperand = null;
                                        currentOperation = null;
                                    }
                                } else {
                                    firstOperand = number;
                                    currentOperation = operation;
                                    // 更新表达式显示
                                    currentExpression = new StringBuilder(currentInput + operation);
                                    expressionDisplay.setText(currentExpression.toString());
                                    newNumber = true;
                                    
                                    // 添加语音播报运算符
                                    String operationText = "";
                                    switch (operation) {
                                        case "+":
                                            operationText = "加";
                                            break;
                                        case "-":
                                            operationText = "减";
                                            break;
                                        case "×":
                                            operationText = "乘";
                                            break;
                                        case "÷":
                                            operationText = "除以";
                                            break;
                                    }
                                    if (!operationText.isEmpty()) {
                                         speakResult(operationText);
                                    }
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
        } else if (operation.equals("÷")) {
            if (b == 0) {
                Toast.makeText(this, "错误：除数不能为零", Toast.LENGTH_SHORT).show();
                return 0;
            }
            return a / b;
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
    
    // 添加语音播报方法
    private void speakResult(String text) {
        if (!isMuted && isTTSInitialized && !paused) {
            if (textToSpeech != null) {
                HashMap<String, String> params = new HashMap<>();
                // params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "result"); // 可以添加utterance ID用于监听回调
                try {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
                    Log.i("TTS", "正在播报: " + text);
                } catch (Exception e) {
                    Log.e("TTS", "播报失败: " + e.getMessage());
                    // 播报失败时更新静音状态和UI
                    isMuted = true;
                    paused = true;
                    isTTSInitialized = false;
                    updateMuteButtonState();
                }
            } else {
                 Log.w("TTS", "TextToSpeech 对象为 null，无法播报。");
                 // TTS对象为null时更新静音状态和UI
                 isMuted = true;
                 paused = true;
                 isTTSInitialized = false;
                 updateMuteButtonState();
            }
        } else {
            Log.d("TTS", "静音或TTS未初始化/暂停，跳过播报。");
        }
    }
    
    // 创建一个简单的后备视图，以防主视图加载失败
    private TextView createFallbackView() {
        TextView fallbackView = new TextView(this);
        fallbackView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        fallbackView.setGravity(android.view.Gravity.CENTER);
        fallbackView.setPadding(50, 50, 50, 50);
        fallbackView.setTextColor(android.graphics.Color.BLACK);
        fallbackView.setTextSize(16);
        fallbackView.setText("计算器页面加载失败\n\n" +
                           "请尝试以下解决方案：\n" +
                           "1. 重启应用\n" +
                           "2. 清除应用数据\n" +
                           "3. 检查系统版本是否支持\n\n" +
                           "如果问题持续，请联系开发人员");
        return fallbackView;
    }
} 