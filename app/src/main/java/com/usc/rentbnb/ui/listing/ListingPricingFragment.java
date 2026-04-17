package com.usc.rentbnb.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.usc.rentbnb.R;

/**
 * Step 3 / 6 — Pricing & Payment Methods
 *
 * Fields map to Firestore listing document:
 *   pricePerDay  → base price (reused for all units; server normalises)
 *   priceUnit    → "hourly" | "daily" | "weekly"   (new field, custom)
 *   paymentMethods → String[] e.g. ["cash","gcash","paypal"]
 */
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

        // Unit toggle: Hourly / Daily / Weekly
        View btnHourly  = view.findViewById(R.id.btnHourly);
        View btnDaily   = view.findViewById(R.id.btnDaily);
        View btnWeekly  = view.findViewById(R.id.btnWeekly);

        // Default: Daily selected
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
        int[] ids = { R.id.btnHourly, R.id.btnDaily, R.id.btnWeekly };
        for (int id : ids) {
            root.findViewById(id).setSelected(id == targetId);
            root.findViewById(id).setBackground(
                    androidx.core.content.ContextCompat.getDrawable(requireContext(),
                            id == targetId
                                    ? R.drawable.unit_btn_selected_bg
                                    : R.drawable.unit_btn_bg));
            ((android.widget.TextView) root.findViewById(id)).setTextColor(
                    androidx.core.content.ContextCompat.getColor(requireContext(),
                            id == targetId ? R.color.black : R.color.white));
        }
        selectedUnitId = targetId;
    }
}