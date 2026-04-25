package com.usc.rentbnb.utils;

import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.usc.rentbnb.R;

public class NavigationHelper {

    private final AppCompatActivity activity;
    private final int containerId;
    private final View homeHeader;


    private ImageView navHome, navFavorites, navBookings, navProfile;
    private View dotHome, dotFavorites, dotBookings, dotProfile;

    public NavigationHelper(AppCompatActivity activity, int containerId, View homeHeader) {
        this.activity = activity;
        this.containerId = containerId;
        this.homeHeader = homeHeader;

        initializeViews();
    }

    private void initializeViews(){
        navHome = activity.findViewById(R.id.navHome);
        navFavorites = activity.findViewById(R.id.navFavorites);
        navBookings = activity.findViewById(R.id.navBookings);
        navProfile = activity.findViewById(R.id.navProfile);

        dotHome = activity.findViewById(R.id.dotHome);
        dotFavorites = activity.findViewById(R.id.dotFavorites);
        dotBookings = activity.findViewById(R.id.dotBookings);
        dotProfile = activity.findViewById(R.id.dotProfile);
    }
    private void setOnClickListeners() {
        activity.findViewById(R.id.navHome).setOnClickListener(v -> {
            // navigate (new IslandsFragment(), navHome, dotHome, true);
        });

        activity.findViewById(R.id.navFavorites).setOnClickListener(v -> {
            //navigate (new FavoritesFragment(), navFavorites, dotFavorites, false);
        });

        activity.findViewById(R.id.navBookings).setOnClickListener(v -> {
            //navigate (new BookingsFragment(), navBookings, dotBookings, false);
        });

        activity.findViewById(R.id.navProfile).setOnClickListener(v -> {
            // navigate (new ProfileFragment(), navProfile, dotProfile, false);
        });
    }

    private void navigate(Fragment fragment, ImageView activeIcon, View activeDot, boolean showHeader){
        resetUI();

        activeIcon.setAlpha(1.0f);
        activeDot.setVisibility(View.VISIBLE);

        if (homeHeader != null){
            homeHeader.setVisibility(showHeader ? View.VISIBLE : View.GONE);
        }


        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }

    private void resetUI() {
        navHome.setAlpha(0.7f);
        navFavorites.setAlpha(0.7f);
        navBookings.setAlpha(0.7f);
        navProfile.setAlpha(0.7f);

        dotHome.setVisibility(View.GONE);
        dotFavorites.setVisibility(View.GONE);
        dotBookings.setVisibility(View.GONE);
        dotProfile.setVisibility(View.GONE);
    }

}
