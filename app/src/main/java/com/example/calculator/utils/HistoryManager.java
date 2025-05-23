package com.example.calculator.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.calculator.model.HistoryItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 历史记录管理工具类
 */
public class HistoryManager {
    private static final String TAG = "HistoryManager";
    private static final String PREF_NAME = "calculator_history";
    private static final String KEY_HISTORY = "history_list";
    private static final int MAX_HISTORY_COUNT = 100; // 最多保存100条历史记录

    private final SharedPreferences preferences;
    private final Gson gson;

    public HistoryManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * 保存计算历史
     *
     * @param expression 计算表达式
     * @param result     计算结果
     */
    public void saveHistory(String expression, String result) {
        try {
            // 获取当前时间
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            
            // 创建新的历史记录项
            HistoryItem newItem = new HistoryItem(expression, result, currentTime);
            
            // 获取现有历史记录
            List<HistoryItem> historyList = getHistoryList();
            
            // 将新记录添加到列表开头（最新的记录在最前面）
            historyList.add(0, newItem);
            
            // 如果历史记录超过最大限制，移除最旧的记录
            if (historyList.size() > MAX_HISTORY_COUNT) {
                historyList = historyList.subList(0, MAX_HISTORY_COUNT);
            }
            
            // 保存更新后的历史记录
            saveHistoryList(historyList);
        } catch (Exception e) {
            Log.e(TAG, "Error saving history: " + e.getMessage());
        }
    }

    /**
     * 获取所有历史记录
     *
     * @return 历史记录列表
     */
    public List<HistoryItem> getHistoryList() {
        try {
            String json = preferences.getString(KEY_HISTORY, "");
            if (json.isEmpty()) {
                return new ArrayList<>();
            }
            
            Type type = new TypeToken<List<HistoryItem>>() {}.getType();
            List<HistoryItem> historyList = gson.fromJson(json, type);
            return historyList != null ? historyList : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error getting history: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 保存历史记录列表
     *
     * @param historyList 历史记录列表
     */
    private void saveHistoryList(List<HistoryItem> historyList) {
        try {
            String json = gson.toJson(historyList);
            preferences.edit().putString(KEY_HISTORY, json).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving history list: " + e.getMessage());
        }
    }

    /**
     * 清空所有历史记录
     */
    public void clearHistory() {
        preferences.edit().remove(KEY_HISTORY).apply();
    }
} 