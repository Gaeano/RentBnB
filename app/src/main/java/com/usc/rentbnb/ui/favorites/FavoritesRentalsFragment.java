package com.usc.rentbnb.ui.favorites;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faltenreich.skeletonlayout.Skeleton;
import com.faltenreich.skeletonlayout.SkeletonLayoutUtils;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ListingAdapter;
import com.usc.rentbnb.models.Listing;

import java.util.ArrayList;
import java.util.List;


public class FavoritesRentalsFragment extends Fragment {
    private RecyclerView rv;
    private Skeleton skeleton;

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

        List<Listing> dummyListings = getDummyListings();

        rv = view.findViewById(R.id.islandsRecyclerView);

        skeleton = SkeletonLayoutUtils.applySkeleton(rv, R.layout.rentable_item_card, 3);
        skeleton.setMaskColor(ContextCompat.getColor(requireContext(), R.color.text_grey));


        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(),2);

        rv.setLayoutManager(gridLayoutManager);
        ListingAdapter adapter = new ListingAdapter(dummyListings);
        rv.setAdapter(adapter);



    }

    private List<Listing> getDummyListings() {
        List<Listing> list = new ArrayList<>();

        list.add(new Listing(
                "rent_001",
                "Standard Motorized Bangka (10 Pax)",
                "Perfect for island hopping around the northern islands. Includes life jackets and a local guide.",
                "Boat",
                "Bantayan Island",
                2500.00,
                "/day",
                4.8,
                124,
                new ArrayList<>(), // Empty list for images for now
                false,
                true // Trending
        ));

        list.add(new Listing(
                "rent_002",
                "Premium Snorkeling Gear Set",
                "High-quality tempered glass mask and fins. Professionally sanitized after every use.",
                "Equipment",
                "Malapascua",
                300.00,
                "/day",
                4.5,
                89,
                new ArrayList<>(),
                false,
                false
        ));

        list.add(new Listing(
                "rent_003",
                "Transparent Kayak (2-Seater)",
                "Experience the crystal clear waters from above. Perfect for photos and sunset viewing.",
                "Water Sports",
                "Camotes Islands",
                500.00,
                "/hour",
                4.9,
                210,
                new ArrayList<>(),
                true, // New
                true  // Trending
        ));

        list.add(new Listing(
                "rent_004",
                "Luxury Catamaran Charter",
                "Full-day charter with a private captain, onboard grill, and lounging nets. Perfect for large groups.",
                "Boat",
                "Mactan",
                15000.00,
                "/day",
                5.0,
                32,
                new ArrayList<>(),
                true, // New
                false
        ));

        return list;
    }
}