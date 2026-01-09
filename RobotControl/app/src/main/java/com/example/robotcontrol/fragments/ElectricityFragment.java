package com.example.robotcontrol.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.robotcontrol.R;

/**
 * Fragment for Electricity education content
 */
public class ElectricityFragment extends Fragment {
    
    private TextView tvContent;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_electricity, container, false);
        
        initializeViews(view);
        loadContent();
        
        return view;
    }
    
    private void initializeViews(View view) {
        tvContent = view.findViewById(R.id.tvElectricityContent);
    }
    
    private void loadContent() {
        // Load electricity content
        // This can be enhanced with database or JSON content
        String content = "Learn about:\n\n" +
                "• Voltage and Current\n" +
                "• Ohm's Law (V = I × R)\n" +
                "• Power Calculations\n" +
                "• Series and Parallel Circuits\n" +
                "• Basic Electrical Safety";
        
        if (tvContent != null) {
            tvContent.setText(content);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Track learning progress
    }
}
