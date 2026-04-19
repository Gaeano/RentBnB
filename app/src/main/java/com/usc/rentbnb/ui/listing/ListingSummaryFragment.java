package com.usc.rentbnb.ui.listing;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.CreateListingRequest;
import com.usc.rentbnb.models.CreateListingResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.viewmodels.AddListingViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListingSummaryFragment extends Fragment {
    private AddListingViewModel viewModel;
    
    private View btnContainer;
    private TextView tvBtnText;
    private ProgressBar progressBar;

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
        viewModel = new ViewModelProvider(requireActivity()).get(AddListingViewModel.class);

        populateSummaryData(view);
        
        btnContainer = view.findViewById(R.id.btnListProductContainer);
        tvBtnText = view.findViewById(R.id.tvBtnText);
        progressBar = view.findViewById(R.id.progressBar);

        if (btnContainer != null) {
            btnContainer.setOnClickListener(v -> {
                btnContainer.setEnabled(false);
                btnContainer.setAlpha(0.8f);

                if (tvBtnText != null) tvBtnText.setVisibility(View.GONE);
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                
                submitListing();
            });
        }
    }

    private void populateSummaryData(View view) {
        setTextForIncludedRow(view, R.id.rowName, viewModel.productName);
        setTextForIncludedRow(view, R.id.rowDesc, viewModel.description);
        setTextForIncludedRow(view, R.id.rowLocation, viewModel.island);

        TextView tvCategory = view.findViewById(R.id.tvSummaryCategory);
        tvCategory.setText(viewModel.category.isEmpty() ? "— none selected —" : viewModel.category);

        TextView tvPrice = view.findViewById(R.id.tvSummaryPrice);
        tvPrice.setText(String.format("₱%.2f / %s", viewModel.price, viewModel.priceUnit));

        TextView tvPayments = view.findViewById(R.id.tvSummaryPayments);
        if(viewModel.paymentMethods.isEmpty()) {
            tvPayments.setText("— none selected —");
        } else {
            tvPayments.setText(TextUtils.join(", ", viewModel.paymentMethods));
        }

        TextView tvActivities = view.findViewById(R.id.tvSummaryActivities);
        if(viewModel.suggestedActivities.isEmpty()) {
            tvActivities.setText("— none selected —");
        } else {
            tvActivities.setText(TextUtils.join(", ", viewModel.suggestedActivities));
        }

        TextView tvPhotos = view.findViewById(R.id.tvSummaryPhotos);
        tvPhotos.setText(viewModel.imageUris.size() + " photos attached");

        ImageView ivCover = view.findViewById(R.id.ivSummaryCover);
        if (!viewModel.imageUris.isEmpty()) {
            ivCover.setVisibility(View.VISIBLE);
            ivCover.setImageURI(Uri.parse(viewModel.imageUris.get(0)));
        }
    }

    private void setTextForIncludedRow(View parentView, int includeId, String value) {
        // TODO: Change "Label" to respective labels
        View row = parentView.findViewById(includeId);
        if (row != null) {
            int innerTextViewId = getResources().getIdentifier("tvValue", "id", requireContext().getPackageName());
            TextView tvValue = row.findViewById(innerTextViewId);

            if (tvValue != null) {
                tvValue.setText(value != null && !value.isEmpty() ? value : "— missing —");
            }
        }
    }

    private void submitListing() {
        CreateListingRequest createListingRequest = new CreateListingRequest(
                viewModel.productName,
                viewModel.description,
                viewModel.category,
                viewModel.island,
                viewModel.price,
                viewModel.priceUnit,
                viewModel.paymentMethods,
                viewModel.suggestedActivities,
                viewModel.imageUris
        );

        ApiClient.getApiService().createListing(createListingRequest).enqueue(new Callback<CreateListingResponse>() {
            @Override
            public void onResponse(Call<CreateListingResponse> call, Response<CreateListingResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (tvBtnText != null) tvBtnText.setVisibility(View.VISIBLE);
                if (btnContainer != null) {
                    btnContainer.setEnabled(true);
                    btnContainer.setAlpha(1.0f);
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (getActivity() instanceof AddListingActivity) {
                        ((AddListingActivity) getActivity()).goNextStep();
                    }
                } else {
                    String errorMessage = "Unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMessage = e.getMessage();
                    }

                    Log.e("AddListing", "HTTP " + response.code() + ": " + errorMessage);
                    Toast.makeText(requireContext(), "Error " + response.code() + ": " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreateListingResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (tvBtnText != null) tvBtnText.setVisibility(View.VISIBLE);
                if (btnContainer != null) {
                    btnContainer.setEnabled(true);
                    btnContainer.setAlpha(1.0f);
                }

                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
