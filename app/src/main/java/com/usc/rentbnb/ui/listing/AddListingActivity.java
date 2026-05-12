package com.usc.rentbnb.ui.listing;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.FAQ;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.viewmodels.AddListingViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddListingActivity extends AppCompatActivity {

    public static final int TOTAL_STEPS = 6;

    private ImageButton btnBack;
    private TextView tvStepLabel;
    private TextView tvStepCounter;
    private ProgressBar progressBar;
    private int currentStep = 1;
    private AddListingViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_listing);

        viewModel = new ViewModelProvider(this).get(AddListingViewModel.class);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top,
                        systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        bindViews();
        setupBackButton();

        if (savedInstanceState == null) {
            mainView.post(() -> navigateToStep(1, false));
        }

        fetchDefaultFaqs();
    }

    private void fetchDefaultFaqs() {
        ApiClient.getApiService().getDefaultFaqs().enqueue(new Callback<List<FAQ>>() {
            @Override
            public void onResponse(@NonNull Call<List<FAQ>> call, @NonNull Response<List<FAQ>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    viewModel.faqs = response.body();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FAQ>> call, @NonNull Throwable t) {
                // Silently fail, just means no default FAQs
            }
        });
    }

    public void goNextStep() {
        if (currentStep < TOTAL_STEPS) {
            navigateToStep(currentStep + 1, true);
        } else if (currentStep == TOTAL_STEPS) {
            navigateToSuccess();
        }
    }

    public void onBackStep() {
        if (currentStep > 1) {
            navigateToStep(currentStep - 1, false);
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        onBackStep();
    }

    public void restartFormForNewDraft() {
        currentStep = 1;
        updateToolbar(1);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_left
        );
        ft.replace(R.id.fragmentContainer, new ListingInfoFragment()).commit();
    }

    public void goToSuccessWithCount(int count) {
        currentStep = TOTAL_STEPS + 1;

        tvStepLabel.setVisibility(View.GONE);
        tvStepCounter.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) tvTitle.setVisibility(View.GONE);

        ListingSuccessFragment successFragment = new ListingSuccessFragment();
        Bundle args = new Bundle();
        args.putInt("listing_count", count);
        successFragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragmentContainer, successFragment)
                .commit();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        tvStepLabel = findViewById(R.id.tvStepLabel);
        tvStepCounter = findViewById(R.id.tvStepCounter);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> onBackStep());
    }

    private void navigateToStep(int step, boolean forward) {
        currentStep = step;
        updateToolbar(step);
        replaceFragment(fragmentForStep(step), forward);
    }

    private void navigateToSuccess() { goToSuccessWithCount(1);}

    private void updateToolbar(int step) {
        btnBack.setVisibility(View.VISIBLE);
        tvStepLabel.setVisibility(View.VISIBLE);
        tvStepCounter.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) tvTitle.setVisibility(View.VISIBLE);

        tvStepCounter.setText(String.format("%02d / %02d", step, TOTAL_STEPS));
        progressBar.setMax(TOTAL_STEPS);
        progressBar.setProgress(step);

        switch (step) {
            case 1: tvStepLabel.setText("Listing Details");  break;
            case 2: tvStepLabel.setText("Listing Type"); break;
            case 3: tvStepLabel.setText("Pricing"); break;
            case 4: tvStepLabel.setText("Activities"); break;
            case 5: tvStepLabel.setText("Photos"); break;
            case 6: tvStepLabel.setText("Review & Submit"); break;
        }
    }

    private Fragment fragmentForStep(int step) {
        switch (step) {
            case 1: return new ListingInfoFragment();
            case 2: return new ListingTypeFragment();
            case 3: return new ListingPricingFragment();
            case 4: return new ListingActivitiesFragment();
            case 5: return new ListingImagesFragment();
            case 6: return new ListingSummaryFragment();
            default: return new ListingInfoFragment();
        }
    }

    private void replaceFragment(Fragment fragment, boolean forward) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (forward) {
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
                    R.anim.slide_in_right, R.anim.slide_out_left);
        }
        ft.replace(R.id.fragmentContainer, fragment).commit();
    }
}