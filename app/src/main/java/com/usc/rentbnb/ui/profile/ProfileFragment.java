package com.usc.rentbnb.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.User;
import com.usc.rentbnb.ui.auth.LoginActivity;
import com.usc.rentbnb.ui.history.HistoryActivity;
import com.usc.rentbnb.viewmodels.AuthViewModel;
import com.usc.rentbnb.viewmodels.UserProfileViewModel;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    // UI Elements
    private TextView tvName, tvEmail;
    private ImageView ivAvatar;
    private ProgressBar progressBar;
    private LinearLayout profileHeader;
    private TextView tvListingsCount, tvPurchasesCount, tvRatingValue, tvEarningsValue;

    private UserProfileViewModel profileViewModel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        initViews(view);
        setupObservers();

        ViewCompat.setOnApplyWindowInsetsListener(profileHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = statusBarHeight + 16;
            v.setLayoutParams(params);
            return insets;
        });
        setupClickListeners(view);

        profileViewModel.loadUserData();
    }

    private void setupObservers() {
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                populateUI(user);
            }
        });

        profileViewModel.getIsloading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        profileViewModel.getErrorData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews(View view) {
        // Map the UI elements
        tvName = view.findViewById(R.id.profile_name);
        tvEmail = view.findViewById(R.id.profile_email);
        ivAvatar = view.findViewById(R.id.profile_image);
        progressBar = view.findViewById(R.id.profile_progress_bar);
        profileHeader = view.findViewById(R.id.profile_header);

        tvListingsCount = view.findViewById(R.id.tv_listings_count);
        tvPurchasesCount = view.findViewById(R.id.tv_purchases_count);
        tvRatingValue = view.findViewById(R.id.tv_rating_value);
        tvEarningsValue = view.findViewById(R.id.tv_earnings_value);
    }

    private void populateUI(User user) {
        tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "N/A");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");

        tvRatingValue.setText(String.format(Locale.getDefault(), "%.1f", user.getRating()));
        tvEarningsValue.setText(String.format(Locale.getDefault(), "P%.1f", user.getTotalEarnings()));

        // Placeholders for now
        tvListingsCount.setText("0");
        tvPurchasesCount.setText("0");

        // Bind the image using Glide
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.userprofile)
                    .circleCrop() // Makes the image circular
                    .into(ivAvatar);
        }
    }

    private void setupClickListeners(View view) {
        LinearLayout menuHistory = view.findViewById(R.id.menu_history);
        LinearLayout menuHelpCenter = view.findViewById(R.id.menu_help_center);
        LinearLayout menuAppSettings = view.findViewById(R.id.menu_app_settings);
        LinearLayout menuLogout = view.findViewById(R.id.menu_logout);
        View btnEditProfile = view.findViewById(R.id.menu_profile_detail);

        menuHistory.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), HistoryActivity.class);
            startActivity(intent);
        });

        menuHelpCenter.setOnClickListener(v -> {
            Log.d("ProfileFragment", "Help Center button clicked");
        });

        menuAppSettings.setOnClickListener(v -> {
            Log.d("ProfileFragment", "App Settings button clicked");
        });

        menuLogout.setOnClickListener(v -> {
            AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
            authViewModel.logout();

            clearRememberMeData();

            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ProfileDetailsActivity.class);

                // TODO: Replace with actual check later (e.g., user.getAccountType().equals("COMPANY"))
                boolean isCompany = true;

                intent.putExtra("IS_COMPANY", isCompany);
                startActivity(intent);
            });
        }
    }

    private void clearRememberMeData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("RentBnBPrefs", requireActivity().MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("IS_REMEMBERED", false);
        edit.putString("SAVED_EMAIL", "");
        edit.apply();
    }
}