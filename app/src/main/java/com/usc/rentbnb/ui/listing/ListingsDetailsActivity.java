package com.usc.rentbnb.ui.listing;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.ui.booking.RentForm;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display detailed information about a rental listing.
 */
public class ListingsDetailsActivity extends AppCompatActivity {

    private TextView titleView, categoryView, priceView, descriptionText, ratingText, reviewCountText;
    private TextView ownerNameView, ownerResponseTimeView;
    private ImageView ownerAvatarView;
    private MaterialButton btnRentNow;
    private ChipGroup chipGroupActivities;
    private ViewPager2 imageViewPager;
    private TabLayout tabIndicator;
    private ImageView btnFavorite;
    private FloatingActionButton btnChat;
    private Toolbar toolbar;
    private Listing currentListing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings_details);

        initViews();
        setupToolbar();
        bindListingData();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        titleView = findViewById(R.id.listing_title);
        categoryView = findViewById(R.id.listing_category);
        priceView = findViewById(R.id.listing_price);
        descriptionText = findViewById(R.id.listing_description);
        ratingText = findViewById(R.id.rating_text);
        reviewCountText = findViewById(R.id.review_count_text);
        
        ownerNameView = findViewById(R.id.owner_name);
        ownerResponseTimeView = findViewById(R.id.owner_response_time);
        ownerAvatarView = findViewById(R.id.owner_avatar);

        btnRentNow = findViewById(R.id.btn_rent_now);
        chipGroupActivities = findViewById(R.id.chip_group_activities);
        imageViewPager = findViewById(R.id.image_viewpager);
        tabIndicator = findViewById(R.id.tab_indicator);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnChat = findViewById(R.id.btn_chat);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void bindListingData() {
        Intent intent = getIntent();
        currentListing = (Listing) intent.getSerializableExtra("listing_object");

        // If launched as MAIN (Launcher) or no data passed, use a sample listing for testing
        if (currentListing == null && Intent.ACTION_MAIN.equals(intent.getAction())) {
            List<String> mockImages = new ArrayList<>();
            mockImages.add("https://images.unsplash.com/photo-1544551763-46a013bb70d5");
            
            currentListing = new Listing(
                "test_id", "owner_123", "Sample Luxury Boat",
                "This is a high-quality listing fetched from the database model. It features premium amenities and a great view.",
                "Premium Boat", "Siargao", 15000.0, "Day", 4.9, 128,
                mockImages, true, true
            );
        }

        if (currentListing == null) {
            // Only finish if it wasn't the launcher and still has no data
            if (!Intent.ACTION_MAIN.equals(intent.getAction())) {
                Toast.makeText(this, "No listing data found", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }

        // Data Binding
        titleView.setText(currentListing.getProductName());
        String categoryIsland = currentListing.getCategory();
        if (currentListing.getIsland() != null) {
            categoryIsland += " • " + currentListing.getIsland();
        }
        categoryView.setText(categoryIsland);
        descriptionText.setText(currentListing.getDescription());
        ratingText.setText(String.format("%.1f", currentListing.getRating()));
        reviewCountText.setText("(" + currentListing.getTotalReviews() + " reviews)");

        String formattedPrice = "₱" + String.format("%,.0f", currentListing.getPrice());
        if (currentListing.getPriceUnit() != null) {
            formattedPrice += " / " + currentListing.getPriceUnit();
        }
        priceView.setText(formattedPrice);

        ownerNameView.setText("Hosted by " + (currentListing.getOwnerId() != null ? "Host " + currentListing.getOwnerId().substring(0, Math.min(5, currentListing.getOwnerId().length())) : "Verified Host"));
        
        setupActivities(currentListing.getCategory());
        setupCarousel(currentListing.getImageUrls());
    }

    private void setupCarousel(List<String> imageUrls) {
        List<Object> imagesToDisplay = new ArrayList<>();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            imagesToDisplay.addAll(imageUrls);
        } else {
            imagesToDisplay.add(R.drawable.ic_no_image_placeholder);
        }

        GalleryAdapter adapter = new GalleryAdapter(imagesToDisplay);
        imageViewPager.setAdapter(adapter);
        new TabLayoutMediator(tabIndicator, imageViewPager, (tab, position) -> {}).attach();
    }

    private void setupActivities(String category) {
        chipGroupActivities.removeAllViews();
        String[] suggestions = {"Sightseeing", "Photography", "Local Tour"};
        if (category != null && category.toLowerCase().contains("boat")) {
            suggestions = new String[]{"Island Hopping", "Snorkeling", "Sunset Cruise"};
        }
        
        for (String activity : suggestions) {
            Chip chip = new Chip(this);
            chip.setText(activity);
            chip.setChipBackgroundColorResource(R.color.white);
            chip.setChipStrokeColorResource(R.color.teal_primary);
            chip.setChipStrokeWidth(2f);
            chip.setTextColor(getResources().getColor(R.color.text_dark));
            chipGroupActivities.addView(chip);
        }
    }

    private void setupListeners() {
        btnRentNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, RentForm.class);
            intent.putExtra("listing_object", currentListing);
            startActivity(intent);
        });
        btnFavorite.setOnClickListener(v -> btnFavorite.setImageResource(R.drawable.ic_favorites_filled));
    }

    private static class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
        private final List<Object> imageSources;
        GalleryAdapter(List<Object> imageSources) { this.imageSources = imageSources; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_gallery, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(holder.imageView.getContext())
                .load(imageSources.get(position))
                .centerCrop()
                .placeholder(R.drawable.ic_no_image_placeholder)
                .into(holder.imageView);
        }

        @Override
        public int getItemCount() { return imageSources.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.gallery_image);
            }
        }
    }
}
