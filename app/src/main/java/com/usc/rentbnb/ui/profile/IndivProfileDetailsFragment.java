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

public class IndivProfileDetailsFragment extends Fragment {

    private UserProfileViewModel profileViewModel;
    private ImageView profileImg;
    private TextView userName;
    private Skeleton skeleton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_indiv_profile_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        initViews(view);
        setUpObservers();
        Log.d("IndivProfileDetails", "IM opening");

        TextView btnEditProfile = view.findViewById(R.id.btn_edit_profile_toggle);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ProfileDetailsEditActivity.class);
                intent.putExtra("IS_COMPANY", false);
                startActivity(intent);
            });
        }

        // Trigger the network call via ViewModel
        profileViewModel.loadUserData();
    }

    private void initViews(View view) {
        profileImg = view.findViewById(R.id.iv_avatar);
        userName = view.findViewById(R.id.user_name);

        // Map the Skeleton wrapper from XML
        skeleton = view.findViewById(R.id.skeleton_profile_details);
    }

    private void populateUI(User user) {
        if (user == null) return;

        // 1. Setup Header
        String nameStr = (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) ? user.getDisplayName() : "Not Set";
        userName.setText(nameStr);

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.profile_display_picture)
                    .circleCrop()
                    .into(profileImg);
        }

        // 2. Setup Include Rows WITH LABELS
        updateRowText(R.id.field_name, "Name", nameStr);
        updateRowText(R.id.field_email, "Email", user.getEmail() != null ? user.getEmail() : "Not Set");
        updateRowText(R.id.field_phone, "Phone Number", user.getPhone() != null ? user.getPhone() : "Not Set");

        // Format Location Safely
        String address = "Not Set";
        if (user.getLocation() != null) {
            String city = user.getLocation().getCity() != null ? user.getLocation().getCity() : "";
            String prov = user.getLocation().getProvince() != null ? user.getLocation().getProvince() : "";
            if (!city.isEmpty() || !prov.isEmpty()) {
                address = city + ", " + prov;
            }
        }
        updateRowText(R.id.field_address, "Address", address);

        // Your current User model does not track Age and Gender natively.
        // We set these as placeholders until you update the User.java model and backend schema to support them.
        updateRowText(R.id.field_age, "Age", user.getAge());
        updateRowText(R.id.field_gender, "Gender", user.getGender());
    }

    // Helper method to target the specific included XML rows, now including the label
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