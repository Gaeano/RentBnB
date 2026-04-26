package com.usc.rentbnb.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faltenreich.skeletonlayout.Skeleton;
import com.faltenreich.skeletonlayout.SkeletonLayoutUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ListingAdapter;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.viewmodels.FavoriteViewModel;
import com.usc.rentbnb.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class RentalsFragment extends Fragment {

    private Skeleton skeleton;
    private HomeViewModel homeViewModel;
    private FavoriteViewModel favoriteViewModel;
    private ListingAdapter adapter;
    private List<Listing> currentAllListings = new ArrayList<>();
    private List<String> currentFavoriteIds = new ArrayList<>();

    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_rentals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        RecyclerView rv = view.findViewById(R.id.rentalsRecyclerView);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

         homeViewModel = new ViewModelProvider(requireActivity())
                .get(HomeViewModel.class);
        favoriteViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(FavoriteViewModel.class);

        adapter = new ListingAdapter((listing, isCurrentlyFavorite) -> {
            if (isCurrentlyFavorite){
                favoriteViewModel.deleteFavoriteListing(userId, listing.getId());
            } else {
                favoriteViewModel.addFavoriteListing(userId, listing);
            }
        });
        rv.setAdapter(adapter);

        skeleton = SkeletonLayoutUtils.applySkeleton(rv, R.layout.rentable_item_card, 4);
        skeleton.setMaskColor(ContextCompat.getColor(requireContext(), R.color.text_grey));
        skeleton.showSkeleton();

        setupObservers();

        favoriteViewModel.loadListings(userId);
        homeViewModel.fetchListings();

    }

    private void setupObservers() {

        homeViewModel.getListings().observe(getViewLifecycleOwner(), listings -> {
            if (listings != null) {
                currentAllListings = listings;
                    adapter.submitData(currentAllListings, currentFavoriteIds);
            }
        });

        favoriteViewModel.getFavoriteListings().observe(getViewLifecycleOwner(), favorites -> {
            if (favorites != null) {
                currentFavoriteIds.clear();

                for (Listing listing : favorites) {
                    currentFavoriteIds.add(listing.getId());
                }
                skeleton.showOriginal();
                adapter.submitData(currentAllListings, currentFavoriteIds);
            }
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                skeleton.showOriginal(); // Stop the shimmer
                Toast.makeText(requireContext(), "Home Error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        // NEW: Listen for Favorite engine errors
        favoriteViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Favorites Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}