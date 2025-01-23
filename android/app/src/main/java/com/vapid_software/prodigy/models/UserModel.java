package com.vapid_software.prodigy.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.DBHelper;

import retrofit2.Call;
import retrofit2.Response;

public class UserModel {
    private String id;
    private String name;
    private String login;
    private long createdAt;
    private String locale;
    private int balance;
    private String icon;
    private RoleModel role;
    private String status;
    private static UserModel loggedUser;
    private static String loggedUserId;

    public interface OnUserFetchListener {
        void fetchedUser(UserModel userModel);
    }

    private static class FetchUserTask extends AsyncTask<Void, Void, String> {
        private DBHelper dbHelper;
        private SQLiteDatabase db;
        private Cursor cursor;
        private OnUserFetchListener fetchListener;
        private Context context;

        public FetchUserTask(Context context, OnUserFetchListener fetchListener) {
            this.fetchListener = fetchListener;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            dbHelper = new DBHelper(context);
            db = dbHelper.getReadableDatabase();
            cursor = db.query(DBHelper.USERS_TABLE, new String[]{"user_id"}, null,null,null,null,null);
            if(cursor.getCount() == 0) return null;
            cursor.moveToFirst();
            return cursor.getString(0);
        }

        @Override
        protected void onPostExecute(String s) {
            cursor.close();
            db.close();
            dbHelper.close();
            loggedUserId = s;
            fetchUser(context, s, fetchListener);
        }
    }

    private static class UserLogoutTask extends AsyncTask<Void, Void, Void> {
        private Runnable runnable;
        private DBHelper dbHelper;
        private SQLiteDatabase db;
        private Context context;

        public UserLogoutTask(Runnable runnable, Context context) {
            this.runnable = runnable;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dbHelper = new DBHelper(context);
            db = dbHelper.getWritableDatabase();
            db.delete(DBHelper.USERS_TABLE, null, null);
            db.close();
            dbHelper.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            loggedUser = null;
            if(runnable != null) {
                runnable.run();
            }
        }
    }

    public void setRole(RoleModel role) {
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLogin() {
        return login;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getLocale() {
        return locale;
    }

    public int getBalance() {
        return balance;
    }

    public String getIcon() {
        return icon;
    }

    public RoleModel getRole() {
        return role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getStatus() {
        return status;
    }

    public static void fetchUser(Context context, String userId, OnUserFetchListener fetchListener) {
        if(userId == null) {
            if(fetchListener != null) {
                fetchListener.fetchedUser(null);
            }
            return;
        }
        ApiBuilder builder = ApiBuilder.getInstance(context);
        builder.setResponseListener(new ApiBuilder.ResponseListener<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if(response.code() == 200) {
                    if(loggedUser == null && loggedUserId.equals(response.body().getId())) {
                        loggedUser = response.body();
                        if(fetchListener != null) {
                            fetchListener.fetchedUser(loggedUser);
                        }
                    }
                }
                else {
                    Toast.makeText(context, context.getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).getUserById(userId));
        });
    }

    public static void fetchLoggedUser(Context context, OnUserFetchListener fetchListener) {
        if(loggedUser == null) {
            new FetchUserTask(context, fetchListener).execute();
        }
        else if(fetchListener != null) {
            fetchListener.fetchedUser(loggedUser);
        }
    }

    public static void logout(Runnable runnable, Context context) {
        new UserLogoutTask(runnable, context).execute();
    }
}
