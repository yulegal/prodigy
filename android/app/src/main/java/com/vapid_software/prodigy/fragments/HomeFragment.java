package com.vapid_software.prodigy.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.adapters.ServiceAdapter;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.models.AddressModel;
import com.vapid_software.prodigy.models.ServiceModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private RecyclerView rv;
    private View empty, loader, filter;
    private int page = 1;
    private int total;
    private CoreActivity activity;
    private List<ServiceModel> services;
    private AddressModel.Location location;
    private final static int LIMIT = 20;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
    }

    private View.OnClickListener filterClicked = (View v) -> {
        View root = getLayoutInflater().inflate(R.layout.filter_home_dialog, null);
        Dialog dialog = new Dialog(getContext());
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        View close = root.findViewById(R.id.close);

        close.setOnClickListener((View v1) -> {
            dialog.dismiss();
        });

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.setContentView(root);
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        rv = root.findViewById(R.id.rv);
        empty = root.findViewById(R.id.empty);
        loader = root.findViewById(R.id.loader);
        filter = root.findViewById(R.id.filter);
        filter.setOnClickListener(filterClicked);
        init();
        return root;
    }

    private void init() {
        checkLocationPermissions();
    }

    private void checkLocationPermissions() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, Defs.PermissionCode.LOCATION_CODE);
        }
        else {
            getLocation();
        }
    }

    private void getLocation() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient providerClient = LocationServices.getFusedLocationProviderClient(getContext());
            providerClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnCompleteListener((Task< Location > task) -> {
                        if(task.isSuccessful()) {
                            if(location == null) {
                                location = new AddressModel.Location();
                            }
                            location.setLongitude(task.getResult().getLongitude());
                            location.setLatitude(task.getResult().getLatitude());
                        }
                        load();
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == Defs.PermissionCode.LOCATION_CODE && grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }

    private FilterQueryOptions getFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        return new FilterQueryOptions(page, LIMIT, items);
    }

    private void load() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterServices(getFilterQueryOptions()));
        });
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<ServiceModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<ServiceModel>> call, Response<FilterResponse<ServiceModel>> response) {
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
                        if(services == null) {
                            services = new ArrayList<>();
                        }
                        services.addAll(response.body().getData());
                        if(rv.getLayoutManager() == null) {
                            rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        }
                        if(rv.getAdapter() == null) {
                            ServiceAdapter adapter = new ServiceAdapter(services);
                            if(location != null) {
                                adapter.setCurrentLocation(location);
                            }
                            adapter.setOnServiceItemClickedListener((ServiceModel service) -> {
                                ServiceFragment serviceFragment = new ServiceFragment();
                                serviceFragment.setService(service);
                                serviceFragment.setOnBackPressedListener(activity);
                                activity.loadExtra(serviceFragment);
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
    }
}