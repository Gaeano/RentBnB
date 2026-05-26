package com.usc.rentbnb.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.RegisterRequest;
import com.usc.rentbnb.models.User;
import com.usc.rentbnb.viewmodels.UserProfileViewModel;

public class CompProfileEditFragment extends Fragment {

    private EditText etName, etType, etYears, etEmail, etPhone, etAddress,
            etCity, etProvince, etRadius, etCoverage, etAreas;
    private CardView btnSaveProfile;

    private String origName = "", origType = "", origYears = "", origPhone = "",
            origAddress = "", origCity = "", origProvince = "",
            origRadius = "", origCoverage = "", origAreas = "";

    private boolean isSaveReady = false;
    private UserProfileViewModel profileViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comp_profile_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        initViews(view);
        setupTextWatchers();
        setUpObservers();

        // Lock save button until something changes
        btnSaveProfile.setClickable(false);
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setAlpha(0.5f);

        // Email is read-only
        etEmail.setEnabled(false);
        etEmail.setAlpha(0.5f);

        profileViewModel.loadUserData();

        btnSaveProfile.setOnClickListener(v -> {
            if (!isSaveReady) return;

            // Lock while saving
            btnSaveProfile.setEnabled(false);
            btnSaveProfile.setAlpha(0.5f);

            RegisterRequest.ServiceArea serviceArea = new RegisterRequest.ServiceArea(
                    etRadius.getText().toString().trim(),
                    etCoverage.getText().toString().trim(),
                    etAreas.getText().toString().trim()
            );

            // Build the nested CompanyDetails object
            RegisterRequest.CompanyDetails companyDetails = new RegisterRequest.CompanyDetails(
                    etName.getText().toString().trim(),  // companyName
                    null,                                // permitNumber — not editable here, pass null
                    etType.getText().toString().trim(),
                    etYears.getText().toString().trim()
            );
            companyDetails.setServiceArea(serviceArea);

            RegisterRequest payload = new RegisterRequest();
            payload.setDisplayName(etName.getText().toString().trim());
            payload.setPhone(etPhone.getText().toString().trim());
            payload.setCompleteAddress(etAddress.getText().toString().trim());
            payload.setCity(etCity.getText().toString().trim());
            payload.setProvince(etProvince.getText().toString().trim());
            payload.setUserType("COMPANY");
            payload.setCompanyDetails(companyDetails);



            profileViewModel.updateUserProfile(payload);
        });
    }

    private void initViews(View view) {
        etName     = view.findViewById(R.id.et_edit_comp_name);
        etType     = view.findViewById(R.id.et_edit_comp_type);
        etYears    = view.findViewById(R.id.et_edit_comp_years);
        etEmail    = view.findViewById(R.id.et_edit_comp_email);
        etPhone    = view.findViewById(R.id.et_edit_comp_phone);
        etAddress  = view.findViewById(R.id.et_edit_comp_address);
        etCity     = view.findViewById(R.id.et_edit_comp_city);
        etProvince = view.findViewById(R.id.et_edit_comp_province);
        etRadius   = view.findViewById(R.id.et_edit_comp_radius);
        etCoverage = view.findViewById(R.id.et_edit_comp_coverage);
        etAreas    = view.findViewById(R.id.et_edit_comp_specific_areas);
        btnSaveProfile = view.findViewById(R.id.btn_save_comp_profile);
    }

    private void populateUI(User user) {
        if (user == null) return;

        etName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
        etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        etPhone.setText(user.getPhone() != null ? user.getPhone() : "");

        if (user.getLocation() != null) {
            etCity.setText(user.getLocation().getCity() != null ? user.getLocation().getCity() : "");
            etProvince.setText(user.getLocation().getProvince() != null ? user.getLocation().getProvince() : "");
        }

        etAddress.setText(user.getCompleteAddress() != null ? user.getCompleteAddress() : "");

        if (user.getCompanyDetails() != null) {
            etType.setText(user.getCompanyDetails().getBusinessType() != null
                    ? user.getCompanyDetails().getBusinessType() : "");
            etYears.setText(user.getCompanyDetails().getYearsOfOperation() != null
                    ? user.getCompanyDetails().getYearsOfOperation() : "");

            User.ServiceArea serviceArea = user.getCompanyDetails().getServiceArea();
            if (serviceArea != null) {
                etRadius.setText(serviceArea.getRadius() != null ? serviceArea.getRadius() : "");
                etCoverage.setText(serviceArea.getCoverage() != null ? serviceArea.getCoverage() : "");
                etAreas.setText(serviceArea.getSpecificAreas() != null ? serviceArea.getSpecificAreas() : "");
            }
        }

        origName     = etName.getText().toString().trim();
        origType     = etType.getText().toString().trim();
        origYears    = etYears.getText().toString().trim();
        origPhone    = etPhone.getText().toString().trim();
        origAddress  = etAddress.getText().toString().trim();
        origCity     = etCity.getText().toString().trim();
        origProvince = etProvince.getText().toString().trim();
        origRadius   = etRadius.getText().toString().trim();
        origCoverage = etCoverage.getText().toString().trim();
        origAreas    = etAreas.getText().toString().trim();
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { checkIfModified(); }
        };

        // Attach to every editable field (not email)
        for (EditText field : new EditText[]{
                etName, etType, etYears, etPhone, etAddress,
                etCity, etProvince, etRadius, etCoverage, etAreas
        }) {
            field.addTextChangedListener(watcher);
        }
    }

    private void checkIfModified() {
        String currentPhone = etPhone.getText().toString().trim();
        boolean isValidPhone = currentPhone.length() >= 10;

        boolean isModified =
                !origName.equals(etName.getText().toString().trim())
                        || !origType.equals(etType.getText().toString().trim())
                        || !origYears.equals(etYears.getText().toString().trim())
                        || !origPhone.equals(currentPhone)
                        || !origAddress.equals(etAddress.getText().toString().trim())
                        || !origCity.equals(etCity.getText().toString().trim())
                        || !origProvince.equals(etProvince.getText().toString().trim())
                        || !origRadius.equals(etRadius.getText().toString().trim())
                        || !origCoverage.equals(etCoverage.getText().toString().trim())
                        || !origAreas.equals(etAreas.getText().toString().trim());

        isSaveReady = isModified && isValidPhone;
        btnSaveProfile.setEnabled(isSaveReady);
        btnSaveProfile.setClickable(isSaveReady);
        btnSaveProfile.setAlpha(isSaveReady ? 1.0f : 0.5f);
    }

    private void setUpObservers() {
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) populateUI(user);
        });

        profileViewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(getContext(), "Company profile updated!", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            }
        });

        profileViewModel.getErrorData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                checkIfModified(); // Re-enable save so they can retry
            }
        });
    }
}