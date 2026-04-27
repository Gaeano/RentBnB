package com.usc.rentbnb.ui.listing;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.viewmodels.AddListingViewModel;

import java.util.HashMap;

public class ListingImagesFragment extends Fragment {
    private int currentSelectedSlotId = -1;
    private HashMap<Integer, Uri> selectedImages = new HashMap<>();
    private Button btnContinue;
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null && currentSelectedSlotId != -1) {
                    selectedImages.put(currentSelectedSlotId, uri);
                    displaySelectedImage(currentSelectedSlotId, uri);
                    updateContinueButtonState();
                } else {
                    //
                }
            });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing_images, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnContinue = view.findViewById(R.id.btnContinue);

        view.findViewById(R.id.coverPhotoSlot).setOnClickListener(v -> openImagePicker(R.id.coverPhotoSlot));
        view.findViewById(R.id.btnRemoveCover).setOnClickListener(v -> removeImage(R.id.coverPhotoSlot));

        int[][] slotsAndRemoveButtons = {
                {R.id.photoSlot1, R.id.btnRemove1},
                {R.id.photoSlot2, R.id.btnRemove2},
                {R.id.photoSlot3, R.id.btnRemove3},
                {R.id.photoSlot4, R.id.btnRemove4},
                {R.id.photoSlot5, R.id.btnRemove5}
        };

        for (int[] pair : slotsAndRemoveButtons) {
            int slotId = pair[0];
            int removeBtnId = pair[1];

            view.findViewById(slotId).setOnClickListener(v -> openImagePicker(slotId));

            View removeBtn = view.findViewById(removeBtnId);
            if (removeBtn != null) {
                removeBtn.setOnClickListener(v -> removeImage(slotId));
            }
        }

        restoreImagesFromViewModel();
        updateContinueButtonState();

        btnContinue.setOnClickListener(v -> {
            AddListingViewModel viewModel = new ViewModelProvider(requireActivity()).get(AddListingViewModel.class);
            viewModel.imageUris.clear();

            if (selectedImages.containsKey(R.id.coverPhotoSlot)) {
                viewModel.imageUris.add(selectedImages.get(R.id.coverPhotoSlot).toString());
            }

            int[] otherSlots = {
                    R.id.photoSlot1, R.id.photoSlot2,
                    R.id.photoSlot3, R.id.photoSlot4,
                    R.id.photoSlot5
            };

            for (int slot : otherSlots) {
                if (selectedImages.containsKey(slot)) {
                    viewModel.imageUris.add(selectedImages.get(slot).toString());
                }
            }

            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).goNextStep();
            }
        });
    }

    private void updateContinueButtonState() {
        if (btnContinue != null) {
            boolean hasCoverPhoto = selectedImages.containsKey(R.id.coverPhotoSlot);
            btnContinue.setEnabled(hasCoverPhoto);
            btnContinue.setAlpha(hasCoverPhoto ? 1.0f : 0.5f);
        }
    }

    private void openImagePicker(int slotId) {
        currentSelectedSlotId = slotId;
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void displaySelectedImage(int slotId, Uri uri) {
        View view = getView();
        if (view == null) return;

        ImageView previewImage = null;
        ImageView removeBtn = null;

        if (slotId == R.id.coverPhotoSlot) {
            previewImage = view.findViewById(R.id.ivCoverPreview);
            removeBtn = view.findViewById(R.id.btnRemoveCover);
        } else {
            if (slotId == R.id.photoSlot1) {
                previewImage = view.findViewById(R.id.listingImagePreview1);
                removeBtn = view.findViewById(R.id.btnRemove1);
            } else if (slotId == R.id.photoSlot2) {
                previewImage = view.findViewById(R.id.listingImagePreview2);
                removeBtn = view.findViewById(R.id.btnRemove2);
            } else if (slotId == R.id.photoSlot3) {
                previewImage = view.findViewById(R.id.listingImagePreview3);
                removeBtn = view.findViewById(R.id.btnRemove3);
            } else if (slotId == R.id.photoSlot4) {
                previewImage = view.findViewById(R.id.listingImagePreview4);
                removeBtn = view.findViewById(R.id.btnRemove4);
            } else if (slotId == R.id.photoSlot5) {
                previewImage = view.findViewById(R.id.listingImagePreview5);
                removeBtn = view.findViewById(R.id.btnRemove5);
            }
        }

        if (previewImage != null && removeBtn != null) {
            previewImage.setImageURI(uri);
            previewImage.setVisibility(View.VISIBLE);
            removeBtn.setVisibility(View.VISIBLE);
        }
    }

    private void removeImage(int slotId) {
        View view = getView();
        if (view == null) return;

        selectedImages.remove(slotId);

        ImageView previewImage = null;
        ImageView removeBtn = null;

        if (slotId == R.id.coverPhotoSlot) {
            previewImage = view.findViewById(R.id.ivCoverPreview);
            removeBtn = view.findViewById(R.id.btnRemoveCover);
        } else {
            if (slotId == R.id.photoSlot1) {
                previewImage = view.findViewById(R.id.listingImagePreview1);
                removeBtn = view.findViewById(R.id.btnRemove1);
            } else if (slotId == R.id.photoSlot2) {
                previewImage = view.findViewById(R.id.listingImagePreview2);
                removeBtn = view.findViewById(R.id.btnRemove2);
            } else if (slotId == R.id.photoSlot3) {
                previewImage = view.findViewById(R.id.listingImagePreview3);
                removeBtn = view.findViewById(R.id.btnRemove3);
            } else if (slotId == R.id.photoSlot4) {
                previewImage = view.findViewById(R.id.listingImagePreview4);
                removeBtn = view.findViewById(R.id.btnRemove4);
            } else if (slotId == R.id.photoSlot5) {
                previewImage = view.findViewById(R.id.listingImagePreview5);
                removeBtn = view.findViewById(R.id.btnRemove5);
            }
        }

        if (previewImage != null && removeBtn != null) {
            previewImage.setImageURI(null);
            previewImage.setVisibility(View.GONE);
            removeBtn.setVisibility(View.GONE);
        }
        updateContinueButtonState();
    }

    private void restoreImagesFromViewModel() {
        AddListingViewModel viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(AddListingViewModel.class);

        if (viewModel.imageUris == null || viewModel.imageUris.isEmpty()) {
            return;
        }

        int[] allSlots = {
                R.id.coverPhotoSlot,
                R.id.photoSlot1, R.id.photoSlot2,
                R.id.photoSlot3, R.id.photoSlot4,
                R.id.photoSlot5
        };

        for (int i = 0; i < viewModel.imageUris.size() && i < allSlots.length; i++) {
            Uri uri = Uri.parse(viewModel.imageUris.get(i));
            int slotId = allSlots[i];

            selectedImages.put(slotId, uri);

            displaySelectedImage(slotId, uri);
        }
        updateContinueButtonState();
    }
}