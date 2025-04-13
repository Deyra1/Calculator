package com.example.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_menu);
            
            // 获取传递过来的用户名
            username = getIntent().getStringExtra("username");
            if (username == null || username.isEmpty()) {
                username = "Guest"; // 默认用户名
            }
            
            // 设置欢迎信息
            TextView welcomeTextView = findViewById(R.id.txtUsername);
            if (welcomeTextView != null) {
                welcomeTextView.setText("用户：" + username);
            }
            
            // 初始化菜单按钮
            setupMenuButtons();
            
        } catch (Exception e) {
            // 如果布局加载失败，显示一个简单的后备视图
            setContentView(createFallbackView());
            Toast.makeText(this, "菜单界面加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void setupMenuButtons() {
        try {
            // 设置"返回计算器"按钮
            View btnBack = findViewById(R.id.btnBack);
            if (btnBack != null) {
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish(); // 返回上一个Activity
                    }
                });
            }
            
            // 设置"关于我们"按钮
            View menuItemAbout = findViewById(R.id.menuItemAbout);
            if (menuItemAbout != null) {
                menuItemAbout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent aboutIntent = new Intent(MenuActivity.this, AboutActivity.class);
                        startActivity(aboutIntent);
                    }
                });
            }
            
            // 设置历史记录和主题设置菜单项
            setupMenuItem(R.id.menuItemHistory, "历史记录功能待实现");
            setupMenuItem(R.id.menuItemTheme, "主题设置功能待实现");
            
            // 设置发送广播菜单项
            View menuItemBroadcast = findViewById(R.id.menuItemBroadcast);
            if (menuItemBroadcast != null) {
                menuItemBroadcast.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendCustomBroadcast();
                    }
                });
            }
            
            // 设置"退出登录"按钮
            Button btnLogout = findViewById(R.id.btnLogout);
            if (btnLogout != null) {
                btnLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 返回登录界面
                        Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(MenuActivity.this, "已退出登录", Toast.LENGTH_SHORT).show();
                        finishAffinity(); // 结束所有Activity
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 发送自定义广播
     */
    private void sendCustomBroadcast() {
        try {
            // 创建一个Intent，指定action
            Intent broadcastIntent = new Intent("com.example.calculator.CUSTOM_ACTION");
            
            // 添加额外数据（可选）
            broadcastIntent.putExtra("message", "来自" + username + "的广播消息");
            broadcastIntent.putExtra("timestamp", System.currentTimeMillis());
            
            // 发送广播
            sendBroadcast(broadcastIntent);
            
            // 提示用户
            Toast.makeText(this, "广播已发送！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "发送广播失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void setupMenuItem(int itemId, final String message) {
        View menuItem = findViewById(itemId);
        if (menuItem != null) {
            menuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MenuActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    // 创建一个简单的后备视图，以防主视图加载失败
    private TextView createFallbackView() {
        TextView textView = new TextView(this);
        textView.setText("菜单界面加载失败，请返回重试");
        textView.setTextSize(20);
        textView.setPadding(50, 50, 50, 50);
        return textView;
    }
} 