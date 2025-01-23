package com.vapid_software.prodigy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.helpers.ViewHolder;
import com.vapid_software.prodigy.models.UserModel;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter {
    private List<UserModel> contacts;
    private OnContactClickedListener onContactClickedListener;

    public interface OnContactClickedListener {
        void onContactClicked(UserModel contact);
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        private TextView name, status;
        private ImageView avatar;
        public CustomViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            status = v.findViewById(R.id.status);
            avatar = v.findViewById(R.id.avatar);
        }

        public TextView getName() {
            return name;
        }

        public TextView getStatus() {
            return status;
        }

        public ImageView getAvatar() {
            return avatar;
        }
    }

    public ContactAdapter(List<UserModel> contacts) {
        this.contacts = contacts;
    }

    public void setOnContactClickedListener(OnContactClickedListener onContactClickedListener) {
        this.onContactClickedListener = onContactClickedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_item, parent, false);
        CustomViewHolder holder = new CustomViewHolder(root);
        root.setOnClickListener((View v) -> {
            if(onContactClickedListener != null) {
                onContactClickedListener.onContactClicked(contacts.get(holder.getAdapterPosition()));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        CustomViewHolder holder = (CustomViewHolder) h;
        TextView name = holder.getName();
        TextView status = holder.getStatus();
        ImageView avatar = holder.getAvatar();
        UserModel contact = contacts.get(position);
        name.setText(contact.getName());
        if(contact.getIcon() != null) {
            Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, contact.getIcon())).into(avatar);
        }
        if(contact.getStatus() != null) {
            status.setVisibility(View.VISIBLE);
            status.setText(contact.getStatus());
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
