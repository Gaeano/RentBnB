package com.usc.rentbnb.ui.listing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

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

        // 1. Handle the Top-Left Back Button
        // Using finish() safely pops this activity off the stack and returns to whatever was before it (HomeActivity)
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

        // 2. Handle the Bottom Nav Home Button
        ImageView navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(IslandDetailsActivity.this, HomeActivity.class);
            // These flags are important! They prevent the app from creating endless duplicate copies of HomeActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}