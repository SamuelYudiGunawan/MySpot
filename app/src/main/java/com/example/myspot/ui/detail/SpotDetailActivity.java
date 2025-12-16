package com.example.myspot.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.File;
import com.example.myspot.R;
import com.example.myspot.model.Spot;
import com.example.myspot.repository.SpotRepository;
import com.example.myspot.service.NavigationService;
import com.example.myspot.util.DateUtils;
import com.example.myspot.util.ImageUtils;
import android.graphics.Bitmap;

public class SpotDetailActivity extends AppCompatActivity {
    private TextView tvSpotTitle, tvSpotCategory, tvSpotDate, tvJournal;
    private Button btnNavigate, btnShare, btnDelete;
    private WebView wvMap;
    
    private SpotRepository spotRepository;
    private NavigationService navigationService;
    private Spot currentSpot;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_detail);
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(R.string.label);
            }
            toolbar.setNavigationOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            });
        }
        
        spotRepository = new SpotRepository(getApplication());
        navigationService = new NavigationService();
        
        initializeViews();
        
        long spotId = getIntent().getLongExtra("spot_id", -1);
        if (spotId != -1) {
            loadSpot(spotId);
        } else {
            // Handle case where spot_id is missing
            finish();
        }
    }
    
    private void initializeViews() {
        tvSpotTitle = findViewById(R.id.tvSpotTitle);
        tvSpotCategory = findViewById(R.id.tvSpotCategory);
        tvSpotDate = findViewById(R.id.tvSpotDate);
        tvJournal = findViewById(R.id.tvJournal);
        btnNavigate = findViewById(R.id.btnNavigate);
        btnShare = findViewById(R.id.btnShare);
        btnDelete = findViewById(R.id.btnDelete);
        wvMap = findViewById(R.id.wvMap);
        
        // Configure WebView safely
        if (wvMap != null) {
            try {
                wvMap.getSettings().setJavaScriptEnabled(true);
                wvMap.setWebViewClient(new WebViewClient());
                // Disable zoom controls for cleaner look
                wvMap.getSettings().setBuiltInZoomControls(false);
                wvMap.getSettings().setDisplayZoomControls(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (btnNavigate != null) {
            btnNavigate.setOnClickListener(v -> navigateToSpot());
        }
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> shareSpot());
        }
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        }
    }
    
    private void loadSpot(long spotId) {
        spotRepository.getSpotById(spotId, new SpotRepository.RepositoryCallback<Spot>() {
            @Override
            public void onSuccess(Spot result) {
                runOnUiThread(() -> {
                    if (result != null) {
                        currentSpot = result;
                        displaySpot(result);
                    } else {
                        // Spot not found
                        finish();
                    }
                });
            }
            
            @Override
            public void onError(Exception error) {
                error.printStackTrace();
                runOnUiThread(() -> {
                    // Show error and finish
                    finish();
                });
            }
        });
    }
    
    private void displaySpot(Spot spot) {
        if (spot == null) {
            finish();
            return;
        }
        
        if (tvSpotTitle != null) {
            tvSpotTitle.setText(spot.getTitle() != null ? spot.getTitle() : "Untitled");
        }
        if (tvSpotCategory != null) {
            tvSpotCategory.setText(spot.getCategory() != null ? spot.getCategory() : "");
        }
        if (tvSpotDate != null && spot.getCreatedAt() != null) {
            tvSpotDate.setText(DateUtils.formatDate(spot.getCreatedAt()));
        }
        
        String journal = spot.getJournal();
        if (tvJournal != null) {
            if (journal != null && !journal.isEmpty()) {
                tvJournal.setText(journal);
            } else {
                tvJournal.setText("No journal entry");
            }
        }
        
        // Load and display image if available
        ImageView ivPlaceholder = findViewById(R.id.ivPlaceholder);
        if (ivPlaceholder != null) {
            if (spot.getImageUri() != null && !spot.getImageUri().isEmpty()) {
                Bitmap bitmap = ImageUtils.loadBitmapFromPath(spot.getImageUri());
                if (bitmap != null) {
                    ivPlaceholder.setImageBitmap(bitmap);
                    ivPlaceholder.setBackground(null);
                } else {
                    // If image failed to load, show placeholder
                    ivPlaceholder.setImageBitmap(null);
                    ivPlaceholder.setBackgroundResource(R.color.purple_200);
                }
            } else {
                // No image, show placeholder
                ivPlaceholder.setImageBitmap(null);
                ivPlaceholder.setBackgroundResource(R.color.purple_200);
            }
        }
        
        // Load map in WebView safely - using iframe embed for clean map view with marker
        if (wvMap != null && navigationService != null) {
            try {
                double lat = spot.getLatitude();
                double lng = spot.getLongitude();
                // Use Google Maps with location parameter - shows map with marker, no buttons
                String embedHtml = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>" +
                        "<body style='margin:0;padding:0;overflow:hidden;'>" +
                        "<iframe width='100%' height='100%' frameborder='0' style='border:0' " +
                        "src='https://www.google.com/maps?q=" + lat + "," + lng + "&hl=en&z=15&output=embed'>" +
                        "</iframe></body></html>";
                wvMap.loadDataWithBaseURL(null, embedHtml, "text/html", "utf-8", null);
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback: use simple maps URL
                try {
                    String mapsUrl = navigationService.createMapsUrl(spot.getLatitude(), spot.getLongitude());
                    wvMap.loadUrl(mapsUrl);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    if (wvMap != null) {
                        wvMap.loadData("<html><body><p>Map unavailable</p></body></html>", "text/html", "utf-8");
                    }
                }
            }
        }
    }
    
    private void navigateToSpot() {
        if (currentSpot != null) {
            Intent intent = navigationService.createNavigationIntent(this, 
                    currentSpot.getLatitude(), currentSpot.getLongitude());
            startActivity(intent);
            // No transition for external apps
        }
    }
    
    private void shareSpot() {
        if (currentSpot != null) {
            Intent intent = navigationService.createShareIntent(
                    currentSpot.getTitle(),
                    currentSpot.getLatitude(),
                    currentSpot.getLongitude());
            startActivity(intent);
            // No transition for external apps
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showDeleteConfirmation() {
        if (currentSpot == null) {
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Delete Spot")
            .setMessage("Are you sure you want to delete \"" + currentSpot.getTitle() + "\"? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteSpot())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteSpot() {
        if (currentSpot == null) {
            return;
        }
        
        // Delete associated image file if it exists
        if (currentSpot.getImageUri() != null && !currentSpot.getImageUri().isEmpty()) {
            try {
                File imageFile = new File(currentSpot.getImageUri());
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Delete spot from database
        spotRepository.deleteSpot(currentSpot, new SpotRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(SpotDetailActivity.this, "Spot deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                });
            }
            
            @Override
            public void onError(Exception error) {
                runOnUiThread(() -> {
                    Toast.makeText(SpotDetailActivity.this, "Error deleting spot: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wvMap != null) {
            wvMap.destroy();
        }
    }
}

