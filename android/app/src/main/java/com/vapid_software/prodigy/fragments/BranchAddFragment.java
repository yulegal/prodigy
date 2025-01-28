package com.vapid_software.prodigy.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.qkopy.richlink.data.model.MetaData;
import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.data.ServiceExtraData;
import com.vapid_software.prodigy.helpers.AddAddressDialog;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.ResponseError;
import com.vapid_software.prodigy.helpers.ViewHolder;
import com.vapid_software.prodigy.helpers.WorkScheduleWindow;
import com.vapid_software.prodigy.models.AddressModel;
import com.vapid_software.prodigy.models.BranchCreateModel;
import com.vapid_software.prodigy.models.BranchModel;
import com.vapid_software.prodigy.models.BranchUpdateModel;
import com.vapid_software.prodigy.models.ServiceModel;
import com.vapid_software.prodigy.models.UserModel;
import com.vapid_software.prodigy.models.WorkScheduleModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BranchAddFragment extends BaseExtraFragment {
    private View close, extraWrp, usersWrp, addAddress, addressWrp;
    private RecyclerView usersRv;
    private EditText address, session;
    private Button button, galleryBtn, scheduleBtn, addUsersBtn;
    private TextView title, error;
    private Spinner unit;
    private ServiceModel service;
    private UserModel loggedUser;
    private BranchModel currentBranch;
    private boolean clickable = true;
    private boolean addUserClickable = true;
    private List<UserModel> users = new ArrayList<>();
    private WorkScheduleModel schedules[] = new WorkScheduleModel[7];
    private AddressModel addUrl;
    private Fragment extraFragment;
    private CoreActivity activity;
    private OnBranchAddedListener onBranchAddedListener;
    private List<String> cafeItems;
    private ServiceExtraData extra;
    private String[][] units;

    public interface OnBranchAddedListener {
        void onBranchAdded(BranchModel branch);
    }

    private class UnitAdapter extends ArrayAdapter {
        public UnitAdapter(Context context) {
            super(context, 0);
            units = Defs.SessionUnit.getTranslations(getContext());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView tv = (TextView) getLayoutInflater().inflate(R.layout.unit_item, parent, false);
            tv.setText(units[position][1]);
            return tv;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView tv = (TextView) getLayoutInflater().inflate(R.layout.unit_dd_item, parent, false);
            tv.setText(units[position][1]);
            return tv;
        }

        @Override
        public int getCount() {
            return units.length;
        }
    }

    private interface OnUserRemovedListener {
        void onUserRemoved(UserModel user);
    }

    private class UserAdapter extends RecyclerView.Adapter {
        private OnUserRemovedListener onUserRemovedListener;

        public void setOnUserRemovedListener(OnUserRemovedListener onUserRemovedListener) {
            this.onUserRemovedListener = onUserRemovedListener;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.add_user_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            View root = holder.itemView;
            ImageView avatar = root.findViewById(R.id.avatar);
            TextView name = root.findViewById(R.id.name);
            View close = root.findViewById(R.id.close);
            UserModel user = users.get(holder.getAdapterPosition());
            name.setText(user.getName());
            if(user.getIcon() != null) {
                Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, user.getIcon())).into(avatar);
            }
            close.setOnClickListener((View v) -> {
                users.remove(user);
                notifyItemRemoved(holder.getAdapterPosition());
                if(onUserRemovedListener != null) {
                    onUserRemovedListener.onUserRemoved(user);
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
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

    public void setCurrentBranch(BranchModel currentBranch) {
        this.currentBranch = currentBranch;
    }

    private View.OnClickListener closeClicked = (View v) -> {
        activity.getSupportFragmentManager().popBackStack();
        clickable = true;
        addUrl = null;
        currentBranch = null;
    };

    public void setOnBranchAddedListener(OnBranchAddedListener onBranchAddedListener) {
        this.onBranchAddedListener = onBranchAddedListener;
    }

    private View.OnClickListener addUserBtnClicked = (View v) -> {
        if(!addUserClickable) return;
        addUserClickable = false;
        Dialog dialog = new Dialog(getContext());
        View r = getLayoutInflater().inflate(R.layout.add_users_dialog, null);
        View textWrp = r.findViewById(R.id.text_wrp);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        View loader = r.findViewById(R.id.loader);
        View btn = r.findViewById(R.id.button);
        TextView e = r.findViewById(R.id.error);
        View c = r.findViewById(R.id.close);
        EditText number = r.findViewById(R.id.number);

        c.setOnClickListener((View v1) -> {
            dialog.dismiss();
            addUserClickable = true;
        });

        dialog.setOnDismissListener((DialogInterface d) -> {
            addUserClickable = true;
        });

        btn.setOnClickListener((View v1) -> {
            e.setVisibility(View.GONE);
            String numberValue = number.getText().toString();
            textWrp.setBackgroundResource(R.drawable.standard_input);
            if(numberValue.isEmpty()) {
                e.setVisibility(View.VISIBLE);
                e.setText(getContext().getResources().getString(R.string.fill_in_the_number));
                textWrp.setBackgroundResource(R.drawable.standard_input_e);
                addUserClickable = true;
                return;
            }
            loader.setVisibility(View.VISIBLE);
            ApiBuilder builder = ApiBuilder.getInstance(getContext());
            builder.setResponseListener(new ApiBuilder.ResponseListener<UserModel>() {
                @Override
                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                    if(response.code() == 200) {
                        dialog.dismiss();
                        usersWrp.setVisibility(View.VISIBLE);
                        if(usersRv.getLayoutManager() == null) {
                            usersRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        }
                        if(users == null) {
                            users = new ArrayList<>();
                        }
                        users.add(response.body());
                        UserAdapter adapter = new UserAdapter();
                        adapter.setOnUserRemovedListener((UserModel user) -> {
                            if(users.size() == 0) {
                                usersWrp.setVisibility(View.GONE);
                            }
                        });
                        usersRv.setAdapter(adapter);
                    }
                    else if(response.code() == 400) {
                        try {
                            JSONObject payload = new JSONObject(response.errorBody().string());
                            String message = payload.getString("message");
                            if(message.equals(ResponseError.error_user_not_found)) {
                                e.setVisibility(View.VISIBLE);
                                e.setText(getContext().getResources().getString(R.string.error_user_not_found));
                            }
                            else {
                                Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                    loader.setVisibility(View.GONE);
                    addUserClickable = true;
                }
            });
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).findUserByLogin("+996" + numberValue));
            });
        });

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.setContentView(r);
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    };

    private View.OnClickListener scheduleBtnClicked = (View v) -> {
        WorkScheduleWindow w = new WorkScheduleWindow(getContext(), schedules);
        w.show();
    };

    private View.OnClickListener buttonClicked = (View v) -> {
        if(!clickable) return;
        clickable = false;
        error.setVisibility(View.GONE);
        session.setBackgroundResource(R.drawable.standard_input);
        addressWrp.setBackgroundResource(R.drawable.standard_input);
        String sessionValue = session.getText().toString();
        if(addUrl == null || sessionValue.isEmpty()) {
            error.setVisibility(View.VISIBLE);
            error.setText(getContext().getResources().getString(R.string.fill_in_all_fields));
            if(addUrl == null) {
                addressWrp.setBackgroundResource(R.drawable.standard_input_e);
            }
            if(sessionValue.isEmpty()) {
                session.setBackgroundResource(R.drawable.standard_input_e);
            }
            clickable = true;
            return;
        }
        if(users.size() == 0) {
            clickable = true;
            error.setVisibility(View.VISIBLE);
            error.setText(getContext().getResources().getString(R.string.select_user_error));
            return;
        }
        List<WorkScheduleModel> wsList = new ArrayList<>();
        for(WorkScheduleModel ws : schedules) {
            if(ws == null) continue;
            wsList.add(ws);
        }
        if(wsList.isEmpty()) {
            error.setVisibility(View.VISIBLE);
            error.setText(getContext().getResources().getString(R.string.error_select_weekday));
            clickable = true;
            return;
        }
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setOnInitializedListener(() -> {
            List<String> ids = new ArrayList<>();
            for(int i = 0;i < users.size(); ++i) {
                ids.add(users.get(i).getId());
            }
            if(service.getCategory().getType().equals(Defs.CategoryType.CAFE)) {
                if(cafeItems == null) {
                    error.setVisibility(View.VISIBLE);
                    error.setText(getContext().getResources().getString(R.string.error_specify_table));
                    clickable = true;
                    return;
                }
                List<Integer> items = new ArrayList<>();
                for(String item : cafeItems) {
                    if(!item.equals("")) {
                        items.add(Integer.parseInt(item));
                    }
                }
                if(items.isEmpty()) {
                    error.setVisibility(View.VISIBLE);
                    error.setText(getContext().getResources().getString(R.string.error_specify_table));
                    clickable = true;
                    return;
                }
                extra = new ServiceExtraData(items);
            }
            if(currentBranch != null) {
                builder.send(builder.getApi(ApiService.class).updateBranch(new BranchUpdateModel(
                        service.getId(),
                        Integer.parseInt(sessionValue),
                        addUrl,
                        ids,
                        units[unit.getSelectedItemPosition()][0],
                        wsList,
                        extra,
                        currentBranch.getId()
                )));
            }
            else {
                builder.send(builder.getApi(ApiService.class).createBranch(new BranchCreateModel(
                        service.getId(),
                        Integer.parseInt(sessionValue),
                        addUrl,
                        ids,
                        units[unit.getSelectedItemPosition()][0],
                        wsList,
                        extra
                )));
            }
        });
        builder.setResponseListener(new ApiBuilder.ResponseListener<BranchModel>() {
            @Override
            public void onResponse(Call<BranchModel> call, Response<BranchModel> response) {
                if(response.code() == 201 || response.code() == 200) {
                    if(currentBranch == null) {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.created_branch), Toast.LENGTH_LONG).show();
                    }
                    else {
                        currentBranch = null;
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.updated_branch), Toast.LENGTH_LONG).show();
                    }
                    close.performClick();
                    if(onBranchAddedListener != null) {
                        onBranchAddedListener.onBranchAdded(response.body());
                    }
                }
                else if(response.code() == 400) {
                    try {
                        JSONObject payload = new JSONObject(response.errorBody().string());
                        String message = payload.getString("message");
                        if(message.startsWith(ResponseError.error_user_add_branch_forbidden)) {
                            String id = message.split(" ")[1];
                            String name = null;
                            for(UserModel user: users) {
                                if(user.getId().equals(id)) {
                                    name = user.getName();
                                    break;
                                }
                            }
                            error.setText(getContext().getResources().getString(R.string.error_user_add_branch_forbidden, name));
                            error.setVisibility(View.VISIBLE);
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
    };

    private View.OnClickListener addAddressClicked = (View v) -> {
        AddAddressDialog dialog = new AddAddressDialog(getContext(), addUrl);
        dialog.setOnAddressAddedListener((MetaData metaData, AddressModel model) -> {
            address.setText(model.getAddress());
            addUrl = model;
        });
        dialog.show();
    };

    private View.OnClickListener galleryBtnClicked = (View v) -> {
        GalleryFragment fragment = new GalleryFragment();
        fragment.setService(service);
        if(currentBranch != null) {
            fragment.setBranch(currentBranch);
        }
        fragment.setOnBackPressedListener(() -> {
            activity.getSupportFragmentManager().popBackStack();
        });
        activity.loadExtra(fragment, true);
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_branch_add, container, false);
        close = root.findViewById(R.id.close);
        address = root.findViewById(R.id.address);
        addressWrp = root.findViewById(R.id.address_wrp);
        extraWrp = root.findViewById(R.id.extra_wrp);
        galleryBtn = root.findViewById(R.id.gallery_btn);
        addUsersBtn = root.findViewById(R.id.add_users_btn);
        usersWrp = root.findViewById(R.id.users_wrp);
        usersRv = root.findViewById(R.id.users_rv);
        button = root.findViewById(R.id.button);
        title = root.findViewById(R.id.title);
        addAddress = root.findViewById(R.id.add_address);
        session = root.findViewById(R.id.session);
        unit = root.findViewById(R.id.unit);
        error = root.findViewById(R.id.error);
        scheduleBtn = root.findViewById(R.id.schedule_btn);
        galleryBtn.setOnClickListener(galleryBtnClicked);
        init();
        return root;
    }

    protected void init() {
        if(!loggedUser.getRole().getId().equals(Defs.Role.HELPER)) {
            close.setOnClickListener(closeClicked);
        }
        else {
            back = close;
        }
        super.init();
        if(service.getCategory().getType().equals(Defs.CategoryType.CAFE)) {
            extraWrp.setVisibility(View.VISIBLE);
            extraFragment = new CafeServiceExtraFragment();
            if(currentBranch != null && currentBranch.getExtra() != null) {
                List<Integer> tables = currentBranch.getExtra().getTables();
                List<String> items = new ArrayList<>();
                if(tables != null) {
                    for(int i = 0;i < tables.size(); ++i) {
                        items.add(String.valueOf(tables.get(i)));
                    }
                }
                ((CafeServiceExtraFragment) extraFragment).setItems(items);
            }
            ((CafeServiceExtraFragment) extraFragment).setOnItemChangeListener((List<String> items) -> {
                cafeItems = items;
            });
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.branch_extra_wrapper, extraFragment);
            transaction.commit();
        }
        unit.setAdapter(new UnitAdapter(getContext()));
        if(currentBranch != null) {
            if(currentBranch.getAddress() != null) {
                address.setText(currentBranch.getAddress().getAddress());
                addUrl = currentBranch.getAddress();
            }
            session.setText(String.valueOf(currentBranch.getAverageSession()));
            usersWrp.setVisibility(View.VISIBLE);
            users = new ArrayList<>(currentBranch.getUsers());
            if(usersRv.getLayoutManager() == null) {
                usersRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            }
            unit.setSelection(Defs.SessionUnit.UNITS.indexOf(currentBranch.getUnit()));
            UserAdapter userAdapter = new UserAdapter();
            userAdapter.setOnUserRemovedListener((UserModel user) -> {
                if(users.size() == 0) {
                    usersWrp.setVisibility(View.GONE);
                }
            });
            usersRv.setAdapter(userAdapter);
            for(WorkScheduleModel model : currentBranch.getWorkSchedule()) {
                schedules[Defs.WeekDay.getWeekdayPositionByName(model.getWeekDay())] = model;
            }
            title.setText(getContext().getResources().getString(R.string.branch_info_title));
            button.setText(getContext().getResources().getString(R.string.update_branch_btn));
            galleryBtn.setVisibility(View.VISIBLE);
        }
        addUsersBtn.setOnClickListener(addUserBtnClicked);
        scheduleBtn.setOnClickListener(scheduleBtnClicked);
        button.setOnClickListener(buttonClicked);
        addAddress.setOnClickListener(addAddressClicked);
        if(loggedUser.getRole().getId().equals(Defs.Role.HELPER)) {
            addUsersBtn.setVisibility(View.GONE);
            usersWrp.setVisibility(View.GONE);
        }
    }
}