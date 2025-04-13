package com.example.calculator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalculatorBroadcastReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            
            if (action != null) {
                String message = "";
                
                // 根据接收到的广播类型显示不同的消息
                switch (action) {
                    case "com.example.calculator.CUSTOM_ACTION":
                        // 获取自定义广播中的额外数据
                        String customMessage = intent.getStringExtra("message");
                        long timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis());
                        
                        // 格式化时间戳
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String timeString = sdf.format(new Date(timestamp));
                        
                        // 构建显示消息
                        if (customMessage != null && !customMessage.isEmpty()) {
                            message = customMessage + "\n接收时间: " + timeString;
                        } else {
                            message = "收到自定义广播消息\n接收时间: " + timeString;
                        }
                        break;
                        
                    case Intent.ACTION_BATTERY_LOW:
                        message = "电池电量低，请连接充电器";
                        break;
                        
                    case Intent.ACTION_POWER_CONNECTED:
                        message = "充电器已连接";
                        break;
                        
                    default:
                        message = "收到未知广播: " + action;
                        break;
                }
                
                // 显示消息
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "处理广播时出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
} 