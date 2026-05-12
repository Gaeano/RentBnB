package com.usc.rentbnb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.FAQ;

import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

    private List<FAQ> faqs;
    private OnFaqClickListener listener;

    public interface OnFaqClickListener {
        void onFaqClick(FAQ faq);
        void onDeleteClick(FAQ faq);
    }

    public FaqAdapter(List<FAQ> faqs, OnFaqClickListener listener) {
        this.faqs = faqs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
        FAQ faq = faqs.get(position);
        holder.tvQuestion.setText(faq.getQuestion());
        holder.tvAnswer.setText(faq.getAnswer());

        holder.itemView.setOnClickListener(v -> listener.onFaqClick(faq));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(faq));
    }

    @Override
    public int getItemCount() {
        return faqs.size();
    }

    public void setFaqs(List<FAQ> faqs) {
        this.faqs = faqs;
        notifyDataSetChanged();
    }

    static class FaqViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer;
        ImageView btnDelete;

        public FaqViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
            btnDelete = itemView.findViewById(R.id.btnDeleteFaq);
        }
    }
}
