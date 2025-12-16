package com.example.myspot.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myspot.R;
import com.example.myspot.adapter.SpotAdapter;
import com.example.myspot.model.Category;
import com.example.myspot.model.Spot;
import com.example.myspot.repository.CategoryRepository;
import com.example.myspot.repository.SpotRepository;
import com.example.myspot.ui.detail.SpotDetailActivity;
import com.example.myspot.ui.newspot.NewSpotActivity;
import com.example.myspot.ui.timeline.TimelineActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    
    private RecyclerView rvSpots;
    private SpotAdapter adapter;
    private SpotRepository spotRepository;
    private CategoryRepository categoryRepository;
    private List<Spot> allSpots;
    private String currentFilter = "All";
    private boolean sortNewToOld = true;
    
    private AutoCompleteTextView actvFilter;
    private ArrayAdapter<String> filterAdapter;
    private ImageButton btnSort;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        spotRepository = new SpotRepository(getApplication());
        categoryRepository = new CategoryRepository(getApplication());
        allSpots = new ArrayList<>();
        
        initializeViews();
        setupRecyclerView();
        setupFilters();
        setupSort();
        setupBottomNavigation();
        
        // Initialize default categories
        categoryRepository.initializeDefaultCategories(new SpotRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadCategories();
            }
            
            @Override
            public void onError(Exception error) {
                error.printStackTrace();
                loadCategories();
            }
        });
        
        loadSpots();
    }
    
    private void initializeViews() {
        rvSpots = findViewById(R.id.rvSpots);
        actvFilter = findViewById(R.id.actvFilter);
        btnSort = findViewById(R.id.btnSort);
    }
    
    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        rvSpots.setLayoutManager(layoutManager);
        
        adapter = new SpotAdapter(allSpots, spot -> {
            Intent intent = new Intent(HomeActivity.this, SpotDetailActivity.class);
            intent.putExtra("spot_id", spot.getId());
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        
        rvSpots.setAdapter(adapter);
    }
    
    private void setupFilters() {
        // Create filter adapter with "All" option
        List<String> filterOptions = new ArrayList<>();
        filterOptions.add("All");
        filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filterOptions);
        actvFilter.setAdapter(filterAdapter);
        actvFilter.setThreshold(0); // Show dropdown immediately
        actvFilter.setText("All", false);
        
        // Show dropdown when clicked
        actvFilter.setOnClickListener(v -> actvFilter.showDropDown());
        
        // Set filter selection listener
        actvFilter.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            actvFilter.setText(selected, false);
            filterSpots(selected);
        });
        
        // Also handle when user types (though inputType is none, this is a fallback)
        actvFilter.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actvFilter.showDropDown();
            }
        });
    }
    
    private void loadCategories() {
        categoryRepository.getAllCategories(new SpotRepository.RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                runOnUiThread(() -> {
                    List<String> filterOptions = new ArrayList<>();
                    filterOptions.add("All");
                    for (Category cat : result) {
                        filterOptions.add(cat.getName());
                    }
                    filterAdapter.clear();
                    filterAdapter.addAll(filterOptions);
                    filterAdapter.notifyDataSetChanged();
                });
            }
            
            @Override
            public void onError(Exception error) {
                error.printStackTrace();
            }
        });
    }
    
    private void setupSort() {
        btnSort.setOnClickListener(v -> {
            sortNewToOld = !sortNewToOld;
            applySortAndFilter();
        });
    }
    
    private void filterSpots(String category) {
        currentFilter = category;
        applySortAndFilter();
    }
    
    private void applySortAndFilter() {
        List<Spot> filtered = new ArrayList<>(allSpots);
        
        // Apply filter
        if (!currentFilter.equals("All")) {
            filtered.removeIf(spot -> !spot.getCategory().equals(currentFilter));
        }
        
        // Apply sort
        if (sortNewToOld) {
            filtered.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
        } else {
            filtered.sort((a, b) -> a.getUpdatedAt().compareTo(b.getUpdatedAt()));
        }
        
        adapter.updateSpots(filtered);
    }
    
    private void loadSpots() {
        spotRepository.getAllSpots(new SpotRepository.RepositoryCallback<List<Spot>>() {
            @Override
            public void onSuccess(List<Spot> result) {
                runOnUiThread(() -> {
                    allSpots = result;
                    applySortAndFilter();
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
        bottomNav.setSelectedItemId(R.id.nav_home);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_new) {
                startActivity(new Intent(this, NewSpotActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (itemId == R.id.nav_timeline) {
                startActivity(new Intent(this, TimelineActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }
            return false;
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Ensure bottom nav shows correct selection when returning from other activities
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        loadSpots();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            }
        }
    }
}

