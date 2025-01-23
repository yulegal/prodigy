package com.vapid_software.prodigy.api;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.GsonBuilder;
import com.vapid_software.prodigy.LoginActivity;
import com.vapid_software.prodigy.helpers.DBHelper;
import com.vapid_software.prodigy.helpers.NotificationBuilder;
import com.vapid_software.prodigy.helpers.Utils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiBuilder {
    public final static String HOST = "http://192.168.0.105:8000";
    public final static String BASE_PATH = HOST + "/api/";
    public final static String PUBLIC_PATH = BASE_PATH + "public_files";
    public final static String ADDONS_PATH = PUBLIC_PATH + "/addon";
    private Retrofit retrofit;
    private ResponseListener responseListener;
    private ErrorResponseListener errorResponseListener;
    private Context context;
    private OnInitializedListener initializedListener;

    public interface ResponseListener<T> {
        void onResponse(Call<T> call, Response<T> response);
    }

    public interface ErrorResponseListener<T> {
        void onFailure(Call<T> call, Throwable throwable);
    }

    public interface OnInitializedListener {
        void OnInitialized();
    }

    public void setOnInitializedListener(OnInitializedListener listener) {
        this.initializedListener = listener;
    }

    private class RemoveUserAsync extends AsyncTask<Void, Void, Void> {
        private DBHelper dbHelper;
        private SQLiteDatabase db;

        @Override
        protected Void doInBackground(Void... voids) {
            dbHelper = new DBHelper(context);
            db = dbHelper.getWritableDatabase();
            db.delete(DBHelper.USERS_TABLE, null, null);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            db.close();
            dbHelper.close();
            context.startActivity(new Intent(context, LoginActivity.class));
        }
    }

    private class NullableResponseFactory extends Converter.Factory {
        @Nullable
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
            return (ResponseBody responseBody) -> {
                if(responseBody.contentLength() == 0) return null;
                return delegate.convert(responseBody);
            };
        }
    }

    private class InitializeAsync extends AsyncTask<Void, Void, Void> {
        private DBHelper dbHelper;
        private SQLiteDatabase db;
        private Cursor cursor;

        @Override
        protected Void doInBackground(Void... voids) {
            dbHelper = new DBHelper(context);
            db = dbHelper.getReadableDatabase();
            cursor = db.query(DBHelper.USERS_TABLE, new String[]{"access_token"}, null, null, null,null,null);
            final StringBuilder s = new StringBuilder();
            if(cursor.getCount() != 0) {
                if(cursor.moveToFirst()) s.append(cursor.getString(0));
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_PATH)
                    .addConverterFactory(new NullableResponseFactory())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                    .client(new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Chain chain) throws IOException {
                            Request.Builder builder = chain.request().newBuilder();
                            if(s.length() != 0) builder = builder.header("Authorization", "bearer " + s);
                            return chain.proceed(builder.build());
                        }
                    }).build()).build();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            cursor.close();
            db.close();
            dbHelper.close();
            if(initializedListener != null) initializedListener.OnInitialized();
        }
    }

    private ApiBuilder(Context context) {
        this.context = context;
        if(!Utils.isNetworkAvailable(context)) {
            NotificationBuilder builder = NotificationBuilder.create(context);
            builder.getBuilder().setContentText("Check that your device has internet connection");
            builder.getBuilder().setContentTitle("Connection problem");
            builder.build();
        }
        else {
            new InitializeAsync().execute();
        }
    }

    public static ApiBuilder getInstance(Context context) {
        return new ApiBuilder(context);
    }

    public <T> T getApi(Class<T> apiClass) {
        return retrofit.create(apiClass);
    }

    public void setResponseListener(ResponseListener listener) {
        responseListener = listener;
    }

    public void setErrorResponseListener(ErrorResponseListener listener) {
        errorResponseListener = listener;
    }

    public <T> void send(Call<T> payload) {
        payload.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                int code = response.code();
                if(code == 401) {
                    new RemoveUserAsync().execute();
                    return;
                }
                if(responseListener != null) responseListener.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<T> call, Throwable throwable) {
                Log.e("error", throwable.getMessage());
                if(errorResponseListener != null) errorResponseListener.onFailure(call, throwable);
            }
        });
    }
}
