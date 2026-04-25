package com.usc.rentbnb.ui.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.splashscreen.SplashScreen;

import com.airbnb.lottie.LottieAnimationView;
import android.graphics.Typeface;
import android.widget.Toast;

import com.airbnb.lottie.FontAssetDelegate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.auth.LoginActivity;
import com.usc.rentbnb.ui.home.HomeActivity;

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
                checkSessionAndNavigate();
            }
        });
    }

    // gi migrate from signup
    private void checkSessionAndNavigate() {
        SharedPreferences sharedPreferences = getSharedPreferences("RentBnBPrefs", MODE_PRIVATE);
        boolean isRemembered = sharedPreferences.getBoolean("IS_REMEMBERED", false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null && isRemembered) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                } else {
                    purgeLocalSession(sharedPreferences);
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            });

        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void purgeLocalSession(SharedPreferences sharedPreferences) {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("IS_REMEMBERED", false);
        editor.putString("SAVED_EMAIL", "");
        editor.apply();
    }
}