package com.usc.rentbnb.ui.listing;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.viewmodels.AddListingViewModel;

import java.util.ArrayList;
import java.util.List;

public class ListingPricingFragment extends Fragment {
    private AddListingViewModel viewModel;
    private String selectedUnit = "hourly";
    public List<String> paymentMethods = new ArrayList<>();

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
        viewModel = new ViewModelProvider(requireActivity()).get(AddListingViewModel.class);

        EditText etPrice = view.findViewById(R.id.etBasePrice);
        etPrice.setText(viewModel.price > 0 ? String.valueOf(viewModel.price) : "");

        View btnHourly  = view.findViewById(R.id.btnHourly);
        View btnDaily   = view.findViewById(R.id.btnDaily);
        View btnWeekly  = view.findViewById(R.id.btnWeekly);
        View btnMonthly = view.findViewById(R.id.btnMonthly);

        selectUnit(view, R.id.btnHourly);

        btnHourly.setOnClickListener(v -> selectUnit(view, R.id.btnHourly));
        btnDaily.setOnClickListener(v -> selectUnit(view, R.id.btnDaily));
        btnWeekly.setOnClickListener(v -> selectUnit(view, R.id.btnWeekly));
        btnMonthly.setOnClickListener(v-> selectUnit(view, R.id.btnMonthly));

        view.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String priceStr = etPrice.getText().toString();
            if (priceStr.isEmpty()) {
                etPrice.setError("Price is required!");
                return;
            }
            viewModel.price = Double.parseDouble(priceStr);
            viewModel.priceUnit = selectedUnit;

            viewModel.paymentMethods.clear();
            if (((CheckBox) view.findViewById(R.id.cbCash)).isChecked()) {
                viewModel.paymentMethods.add("Cash");
            }
            if (((CheckBox) view.findViewById(R.id.cbGcash)).isChecked()) {
                viewModel.paymentMethods.add("GCash");
            }
            if (((CheckBox) view.findViewById(R.id.cbPaypal)).isChecked()) {
                viewModel.paymentMethods.add("PayPal");
            }

            if (viewModel.paymentMethods.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one payment method!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).goNextStep();
            }
        });
    }

    private int selectedUnitId = R.id.btnDaily;

    private void selectUnit(View root, int targetId) {
        int[] ids = {R.id.btnHourly, R.id.btnDaily, R.id.btnWeekly, R.id.btnMonthly };

        for (int id : ids) {
            TextView textView = root.findViewById(id);
            boolean isSelected = (id == targetId);
            
            textView.setSelected(isSelected);
            textView.setBackground(ContextCompat.getDrawable(requireContext(),
                            isSelected ? R.drawable.unit_btn_selected_bg : R.drawable.unit_btn_bg));
            
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            textView.setTypeface(isSelected ? ResourcesCompat.getFont(requireContext(), R.font.jost_bold) : ResourcesCompat.getFont(requireContext(), R.font.jost));

            if (isSelected) {
                if (id == R.id.btnHourly) {
                    selectedUnit = "hourly";
                } else if (id == R.id.btnDaily) {
                    selectedUnit = "daily";
                } else if (id == R.id.btnWeekly) {
                    selectedUnit = "weekly";
                } else if (id == R.id.btnMonthly) {
                    selectedUnit = "monthly";
                }
            }
        }
    }
}
