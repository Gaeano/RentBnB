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
    private TextView tvDetailName, tvDetailAge, tvDetailGender, tvDetailEmail, tvDetailPhone, tvDetailAddress;
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

        View btnEditProfile = view.findViewById(R.id.btn_edit_profile_toggle);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ProfileDetailsEditActivity.class);
                intent.putExtra("IS_COMPANY", false);
                startActivity(intent);
            });
        }

        profileViewModel.loadUserData();
    }

    private void initViews(View view) {

        tvDetailName = view.findViewById(R.id.tv_detail_name);
        tvDetailAge = view.findViewById(R.id.tv_detail_age);
        tvDetailGender = view.findViewById(R.id.tv_detail_gender);
        tvDetailEmail = view.findViewById(R.id.tv_detail_email);
        tvDetailPhone = view.findViewById(R.id.tv_detail_phone);
        tvDetailAddress = view.findViewById(R.id.tv_detail_address);

        skeleton = view.findViewById(R.id.skeleton_profile_details);
    }

    private void populateUI(User user) {
        if (user == null) return;

        String nameStr = (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) ? user.getDisplayName() : "Not Set";

        if (tvDetailName != null) tvDetailName.setText(nameStr);
        if (tvDetailEmail != null) tvDetailEmail.setText(user.getEmail() != null ? user.getEmail() : "Not Set");
        if (tvDetailPhone != null) tvDetailPhone.setText(user.getPhone() != null ? "(+63) " + user.getPhone() : "Not Set");
        if (tvDetailAge != null) tvDetailAge.setText(user.getAge() != null ? user.getAge() : "Not Set");
        if (tvDetailGender != null) tvDetailGender.setText(user.getGender() != null ? user.getGender() : "Not Set");

        String address = "Not Set";
        if (user.getLocation() != null) {
            String city = user.getLocation().getCity() != null ? user.getLocation().getCity() : "";
            String prov = user.getLocation().getProvince() != null ? user.getLocation().getProvince() : "";
            if (!city.isEmpty() || !prov.isEmpty()) {
                address = city + ", " + prov;
            }
        }
        if (tvDetailAddress != null) tvDetailAddress.setText(address);
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