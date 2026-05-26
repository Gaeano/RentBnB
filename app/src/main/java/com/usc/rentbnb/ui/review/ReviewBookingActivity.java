package com.usc.rentbnb.ui.review;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Review;
import com.usc.rentbnb.models.ReviewRequest;
import com.usc.rentbnb.viewmodels.ReviewViewModel;

public class ReviewBookingActivity extends AppCompatActivity {

    private ReviewViewModel reviewViewModel;
    private LinearLayout header;
    private String bookingId;
    private String listingId;
    private String listingTitle;

    private TextView tvReviewPrompt;
    private RatingBar ratingBar;
    private TextInputEditText etComment;
    private MaterialButton btnSubmit;
    private View loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_booking);

        header = findViewById(R.id.review_header);
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

        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);


        extractIntents();
        initViews();
        setupListeners();
        setUpObservers();

    }



    private void extractIntents() {
        if (getIntent() != null) {
            bookingId = getIntent().getStringExtra("EXTRA_BOOKING_ID");
            listingId = getIntent().getStringExtra("EXTRA_LISTING_ID");
            listingTitle = getIntent().getStringExtra("EXTRA_LISTING_TITLE");
        }
    }

    private void initViews() {
        tvReviewPrompt = findViewById(R.id.tv_review_prompt);
        ratingBar = findViewById(R.id.rating_bar);
        etComment = findViewById(R.id.et_comment);
        btnSubmit = findViewById(R.id.btn_submit_review);
        loadingOverlay = findViewById(R.id.loading_overlay);
        ImageView btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        // Contextualize the UI for the user
        if (listingTitle != null && !listingTitle.isEmpty()) {
            tvReviewPrompt.setText("How was your experience with " + listingTitle + "?");
        }
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> {
            double rating = ratingBar.getRating();
            String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";

            if (rating < 1.0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            ReviewRequest request = new ReviewRequest(listingId, rating, comment);
            reviewViewModel.createReview(request);
        });
    }

    private void setUpObservers(){
        reviewViewModel.getSuccessMessage().observe(this, successMssg ->{
            if (successMssg != null && !successMssg.isEmpty()){
                Toast.makeText(this, successMssg, Toast.LENGTH_LONG).show();
                finish();
            }
        });

        reviewViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading){
                loadingOverlay.setVisibility(View.VISIBLE);
            } else {
                loadingOverlay.setVisibility(View.GONE);
            }
        });

        reviewViewModel.getErrorData().observe(this, errorMssg ->{
            if (errorMssg != null && !errorMssg.isEmpty()){
                Toast.makeText(this, errorMssg, Toast.LENGTH_LONG).show();
            }
        });
    }
}