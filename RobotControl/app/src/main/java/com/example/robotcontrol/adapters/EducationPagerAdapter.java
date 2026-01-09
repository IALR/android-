package com.example.robotcontrol.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.robotcontrol.fragments.CircuitsFragment;
import com.example.robotcontrol.fragments.ElectricityFragment;
import com.example.robotcontrol.fragments.MechatronicsFragment;
import com.example.robotcontrol.fragments.QuizFragment;

/**
 * Adapter for education content ViewPager2
 * Manages navigation between educational topics
 */
public class EducationPagerAdapter extends FragmentStateAdapter {
    
    private static final int NUM_PAGES = 4;
    
    public EducationPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ElectricityFragment();
            case 1:
                return new CircuitsFragment();
            case 2:
                return new MechatronicsFragment();
            case 3:
                return new QuizFragment();
            default:
                return new ElectricityFragment();
        }
    }
    
    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
    
    /**
     * Get the title for each page
     */
    public String getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Electricity";
            case 1:
                return "Circuits";
            case 2:
                return "Mechatronics";
            case 3:
                return "Quiz";
            default:
                return "";
        }
    }
}
