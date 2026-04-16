package com.usc.rentbnb.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.auth.LoginActivity;
import com.usc.rentbnb.ui.listing.IslandDetailsActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView logout;
    private FirebaseAuth auth;
    private TextView[] filterChips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

//        logout = findViewById(R.id.logout_btn);
        auth = FirebaseAuth.getInstance();

        // --- 1. Setup Island Cards ---
        setupIslandCards();

        // --- 2. Setup Filter Chips ---
        setupFilterChips();

        // --- 3. Setup Logout ---
        if (logout != null) {
            logout.setOnClickListener(v -> {
                auth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupIslandCards() {
        // Create an array containing all the IDs of your island cards
        int[] cardIds = {
                R.id.island_card_1, R.id.island_card_2,
                R.id.island_card_3, R.id.island_card_4,
                R.id.island_card_5, R.id.island_card_6
        };

        // Loop through every ID in the array
        for (int id : cardIds) {
            View card = findViewById(id);
            if (card != null) {
                // Set the click listener for each card found
                card.setOnClickListener(v -> {
                    Intent intent = new Intent(HomeActivity.this, IslandDetailsActivity.class);
                    startActivity(intent);
                });
            }
        }
    }

    private void setupFilterChips() {
        TextView chipPopular = findViewById(R.id.chip_popular);
        TextView chipTrending = findViewById(R.id.chip_trending);
        TextView chipNew = findViewById(R.id.chip_new);
        TextView chipLabel1 = findViewById(R.id.chip_label1);
        TextView chipLabel2 = findViewById(R.id.chip_label2);

        filterChips = new TextView[]{chipPopular, chipTrending, chipNew, chipLabel1, chipLabel2};

        for (TextView chip : filterChips) {
            if (chip != null) {
                chip.setOnClickListener(v -> handleChipSelection((TextView) v));
            }
        }
    }

    private void handleChipSelection(TextView selectedChip) {
        for (TextView chip : filterChips) {
            chip.setBackgroundResource(R.drawable.chip_background);
            chip.setTextColor(Color.parseColor("#5F5F5F"));
        }

        selectedChip.setBackgroundResource(R.drawable.chip_background_selected);
        selectedChip.setTextColor(Color.WHITE);
    }
}