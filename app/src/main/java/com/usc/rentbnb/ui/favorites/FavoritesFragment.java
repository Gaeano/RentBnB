package com.usc.rentbnb.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.home.IslandsFragment;

public class FavoritesFragment extends Fragment {
    private TextView favoritesIsland;
    private TextView favoritesRentals;
    private boolean isActiveIslands;

    private Fragment islandFragment = new FavoritesIslandsFragment();
    private Fragment rentalFragment = new FavoritesRentalsFragment();

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);



    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView favoritesTitle = view.findViewById(R.id.favorites_title);

        ViewCompat.setOnApplyWindowInsetsListener(favoritesTitle, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + 8,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.favorites_fragment_container, islandFragment)
                    .commit();
        }

        favoritesIsland = view.findViewById(R.id.tab_islands);
        favoritesRentals = view.findViewById(R.id.tab_rentals);
        isActiveIslands = true;

        setOnClickListeners();



        // Initialize your views here using view.findViewById(...)
        // EditText searchBar = view.findViewById(R.id.search_bar);
    }
    private void setOnClickListeners(){


        favoritesIsland.setOnClickListener(v -> {
            if (!isActiveIslands) {
                favoritesIsland.setBackgroundResource(R.drawable.bg_tab_active);
                favoritesRentals.setBackgroundResource(android.R.color.transparent);

                favoritesIsland.setTextColor(getResources().getColor(R.color.teal_dark, null));
                favoritesRentals.setTextColor(getResources().getColor(R.color.text_grey, null));

                isActiveIslands = true;

                fragmentNavigator(islandFragment);
            }
        });

        favoritesRentals.setOnClickListener(v -> {
           if (isActiveIslands){
               favoritesRentals.setBackgroundResource(R.drawable.bg_tab_active);
               favoritesIsland.setBackgroundResource(android.R.color.transparent);

               favoritesRentals.setTextColor(getResources().getColor(R.color.teal_dark, null));
               favoritesIsland.setTextColor(getResources().getColor(R.color.text_grey, null));

               isActiveIslands = false;


               fragmentNavigator(rentalFragment);
           }
        });
    }

    private void fragmentNavigator(Fragment newFragment){
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.favorites_fragment_container, newFragment)
                .commit();

    }

}