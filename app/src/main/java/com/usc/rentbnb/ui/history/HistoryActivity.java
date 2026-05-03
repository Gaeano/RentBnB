package com.usc.rentbnb.ui.history;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.HistoryAdapter;
import com.usc.rentbnb.models.Booking;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private TextView[] filterChips;
    private ImageView backButton;
    private LinearLayout historyHeader;

    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private LinearLayout emptyStateLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        setupFilterChips();

        backButton = findViewById(R.id.back_button);
        historyHeader = findViewById(R.id.history_header);
        rvHistory = findViewById(R.id.rvHistory);
        emptyStateLayout = findViewById(R.id.empty_state_layout);

        ViewCompat.setOnApplyWindowInsetsListener(historyHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight + 8, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        backButton.setOnClickListener(v -> finish());

        // Setup RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter();
        rvHistory.setAdapter(historyAdapter);

        // Fetch dummy data just so the screen isn't empty
        fetchHistoryData();
    }

    private void fetchHistoryData() {
        List<Booking> dummyData = new ArrayList<>();

        dummyData.add(new Booking("1", "Yamaha NMAX", "Motorcycle", "Nicole Heredia", "₱1000.00 / day", "", "Oct 25 - Oct 28, 2024", "ACTIVE"));
        dummyData.add(new Booking("2", "GoPro Hero 11", "Camera", "Mark D.", "₱500.00 / day", "", "Nov 02 - Nov 05, 2024", "ACTIVE"));
        dummyData.add(new Booking("3", "Snorkel Gear Set", "Snorkel", "Cebu Dives", "₱250.00 / day", "", "Aug 10 - Aug 11, 2024", "COMPLETED"));
        dummyData.add(new Booking("4", "Mountain Bike", "Bike", "Dave's Rentals", "₱400.00 / day", "", "May 15 - May 16, 2024", "COMPLETED"));

        updateUI(dummyData);
    }

    private void updateUI(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            historyAdapter.setBookings(bookings);
        }
    }

    private void setupFilterChips() {
        TextView chipCamera = findViewById(R.id.chip_camera);
        TextView chipSnorkel = findViewById(R.id.chip_snorkel);
        TextView chipMotorcycle = findViewById(R.id.chip_motorcycle);
        TextView chipBike = findViewById(R.id.chip_bike);
        TextView chipLabel = findViewById(R.id.chip_label);

        // Group them into an array
        filterChips = new TextView[]{chipCamera, chipSnorkel, chipMotorcycle, chipBike, chipLabel};

        // Attach an OnTouchListener for the bounce/scale animations
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