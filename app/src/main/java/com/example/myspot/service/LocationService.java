package com.example.myspot.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationService {
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    
    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onError(String error);
    }
    
    public LocationService(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }
    
    public void getCurrentLocation(LocationCallback callback) {
        // Check permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            if (callback != null) {
                callback.onError("Location permission not granted");
            }
            return;
        }
        
        // Create high-accuracy location request
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(true)  // Wait for accurate location
                .setMinUpdateIntervalMillis(500)   // Update every 500ms
                .setMaxUpdateDelayMillis(2000)      // Max delay 2 seconds
                .build();
        
        // Request location updates with high accuracy
        try {
            Task<Location> locationTask = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, null);
            
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Check if location has good accuracy (within 20 meters)
                        if (location.hasAccuracy() && location.getAccuracy() <= 20.0f) {
                            if (callback != null) {
                                callback.onLocationReceived(location);
                            }
                        } else {
                            // If accuracy is poor, request continuous updates until we get a good one
                            requestAccurateLocation(callback, locationRequest);
                        }
                    } else {
                        // No location available, request updates
                        requestAccurateLocation(callback, locationRequest);
                    }
                }
            });
        } catch (SecurityException e) {
            if (callback != null) {
                callback.onError("Security exception: " + e.getMessage());
            }
        }
    }
    
    private void requestAccurateLocation(LocationCallback callback, LocationRequest locationRequest) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            if (callback != null) {
                callback.onError("Location permission not granted");
            }
            return;
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, 
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                        if (locationResult != null && locationResult.getLastLocation() != null) {
                            Location location = locationResult.getLastLocation();
                            // Accept location if accuracy is good (within 20 meters) or if it's the best we can get
                            if (location.hasAccuracy() && location.getAccuracy() <= 20.0f) {
                                if (callback != null) {
                                    callback.onLocationReceived(location);
                                }
                                // Stop updates after getting accurate location
                                fusedLocationClient.removeLocationUpdates(this);
                            }
                        }
                    }
                }, null);
        } catch (SecurityException e) {
            if (callback != null) {
                callback.onError("Security exception: " + e.getMessage());
            }
        }
    }
    
    public void stopLocationUpdates() {
        // FusedLocationProviderClient handles this automatically
    }
}

