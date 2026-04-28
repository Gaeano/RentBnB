package com.usc.rentbnb.utils;

import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.chat.ChatFragment;
import com.usc.rentbnb.ui.favorites.FavoritesFragment;
import com.usc.rentbnb.ui.home.IslandsFragment;
import com.usc.rentbnb.ui.home.RentalsFragment;
import com.usc.rentbnb.ui.profile.ProfileFragment;

public class NavigationHelper {

    private final AppCompatActivity activity;
    private final int containerId;
    private final View homeHeader;


    private ImageView navHome, navFavorites, navBookings, navProfile;
    private View dotHome, dotFavorites, dotBookings, dotProfile;
    private final Fragment rentalsFragment = new RentalsFragment();
    private final Fragment favoritesFragment = new FavoritesFragment();

    public NavigationHelper(AppCompatActivity activity, int containerId, View homeHeader) {
        this.activity = activity;
        this.containerId = containerId;
        this.homeHeader = homeHeader;

        initializeViews();
        setOnClickListeners();
    }

    private void initializeViews(){
        navHome = activity.findViewById(R.id.navHome);
        navFavorites = activity.findViewById(R.id.navFavorites);
        navBookings = activity.findViewById(R.id.navChat);
        navProfile = activity.findViewById(R.id.navProfile);

        dotHome = activity.findViewById(R.id.dotHome);
        dotFavorites = activity.findViewById(R.id.dotFavorites);
        dotBookings = activity.findViewById(R.id.dotChat);
        dotProfile = activity.findViewById(R.id.dotProfile);
    }
    private void setOnClickListeners() {
        activity.findViewById(R.id.navHome).setOnClickListener(v -> {
             navigate (rentalsFragment, navHome, dotHome, true);
        });

        activity.findViewById(R.id.navFavorites).setOnClickListener(v -> {
            navigate (favoritesFragment, navFavorites, dotFavorites, false);
        });

        activity.findViewById(R.id.navChat).setOnClickListener(v -> {
            navigate (new ChatFragment(), navBookings, dotBookings, false);
        });

        activity.findViewById(R.id.navProfile).setOnClickListener(v -> {
            Log.d("NavigationHelper", "Profile button clicked");
             navigate (new ProfileFragment(), navProfile, dotProfile, false);
        });
    }

    private void navigate(Fragment fragment, ImageView activeIcon, View activeDot, boolean showHeader){


        resetUI();

        activeIcon.setAlpha(1.0f);
        activeDot.setVisibility(View.VISIBLE);

        if (homeHeader != null){
            TransitionManager.beginDelayedTransition((ViewGroup) homeHeader.getParent());

            homeHeader.setVisibility(showHeader ? View.VISIBLE : View.GONE);
        }


        activity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
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

    public void setInitialState() {
        navigate(new IslandsFragment(), navHome, dotHome, true);
    }

}
