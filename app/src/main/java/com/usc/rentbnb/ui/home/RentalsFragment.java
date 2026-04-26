package com.usc.rentbnb.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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
    private TextView locationTitleView;

    private ListingAdapter adapterNear;
    private ListingAdapter adapterAll;


    private Skeleton skeletonNear;
    private Skeleton skeletonAll;

    private HomeViewModel homeViewModel;
    private FavoriteViewModel favoriteViewModel;

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

        locationTitleView = view.findViewById(R.id.tv_rentals_location_title);
        RecyclerView rvNear = view.findViewById(R.id.rv_rentals_near);
        RecyclerView rvAll = view.findViewById(R.id.rv_rentals_all);

        ListingAdapter.onFavoriteClickListener favListener = (listing, isCurrentlyFavorite) -> {
            if (isCurrentlyFavorite){
                favoriteViewModel.deleteFavoriteListing(userId, listing.getId());
            } else {
                favoriteViewModel.addFavoriteListing(userId, listing);
            }
        };

        // near you list
        rvNear.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        adapterNear = new ListingAdapter(favListener);
        rvNear.setAdapter(adapterNear);

        // all rentals list
        rvAll.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapterAll = new ListingAdapter(favListener);
        rvAll.setAdapter(adapterAll);

        skeletonNear = SkeletonLayoutUtils.applySkeleton(rvNear, R.layout.rentable_item_card, 2);
        skeletonNear.setMaskColor(ContextCompat.getColor(requireContext(), R.color.text_grey));
        skeletonNear.setMaskCornerRadius(16);
        skeletonNear.showSkeleton();

        skeletonAll = SkeletonLayoutUtils.applySkeleton(rvAll, R.layout.rentable_item_card, 4);
        skeletonAll.setMaskColor(ContextCompat.getColor(requireContext(), R.color.text_grey));
        skeletonAll.setMaskCornerRadius(16);
        skeletonAll.showSkeleton();

        if (requireActivity() instanceof HomeActivity) {
            HomeActivity activity = (HomeActivity) requireActivity();
            onLocationUpdated(activity.userCity, activity.userLat, activity.userLon);
        }

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        favoriteViewModel = new ViewModelProvider(requireActivity()).get(FavoriteViewModel.class);

        setupObservers();
        favoriteViewModel.loadListings(userId);
    }

    private void setupObservers() {

        homeViewModel.getListings().observe(getViewLifecycleOwner(), listings -> {
            if (listings != null) {
                currentAllListings = listings;
                    skeletonNear.showOriginal();
                    skeletonAll.showOriginal();

                    adapterNear.submitData(currentAllListings, currentFavoriteIds);
                    adapterAll.submitData(currentAllListings, currentFavoriteIds);
            }
        });

        favoriteViewModel.getFavoriteListings().observe(getViewLifecycleOwner(), favorites -> {
            if (favorites != null) {
                currentFavoriteIds.clear();

                for (Listing listing : favorites) {
                    currentFavoriteIds.add(listing.getId());
                }

                adapterNear.submitData(currentAllListings, currentFavoriteIds);
                adapterAll.submitData(currentAllListings, currentFavoriteIds);
            }
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                skeletonNear.showOriginal();
                skeletonAll.showOriginal();
                Toast.makeText(requireContext(), "Home Error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        favoriteViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Favorites Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onLocationUpdated(String city, double lat, double lon) {
        if (locationTitleView != null) {
            locationTitleView.setText("Near " + city);
        }

        if (adapterNear != null) {
            adapterNear.setUserLocation(lat, lon);
        }
        if (adapterAll != null) {
            adapterAll.setUserLocation(lat, lon);
        }
    }
}