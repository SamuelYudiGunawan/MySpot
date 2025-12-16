package com.example.myspot.ui.newspot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.myspot.R;
import com.example.myspot.model.Category;
import com.example.myspot.model.Spot;
import com.example.myspot.repository.CategoryRepository;
import com.example.myspot.repository.SpotRepository;
import com.example.myspot.service.LocationService;
import com.example.myspot.service.PlusCodeService;
import com.example.myspot.util.ImageUtils;
import android.widget.EditText;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewSpotActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int PERMISSION_REQUEST_IMAGE = 3;
    
    private TextInputEditText etTitle;
    private EditText etJournal;
    private AutoCompleteTextView actvCategory;
    private ImageView ivSpotImage;
    private Button btnUploadImage;
    private FloatingActionButton fabSave;
    private android.widget.ProgressBar progressBar;
    
    private Uri selectedImageUri;
    
    private SpotRepository spotRepository;
    private CategoryRepository categoryRepository;
    private LocationService locationService;
    private PlusCodeService plusCodeService;
    
    private List<Category> categories;
    private ArrayAdapter<String> categoryAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_spot);
        
        spotRepository = new SpotRepository(getApplication());
        categoryRepository = new CategoryRepository(getApplication());
        locationService = new LocationService(this);
        plusCodeService = new PlusCodeService();
        
        initializeViews();
        setupCategoryDropdown();
        setupImageUpload();
        setupSaveButton();
        setupBottomNavigation();
        
        // Initialize default categories if needed
        categoryRepository.initializeDefaultCategories(new SpotRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadCategories();
            }
            
            @Override
            public void onError(Exception error) {
                error.printStackTrace();
            }
        });
    }
    
    private void initializeViews() {
        etTitle = findViewById(R.id.etTitle);
        actvCategory = findViewById(R.id.actvCategory);
        etJournal = findViewById(R.id.etJournal);
        ivSpotImage = findViewById(R.id.ivSpotImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        fabSave = findViewById(R.id.fabSave);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupCategoryDropdown() {
        categories = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        actvCategory.setAdapter(categoryAdapter);
        actvCategory.setThreshold(0); // Show dropdown immediately
        
        // Show dropdown when clicked
        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
        
        // Also handle focus
        actvCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actvCategory.showDropDown();
            }
        });
        
        loadCategories();
    }
    
    private void loadCategories() {
        categoryRepository.getAllCategories(new SpotRepository.RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                runOnUiThread(() -> {
                    categories = result;
                    List<String> categoryNames = new ArrayList<>();
                    for (Category cat : categories) {
                        categoryNames.add(cat.getName());
                    }
                    categoryAdapter.clear();
                    categoryAdapter.addAll(categoryNames);
                    categoryAdapter.notifyDataSetChanged();
                });
            }
            
            @Override
            public void onError(Exception error) {
                error.printStackTrace();
            }
        });
    }
    
    private void setupImageUpload() {
        btnUploadImage.setOnClickListener(v -> requestImagePermission());
    }
    
    private void requestImagePermission() {
        // Check Android version for permission handling
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_IMAGE);
            } else {
                openImagePicker();
            }
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_IMAGE);
            } else {
                openImagePicker();
            }
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = ImageUtils.loadBitmapFromUri(this, selectedImageUri);
                if (bitmap != null) {
                    ivSpotImage.setImageBitmap(bitmap);
                    ivSpotImage.setVisibility(android.view.View.VISIBLE);
                    btnUploadImage.setText("Change Image");
                } else {
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_IMAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Image permission is required to upload images", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveSpot(); // Retry saving
            } else {
                Toast.makeText(this, "Location permission is required to save spots", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void setupSaveButton() {
        fabSave.setOnClickListener(v -> saveSpot());
    }
    
    private void saveSpot() {
        String title = etTitle.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String journal = etJournal.getText().toString().trim();
        
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
            return;
        }
        
        // Show loading and disable save button
        setLoadingState(true);
        
        // Get location
        locationService.getCurrentLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String plusCode = plusCodeService.encode(latitude, longitude);
                
                // Compress and save image if selected
                String imagePath = null;
                if (selectedImageUri != null) {
                    imagePath = ImageUtils.compressAndSaveImage(NewSpotActivity.this, selectedImageUri);
                    if (imagePath == null) {
                        runOnUiThread(() -> {
                            Toast.makeText(NewSpotActivity.this, "Error compressing image", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                
                Spot spot = new Spot(title, category, journal, latitude, longitude, plusCode, imagePath);
                
                spotRepository.insertSpot(spot, new SpotRepository.RepositoryCallback<Long>() {
                    @Override
                    public void onSuccess(Long result) {
                        runOnUiThread(() -> {
                            setLoadingState(false);
                            Toast.makeText(NewSpotActivity.this, "Spot saved successfully", Toast.LENGTH_SHORT).show();
                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        });
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        runOnUiThread(() -> {
                            setLoadingState(false);
                            Toast.makeText(NewSpotActivity.this, "Error saving spot: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    Toast.makeText(NewSpotActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void setLoadingState(boolean isLoading) {
        if (fabSave != null) {
            fabSave.setEnabled(!isLoading);
            fabSave.setAlpha(isLoading ? 0.5f : 1.0f);
        }
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }
    
    private void setupBottomNavigation() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(NewSpotActivity.this, com.example.myspot.ui.home.HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_new) {
                    // Already on this screen
                    return true;
                } else if (itemId == R.id.nav_timeline) {
                    Intent intent = new Intent(NewSpotActivity.this, com.example.myspot.ui.timeline.TimelineActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                    return true;
                }
                return false;
            });
            // Set the current item as selected
            bottomNav.setSelectedItemId(R.id.nav_new);
        }
    }
    
}

