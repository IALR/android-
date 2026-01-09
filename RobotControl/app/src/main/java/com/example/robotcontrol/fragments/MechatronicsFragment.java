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
 * Fragment for Mechatronics education content
 */
public class MechatronicsFragment extends Fragment {
    
    private TextView tvContent;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mechatronics, container, false);
        
        initializeViews(view);
        loadContent();
        
        return view;
    }
    
    private void initializeViews(View view) {
        tvContent = view.findViewById(R.id.tvMechatronicsContent);
    }
    
    private void loadContent() {
        // Load mechatronics content
        String content = "Learn about:\n\n" +
                "• Servo Motors and Control\n" +
                "• DC Motors and Motor Drivers\n" +
                "• Sensors (Ultrasonic, IR, Temperature)\n" +
                "• PWM (Pulse Width Modulation)\n" +
                "• Robot Kinematics\n" +
                "• Bluetooth/WiFi Communication\n" +
                "• Robot Assembly and Design";
        
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
