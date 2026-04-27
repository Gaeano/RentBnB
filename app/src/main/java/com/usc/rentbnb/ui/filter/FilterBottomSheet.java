package com.usc.rentbnb.ui.filter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.FilterCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "FilterBottomSheet";

    // ── Argument key for pre-populating the sheet with existing criteria ──
    private static final String ARG_CRITERIA = "arg_filter_criteria";

    // ── Price defaults ────────────────────────────────────────────────────
    private static final float PRICE_MIN_DEFAULT = 0f;
    private static final float PRICE_MAX_DEFAULT = 10_000f;

    // ── Callback interface ────────────────────────────────────────────────

    /**
     * Implement this in HomeActivity (or any host) to receive filter results.
     */
    public interface FilterListener {
        void onFiltersApplied(FilterCriteria criteria);
    }

    private FilterListener filterListener;

    // ── State ─────────────────────────────────────────────────────────────
    private float currentMin = PRICE_MIN_DEFAULT;
    private float currentMax = PRICE_MAX_DEFAULT;

    // ── Views ─────────────────────────────────────────────────────────────
    private RangeSlider  priceSlider;
    private TextView     tvPriceMin;
    private TextView     tvPriceMax;
    private ChipGroup    chipGroupCategory;
    private ChipGroup    chipGroupPriceUnit;
    private ChipGroup    chipGroupSortBy;
    private TextView     btnClearAll;

    // ── Factory ───────────────────────────────────────────────────────────

    /**
     * Create a new instance.
     * Pass null for {@code existing} when opening with no prior filters set.
     */
    public static FilterBottomSheet newInstance(@Nullable FilterCriteria existing) {
        FilterBottomSheet sheet = new FilterBottomSheet();
        // We store criteria as individual primitives because FilterCriteria
        // is not Parcelable; simple args bundle is sufficient here.
        if (existing != null) {
            Bundle args = new Bundle();
            args.putFloat("minPrice",   existing.minPrice);
            args.putFloat("maxPrice",   existing.maxPrice);
            args.putString("priceUnit", existing.priceUnit);
            args.putString("sortBy",    existing.sortBy);
            if (existing.categories != null) {
                args.putStringArrayList("categories",
                        new ArrayList<>(existing.categories));
            }
            sheet.setArguments(args);
        }
        return sheet;
    }

    // ── Listener setter ───────────────────────────────────────────────────

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── Bind views ────────────────────────────────────────────
        priceSlider       = view.findViewById(R.id.priceRangeSlider);
        tvPriceMin        = view.findViewById(R.id.tvPriceMin);
        tvPriceMax        = view.findViewById(R.id.tvPriceMax);
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        chipGroupPriceUnit= view.findViewById(R.id.chipGroupPriceUnit);
        chipGroupSortBy   = view.findViewById(R.id.chipGroupSortBy);
        btnClearAll       = view.findViewById(R.id.btnClearAll);

        // Must set initial values in code — app:values removed from XML to
        // avoid typed-array resolution crash on MaterialComponents theme
        priceSlider.setValues(PRICE_MIN_DEFAULT, PRICE_MAX_DEFAULT);

        // ── Pre-populate from arguments (if re-opening with saved state) ─
        restoreState();

        // ── Slider listener ───────────────────────────────────────
        priceSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            currentMin = values.get(0);
            currentMax = values.get(1);
            updatePriceLabels();
        });

        // ── Clear all ─────────────────────────────────────────────
        btnClearAll.setOnClickListener(v -> clearAll());

        // ── Close ─────────────────────────────────────────────────
        view.findViewById(R.id.btnCloseFilter).setOnClickListener(v -> dismiss());

        // ── Apply ─────────────────────────────────────────────────
        view.findViewById(R.id.btnApplyFilters).setOnClickListener(v -> applyFilters());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Read args bundle and pre-select chips / slider to match existing criteria.
     */
    private void restoreState() {
        Bundle args = getArguments();
        if (args == null) {
            updatePriceLabels();
            return;
        }

        currentMin = args.getFloat("minPrice", PRICE_MIN_DEFAULT);
        currentMax = args.getFloat("maxPrice", PRICE_MAX_DEFAULT);
        priceSlider.setValues(currentMin, currentMax);
        updatePriceLabels();

        // Re-check category chips
        List<String> savedCats = args.getStringArrayList("categories");
        if (savedCats != null && !savedCats.isEmpty()) {
            for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupCategory.getChildAt(i);
                chip.setChecked(savedCats.contains(chip.getText().toString()));
            }
        }

        // Re-select price unit chip
        String savedUnit = args.getString("priceUnit");
        if (savedUnit != null) {
            for (int i = 0; i < chipGroupPriceUnit.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupPriceUnit.getChildAt(i);
                if (chip.getText().toString().equals(savedUnit)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }

        // Re-select sort chip
        String savedSort = args.getString("sortBy");
        if (savedSort != null) {
            setCheckedSortChip(savedSort);
        }
    }

    private void updatePriceLabels() {
        tvPriceMin.setText(formatPrice(currentMin));
        tvPriceMax.setText(formatPrice(currentMax));
    }

    private String formatPrice(float value) {
        if (value >= 1000) {
            return String.format(Locale.getDefault(), "%,.0f", value);
        }
        return String.valueOf((int) value);
    }

    /**
     * Resets every control to its default state.
     */
    private void clearAll() {
        // Slider
        currentMin = PRICE_MIN_DEFAULT;
        currentMax = PRICE_MAX_DEFAULT;
        priceSlider.setValues(PRICE_MIN_DEFAULT, PRICE_MAX_DEFAULT);
        updatePriceLabels();

        // Chips — deselect all
        uncheckAllChips(chipGroupCategory);
        uncheckAllChips(chipGroupPriceUnit);
        uncheckAllChips(chipGroupSortBy);
    }

    private void uncheckAllChips(ChipGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            ((Chip) group.getChildAt(i)).setChecked(false);
        }
    }

    /**
     * Collects current UI state, builds a FilterCriteria, and fires the callback.
     */
    private void applyFilters() {
        // Categories (multi-select)
        List<String> selectedCategories = new ArrayList<>();
        for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategory.getChildAt(i);
            if (chip.isChecked()) {
                selectedCategories.add(chip.getText().toString());
            }
        }

        // Price unit (single-select)
        String selectedUnit = null;
        int checkedUnitId = chipGroupPriceUnit.getCheckedChipId();
        if (checkedUnitId != View.NO_ID) {
            Chip chip = chipGroupPriceUnit.findViewById(checkedUnitId);
            if (chip != null) selectedUnit = chip.getText().toString();
        }

        // Sort (single-select → convert label to key)
        String selectedSort = null;
        int checkedSortId = chipGroupSortBy.getCheckedChipId();
        if (checkedSortId != View.NO_ID) {
            selectedSort = sortChipIdToKey(checkedSortId);
        }

        FilterCriteria criteria = new FilterCriteria(
                currentMin,
                currentMax,
                selectedCategories,
                selectedUnit,
                selectedSort
        );

        if (filterListener != null) {
            filterListener.onFiltersApplied(criteria);
        }

        dismiss();
    }

    /**
     * Maps a sort ChipGroup child ID to a stable string key used by HomeViewModel.
     */
    private String sortChipIdToKey(int chipId) {
        if (chipId == R.id.chipSortMostRented) return "most_rented";
        if (chipId == R.id.chipSortNewest)     return "newest";
        if (chipId == R.id.chipSortPriceLow)   return "price_asc";
        if (chipId == R.id.chipSortPriceHigh)  return "price_desc";
        return null;
    }

    /**
     * Selects the sort chip that matches the given key (used during restore).
     */
    private void setCheckedSortChip(String key) {
        int targetId;
        switch (key) {
            case "most_rented": targetId = R.id.chipSortMostRented; break;
            case "newest":      targetId = R.id.chipSortNewest;     break;
            case "price_asc":   targetId = R.id.chipSortPriceLow;   break;
            case "price_desc":  targetId = R.id.chipSortPriceHigh;  break;
            default: return;
        }
        Chip chip = chipGroupSortBy.findViewById(targetId);
        if (chip != null) chip.setChecked(true);
    }
}