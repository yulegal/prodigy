package com.vapid_software.prodigy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.DBHelper;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.Utils;
import com.vapid_software.prodigy.models.AuthResponseModel;
import com.vapid_software.prodigy.models.UserModel;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private String login;
    private Button upload, remove, button;
    private TextView error;
    private File file;
    private ImageView image;
    private EditText name;
    private boolean clickable = true;
    private View go;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        login = getIntent().getStringExtra("login");
        button = findViewById(R.id.button);
        error = findViewById(R.id.error);
        upload = findViewById(R.id.action_upload);
        image = findViewById(R.id.image);
        remove = findViewById(R.id.action_remove);
        go = findViewById(R.id.go);
        name = findViewById(R.id.name);
        go.setOnClickListener(this);
        button.setOnClickListener(this);
        upload.setOnClickListener(this);
        remove.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Defs.PermissionCode.GALLERY_CODE && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Defs.ResultCode.GALLERY_CODE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.action_upload) {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.READ_MEDIA_IMAGES }, Defs.PermissionCode.GALLERY_CODE);
                return;
            }
            selectImage();
        }
        else if(id == R.id.go) {
            startActivity(new Intent(this, CoreActivity.class));
        }
        else if(id == R.id.action_remove) {
            file = null;
            v.setVisibility(View.GONE);
            image.setImageDrawable(getResources().getDrawable(R.drawable.avatar));
        }
        else if(id == R.id.button) {
            if(!clickable) return;
            clickable = false;
            String nameValue = name.getText().toString().trim();
            error.setVisibility(View.GONE);
            if(nameValue.equals("")) {
                error.setVisibility(View.VISIBLE);
                error.setText(getResources().getString(R.string.fill_name_error));
                clickable = true;
                return;
            }
            ApiBuilder builder = ApiBuilder.getInstance(this);
            builder.setResponseListener(new ApiBuilder.ResponseListener<AuthResponseModel>() {
                @Override
                public void onResponse(Call<AuthResponseModel> call, Response<AuthResponseModel> response) {
                    if(response.code() == 201) {
                        DBHelper dbHelper = new DBHelper(context);
                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                        UserModel userModel = response.body().getUser();
                        ContentValues values = new ContentValues();
                        values.put("access_token", response.body().getAccessToken());
                        values.put("user_id", userModel.getId());
                        db.insert(DBHelper.USERS_TABLE, null, values);
                        db.close();
                        dbHelper.close();
                        startActivity(new Intent(context, CoreActivity.class));
                    }
                    else {
                        Toast.makeText(context, context.getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                    clickable = true;
                }
            });
            MultipartBody.Part image[] = new MultipartBody.Part[1];
            if(file != null) {
                RequestBody rf = RequestBody.create(MultipartBody.FORM, file);
                image[0] = MultipartBody.Part.createFormData("file",file.getName(), rf);
            }
            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String locale = getResources().getConfiguration().getLocales().get(0).getLanguage();
            String token = "";
            db.close();
            dbHelper.close();
            builder.setOnInitializedListener((() -> {
                builder.send(builder.getApi(ApiService.class).register(
                        RequestBody.create(MultipartBody.FORM, login),
                        RequestBody.create(MultipartBody.FORM, nameValue),
                        RequestBody.create(MultipartBody.FORM, locale),
                        RequestBody.create(MultipartBody.FORM, token),
                        image[0]
                ));
            }));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Defs.ResultCode.GALLERY_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            file = new File(Utils.getMediaPath(uri, getContentResolver()));
            remove.setVisibility(View.VISIBLE);
            try {
                Bitmap bitmap = Utils.decodeBitmap(getContentResolver(), uri);
                image.setImageBitmap(bitmap);
            }
            catch(FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}