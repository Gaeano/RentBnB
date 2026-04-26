package com.usc.rentbnb.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import com.faltenreich.skeletonlayout.Skeleton;
import com.faltenreich.skeletonlayout.SkeletonLayoutUtils;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ListingAdapter;
import com.usc.rentbnb.viewmodels.HomeViewModel;

import java.util.ArrayList;

public class RentalsFragment extends Fragment {

    private Skeleton skeleton;

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

        RecyclerView rv = view.findViewById(R.id.rentalsRecyclerView);
        SmartRefreshLayout swipeRefreshLayout = view.findViewById(R.id.smartRefreshLayoutRentals);

        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        HomeViewModel viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        ListingAdapter adapter = new ListingAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        skeleton = SkeletonLayoutUtils.applySkeleton(rv, R.layout.rentable_item_card, 4);
        skeleton.setMaskColor(ContextCompat.getColor(requireContext(), R.color.text_grey));
        skeleton.showSkeleton();

        // 1. Trigger the data fetch
        swipeRefreshLayout.setOnRefreshListener(refreshLayout -> {
            skeleton.showSkeleton();
            viewModel.fetchListings();
        });

        // 2. Watch for data and snap the custom layout back up
        viewModel.getListings().observe(getViewLifecycleOwner(), listings -> {
            skeleton.showOriginal();
            adapter.updateListings(listings);

            // Stop the refresh animation
            swipeRefreshLayout.finishRefresh();
        });
    }
}