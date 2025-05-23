package com.example.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    // 定义固定的用户名和密码
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "123";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            
            // 在设置布局之前进行错误捕获
            try {
                setContentView(R.layout.activity_login);
            } catch (Exception e) {
                Log.e(TAG, "Failed to set content view: " + e.getMessage());
                Toast.makeText(this, "界面加载失败，请重试", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // Hide the action bar for clean UI
            try {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to hide action bar: " + e.getMessage());
            }
            
            // 找到用户名和密码输入框以及登录按钮
            final EditText usernameInput = findViewById(R.id.usernameInput);
            final EditText passwordInput = findViewById(R.id.passwordInput);
            Button loginButton = findViewById(R.id.loginButton);
            
            if (usernameInput == null || passwordInput == null || loginButton == null) {
                Log.e(TAG, "Failed to find views");
                Toast.makeText(this, "界面初始化失败，请重试", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // 设置点击监听器
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String username = usernameInput.getText().toString();
                        String password = passwordInput.getText().toString();
                        
                        // 验证用户名和密码
                        if (username.equals(USERNAME) && password.equals(PASSWORD)) {
                            // 验证成功，跳转到计算器界面
                            Intent intent = new Intent(LoginActivity.this, CalculatorActivity.class);
                            // 传递用户名
                            intent.putExtra("username", username);
                            // 添加标志以确保正确的启动模式
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            try {
                                startActivity(intent);
                                Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to start CalculatorActivity: " + e.getMessage());
                                Toast.makeText(LoginActivity.this, "启动计算器失败，请重试", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // 验证失败，显示错误信息
                            Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Login process failed: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "登录过程出错，请重试", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Fatal error in onCreate: " + e.getMessage());
            Toast.makeText(this, "应用启动失败，请重试", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        try {
            // 清除可能的残留数据
            EditText usernameInput = findViewById(R.id.usernameInput);
            EditText passwordInput = findViewById(R.id.passwordInput);
            if (usernameInput != null) usernameInput.setText("");
            if (passwordInput != null) passwordInput.setText("");
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage());
        }
    }
} 