package com.example.calculator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.R;
import com.example.calculator.model.HistoryItem;

import java.util.List;

/**
 * 历史记录列表适配器
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private List<HistoryItem> historyItems;
    private OnItemClickListener listener;

    public HistoryAdapter(Context context, List<HistoryItem> historyItems) {
        this.context = context;
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyItems.get(position);
        holder.txtExpression.setText(item.getExpression());
        holder.txtResult.setText(item.getResult());
        holder.txtTime.setText(item.getTime());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyItems != null ? historyItems.size() : 0;
    }

    // 更新历史记录列表
    public void updateList(List<HistoryItem> newList) {
        this.historyItems = newList;
        notifyDataSetChanged();
    }

    // 清空历史记录
    public void clearHistory() {
        if (historyItems != null) {
            historyItems.clear();
            notifyDataSetChanged();
        }
    }

    // ViewHolder类
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtExpression;
        TextView txtResult;
        TextView txtTime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtExpression = itemView.findViewById(R.id.txtExpression);
            txtResult = itemView.findViewById(R.id.txtResult);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }

    // 点击事件接口
    public interface OnItemClickListener {
        void onItemClick(HistoryItem item, int position);
    }

    // 设置点击事件监听器
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
} 