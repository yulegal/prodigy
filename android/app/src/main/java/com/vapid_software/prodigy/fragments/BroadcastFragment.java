package com.vapid_software.prodigy.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.adapters.SimpleArrayAdapter;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.helpers.ResponseError;
import com.vapid_software.prodigy.models.BranchModel;
import com.vapid_software.prodigy.models.BroadcastCreateModel;
import com.vapid_software.prodigy.models.UserModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BroadcastFragment extends BaseExtraFragment {
    private View button, date, dateWrp;
    private EditText message, dateValue;
    private Spinner action, branches;
    private long selectedDate;
    private CoreActivity activity;
    private UserModel loggedUser;
    private String branchId;
    private boolean clickable = true;
    private TextView error;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
        loggedUser = activity.getLoggedUser();
    }

    private View.OnClickListener dateClicked = (View v) -> {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                R.style.TimePickerDialogStyle,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar c1 = Calendar.getInstance();
                    c1.set(Calendar.YEAR, year);
                    c1.set(Calendar.MONTH, month);
                    c1.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selectedDate = c1.getTimeInMillis();
                    dateValue.setText(String.join(".", String.valueOf(dayOfMonth), String.valueOf(month), String.valueOf(year)));
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(c.getTimeInMillis());
        dialog.show();
    };

    private View.OnClickListener buttonClicked = (View v) -> {
        if(!clickable) return;
        clickable = false;
        String msg = message.getText().toString().trim();
        int actionPos = action.getSelectedItemPosition();
        error.setVisibility(View.GONE);
        message.setBackgroundResource(R.drawable.standard_input);
        dateWrp.setBackgroundResource(R.drawable.standard_input);
        action.setBackgroundResource(R.drawable.standard_input);
        if(msg.isEmpty() || actionPos == 0 || selectedDate == 0) {
            error.setVisibility(View.VISIBLE);
            error.setText(getContext().getResources().getString(R.string.fill_in_all_fields));
            if(msg.isEmpty()) {
                message.setBackgroundResource(R.drawable.standard_input_e);
            }
            if(actionPos == 0) {
                action.setBackgroundResource(R.drawable.standard_input_e);
            }
            if(selectedDate == 0) {
                dateWrp.setBackgroundResource(R.drawable.standard_input_e);
            }
            clickable = true;
            return;
        }
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener() {
            @Override
            public void onResponse(Call call, Response response) {
                if(response.code() == 201) {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.broadcast_done), Toast.LENGTH_LONG).show();
                    back.performClick();
                }
                else if(response.code() == 400) {
                    try {
                        JSONObject payload = new JSONObject(response.errorBody().string());
                        String message = payload.getString("message");
                        if(message.equals(ResponseError.error_empty_bookings)) {
                            error.setVisibility(View.VISIBLE);
                            error.setText(getContext().getResources().getString(R.string.error_empty_bookings));
                        }
                        else {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                        }
                    }
                    catch(Exception e) {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
                clickable = true;
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).broadcast(new BroadcastCreateModel(msg, selectedDate, getAction(), branchId)));
        });
    };

    private String getAction() {
        switch(action.getSelectedItemPosition()) {
            case 1: return Defs.BroadcastActions.CANCEL_BOOKING;
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_broadcast, container, false);
        back = root.findViewById(R.id.back);
        dateWrp = root.findViewById(R.id.date_wrp);
        date = root.findViewById(R.id.date);
        button = root.findViewById(R.id.button);
        error = root.findViewById(R.id.error);
        branches = root.findViewById(R.id.branches);
        message = root.findViewById(R.id.message);
        dateValue = root.findViewById(R.id.date_value);
        action = root.findViewById(R.id.action);
        date.setOnClickListener(dateClicked);
        button.setOnClickListener(buttonClicked);
        action.setAdapter(new SimpleArrayAdapter(getContext(), getContext().getResources().getStringArray(R.array.broadcast_actions)));
        init();
        return root;
    }

    private FilterQueryOptions getBranchesFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        items.add(new FilterItem("serviceOwner", loggedUser.getId()));
        return new FilterQueryOptions(items);
    }

    private void loadBranches() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<BranchModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<BranchModel>> call, Response<FilterResponse<BranchModel>> response) {
                if(response.code() == 201) {
                    if(response.body().getCount() != 0) {
                        branches.setVisibility(View.VISIBLE);
                        List<String> listItems = new ArrayList<>();
                        listItems.add(getContext().getResources().getString(R.string.select_branch_option));
                        for(BranchModel branch : response.body().getData()) {
                            listItems.add(branch.getAddress().getAddress());
                        }
                        String items[] = new String[listItems.size()];
                        listItems.toArray(items);
                        branches.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                branchId = position == 0 ? null : response.body().getData().get(position - 1).getId();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                        branches.setAdapter(new SimpleArrayAdapter(getContext(), items));
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterBranches(getBranchesFilterQueryOptions()));
        });
    }
}