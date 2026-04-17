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
 * Step 5 / 6 — Attach Photos
 * Maps to: listings/{id}.imageUrls (String[] — Firebase Storage URLs)
 *
 * UI: a "cover photo" slot + up to 5 additional photo thumbnails.
 * Tapping any empty slot opens an image picker (intent not wired yet).
 */
public class ListingImagesFragment extends Fragment {

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

        // Cover photo tap
        view.findViewById(R.id.coverPhotoSlot).setOnClickListener(v ->
                openImagePicker("cover"));

        // Additional photo slots
        int[] slotIds = {
                R.id.photoSlot1, R.id.photoSlot2,
                R.id.photoSlot3, R.id.photoSlot4, R.id.photoSlot5
        };
        for (int id : slotIds) {
            final int slotId = id;
            view.findViewById(slotId).setOnClickListener(
                    v -> openImagePicker("slot_" + slotId));
        }

        view.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            if (getActivity() instanceof AddListingActivity) {
                ((AddListingActivity) getActivity()).goNextStep();
            }
        });
    }

    /** Placeholder — connect ActivityResultLauncher here when wiring logic. */
    private void openImagePicker(String slotTag) {
        // Intent intent = new Intent(Intent.ACTION_PICK,
        //         MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // imagePickerLauncher.launch(intent);
    }
}