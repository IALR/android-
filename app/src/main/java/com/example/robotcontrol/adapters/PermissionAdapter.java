package com.example.robotcontrol.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robotcontrol.R;
import com.example.robotcontrol.models.RobotPermission;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder> {

    private Context context;
    private List<RobotPermission> permissionList;
    private OnPermissionActionListener listener;

    public interface OnPermissionActionListener {
        void onRevokePermission(RobotPermission permission);
    }

    public PermissionAdapter(Context context, List<RobotPermission> permissionList, 
                            OnPermissionActionListener listener) {
        this.context = context;
        this.permissionList = permissionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_permission, parent, false);
        return new PermissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PermissionViewHolder holder, int position) {
        RobotPermission permission = permissionList.get(position);

        holder.userEmail.setText(permission.getUserEmail());

        holder.revokeButton.setOnClickListener(v -> listener.onRevokePermission(permission));
    }

    @Override
    public int getItemCount() {
        return permissionList.size();
    }

    static class PermissionViewHolder extends RecyclerView.ViewHolder {
        TextView userEmail;
        Button revokeButton;

        public PermissionViewHolder(@NonNull View itemView) {
            super(itemView);
            userEmail = itemView.findViewById(R.id.userEmailText);
            revokeButton = itemView.findViewById(R.id.revokeButton);
        }
    }
}
