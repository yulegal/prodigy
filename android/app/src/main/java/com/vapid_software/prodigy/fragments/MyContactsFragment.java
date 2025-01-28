package com.vapid_software.prodigy.fragments;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.adapters.ContactAdapter;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.helpers.ViewHolder;
import com.vapid_software.prodigy.models.ContactModel;
import com.vapid_software.prodigy.models.UserHandleContactModel;
import com.vapid_software.prodigy.models.UserModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MyContactsFragment extends BaseExtraFragment {
    private CoreActivity activity;
    private View accessWrp, giveBtn, loader, empty;
    private RecyclerView rv;
    private List<UserModel> contacts;
    private int page = 1;
    private String searchText;
    private int total;
    private SearchFragment searchFragment;
    private final static int LIMIT = 20;

    private final static String[] CONTACT_PROJECTIONS = {
            ContactsContract.Contacts._ID,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
    };

    private class HandleContacts extends AsyncTask<Void, Void, List<ContactModel>> {
        @Override
        protected List<ContactModel> doInBackground(Void... voids) {
            ContentResolver cr = activity.getContentResolver();
            Cursor crCursor = cr.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    CONTACT_PROJECTIONS,
                    null,
                    null,
                    null
            );
            List<ContactModel> contacts = new ArrayList<>();
            while(crCursor.moveToNext() && crCursor.getInt(2) > 0) {
                Cursor c = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{ crCursor.getString(0) },
                        null
                );
                if(c.moveToFirst()) {
                    int i = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    if(i < 0) continue;
                    contacts.add(new ContactModel(crCursor.getString(1), c.getString(i).replaceAll("\\s+", "")));
                }
                c.close();
            }
            crCursor.close();
            return contacts;
        }

        @Override
        protected void onPostExecute(List<ContactModel> contactModels) {
            if(contactModels.size() != 0) {
                ApiBuilder builder = ApiBuilder.getInstance(getContext());
                builder.setOnInitializedListener(() -> {
                    builder.send(builder.getApi(ApiService.class).addContacts(new UserHandleContactModel(contactModels)));
                });
                builder.setResponseListener(new ApiBuilder.ResponseListener() {
                    @Override
                    public void onResponse(Call call, Response response) {
                        if(response.code() == 201) {
                            load();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
    }

    private void validatePermissions() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, Defs.PermissionCode.CONTACTS_CODE);
        }
        else {
            new HandleContacts().execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == Defs.PermissionCode.CONTACTS_CODE) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new HandleContacts().execute();
            }
            else {
                accessWrp.setVisibility(View.VISIBLE);
            }
            loader.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener giveBtnClicked = (View v) -> {
        validatePermissions();
    };

    private SearchFragment.OnSearchChangeListener onSearchChangeListener = (String text) -> {
        searchText = text;
        page = 1;
        contacts = null;
        rv.setAdapter(null);
        load();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_contacts, container, false);
        accessWrp = root.findViewById(R.id.access_wrp);
        back = root.findViewById(R.id.back);
        searchFragment = (SearchFragment) getChildFragmentManager().findFragmentById(R.id.search_fragment);
        loader = root.findViewById(R.id.loader);
        rv = root.findViewById(R.id.rv);
        empty = root.findViewById(R.id.empty);
        giveBtn = root.findViewById(R.id.give_btn);
        validatePermissions();
        giveBtn.setOnClickListener(giveBtnClicked);
        init();
        return root;
    }

    @Override
    protected void init() {
        super.init();
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(!recyclerView.canScrollVertically(1) && total != 0 && total != contacts.size()) {
                    ++page;
                    load();
                }
            }
        });
        searchFragment.setOnSearchChangeListener(onSearchChangeListener);
    }

    private FilterQueryOptions getFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
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
                    accessWrp.setVisibility(View.GONE);
                    if(response.body().getTotal() == 0 && total == 0) {
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
                                ChatFragment fragment = new ChatFragment();
                                fragment.setCurrentUser(contact);
                                fragment.setOnBackPressedListener(() -> {
                                    activity.getSupportFragmentManager().popBackStack();
                                });
                                activity.loadExtra(fragment, true);
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