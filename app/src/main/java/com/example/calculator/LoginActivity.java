package com.example.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    // 定义固定的用户名和密码
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "123";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_login);
            
            // 找到用户名和密码输入框以及登录按钮
            final EditText usernameInput = findViewById(R.id.usernameInput);
            final EditText passwordInput = findViewById(R.id.passwordInput);
            Button loginButton = findViewById(R.id.loginButton);
            
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
                            startActivity(intent);
                            Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                        } else {
                            // 验证失败，显示错误信息
                            Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "登录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            // 如果布局加载失败，创建一个简单的后备布局
            setContentView(createFallbackView());
            e.printStackTrace();
        }
    }
    
    // 创建一个简单的后备视图，以防主视图加载失败
    private Button createFallbackView() {
        Button button = new Button(this);
        button.setText("进入计算器");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(LoginActivity.this, CalculatorActivity.class);
                    intent.putExtra("username", USERNAME);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "跳转失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        return button;
    }
} 