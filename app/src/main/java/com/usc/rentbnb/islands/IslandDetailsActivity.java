package com.usc.rentbnb.islands;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faltenreich.skeletonlayout.Skeleton;
import com.faltenreich.skeletonlayout.SkeletonLayoutUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ListingAdapter;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.viewmodels.FavoriteViewModel;

import java.util.ArrayList;
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

    private FrameLayout btnBackWrapper;
    private FavoriteViewModel favoriteViewModel;
    private ListingAdapter adapter;
    private List<Listing> currentListings = new ArrayList<>();
    private RecyclerView rv;
    private Skeleton skeleton;
    private LinearLayout emptyStateLayout;
    private TextView islandName, islandLocation, islandRating, islandDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_island_details);

        island_name = getIntent().getStringExtra("island_name");
        location = getIntent().getStringExtra("location");
        rating = getIntent().getDoubleExtra("rating", 0.0);
        category = getIntent().getStringExtra("category");
        description = getIntent().getStringExtra("description");

        islandName = findViewById(R.id.island_title);
        islandLocation = findViewById(R.id.island_location);
        islandRating = findViewById(R.id.island_rating);
        islandDescription = findViewById(R.id.island_description);
        emptyStateLayout = findViewById(R.id.empty_state_layout);

        islandName.setText(island_name);
        islandRating.setText(String.valueOf(rating));
        islandDescription.setText(description);

        btnBackWrapper = findViewById(R.id.btn_back_wrapper);
        ViewCompat.setOnApplyWindowInsetsListener(btnBackWrapper, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = statusBarHeight + 16;
            v.setLayoutParams(params);
            return insets;
        });

        btnBackWrapper.setOnClickListener(v -> finish());


        setupRecyclerView();
        skeleton = SkeletonLayoutUtils.applySkeleton(rv, R.layout.rentable_item_card, 3);
        skeleton.setMaskColor(ContextCompat.getColor(this, R.color.text_grey));
        skeleton.setMaskCornerRadius(16);
        skeleton.showSkeleton();

        setupFavoritesObserver();
        fetchListings();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        favoriteViewModel.loadListings(userId);
    }

    private void setupRecyclerView() {
        rv = findViewById(R.id.rvIslandListings);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new ListingAdapter((listing, isCurrentlyFavorite) -> {
            if (isCurrentlyFavorite) {
                favoriteViewModel.deleteFavoriteListing(userId, listing.getId());
            } else {
                favoriteViewModel.addFavoriteListing(userId, listing);
            }
        });

        rv.setAdapter(adapter);
    }

    private void setupFavoritesObserver() {
        favoriteViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(FavoriteViewModel.class);

        favoriteViewModel.getFavoriteListings().observe(this, favoriteListings -> {
            if (favoriteListings != null) {
                List<String> favoriteIds = new ArrayList<>();
                for (Listing favorite : favoriteListings) {
                    favoriteIds.add(favorite.getId());
                }
                // Update adapter with existing listings and new favorite states
                adapter.submitData(currentListings, favoriteIds);
            }
        });
    }

    private void fetchListings() {
        ApiClient.getApiService().getListings(island_name).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                    // Save the fetched listings globally
                    currentListings = response.body().getData();

                    if (currentListings.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    } else {
                        emptyStateLayout.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                    }

                    List<String> favoriteIds = new ArrayList<>();
                    if (favoriteViewModel.getFavoriteListings().getValue() != null) {
                        for (Listing fav : favoriteViewModel.getFavoriteListings().getValue()) {
                            favoriteIds.add(fav.getId());
                        }
                    }

                    adapter.submitData(currentListings, favoriteIds);
                    skeleton.showOriginal();

                } else {
                    Toast.makeText(IslandDetailsActivity.this, "Failed to load listings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                Toast.makeText(IslandDetailsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}