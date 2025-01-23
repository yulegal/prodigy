package com.vapid_software.prodigy.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.LoginActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.adapters.BookingAdapter;
import com.vapid_software.prodigy.adapters.SimpleArrayAdapter;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.BookingDialog;
import com.vapid_software.prodigy.helpers.ConfirmDialog;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.FilterDialog;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.models.BookingModel;
import com.vapid_software.prodigy.models.UserModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BookingsFragment extends Fragment {
    private View loader, empty, container, filter;
    private RecyclerView rv;
    private int page = 1;
    private int total;
    private Spinner type;
    private List<BookingModel> bookings;
    private long filterDateFrom, filterDateTo;
    private CoreActivity activity;
    private UserModel loggedUser;
    private final static int LIMIT = 20;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
        loggedUser = activity.getLoggedUser();
    }

    private View.OnClickListener filterClicked = (View v) -> {
        FilterDialog filterDialog = new FilterDialog(getContext(), filterDateFrom, filterDateTo);
        Calendar c = Calendar.getInstance();
        filterDialog.setOnFilterClearListener(() -> {
            bookings = null;
            page = 1;
            filterDateTo = 0;
            filterDateFrom = 0;
            load();
            filterDialog.dismiss();
        });
        filterDialog.setOnFilterUseListener(() -> {
            bookings = null;
            page = 1;
            load();
        });
        filterDialog.setOnDateSelectedListener((long dateFrom, long dateTo) -> {
            filterDateFrom = dateFrom;
            filterDateTo = dateTo;
        });
        filterDialog.setOnFromDatePickListener((DatePicker datePicker, long dateFrom, long to) -> {
            datePicker.setMinDate(c.getTimeInMillis());
        });
        filterDialog.setOnToDatePickListener((DatePicker datePicker, long dateFrom, long to) -> {
            datePicker.setMinDate(filterDateFrom == 0 ? c.getTimeInMillis() : filterDateFrom);
        });
        filterDialog.show();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bookings, container, false);
        this.container = root.findViewById(R.id.container);
        empty = root.findViewById(R.id.empty);
        loader = root.findViewById(R.id.loader);
        type = root.findViewById(R.id.type);
        filter = root.findViewById(R.id.filter);
        rv = root.findViewById(R.id.rv);
        filter.setOnClickListener(filterClicked);
        init();
        return root;
    }

    private FilterQueryOptions getFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        items.add(new FilterItem("status", Defs.BookingStatus.ACTIVE));
        if(loggedUser.getRole().getId().equals(Defs.Role.PROVIDER) || loggedUser.getRole().getId().equals(Defs.Role.HELPER)) {
            if(type.getSelectedItemPosition() == 0) {
                items.add(new FilterItem("type", "me"));
            }
            else {
                items.add(new FilterItem("type", "clients"));
            }
        }
        else {
            items.add(new FilterItem("user", loggedUser.getId()));
        }
        if(filterDateTo != 0) {
            items.add(new FilterItem("dateTo", String.valueOf(filterDateTo)));
        }
        if(filterDateFrom != 0) {
            items.add(new FilterItem("dateFrom", String.valueOf(filterDateFrom)));
        }
        return new FilterQueryOptions(page, LIMIT, items);
    }

    private void init() {
        if(loggedUser.getRole().getId().equals(Defs.Role.PROVIDER) || loggedUser.getRole().getId().equals(Defs.Role.HELPER)) {
            type.setAdapter(new SimpleArrayAdapter(getContext(), getContext().getResources().getStringArray(R.array.booking_types)));
            type.setVisibility(View.VISIBLE);
            type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    page = 1;
                    bookings = null;
                    load();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(!recyclerView.canScrollVertically(1) && total != 0 && total != bookings.size()) {
                    ++page;
                    load();
                }
            }
        });
        load();
    }

    private void load() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<BookingModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<BookingModel>> call, Response<FilterResponse<BookingModel>> response) {
                if(response.code() == 201) {
                    total = response.body().getCount();
                    loader.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);
                    if(response.body().getTotal() == 0 && total == 0) {
                        empty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    }
                    else {
                        empty.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        if(bookings == null) {
                            bookings = new ArrayList<>();
                        }
                        bookings.addAll(response.body().getData());
                        if(rv.getLayoutManager() == null) {
                            rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        }
                        if(rv.getAdapter() == null || page == 1) {
                            BookingAdapter adapter = new BookingAdapter(bookings);
                            adapter.setOnBookingClickedListener((BookingModel booking) -> {
                                BookingDialog dialog = new BookingDialog(getContext(), booking);
                                dialog.setOnBookingCanceledListener(() -> {
                                    dialog.dismiss();
                                    ConfirmDialog confirmDialog = new ConfirmDialog(
                                            getContext(),
                                            getContext().getResources().getString(R.string.booking_cancel_confirm_body),
                                            getContext().getResources().getString(R.string.booking_cancel_confirm_title),
                                            null,
                                            null
                                    );
                                    confirmDialog.setOnPositiveConfirmListener(() -> {
                                        cancelBooking(booking);
                                        confirmDialog.dismiss();
                                    });
                                    confirmDialog.show();
                                });
                                dialog.setOnBookingDoneListener(() -> {
                                    dialog.dismiss();
                                    ConfirmDialog confirmDialog = new ConfirmDialog(
                                            getContext(),
                                            getContext().getResources().getString(R.string.booking_done_confirm_body),
                                            getContext().getResources().getString(R.string.booking_done_confirm_title),
                                            null,
                                            null
                                    );
                                    confirmDialog.setOnPositiveConfirmListener(() -> {
                                        finishBooking(booking);
                                        confirmDialog.dismiss();
                                    });
                                    confirmDialog.show();
                                });
                                dialog.show();
                            });
                            rv.setAdapter(adapter);
                        }
                        else {
                            rv.getAdapter().notifyItemRangeInserted(bookings.size() - response.body().getTotal(), response.body().getTotal());
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterBookings(getFilterQueryOptions()));
        });
    }

    private void finishBooking(BookingModel booking) {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).finishBooking(booking.getId()));
        });
        builder.setResponseListener(new ApiBuilder.ResponseListener<BookingModel>() {
            @Override
            public void onResponse(Call<BookingModel> call, Response<BookingModel> response) {
                if(response.code() == 200) {
                    int i = bookings.indexOf(booking);
                    bookings.remove(i);
                    rv.getAdapter().notifyItemRemoved(i);
                    if(bookings.size() == 0) {
                        rv.setVisibility(View.GONE);
                        empty.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.finished_booking), Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cancelBooking(BookingModel booking) {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).cancelBooking(booking.getId()));
        });
        builder.setResponseListener(new ApiBuilder.ResponseListener<BookingModel>() {
            @Override
            public void onResponse(Call<BookingModel> call, Response<BookingModel> response) {
                if(response.code() == 200) {
                    int i = bookings.indexOf(booking);
                    bookings.remove(i);
                    rv.getAdapter().notifyItemRemoved(i);
                    if(bookings.size() == 0) {
                        rv.setVisibility(View.GONE);
                        empty.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.booking_canceled), Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}