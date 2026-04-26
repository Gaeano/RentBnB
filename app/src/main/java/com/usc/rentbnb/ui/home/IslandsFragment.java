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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.IslandCardAdapter;
import com.usc.rentbnb.viewmodels.HomeViewModel;

import com.faltenreich.skeletonlayout.Skeleton;
import com.faltenreich.skeletonlayout.SkeletonLayoutUtils;

public class IslandsFragment extends Fragment {
    private Skeleton skeleton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_islands, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.islandsRecyclerView);
        SmartRefreshLayout swipeRefreshLayout = view.findViewById(R.id.smartRefreshLayoutIslands);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rv.setLayoutManager(layoutManager);

        SnapHelper snapHelper = new PagerSnapHelper();
        rv.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(rv);

        HomeViewModel viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        IslandCardAdapter adapter = new IslandCardAdapter();
        rv.setAdapter(adapter);

        skeleton = SkeletonLayoutUtils.applySkeleton(rv, R.layout.card_island, 3);
        skeleton.setMaskColor(ContextCompat.getColor(requireContext(), R.color.text_grey));
        skeleton.setMaskCornerRadius(28);
        skeleton.showSkeleton();

        // 1. Trigger the data fetch
        swipeRefreshLayout.setOnRefreshListener(refreshLayout -> {
            skeleton.showSkeleton();
            viewModel.fetchIslands();
        });

        // 2. Watch for data and snap the custom layout back up
        viewModel.getIslands().observe(getViewLifecycleOwner(), islands -> {
            skeleton.showOriginal();
            adapter.setIslands(islands);

            // This tiny scroll trick ensures the 3D effect calculates properly on load
            rv.post(() -> rv.scrollBy(1, 0));

            // Stop the refresh animation
            swipeRefreshLayout.finishRefresh();
        });

        // Scroll listener for the 3D effect
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                float centerX = recyclerView.getWidth() / 2f;
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    float childCenterX = (child.getLeft() + child.getRight()) / 2f;
                    float distanceFromCenter = Math.abs(centerX - childCenterX);
                    float scale = 1f - (distanceFromCenter / recyclerView.getWidth()) * 0.15f;
                    scale = Math.max(0.85f, scale);
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                }
            }
        });
//        viewModel.getIslands().observe(getViewLifecycleOwner(), islands -> {
//            adapter.setIslands(islands);
//
//            skeleton.showOriginal();
//            rv.post(() -> rv.scrollBy(1, 0));
//        });
    }
}