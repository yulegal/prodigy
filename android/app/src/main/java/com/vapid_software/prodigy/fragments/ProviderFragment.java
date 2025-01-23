package com.vapid_software.prodigy.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.qkopy.richlink.data.model.MetaData;
import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.data.BaseData;
import com.vapid_software.prodigy.data.ServiceExtraData;
import com.vapid_software.prodigy.helpers.AddAddressDialog;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.helpers.Utils;
import com.vapid_software.prodigy.helpers.WorkScheduleWindow;
import com.vapid_software.prodigy.models.AddressModel;
import com.vapid_software.prodigy.models.CategoryModel;
import com.vapid_software.prodigy.models.ServiceModel;
import com.vapid_software.prodigy.models.WorkScheduleModel;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class ProviderFragment extends BaseExtraFragment {
    private Spinner categoriesView, unit;
    private List<BaseData> categories;
    private EditText name, address, averageSession;
    private ImageView avatar;
    private View uploadPhoto, removePhoto, addAddress;
    private View extraWrp, addressWrp;
    private ServiceModel service;
    private CategoryModel selectedCategory;
    private List<String> cafeItems;
    private Button button, addBranchesBtn, workScheduleBtn, galleryBtn;
    private TextView error, title, notification;
    private AddressModel addUrl;
    private CoreActivity activity;
    private File file;
    private Fragment extraFragment;
    private WorkScheduleModel schedules[] = new WorkScheduleModel[7];
    private boolean clickable = true;
    private boolean createMode = true;
    private String[][] units;

    private class CategoriesAdapter extends ArrayAdapter {
        private int padding;
        public CategoriesAdapter(Context context) {
            super(context, 0);
            padding = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    10,
                    getContext().getResources().getDisplayMetrics()
            );
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView tv = (TextView) getLayoutInflater().inflate(R.layout.cateogory_item, parent, false);
            tv.setText(categories.get(position).getName());
            return tv;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView tv = (TextView) getLayoutInflater().inflate(R.layout.category_dd_item, parent, false);
            tv.setText(categories.get(position).getName());
            tv.setPadding(tv.getPaddingLeft(), position == 0 ? padding : tv.getPaddingTop(), tv.getPaddingRight(), position == categories.size() - 1 ? padding : tv.getPaddingBottom());
            return tv;
        }

        @Override
        public int getCount() {
            return categories.size();
        }
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == Defs.PermissionCode.GALLERY_CODE && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == Defs.ResultCode.GALLERY_CODE && resultCode == Activity.RESULT_OK) {
            try {
                file = new File(Utils.getMediaPath(data.getData(), activity.getContentResolver()));
                avatar.setImageBitmap(Utils.decodeBitmap(activity.getContentResolver(), data.getData()));
                removePhoto.setVisibility(View.VISIBLE);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private View.OnClickListener uploadPhotoClicked = (View v) -> {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, Defs.PermissionCode.GALLERY_CODE);
        }
        else {
            selectImage();
        }
    };

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Defs.ResultCode.GALLERY_CODE);
    }

    private View.OnClickListener removePhotoClicked = (View v) -> {
        if(service != null && service.getIcon() != null) {
            ApiBuilder builder = ApiBuilder.getInstance(getContext());
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).removeServicePhoto(service.getId()));
            });
            builder.setResponseListener(new ApiBuilder.ResponseListener<ServiceModel>() {
                @Override
                public void onResponse(Call<ServiceModel> call, Response<ServiceModel> response) {
                    if(response.code() == 200) {
                        v.setVisibility(View.GONE);
                        avatar.setImageResource(R.drawable.avatar);
                        file = null;
                        service.setIcon(null);
                    }
                    else {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else {
            v.setVisibility(View.GONE);
            avatar.setImageResource(R.drawable.avatar);
            file = null;
        }
    };
    private View.OnClickListener workScheduleBtnClicked = (View v) -> {
        WorkScheduleWindow win = new WorkScheduleWindow(getContext(), schedules);
        win.show();
    };
    private View.OnClickListener buttonClicked = (View v) -> {
        if(!clickable) return;
        clickable = false;
        error.setVisibility(View.GONE);
        averageSession.setBackgroundResource(R.drawable.auth_input_wrp);
        name.setBackgroundResource(R.drawable.auth_input_wrp);
        categoriesView.setBackgroundResource(R.drawable.auth_input_wrp);
        addressWrp.setBackgroundResource(R.drawable.auth_input_wrp);
        unit.setBackgroundResource(R.drawable.auth_input_wrp);
        String sessionValue = averageSession.getText().toString();
        String nameValue = name.getText().toString().trim();
        if(nameValue.isEmpty() || sessionValue.isEmpty() || selectedCategory == null || addUrl == null) {
            error.setVisibility(View.VISIBLE);
            error.setText(getContext().getResources().getString(R.string.fill_in_all_fields));
            clickable = true;
            if(nameValue.isEmpty()) {
                name.setBackgroundResource(R.drawable.standard_input_e);
            }
            if(sessionValue.isEmpty()) {
                averageSession.setBackgroundResource(R.drawable.standard_input_e);
            }
            if(selectedCategory == null) {
                categoriesView.setBackgroundResource(R.drawable.standard_input_e);
            }
            if(addUrl == null) {
                addressWrp.setBackgroundResource(R.drawable.standard_input_e);
            }
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
            RequestBody extraRb = null;
            Gson gson = new Gson();
            if(selectedCategory.getType().equals(Defs.CategoryType.CAFE)) {
                if(cafeItems == null) {
                    error.setVisibility(View.VISIBLE);
                    error.setText(getContext().getResources().getString(R.string.error_specify_table));
                    clickable = true;
                    return;
                }
                List<Integer> items = null;
                for(String item : cafeItems) {
                    if(!item.equals("")) {
                        if(items == null) items = new ArrayList<>();
                        items.add(Integer.parseInt(item));
                    }
                }
                if(items == null) {
                    error.setVisibility(View.VISIBLE);
                    error.setText(getContext().getResources().getString(R.string.error_specify_table));
                    clickable = true;
                    return;
                }
                extraRb = MultipartBody.create(MultipartBody.FORM, gson.toJson(new ServiceExtraData(items), new TypeToken<ServiceExtraData>(){}.getType()));
            }
            if(createMode) {
                builder.send(builder.getApi(ApiService.class).createService(
                        RequestBody.create(MultipartBody.FORM, nameValue),
                        RequestBody.create(MultipartBody.FORM, selectedCategory.getId()),
                        RequestBody.create(MultipartBody.FORM, sessionValue),
                        file == null ? null : MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(MultipartBody.FORM, file)),
                        RequestBody.create(MultipartBody.FORM, units[unit.getSelectedItemPosition()][0]),
                        RequestBody.create(MultipartBody.FORM, gson.toJson(addUrl, new TypeToken<AddressModel>(){}.getType())),
                        RequestBody.create(MultipartBody.FORM, gson.toJson(wsList, new TypeToken<List<WorkScheduleModel>>(){}.getType())),
                        extraRb
                ));
            }
            else {
                builder.send(builder.getApi(ApiService.class).updateService(
                        RequestBody.create(MultipartBody.FORM, service.getId()),
                        RequestBody.create(MultipartBody.FORM, nameValue),
                        RequestBody.create(MultipartBody.FORM, selectedCategory.getId()),
                        RequestBody.create(MultipartBody.FORM, sessionValue),
                        file == null ? null : MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(MultipartBody.FORM, file)),
                        RequestBody.create(MultipartBody.FORM, units[unit.getSelectedItemPosition()][0]),
                        RequestBody.create(MultipartBody.FORM, gson.toJson(addUrl, new TypeToken<AddressModel>(){}.getType())),
                        RequestBody.create(MultipartBody.FORM, gson.toJson(wsList, new TypeToken<List<WorkScheduleModel>>(){}.getType())),
                        extraRb
                ));
            }
        });
        builder.setResponseListener(new ApiBuilder.ResponseListener<ServiceModel>() {
            @Override
            public void onResponse(Call<ServiceModel> call, Response<ServiceModel> response) {
                int code = response.code();
                if(code == 201 || code == 200) {
                    clickable = true;
                    load();
                    Toast.makeText(getContext(), getContext().getResources().getString(createMode ? R.string.service_created : R.string.service_updated), Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
    };
    private View.OnClickListener galleryBtnClicked = (View v) -> {
        GalleryFragment fragment = new GalleryFragment();
        fragment.setService(service);
        fragment.setOnBackPressedListener(() -> {
            activity.getSupportFragmentManager().popBackStack();
        });
        activity.loadExtra(fragment, true);
    };
    private View.OnClickListener addAddressClicked = (View v) -> {
        AddAddressDialog dialog = new AddAddressDialog(getContext(), addUrl);
        dialog.setOnAddressAddedListener((MetaData metaData, AddressModel model) -> {
            address.setText(model.getAddress());
            addUrl = model;
        });
        dialog.show();
    };
    private View.OnClickListener addBranchesBtnClicked = (View v) -> {
        BranchesFragment fragment = new BranchesFragment();
        fragment.setService(service);
        fragment.setOnBackPressedListener(() -> {
            activity.getSupportFragmentManager().popBackStack();
        });
        activity.loadExtra(fragment, true);
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_provider, container, false);
        back = root.findViewById(R.id.back);
        categoriesView = root.findViewById(R.id.categories);
        unit = root.findViewById(R.id.unit);
        name = root.findViewById(R.id.name);
        title = root.findViewById(R.id.title);
        address = root.findViewById(R.id.address);
        averageSession = root.findViewById(R.id.session);
        notification = root.findViewById(R.id.notification);
        addressWrp = root.findViewById(R.id.address_wrp);
        addAddress = root.findViewById(R.id.add_address);
        avatar = root.findViewById(R.id.avatar);
        uploadPhoto = root.findViewById(R.id.upload_photo);
        removePhoto = root.findViewById(R.id.remove_photo);
        extraWrp = root.findViewById(R.id.extra_wrp);
        button = root.findViewById(R.id.button);
        addBranchesBtn = root.findViewById(R.id.add_branches_btn);
        workScheduleBtn = root.findViewById(R.id.work_schedule_btn);
        galleryBtn = root.findViewById(R.id.gallery_btn);
        error = root.findViewById(R.id.error);
        button.setOnClickListener(buttonClicked);
        uploadPhoto.setOnClickListener(uploadPhotoClicked);
        removePhoto.setOnClickListener(removePhotoClicked);
        galleryBtn.setOnClickListener(galleryBtnClicked);
        workScheduleBtn.setOnClickListener(workScheduleBtnClicked);
        addBranchesBtn.setOnClickListener(addBranchesBtnClicked);
        addAddress.setOnClickListener(addAddressClicked);
        init();
        return root;
    }

    protected void init() {
        super.init();
        unit.setAdapter(new UnitAdapter(getContext()));
        loadCategories();
    }

    private void load() {
       ApiBuilder builder = ApiBuilder.getInstance(getContext());
       builder.setOnInitializedListener(() -> {
           builder.send(builder.getApi(ApiService.class).getUserService());
       });
       builder.setResponseListener(new ApiBuilder.ResponseListener<ServiceModel>() {
           @Override
           public void onResponse(Call<ServiceModel> call, Response<ServiceModel> response) {
               if(response.code() == 200) {
                   createMode = false;
                   service = response.body();
                   addBranchesBtn.setVisibility(View.VISIBLE);
                   galleryBtn.setVisibility(View.VISIBLE);
                   title.setText(getContext().getResources().getString(R.string.service_info_title));
                   button.setText(getContext().getResources().getString(R.string.update_service_btn));
                   name.setText(service.getName());
                   averageSession.setText(String.valueOf(service.getAverageSession()));
                   if(service.getAddress() != null) {
                       addUrl = service.getAddress();
                       address.setText(service.getAddress().getAddress());
                   }
                   categoriesView.setSelection(getBaseCategoryPosition(service.getCategory().getId()));
                   categoriesView.setEnabled(false);
                   for(WorkScheduleModel model : service.getWorkSchedule()) {
                       schedules[Defs.WeekDay.getWeekdayPositionByName(model.getWeekDay())] = model;
                   }
                   if(service.getIcon() != null) {
                       removePhoto.setVisibility(View.VISIBLE);
                       Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, service.getIcon())).into(avatar);
                   }
                   if(service.getExtra() != null) {
                       List<String> items = new ArrayList<>();
                       List<Integer> tables = service.getExtra().getTables();
                       if(tables != null) {
                           for(int table: tables) {
                               items.add(String.valueOf(table));
                           }
                       }
                       extraFragment = new CafeServiceExtraFragment();
                       ((CafeServiceExtraFragment) extraFragment).setItems(items);
                   }
                   unit.setSelection(Defs.SessionUnit.UNITS.indexOf(service.getUnit()));
                   if(service.isBlocked()) {
                       notification.setVisibility(View.VISIBLE);
                       notification.setText(getContext().getResources().getString(R.string.service_blocked));
                   }
                   else if(service.getUser().getBalance() >= 0) {
                       boolean show = false;
                       if(service.getTrialEndDate() != 0) {
                           show = true;
                           SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY");
                           notification.setTextColor(Color.parseColor("#bb0000"));
                           notification.setText(getContext().getResources().getString(R.string.trial_period_notify, format.format(new Date(service.getTrialEndDate()))));
                       }
                       else if(service.getPaymentEndDate() != 0) {
                           show = true;
                           SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY");
                           notification.setTextColor(Color.parseColor("#666666"));
                           notification.setText(getContext().getResources().getString(R.string.payment_period_notify, format.format(new Date(service.getPaymentEndDate()))));
                       }
                       if(show) {
                           notification.setVisibility(View.VISIBLE);
                       }
                   }
               }
               else if(response.code() != 400) {
                   Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
               }
           }
       });
    }

    private void loadCategories() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<CategoryModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<CategoryModel>> call, Response<FilterResponse<CategoryModel>> response) {
                if(response.code() == 201) {
                    categories = new ArrayList<>();
                    String locale = getContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
                    categories.add(new BaseData(
                            null,
                            getContext().getResources().getString(R.string.categories_title)
                    ));
                    for(CategoryModel category : response.body().getData()) {
                        categories.add(new BaseData(
                                category.getId(),
                                category.getName().get(locale)
                        ));
                    }
                    categoriesView.setAdapter(new CategoriesAdapter(getContext()));
                    categoriesView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            cafeItems = null;
                            if(position == 0) {
                                selectedCategory = null;
                                return;
                            }
                            selectedCategory = response.body().getData().get(position - 1);
                            if(selectedCategory.getType().equals(Defs.CategoryType.CAFE)) {
                                extraWrp.setVisibility(View.VISIBLE);
                                if(extraFragment == null) {
                                    extraFragment = new CafeServiceExtraFragment();
                                }
                                ((CafeServiceExtraFragment) extraFragment).setOnItemChangeListener((List<String> items) -> {
                                    cafeItems = items;
                                });
                                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                                transaction.replace(R.id.extra_wrapper, extraFragment);
                                transaction.commit();
                            }
                            else {
                                extraWrp.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    load();
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterCategories(new FilterQueryOptions(null)));
        });
    }

    private int getBaseCategoryPosition(String id) {
        for(int i = 0;i < categories.size(); ++i) {
            if(categories.get(i).getId() == null) continue;
            if(categories.get(i).getId().equals(id)) return i;
        }
        return -1;
    }
}