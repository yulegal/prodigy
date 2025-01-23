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
import com.vapid_software.prodigy.models.ServiceModel;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter {
    private List<ServiceModel> services;
    private OnServiceItemClickedListener onServiceItemClickedListener;

    public interface OnServiceItemClickedListener {
        void onServiceItemClicked(ServiceModel service);
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        private TextView name, type;
        private ImageView avatar;
        public CustomViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            type = v.findViewById(R.id.type);
            avatar = v.findViewById(R.id.avatar);
        }

        public TextView getName() {
            return name;
        }

        public TextView getType() {
            return type;
        }

        public ImageView getAvatar() {
            return avatar;
        }
    }

    public ServiceAdapter(List<ServiceModel> services) {
        this.services = services;
    }

    public void setOnServiceItemClickedListener(OnServiceItemClickedListener onServiceItemClickedListener) {
        this.onServiceItemClickedListener = onServiceItemClickedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.services_item, parent, false);
        CustomViewHolder holder = new CustomViewHolder(root);
        root.setOnClickListener((View v) -> {
            if(onServiceItemClickedListener != null) {
                onServiceItemClickedListener.onServiceItemClicked(services.get(holder.getAdapterPosition()));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        CustomViewHolder holder = (CustomViewHolder) h;
        String locale = holder.itemView.getContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
        ServiceModel service = services.get(holder.getAdapterPosition());
        holder.getName().setText(service.getName());
        if(service.getIcon() != null) {
            Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, service.getIcon())).into(holder.getAvatar());
        }
        holder.getType().setText(service.getCategory().getName().get(locale));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }
}
