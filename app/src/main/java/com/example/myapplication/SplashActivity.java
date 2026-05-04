package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.DemoDataSeeder;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Hide status bar for a true splash experience
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        init();
    }

    private void init() {
        View logo = findViewById(R.id.iv_logo);
        View title = findViewById(R.id.tv_title);
        View tagline = findViewById(R.id.tv_tagline);
        View progress = findViewById(R.id.splash_progress);

        // Modern entry animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);
        
        logo.startAnimation(fadeIn);
        title.startAnimation(fadeIn);
        tagline.startAnimation(fadeIn);
        if (progress != null) progress.startAnimation(fadeIn);

        // Seed demo data if needed (running on background thread is usually better but keeping user logic)
        new Thread(() -> {
            DemoDataSeeder.seedIfNeeded(this);
        }).start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 2500); // Slightly longer for a premium feel
    }
}
