package com.usc.rentbnb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AllReviewsAdapter extends RecyclerView.Adapter<AllReviewsAdapter.ReviewViewHolder> {

    private List<Review> reviews = new ArrayList<>();

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvDate, tvRating, tvComment;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_reviewer_avatar);
            tvName = itemView.findViewById(R.id.tv_reviewer_name);
            tvDate = itemView.findViewById(R.id.tv_review_date);
            tvRating = itemView.findViewById(R.id.tv_review_rating);
            tvComment = itemView.findViewById(R.id.tv_review_comment);
        }

        public void bind(Review review) {
            tvName.setText(review.getReviewerName() != null ? review.getReviewerName() : "Renter");
            tvRating.setText(String.format(Locale.getDefault(), "%.1f", review.getRating()));
            tvComment.setText(review.getComment() != null ? review.getComment() : "");
            tvDate.setText(formatDate(review.getCreatedAt()));

            String photoUrl = review.getReviewerPhotoUrl();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(photoUrl)
                        .placeholder(R.drawable.userprofile)
                        .error(R.drawable.userprofile)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.userprofile);
            }
        }

        private String formatDate(String isoDate) {
            if (isoDate == null) return "N/A";
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                input.setTimeZone(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat output = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return output.format(input.parse(isoDate));
            } catch (Exception e) {
                return isoDate;
            }
        }
    }
}