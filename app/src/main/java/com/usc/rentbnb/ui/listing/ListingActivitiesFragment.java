package com.usc.rentbnb.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.flexbox.FlexboxLayout;
import com.usc.rentbnb.R;

import java.util.ArrayList;
import java.util.List;

public class ListingActivitiesFragment extends Fragment {

    private static final String[] ACTIVITIES = {
            "Swimming", "Snorkeling", "Scuba Diving",
            "Canyoneering", "Cliff Jumping", "Island Hopping",
            "Fishing", "Surfing", "Kayaking",
            "Paddleboarding", "Whale Watching", "Biking",
            "Hiking", "Running", "Spelunking",
            "Camping", "Photography", "Bird Watching",
            "Horseback Riding", "ATV / Off-road"
    };

    private final List<Integer> selectedActivities = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing_activities, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FlexboxLayout chipContainer = view.findViewById(R.id.chipContainer);
        buildActivityChips(chipContainer);

        view.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).goNextStep();
            }
        });
    }

    private void buildActivityChips(FlexboxLayout container) {
        int dpMargin = (int) (8 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < ACTIVITIES.length; i++) {
            final int idx = i;
            TextView chip = (TextView) getLayoutInflater()
                    .inflate(R.layout.item_activity_chip, container, false);
            chip.setText(ACTIVITIES[i]);

            com.google.android.flexbox.FlexboxLayout.LayoutParams lp =
                    new com.google.android.flexbox.FlexboxLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, dpMargin, dpMargin);
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> toggleChip(chip, idx));
            container.addView(chip);
        }
    }

    private void toggleChip(TextView chip, int idx) {
        if (selectedActivities.contains(idx)) {
            selectedActivities.remove(Integer.valueOf(idx));
            chip.setBackground(ContextCompat.getDrawable(requireContext(),
                    R.drawable.chip_background));
            chip.setTextColor(ContextCompat.getColor(requireContext(),
                    R.color.black));
        } else {
            selectedActivities.add(idx);
            chip.setBackground(ContextCompat.getDrawable(requireContext(),
                    R.drawable.chip_background_selected));
            chip.setTextColor(ContextCompat.getColor(requireContext(),
                    R.color.black));
        }
    }
}