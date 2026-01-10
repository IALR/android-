package com.example.robotcontrol.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.robotcontrol.R;
import com.example.robotcontrol.fragments.CircuitsFragment;
import com.example.robotcontrol.fragments.EducationTopicFragment;
import com.example.robotcontrol.fragments.ElectricityFragment;
import com.example.robotcontrol.fragments.MechatronicsFragment;

/**
 * Adapter for education content ViewPager2
 * Manages navigation between educational topics
 */
public class EducationPagerAdapter extends FragmentStateAdapter {
    
    private static final int NUM_PAGES = 9;

    private final FragmentActivity activity;
    
    public EducationPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.activity = fragmentActivity;
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
                return EducationTopicFragment.newInstance(
                        R.string.education_tab_arduino,
                        R.string.education_arduino_content
                );
            case 4:
                return EducationTopicFragment.newInstance(
                        R.string.education_tab_bluetooth_wifi,
                        R.string.education_bluetooth_wifi_content
                );
            case 5:
                return EducationTopicFragment.newInstance(
                        R.string.education_tab_motors_drivers,
                        R.string.education_motors_drivers_content
                );
            case 6:
                return EducationTopicFragment.newInstance(
                        R.string.education_tab_sensors,
                        R.string.education_sensors_content
                );
            case 7:
                return EducationTopicFragment.newInstance(
                        R.string.education_tab_power_safety,
                        R.string.education_power_safety_content
                );
            case 8:
                return EducationTopicFragment.newInstance(
                        R.string.education_tab_robot_building,
                        R.string.education_robot_building_content
                );
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
                return activity.getString(R.string.education_tab_electricity);
            case 1:
                return activity.getString(R.string.education_tab_circuits);
            case 2:
                return activity.getString(R.string.education_tab_mechatronics);
            case 3:
                return activity.getString(R.string.education_tab_arduino);
            case 4:
                return activity.getString(R.string.education_tab_bluetooth_wifi);
            case 5:
                return activity.getString(R.string.education_tab_motors_drivers);
            case 6:
                return activity.getString(R.string.education_tab_sensors);
            case 7:
                return activity.getString(R.string.education_tab_power_safety);
            case 8:
                return activity.getString(R.string.education_tab_robot_building);
            default:
                return "";
        }
    }
}
