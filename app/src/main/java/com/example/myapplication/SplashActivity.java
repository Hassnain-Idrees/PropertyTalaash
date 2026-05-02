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

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private View title;
    private View footer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        init();
    }

    private void init() {
        title = findViewById(R.id.tv_title);
        footer = findViewById(R.id.splash_footer);

        Animation titleAnim = AnimationUtils.loadAnimation(this, R.anim.splash_title);
        title.startAnimation(titleAnim);

        Animation footerAnim = AnimationUtils.loadAnimation(this, R.anim.splash_footer);
        footer.startAnimation(footerAnim);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {

                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 1400);
    }
}
