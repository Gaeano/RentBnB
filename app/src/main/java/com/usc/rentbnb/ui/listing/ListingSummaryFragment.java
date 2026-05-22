package com.usc.rentbnb.ui.listing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.usc.rentbnb.viewmodels.ListingDraft;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListingSummaryFragment extends Fragment {

    private AddListingViewModel viewModel;
    private View btnAddAnother;
    private View btnSubmitAll;
    private TextView tvSubmitLabel;
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

        btnAddAnother = view.findViewById(R.id.btnAddAnother);
        btnSubmitAll = view.findViewById(R.id.btnListProductContainer);
        tvSubmitLabel = view.findViewById(R.id.tvBtnText);
        progressBar = view.findViewById(R.id.progressBar);

        populateSummaryData(view);
        updateSubmitButtonLabel();

        btnAddAnother.setOnClickListener(v -> {
            viewModel.queueCurrentAndStartNew();

            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).restartFormForNewDraft();
            }
        });

        btnSubmitAll.setOnClickListener(v -> {
            setLoadingState(true);
            uploadAllAndSubmit();
        });
    }

    private void updateSubmitButtonLabel() {
        int count = viewModel.getDraftCount();
        tvSubmitLabel.setText(count == 1 ? "List Product" : "Submit All (" + count + ")");
    }

    private void populateSummaryData(View view) {
        ListingDraft draft = viewModel.currentDraft();

        setTextForIncludedRow(view, R.id.rowName,     "Title",       draft.productName);
        setTextForIncludedRow(view, R.id.rowDesc,     "Description", draft.description);
        setTextForIncludedRow(view, R.id.rowLocation, "Island",      draft.island);

        TextView tvCategory = view.findViewById(R.id.tvSummaryCategory);
        tvCategory.setText(draft.category.isEmpty() ? "— none selected —" : draft.category);

        TextView tvPrice = view.findViewById(R.id.tvSummaryPrice);
        tvPrice.setText(String.format("₱%.2f / %s", draft.price, draft.priceUnit));

        TextView tvPayments = view.findViewById(R.id.tvSummaryPayments);
        tvPayments.setText(draft.paymentMethods.isEmpty()
                ? "— none selected —"
                : TextUtils.join(", ", draft.paymentMethods));

        TextView tvActivities = view.findViewById(R.id.tvSummaryActivities);
        tvActivities.setText(draft.suggestedActivities.isEmpty()
                ? "— none selected —"
                : TextUtils.join(", ", draft.suggestedActivities));

        TextView tvPhotos = view.findViewById(R.id.tvSummaryPhotos);
        tvPhotos.setText(draft.imageUris.size() + " photos attached");

        ImageView ivCover = view.findViewById(R.id.ivSummaryCover);
        TextView tvCoverPlaceholder = view.findViewById(R.id.tvSummaryCoverPlaceholder);
        if (!draft.imageUris.isEmpty()) {
            ivCover.setVisibility(View.VISIBLE);
            ivCover.setImageURI(Uri.parse(draft.imageUris.get(0)));
            tvCoverPlaceholder.setVisibility(View.GONE);
        } else {
            ivCover.setVisibility(View.GONE);
            tvCoverPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void uploadAllAndSubmit() {
        List<ListingDraft> allDrafts = viewModel.getAllDrafts();
        int totalDrafts = allDrafts.size();

        AtomicInteger completedDrafts = new AtomicInteger(0);
        AtomicInteger failedDrafts    = new AtomicInteger(0);

        for (ListingDraft draft : allDrafts) {
            if (draft.imageUris.isEmpty()) {
                submitSingleListing(draft, new ArrayList<>(),
                        completedDrafts, failedDrafts, totalDrafts);
            } else {
                uploadImagesForDraft(draft, completedDrafts, failedDrafts, totalDrafts);
            }
        }
    }

    private void uploadImagesForDraft(ListingDraft draft,
                                      AtomicInteger completedDrafts,
                                      AtomicInteger failedDrafts,
                                      int totalDrafts) {
        List<String> uploadedUrls = new ArrayList<>();
        int totalImages = draft.imageUris.size();
        AtomicInteger uploadedCount = new AtomicInteger(0);

        String cloudName    = "ddfqh3atl";
        String uploadPreset = "rentbnb_preset";
        String cloudinaryUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

        for (String uriString : draft.imageUris) {
            Uri localUri = Uri.parse(uriString);
            byte[] compressedData = getCompressedImageBytes(localUri);

            if (compressedData == null) {
                if (uploadedCount.incrementAndGet() == totalImages) {
                    submitSingleListing(draft, uploadedUrls,
                            completedDrafts, failedDrafts, totalDrafts);
                }
                continue;
            }

            RequestBody presetBody = RequestBody.create(
                    MediaType.parse("text/plain"), uploadPreset);
            RequestBody fileBody   = RequestBody.create(
                    MediaType.parse("image/jpeg"), compressedData);
            MultipartBody.Part filePart = MultipartBody.Part
                    .createFormData("file", "image.jpg", fileBody);

            ApiClient.getApiService()
                    .uploadImageToCloudinary(cloudinaryUrl, presetBody, filePart)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call,
                                               Response<JsonObject> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String secureUrl = response.body()
                                        .get("secure_url").getAsString();
                                synchronized (uploadedUrls) {
                                    uploadedUrls.add(secureUrl);
                                }
                            }
                            if (uploadedCount.incrementAndGet() == totalImages) {
                                submitSingleListing(draft, uploadedUrls,
                                        completedDrafts, failedDrafts, totalDrafts);
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            if (uploadedCount.incrementAndGet() == totalImages) {
                                submitSingleListing(draft, uploadedUrls,
                                        completedDrafts, failedDrafts, totalDrafts);
                            }
                        }
                    });
        }
    }

    private void submitSingleListing(ListingDraft draft,
                                     List<String> imageUrls,
                                     AtomicInteger completedDrafts,
                                     AtomicInteger failedDrafts,
                                     int totalDrafts) {
        CreateListingRequest request = new CreateListingRequest(
                draft.productName,
                draft.description,
                draft.category,
                draft.island,
                draft.price,
                draft.priceUnit,
                draft.penaltyPrice,
                draft.penaltyUnit,
                draft.paymentMethods,
                draft.suggestedActivities,
                imageUrls
        );

        ApiClient.getApiService().createListing(request)
                .enqueue(new Callback<CreateListingResponse>() {
                    @Override
                    public void onResponse(Call<CreateListingResponse> call,
                                           Response<CreateListingResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            checkAllDone(completedDrafts, failedDrafts, totalDrafts);
                        } else {
                            failedDrafts.incrementAndGet();
                            checkAllDone(completedDrafts, failedDrafts, totalDrafts);
                        }
                    }

                    @Override
                    public void onFailure(Call<CreateListingResponse> call, Throwable t) {
                        failedDrafts.incrementAndGet();
                        checkAllDone(completedDrafts, failedDrafts, totalDrafts);
                    }
                });
    }

    private void checkAllDone(AtomicInteger completedDrafts,
                              AtomicInteger failedDrafts,
                              int totalDrafts) {
        int done = completedDrafts.incrementAndGet();

        if (done == totalDrafts) {
            if (getView() == null) return;
            requireActivity().runOnUiThread(() -> {
                setLoadingState(false);
                int failed = failedDrafts.get();

                if (failed == 0) {
                    viewModel.resetAll();
                    if (getActivity() instanceof AddListingActivity) {
                        ((AddListingActivity) getActivity()).goToSuccessWithCount(totalDrafts);
                    }
                } else {
                    Toast.makeText(requireContext(),
                            (totalDrafts - failed) + " listings published, "
                                    + failed + " failed. Try again.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setLoadingState(boolean loading) {
        if (btnSubmitAll != null) {
            btnSubmitAll.setEnabled(!loading);
            btnSubmitAll.setAlpha(loading ? 0.8f : 1.0f);
        }
        if (btnAddAnother != null) btnAddAnother.setEnabled(!loading);
        if (progressBar != null)
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (tvSubmitLabel != null)
            tvSubmitLabel.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void setTextForIncludedRow(View parentView, int includeId,
                                       String label, String value) {
        View row = parentView.findViewById(includeId);
        if (row == null) return;
        int labelId = getResources().getIdentifier("tvLabel", "id",
                requireContext().getPackageName());
        int valueId = getResources().getIdentifier("tvValue", "id",
                requireContext().getPackageName());
        TextView tvLabel = row.findViewById(labelId);
        TextView tvValue = row.findViewById(valueId);
        if (tvLabel != null) tvLabel.setText(label);
        if (tvValue != null)
            tvValue.setText(value != null && !value.isEmpty() ? value : "— missing —");
    }

    private byte[] getCompressedImageBytes(Uri imageUri) {
        try {
            InputStream inputStream = requireContext()
                    .getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap =
                    BitmapFactory.decodeStream(inputStream);
            if (originalBitmap == null) return null;

            int maxSize = 1080;
            float ratio = Math.min(
                    (float) maxSize / originalBitmap.getWidth(),
                    (float) maxSize / originalBitmap.getHeight());

            Bitmap finalBitmap = ratio < 1.0f
                    ? Bitmap.createScaledBitmap(originalBitmap,
                    Math.round(originalBitmap.getWidth() * ratio),
                    Math.round(originalBitmap.getHeight() * ratio), true)
                    : originalBitmap;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            Log.e("ImageCompression", "Failed to compress image", e);
            return null;
        }
    }
}