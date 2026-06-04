package com.carlauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppGridAdapter extends RecyclerView.Adapter<AppGridAdapter.AppVH> {

    public interface OnAppClick { void onClick(String packageName); }

    private final Context ctx;
    private List<AppItem> apps;
    private final OnAppClick listener;

    public AppGridAdapter(Context ctx, List<AppItem> apps, OnAppClick listener) {
        this.ctx = ctx;
        this.apps = apps;
        this.listener = listener;
    }

    public void updateApps(List<AppItem> newApps) {
        this.apps = newApps;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_app, parent, false);
        return new AppVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AppVH h, int pos) {
        AppItem item = apps.get(pos);
        h.icon.setImageDrawable(item.icon);
        h.name.setText(item.name);
        h.itemView.setOnClickListener(v -> listener.onClick(item.packageName));
    }

    @Override
    public int getItemCount() { return apps.size(); }

    static class AppVH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        AppVH(View v) {
            super(v);
            icon = v.findViewById(R.id.ivAppIcon);
            name = v.findViewById(R.id.tvAppName);
        }
    }
}
