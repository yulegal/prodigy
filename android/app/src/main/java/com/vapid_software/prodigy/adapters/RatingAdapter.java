package com.vapid_software.prodigy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.helpers.ViewHolder;

public class RatingAdapter extends RecyclerView.Adapter {
    private OnRatingSelectedListener onRatingSelectedListener;
    private int rating;

    public interface OnRatingSelectedListener {
        void onRatingSelected(int rating);
    }

    public RatingAdapter(int rating) {
        setRating(rating);
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setOnRatingSelectedListener(OnRatingSelectedListener onRatingSelectedListener) {
        this.onRatingSelectedListener = onRatingSelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(viewType == 0 ? R.layout.rating_selected_item : R.layout.rating_item, parent, false);
        ViewHolder holder =  new ViewHolder(root);
        root.setOnClickListener((View v) -> {
            if(onRatingSelectedListener != null) {
                onRatingSelectedListener.onRatingSelected(holder.getAdapterPosition() + 1);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    public int getRating() {
        return rating;
    }

    @Override
    public int getItemViewType(int position) {
        return position + 1 <= rating ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
