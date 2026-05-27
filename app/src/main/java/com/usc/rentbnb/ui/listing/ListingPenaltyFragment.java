package com.usc.rentbnb.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.viewmodels.AddListingViewModel;
import com.usc.rentbnb.viewmodels.ListingDraft;

public class ListingPenaltyFragment extends Fragment {
    private AddListingViewModel viewModel;
    private ListingDraft draft;
    private String selectedUnit = "hourly";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing_penalty, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AddListingViewModel.class);
        draft = viewModel.currentDraft();

        EditText etPenalty = view.findViewById(R.id.etPenaltyPrice);
        etPenalty.setText(draft.penaltyAmount > 0 ? String.valueOf(draft.penaltyAmount) : "");

        View btnHourly  = view.findViewById(R.id.btnHourlyPenalty);
        View btnDaily   = view.findViewById(R.id.btnDailyPenalty);
        View btnWeekly  = view.findViewById(R.id.btnWeeklyPenalty);
        View btnMonthly = view.findViewById(R.id.btnMonthlyPenalty);

        // Initialize selection based on draft
        if (draft.penaltyUnit != null) {
            selectedUnit = draft.penaltyUnit;
        }
        
        int initialTargetId = R.id.btnHourlyPenalty;
        if ("daily".equals(selectedUnit)) initialTargetId = R.id.btnDailyPenalty;
        else if ("weekly".equals(selectedUnit)) initialTargetId = R.id.btnWeeklyPenalty;
        else if ("monthly".equals(selectedUnit)) initialTargetId = R.id.btnMonthlyPenalty;
        
        selectUnit(view, initialTargetId);

        btnHourly.setOnClickListener(v -> selectUnit(view, R.id.btnHourlyPenalty));
        btnDaily.setOnClickListener(v -> selectUnit(view, R.id.btnDailyPenalty));
        btnWeekly.setOnClickListener(v -> selectUnit(view, R.id.btnWeeklyPenalty));
        btnMonthly.setOnClickListener(v-> selectUnit(view, R.id.btnMonthlyPenalty));

        view.findViewById(R.id.btnContinuePenalty).setOnClickListener(v -> {
            String penaltyStr = etPenalty.getText().toString();
            if (penaltyStr.isEmpty()) {
                etPenalty.setError("Penalty fee is required!");
                return;
            }
            draft.penaltyAmount = Double.parseDouble(penaltyStr);
            draft.penaltyUnit = selectedUnit;

            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).goNextStep();
            }
        });
    }

    private void selectUnit(View root, int targetId) {
        int[] ids = {R.id.btnHourlyPenalty, R.id.btnDailyPenalty, R.id.btnWeeklyPenalty, R.id.btnMonthlyPenalty };

        for (int id : ids) {
            TextView textView = root.findViewById(id);
            if (textView == null) continue;
            
            boolean isSelected = (id == targetId);
            
            textView.setSelected(isSelected);
            textView.setBackground(ContextCompat.getDrawable(requireContext(),
                            isSelected ? R.drawable.unit_btn_selected_bg : R.drawable.unit_btn_bg));
            
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            textView.setTypeface(isSelected ? ResourcesCompat.getFont(requireContext(), R.font.jost_bold) : ResourcesCompat.getFont(requireContext(), R.font.jost));

            if (isSelected) {
                if (id == R.id.btnHourlyPenalty) selectedUnit = "Hourly";
                else if (id == R.id.btnDailyPenalty) selectedUnit = "Daily";
                else if (id == R.id.btnWeeklyPenalty) selectedUnit = "Weekly";
                else if (id == R.id.btnMonthlyPenalty) selectedUnit = "Monthly";
            }
        }
    }
}
