package com.usc.rentbnb.ui.profile;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.usc.rentbnb.R;

public class ProfileActivity extends AppCompatActivity {

    private TextView[] filterChips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        setupFilterChips();
        setupDisabledNavBar();
    }

    private void setupDisabledNavBar() {
        ImageView navHome = findViewById(R.id.navHome);
        ImageView navFavorites = findViewById(R.id.navFavorites);
        ImageView navBookings = findViewById(R.id.navBookings);
        ImageView navProfile = findViewById(R.id.navProfile); // This is your Profile icon

        // Make the Profile tab look active (100% opacity)
        if (navProfile != null) navProfile.setAlpha(1.0f);

        // Make sure the others look inactive (65% opacity)
        if (navHome != null) navHome.setAlpha(0.65f);
        if (navFavorites != null) navFavorites.setAlpha(0.65f);
        if (navBookings != null) navBookings.setAlpha(0.65f);
    }

    private void setupFilterChips() {
        TextView chipCamera = findViewById(R.id.chip_camera);
        TextView chipSnorkel = findViewById(R.id.chip_snorkel);
        TextView chipMotorcycle = findViewById(R.id.chip_motorcycle);
        TextView chipBike = findViewById(R.id.chip_bike);
        TextView chipLabel = findViewById(R.id.chip_label);

        filterChips = new TextView[]{chipCamera, chipSnorkel, chipMotorcycle, chipBike, chipLabel};

        for (TextView chip : filterChips) {
            if (chip != null) {
                chip.setOnTouchListener((v, event) -> {
                    TextView clickedChip = (TextView) v;

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            clickedChip.animate().scaleX(0.90f).scaleY(0.90f).setDuration(100).start();
                            break;

                        case MotionEvent.ACTION_UP:
                            clickedChip.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                            handleChipSelection(clickedChip);
                            v.performClick();
                            break;

                        case MotionEvent.ACTION_CANCEL:
                            clickedChip.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                            break;
                    }
                    return true;
                });
            }
        }
    }

    private void handleChipSelection(TextView selectedChip) {
        if (selectedChip.getCurrentTextColor() == Color.WHITE) {
            return;
        }

        for (TextView chip : filterChips) {
            if (chip != null && chip != selectedChip) {
                chip.setBackgroundResource(R.drawable.chip_background_teal);

                if (chip.getCurrentTextColor() != Color.BLACK) {
                    ValueAnimator colorAnim = ValueAnimator.ofArgb(chip.getCurrentTextColor(), Color.BLACK);
                    colorAnim.setDuration(200);
                    colorAnim.addUpdateListener(animator -> chip.setTextColor((int) animator.getAnimatedValue()));
                    colorAnim.start();
                }
            }
        }

        selectedChip.setBackgroundResource(R.drawable.chip_background_selected);

        ValueAnimator colorAnimation = ValueAnimator.ofArgb(selectedChip.getCurrentTextColor(), Color.WHITE);
        colorAnimation.setDuration(200);
        colorAnimation.addUpdateListener(animator -> selectedChip.setTextColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }
}