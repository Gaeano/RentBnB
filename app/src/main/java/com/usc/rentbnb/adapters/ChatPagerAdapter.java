package com.usc.rentbnb.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.usc.rentbnb.ui.chat.OwnerChatFragment;
import com.usc.rentbnb.ui.chat.RenterChatFragment;

public class ChatPagerAdapter extends FragmentStateAdapter {

    public ChatPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new OwnerChatFragment();
        }
        return new RenterChatFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: Renter and Owner
    }
}