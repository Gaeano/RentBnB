package com.usc.rentbnb.islands;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ListingAdapter;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.ui.home.HomeActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IslandDetailsActivity extends AppCompatActivity {
    private String island_name;
    private String location;
    private double rating;
    private String category;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_island_details);

         island_name = getIntent().getStringExtra("island_name");
         location = getIntent().getStringExtra("location");
         rating = getIntent().getDoubleExtra("rating", 0.0);
         category = getIntent().getStringExtra("category"); // TODO: Add a chip next to island name showing the category of the island
         description = getIntent().getStringExtra("description");

        fetchListings();

        TextView islandName = findViewById(R.id.island_title);
        TextView islandLocation = findViewById(R.id.island_location);
        TextView islandRating = findViewById(R.id.island_rating);
        TextView islandDescription = findViewById(R.id.island_description);

        islandName.setText(island_name);
        islandRating.setText(String.valueOf(rating));
        islandDescription.setText(description);

        // 1. Handle the Top-Left Back Button
        // Using finish() safely pops this activity off the stack and returns to whatever was before it (HomeActivity)
        //TODO: Fix back button (inconsistent, cant be clicked sometimes)
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

    private void fetchListings() {
        ApiClient.getApiService().getListings(island_name).enqueue(new Callback<ListingResponse>() {

                    @Override
                    public void onResponse(Call<ListingResponse> call,
                                           Response<ListingResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {

                            List<Listing> listings = response.body().getData();

                            displayListings(listings);

                        } else {
                            Toast.makeText(IslandDetailsActivity.this,
                                    "Failed to load listings", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ListingResponse> call, Throwable t) {
                        Toast.makeText(IslandDetailsActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayListings(List<Listing> listings) {
        RecyclerView recyclerView = findViewById(R.id.rvIslandListings);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        ListingAdapter adapter = new ListingAdapter(listings);
        recyclerView.setAdapter(adapter);
    }
}