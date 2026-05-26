package com.usc.rentbnb.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

    private TextView tvInboxHeader;
    private EditText etSearchChats;
    private ChatPagerAdapter pagerAdapter;

    public ChatFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 viewPager = view.findViewById(R.id.viewPagerChats);
        tvInboxHeader = view.findViewById(R.id.tvInboxHeader);
        etSearchChats = view.findViewById(R.id.etSearchChats); // NEW

        pagerAdapter = new ChatPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        applyWindowInsets();


        // NEW: Listen for text changes in the search bar
        etSearchChats.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Fire the search query into the pager adapter!
                if (pagerAdapter != null) {
                    pagerAdapter.filterAll(s.toString().trim());
                }
            }
        });
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