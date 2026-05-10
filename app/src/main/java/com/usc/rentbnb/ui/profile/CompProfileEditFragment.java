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

public class CompProfileEditFragment extends Fragment {

    private EditText etName, etType, etYears, etPhone, etAddress, etRadius, etCoverage, etAreas;
    private CardView btnSaveProfile;
    private EditText[] allEditableFields;
    private String[] originalData;
    private boolean isSaveReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comp_profile_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.et_edit_comp_name);
        etType = view.findViewById(R.id.et_edit_comp_type);
        etYears = view.findViewById(R.id.et_edit_comp_years);
        etPhone = view.findViewById(R.id.et_edit_comp_phone);
        etAddress = view.findViewById(R.id.et_edit_comp_address);
        etRadius = view.findViewById(R.id.et_edit_comp_radius);
        etCoverage = view.findViewById(R.id.et_edit_comp_coverage);
        etAreas = view.findViewById(R.id.et_edit_comp_specific_areas);
        btnSaveProfile = view.findViewById(R.id.btn_save_comp_profile);

        btnSaveProfile.setClickable(false);
        btnSaveProfile.setEnabled(false);

        // --- NEW: FETCH FROM DATABASE ON LOAD ---
        SharedPreferences mockDB = requireActivity().getSharedPreferences("MockFirebaseDB", 0);
        etName.setText(mockDB.getString("comp_name", "Insert Company / Store"));
        etType.setText(mockDB.getString("comp_type", "Boat Rentals"));
        etYears.setText(mockDB.getString("comp_years", "2 years"));
        etPhone.setText(mockDB.getString("comp_phone", "9123456780"));
        etAddress.setText(mockDB.getString("comp_address", "N.s Cabanhud, Lapu-Lapu"));
        etRadius.setText(mockDB.getString("comp_radius", "45 km"));
        etCoverage.setText(mockDB.getString("comp_coverage", "Within City"));
        etAreas.setText(mockDB.getString("comp_areas", "Lakawon Islands, Sipalay, Guimaras"));

        allEditableFields = new EditText[]{etName, etType, etYears, etPhone, etAddress, etRadius, etCoverage, etAreas};
        originalData = new String[allEditableFields.length];

        for (int i = 0; i < allEditableFields.length; i++) {
            originalData[i] = allEditableFields[i].getText().toString().trim();
        }

        setupTextWatchers();

        // --- NEW: PUSH TO DATABASE ON SAVE ---
        btnSaveProfile.setOnClickListener(v -> {
            if (!isSaveReady) return;

            mockDB.edit()
                    .putString("comp_name", etName.getText().toString().trim())
                    .putString("comp_type", etType.getText().toString().trim())
                    .putString("comp_years", etYears.getText().toString().trim())
                    .putString("comp_phone", etPhone.getText().toString().trim())
                    .putString("comp_address", etAddress.getText().toString().trim())
                    .putString("comp_radius", etRadius.getText().toString().trim())
                    .putString("comp_coverage", etCoverage.getText().toString().trim())
                    .putString("comp_areas", etAreas.getText().toString().trim())
                    .apply(); // Mimics uploading to Firebase!

            Toast.makeText(getContext(), "Company Profile Saved!", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        });

        View btnChangePassword = view.findViewById(R.id.btn_comp_change_password);
        if(btnChangePassword != null) {
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

        for (EditText field : allEditableFields) {
            field.addTextChangedListener(watcher);
        }
    }

    private void checkIfModified() {
        boolean isModified = false;
        for (int i = 0; i < allEditableFields.length; i++) {
            if (!allEditableFields[i].getText().toString().trim().equals(originalData[i])) {
                isModified = true;
                break;
            }
        }

        boolean isValidPhone = etPhone.getText().toString().trim().length() == 10;
        isSaveReady = isModified && isValidPhone;

        btnSaveProfile.setEnabled(isSaveReady);
        btnSaveProfile.setClickable(isSaveReady);
        btnSaveProfile.setAlpha(isSaveReady ? 1.0f : 0.5f);
    }
}