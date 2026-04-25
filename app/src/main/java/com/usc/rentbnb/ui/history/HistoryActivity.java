package com.usc.rentbnb.ui.history;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.usc.rentbnb.R;

public class HistoryActivity extends AppCompatActivity {

    private TextView[] filterChips;
    private ImageView backButton;
    private LinearLayout historyHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        setupFilterChips();

        // Handle the Back Button
        backButton = findViewById(R.id.back_button);
        historyHeader = findViewById(R.id.history_header);

        ViewCompat.setOnApplyWindowInsetsListener(historyHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + 8,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });


        backButton.setOnClickListener(v -> {
            finish();
        });

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
        // 1. Check if the user is trying to DESELECT the currently active chip
        if (selectedChip.getCurrentTextColor() == Color.WHITE) {
            // Remove the selected background
            selectedChip.setBackgroundResource(R.drawable.chip_background_teal);

            // Fade the text smoothly back to Black
            ValueAnimator deselectAnim = ValueAnimator.ofArgb(Color.WHITE, Color.BLACK);
            deselectAnim.setDuration(200);
            deselectAnim.addUpdateListener(animator -> selectedChip.setTextColor((int) animator.getAnimatedValue()));
            deselectAnim.start();

            return; // Stop here so it doesn't get re-selected!
        }

        // 2. Otherwise, the user clicked a new unselected chip.
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

        // 3. Apply the selected styles to the newly clicked chip
        selectedChip.setBackgroundResource(R.drawable.chip_background_selected);

        // Smooth color transition from its current color to White
        ValueAnimator colorAnimation = ValueAnimator.ofArgb(selectedChip.getCurrentTextColor(), Color.WHITE);
        colorAnimation.setDuration(200);
        colorAnimation.addUpdateListener(animator -> selectedChip.setTextColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }
}