package com.usc.rentbnb.ui.listing;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.usc.rentbnb.R;

public class ListingPricingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing_pricing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View btnHourly  = view.findViewById(R.id.btnHourly);
        View btnDaily   = view.findViewById(R.id.btnDaily);
        View btnWeekly  = view.findViewById(R.id.btnWeekly);

        selectUnit(view, R.id.btnDaily);

        btnHourly.setOnClickListener(v -> selectUnit(view, R.id.btnHourly));
        btnDaily .setOnClickListener(v -> selectUnit(view, R.id.btnDaily));
        btnWeekly.setOnClickListener(v -> selectUnit(view, R.id.btnWeekly));

        view.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).goNextStep();
            }
        });
    }

    private int selectedUnitId = R.id.btnDaily;

    private void selectUnit(View root, int targetId) {
        int[] ids = {R.id.btnHourly, R.id.btnDaily, R.id.btnWeekly };

        for (int id : ids) {
            TextView textView = root.findViewById(id);
            boolean isSelected = (id == targetId);
            
            textView.setSelected(isSelected);
            textView.setBackground(ContextCompat.getDrawable(requireContext(),
                            isSelected ? R.drawable.unit_btn_selected_bg : R.drawable.unit_btn_bg));
            
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

            textView.setTypeface(isSelected ? ResourcesCompat.getFont(requireContext(), R.font.jost_bold) : ResourcesCompat.getFont(requireContext(), R.font.jost));
        }
        selectedUnitId = targetId;
    }
}
