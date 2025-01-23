package com.vapid_software.prodigy.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.helpers.ViewHolder;
import com.vapid_software.prodigy.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class NotificationsFragment extends BaseExtraFragment {
    private View empty, loader;
    private RecyclerView rv;
    private int page = 1;
    private int total;
    private List<NotificationModel> notifications;
    private final static int LIMIT = 20;

    private class Adapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.notifications_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            View root = holder.itemView;
            TextView title = root.findViewById(R.id.title);
            TextView body = root.findViewById(R.id.body);
            NotificationModel notification = notifications.get(holder.getAdapterPosition());
            title.setText(notification.getTitle());
            body.setText(notification.getBody());
            if(notification.isRead()) {
                title.setTextColor(Color.parseColor("#666666"));
            }
            root.setOnClickListener((View v) -> {
                ApiBuilder builder = ApiBuilder.getInstance(getContext());
                builder.setOnInitializedListener(() -> {
                    builder.send(builder.getApi(ApiService.class).read(notification.getId()));
                });
                builder.setResponseListener(new ApiBuilder.ResponseListener<NotificationModel>() {
                    @Override
                    public void onResponse(Call<NotificationModel> call, Response<NotificationModel> response) {
                        if(response.code() == 200) {
                            notifyItemChanged(holder.getLayoutPosition());
                            notification.setRead(true);
                        }
                        else {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        back = root.findViewById(R.id.back);
        rv = root.findViewById(R.id.rv);
        empty = root.findViewById(R.id.empty);
        loader = root.findViewById(R.id.loader);
        init();
        return root;
    }

    @Override
    protected void init() {
        super.init();
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(!recyclerView.canScrollVertically(1) && total != 0 && total != notifications.size()) {
                    ++page;
                    load();
                }
            }
        });
        load();
    }

    private FilterQueryOptions getFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        return new FilterQueryOptions(page, LIMIT, items);
    }

    private void load() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<NotificationModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<NotificationModel>> call, Response<FilterResponse<NotificationModel>> response) {
                if(response.code() == 201) {
                    loader.setVisibility(View.GONE);
                    total = response.body().getCount();
                    if(response.body().getTotal() == 0 && total == 0) {
                        empty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    }
                    else {
                        empty.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        if(notifications == null) {
                            notifications = new ArrayList<>();
                        }
                        notifications.addAll(response.body().getData());
                        if(rv.getLayoutManager() == null) {
                            LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                            rv.addItemDecoration(new DividerItemDecoration(getContext(), lm.getOrientation()));
                            rv.setLayoutManager(lm);
                        }
                        if(rv.getAdapter() == null) {
                            rv.setAdapter(new Adapter());
                        }
                        else {
                            rv.getAdapter().notifyItemRangeInserted(notifications.size() - response.body().getTotal(), response.body().getTotal());
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterNotifications(getFilterQueryOptions()));
        });
    }
}