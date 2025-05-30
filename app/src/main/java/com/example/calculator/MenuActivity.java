package com.example.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {
    private String username;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ThemePrefs";
    private static final String THEME_KEY = "selectedTheme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply saved theme before setting content view
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedTheme = sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        try {
            setContentView(R.layout.activity_menu);
            
            // 更新主题菜单项旁的文本
            updateThemeText();

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
            View menuItemTheme = findViewById(R.id.menuItemTheme);
            if (menuItemTheme != null) {
                menuItemTheme.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showThemeSelectionDialog();
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
    
    private void showThemeSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择主题");

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.activity_settings, null);
        RadioGroup themeRadioGroup = dialogLayout.findViewById(R.id.themeRadioGroup);

        // Load saved theme preference
        int savedTheme = sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        switch (savedTheme) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                themeRadioGroup.check(R.id.radioLight);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                themeRadioGroup.check(R.id.radioDark);
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
            default:
                themeRadioGroup.check(R.id.radioSystem);
                break;
        }

        themeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selectedTheme;
                if (checkedId == R.id.radioLight) {
                    selectedTheme = AppCompatDelegate.MODE_NIGHT_NO;
                } else if (checkedId == R.id.radioDark) {
                    selectedTheme = AppCompatDelegate.MODE_NIGHT_YES;
                } else {
                    selectedTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                }

                // Save and apply theme preference
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(THEME_KEY, selectedTheme);
                editor.apply();

                AppCompatDelegate.setDefaultNightMode(selectedTheme);
                 // Optionally close the dialog after selection, or keep it open.
                 // For now, let's keep it open until the user dismisses it.
                recreate(); // Recreate activity to apply the new theme
            }
        });

        builder.setView(dialogLayout);

        // Add a neutral button to dismiss the dialog
        builder.setNeutralButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
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

    private void updateThemeText() {
        TextView currentThemeTextView = findViewById(R.id.currentThemeText);
        if (currentThemeTextView != null) {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            String themeText;
            switch (currentMode) {
                case AppCompatDelegate.MODE_NIGHT_NO:
                    themeText = "浅色模式";
                    break;
                case AppCompatDelegate.MODE_NIGHT_YES:
                    themeText = "深色模式";
                    break;
                case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                default:
                    themeText = "跟随系统";
                    break;
            }
            currentThemeTextView.setText(themeText);
        }
    }
} 