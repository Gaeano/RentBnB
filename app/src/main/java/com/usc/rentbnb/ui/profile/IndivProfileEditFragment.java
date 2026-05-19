package com.usc.rentbnb.ui.profile;

import android.content.SharedPreferences;
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
import com.usc.rentbnb.R;

public class IndivProfileEditFragment extends Fragment {

    private EditText etName, etAge, etGender, etEmail, etPhone, etAddress;
    private CardView btnSaveProfile;
    private String origName, origAge, origGender, origPhone, origAddress;
    private boolean isSaveReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_indiv_profile_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.et_edit_name);
        etAge = view.findViewById(R.id.et_edit_age);
        etGender = view.findViewById(R.id.et_edit_gender);
        etEmail = view.findViewById(R.id.et_edit_email);
        etPhone = view.findViewById(R.id.et_edit_phone);
        etAddress = view.findViewById(R.id.et_edit_address);
        btnSaveProfile = view.findViewById(R.id.btn_save_profile);

        btnSaveProfile.setClickable(false);
        btnSaveProfile.setEnabled(false);

        SharedPreferences mockDB = requireActivity().getSharedPreferences("MockFirebaseDB", 0);
        etName.setText(mockDB.getString("indiv_name", "Bulgogi Bibbing Heredia"));
        etAge.setText(mockDB.getString("indiv_age", "28"));
        etGender.setText(mockDB.getString("indiv_gender", "Female"));
        etEmail.setText(mockDB.getString("indiv_email", "amazingGrace@gmail.com"));
        etPhone.setText(mockDB.getString("indiv_phone", "9123456780"));
        etAddress.setText(mockDB.getString("indiv_address", "Canada, Vancouver"));

        origName = etName.getText().toString().trim();
        origAge = etAge.getText().toString().trim();
        origGender = etGender.getText().toString().trim();
        origPhone = etPhone.getText().toString().trim();
        origAddress = etAddress.getText().toString().trim();

        setupTextWatchers();

        btnSaveProfile.setOnClickListener(v -> {
            if (!isSaveReady) return;

            mockDB.edit()
                    .putString("indiv_name", etName.getText().toString().trim())
                    .putString("indiv_age", etAge.getText().toString().trim())
                    .putString("indiv_gender", etGender.getText().toString().trim())
                    .putString("indiv_phone", etPhone.getText().toString().trim())
                    .putString("indiv_address", etAddress.getText().toString().trim())
                    .apply();

            Toast.makeText(getContext(), "Profile Saved!", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        });

        View btnChangePassword = view.findViewById(R.id.btn_change_password);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                startActivity(new android.content.Intent(requireActivity(), ChangePassActivity.class));
            });
        }
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
    }

    private void checkIfModified() {
        String currentPhone = etPhone.getText().toString().trim();
        boolean isValidPhone = currentPhone.length() == 10;

        boolean isModified = !origName.equals(etName.getText().toString().trim())
                || !origAge.equals(etAge.getText().toString().trim())
                || !origGender.equals(etGender.getText().toString().trim())
                || !origPhone.equals(currentPhone)
                || !origAddress.equals(etAddress.getText().toString().trim());

        isSaveReady = isModified && isValidPhone;
        btnSaveProfile.setEnabled(isSaveReady);
        btnSaveProfile.setClickable(isSaveReady);
        btnSaveProfile.setAlpha(isSaveReady ? 1.0f : 0.5f);
    }
}