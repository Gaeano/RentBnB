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

import com.google.gson.JsonObject;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.CreateListingRequest;
import com.usc.rentbnb.models.CreateListingResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.viewmodels.AddListingViewModel;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
                
                ///submitListing();

                uploadImagesToCloudinary();
            });
        }
    }

    private void populateSummaryData(View view) {
        setTextForIncludedRow(view, R.id.rowName, "Title", viewModel.productName);
        setTextForIncludedRow(view, R.id.rowDesc, "Description", viewModel.description);
        setTextForIncludedRow(view, R.id.rowLocation, "Island", viewModel.island);

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
        TextView tvCoverPlaceholder = view.findViewById(R.id.tvSummaryCoverPlaceholder);
        if (!viewModel.imageUris.isEmpty()) {
            ivCover.setVisibility(View.VISIBLE);
            ivCover.setImageURI(Uri.parse(viewModel.imageUris.get(0)));
            tvCoverPlaceholder.setVisibility(View.GONE);
        } else {
            ivCover.setVisibility(View.GONE);
            tvCoverPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void setTextForIncludedRow(View parentView, int includeId, String label, String value) {
        View row = parentView.findViewById(includeId);
        if (row != null) {
            int labelTextViewId = getResources().getIdentifier("tvLabel", "id", requireContext().getPackageName());
            TextView tvLabel = row.findViewById(labelTextViewId);
            if(tvLabel != null) {
                tvLabel.setText(label);
            }

            int innerTextViewId = getResources().getIdentifier("tvValue", "id", requireContext().getPackageName());
            TextView tvValue = row.findViewById(innerTextViewId);

            if (tvValue != null) {
                tvValue.setText(value != null && !value.isEmpty() ? value : "— missing —");
            }
        }
    }

    private void submitListing(List<String> uploadedImageUrls) {
        CreateListingRequest createListingRequest = new CreateListingRequest(
                viewModel.productName,
                viewModel.description,
                viewModel.category,
                viewModel.island,
                viewModel.price,
                viewModel.priceUnit,
                viewModel.paymentMethods,
                viewModel.suggestedActivities,
                uploadedImageUrls
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

    private byte[] getCompressedImageBytes(Uri imageUri) {
        try {
            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            android.graphics.Bitmap originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream);

            if (originalBitmap == null) return null;

            int maxWidth = 1080;
            int maxHeight = 1080;
            float ratio = Math.min(
                    (float) maxWidth / originalBitmap.getWidth(),
                    (float) maxHeight / originalBitmap.getHeight()
            );

            android.graphics.Bitmap finalBitmap = originalBitmap;
            if (ratio < 1.0f) {
                int width = Math.round(originalBitmap.getWidth() * ratio);
                int height = Math.round(originalBitmap.getHeight() * ratio);
                finalBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, width, height, true);
            }

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            finalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos);

            return baos.toByteArray();

        } catch (Exception e) {
            Log.e("ImageCompression", "Failed to compress image", e);
            return null;
        }
    }

    private void uploadImagesToCloudinary() {
        if (viewModel.imageUris == null || viewModel.imageUris.isEmpty()) {
            submitListing(new java.util.ArrayList<>());
            return;
        }

        java.util.List<String> uploadedDownloadUrls = new java.util.ArrayList<>();
        int totalImages = viewModel.imageUris.size();

        String cloudName = "ddfqh3atl";
        String uploadPreset = "rentbnb_preset";
        String cloudinaryUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

        for (int i = 0; i < totalImages; i++) {
            Uri localUri = Uri.parse(viewModel.imageUris.get(i));

            byte[] compressedData = getCompressedImageBytes(localUri);
            if (compressedData == null) {
                Toast.makeText(requireContext(), "Error processing an image", Toast.LENGTH_SHORT).show();
                continue;
            }

            RequestBody presetBody = RequestBody.create(MediaType.parse("text/plain"), uploadPreset);
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), compressedData);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "image.jpg", fileBody);

            ApiClient.getApiService().uploadImageToCloudinary(cloudinaryUrl, presetBody, filePart).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String secureUrl = response.body().get("secure_url").getAsString();

                        synchronized (uploadedDownloadUrls) {
                            uploadedDownloadUrls.add(secureUrl);

                            if (uploadedDownloadUrls.size() == totalImages) {
                                submitListing(uploadedDownloadUrls);
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Cloudinary Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        resetButtonState();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(requireContext(), "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    resetButtonState();
                }
            });
        }
    }

    private void resetButtonState() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (tvBtnText != null) tvBtnText.setVisibility(View.VISIBLE);
        if (btnContainer != null) {
            btnContainer.setEnabled(true);
            btnContainer.setAlpha(1.0f);
        }
    }
}
