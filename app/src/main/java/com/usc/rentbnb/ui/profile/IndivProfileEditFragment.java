package com.usc.rentbnb.ui.profile;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.RegisterRequest;
import com.usc.rentbnb.models.User;
import com.usc.rentbnb.viewmodels.UserProfileViewModel;

public class IndivProfileEditFragment extends Fragment {

    private EditText etName, etAge, etGender, etEmail, etPhone, etAddress, etCity, etProvince;
    private CardView btnSaveProfile;
    private String origName = "", origAge = "", origGender = "", origPhone = "", origAddress = "", origCity = "", origProvince = "";
    private boolean isSaveReady = false;

    private UserProfileViewModel profileViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_indiv_profile_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        etName = view.findViewById(R.id.et_edit_name);
        etAge = view.findViewById(R.id.et_edit_age);
        etGender = view.findViewById(R.id.et_edit_gender);
        etEmail = view.findViewById(R.id.et_edit_email);
        etPhone = view.findViewById(R.id.et_edit_phone);
        etAddress = view.findViewById(R.id.et_edit_address);
        etCity = view.findViewById(R.id.et_edit_city);
        etProvince = view.findViewById(R.id.et_edit_province);
        btnSaveProfile = view.findViewById(R.id.btn_save_profile);

        etEmail.setEnabled(false);
        etEmail.setAlpha(0.5f);

        btnSaveProfile.setClickable(false);
        btnSaveProfile.setEnabled(false);

        setupTextWatchers();
        setUpObservers();

        //check if user is logged in via google
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isGoogleUser = false;

        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                if (profile.getProviderId().equals("google.com")) {
                    isGoogleUser = true;
                    break;
                }
            }
        }

        // Hide the button if they are a Google user
        View btnChangePassword = view.findViewById(R.id.btn_change_password);
        if (isGoogleUser) {
            btnChangePassword.setVisibility(View.GONE);
        } else {
            btnChangePassword.setVisibility(View.VISIBLE);
        }

        // Trigger the backend to fetch the user data to populate the fields
        profileViewModel.loadUserData();

        btnSaveProfile.setOnClickListener(v -> {
            if (!isSaveReady) return;

            // 1. Lock the button to prevent spam clicks
            btnSaveProfile.setEnabled(false);
            btnSaveProfile.setAlpha(0.5f);

            // 2. Package the new data into a RegisterRequest model
            RegisterRequest updatePayload = new RegisterRequest();
            updatePayload.setDisplayName(etName.getText().toString().trim());
            updatePayload.setAge(etAge.getText().toString().trim());
            updatePayload.setGender(etGender.getText().toString().trim());
            updatePayload.setPhone(etPhone.getText().toString().trim());
            updatePayload.setCompleteAddress(etAddress.getText().toString().trim());
            updatePayload.setCity(etCity.getText().toString().trim());
            updatePayload.setProvince(etProvince.getText().toString().trim());

            // 3. Send it to the ViewModel
            profileViewModel.updateUserProfile(updatePayload);
        });

         btnChangePassword = view.findViewById(R.id.btn_change_password);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                startActivity(new Intent(requireActivity(), ChangePassActivity.class));
            });
        }
    }

    private void populateUI(User user) {
        etName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
        etAge.setText(user.getAge() != null ? user.getAge() : "");
        etGender.setText(user.getGender() != null ? user.getGender() : "");
        etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        etCity.setText(user.getLocation().getCity() != null ? user.getLocation().getCity() : "");
        etProvince.setText(user.getLocation().getProvince() != null ? user.getLocation().getProvince() : "");


        etAddress.setText(user.getCompleteAddress());


        // Update tracking variables so the "Save" button knows what the baseline is
        origName = etName.getText().toString().trim();
        origAge = etAge.getText().toString().trim();
        origGender = etGender.getText().toString().trim();
        origPhone = etPhone.getText().toString().trim();
        origAddress = etAddress.getText().toString().trim();
        origCity = etCity.getText().toString().trim();
        origProvince = etProvince.getText().toString().trim();

    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { checkIfModified(); }
        };
        etName.addTextChangedListener(watcher);
        etAge.addTextChangedListener(watcher);
        etGender.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);
        etAddress.addTextChangedListener(watcher);
        etCity.addTextChangedListener(watcher);
        etProvince.addTextChangedListener(watcher);
    }

    private void checkIfModified() {
        String currentPhone = etPhone.getText().toString().trim();
        boolean isValidPhone = currentPhone.length() >= 10;

        boolean isModified = !origName.equals(etName.getText().toString().trim())
                || !origAge.equals(etAge.getText().toString().trim())
                || !origGender.equals(etGender.getText().toString().trim())
                || !origPhone.equals(currentPhone)
                || !origAddress.equals(etAddress.getText().toString().trim())
                || !origCity.equals(etCity.getText().toString().trim())
                || !origProvince.equals(etProvince.getText().toString().trim());

        isSaveReady = isModified && isValidPhone;
        btnSaveProfile.setEnabled(isSaveReady);
        btnSaveProfile.setClickable(isSaveReady);
        btnSaveProfile.setAlpha(isSaveReady ? 1.0f : 0.5f);
    }

    // FIXED OBSERVERS
    private void setUpObservers(){
        // 1. Observe User Data to fill the UI
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null){
                populateUI(user);
            }
        });

        // 2. Observe the Success signal
        profileViewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            }
        });

        // 3. Observe Errors
        profileViewModel.getErrorData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                checkIfModified(); // Re-enable the save button so they can try again
            }
        });
    }
}