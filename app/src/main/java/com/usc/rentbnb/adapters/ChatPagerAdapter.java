package com.usc.rentbnb.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.usc.rentbnb.ui.chat.OwnerChatFragment;
import com.usc.rentbnb.ui.chat.RenterChatFragment;

public class ChatPagerAdapter extends FragmentStateAdapter {

    private final RenterChatFragment renterFragment;
    private final OwnerChatFragment ownerFragment;

    public ChatPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        // Create the fragments once so we can talk to them directly
        renterFragment = new RenterChatFragment();
        ownerFragment = new OwnerChatFragment();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 1 ? ownerFragment : renterFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    // NEW: Pass the search query to both tabs instantly
    public void filterAll(String query) {
        renterFragment.filterChats(query);
        ownerFragment.filterChats(query);
    }
}