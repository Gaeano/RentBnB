package com.usc.rentbnb.ui.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;

import java.util.List;

public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

    private List<OnboardingPageModel> pages;
    private OnboardingActionCallback callback;

    public interface OnboardingActionCallback {
        void onNextClicked(int currentPosition);
        void onGetStartedClicked();
    }

    public OnboardingPagerAdapter(List<OnboardingPageModel> pages, OnboardingActionCallback callback) {
        this.pages = pages;
        this.callback = callback;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingPageModel page = pages.get(position);

        // Bind Text
        if (page.getTitle() != null && !page.getTitle().isEmpty()) {
            holder.tvTitle.setText(page.getTitle());
            holder.tvTitle.setVisibility(View.VISIBLE);
        } else {
            holder.tvTitle.setVisibility(View.GONE);
        }

        if (page.getDescription() != null && !page.getDescription().isEmpty()) {
            holder.tvDescription.setText(page.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Configure Layout based on PageType
        switch (page.getPageType()) {
            case LANDING:
                holder.ivFullscreenBackground.setVisibility(View.VISIBLE);
                holder.ivFullscreenBackground.setImageResource(page.getBackgroundResId());
                holder.bgOverlay.setVisibility(View.VISIBLE); // optional overlay for better text reading
                
                holder.ivCenterIcon.setVisibility(View.GONE);
                holder.btnNext.setVisibility(View.VISIBLE);
                holder.btnGetStarted.setVisibility(View.GONE);
                
                // Set text color to white for better contrast on background image
                holder.tvTitle.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
                holder.tvDescription.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
                break;

            case TUTORIAL:
                holder.ivFullscreenBackground.setVisibility(View.GONE);
                holder.bgOverlay.setVisibility(View.GONE);
                
                holder.ivCenterIcon.setVisibility(View.VISIBLE);
                if (page.getIconResId() != 0) {
                    holder.ivCenterIcon.setImageResource(page.getIconResId());
                }
                holder.btnNext.setVisibility(View.VISIBLE);
                holder.btnGetStarted.setVisibility(View.GONE);
                
                // Set text color to dark for white background
                holder.tvTitle.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
                holder.tvDescription.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                break;

            case FINAL:
                holder.ivFullscreenBackground.setVisibility(View.VISIBLE);
                holder.ivFullscreenBackground.setImageResource(page.getBackgroundResId());
                holder.bgOverlay.setVisibility(View.VISIBLE);
                
                holder.ivCenterIcon.setVisibility(View.GONE);
                holder.btnNext.setVisibility(View.GONE);
                holder.btnGetStarted.setVisibility(View.VISIBLE);
                
                // Set text color to white for better contrast on background image
                holder.tvTitle.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
                holder.tvDescription.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
                break;
        }

        // Setup Buttons
        holder.btnNext.setOnClickListener(v -> {
            if (callback != null) {
                callback.onNextClicked(holder.getAdapterPosition());
            }
        });

        holder.btnGetStarted.setOnClickListener(v -> {
            if (callback != null) {
                callback.onGetStartedClicked();
            }
        });
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFullscreenBackground, ivCenterIcon;
        View bgOverlay;
        TextView tvTitle, tvDescription;
        Button btnNext, btnGetStarted;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFullscreenBackground = itemView.findViewById(R.id.ivFullscreenBackground);
            bgOverlay = itemView.findViewById(R.id.bgOverlay);
            ivCenterIcon = itemView.findViewById(R.id.ivCenterIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnNext = itemView.findViewById(R.id.btnNext);
            btnGetStarted = itemView.findViewById(R.id.btnGetStarted);
        }
    }
}
