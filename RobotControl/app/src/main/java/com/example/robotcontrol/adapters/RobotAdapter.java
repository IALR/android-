package com.example.robotcontrol.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robotcontrol.R;
import com.example.robotcontrol.models.Robot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RobotAdapter extends RecyclerView.Adapter<RobotAdapter.RobotViewHolder> {

    private Context context;
    private List<Robot> robotList;
    private OnRobotClickListener listener;

    public interface OnRobotClickListener {
        void onRobotClick(Robot robot);
        void onRobotLongClick(Robot robot);
    }

    public RobotAdapter(Context context, List<Robot> robotList, OnRobotClickListener listener) {
        this.context = context;
        this.robotList = robotList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RobotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_robot, parent, false);
        return new RobotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RobotViewHolder holder, int position) {
        Robot robot = robotList.get(position);

        holder.robotName.setText(robot.getName());
        holder.robotType.setText(robot.getType());
        
        // Format last connected time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        String lastConnectedStr = sdf.format(new Date(robot.getLastConnected()));
        holder.lastConnected.setText("Last: " + lastConnectedStr);

        // Set connection status
        if (robot.isConnected()) {
            holder.connectionStatus.setImageResource(R.drawable.ic_connected);
            holder.statusText.setText("Connected");
            holder.statusText.setTextColor(context.getResources().getColor(R.color.success));
        } else {
            holder.connectionStatus.setImageResource(R.drawable.ic_disconnected);
            holder.statusText.setText("Disconnected");
            holder.statusText.setTextColor(context.getResources().getColor(R.color.text_secondary));
        }

        // Set connection type icon
        if ("wifi".equals(robot.getConnectionType())) {
            holder.connectionTypeIcon.setImageResource(R.drawable.ic_wifi);
        } else {
            holder.connectionTypeIcon.setImageResource(R.drawable.ic_bluetooth);
        }

        holder.itemView.setOnClickListener(v -> listener.onRobotClick(robot));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onRobotLongClick(robot);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return robotList.size();
    }

    static class RobotViewHolder extends RecyclerView.ViewHolder {
        TextView robotName, robotType, lastConnected, statusText;
        ImageView connectionStatus, connectionTypeIcon;

        public RobotViewHolder(@NonNull View itemView) {
            super(itemView);
            robotName = itemView.findViewById(R.id.robotName);
            robotType = itemView.findViewById(R.id.robotType);
            lastConnected = itemView.findViewById(R.id.lastConnectedText);
            statusText = itemView.findViewById(R.id.connectionStatusText);
            connectionStatus = itemView.findViewById(R.id.connectionStatusIcon);
            connectionTypeIcon = itemView.findViewById(R.id.connectionTypeIcon);
        }
    }
}
