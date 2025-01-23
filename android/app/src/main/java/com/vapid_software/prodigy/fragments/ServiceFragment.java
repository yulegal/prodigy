package com.vapid_software.prodigy.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.adapters.RatingAdapter;
import com.vapid_software.prodigy.adapters.SimpleArrayAdapter;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.data.BookingExtraData;
import com.vapid_software.prodigy.data.ServiceExtraData;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.helpers.ResponseError;
import com.vapid_software.prodigy.helpers.ViewHolder;
import com.vapid_software.prodigy.models.BookingCreateModel;
import com.vapid_software.prodigy.models.BookingModel;
import com.vapid_software.prodigy.models.BranchModel;
import com.vapid_software.prodigy.models.RatingModel;
import com.vapid_software.prodigy.models.RebookModel;
import com.vapid_software.prodigy.models.ServiceModel;
import com.vapid_software.prodigy.models.UserModel;
import com.vapid_software.prodigy.models.WorkScheduleModel;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ServiceFragment extends BaseExtraFragment {
    private View timeBtn, rateBtn, wrapper, loader;
    private CoreActivity activity;
    private RecyclerView ratingRv;
    private TextView selectedTime, name;
    private ImageView avatar, add2fav;
    private BranchModel branch;
    private Button selectBranchBtn, button;
    private UserModel loggedUser;
    private RecyclerView scheduleRv;
    private ServiceModel service;
    private TextView addressLink, selectedBranchAddress;
    private List<BranchModel> branches;
    private Calendar gc = Calendar.getInstance();
    private View dateBtn, extraWrp, bookedWrp, galleryBtn;
    private TextView averageDuration;
    private boolean timePicked;
    private BookingExtraData extraData;
    private Fragment extraFragment;
    private BookingModel booking;
    private RecyclerView bookedRv;
    private Toolbar tb;

    private View address, selectTimeWrp, bottomWrp, selectedBranchWrp, removeBranch;

    private class ScheduleAdapter extends RecyclerView.Adapter {
        private List<WorkScheduleModel> schedules;
        private Calendar c;

        public ScheduleAdapter(List<WorkScheduleModel> schedules) {
            this.schedules = schedules;
            c = Calendar.getInstance();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.schedule_show_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            View root = holder.itemView;
            TextView name = root.findViewById(R.id.name);
            TextView time = root.findViewById(R.id.time);
            WorkScheduleModel ws = branch != null ? branch.getWorkSchedule().get(position) : service.getWorkSchedule().get(position);
            if(!ws.isAllDay()) {
                long h = (ws.getStartTime() / 1000) / 3600;
                long m = (ws.getStartTime() / 1000) / 60 % 60;
                String s = String.join(":", String.valueOf(h < 10 ? "0" + h : h), String.valueOf(m < 10 ? "0" + m : m));
                h =  (ws.getEndTime() / 1000) / 3600;
                m = (ws.getEndTime() / 1000) / 60 % 60;
                String e = String.join(":", String.valueOf(h < 10 ? "0" + h : h), String.valueOf(m < 10 ? "0" + m : m));
                time.setText(String.join(" - ", s, e));
            }
            else {
                time.setText(getContext().getResources().getString(R.string.all_day));
                time.setTextColor(Color.parseColor("#458B00"));
            }
            if(c.get(Calendar.DAY_OF_WEEK) == Defs.WeekDay.getWeekdayDatePositionByName(ws.getWeekDay())) {
                root.setBackgroundColor(getContext().getResources().getColor(R.color.current_schedule_bg));
                name.setTextColor(Color.parseColor("#ffffff"));
                if(!ws.isAllDay()) {
                    time.setTextColor(Color.parseColor("#ffffff"));
                }
            }
            name.setText(Defs.WeekDay.getWeekDayTranslation(getContext(), ws.getWeekDay()));
        }

        @Override
        public int getItemCount() {
            return schedules.size();
        }
    }

    private interface OnBranchSelectedListener {
        void onBranchSelected(BranchModel branch);
    }

    private class BranchAdapter extends RecyclerView.Adapter {
        private OnBranchSelectedListener onBranchSelectedListener;

        public void setOnBranchSelectedListener(OnBranchSelectedListener onBranchSelectedListener) {
            this.onBranchSelectedListener = onBranchSelectedListener;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.service_branch_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView tv = (TextView) holder.itemView;
            BranchModel branch = branches.get(position);
            tv.setText(branch.getAddress().getAddress());
            tv.setOnClickListener((View v) -> {
                if(onBranchSelectedListener != null) {
                    onBranchSelectedListener.onBranchSelected(branch);
                }
            });
        }

        @Override
        public int getItemCount() {
            return branches.size();
        }
    }

    private class BookingAdapter extends RecyclerView.Adapter {
        private List<BookingModel> bookings;

        public BookingAdapter(List<BookingModel> bookings) {
            this.bookings = bookings;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.booking_service_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            View root = holder.itemView;
            TextView time = root.findViewById(R.id.time);
            BookingModel model = bookings.get(position);
            TextView status = root.findViewById(R.id.status);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            time.setText(dateFormat.format(new Date(model.getBookDate())));
            status.setText(Defs.BookingStatus.getTranslationByType(getContext(), model.getStatus()));
            status.setTextColor(Defs.BookingStatus.getStatusColorByType(model.getStatus()));
        }

        @Override
        public int getItemCount() {
            return bookings.size();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
        loggedUser = activity.getLoggedUser();
    }

    public void setService(ServiceModel service) {
        this.service = service;
    }

    public void setBooking(BookingModel booking) {
        this.booking = booking;
    }

    private View.OnClickListener timeClicked = (View v) -> {
        Calendar c = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(getContext(), R.style.TimePickerDialogStyle, (TimePicker view, int hourOfDay, int minute) -> {
            Calendar c1 = Calendar.getInstance();
            c1.setTimeInMillis(gc.getTimeInMillis());
            c1.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c1.set(Calendar.MINUTE, minute);
            if(c1.getTimeInMillis() - c.getTimeInMillis() < 0) {
                timePicked = false;
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.invalid_time), Toast.LENGTH_LONG).show();
                return;
            }
            gc.set(Calendar.HOUR_OF_DAY, hourOfDay);
            gc.set(Calendar.MINUTE, minute);
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY HH:mm");
            selectedTime.setText(String.join(": ", getContext().getResources().getString(R.string.book_date), format.format(gc.getTime())));
            selectedTime.setVisibility(View.VISIBLE);
            timePicked = true;
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        dialog.show();
    };

    private View.OnClickListener rateClicked = (View v) -> {
        Dialog dialog = new Dialog(getContext());
        View root = getLayoutInflater().inflate(R.layout.rate_dialog, null);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        View close = root.findViewById(R.id.close);
        RecyclerView rv = root.findViewById(R.id.rv);
        View button = root.findViewById(R.id.button);

        RatingAdapter adapter = new RatingAdapter(branch != null ? branch.getRating() : service.getRating());
        adapter.setOnRatingSelectedListener((int position) -> {
            adapter.setRating(position);
            adapter.notifyDataSetChanged();
            button.setVisibility(View.VISIBLE);
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);

        button.setOnClickListener((View view) -> {
            ApiBuilder builder = ApiBuilder.getInstance(getContext());
            builder.setOnInitializedListener(() -> {
                if(branch == null) {
                    builder.send(builder.getApi(ApiService.class).rateService(new RatingModel(service.getId(), adapter.getRating())));
                }
                else {
                    builder.send(builder.getApi(ApiService.class).rateBranch(new RatingModel(branch.getId(), adapter.getRating())));
                }
            });
            builder.setResponseListener(new ApiBuilder.ResponseListener<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if(response.code() == 201) {
                        ((RatingAdapter)ratingRv.getAdapter()).setRating(response.body());
                        if(branch == null) {
                            service.setRating(response.body());
                        }
                        else {
                            branch.setRating(response.body());
                        }
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.thanks_for_rating), Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        ratingRv.getAdapter().notifyDataSetChanged();
                    }
                    else {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                }
            });
        });

        lp.width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                300,
                getContext().getResources().getDisplayMetrics()
        );
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        close.setOnClickListener((View view) -> {
            dialog.dismiss();
        });

        dialog.setContentView(root);
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    };

    private View.OnClickListener buttonClicked = (View v) -> {
        if(!timePicked) {
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.select_booking_date), Toast.LENGTH_LONG).show();
            return;
        }
        if(
                (branch == null && service.getCategory().getType().equals(Defs.CategoryType.CAFE) && extraData == null) ||
                        (branch != null && branch.getService().getCategory().getType().equals(Defs.CategoryType.CAFE) && extraData == null)
        ) {
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_specify_table), Toast.LENGTH_LONG).show();
            return;
        }
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<BookingModel>() {
            @Override
            public void onResponse(Call<BookingModel> call, Response<BookingModel> response) {
                if(response.code() == 201) {
                    Toast.makeText(getContext(), getContext().getResources().getString(booking == null ? R.string.booked_msg : R.string.successfully_rebooked), Toast.LENGTH_LONG).show();
                    back.performClick();
                }
                else if(response.code() == 400) {
                    try {
                        JSONObject payload = new JSONObject(response.errorBody().string());
                        String message = payload.getString("message");
                        if(message.equals(ResponseError.error_booking_date_busy)) {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.book_date_is_busy), Toast.LENGTH_LONG).show();
                        }
                        else if(message.equals(ResponseError.error_invalid_book_time)) {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.invalid_book_time), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            if(booking == null) {
                builder.send(builder.getApi(ApiService.class).book(new BookingCreateModel(service.getId(), gc.getTimeInMillis(), branch == null ? null : branch.getId(), extraData)));
            }
            else {
                builder.send(builder.getApi(ApiService.class).rebook(new RebookModel(service.getId(), gc.getTimeInMillis(), branch == null ? null : branch.getId(), extraData, booking.getId())));
            }
        });
    };

    private View.OnClickListener add2FavoritesClicked = (View v) -> {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).toggleFavorites(service.getId()));
        });
        builder.setResponseListener(new ApiBuilder.ResponseListener<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(response.code() == 200) {
                    if(response.body() == 0) {
                        add2fav.setImageResource(R.drawable.favorite_outline);
                    }
                    else {
                        add2fav.setImageResource(R.drawable.favorite);
                        add2fav.setColorFilter(Color.parseColor("#FFFF00"));
                    }
                    service.setAddedToFavorites(response.body() != 0);
                    Toast.makeText(getContext(), getContext().getResources().getString(response.body() == 0 ? R.string.removed_from_favorites : R.string.added_to_favorites), Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
    };

    private View.OnClickListener addressClicked = (View v) -> {
        if(service.getAddress().getUrl() != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(service.getAddress().getUrl()));
            startActivity(intent);
        }
    };

    private View.OnClickListener selectBranchClicked = (View v) -> {
        View root = getLayoutInflater().inflate(R.layout.service_branches_dialog, null);
        RecyclerView rv = root.findViewById(R.id.rv);
        View close = root.findViewById(R.id.close);
        LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(lm);
        rv.addItemDecoration(new DividerItemDecoration(getContext(), lm.getOrientation()));
        PopupWindow win = new PopupWindow(root, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        close.setOnClickListener((View view) -> {
            win.dismiss();
        });
        BranchAdapter adapter = new BranchAdapter();
        adapter.setOnBranchSelectedListener((BranchModel branch) -> {
            this.branch = branch;
            selectedBranchAddress.setText(branch.getAddress().getAddress());
            selectedBranchWrp.setVisibility(View.VISIBLE);
            win.dismiss();
            scheduleRv.setAdapter(new ScheduleAdapter(branch.getWorkSchedule()));
            ratingRv.setAdapter(new RatingAdapter(branch.getRating()));
            addressLink.setText(branch.getAddress().getAddress());
            extraData = null;
            if(extraFragment != null) {
                if(extraFragment instanceof CafeServiceExtraSelectFragment) {
                    initCafeBookingOptions(branch.getExtra());
                }
            }
        });
        rv.setAdapter(adapter);
        win.showAtLocation(rv, Gravity.NO_GRAVITY, 0, 0);
    };

    private void initCafeBookingOptions(ServiceExtraData extra) {
        extraFragment = new CafeServiceExtraSelectFragment();
        List<String> items = new ArrayList<>();
        items.add(getContext().getResources().getString(R.string.select_table_option));

        if(extra != null) {
            if(extra.getTables() != null) {
                List<Integer> tables = extra.getTables();
                for(int i = 0;i < tables.size(); ++i) {
                    items.add(getContext().getResources().getString(R.string.table, i + 1, tables.get(i)));
                }
            }
        }
        String itms[] = new String[items.size()];
        items.toArray(itms);
        ((CafeServiceExtraSelectFragment) extraFragment).setAdapter(new SimpleArrayAdapter(getContext(), itms));
        ((CafeServiceExtraSelectFragment) extraFragment).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                extraData = position == 0 ? null : new BookingExtraData(position - 1, 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ((CafeServiceExtraSelectFragment) extraFragment).setOnCustomSeatsDefinedListener((int amount) -> {
            extraData = new BookingExtraData(-1, amount);
        });
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.extra_select_wrapper, extraFragment);
        transaction.commit();
    }

    private View.OnClickListener removeBranchClicked = (View v) -> {
        branch = null;
        selectedBranchWrp.setVisibility(View.GONE);
        scheduleRv.setAdapter(new ScheduleAdapter(service.getWorkSchedule()));
        ratingRv.setAdapter(new RatingAdapter(service.getRating()));
        addressLink.setText(service.getAddress().getAddress());
        extraData = null;
        initCafeBookingOptions(service.getExtra());
    };

    private View.OnClickListener dateBtnClicked = (View v) -> {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(getContext(), R.style.TimePickerDialogStyle, (DatePicker view, int year, int month, int dayOfMonth) -> {
            gc.set(Calendar.YEAR, year);
            gc.set(Calendar.MONTH, month);
            gc.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if(timePicked) {
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY HH:mm");
                selectedTime.setText(String.join(": ", getContext().getResources().getString(R.string.book_date), format.format(gc.getTime())));
                selectedTime.setVisibility(View.VISIBLE);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(c.getTimeInMillis());
        dialog.show();
    };

    private View.OnClickListener galleryBtnClicked = (View v) -> {
        GalleryFragment fragment = new GalleryFragment();
        fragment.setService(service);
        if(branch != null) {
            fragment.setBranch(branch);
        }
        fragment.setOnBackPressedListener(() -> {
            activity.getSupportFragmentManager().popBackStack();
        });
        activity.loadExtra(fragment, true);
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_service, container, false);
        bookedWrp = root.findViewById(R.id.booked_wrp);
        back = root.findViewById(R.id.back);
        bookedRv = root.findViewById(R.id.booked_rv);
        tb = root.findViewById(R.id.tb);
        rateBtn = root.findViewById(R.id.rate_btn);
        galleryBtn = root.findViewById(R.id.gallery_btn);
        button = root.findViewById(R.id.button);
        extraWrp = root.findViewById(R.id.extra_wrp);
        name = root.findViewById(R.id.name);
        dateBtn = root.findViewById(R.id.date_btn);
        selectBranchBtn = root.findViewById(R.id.select_branch_btn);
        add2fav = root.findViewById(R.id.add_to_fav);
        averageDuration = root.findViewById(R.id.average_duration);
        addressLink = root.findViewById(R.id.address_link);
        address = root.findViewById(R.id.address);
        avatar = root.findViewById(R.id.avatar);
        scheduleRv = root.findViewById(R.id.schedule_rv);
        loader = root.findViewById(R.id.loader);
        wrapper = root.findViewById(R.id.wrapper);
        selectedBranchWrp = root.findViewById(R.id.selected_branch_wrp);
        selectedBranchAddress = root.findViewById(R.id.selected_branch_address);
        removeBranch = root.findViewById(R.id.remove_branch);
        timeBtn = root.findViewById(R.id.time_btn);
        selectTimeWrp = root.findViewById(R.id.select_time_wrp);
        bottomWrp = root.findViewById(R.id.bottom_wrp);
        ratingRv = root.findViewById(R.id.rating_rv);
        selectedTime = root.findViewById(R.id.selected_time);
        removeBranch.setOnClickListener(removeBranchClicked);
        timeBtn.setOnClickListener(timeClicked);
        rateBtn.setOnClickListener(rateClicked);
        button.setOnClickListener(buttonClicked);
        add2fav.setOnClickListener(add2FavoritesClicked);
        addressLink.setOnClickListener(addressClicked);
        selectBranchBtn.setOnClickListener(selectBranchClicked);
        dateBtn.setOnClickListener(dateBtnClicked);
        galleryBtn.setOnClickListener(galleryBtnClicked);
        init();
        return root;
    }

    protected void init() {
        super.init();
        ratingRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        ratingRv.setAdapter(new RatingAdapter(service.getRating()));
        loader.setVisibility(View.GONE);
        wrapper.setVisibility(View.VISIBLE);
        name.setText(service.getName());
        if(service.getIcon() != null) {
            Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, service.getIcon())).into(avatar);
        }
        if(loggedUser != null) {
            selectTimeWrp.setVisibility(View.VISIBLE);
            bottomWrp.setVisibility(View.VISIBLE);
            if(service.isAddedToFavorites()) {
                add2fav.setImageResource(R.drawable.favorite);
                add2fav.setColorFilter(Color.parseColor("#FFFF00"));
            }
            else {
                add2fav.setImageResource(R.drawable.favorite_outline);
            }
        }
        averageDuration.setText(String.join(" ", String.valueOf(service.getAverageSession()), Defs.SessionUnit.getTranslations(getContext())[Defs.SessionUnit.UNITS.indexOf(service.getUnit())][1]));
        address.setVisibility(View.VISIBLE);
        addressLink.setText(service.getAddress().getAddress());
        scheduleRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        scheduleRv.setAdapter(new ScheduleAdapter(service.getWorkSchedule()));
        if(service.getCategory().getType().equals(Defs.CategoryType.CAFE)) {
            extraWrp.setVisibility(View.VISIBLE);
            initCafeBookingOptions(service.getExtra());
        }
        if(booking != null) {
            button.setText(getContext().getResources().getString(R.string.rebook_btn));
        }
        loadBranches();
        loadBookings();
    }

    private FilterQueryOptions getBookingsFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        items.add(new FilterItem("service", service.getId()));
        items.add(new FilterItem("status", Defs.BookingStatus.ACTIVE));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        items.add(new FilterItem("dateFrom", String.valueOf(c.getTimeInMillis())));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        items.add(new FilterItem("dateTo", String.valueOf(c.getTimeInMillis())));
        return new FilterQueryOptions(items);
    }

    private void loadBookings() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterBookings(getBookingsFilterQueryOptions()));
        });
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<BookingModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<BookingModel>> call, Response<FilterResponse<BookingModel>> response) {
                if(response.code() == 201) {
                    if(response.body().getCount() != 0) {
                        bookedWrp.setVisibility(View.VISIBLE);
                        bookedRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        bookedRv.setAdapter(new BookingAdapter(response.body().getData()));
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private FilterQueryOptions getBranchesFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        items.add(new FilterItem("service", service.getId()));
        return new FilterQueryOptions(items);
    }

    private void loadBranches() {
        if(loggedUser == null) return;
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<BranchModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<BranchModel>> call, Response<FilterResponse<BranchModel>> response) {
                if(response.code() == 201) {
                    if(response.body().getCount() != 0) {
                        selectBranchBtn.setVisibility(View.VISIBLE);
                        if(branches == null) {
                            branches = new ArrayList<>();
                        }
                        branches.addAll(response.body().getData());
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