package com.usc.rentbnb.ui.review;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.AllReviewsAdapter;
import com.usc.rentbnb.viewmodels.ReviewViewModel;

public class AllReviewsActivity extends AppCompatActivity {

    public static final String EXTRA_LISTING_ID = "EXTRA_LISTING_ID";
    public static final String EXTRA_LISTING_TITLE = "EXTRA_LISTING_TITLE";

    private ReviewViewModel reviewViewModel;
    private AllReviewsAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvListingTitle;
    private LinearLayout header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reviews);

        header = findViewById(R.id.all_reviews_header);

        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + 8,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        String listingId = getIntent().getStringExtra(EXTRA_LISTING_ID);
        String listingTitle = getIntent().getStringExtra(EXTRA_LISTING_TITLE);

        if (listingId == null) {
            Toast.makeText(this, "Error: Missing listing ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews(listingTitle);
        setupRecyclerView();
        setupViewModel(listingId);
    }

    private void initViews(String title) {
        ImageView btnBack = findViewById(R.id.btn_back);
        tvListingTitle = findViewById(R.id.tv_listing_title_subtitle);
        progressBar = findViewById(R.id.all_reviews_progress_bar);

        btnBack.setOnClickListener(v -> finish());
        tvListingTitle.setText(title != null ? title : "Reviews");
    }

    private void setupRecyclerView() {
        RecyclerView rvReviews = findViewById(R.id.rv_all_reviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AllReviewsAdapter();
        rvReviews.setAdapter(adapter);
    }

    private void setupViewModel(String listingId) {
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        reviewViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        reviewViewModel.getErrorData().observe(this, errorMssg -> {
            if (errorMssg != null) {
                Toast.makeText(this, errorMssg, Toast.LENGTH_SHORT).show();
            }
        });

        reviewViewModel.getReviewData().observe(this, reviews -> {
            if (reviews != null) {
                adapter.setReviews(reviews);
            }
        });

        reviewViewModel.getReviews(listingId);
    }
}