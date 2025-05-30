package com.example.calculator;

import static android.content.Context.RECEIVER_NOT_EXPORTED;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.example.calculator.model.HistoryItem;
import com.example.calculator.utils.HistoryManager;

import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.ArrayList;
import android.Manifest;
import android.content.pm.PackageManager;
import java.util.Map;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.ActivityNotFoundException;

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

    private ImageButton btnVoiceInput; // 声明语音输入按钮
    private SpeechRecognizer speechRecognizer; // 声明 SpeechRecognizer
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200; // 权限请求码

    private boolean isListeningForSpeech = false; // 标记是否正在进行语音输入

    // 添加常量
    private static final int REQUEST_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 读取并应用主题偏好设置
        SharedPreferences preferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int themeMode = preferences.getInt("selectedTheme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        // 根据偏好设置应用主题
        AppCompatDelegate.setDefaultNightMode(themeMode);
        
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

            // 初始化语音输入
            setupVoiceInput();
            
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
        // 在 Activity 销毁时销毁 SpeechRecognizer
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
            Log.d("SpeechRecognizer", "SpeechRecognizer 已销毁");
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
            // 获取导航栏布局中的控件
            View navBar = findViewById(R.id.nav_bar);
            if (navBar == null) {
                Log.e("CalculatorActivity", "Navigation bar not found");
                return;
            }
            
            // 设置菜单按钮点击事件
            ImageButton btnMenu = navBar.findViewById(R.id.btnMenu);
            ImageButton btnBack = navBar.findViewById(R.id.btnBack);
            
            // 在计算器页面，显示菜单按钮，隐藏返回按钮
            if (btnMenu != null && btnBack != null) {
                btnMenu.setVisibility(View.VISIBLE);
                btnBack.setVisibility(View.GONE);
                
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
            
            // 设置选项卡样式
            TextView tabBasic = navBar.findViewById(R.id.tabBasic);
            TextView tabBinary = navBar.findViewById(R.id.tabBinary);
            
            if (tabBasic != null && tabBinary != null) {
                // 设置当前页面标签样式
                tabBasic.setTextColor(getResources().getColor(R.color.colorPrimary));
                tabBasic.setTypeface(null, android.graphics.Typeface.BOLD);
                
                // 设置其他标签样式
                tabBinary.setTextColor(getResources().getColor(android.R.color.darker_gray));
                tabBinary.setTypeface(null, android.graphics.Typeface.NORMAL);
                
                // 设置标签点击事件
                tabBinary.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 跳转到进制转换页面
                        Intent binaryIntent = new Intent(CalculatorActivity.this, BinaryConverterActivity.class);
                        // 传递当前登录的用户名
                        binaryIntent.putExtra("username", getIntent().getStringExtra("username") != null ? 
                            getIntent().getStringExtra("username") : "admin");
                        startActivity(binaryIntent);
                    }
                });
                
                // 基础标签点击不需要处理，因为已经在基础页面
            }
            
            // 设置音量按钮点击事件
            setupVolumeButton();
        } catch (Exception e) {
            Log.e("CalculatorActivity", "Error setting up top bar: " + e.getMessage(), e);
        }
    }
    
    private void setupVolumeButton() {
        try {
            // 获取导航栏布局中的音量按钮
            View navBar = findViewById(R.id.nav_bar);
            if (navBar == null) {
                Log.e("CalculatorActivity", "Navigation bar not found");
                return;
            }
            
            ImageButton btnMute = navBar.findViewById(R.id.btnMute);
        if (btnMute != null) {
                // 获取当前音量状态
                SharedPreferences preferences = getSharedPreferences("CalculatorPrefs", MODE_PRIVATE);
                boolean isMuted = preferences.getBoolean("isMuted", false);
                
                // 设置初始图标
                btnMute.setImageResource(isMuted ? R.drawable.volume_off : R.drawable.volume_on);
                
                // 设置点击事件
            btnMute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        // 切换音量状态
                        SharedPreferences prefs = getSharedPreferences("CalculatorPrefs", MODE_PRIVATE);
                        boolean currentMuted = prefs.getBoolean("isMuted", false);
                        boolean newMuted = !currentMuted;
                        
                        // 保存新的音量状态
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("isMuted", newMuted);
                        editor.apply();
                        
                        // 更新按钮图标
                        btnMute.setImageResource(newMuted ? R.drawable.volume_off : R.drawable.volume_on);
                        
                        // 显示提示
                        Toast.makeText(CalculatorActivity.this, 
                            newMuted ? "已关闭按键音效" : "已开启按键音效", 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            // 设置语音输入按钮
            ImageButton btnVoiceInput = navBar.findViewById(R.id.btnVoiceInput);
            if (btnVoiceInput != null) {
                btnVoiceInput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        // 启动语音输入
                        startVoiceInput();
                    }
                });
            }
        } catch (Exception e) {
            Log.e("CalculatorActivity", "Error setting up volume button: " + e.getMessage(), e);
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
            
            // 设置删除按钮
            ImageButton btnDelete = findViewById(R.id.btnDelete);
            if (btnDelete != null) {
                // 设置删除按钮的颜色为主题色
                int themeColor = getResources().getColor(R.color.colorPrimary);
                btnDelete.setColorFilter(themeColor);
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentInput.length() > 0) {
                            if (currentInput.length() == 1) {
                                currentInput = new StringBuilder("0");
                                newNumber = true;
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

    private void setupVoiceInput() {
        // 初始化语音输入按钮
        btnVoiceInput = findViewById(R.id.btnVoiceInput);
        if (btnVoiceInput != null) {
            // 检查设备是否支持语音识别
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                btnVoiceInput.setEnabled(true); // 如果支持，启用按钮
                btnVoiceInput.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 检查并请求录音权限
                        if (ContextCompat.checkSelfPermission(CalculatorActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CalculatorActivity.this, new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                        } else {
                            // 权限已授予，开始语音识别
                            startVoiceInput();
                        }
                    }
                });

            } else {
                btnVoiceInput.setEnabled(false); // 如果不支持，禁用按钮
                Toast.makeText(this, "设备不支持语音识别", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("CalculatorActivity", "无法找到语音输入按钮");
        }
        // 设置麦克风按钮初始状态
        updateVoiceInputButtonState();
    }

    private void startVoiceInput() {
        try {
            // 检查是否有语音识别功能
            PackageManager pm = getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(
                    new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
            if (activities.size() == 0) {
                Toast.makeText(this, "您的设备不支持语音识别", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 创建语音识别意图
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出您要计算的表达式");
            
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "您的设备不支持语音识别", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("CalculatorActivity", "Error starting voice input: " + e.getMessage(), e);
            Toast.makeText(this, "启动语音输入失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 新增方法：停止语音识别
    private void stopVoiceRecognition() {
        if (speechRecognizer != null && isListeningForSpeech) {
            speechRecognizer.stopListening();
            // SpeechRecognizer 会在 onEndOfSpeech 或 onError 中被销毁
            isListeningForSpeech = false; // 设置不在聆听状态
            updateVoiceInputButtonState(); // 更新按钮UI
            Log.d("SpeechRecognizer", "停止聆听...");
        }
    }

    // 新增方法：更新语音输入按钮状态
    private void updateVoiceInputButtonState() {
        if (btnVoiceInput != null) {
             // 根据 isListeningForSpeech 状态更新按钮图标或颜色
             // 例如：如果正在聆听，可以使用不同的 tint 或背景
             btnVoiceInput.setColorFilter(isListeningForSpeech ?
                 ContextCompat.getColor(this, R.color.colorPrimary) : // 正在聆听时的颜色
                 ContextCompat.getColor(this, R.color.gray)); // 默认颜色
        }
    }

    // 新增方法：处理识别到的文本
    private String processRecognizedText(String text) {
        // 将识别到的文本转换为计算器输入字符串

        // 创建中文数字到阿拉伯数字的映射
        Map<String, String> numberMap = new HashMap<>();
        numberMap.put("零", "0");
        numberMap.put("一", "1");
        numberMap.put("二", "2");
        numberMap.put("三", "3");
        numberMap.put("四", "4");
        numberMap.put("五", "5");
        numberMap.put("六", "6");
        numberMap.put("七", "7");
        numberMap.put("八", "8");
        numberMap.put("九", "9");
        numberMap.put("点", "."); // 处理小数点

        // 创建中文运算符到计算器符号的映射
        Map<String, String> operatorMap = new HashMap<>();
        operatorMap.put("加", "+");
        operatorMap.put("减", "-");
        operatorMap.put("乘", "×");
        operatorMap.put("乘以", "×"); // 兼容"乘以"
        operatorMap.put("除以", "÷"); // 兼容"除以"
        operatorMap.put("除", "÷");
        operatorMap.put("等于", "=");

        String cleanedText = text.replace(" ", ""); // 移除空格方便处理
        Log.d("SpeechRecognizer", "清洗后的文本: " + cleanedText);

        StringBuilder processedInput = new StringBuilder();

        // 改进解析逻辑：先替换中文数字和运算符关键词为符号，然后处理剩余字符
        String tempText = cleanedText;

        // 为了避免替换时出现部分匹配问题（例如先替换"一"再替换"十一"），
        // 我们可以按词语长度降序排序，或者使用更复杂的匹配方式。
        // 对于基础实现，我们简单地按顺序替换，这可能会有一些局限性。

        // 先替换中文数字（避免影响包含数字的运算符关键词，如"乘以"）
        for (Map.Entry<String, String> entry : numberMap.entrySet()) {
            tempText = tempText.replace(entry.getKey(), entry.getValue());
        }

        // 然后替换运算符关键词
        for (Map.Entry<String, String> entry : operatorMap.entrySet()) {
            tempText = tempText.replace(entry.getKey(), entry.getValue());
        }

        // 现在 tempText 中应该大部分是数字、小数点和运算符符号了
        // 我们再遍历一次，只保留数字、小数点和已知的运算符符号
        for (char c : tempText.toCharArray()) {
            String charStr = String.valueOf(c);
            if (Character.isDigit(c) || charStr.equals(".") ||
                charStr.equals("+") || charStr.equals("-") ||
                charStr.equals("×") || charStr.equals("÷") ||
                charStr.equals("=")) {
                processedInput.append(charStr);
            } else {
                 // 忽略其他不能识别的字符
            }
        }

        String finalInput = processedInput.toString();
        Log.d("SpeechRecognizer", "解析后的输入字符串: " + finalInput);

        return finalInput; // 返回解析后的字符串
    }

    // 新增方法：处理解析后的表达式字符串输入
    private void handleExpressionInput(String expression) {
        // 遍历解析后的表达式字符串，模拟按键输入
        // 这里需要根据计算器的现有逻辑来决定如何馈送输入
        for (char c : expression.toCharArray()) {
            String inputChar = String.valueOf(c);

            // 模拟数字按钮点击或小数点
            if (Character.isDigit(c) || inputChar.equals(".")) {
                // 使用现有的处理数字按钮的逻辑
                setupNumberButtonInput(inputChar); // 调用一个辅助方法
            } else if (inputChar.equals("+") || inputChar.equals("-") || inputChar.equals("×") || inputChar.equals("÷")) {
                // 模拟运算符按钮点击
                // 使用现有的处理运算符按钮的逻辑
                 setupOperationButtonInput(inputChar); // 调用一个辅助方法
            } else if (inputChar.equals("=")) {
                // 模拟等于按钮点击
                setupOperationButtonInput(inputChar); // 使用现有的处理等于按钮的逻辑
            }
             // 忽略其他字符
        }
    }

    // 新增辅助方法：模拟数字按钮输入逻辑
    private void setupNumberButtonInput(String number) {
         if (newNumber || currentInput.toString().equals("0")) {
             currentInput = new StringBuilder(number);
             newNumber = false;
         } else {
             currentInput.append(number);
         }
         display.setText(currentInput.toString());
         // 如果需要，可以在这里添加语音播报输入的数字
         // speakResult(number);
    }

    // 新增辅助方法：模拟运算符按钮输入逻辑
    private void setupOperationButtonInput(String operation) {
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

                        // 如果需要，可以在这里添加语音播报结果
                        // speakResult("等于");
                        // new Handler().postDelayed(() -> speakResult(formattedResult), 800);

                        // 重置状态
                        currentExpression = new StringBuilder();
                        firstOperand = null;
                        currentOperation = null;
                        newNumber = true; // 计算完成后准备输入新数字
                    } else if (currentOperation == null) { // 如果没有进行中的运算，将当前结果作为下一个运算的第一个操作数
                         firstOperand = number;
                         currentOperation = operation;
                         currentExpression = new StringBuilder(currentInput + operation);
                         expressionDisplay.setText(currentExpression.toString());
                         newNumber = true;
                    }
                } else { // 基本运算符
                    if (firstOperand == null) {
                        firstOperand = number;
                        currentOperation = operation;
                        currentExpression = new StringBuilder(currentInput + operation);
                        expressionDisplay.setText(currentExpression.toString());
                        newNumber = true;
                    } else { // 连续运算
                        // 执行上一个运算
                        double result = performOperation(firstOperand, number, currentOperation);
                        String formattedResult = formatResult(result);
                        display.setText(formattedResult);
                        currentInput = new StringBuilder(display.getText());

                        // 设置当前运算为新的操作数和运算符
                        firstOperand = result; // 将上一步结果作为第一个操作数
                        currentOperation = operation;
                        currentExpression = new StringBuilder(formattedResult + operation);
                        expressionDisplay.setText(currentExpression.toString());
                        newNumber = true;
                         // 如果需要，可以在这里添加语音播报运算符
                        // String operationText = "";
                        // switch (operation) { ... speakResult(operationText); }
                    }
                }
            } catch (Exception e) {
                Toast.makeText(CalculatorActivity.this, "语音输入解析错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                // 错误时重置状态
                currentInput = new StringBuilder("0");
                currentExpression = new StringBuilder();
                currentOperation = null;
                firstOperand = null;
                display.setText("0");
                expressionDisplay.setText("");
                newNumber = true;
            }
        }
    }

    // 新增方法：获取SpeechRecognizer错误文本描述
    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case 3: // SpeechRecognizer.ERROR_AUDIO_ERROR:
                message = "音频错误";
                break;
            case 5: // SpeechRecognizer.ERROR_CLIENT:
                message = "客户端错误";
                break;
            case 9: // SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "权限不足";
                break;
            case 2: // SpeechRecognizer.ERROR_NETWORK:
                message = "网络错误";
                break;
            case 1: // SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "网络超时";
                break;
            case 7: // SpeechRecognizer.ERROR_NO_MATCH:
                message = "未识别到匹配项";
                break;
            case 8: // SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "识别服务忙";
                break;
            case 4: // SpeechRecognizer.ERROR_SERVER:
                message = "服务器错误";
                break;
            case 6: // SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "说话超时";
                break;
            default:
                message = "未知错误";
                break;
        }
        return message;
    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予
                Log.d("Permissions", "录音权限已授予");
                // 提示用户再次点击按钮以启动语音输入
                Toast.makeText(this, "录音权限已授予，请再次点击麦克风按钮开始语音输入", Toast.LENGTH_LONG).show();
            } else {
                // 权限被拒绝
                Log.w("Permissions", "录音权限被拒绝");
                Toast.makeText(this, "录音权限被拒绝，无法使用语音输入功能", Toast.LENGTH_LONG).show();
                // 禁用语音输入按钮
                if (btnVoiceInput != null) {
                    btnVoiceInput.setEnabled(false);
                }
            }
        }
    }

    // 添加onActivityResult方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String recognizedText = result.get(0);
                
                // 在处理语音输入结果之前，先清空当前计算器状态
                currentInput = new StringBuilder("0");
                currentExpression = new StringBuilder();
                currentOperation = null;
                firstOperand = null;
                newNumber = true;
                display.setText("0");
                expressionDisplay.setText("");
                
                // 调用processRecognizedText解析文本，并获取解析后的字符串
                String processedExpression = processRecognizedText(recognizedText);
                // 将解析后的字符串传递给handleExpressionInput方法进行处理
                handleExpressionInput(processedExpression);
            }
        }
    }
} 