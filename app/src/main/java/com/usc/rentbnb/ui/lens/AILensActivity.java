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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ListingAdapter;
import com.usc.rentbnb.models.LensRequest;
import com.usc.rentbnb.models.LensResponse;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.network.ApiClient;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AILensActivity extends AppCompatActivity {

    private ImageView ivCapturedImage;
    private LinearLayout layoutIdleState, layoutStatus, layoutNoResults;
    private ChipGroup chipGroupKeywords;
    private RecyclerView rvLensResults;
    private MaterialButton btnCapture;
    private TextView tvStatus;

    private Bitmap capturedBitmap;
    private List<Listing> allListings = new ArrayList<>();

    // ---------------------------------------------------------------------------
    // Camera launcher — TakePicturePreview returns a thumbnail Bitmap directly,
    // no URI or FileProvider setup required.
    // ---------------------------------------------------------------------------
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicturePreview(),
                    bitmap -> {
                        if (bitmap != null) {
                            capturedBitmap = bitmap;
                            showCapturedImage(bitmap);
                            fetchAllListingsThenAnalyse();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_lens);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.aiLensRoot), (v, insets) -> {
            androidx.core.graphics.Insets bars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        initViews();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnCapture.setOnClickListener(v -> cameraLauncher.launch(null));
    }

    // ---------------------------------------------------------------------------
    // View setup
    // ---------------------------------------------------------------------------

    private void initViews() {
        ivCapturedImage   = findViewById(R.id.ivCapturedImage);
        layoutIdleState   = findViewById(R.id.layoutIdleState);
        layoutStatus      = findViewById(R.id.layoutStatus);
        layoutNoResults   = findViewById(R.id.layoutNoResults);
        chipGroupKeywords = findViewById(R.id.chipGroupKeywords);
        rvLensResults     = findViewById(R.id.rvLensResults);
        btnCapture        = findViewById(R.id.btnCapture);
        tvStatus          = findViewById(R.id.tvStatus);

        rvLensResults.setLayoutManager(new GridLayoutManager(this, 2));
    }

    // ---------------------------------------------------------------------------
    // Step 1 — show captured image and update button label
    // ---------------------------------------------------------------------------

    private void showCapturedImage(Bitmap bitmap) {
        layoutIdleState.setVisibility(View.GONE);
        ivCapturedImage.setVisibility(View.VISIBLE);
        ivCapturedImage.setImageBitmap(bitmap);
        btnCapture.setText("Retake Photo");
        btnCapture.setIconResource(R.drawable.ic_lens);

        // Reset result views for fresh analysis
        chipGroupKeywords.removeAllViews();
        chipGroupKeywords.setVisibility(View.GONE);
        rvLensResults.setVisibility(View.GONE);
        layoutNoResults.setVisibility(View.GONE);
    }

    // ---------------------------------------------------------------------------
    // Step 2 — fetch all listings first so we can filter client-side after
    // the AI returns keywords. This avoids a second network round trip.
    // ---------------------------------------------------------------------------

    private void fetchAllListingsThenAnalyse() {
        setLoadingState(true, "Preparing listings...");

        ApiClient.getApiService().getListings(null).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(@NonNull Call<ListingResponse> call,
                                   @NonNull Response<ListingResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    allListings = response.body().getData();
                }
                // Proceed to AI analysis regardless of whether listings loaded
                analyseImage();
            }

            @Override
            public void onFailure(@NonNull Call<ListingResponse> call, @NonNull Throwable t) {
                analyseImage();
            }
        });
    }

    // ---------------------------------------------------------------------------
    // Step 3 — send image to backend AI endpoint
    // ---------------------------------------------------------------------------

    private void analyseImage() {
        if (capturedBitmap == null) return;
        setLoadingState(true, "AI is analysing your image...");

        // Compress bitmap to JPEG and base64-encode for transport
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

        LensRequest request = new LensRequest(base64Image);

        ApiClient.getApiService().analyseImageWithLens(request)
                .enqueue(new Callback<LensResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LensResponse> call,
                                           @NonNull Response<LensResponse> response) {
                        setLoadingState(false, "");

                        if (response.isSuccessful() && response.body() != null
                                && response.body().getKeywords() != null
                                && !response.body().getKeywords().isEmpty()) {
                            displayResults(response.body().getKeywords());
                        } else {
                            showNoResults("Could not identify the object. Try a clearer photo.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LensResponse> call, @NonNull Throwable t) {
                        setLoadingState(false, "");
                        showNoResults("Network error. Please try again.");
                    }
                });
    }

    // ---------------------------------------------------------------------------
    // Step 4 — display keyword chips and filter listings
    // ---------------------------------------------------------------------------

    private void displayResults(List<String> keywords) {
        // Show keyword chips
        chipGroupKeywords.removeAllViews();
        for (String keyword : keywords) {
            Chip chip = new Chip(this);
            chip.setText(keyword);
            chip.setClickable(false);
            chip.setChipBackgroundColorResource(R.color.teal_primary);
            chip.setTextColor(getColor(android.R.color.white));
            chipGroupKeywords.addView(chip);
        }
        chipGroupKeywords.setVisibility(View.VISIBLE);

        // Filter listings by keywords — match against productName, category,
        // suggestedActivities, description (case-insensitive)
        List<Listing> matched = filterListingsByKeywords(allListings, keywords);

        if (matched.isEmpty()) {
            showNoResults("No similar rentals found for this object.");
            return;
        }

        // Display matched listings
        ListingAdapter adapter = new ListingAdapter((listing, isFav) ->
                Toast.makeText(this, listing.getProductName(), Toast.LENGTH_SHORT).show());
        adapter.submitData(matched, new ArrayList<>());

        rvLensResults.setAdapter(adapter);
        rvLensResults.setVisibility(View.VISIBLE);
        layoutNoResults.setVisibility(View.GONE);
    }

    /**
     * Filters listings where any keyword matches productName, category,
     * suggestedActivities list, or description. Case-insensitive.
     */
    private List<Listing> filterListingsByKeywords(List<Listing> listings,
                                                   List<String> keywords) {
        List<Listing> result = new ArrayList<>();
        for (Listing listing : listings) {
            for (String keyword : keywords) {
                String kw = keyword.toLowerCase();
                boolean matches = false;

                if (listing.getProductName() != null
                        && listing.getProductName().toLowerCase().contains(kw)) {
                    matches = true;
                } else if (listing.getCategory() != null
                        && listing.getCategory().toLowerCase().contains(kw)) {
                    matches = true;
                } else if (listing.getDescription() != null
                        && listing.getDescription().toLowerCase().contains(kw)) {
                    matches = true;
                } else if (listing.getSuggestedActivities() != null) {
                    for (String activity : listing.getSuggestedActivities()) {
                        if (activity.toLowerCase().contains(kw)) {
                            matches = true;
                            break;
                        }
                    }
                }

                if (matches) {
                    result.add(listing);
                    break; // don't add the same listing twice for multiple keywords
                }
            }
        }
        return result;
    }

    // ---------------------------------------------------------------------------
    // UI state helpers
    // ---------------------------------------------------------------------------

    private void setLoadingState(boolean loading, String message) {
        layoutStatus.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (tvStatus != null) tvStatus.setText(message);
        btnCapture.setEnabled(!loading);
        btnCapture.setAlpha(loading ? 0.6f : 1.0f);
    }

    private void showNoResults(String message) {
        TextView tvNoResults = findViewById(R.id.tvNoResults);
        if (tvNoResults != null) tvNoResults.setText(message);
        rvLensResults.setVisibility(View.GONE);
        layoutNoResults.setVisibility(View.VISIBLE);
    }
}