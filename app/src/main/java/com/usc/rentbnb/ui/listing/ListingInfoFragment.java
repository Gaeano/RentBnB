package com.usc.rentbnb.ui.listing;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.models.IslandResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.viewmodels.AddListingViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class ListingInfoFragment extends Fragment {
    private AddListingViewModel viewModel;
    private Spinner spinnerIsland;
    private ProgressBar islandLoadingProgress;
    private EditText etProductName, etDescription, etAddress;
    private Button btnContinue;

    private List<Island> islandList = new ArrayList<>();
    private String selectedIslandName = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(super.getContext() != null ? view : view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AddListingViewModel.class);

        etProductName = view.findViewById(R.id.etProductName);
        etDescription = view.findViewById(R.id.etDescription);
        etAddress = view.findViewById(R.id.etAddress);
        btnContinue = view.findViewById(R.id.btnContinue);
        spinnerIsland = view.findViewById(R.id.spinnerIsland);
        islandLoadingProgress = view.findViewById(R.id.islandLoadingProgress);

        etProductName.setText(viewModel.productName);
        etDescription.setText(viewModel.description);
        etAddress.setText(viewModel.address);

        fetchIslands();

        setupListeners();
        updateContinueButtonState();

        btnContinue.setOnClickListener(v -> {
            viewModel.productName = etProductName.getText().toString();
            viewModel.description = etDescription.getText().toString();
            viewModel.address = etAddress.getText().toString();
            viewModel.island = selectedIslandName;

            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).goNextStep();
            }
        });
    }

    private void setupListeners() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContinueButtonState();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etProductName.addTextChangedListener(validationWatcher);
        etDescription.addTextChangedListener(validationWatcher);
        etAddress.addTextChangedListener(validationWatcher);

        spinnerIsland.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedIslandName = "";
                } else {
                    selectedIslandName = islandList.get(position - 1).getIslandName();
                }
                updateContinueButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedIslandName = "";
                updateContinueButtonState();
            }
        });
    }

    private void updateContinueButtonState() {
        if (btnContinue == null) return;

        String name = etProductName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // The IF statement checking all 4 required fields
        boolean isEnabled = !name.isEmpty() 
                && !description.isEmpty() 
                && !selectedIslandName.isEmpty() 
                && !address.isEmpty();

        btnContinue.setEnabled(isEnabled);
        btnContinue.setAlpha(isEnabled ? 1.0f : 0.5f);
    }

    private void fetchIslands() {
        islandLoadingProgress.setVisibility(View.VISIBLE);
        spinnerIsland.setVisibility(View.GONE);

        ApiClient.getApiService().getIslands().enqueue(new Callback<IslandResponse>() {
            @Override
            public void onResponse(Call<IslandResponse> call, Response<IslandResponse> response) {
                islandLoadingProgress.setVisibility(View.GONE);
                spinnerIsland.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    islandList = response.body().getData();
                    populateSpinner(islandList);
                } else {
                    Toast.makeText(requireContext(), "Failed to load islands", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<IslandResponse> call, Throwable t) {
                islandLoadingProgress.setVisibility(View.GONE);
                spinnerIsland.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Network error loading islands", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateSpinner(List<Island> islands) {
        List<String> displayNames = new ArrayList<>();
        displayNames.add("Select an Island");

        for (Island island : islands) {
            displayNames.add(island.getIslandName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                displayNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIsland.setAdapter(adapter);

        if (!viewModel.island.isEmpty()) {
            for (int i = 0; i < islands.size(); i++) {
                if (islands.get(i).getIslandName().equals(viewModel.island)) {
                    spinnerIsland.setSelection(i + 1);
                    selectedIslandName = viewModel.island;
                    break;
                }
            }
        }
        updateContinueButtonState();
    }
}