package com.vapid_software.prodigy.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.adapters.ServiceAdapter;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.models.CategoryModel;
import com.vapid_software.prodigy.models.ServiceModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ServicesFragment extends BaseExtraFragment {
    private View loader, empty;
    private TextView type;
    private CategoryModel category;
    private int page = 1;
    private int total;
    private RecyclerView rv;
    private final static int LIMIT = 20;
    private CoreActivity activity;
    private List<ServiceModel> services;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
    }

    public void setCategory(CategoryModel category) {
        this.category = category;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_services, container, false);
        back = root.findViewById(R.id.back);
        type = root.findViewById(R.id.type);
        loader = root.findViewById(R.id.loader);
        empty = root.findViewById(R.id.empty);
        rv = root.findViewById(R.id.rv);
        init();
        return root;
    }

    protected void init() {
        super.init();
        if(category != null) {
            String locale = getContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
            type.setVisibility(View.VISIBLE);
            type.setText(category.getName().get(locale));
        }
        load();
    }

    private FilterQueryOptions getFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        if(category != null) {
            items.add(new FilterItem("category", category.getId()));
        }
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
                            ServiceAdapter adapter = new ServiceAdapter(services);
                            adapter.setOnServiceItemClickedListener((ServiceModel service) -> {
                                ServiceFragment serviceFragment = new ServiceFragment();
                                serviceFragment.setService(service);
                                serviceFragment.setOnBackPressedListener(() -> {
                                    activity.getSupportFragmentManager().popBackStack();
                                });
                                activity.loadExtra(serviceFragment, true);
                            });
                            rv.setAdapter(adapter);
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
            builder.send(builder.getApi(ApiService.class).filterServices(getFilterQueryOptions()));
        });
    }
}