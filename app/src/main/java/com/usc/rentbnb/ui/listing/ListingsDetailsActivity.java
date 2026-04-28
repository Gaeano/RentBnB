package com.usc.rentbnb.ui.listing;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.viewmodels.FavoriteViewModel;

import java.util.List;

public class ListingsDetailsActivity extends AppCompatActivity {

    private TextView titleView, categoryView, priceView, activitiesView;
    private FrameLayout btnBack;
    private TextView btnRentNow;
    private ImageView btnFavorite, btnChat;
    private TextView descriptionText, btnShowAllReviews;

    // Architecture Variables
    private FavoriteViewModel favoriteViewModel;
    private boolean isFavorite = false;
    private String currentListingId;
    private Listing currentListing;
    private String userId;
    private boolean isActivitiesExpanded = false;

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
        activitiesView = findViewById(R.id.text_suggested_activities);

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

        // 2. Retrieve Data via Parcelable
        Intent intent = getIntent();
        if (intent != null) {
            currentListing = intent.getParcelableExtra("listing_object");

            if (currentListing != null) {
                currentListingId = currentListing.getId();

                // Update UI from object
                titleView.setText(currentListing.getProductName());
                categoryView.setText(currentListing.getCategory());
                priceView.setText("₱" + currentListing.getPrice() + " / " + currentListing.getPriceUnit());
                descriptionText.setText(currentListing.getDescription());

                // Populate activities dynamically
                setupActivitiesList();
            }
        }

        // 3. Setup Architecture
        setupFavoritesObserver();

        // 4. Setup Click Listeners
        btnBack.setOnClickListener(v -> finish());

        btnRentNow.setOnClickListener(v -> {
            Toast.makeText(this, "Proceeding to checkout...", Toast.LENGTH_SHORT).show();
        });

        btnChat.setOnClickListener(v -> {
            Toast.makeText(this, "Opening chat with owner...", Toast.LENGTH_SHORT).show();
        });

        // Animated Favorite Button
        btnFavorite.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(this, "Please log in to save favorites.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnFavorite.animate()
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        isFavorite = !isFavorite;
                        updateHeartUI();

                        btnFavorite.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(200)
                                .setInterpolator(new OvershootInterpolator())
                                .start();

                        if (isFavorite) {
                            favoriteViewModel.addFavoriteListing(userId, currentListing);
                        } else {
                            favoriteViewModel.deleteFavoriteListing(userId, currentListingId);
                        }
                    })
                    .start();
        });
    }

    private void setupActivitiesList() {
        List<String> activities = currentListing.getSuggestedActivities();

        if (activities == null || activities.isEmpty()) {
            activitiesView.setText("No specific activities suggested.");
            return;
        }

        if (activities.size() <= 3) {
            activitiesView.setText(String.join(", ", activities));
            return;
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (!isActivitiesExpanded) {
            String shortText = String.join(", ", activities.subList(0, 3));
            builder.append(shortText).append("... ");
            int start = builder.length();
            builder.append("See More");
            addClickableSpan(builder, start, true);
        } else {
            String fullText = String.join(", ", activities);
            builder.append(fullText).append(" ");
            int start = builder.length();
            builder.append("See Less");
            addClickableSpan(builder, start, false);
        }

        activitiesView.setText(builder);
        activitiesView.setMovementMethod(LinkMovementMethod.getInstance());
        activitiesView.setHighlightColor(Color.TRANSPARENT);
    }

    private void addClickableSpan(SpannableStringBuilder builder, int start, boolean expand) {
        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                isActivitiesExpanded = expand;
                setupActivitiesList();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(ContextCompat.getColor(ListingsDetailsActivity.this, R.color.teal_primary));
                ds.setFakeBoldText(true);
            }
        }, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void setupFavoritesObserver() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return;
        }

        favoriteViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(FavoriteViewModel.class);
        favoriteViewModel.getFavoriteListings().observe(this, favorites -> {
            if (favorites != null) {
                isFavorite = false;
                for (Listing fav : favorites) {
                    if (fav.getId() != null && fav.getId().equals(currentListingId)) {
                        isFavorite = true;
                        break;
                    }
                }
                updateHeartUI();
            }
        });
        favoriteViewModel.loadListings(userId);
    }

    private void updateHeartUI() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorites_filled);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorites);
        }
    }
}