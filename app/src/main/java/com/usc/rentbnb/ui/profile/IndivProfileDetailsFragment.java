package com.usc.rentbnb.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.usc.rentbnb.R;

public class IndivProfileDetailsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_indiv_profile_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView btnEditProfile = view.findViewById(R.id.btn_edit_profile_toggle);
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ProfileDetailsEditActivity.class);
            intent.putExtra("IS_COMPANY", false);
            startActivity(intent);
        });
    }

    // NEW: onResume runs every time this screen becomes visible again!
    @Override
    public void onResume() {
        super.onResume();
        refreshDataFromDatabase();
    }

    private void refreshDataFromDatabase() {
        if (getView() == null) return;

        // 1. Connect to our Mock Database
        SharedPreferences mockDB = requireActivity().getSharedPreferences("MockFirebaseDB", 0);

        // 2. Fetch the latest data (with fallback defaults)
        String name = mockDB.getString("indiv_name", "Bulgogi Bibbing Heredia");
        String phone = mockDB.getString("indiv_phone", "9123456780");
        String address = mockDB.getString("indiv_address", "Canada, Vancouver");

        // 3. Update the UI
        TextView tvHeaderName = getView().findViewById(R.id.tv_header_name);
        if (tvHeaderName != null) tvHeaderName.setText(name);

        // Update the include rows (We target the specific row, then find the value text inside it)
        updateRowText(R.id.field_name, name);
        updateRowText(R.id.field_phone, "+63 " + phone); // Re-add the prefix for display
        updateRowText(R.id.field_address, address);
    }

    // Helper method for the reusable XML rows
    private void updateRowText(int rowId, String text) {
        View row = getView().findViewById(rowId);
        if (row != null) {
            TextView tvValue = row.findViewById(R.id.tv_field_value);
            if (tvValue != null) tvValue.setText(text);
        }
    }
}