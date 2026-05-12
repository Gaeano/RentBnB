package com.usc.rentbnb.ui.chat;

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
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ChatPagerAdapter;

public class ChatFragment extends Fragment {

    public ChatFragment() {}

    private TextView tvInboxHeader;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tabLayoutChats);
        ViewPager2 viewPager = view.findViewById(R.id.viewPagerChats);
        tvInboxHeader = view.findViewById(R.id.tvInboxHeader);

        // Link the ViewPager to your new fragments
        ChatPagerAdapter pagerAdapter = new ChatPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        applyWindowInsets();
        // TabLayoutMediator automatically syncs tab clicks with ViewPager swipes
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("As Renter");
            } else {
                tab.setText("As Owner");
            }
        }).attach();
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(tvInboxHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight + 8,
                    v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }
}