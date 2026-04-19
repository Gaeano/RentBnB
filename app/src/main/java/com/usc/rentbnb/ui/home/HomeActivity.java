package com.usc.rentbnb.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.ui.auth.LoginActivity;
import com.usc.rentbnb.ui.listing.AddListingActivity;
import com.usc.rentbnb.islands.IslandDetailsActivity;
import com.usc.rentbnb.viewmodels.HomeViewModel;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView logout;
    private FirebaseAuth auth;
    private TextView[] filterChips;
    private HomeViewModel homeViewModel;

    // TODO: Replace with real island data (recycler view)
    private final int[] cardIds = {
            R.id.island_card_1, R.id.island_card_2,
            R.id.island_card_3, R.id.island_card_4,
            R.id.island_card_5, R.id.island_card_6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        setupFilterChips();
        setupBottomNavigation();

        homeViewModel.getIslands().observe(this, islands -> {
            populateIslandCards(islands);
        });

        homeViewModel.fetchIslands();

//        logout = findViewById(R.id.logout_btn);
        auth = FirebaseAuth.getInstance();

        // --- 4. Setup Logout ---
        if (logout != null) {
            logout.setOnClickListener(v -> {
                auth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // TODO: Implement search bar logic
    }

    private void populateIslandCards(List<Island> islands) {
        //TODO: Replace with real images from Firebase storage char
        for (int i = 0; i < cardIds.length; i++) {
            if (i >= islands.size()) break;

            Island island = islands.get(i);
            View card = findViewById(cardIds[i]);

            if (card == null) continue;

            TextView nameView = card.findViewById(R.id.island_name);
            TextView locationView = card.findViewById(R.id.island_location);
            TextView ratingView = card.findViewById(R.id.island_rating);

            if (nameView != null) nameView.setText(island.getIslandName());
            if (locationView != null) locationView.setText(island.getLocation());
            if (ratingView != null) ratingView.setText(String.valueOf(island.getRating()));

            card.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, IslandDetailsActivity.class);
                intent.putExtra("island_name", island.getIslandName());
                intent.putExtra("location", island.getLocation());
                intent.putExtra("rating", island.getRating());
                intent.putExtra("category", island.getCategory());
                intent.putExtra("description", island.getDescription());
                startActivity(intent);
            });
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

    // TODO: Setup flexible bottom navbar logic (can be accessed from any page w/o having to manually add the logic for each activity)
    private void setupBottomNavigation() {
        View addListingFab = findViewById(R.id.navFab);
        if (addListingFab != null) {
            addListingFab.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, AddListingActivity.class);
                startActivity(intent);
            });
        }
    }
}