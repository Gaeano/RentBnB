package com.usc.rentbnb.ui.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.splashscreen.SplashScreen;

import com.airbnb.lottie.LottieAnimationView;
import android.graphics.Typeface;

import com.airbnb.lottie.FontAssetDelegate;
import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView lottieSplash = findViewById(R.id.lottieSplash);

        lottieSplash.setFontAssetDelegate(new FontAssetDelegate() {
            @Override
            public Typeface fetchFont(String fontFamily) {
                return ResourcesCompat.getFont(SplashActivity.this, R.font.cherry_bomb_one);
            }
        });

        lottieSplash.addAnimatorListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(@NonNull android.animation.Animator animation) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class)); // TODO: Implement first time user management (if first time user, go to onboarding + signup)
                finish();
            }
        });
    }
}