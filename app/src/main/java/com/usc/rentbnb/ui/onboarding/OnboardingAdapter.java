package com.usc.rentbnb.ui.onboarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<OnboardingItem> items;

    public OnboardingAdapter(Context context, List<OnboardingItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case OnboardingItem.TYPE_LANDING:
                return new LandingViewHolder(
                        inflater.inflate(R.layout.page_landing, parent, false)
                );
            case OnboardingItem.TYPE_FINAL:
                return new FinalViewHolder(
                        inflater.inflate(R.layout.page_final, parent, false)
                );
            default:
                return new TutorialViewHolder(
                        inflater.inflate(R.layout.page_tutorial, parent, false)
                );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        OnboardingItem item = items.get(position);
        switch (item.getType()) {
            case OnboardingItem.TYPE_LANDING:
                ((LandingViewHolder) holder).bind(item);
                break;
            case OnboardingItem.TYPE_FINAL:
                ((FinalViewHolder) holder).bind(item);
                break;
            default:
                ((TutorialViewHolder) holder).bind(item);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LandingViewHolder extends RecyclerView.ViewHolder {
        private final android.widget.ImageView imgBackground;
        private final android.widget.TextView tvTitle;
        private final android.widget.TextView tvSubtitle;

        LandingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBackground = itemView.findViewById(R.id.imgBackground);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
        }

        void bind(OnboardingItem item) {
            if (item.getBackgroundResId() != 0) {
                imgBackground.setImageResource(item.getBackgroundResId());
            }
            tvTitle.setText(item.getTitle());
            tvSubtitle.setText(item.getDescription());
        }
    }

    static class TutorialViewHolder extends RecyclerView.ViewHolder {
        private final android.widget.ImageView imgIcon;
        private final android.widget.TextView tvTitle;
        private final android.widget.TextView tvDescription;

        TutorialViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }

        void bind(OnboardingItem item) {
            if (item.getIconResId() != 0) {
                imgIcon.setImageResource(item.getIconResId());
            }
            tvTitle.setText(item.getTitle());
            tvDescription.setText(item.getDescription());
        }
    }

    static class FinalViewHolder extends RecyclerView.ViewHolder {
        private final android.widget.ImageView imgBackground;
        private final android.widget.TextView tvTitle;
        private final android.widget.TextView tvSubtitle;

        FinalViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBackground = itemView.findViewById(R.id.imgBackground);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
        }

        void bind(OnboardingItem item) {
            if (item.getBackgroundResId() != 0) {
                imgBackground.setImageResource(item.getBackgroundResId());
            }
            tvTitle.setText(item.getTitle());
            tvSubtitle.setText(item.getDescription());
        }
    }
}
