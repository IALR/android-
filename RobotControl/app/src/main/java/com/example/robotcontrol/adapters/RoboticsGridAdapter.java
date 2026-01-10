package com.example.robotcontrol.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robotcontrol.R;

import java.util.List;

public class RoboticsGridAdapter extends RecyclerView.Adapter<RoboticsGridAdapter.CellViewHolder> {

    public static final char CELL_EMPTY = ' ';
    public static final char CELL_START = 'S';
    public static final char CELL_GOAL = 'G';
    public static final char CELL_OBSTACLE = '#';
    public static final char CELL_ROBOT = 'R';

    private final List<Character> cells;

    public RoboticsGridAdapter(@NonNull List<Character> cells) {
        this.cells = cells;
    }

    @NonNull
    @Override
    public CellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_robotics_cell, parent, false);
        return new CellViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CellViewHolder holder, int position) {
        char c = cells.get(position);
        holder.tvCell.setText(c == CELL_EMPTY ? "" : String.valueOf(c));
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    static class CellViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCell;

        CellViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCell = itemView.findViewById(R.id.tvCell);
        }
    }
}
