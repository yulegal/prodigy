package com.vapid_software.prodigy.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.LoginActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.adapters.OptionsAdapter;
import com.vapid_software.prodigy.adapters.ServiceAdapter;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.data.OptionsData;
import com.vapid_software.prodigy.helpers.ConfirmDialog;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.helpers.ViewHolder;
import com.vapid_software.prodigy.models.ServiceModel;
import com.vapid_software.prodigy.models.UserModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FavoritesFragment extends Fragment {
    private View loader, empty;
    private RecyclerView rv;
    private int page = 1;
    private int total;
    private List<ServiceModel> services;
    private final static int LIMIT = 20;

    private class Adapter extends RecyclerView.Adapter {
        private String locale;

        public Adapter() {
            locale = getContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.favorites_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            View root = holder.itemView;
            TextView name = root.findViewById(R.id.name);
            View more = root.findViewById(R.id.more);
            TextView type = root.findViewById(R.id.type);
            ServiceModel service = services.get(position);
            name.setText(service.getName());
            type.setText(service.getCategory().getName().get(locale));
            more.setOnClickListener((View v) -> {
                RecyclerView rv = (RecyclerView) getLayoutInflater().inflate(R.layout.favorite_options, null);
                int w = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        130,
                        getContext().getResources().getDisplayMetrics()
                );
                PopupWindow win = new PopupWindow(rv, w, WindowManager.LayoutParams.WRAP_CONTENT);
                rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                OptionsAdapter adapter = new OptionsAdapter(new OptionsData[]{
                        new OptionsData(
                                getContext().getResources().getString(R.string.option_delete),
                                0,
                                Defs.FavoritesOptions.DELETE
                        )
                });
                adapter.setOnOptionsSelectedListener((OptionsData data) -> {
                    if(data.getAction().equals(Defs.FavoritesOptions.DELETE)) {
                        ConfirmDialog confirmDialog = new ConfirmDialog(
                                getContext(),
                                getContext().getResources().getString(R.string.favorites_remove_confirm_body),
                                getContext().getResources().getString(R.string.favorites_remove_confirm_title),
                                null,
                                null
                        );
                        confirmDialog.setOnPositiveConfirmListener(() -> {
                            confirmDialog.dismiss();
                            removeFromFavorites(service, () -> {
                                services.remove(service);
                                notifyItemRemoved(holder.getAdapterPosition());
                                if(services.size() == 0) {
                                    rv.setVisibility(View.GONE);
                                    empty.setVisibility(View.VISIBLE);
                                }
                            });
                        });
                        confirmDialog.show();
                    }
                    win.dismiss();
                });
                rv.setAdapter(adapter);
                win.setOutsideTouchable(true);
                int coords[] = new int[2];
                v.getLocationOnScreen(coords);
                win.showAtLocation(rv, Gravity.NO_GRAVITY, coords[0] - (w / 2), coords[1] + v.getHeight() + 10);
            });
        }

        @Override
        public int getItemCount() {
            return services.size();
        }

        private void removeFromFavorites(ServiceModel service, Runnable runnable) {
            ApiBuilder builder = ApiBuilder.getInstance(getContext());
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).removeFromFavorites(service.getId()));
            });
            builder.setResponseListener(new ApiBuilder.ResponseListener<ServiceModel>() {
                @Override
                public void onResponse(Call<ServiceModel> call, Response<ServiceModel> response) {
                    if(response.code() == 200) {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.removed_from_favorites), Toast.LENGTH_LONG).show();
                        if(runnable != null) {
                            runnable.run();
                        }
                    }
                    else {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorites, container, false);
        rv = root.findViewById(R.id.rv);
        empty = root.findViewById(R.id.empty);
        loader = root.findViewById(R.id.loader);
        init();
        return root;
    }

    private void init() {
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(!recyclerView.canScrollVertically(1) && total != 0 && total != services.size()) {
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
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<ServiceModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<ServiceModel>> call, Response<FilterResponse<ServiceModel>> response) {
                if(response.code() == 201) {
                    total = response.body().getCount();
                    loader.setVisibility(View.GONE);
                    if(response.body().getTotal() == 0 && total == 0) {
                        empty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    }
                    else {
                        empty.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        if(services == null) {
                            services = new ArrayList<>();
                        }
                        services.addAll(response.body().getData());
                        if(rv.getLayoutManager() == null) {
                            rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        }
                        if(rv.getAdapter() == null) {
                            rv.setAdapter(new Adapter());
                        }
                        else {
                            rv.getAdapter().notifyItemRangeInserted(services.size() - response.body().getTotal(), response.body().getTotal());
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterFavorites(getFilterQueryOptions()));
        });
    }
}