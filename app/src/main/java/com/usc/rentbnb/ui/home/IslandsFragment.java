package com.usc.rentbnb.ui.home;

import android.os.Bundle;
import android.util.DisplayMetrics;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.IslandCardAdapter;
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.viewmodels.FavoriteViewModel;
import com.usc.rentbnb.viewmodels.HomeViewModel;

import com.faltenreich.skeletonlayout.Skeleton;
import com.faltenreich.skeletonlayout.SkeletonLayoutUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IslandsFragment extends Fragment {
    private Skeleton skeleton;
    private TextView locationTitleView;
    private IslandCardAdapter adapter;
    private FavoriteViewModel favoriteViewModel;
    private SmartRefreshLayout swipeRefreshLayout;

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

        swipeRefreshLayout = view.findViewById(R.id.smartRefreshLayoutIslands);

        locationTitleView = view.findViewById(R.id.tv_islands_location_title);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            return;
        }
        String userId = user.getUid();

        if (requireActivity() instanceof HomeActivity) {
            String currentCity = ((HomeActivity) requireActivity()).userCity;
            updateLocationTitle(currentCity);
        }

        RecyclerView rv = view.findViewById(R.id.islandsRecyclerView);
        favoriteViewModel = new ViewModelProvider(requireActivity()).get(FavoriteViewModel.class);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rv.setLayoutManager(layoutManager);

        SnapHelper snapHelper = new PagerSnapHelper();
        rv.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(rv);

        HomeViewModel viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        adapter = new IslandCardAdapter((island, isCurrentlyFavorite) -> {
            if (isCurrentlyFavorite) {
                favoriteViewModel.deleteFavoriteIsland(userId, island.getId());
            } else {
                favoriteViewModel.addFavoriteIsland(userId, island);
            }
        });

        rv.setAdapter(adapter);

        rv.post(() -> {
            int width  = rv.getWidth();
            int height = rv.getHeight();
            if (width == 0) {
                DisplayMetrics dm = getResources().getDisplayMetrics();
                width  = dm.widthPixels;
                height = (int) (dm.widthPixels * 0.65f); // approximate card aspect ratio
            }
            adapter.attachPreloader(rv, width, height);
        });

        setUpObservers();

        skeleton = SkeletonLayoutUtils.applySkeleton(rv, R.layout.card_island, 3);
        skeleton.setMaskColor(ContextCompat.getColor(requireContext(), R.color.text_grey));
        skeleton.setMaskCornerRadius(28);

        skeleton.showSkeleton();

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

        viewModel.getIslands().observe(getViewLifecycleOwner(), islands -> {
            if (islands != null) {
                // Trust the backend! It already sorted and filtered the islands.
                adapter.setIslands(islands);
            } else {
                adapter.setIslands(new ArrayList<>());
            }

            skeleton.showOriginal();

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.finishRefresh();
            }

            rv.post(() -> rv.scrollBy(1, 0));
        });

        favoriteViewModel.loadIslands(userId);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(layout -> {
                skeleton.showSkeleton();
                if (requireActivity() instanceof HomeActivity) {
                    HomeActivity home = (HomeActivity) requireActivity();
                    if (home.userLat != 10.3157 || home.userLon != 123.8854) {
                        viewModel.fetchNearbyIslands(home.userLat, home.userLon);
                    } else {
                        viewModel.fetchIslands();
                    }
                } else {
                    viewModel.fetchIslands();
                }
            });
        }
    }

    public void updateLocationTitle(String city) {
        if (locationTitleView != null) {
            locationTitleView.setText("Islands near " + city);
        }
    }

    public void setUpObservers(){

        favoriteViewModel.getFavoriteIslands().observe(getViewLifecycleOwner(), favorites -> {
            if (favorites != null) {
                List<String> favoriteIds = new ArrayList<>();
                for (Island island : favorites) {
                    favoriteIds.add(island.getId());
                }
                adapter.setFavoriteIds(favoriteIds);
            }
        });

        favoriteViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Favorites Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

    }
}