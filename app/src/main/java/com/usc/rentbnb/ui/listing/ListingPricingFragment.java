package com.usc.rentbnb.ui.listing;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

import java.util.ArrayList;
import java.util.List;

public class ListingPricingFragment extends Fragment {
    private AddListingViewModel viewModel;
    private String selectedUnit = "hourly";
    public List<String> paymentMethods = new ArrayList<>();
    private Button btnContinue;
    private EditText etBasePrice;
    private CheckBox cbCash, cbGcash, cbPaypal;
    ListingDraft draft;

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

        etBasePrice = view.findViewById(R.id.etBasePrice);
        btnContinue = view.findViewById(R.id.btnContinue);
        cbCash = view.findViewById(R.id.cbCash);
        cbGcash = view.findViewById(R.id.cbGcash);
        cbPaypal = view.findViewById(R.id.cbPaypal);
        draft = viewModel.currentDraft();

        // Restore price if it exists
        if (draft.price > 0) { etBasePrice.setText(String.valueOf(draft.price)); }

        setupUnitButtons(view);
        setupValidationListeners();
        
        // Initial check
        updateContinueButtonState();

        btnContinue.setOnClickListener(v -> {
            String priceStr = etBasePrice.getText().toString();
            if (!priceStr.isEmpty()) {
                draft.price = Double.parseDouble(priceStr);
            }
            draft.priceUnit = selectedUnit;

            draft.paymentMethods.clear();
            if (cbCash.isChecked()) draft.paymentMethods.add("Cash");
            if (cbGcash.isChecked()) draft.paymentMethods.add("GCash");
            if (cbPaypal.isChecked()) draft.paymentMethods.add("PayPal");

            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).goNextStep();
            }
        });
    }

    private void setupValidationListeners() {
        // Watch price changes
        etBasePrice.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContinueButtonState();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Watch checkbox changes
        CompoundButton.OnCheckedChangeListener checkListener = (buttonView, isChecked) -> updateContinueButtonState();
        cbCash.setOnCheckedChangeListener(checkListener);
        cbGcash.setOnCheckedChangeListener(checkListener);
        cbPaypal.setOnCheckedChangeListener(checkListener);
    }

    private void updateContinueButtonState() {
        if (btnContinue == null) return;

        boolean hasPrice = !etBasePrice.getText().toString().trim().isEmpty();
        boolean hasPaymentMethod = cbCash.isChecked() || cbGcash.isChecked() || cbPaypal.isChecked();

        boolean isEnabled = hasPrice && hasPaymentMethod;
        btnContinue.setEnabled(isEnabled);
        btnContinue.setAlpha(isEnabled ? 1.0f : 0.5f);
    }

    private void setupUnitButtons(View view) {
        View btnHourly = view.findViewById(R.id.btnHourly);
        View btnDaily = view.findViewById(R.id.btnDaily);
        View btnWeekly = view.findViewById(R.id.btnWeekly);
        View btnMonthly = view.findViewById(R.id.btnMonthly);

        selectUnit(view, R.id.btnHourly);

        btnHourly.setOnClickListener(v -> selectUnit(view, R.id.btnHourly));
        btnDaily.setOnClickListener(v -> selectUnit(view, R.id.btnDaily));
        btnWeekly.setOnClickListener(v -> selectUnit(view, R.id.btnWeekly));
        btnMonthly.setOnClickListener(v -> selectUnit(view, R.id.btnMonthly));
    }

    private void selectUnit(View root, int targetId) {
        int[] ids = {R.id.btnHourly, R.id.btnDaily, R.id.btnWeekly, R.id.btnMonthly};
        for (int id : ids) {
            TextView textView = root.findViewById(id);
            boolean isSelected = (id == targetId);
            textView.setSelected(isSelected);
            textView.setBackgroundResource(isSelected ? R.drawable.unit_btn_selected_bg : R.drawable.unit_btn_bg);
            textView.setTypeface(ResourcesCompat.getFont(requireContext(), isSelected ? R.font.jost_bold : R.font.jost));

            if (isSelected) {
                if (id == R.id.btnHourly) selectedUnit = "hourly";
                else if (id == R.id.btnDaily) selectedUnit = "daily";
                else if (id == R.id.btnWeekly) selectedUnit = "weekly";
                else if (id == R.id.btnMonthly) selectedUnit = "monthly";
            }
        }
    }
}