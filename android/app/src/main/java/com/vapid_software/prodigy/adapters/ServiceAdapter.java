package com.vapid_software.prodigy.adapters;

import android.util.Log;
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
import com.vapid_software.prodigy.models.AddressModel;
import com.vapid_software.prodigy.models.ServiceModel;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter {
    private List<ServiceModel> services;
    private OnServiceItemClickedListener onServiceItemClickedListener;
    private AddressModel.Location location;

    public interface OnServiceItemClickedListener {
        void onServiceItemClicked(ServiceModel service);
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        private TextView name, type, distance;
        private ImageView avatar;
        private View distanceWrp;
        public CustomViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            type = v.findViewById(R.id.type);
            avatar = v.findViewById(R.id.avatar);
            distanceWrp = v.findViewById(R.id.distance_wrp);
            distance = v.findViewById(R.id.distance);
        }

        public TextView getDistance() {
            return distance;
        }

        public View getDistanceWrp() {
            return distanceWrp;
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

    public void setCurrentLocation(AddressModel.Location location) {
        this.location = location;
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
        else {
            holder.getAvatar().setImageResource(R.drawable.avatar);
        }
        if(service.getAddress().getLocation() != null) {
            holder.getDistanceWrp().setVisibility(View.VISIBLE);
            holder.getDistance().setText(calculateDistance(service.getAddress().getLocation()));
        }
        else {
            holder.getDistanceWrp().setVisibility(View.GONE);
        }
        holder.getType().setText(service.getCategory().getName().get(locale));
    }

    private String calculateDistance(AddressModel.Location location) {
        double lat = location.getLatitude() - this.location.getLatitude();
        double lon = location.getLongitude() - this.location.getLongitude();
        double dist = Math.sqrt((lat * lat) - (lon * lon));
        String result;
        if(dist / 1000 == 0) {
            result = String.format("%d m", (int) dist);
        }
        else {
            int r = (int)((dist % 1000) / 10);
            int d = (int)(dist / 1000);
            result = String.format("%d.%d km", d, r);
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return services.size();
    }
}
