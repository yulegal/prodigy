package com.vapid_software.prodigy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.fragments.BaseExtraFragment;
import com.vapid_software.prodigy.fragments.BranchAddFragment;
import com.vapid_software.prodigy.fragments.BroadcastFragment;
import com.vapid_software.prodigy.fragments.ChatsFragment;
import com.vapid_software.prodigy.fragments.CoreFragment;
import com.vapid_software.prodigy.fragments.MyContactsFragment;
import com.vapid_software.prodigy.fragments.ProviderFragment;
import com.vapid_software.prodigy.fragments.UserInfoFragment;
import com.vapid_software.prodigy.helpers.ConfirmDialog;
import com.vapid_software.prodigy.helpers.DBHelper;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.Utils;
import com.vapid_software.prodigy.models.AuthResponseModel;
import com.vapid_software.prodigy.models.BranchModel;
import com.vapid_software.prodigy.models.UserEditNameModel;
import com.vapid_software.prodigy.models.UserModel;

import java.io.File;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class CoreActivity extends AppCompatActivity implements BaseExtraFragment.OnBackPressedListener {
    private DrawerLayout drawer;
    private Fragment currentFragment = new CoreFragment();
    private UserModel loggedUser;
    private Socket socket;
    private Context context = this;
    private ImageView avatar;
    private View extraWrp;
    private OnUserLoggedOutListener onUserLoggedOutListener;
    private NavigationView navigationView;
    private TextView name;
    private View edit, upload, delete;
    private Emitter.Listener connectErrorListener = (Object ...args) -> {
        Log.i(Defs.SOCKET_INFO_LABEL, "Connection error");
    };
    private Emitter.Listener connectListener = (Object ...args) -> {
        Log.i(Defs.SOCKET_INFO_LABEL, "Connected: " + socket.id());
    };
    private Emitter.Listener disconnectListener = (Object ...args) -> {
        Log.i(Defs.SOCKET_INFO_LABEL, "Disconnected");
    };
    private Emitter.Listener whoAreYouListener = (Object ...args) -> {
        Gson gson = new Gson();
        socket.emit(Defs.WS_MESSAGES.CLIENT.I_AM, gson.toJson(loggedUser));
    };
    private Emitter.Listener roleChangedListener = (Object ...args) -> {
        runOnUiThread(() -> {
            reLogin();
        });
    };
    private Emitter.Listener logoutListener = (Object ...args) -> {
        if(loggedUser != null) {
            runOnUiThread(() -> {
                UserModel.logout(() -> {
                    loggedUser = null;
                    if(onUserLoggedOutListener != null) {
                        onUserLoggedOutListener.onUserLoggedOut();
                    }
                }, this);
            });
        }
    };
    {
        try {
            IO.Options options = new IO.Options();
            options.path = "/api/events";
            socket = IO.socket(ApiBuilder.HOST, options);
            socket.on(Socket.EVENT_CONNECT_ERROR, connectErrorListener);
            socket.on(Socket.EVENT_CONNECT, connectListener);
            socket.on(Socket.EVENT_DISCONNECT, disconnectListener);
            socket.on(Defs.WS_MESSAGES.SERVER.WHO_ARE_YOU, whoAreYouListener);
            socket.on(Defs.WS_MESSAGES.SERVER.ROLE_CHANGED, roleChangedListener);
            socket.on(Defs.WS_MESSAGES.SERVER.LOGGED_OUT, logoutListener);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private class SaveUserTask extends AsyncTask<Void, Void, Void> {
        private DBHelper dbHelper;
        private SQLiteDatabase db;
        private AuthResponseModel response;

        public SaveUserTask(AuthResponseModel response) {
            this.response = response;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dbHelper = new DBHelper(context);
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("access_token", response.getAccessToken());
            db.update(DBHelper.USERS_TABLE, values, null, null);
            db.close();
            dbHelper.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            loggedUser.setRole(response.getUser().getRole());
        }
    }

    public interface OnUserLoggedOutListener {
        void onUserLoggedOut();
    }

    private NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = (MenuItem item) -> {
        int id = item.getItemId();
        boolean isSelected = id == R.id.nav_profile || id == R.id.nav_my_contacts || id == R.id.nav_chats || id == R.id.nav_contacts || id == R.id.nav_logout || id == R.id.nav_settings || id == R.id.nav_branches || id == R.id.nav_pricing || id == R.id.nav_broadcast || id == R.id.nav_support || id == R.id.nav_terms || id == R.id.nav_provider;
        if(isSelected) {
            if(id == R.id.nav_logout) {
                ConfirmDialog confirmDialog = new ConfirmDialog(
                        this,
                        getResources().getString(R.string.logout_confirm_body),
                        getResources().getString(R.string.logout_confirm_title),
                        null,
                        null
                );
                confirmDialog.setOnPositiveConfirmListener(() -> {
                    confirmDialog.dismiss();
                    Toast.makeText(context, context.getResources().getString(R.string.logout_success), Toast.LENGTH_LONG).show();
                    socket.emit(Defs.WS_MESSAGES.CLIENT.LOGOUT);
                });
                confirmDialog.show();
            }
            else if(id == R.id.nav_profile) {
                UserInfoFragment fragment = new UserInfoFragment();
                fragment.setUser(loggedUser);
                fragment.setOnBackPressedListener(this);
                loadExtra(fragment);
            }
            else if(id == R.id.nav_provider) {
                if(!loggedUser.getRole().getId().equals(Defs.Role.PROVIDER)) {
                    ConfirmDialog dialog = new ConfirmDialog(
                            this,
                            getResources().getString(R.string.become_provider_confirm_body),
                            getResources().getString(R.string.become_provider_confirm_title),
                            null,
                            null
                    );
                    dialog.setOnPositiveConfirmListener(() -> {
                        ProviderFragment fragment = new ProviderFragment();
                        fragment.setOnBackPressedListener(this);
                        loadExtra(fragment);
                        dialog.dismiss();
                    });
                    dialog.show();
                }
                else {
                    ProviderFragment fragment = new ProviderFragment();
                    fragment.setOnBackPressedListener(this);
                    loadExtra(fragment);
                }
            }
            else if(id == R.id.nav_broadcast) {
                if(!loggedUser.getRole().getId().equals(Defs.Role.PROVIDER)) {
                    Toast.makeText(this, getResources().getString(R.string.you_must_be_provider), Toast.LENGTH_LONG).show();
                }
                else {
                    BroadcastFragment fragment = new BroadcastFragment();
                    fragment.setOnBackPressedListener(this);
                    loadExtra(fragment);
                }
            }
            else if(id == R.id.nav_branches) {
                if(!loggedUser.getRole().getId().equals(Defs.Role.HELPER)) {
                    Toast.makeText(this, getResources().getString(R.string.you_must_be_helper), Toast.LENGTH_LONG).show();
                }
                else {
                    ApiBuilder builder = ApiBuilder.getInstance(this);
                    builder.setResponseListener(new ApiBuilder.ResponseListener<BranchModel>() {
                        @Override
                        public void onResponse(Call<BranchModel> call, Response<BranchModel> response) {
                            if(response.code() == 200) {
                                BranchAddFragment fragment = new BranchAddFragment();
                                fragment.setService(response.body().getService());
                                fragment.setCurrentBranch(response.body());
                                fragment.setOnBackPressedListener(CoreActivity.this);
                                loadExtra(fragment);
                            }
                            else {
                                Toast.makeText(context, context.getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    builder.setOnInitializedListener(() -> {
                        builder.send(builder.getApi(ApiService.class).getUserBranch());
                    });
                }
            }
            else if(id == R.id.nav_my_contacts) {
                MyContactsFragment fragment = new MyContactsFragment();
                fragment.setOnBackPressedListener(this);
                loadExtra(fragment);
            }
            else if(id == R.id.nav_chats) {
                ChatsFragment fragment = new ChatsFragment();
                fragment.setOnBackPressedListener(this);
                loadExtra(fragment);
            }
            drawer.closeDrawer(Gravity.RIGHT);
        }
        return isSelected;
    };

    public interface OnLoadFragmentListener {
        void onLoad(FragmentTransaction transaction);
    }

    private void reLogin() {
        ApiBuilder builder = ApiBuilder.getInstance(this);
        builder.setResponseListener(new ApiBuilder.ResponseListener<AuthResponseModel>() {
            @Override
            public void onResponse(Call<AuthResponseModel> call, Response<AuthResponseModel> response) {
                if(response.code() == 200) {
                    new SaveUserTask(response.body()).execute();
                }
                else {
                    Toast.makeText(context, context.getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).reLogin());
        });
    }

    private View.OnClickListener editNameClicked = (View v) -> {
        Dialog dialog = new Dialog(this);
        View root = getLayoutInflater().inflate(R.layout.edit_name_dialog, null);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        View close = root.findViewById(R.id.close);
        EditText ename = root.findViewById(R.id.name);
        View button = root.findViewById(R.id.button);
        TextView error = root.findViewById(R.id.error);

        drawer.closeDrawer(Gravity.RIGHT);

        close.setOnClickListener((View v1) -> {
            dialog.dismiss();
        });

        button.setOnClickListener((View v1) -> {
            String nameValue = ename.getText().toString().trim();
            error.setVisibility(View.GONE);
            ename.setBackgroundResource(R.drawable.standard_input);
            if(nameValue.isEmpty()) {
                error.setVisibility(View.VISIBLE);
                error.setText(getResources().getString(R.string.fill_name_error));
                ename.setBackgroundResource(R.drawable.standard_input_e);
                return;
            }
            ApiBuilder builder = ApiBuilder.getInstance(this);
            builder.setResponseListener(new ApiBuilder.ResponseListener<UserModel>() {
                @Override
                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                    if(response.code() == 201) {
                        Toast.makeText(context, context.getResources().getString(R.string.saved), Toast.LENGTH_LONG).show();
                        loggedUser.setName(response.body().getName());
                        name.setText(loggedUser.getName());
                    }
                    else {
                        Toast.makeText(context, context.getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                }
            });
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).editName(new UserEditNameModel(nameValue)));
            });
        });

        ename.setText(loggedUser.getName());

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.setContentView(root);
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    };

    private View.OnClickListener uploadClicked = (View v) -> {
        drawer.closeDrawer(Gravity.RIGHT);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, Defs.PermissionCode.GALLERY_CODE);
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

    private View.OnClickListener deleteClicked = (View v) -> {
        drawer.closeDrawer(Gravity.RIGHT);
        ConfirmDialog confirmDialog = new ConfirmDialog(
                this,
                getResources().getString(R.string.remove_avatar_confirm_body),
                getResources().getString(R.string.remove_avatar_confirm_title),
                null,
                null
        );
        confirmDialog.setOnPositiveConfirmListener(() -> {
            ApiBuilder builder = ApiBuilder.getInstance(this);
            builder.setResponseListener(new ApiBuilder.ResponseListener<UserModel>() {
                @Override
                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                    if(response.code() == 200) {
                        loggedUser.setIcon(null);
                        avatar.setVisibility(View.GONE);
                        Toast.makeText(context, context.getResources().getString(R.string.removed), Toast.LENGTH_LONG).show();
                        v.setVisibility(View.GONE);
                    }
                    else {
                        Toast.makeText(context, context.getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                    confirmDialog.dismiss();
                }
            });
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).deleteAvatar());
            });
        });
        confirmDialog.show();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);
        drawer = findViewById(R.id.drawer);
        extraWrp = findViewById(R.id.extra_wrp);
        navigationView = findViewById(R.id.navigation);
        name = navigationView.getHeaderView(0).findViewById(R.id.name);
        upload = navigationView.getHeaderView(0).findViewById(R.id.upload);
        delete = navigationView.getHeaderView(0).findViewById(R.id.delete);
        avatar = navigationView.getHeaderView(0).findViewById(R.id.avatar);
        delete = navigationView.getHeaderView(0).findViewById(R.id.delete);
        edit = navigationView.getHeaderView(0).findViewById(R.id.edit);
        navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener);
        edit.setOnClickListener(editNameClicked);
        upload.setOnClickListener(uploadClicked);
        delete.setOnClickListener(deleteClicked);
        init();
    }

    public void loadExtra(Fragment fragment, boolean addToBackStack) {
        extraWrp.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(addToBackStack) {
            transaction.add(R.id.extra_wrp, fragment);
            transaction.addToBackStack(null);
        }
        else {
            transaction.replace(R.id.extra_wrp, fragment);
        }
        transaction.commit();
    }

    public void loadExtra(Fragment fragment) {
        loadExtra(fragment, false);
    }

    public void hideExtra() {
        extraWrp.setVisibility(View.GONE);
    }

    public void setOnUserLoggedOutListener(OnUserLoggedOutListener onUserLoggedOutListener) {
        this.onUserLoggedOutListener = onUserLoggedOutListener;
    }

    public DrawerLayout getDrawer() {
        return drawer;
    }

    private void init() {
        UserModel.fetchLoggedUser(this, (UserModel user) -> {
            loggedUser = user;
            socket.connect();
            load(false);
            if(loggedUser != null) {
                if(loggedUser.getIcon() != null) {
                    delete.setVisibility(View.VISIBLE);
                    avatar.setVisibility(View.VISIBLE);
                    Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, loggedUser.getIcon())).into(avatar);
                }
                name.setText(loggedUser.getName());
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(loggedUser != null && !socket.connected()) {
            socket.connect();
        }
    }

    public void setCurrentFragment(Fragment currentFragment) {
        this.currentFragment = currentFragment;
    }

    public Socket getSocket() {
        return socket;
    }

    public UserModel getLoggedUser() {
        return loggedUser;
    }

    public void load(OnLoadFragmentListener onLoadFragmentListener) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(onLoadFragmentListener != null) {
            onLoadFragmentListener.onLoad(transaction);
        }
        transaction.commit();
    }

    public void load(boolean add) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(add) {
            transaction.add(R.id.wrapper, currentFragment);
        }
        else {
            transaction.replace(R.id.wrapper, currentFragment);
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void finishSockets() {
        socket.off(Socket.EVENT_CONNECT_ERROR, connectErrorListener);
        socket.off(Socket.EVENT_CONNECT, connectListener);
        socket.off(Socket.EVENT_DISCONNECT, disconnectListener);
        socket.off(Defs.WS_MESSAGES.SERVER.WHO_ARE_YOU, whoAreYouListener);
        socket.off(Defs.WS_MESSAGES.SERVER.ROLE_CHANGED, roleChangedListener);
        socket.off(Defs.WS_MESSAGES.SERVER.LOGGED_OUT, logoutListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(socket != null && socket.connected()) {
            socket.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishSockets();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Defs.ResultCode.GALLERY_CODE && resultCode == RESULT_OK) {
            File file = new File(Utils.getMediaPath(data.getData(), getContentResolver()));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(MultipartBody.FORM, file));
            ApiBuilder builder = ApiBuilder.getInstance(this);
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).uploadAvatar(part));
            });
            builder.setResponseListener(new ApiBuilder.ResponseListener<UserModel>() {
                @Override
                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                    if(response.code() == 201) {
                        loggedUser.setIcon(response.body().getIcon());
                        Toast.makeText(context, context.getResources().getString(R.string.saved), Toast.LENGTH_LONG).show();
                        Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, loggedUser.getIcon())).into(avatar);
                        avatar.setVisibility(View.VISIBLE);
                        delete.setVisibility(View.VISIBLE);
                    }
                    else {
                        Toast.makeText(context, context.getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Defs.PermissionCode.GALLERY_CODE && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        }
    }

    @Override
    public void OnExtraBackPressed() {
        hideExtra();
    }
}