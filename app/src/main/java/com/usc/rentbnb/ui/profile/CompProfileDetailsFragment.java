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

public class CompProfileDetailsFragment extends Fragment {

    // States
    private boolean isMasterEditing = false;
    private boolean isCompEditing = false;
    private boolean isServiceEditing = false;
    private boolean isPasswordExpanded = false;

    // View Arrays
    private View[] compDetailRows;
    private View[] serviceDetailRows;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comp_profile_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Map Header Elements
        TextView tvHeaderName = view.findViewById(R.id.tv_comp_header_name);
        EditText etHeaderName = view.findViewById(R.id.et_comp_header_name);
        TextView btnEditToggleTop = view.findViewById(R.id.btn_edit_profile_toggle);

        // Map the photo overlay and set a dummy click listener for the next dev
        LinearLayout overlayEditPhoto = view.findViewById(R.id.overlay_edit_comp_photo);
        overlayEditPhoto.setOnClickListener(v -> {
            android.widget.Toast.makeText(getContext(), "TODO: Implement Image Picker", android.widget.Toast.LENGTH_SHORT).show();
        });

        // Map Bubble Elements
        TextView btnEditComp = view.findViewById(R.id.btn_edit_comp_details);
        TextView btnConfirmComp = view.findViewById(R.id.btn_confirm_comp_details);
        TextView btnEditService = view.findViewById(R.id.btn_edit_service_details);
        TextView btnConfirmService = view.findViewById(R.id.btn_confirm_service_details);

        compDetailRows = new View[]{ view.findViewById(R.id.field_acct_name), view.findViewById(R.id.field_business_type), view.findViewById(R.id.field_years), view.findViewById(R.id.field_comp_email), view.findViewById(R.id.field_comp_phone), view.findViewById(R.id.field_comp_address) };
        serviceDetailRows = new View[]{ view.findViewById(R.id.field_radius), view.findViewById(R.id.field_within), view.findViewById(R.id.field_specific_areas) };

        // --- MASTER TOGGLE LOGIC ---
        btnEditToggleTop.setOnClickListener(v -> {
            isMasterEditing = !isMasterEditing;
            btnEditToggleTop.setText(isMasterEditing ? "SAVE ALL ✓" : "EDIT PROFILE ✎");

            // Toggle Header Name
            toggleSingleField(isMasterEditing, tvHeaderName, etHeaderName);

            // Toggle Photo Overlay
            overlayEditPhoto.setVisibility(isMasterEditing ? View.VISIBLE : View.GONE);

            // Force all bubbles to match the master state
            isCompEditing = isMasterEditing;
            isServiceEditing = isMasterEditing;
            forceSectionState(isMasterEditing, compDetailRows, btnEditComp, btnConfirmComp);
            forceSectionState(isMasterEditing, serviceDetailRows, btnEditService, btnConfirmService);
        });

        // --- INDIVIDUAL BUBBLE LOGIC ---
        btnEditComp.setOnClickListener(v -> { isCompEditing = !isCompEditing; forceSectionState(isCompEditing, compDetailRows, btnEditComp, btnConfirmComp); });
        btnConfirmComp.setOnClickListener(v -> { isCompEditing = !isCompEditing; forceSectionState(isCompEditing, compDetailRows, btnEditComp, btnConfirmComp); });

        btnEditService.setOnClickListener(v -> { isServiceEditing = !isServiceEditing; forceSectionState(isServiceEditing, serviceDetailRows, btnEditService, btnConfirmService); });
        btnConfirmService.setOnClickListener(v -> { isServiceEditing = !isServiceEditing; forceSectionState(isServiceEditing, serviceDetailRows, btnEditService, btnConfirmService); });

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