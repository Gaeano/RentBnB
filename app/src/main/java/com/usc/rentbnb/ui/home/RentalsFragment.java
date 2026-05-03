package com.usc.rentbnb.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ListingAdapter;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.viewmodels.FavoriteViewModel;
import com.usc.rentbnb.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RentalsFragment extends Fragment {
    private TextView locationTitleView;

    private ListingAdapter adapterNear;
    private ListingAdapter adapterAll;
    private Skeleton skeletonNear;
    private Skeleton skeletonAll;

    private LinearLayout containerPopular, containerFavorites, containerSpotlight;
    private TextView tvSpotlightTitle;
    private ListingAdapter adapterPopular, adapterFavorites, adapterSpotlight;

    private HomeViewModel homeViewModel;
    private FavoriteViewModel favoriteViewModel;

    private List<Listing> currentAllListings = new ArrayList<>();
    private List<String> currentFavoriteIds = new ArrayList<>();

    private FirebaseUser currentUser;
    private SmartRefreshLayout swipeRefreshLayout;

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

        swipeRefreshLayout = view.findViewById(R.id.smartRefreshLayoutRentals);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        locationTitleView = view.findViewById(R.id.tv_rentals_location_title);
        RecyclerView rvNear = view.findViewById(R.id.rv_rentals_near);
        RecyclerView rvAll = view.findViewById(R.id.rv_rentals_all);

        containerPopular = view.findViewById(R.id.container_popular);
        containerFavorites = view.findViewById(R.id.container_favorites);
        containerSpotlight = view.findViewById(R.id.container_spotlight);
        tvSpotlightTitle = view.findViewById(R.id.tv_spotlight_title);

        ListingAdapter.onFavoriteClickListener favListener = (listing, isCurrentlyFavorite) -> {
            if (isCurrentlyFavorite){
                favoriteViewModel.deleteFavoriteListing(userId, listing.getId());
            } else {
                favoriteViewModel.addFavoriteListing(userId, listing);
            }
        };

        adapterPopular = setupHorizontalList(view.findViewById(R.id.rv_rentals_popular), favListener);
        adapterFavorites = setupHorizontalList(view.findViewById(R.id.rv_rentals_favorites), favListener);
        adapterSpotlight = setupHorizontalList(view.findViewById(R.id.rv_rentals_spotlight), favListener);

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

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(layout -> {
                skeletonNear.showSkeleton();
                skeletonAll.showSkeleton();
                homeViewModel.fetchListings();
            });
        }
    }

    private ListingAdapter setupHorizontalList(RecyclerView rv, ListingAdapter.onFavoriteClickListener listener) {
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        ListingAdapter adapter = new ListingAdapter(listener);
        rv.setAdapter(adapter);
        return adapter;
    }

    private void setupObservers() {

        homeViewModel.getListings().observe(getViewLifecycleOwner(), listings -> {
            if (listings != null) {
                currentAllListings = listings;
                skeletonNear.showOriginal();
                skeletonAll.showOriginal();

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.finishRefresh();
                }

                adapterNear.submitData(currentAllListings, currentFavoriteIds);
                adapterAll.submitData(currentAllListings, currentFavoriteIds);

                distributeData(currentAllListings);
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

                if (adapterPopular != null) distributeData(currentAllListings);
            }
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                skeletonNear.showOriginal();
                skeletonAll.showOriginal();

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.finishRefresh();
                }

                Toast.makeText(requireContext(), "Home Error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        favoriteViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Favorites Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void distributeData(List<Listing> allListings) {
        List<Listing> popularList = new ArrayList<>();
        List<Listing> favoritesList = new ArrayList<>();
        List<Listing> spotlightList = new ArrayList<>();

        for (Listing listing : allListings) {
            if (listing.getTimesRented() >= 5) {
                popularList.add(listing);
            }
            if (listing.getRating() >= 4.8) {
                favoritesList.add(listing);
            }
        }

        Collections.sort(popularList, (a, b) -> Integer.compare(b.getTimesRented(), a.getTimesRented()));

        Set<String> uniqueIslands = new HashSet<>();
        for (Listing listing : allListings) {
            if (listing.getIsland() != null && !listing.getIsland().isEmpty()) {
                uniqueIslands.add(listing.getIsland());
            }
        }

        if (!uniqueIslands.isEmpty()) {
            List<String> islandList = new ArrayList<>(uniqueIslands);
            String randomIsland = islandList.get(new Random().nextInt(islandList.size()));
            tvSpotlightTitle.setText("Heading to " + randomIsland +"?");

            for (Listing listing : allListings) {
                if (randomIsland.equals(listing.getIsland())) {
                    spotlightList.add(listing);
                }
            }
        }

        containerPopular.setVisibility(popularList.isEmpty() ? View.GONE : View.VISIBLE);
        adapterPopular.submitData(popularList, currentFavoriteIds);

        containerFavorites.setVisibility(favoritesList.isEmpty() ? View.GONE : View.VISIBLE);
        adapterFavorites.submitData(favoritesList, currentFavoriteIds);

        containerSpotlight.setVisibility(spotlightList.isEmpty() ? View.GONE : View.VISIBLE);
        adapterSpotlight.submitData(spotlightList, currentFavoriteIds);
    }

    public void onLocationUpdated(String city, double lat, double lon) {
        if (locationTitleView != null) {
            locationTitleView.setText("Near " + city);
        }

        if (adapterNear != null) adapterNear.setUserLocation(lat, lon);
        if (adapterAll != null) adapterAll.setUserLocation(lat, lon);
        if (adapterPopular != null) adapterPopular.setUserLocation(lat, lon);
        if (adapterFavorites != null) adapterFavorites.setUserLocation(lat, lon);
        if (adapterSpotlight != null) adapterSpotlight.setUserLocation(lat, lon);
    }
}