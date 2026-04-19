package com.usc.rentbnb.ui.history;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.usc.rentbnb.R;

public class HistoryActivity extends AppCompatActivity {

    private TextView[] filterChips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        setupFilterChips();

        // Add this line to visually update the bottom nav
        setupDisabledNavBar();
    }

    private void setupDisabledNavBar() {
        ImageView navHome = findViewById(R.id.navHome);
        ImageView navFavorites = findViewById(R.id.navFavorites);
        ImageView navBookings = findViewById(R.id.navBookings); // This is your History icon
        ImageView navProfile = findViewById(R.id.navProfile);

        // Make the History tab look active (100% opacity)
        if (navBookings != null) {
            navBookings.setAlpha(1.0f);
        }

        // Make sure the others look inactive (65% opacity)
        if (navHome != null) navHome.setAlpha(0.65f);
        if (navFavorites != null) navFavorites.setAlpha(0.65f);
        if (navProfile != null) navProfile.setAlpha(0.65f);

        // We are intentionally NOT setting click listeners, so the buttons won't function!
    }

    private void setupFilterChips() {
        TextView chipCamera = findViewById(R.id.chip_camera);
        TextView chipSnorkel = findViewById(R.id.chip_snorkel);
        TextView chipMotorcycle = findViewById(R.id.chip_motorcycle);
        TextView chipBike = findViewById(R.id.chip_bike);
        TextView chipLabel = findViewById(R.id.chip_label);

        // Group them into an array
        filterChips = new TextView[]{chipCamera, chipSnorkel, chipMotorcycle, chipBike, chipLabel};

        // Attach an OnTouchListener instead of a ClickListener
        for (TextView chip : filterChips) {
            if (chip != null) {
                chip.setOnTouchListener((v, event) -> {
                    TextView clickedChip = (TextView) v;

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // 1. Shrink the chip instantly when the user presses and holds
                            clickedChip.animate().scaleX(0.90f).scaleY(0.90f).setDuration(100).start();
                            break;

                        case MotionEvent.ACTION_UP:
                            // 2. Bounce back to normal size when the finger is released
                            clickedChip.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();

                            // 3. Trigger the color transition
                            handleChipSelection(clickedChip);
                            v.performClick();
                            break;

                        case MotionEvent.ACTION_CANCEL:
                            // 4. If the user drags their finger off the chip, just bounce back
                            clickedChip.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                            break;
                    }
                    return true; // Consume the touch event
                });
            }
        }
    }

    private void handleChipSelection(TextView selectedChip) {
        // IMPORTANT FIX: If the chip is already selected (white text), ignore the click!
        // This prevents the text from briefly flashing black when tapped again.
        if (selectedChip.getCurrentTextColor() == Color.WHITE) {
            return;
        }

        // Reset all OTHER chips to their normal, unselected state
        for (TextView chip : filterChips) {
            if (chip != null && chip != selectedChip) {
                chip.setBackgroundResource(R.drawable.chip_background_teal);

                // If the text is currently white, fade it smoothly back to black
                if (chip.getCurrentTextColor() != Color.BLACK) {
                    ValueAnimator colorAnim = ValueAnimator.ofArgb(chip.getCurrentTextColor(), Color.BLACK);
                    colorAnim.setDuration(200);
                    colorAnim.addUpdateListener(animator -> chip.setTextColor((int) animator.getAnimatedValue()));
                    colorAnim.start();
                }
            }
        }

        // Apply the selected styles to the newly clicked chip
        selectedChip.setBackgroundResource(R.drawable.chip_background_selected);

        // Smooth color transition from its current color to White
        ValueAnimator colorAnimation = ValueAnimator.ofArgb(selectedChip.getCurrentTextColor(), Color.WHITE);
        colorAnimation.setDuration(200);
        colorAnimation.addUpdateListener(animator -> selectedChip.setTextColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }
}