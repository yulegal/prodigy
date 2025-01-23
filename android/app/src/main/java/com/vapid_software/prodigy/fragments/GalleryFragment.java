package com.vapid_software.prodigy.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.ConfirmDialog;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.helpers.OnSwipeTouchListener;
import com.vapid_software.prodigy.helpers.Utils;
import com.vapid_software.prodigy.helpers.ViewHolder;
import com.vapid_software.prodigy.models.BranchModel;
import com.vapid_software.prodigy.models.ServiceModel;
import com.vapid_software.prodigy.models.UserModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class GalleryFragment extends BaseExtraFragment {
    private View upload, empty, loader;
    private CoreActivity activity;
    private RecyclerView rv;
    private ServiceModel service;
    private BranchModel branch;
    private List<String> photos;
    private int page = 1;
    private int total;
    private final static int LIMIT = 20;
    private int currentShow;
    private UserModel loggedUser;

    private class Adapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.gallery_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            View root = holder.itemView;
            ImageView image = root.findViewById(R.id.image);
            int pos = holder.getAdapterPosition();
            String photo = photos.get(pos);
            String url = String.join("/", ApiBuilder.PUBLIC_PATH, photo);
            Picasso.get().load(url).into(image);
            root.setOnClickListener((View v) -> {
                currentShow = pos;
                View view = getLayoutInflater().inflate(R.layout.gallery_show_item, null);
                TextView of = view.findViewById(R.id.of);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                ImageView img = view.findViewById(R.id.image);
                View close = view.findViewById(R.id.close);
                View delete = view.findViewById(R.id.delete);
                PopupWindow win = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

                of.setText((currentShow + 1) + " of " + photos.size());

                if(loggedUser == null || !loggedUser.getId().equals(service.getUser().getId())) {
                    delete.setVisibility(View.GONE);
                }

                view.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
                    @Override
                    public void onSwipeRight() {
                        if(currentShow != 0) {
                            --currentShow;
                            String photo = photos.get(currentShow);
                            String url = String.join("/", ApiBuilder.PUBLIC_PATH, photo);
                            of.setText((currentShow + 1) + " of " + photos.size());
                            Picasso.get().load(url).into(img);
                        }
                    }

                    @Override
                    public void onSwipeLeft() {
                        if(currentShow != photos.size() - 1) {
                            ++currentShow;
                            String photo = photos.get(currentShow);
                            String url = String.join("/", ApiBuilder.PUBLIC_PATH, photo);
                            of.setText((currentShow + 1) + " of " + photos.size());
                            Picasso.get().load(url).into(img);
                        }
                    }
                });

                close.setOnClickListener((View v1) -> {
                    win.dismiss();
                });

                delete.setOnClickListener((View v1) -> {
                    ConfirmDialog confirmDialog = new ConfirmDialog(
                            getContext(),
                            getContext().getResources().getString(R.string.delete_photo_confirm_body),
                            getContext().getResources().getString(R.string.delete_photo_confirm_title),
                            null,
                            null
                    );
                    confirmDialog.setOnPositiveConfirmListener(() -> {
                        ApiBuilder builder = ApiBuilder.getInstance(getContext());
                        builder.setOnInitializedListener(() -> {
                            builder.send(builder.getApi(ApiService.class).deletePhoto(photo));
                        });
                        builder.setResponseListener(new ApiBuilder.ResponseListener() {
                            @Override
                            public void onResponse(Call call, Response response) {
                                if(response.code() == 200) {
                                    win.dismiss();
                                    photos.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    if(photos.size() == 0) {
                                        empty.setVisibility(View.VISIBLE);
                                        rv.setVisibility(View.GONE);
                                    }
                                }
                                else {
                                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                                }
                                confirmDialog.dismiss();
                            }
                        });
                    });
                    confirmDialog.show();
                });

                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                Picasso.get().load(url).into(img);

                win.setOutsideTouchable(true);
                win.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
            });
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }
    }

    public void setBranch(BranchModel branch) {
        this.branch = branch;
    }

    public void setService(ServiceModel service) {
        this.service = service;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
        loggedUser = activity.getLoggedUser();
    }

    private View.OnClickListener closeClicked = (View v) -> {
        activity.getSupportFragmentManager().popBackStack();
    };

    private View.OnClickListener uploadClicked = (View v) -> {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, Defs.PermissionCode.GALLERY_CODE);
        }
        else {
            selectImages();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == Defs.PermissionCode.GALLERY_CODE && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImages();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == Defs.ResultCode.GALLERY_CODE && resultCode == Activity.RESULT_OK) {
            List<File> files = new ArrayList<>();
            ClipData clipData = data.getClipData();
            for(int i = 0;i < clipData.getItemCount(); ++i) {
                files.add(new File(Utils.getMediaPath(clipData.getItemAt(i).getUri(), activity.getContentResolver())));
            }
            MultipartBody.Part[] parts = new MultipartBody.Part[files.size()];
            for(int i = 0;i < files.size(); ++i) {
                File file = files.get(i);
                parts[i] = MultipartBody.Part.createFormData("files", file.getName(), RequestBody.create(MultipartBody.FORM, file));
            }
            ApiBuilder builder = ApiBuilder.getInstance(getContext());
            builder.setResponseListener(new ApiBuilder.ResponseListener<List<String>>() {
                @Override
                public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                    if(response.code() == 201) {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.uploaded_successfully), Toast.LENGTH_LONG).show();
                        page = 1;
                        photos = null;
                        load();
                    }
                    else {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).uploadPhotosToGallery(
                        RequestBody.create(MultipartBody.FORM, service.getId()),
                        branch == null ? null : RequestBody.create(MultipartBody.FORM, branch.getId()),
                        parts
                ));
            });
        }
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, Defs.ResultCode.GALLERY_CODE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        upload = root.findViewById(R.id.upload);
        loader = root.findViewById(R.id.loader);
        rv = root.findViewById(R.id.rv);
        empty = root.findViewById(R.id.empty);
        back = root.findViewById(R.id.close);
        upload.setOnClickListener(uploadClicked);
        init();
        return root;
    }

    protected void init() {
        super.init();
        if(loggedUser != null && !service.getUser().getId().equals(loggedUser.getId())) {
            upload.setVisibility(View.GONE);
        }
        load();
    }

    private FilterQueryOptions getFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        if(branch != null) {
            items.add(new FilterItem("branch", branch.getId()));
        }
        return new FilterQueryOptions(page, LIMIT, items);
    }

    private void load() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<String>>() {
            @Override
            public void onResponse(Call<FilterResponse<String>> call, Response<FilterResponse<String>> response) {
                int code = response.code();
                if(code == 201) {
                    loader.setVisibility(View.GONE);
                    total = response.body().getCount();
                    if(response.body().getTotal() == 0 && total == 0) {
                        empty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    }
                    else {
                        empty.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        if(photos == null) {
                            photos = new ArrayList<>();
                        }
                        photos.addAll(response.body().getData());
                        if(rv.getLayoutManager() == null) {
                            rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
                        }
                        if(rv.getAdapter() == null || page == 1) {
                            rv.setAdapter(new Adapter());
                        }
                        else {
                            rv.getAdapter().notifyItemRangeInserted(photos.size() - response.body().getTotal(), response.body().getTotal());
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterGallery(getFilterQueryOptions(), service.getId()));
        });
    }
}