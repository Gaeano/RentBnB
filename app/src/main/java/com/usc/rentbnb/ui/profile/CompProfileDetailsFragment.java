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

public class CompProfileDetailsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comp_profile_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView btnEditProfile = view.findViewById(R.id.btn_edit_profile_toggle);
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ProfileDetailsEditActivity.class);
            intent.putExtra("IS_COMPANY", true);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDataFromDatabase();
    }

    private void refreshDataFromDatabase() {
        if (getView() == null) return;

        SharedPreferences mockDB = requireActivity().getSharedPreferences("MockFirebaseDB", 0);

        String name = mockDB.getString("comp_name", "Bulgogi Bibbing Heredia");
        String type = mockDB.getString("comp_type", "Boat Rentals");
        String years = mockDB.getString("comp_years", "2 years");
        String phone = mockDB.getString("comp_phone", "9123456780");
        String address = mockDB.getString("comp_address", "N.s Cabanhud, Lapu-Lapu");
        String radius = mockDB.getString("comp_radius", "45 km");
        String coverage = mockDB.getString("comp_coverage", "Within City");
        String areas = mockDB.getString("comp_areas", "Lakawon Islands, Sipalay, Guimaras");

        TextView tvHeaderName = getView().findViewById(R.id.tv_comp_header_name);
        if (tvHeaderName != null) tvHeaderName.setText(name);

        updateRowText(R.id.field_acct_name, name);
        updateRowText(R.id.field_business_type, type);
        updateRowText(R.id.field_years, years);
        updateRowText(R.id.field_comp_phone, "+63 " + phone);
        updateRowText(R.id.field_comp_address, address);
        updateRowText(R.id.field_radius, radius);
        updateRowText(R.id.field_within, coverage);
        updateRowText(R.id.field_specific_areas, areas);
    }

    private void updateRowText(int rowId, String text) {
        View row = getView().findViewById(rowId);
        if (row != null) {
            TextView tvValue = row.findViewById(R.id.tv_field_value);
            if (tvValue != null) tvValue.setText(text);
        }
    }
}