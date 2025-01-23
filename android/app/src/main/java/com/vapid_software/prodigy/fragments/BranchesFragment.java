package com.vapid_software.prodigy.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.ConfirmDialog;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.helpers.ViewHolder;
import com.vapid_software.prodigy.models.BranchModel;
import com.vapid_software.prodigy.models.ServiceModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BranchesFragment extends BaseExtraFragment {
    private ServiceModel service;
    private View loader, empty, close, add;
    private RecyclerView rv;
    private int page = 1;
    private int total;
    private List<BranchModel> branches;
    private CoreActivity activity;
    private BranchModel currentBranch;
    private final static int LIMIT = 20;

    private class Adapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.branch_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            View root = holder.itemView;
            TextView address = root.findViewById(R.id.address);
            View close = root.findViewById(R.id.close);
            BranchModel branch = branches.get(holder.getAdapterPosition());
            if(branch.getAddress() != null) {
                address.setText(branch.getAddress().getAddress());
            }
            close.setOnClickListener((View v) -> {
                ConfirmDialog dialog = new ConfirmDialog(getContext(), getContext().getResources().getString(R.string.remove_branch_confirm_body), getContext().getResources().getString(R.string.remove_branch_confirm_title), null, null);
                dialog.setOnPositiveConfirmListener(() -> {
                    ApiBuilder builder = ApiBuilder.getInstance(getContext());
                    builder.setOnInitializedListener(() -> {
                        builder.send(builder.getApi(ApiService.class).deleteBranchById(branch.getId()));
                    });
                    builder.setResponseListener(new ApiBuilder.ResponseListener<BranchModel>() {
                        @Override
                        public void onResponse(Call<BranchModel> call, Response<BranchModel> response) {
                            if(response.code() == 200) {
                                if(currentBranch == branch) {
                                    currentBranch = null;
                                }
                                branches.remove(branch);
                                notifyItemRemoved(holder.getAdapterPosition());
                                if(branches.size() == 0) {
                                    rv.setVisibility(View.GONE);
                                    empty.setVisibility(View.VISIBLE);
                                }
                                dialog.dismiss();
                            }
                            else {
                                Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                });
                dialog.show();
            });
            root.setOnClickListener((View v) -> {
                currentBranch = branch;
                add.performClick();
            });
        }

        @Override
        public int getItemCount() {
            return branches.size();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
    }

    public void setService(ServiceModel service) {
        this.service = service;
    }

    private View.OnClickListener addClicked = (View v) -> {
        BranchAddFragment fragment = new BranchAddFragment();
        fragment.setService(service);
        fragment.setCurrentBranch(currentBranch);
        fragment.setOnBranchAddedListener((BranchModel branch) -> {
            page = 1;
            if (branches != null) {
                branches = null;
            }
            load();
        });
        activity.loadExtra(fragment, true);
    };

    private View.OnClickListener closeClicked = (View v) -> {
        if(onBackPressedListener != null) {
            onBackPressedListener.OnExtraBackPressed();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_branches, container, false);
        empty = root.findViewById(R.id.empty);
        rv = root.findViewById(R.id.rv);
        add = root.findViewById(R.id.add);
        close = root.findViewById(R.id.close);
        loader = root.findViewById(R.id.loader);
        add.setOnClickListener(addClicked);
        close.setOnClickListener(closeClicked);
        init();
        return root;
    }

    protected void init() {
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(!recyclerView.canScrollVertically(1) && total != 0 && branches.size() != total) {
                    ++page;
                    load();
                }
            }
        });
        if(branches != null) {
            branches = null;
            page = 1;
        }
        load();
    }

    private FilterQueryOptions getFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        items.add(new FilterItem("service", service.getId()));
        return new FilterQueryOptions(page, LIMIT, items);
    }

    private void load() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<BranchModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<BranchModel>> call, Response<FilterResponse<BranchModel>> response) {
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
                        if(branches == null) {
                            branches = new ArrayList<>();
                        }
                        branches.addAll(response.body().getData());
                        if(rv.getLayoutManager() == null) {
                            LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                            rv.addItemDecoration(new DividerItemDecoration(getContext(), lm.getOrientation()));
                            rv.setLayoutManager(lm);
                        }
                        if(rv.getAdapter() == null || page == 1) {
                            rv.setAdapter(new Adapter());
                        }
                        else {
                            rv.getAdapter().notifyItemRangeInserted(branches.size() - response.body().getTotal(), response.body().getTotal());
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterBranches(getFilterQueryOptions()));
        });
    }
}