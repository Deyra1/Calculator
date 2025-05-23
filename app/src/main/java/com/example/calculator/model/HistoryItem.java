package com.example.calculator.model;

import java.io.Serializable;

/**
 * 历史记录项的数据模型
 */
public class HistoryItem implements Serializable {
    
    private String expression; // 计算表达式
    private String result;     // 计算结果
    private String time;       // 计算时间
    
    public HistoryItem() {
    }
    
    public HistoryItem(String expression, String result, String time) {
        this.expression = expression;
        this.result = result;
        this.time = time;
    }
    
    // Getters and Setters
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    @Override
    public String toString() {
        return "HistoryItem{" +
                "expression='" + expression + '\'' +
                ", result='" + result + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
} 