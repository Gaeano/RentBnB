package com.usc.rentbnb.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.viewmodels.AddListingViewModel;

public class ListingTypeFragment extends Fragment {
    private AddListingViewModel viewModel;

    private static final String[] CATEGORIES = {
            "Wheels", "Water", "Outdoors", "Electronics", "Beach Leisure"
    };

    private static final int[] ICONS = {
            R.drawable.ic_wheels,
            R.drawable.ic_water,
            R.drawable.ic_outdoors,
            R.drawable.ic_electronics,
            R.drawable.ic_beach_leisure
    };

    private int selectedIndex = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AddListingViewModel.class);

        LinearLayout listContainer = view.findViewById(R.id.categoryList);
        buildCategoryList(listContainer);

        view.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            if (selectedIndex == -1) {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.currentDraft().category = CATEGORIES[selectedIndex];

            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).goNextStep();
            }
        });
    }

    private void buildCategoryList(LinearLayout container) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            final int index = i;
            View row = getLayoutInflater().inflate(
                    R.layout.item_category_row, container, false);

            ((android.widget.ImageView) row.findViewById(R.id.ivCategoryIcon))
                    .setImageResource(ICONS[i]);
            ((TextView) row.findViewById(R.id.tvCategoryName))
                    .setText(CATEGORIES[i]);

            row.setOnClickListener(v -> selectCategory(container, index));

            // spacing
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0,
                    (int) (10 * getResources().getDisplayMetrics().density));
            row.setLayoutParams(lp);

            container.addView(row);
        }
    }

    private void selectCategory(LinearLayout container, int index) {
        selectedIndex = index;
        for (int i = 0; i < container.getChildCount(); i++) {
            View row = container.getChildAt(i);
            row.setBackground(ContextCompat.getDrawable(
                    requireContext(),
                    i == index
                            ? R.drawable.category_row_selected_bg
                            : R.drawable.category_row_bg));
            TextView tv = row.findViewById(R.id.tvCategoryName);
            tv.setTextColor(ContextCompat.getColor(requireContext(),
                    i == index ? R.color.black : R.color.text_grey));
            ImageView iv = row.findViewById(R.id.ivCategoryIcon);
            iv.setColorFilter(ContextCompat.getColor(requireContext(),
                    i == index ? R.color.black : R.color.text_grey));
        }
    }
}