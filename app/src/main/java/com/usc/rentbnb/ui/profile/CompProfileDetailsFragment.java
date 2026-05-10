package com.usc.rentbnb.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.faltenreich.skeletonlayout.Skeleton;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.User;
import com.usc.rentbnb.viewmodels.UserProfileViewModel;

public class CompProfileDetailsFragment extends Fragment {

    private UserProfileViewModel profileViewModel;
    private ImageView profileImg;
    private TextView tvHeaderName;
    private Skeleton skeleton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comp_profile_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        initViews(view);
        setUpObservers();

        TextView btnEditProfile = view.findViewById(R.id.btn_edit_profile_toggle);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ProfileDetailsEditActivity.class);
                intent.putExtra("IS_COMPANY", true);
                startActivity(intent);
            });
        }

        // Trigger the network call via ViewModel
        profileViewModel.loadUserData();
    }

    private void initViews(View view) {
        // Ensure your XML has an ImageView with this ID for the company logo/avatar
        profileImg = view.findViewById(R.id.iv_avatar);
        tvHeaderName = view.findViewById(R.id.tv_comp_header_name);

        // IMPORTANT: Ensure you wrap your XML layout in a SkeletonLayout with this ID
        skeleton = view.findViewById(R.id.skeleton_comp_profile_details);
    }

    private void populateUI(User user) {
        if (user == null) return;

        // 1. Setup Header
        String nameStr = (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) ? user.getDisplayName() : "Not Set";
        if (tvHeaderName != null) {
            tvHeaderName.setText(nameStr);
        }

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty() && profileImg != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.profile_display_picture)
                    .circleCrop()
                    .into(profileImg);
        }

        // 2. Setup Include Rows WITH LABELS
        // Mapping the standard user fields
        updateRowText(R.id.field_acct_name, "Account Name", nameStr);
        updateRowText(R.id.field_comp_phone, "Phone Number", user.getPhone() != null ? user.getPhone() : "Not Set");

        // Format Location Safely
        String address = "Not Set";
        if (user.getLocation() != null) {
            String city = user.getLocation().getCity() != null ? user.getLocation().getCity() : "";
            String prov = user.getLocation().getProvince() != null ? user.getLocation().getProvince() : "";
            if (!city.isEmpty() || !prov.isEmpty()) {
                address = city + ", " + prov;
            }
        }
        updateRowText(R.id.field_comp_address, "Address", address);
        String businessType = user.getCompanyDetails().getBusinessType();
        String yearsOperation = user.getCompanyDetails().getYearsOfOperation();
        // Mapping Company-Specific Fields (Placeholders until User model is updated)
        updateRowText(R.id.field_business_type, "Business Type", businessType);
        updateRowText(R.id.field_years, "Years in Operation", yearsOperation);
        updateRowText(R.id.field_radius, "Operational Radius", "Not Set");
        updateRowText(R.id.field_within, "Coverage", "Not Set");
        updateRowText(R.id.field_specific_areas, "Specific Areas", "Not Set");
    }

    // Helper method to target the specific included XML rows, including the label
    private void updateRowText(int rowId, String labelText, String valueText) {
        View row = getView();
        if (row != null) {
            View includeLayout = row.findViewById(rowId);
            if (includeLayout != null) {
                // IMPORTANT: Ensure your item_profile_field.xml has a TextView with id tv_field_label
                TextView tvLabel = includeLayout.findViewById(R.id.tv_field_label);
                TextView tvValue = includeLayout.findViewById(R.id.tv_field_value);

                if (tvLabel != null) {
                    tvLabel.setText(labelText);
                }

                if (tvValue != null) {
                    tvValue.setText(valueText);
                }
            }
        }
    }

    private void setUpObservers() {
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                populateUI(user);
            }
        });

        // Toggle Skeleton animation
        profileViewModel.getIsloading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                if (skeleton != null) skeleton.showSkeleton();
            } else {
                if (skeleton != null) skeleton.showOriginal();
            }
        });

        profileViewModel.getErrorData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}