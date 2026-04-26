package com.usc.rentbnb.ui.favorites;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.faltenreich.skeletonlayout.Skeleton;
import com.faltenreich.skeletonlayout.SkeletonLayoutUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.FavoriteIslandAdapter;
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.viewmodels.FavoriteViewModel;

import java.util.ArrayList;
import java.util.List;


public class FavoritesIslandsFragment extends Fragment {

    private Skeleton skeleton;
    private FavoriteViewModel favoriteViewModel;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser;
    private RecyclerView rv;
    private FavoriteIslandAdapter adapter;
    private LinearLayout emptyStateLayout;
    public FavoritesIslandsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites_islands, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = auth.getCurrentUser();
        if (currentUser == null){
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        favoriteViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(FavoriteViewModel.class);

        rv = view.findViewById(R.id.rentalsRecyclerView);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager);

        adapter = new FavoriteIslandAdapter((island, isCurrentlyFavorite) -> {
           if(isCurrentlyFavorite){
               favoriteViewModel.deleteFavoriteIsland(userId, island.getId());
           } else {
               favoriteViewModel.addFavoriteIsland(userId, island);
           }
        });
        rv.setAdapter(adapter);

        skeleton = SkeletonLayoutUtils.applySkeleton(rv, R.layout.favorite_island_card_item, 3);
        skeleton.setMaskColor(ContextCompat.getColor(requireContext(), R.color.text_grey));

        setUpObservers();

        favoriteViewModel.loadIslands(userId);

    }

    public void setUpObservers(){
        favoriteViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading){
                skeleton.showSkeleton();
            } else {
                skeleton.showOriginal();
            }
        });

        favoriteViewModel.getFavoriteIslands().observe(getViewLifecycleOwner(), islands -> {
            if (islands != null){
                if (islands.isEmpty()){
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                }

                List<String> favoriteIds = new ArrayList<>();
                for (Island island : islands){
                    favoriteIds.add(island.getId());
                }
                adapter.submitData(islands, favoriteIds);
            }
        });


        favoriteViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null){
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}