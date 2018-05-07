package com.example.caden.drawingtest;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RVHistoryAdapter extends RecyclerView.Adapter<RVHistoryAdapter.HistoryViewHolder> {

    private List<Wurm> wurms;
    private SparseArray<Bitmap> bmps;

    RVHistoryAdapter() {
        this.wurms = new ArrayList<>();
        this.bmps = new SparseArray<>();
    }

    public void addWurm(Wurm w) {
        this.wurms.add(w);
        this.notifyDataSetChanged();
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HistoryViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_row, parent, false));
    }

    public void updateImage(int i, Bitmap bmp) {
        bmps.put(i, bmp);
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        Wurm wurm = this.wurms.get(position);
        if (bmps.get(SharedData.totalWurmsDrawn - position - 1) != null)
            holder.ivHistory.setImageBitmap(bmps.get(SharedData.totalWurmsDrawn - position - 1));
        holder.tvDateTime.setText(String.format(Locale.getDefault(),
                "%s | %s", wurm.getWurmDate(), wurm.getWurmTime()));
        holder.tvFileName.setText(String.format(Locale.getDefault(),
                "%s / %s.png", wurm.getWurmBatchName(), wurm.getWurmImgNo()));
        holder.tvItemNo.setText(String.valueOf(SharedData.totalWurmsDrawn - position));
    }

    @Override
    public int getItemCount() {
        return wurms.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivHistory;
        private TextView tvDateTime;
        private TextView tvFileName;
        private TextView tvItemNo;

        HistoryViewHolder(View itemView) {
            super(itemView);
            ivHistory = itemView.findViewById(R.id.iv_history);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvItemNo = itemView.findViewById(R.id.tv_item_no);
        }
    }
}
