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
            
            // 设置用户名显示
            TextView txtUsername = findViewById(R.id.txtUsername);
            if (txtUsername != null) {
                txtUsername.setText(username);
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
            // 设置"返回"按钮
            View btnBack = findViewById(R.id.btnBack);
            if (btnBack != null) {
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish(); // 返回上一个Activity
                    }
                });
            }
            
            // 设置"关于"按钮
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
            
            // 设置历史记录菜单项
            View menuItemHistory = findViewById(R.id.menuItemHistory);
            if (menuItemHistory != null) {
                menuItemHistory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent historyIntent = new Intent(MenuActivity.this, HistoryActivity.class);
                        startActivity(historyIntent);
                    }
                });
            }
            
            // 设置主题菜单项
            setupMenuItem(R.id.menuItemTheme, "主题设置功能待实现");
            
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