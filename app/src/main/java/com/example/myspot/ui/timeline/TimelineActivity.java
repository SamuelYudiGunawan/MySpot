package com.example.myspot.ui.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myspot.R;
import com.example.myspot.adapter.TimelineAdapter;
import com.example.myspot.model.Spot;
import com.example.myspot.repository.SpotRepository;
import com.example.myspot.ui.detail.SpotDetailActivity;
import com.example.myspot.ui.home.HomeActivity;
import com.example.myspot.ui.newspot.NewSpotActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimelineActivity extends AppCompatActivity {
    private RecyclerView rvTimeline;
    private TimelineAdapter adapter;
    private SpotRepository spotRepository;
    private List<Spot> spots;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        
        spotRepository = new SpotRepository(getApplication());
        spots = new ArrayList<>();
        
        initializeViews();
        setupRecyclerView();
        setupBottomNavigation();
        
        loadSpots();
    }
    
    private void initializeViews() {
        rvTimeline = findViewById(R.id.rvTimeline);
    }
    
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTimeline.setLayoutManager(layoutManager);
        
        adapter = new TimelineAdapter(spots, spot -> {
            Intent intent = new Intent(TimelineActivity.this, SpotDetailActivity.class);
            intent.putExtra("spot_id", spot.getId());
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        
        rvTimeline.setAdapter(adapter);
    }
    
    private void loadSpots() {
        spotRepository.getAllSpots(new SpotRepository.RepositoryCallback<List<Spot>>() {
            @Override
            public void onSuccess(List<Spot> result) {
                runOnUiThread(() -> {
                    // Sort by creation date (newest first)
                    Collections.sort(result, (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                    spots = result;
                    adapter.updateSpots(spots);
                });
            }
            
            @Override
            public void onError(Exception error) {
                error.printStackTrace();
            }
        });
    }
    
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_timeline);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                return true;
            } else if (itemId == R.id.nav_new) {
                startActivity(new Intent(this, NewSpotActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                return true;
            } else if (itemId == R.id.nav_timeline) {
                // Already on timeline
                return true;
            }
            return false;
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadSpots();
    }
}

