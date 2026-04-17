package com.usc.rentbnb.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.usc.rentbnb.R;

/**
 * Step 6 / 6 — Review & Submit
 * Shows a read-only summary of all entered data before posting.
 * "List Product" button triggers success screen.
 */
public class ListingSummaryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // "List Product" goes to success screen (which is also step 7 internally)
        view.findViewById(R.id.btnListProduct).setOnClickListener(v -> {
            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).goNextStep();
            }
        });
    }
}