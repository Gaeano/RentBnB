package com.usc.rentbnb.ui.listing;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.usc.rentbnb.R;
import com.usc.rentbnb.callbacks.FavoriteListingsCallback;
import com.usc.rentbnb.models.ChatRoom;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.Review;
import com.usc.rentbnb.repositories.ChatRepository;
import com.usc.rentbnb.repositories.FavoritesRepository;
import com.usc.rentbnb.ui.booking.RentForm;
import com.usc.rentbnb.ui.chat.ChatRoomActivity;
import com.usc.rentbnb.ui.review.AllReviewsActivity;
import com.usc.rentbnb.viewmodels.ReviewViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ListingsDetailsActivity extends AppCompatActivity {

    private TextView titleView, priceView, descriptionText, ratingText, reviewCountText;
    private TextView ownerNameView, ownerTypeView, locationView, pricePerDayView;
    private ImageView ownerAvatarView;
    private ChipGroup paymentMethodsChipGroup;
    private ViewPager2 imageViewPager;
    private LinearLayout galleryIndicatorLayout;

    // Reviews views
    private CardView cardReviewPreview;
    private ShapeableImageView ivReviewerAvatar;
    private TextView tvReviewerName, tvReviewDate, tvReviewRating, tvReviewComment;
    private TextView tvNoReviews, tvSeeAllReviews, tvReviewsCountLabel;

    private Listing currentListing;
    private String currentUserId;
    private boolean isFavorited = false;

    private FavoritesRepository favoritesRepository;
    private ChatRepository chatRepository;
    private ReviewViewModel reviewViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings_details);

        currentListing = (Listing) getIntent().getParcelableExtra("listing_object");

        if (currentListing == null) {
            Toast.makeText(this, "Error loading listing details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);


        favoritesRepository = new FavoritesRepository();
        chatRepository = new ChatRepository();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        initViews();
        populateListingDetails();
        setupImageGallery();
        loadOwnerAvatar();
        loadReviewPreview();
        setupClickListeners();
        setUpObservers();

    }

    private void initViews() {
        titleView = findViewById(R.id.tv_listing_title);
        priceView = findViewById(R.id.listing_price);
        pricePerDayView = findViewById(R.id.tv_listing_price_per_day);
        descriptionText = findViewById(R.id.tv_listing_description);
        ratingText = findViewById(R.id.tv_listing_rating);
        reviewCountText = findViewById(R.id.tv_listing_reviews);

        ownerNameView = findViewById(R.id.tv_owner_name);
        ownerTypeView = findViewById(R.id.tv_owner_type);
        ownerAvatarView = findViewById(R.id.iv_owner_avatar);

        locationView = findViewById(R.id.tv_listing_location);
        paymentMethodsChipGroup = findViewById(R.id.chip_group_payments);
        imageViewPager = findViewById(R.id.image_viewpager);
        galleryIndicatorLayout = findViewById(R.id.gallery_indicator_layout);

        cardReviewPreview = findViewById(R.id.card_review_preview);
        ivReviewerAvatar = findViewById(R.id.iv_reviewer_avatar);
        tvReviewerName = findViewById(R.id.tv_reviewer_name);
        tvReviewDate = findViewById(R.id.tv_review_date);
        tvReviewRating = findViewById(R.id.tv_review_rating);
        tvReviewComment = findViewById(R.id.tv_review_comment);
        tvNoReviews = findViewById(R.id.tv_no_reviews);
        tvSeeAllReviews = findViewById(R.id.tv_see_all_reviews);
        tvReviewsCountLabel = findViewById(R.id.tv_reviews_count_label);
    }

    private void populateListingDetails() {
        if (titleView != null) titleView.setText(currentListing.getProductName() != null ? currentListing.getProductName() : "Unknown Item");

        double price = currentListing.getPrice();
        String unit = currentListing.getPriceUnit() != null ? currentListing.getPriceUnit() : "day";

        if (priceView != null) priceView.setText(String.format(Locale.getDefault(), "₱%,.0f", price));
        if (pricePerDayView != null) pricePerDayView.setText(String.format(Locale.getDefault(), "₱%,.0f / %s", price, unit));

        if (descriptionText != null) descriptionText.setText(currentListing.getDescription());

        if (ratingText != null) ratingText.setText(String.format(Locale.getDefault(), "%.1f", currentListing.getRating()));
        if (reviewCountText != null) reviewCountText.setText(String.format(Locale.getDefault(), "(%d reviews)", currentListing.getTotalReviews()));

        if (tvReviewsCountLabel != null) {
            int total = currentListing.getTotalReviews();
            tvReviewsCountLabel.setText(String.format(Locale.getDefault(), "%d %s", total, total == 1 ? "review" : "reviews"));
        }

        if (locationView != null) locationView.setText(currentListing.getIsland());

        Chip categoryChip = findViewById(R.id.chip_category);
        if (categoryChip != null) categoryChip.setText(currentListing.getCategory());

        Chip conditionChip = findViewById(R.id.chip_condition);
        if (conditionChip != null) conditionChip.setVisibility(View.GONE);

        if (paymentMethodsChipGroup != null) {
            paymentMethodsChipGroup.removeAllViews();
            List<String> payments = currentListing.getPaymentMethods();
            if (payments != null && !payments.isEmpty()) {
                for (String method : payments) {
                    Chip chip = new Chip(this);
                    chip.setText(method);
                    chip.setClickable(false);
                    chip.setCheckable(false);
                    paymentMethodsChipGroup.addView(chip);
                }
            } else {
                paymentMethodsChipGroup.setVisibility(View.GONE);
            }
        }

        if (ownerNameView != null) ownerNameView.setText(currentListing.getOwnerName() != null ? currentListing.getOwnerName() : "Unknown");

        if (ownerTypeView != null) ownerTypeView.setVisibility(View.GONE);
    }

    private void loadOwnerAvatar() {
        String ownerId = currentListing.getOwnerId();

        if (ownerAvatarView != null) {
            ownerAvatarView.setImageResource(R.drawable.userprofile);
        }

        if (ownerId != null && !ownerId.isEmpty()) {
            FirebaseFirestore.getInstance().collection("users").document(ownerId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && !isDestroyed()) {
                            String photoUrl = documentSnapshot.getString("photoUrl");
                            if (photoUrl != null && !photoUrl.isEmpty() && ownerAvatarView != null) {
                                Glide.with(ListingsDetailsActivity.this)
                                        .load(photoUrl)
                                        .placeholder(R.drawable.userprofile)
                                        .error(R.drawable.userprofile)
                                        .circleCrop()
                                        .into(ownerAvatarView);
                            }
                        }
                    });
        }
    }

    private void setupImageGallery() {
        if (imageViewPager == null) return;

        List<Object> imageSources = new ArrayList<>();
        List<String> modelImages = currentListing.getImageUrls();

        if (modelImages != null && !modelImages.isEmpty()) {
            imageSources.addAll(modelImages);
        } else {
            imageSources.add(R.drawable.ic_no_image_placeholder);
        }

        GalleryAdapter adapter = new GalleryAdapter(imageSources);
        imageViewPager.setAdapter(adapter);

        setupGalleryIndicators(imageSources.size());

        imageViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateGalleryIndicators(position);
            }
        });
    }

    private void setupGalleryIndicators(int count) {
        if (galleryIndicatorLayout == null) return;

        galleryIndicatorLayout.removeAllViews();

        // Only show indicators when there is more than one image
        if (count <= 1) {
            galleryIndicatorLayout.setVisibility(View.GONE);
            return;
        }

        galleryIndicatorLayout.setVisibility(View.VISIBLE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_inactive));
            dot.setLayoutParams(params);
            galleryIndicatorLayout.addView(dot);
        }

        updateGalleryIndicators(0);
    }

    private void updateGalleryIndicators(int activePosition) {
        if (galleryIndicatorLayout == null) return;

        int childCount = galleryIndicatorLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView dot = (ImageView) galleryIndicatorLayout.getChildAt(i);
            if (i == activePosition) {
                dot.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_active));
            } else {
                dot.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_inactive));
            }
        }
    }

    private void loadReviewPreview() {
        String listingId = currentListing.getId();
        if (listingId == null || listingId.isEmpty()) {
            showNoReviews();
            return;
        }

        reviewViewModel.getReviews(listingId);
    }

    private void bindReviewPreview(Review review) {
        if (review == null) {
            showNoReviews();
            return;
        }

        if (cardReviewPreview != null) cardReviewPreview.setVisibility(View.VISIBLE);
        if (tvNoReviews != null) tvNoReviews.setVisibility(View.GONE);
        if (tvSeeAllReviews != null) tvSeeAllReviews.setVisibility(View.VISIBLE);

        if (tvReviewerName != null) {
            tvReviewerName.setText(review.getReviewerName() != null ? review.getReviewerName() : "Renter");
        }

        if (tvReviewRating != null) {
            tvReviewRating.setText(String.format(Locale.getDefault(), "%.1f", review.getRating()));
        }

        if (tvReviewComment != null) {
            String comment = review.getComment();
            tvReviewComment.setText(comment != null ? comment : "");
        }

        if (tvReviewDate != null) {
            if (review.getCreatedAt() != null) {
                String date = formatDate(review.getCreatedAt());
                tvReviewDate.setText(date);
            } else {
                tvReviewDate.setText("");
            }
        }

        if (ivReviewerAvatar != null) {
            String photoUrl = review.getReviewerPhotoUrl();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.userprofile)
                        .error(R.drawable.userprofile)
                        .circleCrop()
                        .into(ivReviewerAvatar);
            } else {
                ivReviewerAvatar.setImageResource(R.drawable.userprofile);
            }
        }
    }

    private void showNoReviews() {
        if (cardReviewPreview != null) cardReviewPreview.setVisibility(View.GONE);
        if (tvSeeAllReviews != null) tvSeeAllReviews.setVisibility(View.GONE);
        if (tvNoReviews != null) tvNoReviews.setVisibility(View.VISIBLE);
    }

    private void updateFavoriteIcon() {
        ImageButton btnFavorite = findViewById(R.id.btn_favorite);
        if (btnFavorite != null) {
            btnFavorite.setImageResource(isFavorited ? R.drawable.ic_favorites_filled : R.drawable.ic_favorites);
        }
    }

    private void setupClickListeners() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        MaterialButton btnRentNow = findViewById(R.id.btn_rent_now);
        if (btnRentNow != null) {
            btnRentNow.setOnClickListener(v -> {
                Intent intent = new Intent(this, RentForm.class);
                intent.putExtra("listing_object", currentListing);
                startActivity(intent);
            });
        }

        ImageButton btnFavorite = findViewById(R.id.btn_favorite);
        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                if (currentUserId == null) return;

                if (isFavorited) {
                    favoritesRepository.removeFavoriteListing(currentUserId, currentListing.getId(), new FavoriteListingsCallback() {
                        @Override
                        public void onSuccess(List<Listing> favorites) {
                            isFavorited = false;
                            updateFavoriteIcon();
                        }

                        @Override
                        public void onError(String error) {}
                    });
                } else {
                    favoritesRepository.addFavoriteListing(currentUserId, currentListing, new FavoriteListingsCallback() {
                        @Override
                        public void onSuccess(List<Listing> favorites) {
                            isFavorited = true;
                            updateFavoriteIcon();
                        }

                        @Override
                        public void onError(String error) {}
                    });
                }
            });
        }

        if (tvSeeAllReviews != null) {
            tvSeeAllReviews.setOnClickListener(v -> {
                Intent intent = new Intent(this, AllReviewsActivity.class);
                intent.putExtra(AllReviewsActivity.EXTRA_LISTING_ID, currentListing.getId());
                intent.putExtra(AllReviewsActivity.EXTRA_LISTING_TITLE, currentListing.getProductName());
                startActivity(intent);
            });
        }

        ImageButton btnOwnerChat = findViewById(R.id.btn_owner_chat);
        if (btnOwnerChat != null) {
            btnOwnerChat.setOnClickListener(v -> {
                if (currentUserId == null) {
                    Toast.makeText(this, "Please log in to chat.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String ownerId = currentListing.getOwnerId();
                if (ownerId == null || ownerId.equals(currentUserId)) {
                    Toast.makeText(this, "Cannot message this host.", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnOwnerChat.setEnabled(false);
                Toast.makeText(this, "Loading chat...", Toast.LENGTH_SHORT).show();

                chatRepository.findExistingChatRoom(currentUserId, ownerId, currentListing.getId(), new ChatRepository.ExistingRoomCallback() {
                    @Override
                    public void onResult(ChatRoom chatRoom) {
                        if (chatRoom != null) {
                            btnOwnerChat.setEnabled(true);
                            openChatRoom(chatRoom.getId());
                        } else {
                            String imageUrl = (currentListing.getImageUrls() != null && !currentListing.getImageUrls().isEmpty())
                                    ? currentListing.getImageUrls().get(0) : "";

                            chatRepository.createChatRoom(currentUserId, ownerId, currentListing.getId(),
                                    currentListing.getProductName(), imageUrl, new ChatRepository.ChatRoomCallback() {
                                        @Override
                                        public void onSuccess(ChatRoom newRoom) {
                                            btnOwnerChat.setEnabled(true);
                                            openChatRoom(newRoom.getId());
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            btnOwnerChat.setEnabled(true);
                                            Toast.makeText(ListingsDetailsActivity.this, "Failed to start chat.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
            });
        }
    }

    private void openChatRoom(String chatRoomId) {
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("chatRoomId", chatRoomId);
        startActivity(intent);
    }

    // --- Inner ViewPager Adapter ---
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

    private void setUpObservers(){
        reviewViewModel.getReviewData().observe(this, reviews -> {
            if (reviews != null && !reviews.isEmpty()){
                bindReviewPreview(reviews.get(0));
                Toast.makeText(this, "Reviews successfully fetched", Toast.LENGTH_LONG).show();
            } else {
                showNoReviews();
            }
        });

        reviewViewModel.getErrorData().observe(this, errorMssg -> {
            if (errorMssg != null && !errorMssg.isEmpty()){
                Toast.makeText(this, errorMssg, Toast.LENGTH_LONG).show();
            }
        });


    }

    private String formatDate(String isoDate) {
        if (isoDate == null) return "N/A";
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            input.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat output = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return output.format(input.parse(isoDate));
        } catch (Exception e) {
            return isoDate;
        }
    }
}