package com.usc.rentbnb.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.home.HomeActivity;
import com.usc.rentbnb.ui.profile.ProfileDetailsEditActivity;

public class OwnerProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View rowProfileDetails = view.findViewById(R.id.rowProfileDetails);
        View rowPayoutMethod = view.findViewById(R.id.rowPayoutMethod);
        SwitchMaterial switchPushNotifs = view.findViewById(R.id.switchPushNotifications);
        View rowHelpCenter = view.findViewById(R.id.rowHelpCenter);
        View rowSignOut = view.findViewById(R.id.rowSignOut);

        ExtendedFloatingActionButton fabSwitchMode = view.findViewById(R.id.fab_switch_mode);
        NestedScrollView scrollView = view.findViewById(R.id.owner_profile_scroll_view);

        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY && fabSwitchMode.isShown()) {
                fabSwitchMode.hide();
            } else if (scrollY < oldScrollY && !fabSwitchMode.isShown()) {
                fabSwitchMode.show();
            }
        });

        fabSwitchMode.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), HomeActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        rowProfileDetails.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ProfileDetailsEditActivity.class);
            intent.putExtra("IS_COMPANY", false);
            startActivity(intent);
        });

        rowPayoutMethod.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Open Payout Settings", Toast.LENGTH_SHORT).show();
        });

        if (switchPushNotifs != null) {
            switchPushNotifs.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "enabled" : "disabled";
                Toast.makeText(getContext(), "Notifications " + status, Toast.LENGTH_SHORT).show();
            });
        }

        rowHelpCenter.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Open Inquilino Help Center", Toast.LENGTH_SHORT).show();
        });

        rowSignOut.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Signing out...", Toast.LENGTH_SHORT).show();
        });
    }
}