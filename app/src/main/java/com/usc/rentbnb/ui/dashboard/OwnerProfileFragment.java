package com.usc.rentbnb.ui.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.User;
import com.usc.rentbnb.ui.auth.LoginActivity;
import com.usc.rentbnb.ui.home.HomeActivity;
import com.usc.rentbnb.ui.profile.ProfileDetailsActivity;
import com.usc.rentbnb.viewmodels.AuthViewModel;
import com.usc.rentbnb.viewmodels.UserProfileViewModel;

// TODO: add count reviews in backend

public class OwnerProfileFragment extends Fragment {
    private UserProfileViewModel userProfileViewModel;
    private TextView profileHeader, ownerName, memberSince, profileRating, profileReviews, profileRentals;
    private View rowProfileDetails, rowPayoutMethod, rowHelpCenter, rowSignOut;
    private SwitchMaterial switchPushNotifs;
    private ShapeableImageView ownerAvatar;
    private boolean isCompany = false;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileHeader = view.findViewById(R.id.profile_header);

        ViewCompat.setOnApplyWindowInsetsListener(profileHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = statusBarHeight + 16;
            v.setLayoutParams(params);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews(view);

        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        setUpObservers();

        userProfileViewModel.loadUserData();

        ExtendedFloatingActionButton fabSwitchMode = view.findViewById(R.id.fab_switch_mode);
        NestedScrollView scrollView = view.findViewById(R.id.owner_profile_scroll_view);

        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY && fabSwitchMode.isShown()) {
                fabSwitchMode.hide();
            } else if (scrollY < oldScrollY && !fabSwitchMode.isShown()) {
                fabSwitchMode.show();
            }
        });

        fabSwitchMode.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), HomeActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        rowProfileDetails.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ProfileDetailsActivity.class);

            intent.putExtra("IS_COMPANY", isCompany);
            startActivity(intent);
        });

        rowPayoutMethod.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Open Payout Settings", Toast.LENGTH_SHORT).show();
        });

        if (switchPushNotifs != null) {
            switchPushNotifs.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "enabled" : "disabled";
                Toast.makeText(getContext(), "Notifications " + status, Toast.LENGTH_SHORT).show();
            });
        }

        rowHelpCenter.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Open Inquilino Help Center", Toast.LENGTH_SHORT).show();
        });

        rowSignOut.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Signing out...", Toast.LENGTH_SHORT).show();
            authViewModel.logout();

            clearRememberMeData();

            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void initViews(View view){
        ownerName = view.findViewById(R.id.tvOwnerName);
        memberSince = view.findViewById(R.id.tvOwnerSubtitle);
        profileRating = view.findViewById(R.id.tvProfileRating);
        profileReviews = view.findViewById(R.id.tvProfileReviews);
        profileRentals = view.findViewById(R.id.tvProfileRentals);
        ownerAvatar = view.findViewById(R.id.ivOwnerAvatar);

        rowProfileDetails = view.findViewById(R.id.rowProfileDetails);
        rowPayoutMethod = view.findViewById(R.id.rowPayoutMethod);
        switchPushNotifs = view.findViewById(R.id.switchPushNotifications);
        rowHelpCenter = view.findViewById(R.id.rowHelpCenter);
        rowSignOut = view.findViewById(R.id.rowSignOut);

    }
    private void populateUI(User user) {
        if (user == null) return;

        if (ownerAvatar != null){
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()){
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.userprofile)
                        .into(ownerAvatar);
            }else{
                Glide.with(this)
                        .load(R.drawable.userprofile)
                        .into(ownerAvatar);
            }


        }

        String nameStr = (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) ? user.getDisplayName() : "Not Set";
        String dateStr = (user.getCreatedAt() != null && user.getCreatedAt().length() >= 10) ? "Member since " + user.getCreatedAt().substring(0, 10) : "New Member";

        if (ownerName != null) ownerName.setText(nameStr);
        if (memberSince != null) memberSince.setText(dateStr);
        if (profileRating != null) profileRating.setText(String.format("%.1f", user.getRating()));
        if (profileReviews != null) profileReviews.setText(String.valueOf((int) user.getTotalReviews()));
        if (profileRentals != null) profileRentals.setText(String.valueOf(user.getListingsCount()));
    }

    private void setUpObservers(){

        userProfileViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null){
                populateUI(user);
                String userType = user.getUserType();
                if ("COMPANY".equals(userType)) {
                    isCompany = true;
                }
            }
        });

        userProfileViewModel.getErrorData().observe(getViewLifecycleOwner(), errorMssg -> {
            if (errorMssg != null){
                Toast.makeText(getContext(), "Error: " + errorMssg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearRememberMeData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("RentBnBPrefs", requireActivity().MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("IS_REMEMBERED", false);
        edit.putString("SAVED_EMAIL", "");
        edit.apply();
    }
}