package com.usc.rentbnb.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.usc.rentbnb.R;

public class ListingSuccessFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int count = 1;
        if (getArguments() != null) {
            count = getArguments().getInt("listing_count", 1);
        }

        TextView tvSuccessMessage = view.findViewById(R.id.tvSuccessMessage);
        if (tvSuccessMessage != null) {
            tvSuccessMessage.setText(count == 1 ? "Your listing is now live!" : count + " listings are now live!");
        }

        view.findViewById(R.id.btnViewListing).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().finish();
        });
    }
}
