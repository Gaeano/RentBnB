package com.usc.rentbnb.ui.favorites;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.faltenreich.skeletonlayout.Skeleton;
import com.faltenreich.skeletonlayout.SkeletonLayoutUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ListingAdapter;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.viewmodels.FavoriteViewModel;

import java.util.ArrayList;
import java.util.List;


public class FavoritesRentalsFragment extends Fragment {
    private RecyclerView rv;
    private Skeleton skeleton;
    private FavoriteViewModel favoriteViewModel;
    private ListingAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseUser user;

    public FavoritesRentalsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites_rentals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null){
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        user = auth.getCurrentUser();

        String userId = user.getUid();

        favoriteViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(FavoriteViewModel.class);

        rv = view.findViewById(R.id.rentalsRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(),2);

        rv.setLayoutManager(gridLayoutManager);

        adapter = new ListingAdapter((listing, isCurrentlyFavorite) -> {
           if (isCurrentlyFavorite){
               favoriteViewModel.deleteFavoriteListing(userId, listing.getId());
           } else {
               favoriteViewModel.addFavoriteListing(userId, listing);
           }
        });

        rv.setAdapter(adapter);

        skeleton = SkeletonLayoutUtils.applySkeleton(rv, R.layout.rentable_item_card, 3);
        skeleton.setMaskColor(ContextCompat.getColor(requireContext(), R.color.text_grey));

        setUpObservers();
        favoriteViewModel.loadListings(userId);

    }

    public void setUpObservers(){
        favoriteViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading){
                skeleton.showSkeleton();
            } else {
                skeleton.showOriginal();
            }
        });

        favoriteViewModel.getFavoriteListings().observe(getViewLifecycleOwner(), listings -> {
            if (listings != null){
                if (listings.isEmpty()){
                    Toast.makeText(requireContext(), "No favorite Listings available", Toast.LENGTH_LONG).show();
                }
                List<String> favoriteIds = new ArrayList<>();
                for (Listing listing : listings){
                    favoriteIds.add(listing.getId());
                }
                adapter.submitData(listings, favoriteIds);
            }
        });


        favoriteViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null){
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

}


