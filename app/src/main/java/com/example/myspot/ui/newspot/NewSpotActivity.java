package com.example.myspot.ui.newspot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
import java.util.ArrayList;
import java.util.List;

public class NewSpotActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int PERMISSION_REQUEST_IMAGE = 3;
    private static final int CAMERA_REQUEST = 4;
    private static final int PERMISSION_REQUEST_CAMERA = 5;
    
    private TextInputEditText etTitle;
    private EditText etJournal;
    private AutoCompleteTextView actvCategory;
    private ImageView ivSpotImage;
    private Button btnUploadImage;
    private FloatingActionButton fabSave;
    private android.widget.ProgressBar progressBar;
    
    private Uri selectedImageUri;
    private File cameraImageFile;
    private static final String KEY_CAMERA_IMAGE_PATH = "camera_image_path";
    
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
        
        // Restore camera image file path if activity was recreated
        if (savedInstanceState != null) {
            String savedPath = savedInstanceState.getString(KEY_CAMERA_IMAGE_PATH);
            if (savedPath != null) {
                cameraImageFile = new File(savedPath);
            }
        }
        
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
        btnUploadImage.setOnClickListener(v -> showImageSourceDialog());
    }
    
    private void showImageSourceDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(new String[]{"Gallery", "Camera"}, (dialog, which) -> {
                if (which == 0) {
                    // Gallery
                    requestImagePermission();
                } else {
                    // Camera
                    requestCameraPermission();
                }
            })
            .show();
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
    
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            openCamera();
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Continue only if the File was successfully created
            if (photoFile != null) {
                try {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.myspot.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        
        // Always use external files directory (Pictures) for FileProvider compatibility
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            // Fallback to external files root if Pictures directory doesn't exist
            storageDir = getExternalFilesDir(null);
            if (storageDir == null) {
                // Last resort: use internal files directory
                storageDir = getFilesDir();
            }
        }
        
        // Ensure directory exists
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        
        // Save file reference for later use
        cameraImageFile = image;
        return image;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                // Gallery image selected
                selectedImageUri = data.getData();
                displaySelectedImage();
            } else if (requestCode == CAMERA_REQUEST) {
                // Camera image captured
                if (cameraImageFile != null && cameraImageFile.exists()) {
                    try {
                        selectedImageUri = Uri.fromFile(cameraImageFile);
                        displaySelectedImage();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error processing captured image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == RESULT_CANCELED && requestCode == CAMERA_REQUEST) {
            // User cancelled camera, clean up
            if (cameraImageFile != null && cameraImageFile.exists()) {
                cameraImageFile.delete();
            }
        }
    }
    
    private void displaySelectedImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Bitmap bitmap = ImageUtils.loadBitmapFromUri(this, selectedImageUri);
            if (bitmap != null) {
                if (ivSpotImage != null) {
                    ivSpotImage.setImageBitmap(bitmap);
                    ivSpotImage.setVisibility(android.view.View.VISIBLE);
                }
                if (btnUploadImage != null) {
                    btnUploadImage.setText("Change Image");
                }
            } else {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save camera image file path to restore after activity recreation
        if (cameraImageFile != null) {
            outState.putString(KEY_CAMERA_IMAGE_PATH, cameraImageFile.getAbsolutePath());
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
        } else if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_LONG).show();
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

