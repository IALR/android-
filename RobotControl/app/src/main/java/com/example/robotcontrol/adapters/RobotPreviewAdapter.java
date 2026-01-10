package com.example.robotcontrol.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robotcontrol.R;
import com.example.robotcontrol.models.Robot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RobotPreviewAdapter extends RecyclerView.Adapter<RobotPreviewAdapter.ViewHolder> {

    public interface OnRobotPreviewClickListener {
        void onRobotPreviewClick(Robot robot);
    }

    private final Context context;
    private final List<Robot> robots;
    private final OnRobotPreviewClickListener listener;

    public RobotPreviewAdapter(Context context, List<Robot> robots, OnRobotPreviewClickListener listener) {
        this.context = context;
        this.robots = robots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_robot_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Robot robot = robots.get(position);

        String name = robot.getName();
        holder.tvName.setText(name == null || name.trim().isEmpty() ? "Robot" : name);

        String type = robot.getType();
        holder.tvType.setText(type == null || type.trim().isEmpty() ? "" : type);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        holder.tvLastSeen.setText(sdf.format(new Date(robot.getLastConnected())));

        if ("wifi".equalsIgnoreCase(robot.getConnectionType())) {
            holder.ivConnType.setImageResource(R.drawable.ic_wifi);
        } else {
            holder.ivConnType.setImageResource(R.drawable.ic_bluetooth);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRobotPreviewClick(robot);
        });
    }

    @Override
    public int getItemCount() {
        return robots.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivConnType;
        final TextView tvName;
        final TextView tvType;
        final TextView tvLastSeen;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivConnType = itemView.findViewById(R.id.ivRobotConnectionIcon);
            tvName = itemView.findViewById(R.id.tvRobotName);
            tvType = itemView.findViewById(R.id.tvRobotType);
            tvLastSeen = itemView.findViewById(R.id.tvRobotLastSeen);
        }
    }
}
