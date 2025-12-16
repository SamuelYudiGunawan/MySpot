package com.example.myspot.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myspot.R;
import com.example.myspot.ui.home.HomeActivity;

public class SplashActivity extends AppCompatActivity {
    
    private static final int SPLASH_DURATION = 2000; // 2 seconds
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Delay navigation to home screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish(); // Close splash screen
        }, SPLASH_DURATION);
    }
}

