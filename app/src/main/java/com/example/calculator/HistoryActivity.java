package com.example.calculator;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.adapter.HistoryAdapter;
import com.example.calculator.model.HistoryItem;
import com.example.calculator.utils.HistoryManager;

import java.util.List;

/**
 * 历史记录页面
 */
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private TextView txtNoHistory;
    private HistoryAdapter adapter;
    private HistoryManager historyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // 初始化视图
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        txtNoHistory = findViewById(R.id.txtNoHistory);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnClear = findViewById(R.id.btnClear);

        // 设置返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());

        // 设置清除历史记录按钮点击事件
        btnClear.setOnClickListener(v -> {
            // 显示确认对话框
            showConfirmClearDialog();
        });

        // 初始化历史记录管理器
        historyManager = new HistoryManager(this);

        // 设置RecyclerView
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        
        // 加载历史记录
        loadHistoryData();
    }

    /**
     * 加载历史记录数据
     */
    private void loadHistoryData() {
        List<HistoryItem> historyList = historyManager.getHistoryList();
        
        if (historyList.isEmpty()) {
            recyclerViewHistory.setVisibility(View.GONE);
            txtNoHistory.setVisibility(View.VISIBLE);
        } else {
            recyclerViewHistory.setVisibility(View.VISIBLE);
            txtNoHistory.setVisibility(View.GONE);

            // 初始化适配器
            adapter = new HistoryAdapter(this, historyList);
            recyclerViewHistory.setAdapter(adapter);
            
            // 设置点击事件
            adapter.setOnItemClickListener((item, position) -> {
                // 点击历史记录项时的处理
                // 可以返回计算器界面并自动填入表达式
                Toast.makeText(this, "已选择: " + item.getExpression(), Toast.LENGTH_SHORT).show();
                
                // 这里可以实现将历史记录返回给计算器页面
                // setResult(RESULT_OK, new Intent().putExtra("expression", item.getExpression()));
                // finish();
            });
        }
    }

    /**
     * 显示确认清除历史记录的对话框
     */
    private void showConfirmClearDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清除历史记录")
                .setMessage("确定要清除所有历史记录吗？此操作不可恢复。")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 清除历史记录
                    historyManager.clearHistory();
                    
                    // 刷新界面
                    if (adapter != null) {
                        adapter.clearHistory();
                    }
                    
                    recyclerViewHistory.setVisibility(View.GONE);
                    txtNoHistory.setVisibility(View.VISIBLE);
                    
                    Toast.makeText(this, "历史记录已清除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
} 