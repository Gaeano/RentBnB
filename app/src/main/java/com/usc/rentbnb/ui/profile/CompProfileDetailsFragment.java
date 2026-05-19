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
    private TextView tvCompName, tvCompType, tvCompYears, tvCompEmail, tvCompPhone, tvCompAddress;
    private TextView tvRadius, tvCoverage, tvSpecificAreas;
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

        profileViewModel.loadUserData();
    }

    private void initViews(View view) {

        tvCompName = view.findViewById(R.id.tv_detail_comp_name);
        tvCompType = view.findViewById(R.id.tv_detail_comp_type);
        tvCompYears = view.findViewById(R.id.tv_detail_comp_years);
        tvCompEmail = view.findViewById(R.id.tv_detail_comp_email);
        tvCompPhone = view.findViewById(R.id.tv_detail_comp_phone);
        tvCompAddress = view.findViewById(R.id.tv_detail_comp_address);

        tvRadius = view.findViewById(R.id.tv_detail_radius);
        tvCoverage = view.findViewById(R.id.tv_detail_coverage);
        tvSpecificAreas = view.findViewById(R.id.tv_detail_specific_areas);

        skeleton = view.findViewById(R.id.skeleton_comp_profile_details);
    }

    private void populateUI(User user) {
        if (user == null) return;

        String nameStr = (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) ? user.getDisplayName() : "Not Set";
        if (tvHeaderName != null) tvHeaderName.setText(nameStr);

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty() && profileImg != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.profile_display_picture)
                    .circleCrop()
                    .into(profileImg);
        }

        if (tvCompName != null) tvCompName.setText(nameStr);
        if (tvCompEmail != null) tvCompEmail.setText(user.getEmail() != null ? user.getEmail() : "Not Set");
        if (tvCompPhone != null) tvCompPhone.setText(user.getPhone() != null ? "+63 " + user.getPhone() : "Not Set");

        String address = "Not Set";
        if (user.getLocation() != null) {
            String city = user.getLocation().getCity() != null ? user.getLocation().getCity() : "";
            String prov = user.getLocation().getProvince() != null ? user.getLocation().getProvince() : "";
            if (!city.isEmpty() || !prov.isEmpty()) {
                address = city + ", " + prov;
            }
        }
        if (tvCompAddress != null) tvCompAddress.setText(address);

        if (user.getCompanyDetails() != null) {
            if (tvCompType != null) tvCompType.setText(user.getCompanyDetails().getBusinessType());
            if (tvCompYears != null) tvCompYears.setText(user.getCompanyDetails().getYearsOfOperation());
        }

        // Setup service areas dynamically or via placeholders until fully supported by model schemas
        if (tvRadius != null) tvRadius.setText("45 km");
        if (tvCoverage != null) tvCoverage.setText("Within City");
        if (tvSpecificAreas != null) tvSpecificAreas.setText("Lakawon Islands, Sipalay, Guimaras");
    }

    private void setUpObservers() {
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                populateUI(user);
            }
        });

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