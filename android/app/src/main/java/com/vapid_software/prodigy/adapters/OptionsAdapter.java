package com.vapid_software.prodigy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.data.OptionsData;
import com.vapid_software.prodigy.helpers.ViewHolder;

public class OptionsAdapter extends RecyclerView.Adapter {
    private OptionsData[] options;
    private OnOptionsSelectedListener onOptionsSelectedListener;

    public interface OnOptionsSelectedListener {
        void onOptionsSelected(OptionsData data);
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageView icon;
        public CustomViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            icon = v.findViewById(R.id.icon);
        }

        public TextView getName() {
            return name;
        }

        public ImageView getIcon() {
            return icon;
        }
    }

    public OptionsAdapter(OptionsData[] options) {
        this.options = options;
    }

    public void setOnOptionsSelectedListener(OnOptionsSelectedListener onOptionsSelectedListener) {
        this.onOptionsSelectedListener = onOptionsSelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_options_item, parent, false);
        CustomViewHolder holder = new CustomViewHolder(root);
        root.setOnClickListener((View v) -> {
            if(onOptionsSelectedListener != null) {
                onOptionsSelectedListener.onOptionsSelected(options[holder.getAdapterPosition()]);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        CustomViewHolder holder = (CustomViewHolder) h;
        OptionsData option = options[position];
        holder.getName().setText(option.getText());
        holder.getIcon().setImageResource(option.getIcon());
    }

    @Override
    public int getItemCount() {
        return options.length;
    }
}
