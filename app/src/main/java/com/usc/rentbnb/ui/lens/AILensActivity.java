package com.usc.rentbnb.ui.lens;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ListingAdapter;
import com.usc.rentbnb.models.LensRequest;
import com.usc.rentbnb.models.LensResponse;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.network.ApiClient;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AILensActivity extends AppCompatActivity {

    private ImageView ivCapturedImage;
    private LinearLayout layoutIdleState, layoutLoadingOverlay, layoutNoResults;
    private ChipGroup chipGroupKeywords;
    private RecyclerView rvLensResults;
    private FloatingActionButton btnRetake;
    private TextView tvTagsTitle, tvResultsTitle, tvNoResults;

    private Bitmap capturedBitmap;

    // -------------------------------------------------------------------------
    // Camera launcher — TakePicturePreview returns a Bitmap directly.
    // -------------------------------------------------------------------------
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicturePreview(),
                    bitmap -> {
                        if (bitmap != null) {
                            capturedBitmap = bitmap;
                            showCapturedState(bitmap);
                            analyseAndSearch();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_lens);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.aiLensRoot), (v, insets) -> {
                    androidx.core.graphics.Insets bars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        initViews();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Both the idle state placeholder and the retake button launch the camera
        layoutIdleState.setOnClickListener(v -> cameraLauncher.launch(null));
        btnRetake.setOnClickListener(v -> cameraLauncher.launch(null));
    }

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private void initViews() {
        ivCapturedImage      = findViewById(R.id.ivCapturedImage);
        layoutIdleState      = findViewById(R.id.layoutIdleState);
        layoutLoadingOverlay = findViewById(R.id.layoutLoadingOverlay);
        layoutNoResults      = findViewById(R.id.layoutNoResults);

        chipGroupKeywords    = findViewById(R.id.chipGroupKeywords);
        rvLensResults        = findViewById(R.id.rvLensResults);
        btnRetake            = findViewById(R.id.btnRetake);

        tvTagsTitle          = findViewById(R.id.tvTagsTitle);
        tvResultsTitle       = findViewById(R.id.tvResultsTitle);

        rvLensResults.setLayoutManager(new GridLayoutManager(this, 2));
    }

    // -------------------------------------------------------------------------
    // Step 1 — show captured image
    // -------------------------------------------------------------------------

    private void showCapturedState(Bitmap bitmap) {
        // Show Image & Retake Button
        layoutIdleState.setVisibility(View.GONE);
        ivCapturedImage.setVisibility(View.VISIBLE);
        ivCapturedImage.setImageBitmap(bitmap);
        btnRetake.setVisibility(View.VISIBLE);

        // Hide old results perfectly
        tvTagsTitle.setVisibility(View.GONE);
        chipGroupKeywords.removeAllViews();
        tvResultsTitle.setVisibility(View.GONE);
        rvLensResults.setVisibility(View.GONE);
        layoutNoResults.setVisibility(View.GONE);
    }

    // -------------------------------------------------------------------------
    // Step 2 — single API call: analyse image + search listings on the backend.
    // -------------------------------------------------------------------------

    private void analyseAndSearch() {
        if (capturedBitmap == null) return;

        setLoadingState(true);

        // Compress bitmap to JPEG at 70% quality and base64-encode for transport
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

        ApiClient.getApiService()
                .analyseImageWithLens(new LensRequest(base64Image))
                .enqueue(new Callback<LensResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LensResponse> call,
                                           @NonNull Response<LensResponse> response) {
                        setLoadingState(false);

                        if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                            showNoResults("Could not identify the object. Try a clearer photo.");
                            return;
                        }

                        LensResponse body = response.body();

                        displayKeywordChips(body.getKeywords(), body.getDetectedCategory());

                        List<Listing> listings = body.getData();
                        if (listings == null || listings.isEmpty()) {
                            showNoResults("No similar rentals found for this object.");
                        } else {
                            displayListings(listings);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LensResponse> call,
                                          @NonNull Throwable t) {
                        setLoadingState(false);
                        showNoResults("Network error. Please try again.");
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Display helpers
    // -------------------------------------------------------------------------

    private void displayKeywordChips(List<String> keywords, String category) {
        chipGroupKeywords.removeAllViews();
        boolean hasChips = false;

        // Category chip — teal, shown first
        if (category != null && !category.isEmpty()) {
            Chip catChip = new Chip(this);
            catChip.setText(category);
            catChip.setClickable(false);
            catChip.setChipBackgroundColorResource(R.color.teal_dark);
            catChip.setTextColor(getColor(android.R.color.white));
            chipGroupKeywords.addView(catChip);
            hasChips = true;
        }

        // Keyword chips — lighter teal
        if (keywords != null) {
            for (String keyword : keywords) {
                Chip chip = new Chip(this);
                chip.setText(keyword);
                chip.setClickable(false);
                chip.setChipBackgroundColorResource(R.color.teal_primary);
                chip.setTextColor(getColor(android.R.color.white));
                chipGroupKeywords.addView(chip);
                hasChips = true;
            }
        }

        tvTagsTitle.setVisibility(hasChips ? View.VISIBLE : View.GONE);
    }

    private void displayListings(List<Listing> listings) {
        ListingAdapter adapter = new ListingAdapter(
                (listing, isFav) -> Toast.makeText(
                        this, listing.getProductName(), Toast.LENGTH_SHORT).show());
        adapter.submitData(listings, new ArrayList<>());

        rvLensResults.setAdapter(adapter);
        tvResultsTitle.setVisibility(View.VISIBLE);
        rvLensResults.setVisibility(View.VISIBLE);
        layoutNoResults.setVisibility(View.GONE);
    }

    private void setLoadingState(boolean loading) {
        // Toggles the translucent overlay over the image card
        layoutLoadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRetake.setEnabled(!loading);
    }

    private void showNoResults(String message) {
        if (tvNoResults != null) tvNoResults.setText(message);

        layoutNoResults.setVisibility(View.VISIBLE);
        tvResultsTitle.setVisibility(View.GONE);
        rvLensResults.setVisibility(View.GONE);
    }
}