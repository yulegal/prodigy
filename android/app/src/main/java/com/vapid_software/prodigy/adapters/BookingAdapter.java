package com.vapid_software.prodigy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.helpers.ViewHolder;
import com.vapid_software.prodigy.models.BookingModel;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter {
    private List<BookingModel> bookings;
    private OnBookingClickedListener onBookingClickedListener;

    public interface OnBookingClickedListener {
        void onBookingClicked(BookingModel booking);
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        private TextView name, type;
        public CustomViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            type = v.findViewById(R.id.type);
        }

        public TextView getName() {
            return name;
        }

        public TextView getType() {
            return type;
        }
    }

    public BookingAdapter(List<BookingModel> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_item, parent, false);
        CustomViewHolder holder = new CustomViewHolder(root);
        root.setOnClickListener((View v) -> {
            if(onBookingClickedListener != null) {
                onBookingClickedListener.onBookingClicked(bookings.get(holder.getAdapterPosition()));
            }
        });
        return holder;
    }

    public void setOnBookingClickedListener(OnBookingClickedListener onBookingClickedListener) {
        this.onBookingClickedListener = onBookingClickedListener;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        CustomViewHolder holder = (CustomViewHolder) h;
        TextView name = holder.getName();
        TextView type = holder.getType();
        String locale = holder.itemView.getContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
        BookingModel booking = bookings.get(holder.getAdapterPosition());
        name.setText(booking.getService().getName());
        type.setText(booking.getService().getCategory().getName().get(locale));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }
}
