package com.example.robotcontrol;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.robotcontrol.adapters.EducationPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Education Activity - Educational content hub
 * Contains learning materials for robotics and electronics
 */
public class EducationActivity extends AppCompatActivity {
    
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private EducationPagerAdapter pagerAdapter;
    private TextView tvTitle;
    
    private final String[] tabTitles = {
        "Electricity",
        "Circuits",
        "Mechatronics",
        "Quiz"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education);
        
        initializeViews();
        setupViewPager();
        setupTabLayout();
    }
    
    private void initializeViews() {
        tvTitle = findViewById(R.id.tvEducationTitle);
        viewPager = findViewById(R.id.viewPagerEducation);
        tabLayout = findViewById(R.id.tabLayoutEducation);
        
        // Set title
        if (tvTitle != null) {
            tvTitle.setText("Learn Robotics");
        }
        
        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Education Hub");
        }
    }
    
    private void setupViewPager() {
        pagerAdapter = new EducationPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        // Set page transformer for smooth transitions
        viewPager.setPageTransformer((page, position) -> {
            page.setAlpha(0f);
            page.setVisibility(View.VISIBLE);
            
            // Fade in/out
            page.setAlpha(1f - Math.abs(position));
            
            if (position < -1 || position > 1) {
                page.setAlpha(0f);
            } else if (position <= 0 || position <= 1) {
                // Calculate alpha
                float scaleFactor = Math.max(0.85f, 1 - Math.abs(position));
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
            }
        });
    }
    
    private void setupTabLayout() {
        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
        
        // Add icons to tabs (optional)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Track analytics or update UI
                saveProgress(tab.getPosition());
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Optional: handle tab unselection
            }
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional: handle tab reselection
            }
        });
    }
    
    private void saveProgress(int position) {
        // Save learning progress to database
        getSharedPreferences("LearningProgress", MODE_PRIVATE)
                .edit()
                .putInt("last_visited_tab", position)
                .putLong("last_visit_time", System.currentTimeMillis())
                .apply();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Restore last visited tab
        int lastTab = getSharedPreferences("LearningProgress", MODE_PRIVATE)
                .getInt("last_visited_tab", 0);
        viewPager.setCurrentItem(lastTab, false);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
