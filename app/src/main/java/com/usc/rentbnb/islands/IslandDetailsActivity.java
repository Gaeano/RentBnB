package com.usc.rentbnb.islands;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.home.HomeActivity;

public class IslandDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_island_details);

        String island_name = getIntent().getStringExtra("island_name");
        String location = getIntent().getStringExtra("location");
        double rating = getIntent().getDoubleExtra("rating", 0.0);
        String category = getIntent().getStringExtra("category"); // TODO: Add a chip next to island name showing the category of the island
        String description = getIntent().getStringExtra("description");

        TextView islandName = findViewById(R.id.island_title);
        TextView islandLocation = findViewById(R.id.island_location);
        TextView islandRating = findViewById(R.id.island_rating);
        TextView islandDescription = findViewById(R.id.island_description);

        islandName.setText(island_name);
        islandRating.setText(String.valueOf(rating));
        islandDescription.setText(description);

        // 1. Handle the Top-Left Back Button
        // Using finish() safely pops this activity off the stack and returns to whatever was before it (HomeActivity)
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

        // 2. Handle the Bottom Nav Home Button
        View bottomNav = findViewById(R.id.bottomNavContainer);
        ImageView navHome = bottomNav.findViewById(R.id.navHome);
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(IslandDetailsActivity.this, HomeActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}