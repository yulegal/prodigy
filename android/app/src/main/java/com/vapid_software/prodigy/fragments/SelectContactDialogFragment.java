package com.vapid_software.prodigy.fragments;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.adapters.ContactAdapter;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.models.UserModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SelectContactDialogFragment extends BaseExtraFragment {
    private View empty, loader;
    private RecyclerView rv;
    private int page = 1;
    private int total;
    private List<UserModel> contacts;
    private String title, searchText;
    private SearchFragment searchFragment;
    private TextView titleView;
    private OnContactSelectedListener onContactSelectedListener;
    private final static int LIMIT = 20;

    public interface OnContactSelectedListener {
        void onContactSelected(UserModel contact);
    }

    public void setOnContactSelectedListener(OnContactSelectedListener onContactSelectedListener) {
        this.onContactSelectedListener = onContactSelectedListener;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_select_contact_dialog, container, false);
        back = root.findViewById(R.id.close);
        titleView = root.findViewById(R.id.title);
        rv = root.findViewById(R.id.rv);
        loader = root.findViewById(R.id.loader);
        empty = root.findViewById(R.id.empty);
        init();
        return root;
    }

    private SearchFragment.OnSearchChangeListener onSearchChangeListener = (String text) -> {
        searchText = text;
        page = 1;
        contacts = null;
        load();
    };

    @Override
    protected void init() {
        super.init();
        searchFragment = (SearchFragment) getChildFragmentManager().findFragmentById(R.id.search_fragment);
        if(title != null) {
            titleView.setText(title);
        }
        searchFragment.setOnSearchChangeListener(onSearchChangeListener);
        load();
    }

    private FilterQueryOptions getFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        if(searchText != null) {
            items.add(new FilterItem("name", searchText));
        }
        return new FilterQueryOptions(page, LIMIT, items);
    }

    private void load() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<UserModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<UserModel>> call, Response<FilterResponse<UserModel>> response) {
                if(response.code() == 201) {
                    total = response.body().getCount();
                    loader.setVisibility(View.GONE);
                    if(total == 0 && response.body().getTotal() == 0) {
                        empty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    }
                    else {
                        empty.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        if(contacts == null) {
                            contacts = new ArrayList<>();
                        }
                        contacts.addAll(response.body().getData());
                        if(rv.getLayoutManager() == null) {
                            rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        }
                        if(rv.getAdapter() == null || page == 1) {
                            ContactAdapter adapter = new ContactAdapter(contacts);
                            adapter.setOnContactClickedListener((UserModel contact) -> {
                                if(onContactSelectedListener != null) {
                                    onContactSelectedListener.onContactSelected(contact);
                                }
                            });
                            rv.setAdapter(adapter);
                        }
                        else {
                            rv.getAdapter().notifyItemRangeInserted(contacts.size() - response.body().getTotal(), response.body().getTotal());
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterContacts(getFilterQueryOptions()));
        });
    }
}