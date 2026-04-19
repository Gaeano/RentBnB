package com.usc.rentbnb.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.viewmodels.AddListingViewModel;

public class ListingInfoFragment extends Fragment {
    private AddListingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AddListingViewModel.class);

        EditText etProductName = view.findViewById(R.id.etProductName);
        EditText etDescription = view.findViewById(R.id.etDescription);
        EditText etIsland = view.findViewById(R.id.etIsland);

        etProductName.setText(viewModel.productName);
        etDescription.setText(viewModel.description);
        etIsland.setText(viewModel.island);

        view.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String title = etProductName.getText().toString();
            String description = etDescription.getText().toString();
            String island = etIsland.getText().toString();

            if (title.isEmpty()) {
                etProductName.setError("Product name is required!");
                return;
            }

            if (description.isEmpty()) {
                etDescription.setError("Description is required!");
                return;
            }

            if (island.isEmpty()) {
                etIsland.setError("Island is required!");
                return;
            }

            viewModel.productName = title;
            viewModel.description = description;
            viewModel.island = island;

            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).goNextStep();
            }
        });
    }
}