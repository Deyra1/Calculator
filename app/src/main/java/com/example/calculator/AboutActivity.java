package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_about);
            
            // 设置返回按钮
            ImageButton btnBack = findViewById(R.id.btnBack);
            if (btnBack != null) {
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish(); // 返回上一个Activity
                    }
                });
            }
            
        } catch (Exception e) {
            // The layout may be invalid or the resource identifiers incorrect.
            // If the layout fails to load, create a simple fallback view
            setContentView(createFallbackView());
            Toast.makeText(this, "关于界面加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    // 创建一个简单的后备视图，以防主视图加载失败
    private View createFallbackView() {
        TextView textView = new TextView(this);
        textView.setText("关于界面加载失败，请返回重试");
        textView.setTextSize(20);
        textView.setPadding(50, 50, 50, 50);
        return textView;
    }
} 