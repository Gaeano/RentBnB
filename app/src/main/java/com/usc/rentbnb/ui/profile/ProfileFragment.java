package com.usc.rentbnb.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.faltenreich.skeletonlayout.Skeleton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.User;
import com.usc.rentbnb.ui.auth.LoginActivity;
import com.usc.rentbnb.ui.dashboard.DashboardActivity;
import com.usc.rentbnb.ui.favorites.FavoritesFragment;
import com.usc.rentbnb.ui.history.HistoryActivity;
import com.usc.rentbnb.viewmodels.AuthViewModel;
import com.usc.rentbnb.viewmodels.UserProfileViewModel;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;
    private ImageView ivAvatar;
    private LinearLayout profileHeader;
    private Skeleton skeleton;

    private UserProfileViewModel profileViewModel;
    private boolean isCompany = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        initViews(view);
        setupObservers();

        ViewCompat.setOnApplyWindowInsetsListener(profileHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = statusBarHeight + 16;
            v.setLayoutParams(params);
            return insets;
        });
        setupClickListeners(view);

        profileViewModel.loadUserData();
    }

    private void setupObservers() {
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                populateUI(user);
                String userType = user.getUserType();
                if ("COMPANY".equals(userType)) {
                    isCompany = true;
                }
            }
        });

        profileViewModel.getIsloading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                if (skeleton != null) skeleton.showSkeleton();
            } else {
                if (skeleton != null) skeleton.showOriginal();
            }
        });

        profileViewModel.getErrorData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.profile_name);
        tvEmail = view.findViewById(R.id.profile_email);
        ivAvatar = view.findViewById(R.id.profile_image);
        profileHeader = view.findViewById(R.id.profile_header);

        skeleton = view.findViewById(R.id.skeleton_profile);
    }

    private void populateUI(User user) {
        tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "N/A");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.userprofile)
                    .circleCrop()
                    .into(ivAvatar);
        }
    }

    private void setupClickListeners(View view) {
        LinearLayout menuHistory = view.findViewById(R.id.menu_history);
        LinearLayout menuHelpCenter = view.findViewById(R.id.menu_help_center);
        LinearLayout pushNotifsToggle = view.findViewById(R.id.push_notifs_toggle);
        SwitchMaterial switchPush = view.findViewById(R.id.switch_push_notifications);
        LinearLayout menuLogout = view.findViewById(R.id.menu_logout);
        View btnEditProfile = view.findViewById(R.id.menu_profile_detail);
        LinearLayout menuFavorite = view.findViewById(R.id.menu_favorites);

        ExtendedFloatingActionButton fabSwitchMode = view.findViewById(R.id.fab_switch_mode);
        NestedScrollView scrollView = view.findViewById(R.id.profile_scroll_view);

        // hide FAB
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY && fabSwitchMode.isShown()) {
                fabSwitchMode.hide();
            } else if (scrollY < oldScrollY && !fabSwitchMode.isShown()) {
                fabSwitchMode.show();
            }
        });

        fabSwitchMode.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), DashboardActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        menuHistory.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), HistoryActivity.class);
            startActivity(intent);
        });

        menuHelpCenter.setOnClickListener(v -> {
            Log.d("ProfileFragment", "Help Center button clicked");
        });

        menuFavorite.setOnClickListener(v -> {
            Fragment favoriteFrag = new FavoritesFragment();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.homeFeedContainer, favoriteFrag)
                    .addToBackStack(null)
                    .commit();
        });

        if (pushNotifsToggle != null && switchPush != null) {
            pushNotifsToggle.setOnClickListener(v -> {
                switchPush.setChecked(!switchPush.isChecked());
            });
        }

        menuLogout.setOnClickListener(v -> {
            AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
            authViewModel.logout();

            clearRememberMeData();

            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ProfileDetailsActivity.class);

                intent.putExtra("IS_COMPANY", isCompany);
                startActivity(intent);
            });
        }
    }

    private void clearRememberMeData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("RentBnBPrefs", requireActivity().MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("IS_REMEMBERED", false);
        edit.putString("SAVED_EMAIL", "");
        edit.apply();
    }
}