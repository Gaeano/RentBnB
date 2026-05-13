package com.usc.rentbnb.ui.profile;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.auth.LoginActivity;
import com.usc.rentbnb.ui.history.HistoryActivity;
import com.usc.rentbnb.ui.home.HomeActivity;
import com.usc.rentbnb.ui.listing.AddListingActivity;
import com.usc.rentbnb.viewmodels.AuthViewModel;

public class ProfileFragment extends Fragment {

    private TextView[] filterChips;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment (Make sure you rename your XML file to fragment_profile!)
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout profileHeader = view.findViewById(R.id.profile_header);

        ViewCompat.setOnApplyWindowInsetsListener(profileHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + 8,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        setupFilterChips(view);

        setClickListenersMenu(view);

        TextView btnAddNewListing = view.findViewById(R.id.btn_add_new_listing);
        if (btnAddNewListing != null) {
            btnAddNewListing.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), AddListingActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupFilterChips(View view) {
        TextView chipCamera = view.findViewById(R.id.chip_camera);
        TextView chipSnorkel = view.findViewById(R.id.chip_snorkel);
        TextView chipMotorcycle = view.findViewById(R.id.chip_motorcycle);
        TextView chipBike = view.findViewById(R.id.chip_bike);
        TextView chipLabel = view.findViewById(R.id.chip_label);

        filterChips = new TextView[]{chipCamera, chipSnorkel, chipMotorcycle, chipBike, chipLabel};

        for (TextView chip : filterChips) {
            if (chip != null) {
                chip.setOnTouchListener((v, event) -> {
                    TextView clickedChip = (TextView) v;

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            clickedChip.animate().scaleX(0.90f).scaleY(0.90f).setDuration(100).start();
                            break;

                        case MotionEvent.ACTION_UP:
                            clickedChip.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                            handleChipSelection(clickedChip);
                            v.performClick();
                            break;

                        case MotionEvent.ACTION_CANCEL:
                            clickedChip.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                            break;
                    }
                    return true;
                });
            }
        }
    }

    private void handleChipSelection(TextView selectedChip) {
        if (selectedChip.getCurrentTextColor() == Color.WHITE) {
            return;
        }

        for (TextView chip : filterChips) {
            if (chip != null && chip != selectedChip) {
                chip.setBackgroundResource(R.drawable.chip_background_teal);

                if (chip.getCurrentTextColor() != Color.BLACK) {
                    ValueAnimator colorAnim = ValueAnimator.ofArgb(chip.getCurrentTextColor(), Color.BLACK);
                    colorAnim.setDuration(200);
                    colorAnim.addUpdateListener(animator -> chip.setTextColor((int) animator.getAnimatedValue()));
                    colorAnim.start();
                }
            }
        }

        selectedChip.setBackgroundResource(R.drawable.chip_background_selected);

        ValueAnimator colorAnimation = ValueAnimator.ofArgb(selectedChip.getCurrentTextColor(), Color.WHITE);
        colorAnimation.setDuration(200);
        colorAnimation.addUpdateListener(animator -> selectedChip.setTextColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    private void setClickListenersMenu(View view){
        LinearLayout menuProfileDetail = view.findViewById(R.id.menu_profile_detail);
        LinearLayout menuFavorites = view.findViewById(R.id.menu_favorites);
        LinearLayout menuHistory = view.findViewById(R.id.menu_history);
        LinearLayout menuHelpCenter = view.findViewById(R.id.menu_help_center);
        LinearLayout menuAppSettings = view.findViewById(R.id.menu_app_settings);
        LinearLayout menuLogout = view.findViewById(R.id.menu_logout);


        menuProfileDetail.setOnClickListener(v -> {
            //replace with navigation logic (prob fragment again)
            Log.d("ProfileFragment", "Profile Detail button clicked");
        });

        menuFavorites.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToFavorites();
            }
        });

        menuHistory.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), HistoryActivity.class);
            startActivity(intent);
        });

        menuHelpCenter.setOnClickListener(v -> {
            //replace with navigation logic (prob fragment again)
            Log.d("ProfileFragment", "Help Center button clicked");
        });

        menuAppSettings.setOnClickListener(v -> {
            //replace with navigation logic (prob fragment again)
            Log.d("ProfileFragment", "App Settings button clicked");
        });

        menuLogout.setOnClickListener(v -> {
            AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
            authViewModel.logout();

            clearRememberMeData();

            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void clearRememberMeData(){
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("RentBnBPrefs", requireActivity().MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("IS_REMEMBERED", false);
        edit.putString("SAVED_EMAIL", "");
        edit.apply();
    }
}
