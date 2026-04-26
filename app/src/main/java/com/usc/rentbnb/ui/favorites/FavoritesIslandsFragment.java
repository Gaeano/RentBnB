package com.usc.rentbnb.ui.favorites;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faltenreich.skeletonlayout.Skeleton;
import com.faltenreich.skeletonlayout.SkeletonLayoutUtils;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.FavoriteIslandAdapter;
import com.usc.rentbnb.adapters.IslandCardAdapter;
import com.usc.rentbnb.models.Island;

import java.util.ArrayList;
import java.util.List;


public class FavoritesIslandsFragment extends Fragment {

    private Skeleton skeleton;
    private RecyclerView rv;
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

        rv = view.findViewById(R.id.islandsRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        FavoriteIslandAdapter adapter = new FavoriteIslandAdapter();
        rv.setAdapter(adapter);

        skeleton = SkeletonLayoutUtils.applySkeleton(rv, R.layout.favorite_island_card_item, 3);
        skeleton.setMaskColor(ContextCompat.getColor(requireContext(), R.color.text_grey));

        List<Island> dummyIsland = getDummyIslands();

        adapter.setIslands(dummyIsland);
    }

// testing purposes
    private List<Island> getDummyIslands() {
        List<Island> list = new ArrayList<>();

        // Using the new constructor: Island(id, name, location, rating, category, description)
        list.add(new Island(
                "island_001",
                "Bantayan Island",
                "Cebu, Philippines",
                4.8,
                "Beach",
                "Famous for its powdery white sand beaches and relaxed island vibe."
        ));

        list.add(new Island(
                "island_002",
                "Malapascua",
                "Daanbantayan, Cebu",
                4.6,
                "Diving",
                "A premier diving destination known for thresher shark sightings."
        ));

        list.add(new Island(
                "island_003",
                "Camotes Islands",
                "Cebu, Philippines",
                4.5,
                "Nature",
                "Beautiful cave pools, lakes, and pristine shorelines."
        ));

        list.add(new Island(
                "island_004",
                "Sumilon Island",
                "Oslob, Cebu",
                4.7,
                "Resort",
                "Known for its shifting sandbar and crystal clear waters."
        ));

        return list;
    }
}