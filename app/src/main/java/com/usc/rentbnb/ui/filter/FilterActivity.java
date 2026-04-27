package com.usc.rentbnb.ui.filter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.FilterCriteria;

import java.util.Arrays;
import java.util.List;

public class FilterActivity extends AppCompatActivity {

    private FilterCriteria criteria;

    private ChipGroup cgCategory, cgActivities, cgRating;
    private RangeSlider sliderPrice;
    private TextInputEditText etMinPrice, etMaxPrice;

    private final List<String> categoriesList = Arrays.asList("Wheels", "Water", "Outdoors", "Electronics", "Beach Leisure");

    private final List<String> allActivities = Arrays.asList(
            "Island Hopping tours", "Sunset / Sunrise cruising", "Dolphin or whale watching", "Sandbar picnics and lounging", "Deep-sea fishing", "Private cove exploration",
            "Mangrove forest touring", "Hidden lagoon exploring", "Coastal sightseeing", "Wave surfing / Kite surfing", "Wakeboarding",
            "Coral reef snorkeling", "Freediving / Scuba diving", "Sea turtle swimming", "Underwater photography / videography", "Shipwreck exploring",
            "Coastal road tripping", "Mountain viewpoint chasing", "Waterfall trekking & hiking", "Local food and culture tours", "Off-road trail riding", "Beach-hopping the coastline",
            "Overnight beach camping", "Beachfront BBQs and grilling", "Stargazing", "Bonfire gatherings", "Sunset beach lounging"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        if (getIntent() != null && getIntent().hasExtra("current_criteria")) {
            criteria = (FilterCriteria) getIntent().getSerializableExtra("current_criteria");
        } else {
            criteria = new FilterCriteria();
        }

        cgCategory = findViewById(R.id.cg_category);
        cgActivities = findViewById(R.id.cg_activities);
        cgRating = findViewById(R.id.cg_rating);
        sliderPrice = findViewById(R.id.slider_price);
        etMinPrice = findViewById(R.id.et_min_price);
        etMaxPrice = findViewById(R.id.et_max_price);

        setupChips();
        setupPriceSyncing();
        restorePreviousState();

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        findViewById(R.id.btn_clear_all).setOnClickListener(v -> {
            criteria = new FilterCriteria();
            restorePreviousState();
        });

        findViewById(R.id.btn_show_results).setOnClickListener(v -> {
            syncEditTextsToSlider();
            gatherData();

            Intent returnIntent = new Intent();
            returnIntent.putExtra("updated_criteria", criteria);
            setResult(RESULT_OK, returnIntent);
            finish();
        });
    }

    private void setupPriceSyncing() {
        sliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                List<Float> values = slider.getValues();
                etMinPrice.setText(String.valueOf(Math.round(values.get(0))));
                etMaxPrice.setText(String.valueOf(Math.round(values.get(1))));
            }
        });

        View.OnFocusChangeListener focusChangeListener = (v, hasFocus) -> {
            if (!hasFocus) syncEditTextsToSlider();
        };

        etMinPrice.setOnFocusChangeListener(focusChangeListener);
        etMaxPrice.setOnFocusChangeListener(focusChangeListener);

        etMaxPrice.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                syncEditTextsToSlider();
                etMaxPrice.clearFocus();
            }
            return false;
        });
    }

    private void syncEditTextsToSlider() {
        try {
            float min = Float.parseFloat(etMinPrice.getText().toString());
            float max = Float.parseFloat(etMaxPrice.getText().toString());

            if (min < sliderPrice.getValueFrom()) min = sliderPrice.getValueFrom();
            if (max > sliderPrice.getValueTo()) max = sliderPrice.getValueTo();
            if (min > max) min = max;

            sliderPrice.setValues(min, max);

            etMinPrice.setText(String.valueOf(Math.round(min)));
            etMaxPrice.setText(String.valueOf(Math.round(max)));
        } catch (NumberFormatException ignored) {
            //
        }
    }

    private void setupChips() {
        for (String cat : categoriesList) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_chip_filter, cgCategory, false);
            chip.setText(cat);
            cgCategory.addView(chip);
        }

        for (String act : allActivities) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_chip_filter, cgActivities, false);
            chip.setText(act);
            cgActivities.addView(chip);
        }
    }

    private void restorePreviousState() {
        sliderPrice.setValues(criteria.minPrice, criteria.maxPrice);
        etMinPrice.setText(String.valueOf(Math.round(criteria.minPrice)));
        etMaxPrice.setText(String.valueOf(Math.round(criteria.maxPrice)));

        for (int i = 0; i < cgCategory.getChildCount(); i++) {
            Chip chip = (Chip) cgCategory.getChildAt(i);
            chip.setChecked(criteria.categories.contains(chip.getText().toString()));
        }

        for (int i = 0; i < cgActivities.getChildCount(); i++) {
            Chip chip = (Chip) cgActivities.getChildAt(i);
            chip.setChecked(criteria.activities.contains(chip.getText().toString()));
        }

        if (criteria.minRating == 4f) cgRating.check(R.id.chip_rating_4);
        else if (criteria.minRating == 3f) cgRating.check(R.id.chip_rating_3);
        else if (criteria.minRating == 2f) cgRating.check(R.id.chip_rating_2);
        else if (criteria.minRating == 1f) cgRating.check(R.id.chip_rating_1);
        else cgRating.clearCheck();
    }

    private void gatherData() {
        criteria.categories.clear();
        for (int id : cgCategory.getCheckedChipIds()) {
            Chip chip = cgCategory.findViewById(id);
            criteria.categories.add(chip.getText().toString());
        }

        criteria.activities.clear();
        for (int id : cgActivities.getCheckedChipIds()) {
            Chip chip = cgActivities.findViewById(id);
            criteria.activities.add(chip.getText().toString());
        }

        List<Float> prices = sliderPrice.getValues();
        criteria.minPrice = prices.get(0);
        criteria.maxPrice = prices.get(1);

        int checkedRatingId = cgRating.getCheckedChipId();
        if (checkedRatingId == R.id.chip_rating_4) criteria.minRating = 4f;
        else if (checkedRatingId == R.id.chip_rating_3) criteria.minRating = 3f;
        else if (checkedRatingId == R.id.chip_rating_2) criteria.minRating = 2f;
        else if (checkedRatingId == R.id.chip_rating_1) criteria.minRating = 1f;
        else criteria.minRating = 0f;
    }
}