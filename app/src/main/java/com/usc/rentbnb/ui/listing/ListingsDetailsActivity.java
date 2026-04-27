package com.usc.rentbnb.ui.listing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.viewmodels.FavoriteViewModel;

import java.util.ArrayList;
import java.util.List;

public class ListingsDetailsActivity extends AppCompatActivity {

    private TextView titleView, categoryView, priceView;
    private FrameLayout btnBack;
    private TextView btnRentNow;
    private ListView activitiesListView;

    private ImageView btnFavorite, btnChat;
    private TextView descriptionText, btnShowAllReviews;

    // Architecture Variables
    private FavoriteViewModel favoriteViewModel;
    private boolean isFavorite = false;
    private String currentListingId;
    private Listing currentListing;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings_details);

        // 1. Initialize Views
        titleView = findViewById(R.id.listing_title);
        categoryView = findViewById(R.id.listing_category);
        priceView = findViewById(R.id.listing_price);
        btnBack = findViewById(R.id.btn_back_wrapper);
        btnRentNow = findViewById(R.id.btn_rent_now);
        activitiesListView = findViewById(R.id.list_suggested_activities);

        btnFavorite = findViewById(R.id.btn_favorite);
        btnChat = findViewById(R.id.btn_chat);
        descriptionText = findViewById(R.id.listing_description);
        btnShowAllReviews = findViewById(R.id.btn_show_all_reviews);

        // Handle Status Bar Padding
        ViewCompat.setOnApplyWindowInsetsListener(btnBack, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = statusBarHeight + 16;
            v.setLayoutParams(params);
            return insets;
        });

        // 2. Retrieve Data & Reconstruct Object
        Intent intent = getIntent();
        if (intent != null) {
            currentListing = intent.getParcelableExtra("listing_object");

            if (currentListing != null) {
                currentListingId = currentListing.getId();

                // Update your UI directly from the object
                titleView.setText(currentListing.getProductName());
                categoryView.setText(currentListing.getCategory());
                priceView.setText("₱" + currentListing.getPrice() + " / " + currentListing.getPriceUnit());

            }
        }

        // 3. Setup Architecture
        setupFavoritesObserver();
        setupActivitiesList();

        // 4. Setup Click Listeners
        btnBack.setOnClickListener(v -> finish());

        btnRentNow.setOnClickListener(v -> {
            Toast.makeText(ListingsDetailsActivity.this, "Proceeding to checkout...", Toast.LENGTH_SHORT).show();
        });

        // The Animated Favorite Button Logic
        btnFavorite.setOnClickListener(v -> {
            // Validate user is logged in
            if (userId == null) {
                Toast.makeText(this, "Please log in to save favorites.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Shrink animation
            btnFavorite.animate()
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        // Toggle state locally for immediate visual feedback
                        isFavorite = !isFavorite;
                        updateHeartUI();

                        // 2. Bounce back animation
                        btnFavorite.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(200)
                                .setInterpolator(new OvershootInterpolator())
                                .start();

                        // 3. Database operation
                        if (isFavorite) {
                            favoriteViewModel.addFavoriteListing(userId, currentListing);
                        } else {
                            favoriteViewModel.deleteFavoriteListing(userId, currentListingId);
                        }
                    })
                    .start();
        });

        btnChat.setOnClickListener(v -> {
            Toast.makeText(ListingsDetailsActivity.this, "Opening chat with owner...", Toast.LENGTH_SHORT).show();
        });

        descriptionText.setOnClickListener(v -> {
            Toast.makeText(ListingsDetailsActivity.this, "Expanding description...", Toast.LENGTH_SHORT).show();
        });

        btnShowAllReviews.setOnClickListener(v -> {
            Toast.makeText(ListingsDetailsActivity.this, "Opening all reviews...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupFavoritesObserver() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return; // Exit early if user is not logged in
        }

        favoriteViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(FavoriteViewModel.class);

        // Listen to the database
        favoriteViewModel.getFavoriteListings().observe(this, favorites -> {
            if (favorites != null) {
                isFavorite = false; // reset before checking
                for (Listing fav : favorites) {
                    if (fav.getId() != null && fav.getId().equals(currentListingId)) {
                        isFavorite = true;
                        break;
                    }
                }
                updateHeartUI(); // Paint the UI based on the truth
            }
        });

        // Trigger the fetch
        favoriteViewModel.loadListings(userId);
    }

    private void updateHeartUI() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorites_filled);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorites);
        }
    }

    private void setupActivitiesList() {
        List<String> activities = new ArrayList<>();
        activities.add("· Island Hopping");
        activities.add("· Snorkeling");
        activities.add("· Sunset Watching");
        activities.add("· Deep Sea Fishing");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_activity_list,
                R.id.activity_text,
                activities
        );

        activitiesListView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(activitiesListView);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}