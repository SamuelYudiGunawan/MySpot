package com.example.myspot.service;

import android.content.Intent;
import android.net.Uri;
import android.content.Context;

public class NavigationService {
    
    /**
     * Creates a Google Maps navigation intent
     * @param context The application context
     * @param latitude The destination latitude
     * @param longitude The destination longitude
     * @return Intent for navigation
     */
    public Intent createNavigationIntent(Context context, double latitude, double longitude) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        
        // If Google Maps is not installed, fall back to web browser
        if (mapIntent.resolveActivity(context.getPackageManager()) == null) {
            String url = "https://www.google.com/maps/dir/?api=1&destination=" + latitude + "," + longitude;
            mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
        
        return mapIntent;
    }
    
    /**
     * Creates a Google Maps URL for sharing
     * @param latitude The destination latitude
     * @param longitude The destination longitude
     * @return URL string
     */
    public String createMapsUrl(double latitude, double longitude) {
        return "https://www.google.com/maps?q=" + latitude + "," + longitude;
    }
    
    /**
     * Creates a share intent with location information
     * @param title The spot title
     * @param latitude The destination latitude
     * @param longitude The destination longitude
     * @return Intent for sharing
     */
    public Intent createShareIntent(String title, double latitude, double longitude) {
        String mapsUrl = createMapsUrl(latitude, longitude);
        String shareText = title + "\n" + mapsUrl;
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        
        return Intent.createChooser(shareIntent, "Share location");
    }
}

