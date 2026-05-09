package com.usc.rentbnb.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.usc.rentbnb.R;

public class IndivProfileDetailsFragment extends Fragment {

    private boolean isMasterEditing = false;
    private boolean isPasswordExpanded = false;
    private View[] detailRows;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_indiv_profile_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Map Header Elements
        TextView tvHeaderName = view.findViewById(R.id.tv_header_name);
        EditText etHeaderName = view.findViewById(R.id.et_header_name);
        TextView btnEditToggleTop = view.findViewById(R.id.btn_edit_profile_toggle);

        // Map the photo overlay and set a dummy click listener for the next dev
        LinearLayout overlayEditPhoto = view.findViewById(R.id.overlay_edit_photo);
        overlayEditPhoto.setOnClickListener(v -> {
            android.widget.Toast.makeText(getContext(), "TODO: Implement Image Picker", android.widget.Toast.LENGTH_SHORT).show();
        });

        // Map Bubble Elements
        TextView btnEditBottom = view.findViewById(R.id.btn_edit_details_bottom);
        TextView btnConfirmBottom = view.findViewById(R.id.btn_confirm_details);

        detailRows = new View[]{ view.findViewById(R.id.field_name), view.findViewById(R.id.field_age), view.findViewById(R.id.field_gender), view.findViewById(R.id.field_email), view.findViewById(R.id.field_phone), view.findViewById(R.id.field_address) };

        // --- MASTER TOGGLE LOGIC ---
        btnEditToggleTop.setOnClickListener(v -> {
            isMasterEditing = !isMasterEditing;
            btnEditToggleTop.setText(isMasterEditing ? "SAVE ALL ✓" : "EDIT PROFILE ✎");

            // Toggle Header Name
            toggleSingleField(isMasterEditing, tvHeaderName, etHeaderName);

            // Toggle Photo Overlay
            overlayEditPhoto.setVisibility(isMasterEditing ? View.VISIBLE : View.GONE);

            // Force bubble to match master state
            forceSectionState(isMasterEditing, detailRows, btnEditBottom, btnConfirmBottom);
        });

        // --- INDIVIDUAL BUBBLE LOGIC ---
        btnEditBottom.setOnClickListener(v -> { isMasterEditing = !isMasterEditing; forceSectionState(isMasterEditing, detailRows, btnEditBottom, btnConfirmBottom); });
        btnConfirmBottom.setOnClickListener(v -> { isMasterEditing = !isMasterEditing; forceSectionState(isMasterEditing, detailRows, btnEditBottom, btnConfirmBottom); });

        // Password Area Logic
        TextView btnChangePassword = view.findViewById(R.id.btn_change_password);
        LinearLayout passwordArea = view.findViewById(R.id.password_expansion_area);
        btnChangePassword.setOnClickListener(v -> {
            isPasswordExpanded = !isPasswordExpanded;
            passwordArea.setVisibility(isPasswordExpanded ? View.VISIBLE : View.GONE);
            btnChangePassword.setText(isPasswordExpanded ? "Cancel ✕" : "Change Password ✎");
        });
    }

    // Helper: Toggles a specific bubble array
    private void forceSectionState(boolean isEditing, View[] rows, TextView btnEdit, TextView btnConfirm) {
        btnEdit.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        btnConfirm.setVisibility(isEditing ? View.VISIBLE : View.GONE);

        for (View row : rows) {
            if (row != null) {
                toggleSingleField(isEditing, row.findViewById(R.id.tv_field_value), row.findViewById(R.id.et_field_value));
            }
        }
    }

    // Helper: Swaps a TextView and EditText
    private void toggleSingleField(boolean isEditing, TextView tvValue, EditText etValue) {
        if (isEditing) {
            tvValue.setVisibility(View.GONE);
            etValue.setVisibility(View.VISIBLE);
            etValue.setText(tvValue.getText());
        } else {
            tvValue.setVisibility(View.VISIBLE);
            etValue.setVisibility(View.GONE);
            tvValue.setText(etValue.getText());
        }
    }
}