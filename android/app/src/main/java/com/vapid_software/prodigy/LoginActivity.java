package com.vapid_software.prodigy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.ConfirmDialog;
import com.vapid_software.prodigy.helpers.DBHelper;
import com.vapid_software.prodigy.models.AuthResponseModel;
import com.vapid_software.prodigy.models.LoginModel;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private View button, go;
    private EditText login;
    private boolean clickable = true;
    private Context context = this;

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String v = s.toString().trim();
            button.setVisibility(v.length() != 9 ? View.GONE : View.VISIBLE);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private View.OnClickListener goClicked = (View v) -> {
        startActivity(new Intent(this, CoreActivity.class));
    };

    private View.OnClickListener buttonClicked = (View v) -> {
        if(!clickable) return;
        clickable = false;
        String loginValue = "+996" + login.getText().toString();
        ConfirmDialog confirmDialog = new ConfirmDialog(
                this,
                loginValue,
                getResources().getString(R.string.login_confirm_title),
                null,
                null
        );
        confirmDialog.setOnPositiveConfirmListener(() -> {
            ApiBuilder builder = ApiBuilder.getInstance(this);
            builder.setResponseListener(new ApiBuilder.ResponseListener<AuthResponseModel>() {
                @Override
                public void onResponse(Call<AuthResponseModel> call, Response<AuthResponseModel> response) {
                    Intent intent = new Intent(context, ConfirmNumberActivity.class);
                    intent.putExtra("login", loginValue);
                    if(response.code() == 201) {
                        DBHelper dbHelper = new DBHelper(context);
                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                        Cursor cursor = db.query(DBHelper.USERS_TABLE, new String[]{"_id"},null,null,null,null,null);
                        if(cursor.getCount() == 0) {
                            intent.putExtra("access_token", response.body().getAccessToken());
                            intent.putExtra("user_id", response.body().getUser().getId());
                        }
                        cursor.close();
                        db.close();
                        dbHelper.close();
                        intent.putExtra("code", response.body().getCode());
                        startActivity(intent);
                    }
                    else if(response.code() == 400) {
                        try {
                            JSONObject payload = new JSONObject(response.errorBody().string());
                            intent.putExtra("newUser", true);
                            intent.putExtra("code", payload.getString("message"));
                            startActivity(intent);
                        }
                        catch (Exception e) {
                            Toast.makeText(context, context.getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(context, context.getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                    confirmDialog.dismiss();
                }
            });
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).login(new LoginModel(loginValue)));
            });
        });
        confirmDialog.setOnDismissListener((DialogInterface dialog) -> {
            clickable = true;
        });
        confirmDialog.show();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.login);
        button = findViewById(R.id.button);
        go = findViewById(R.id.go);
        go.setOnClickListener(goClicked);
        login.addTextChangedListener(watcher);
        button.setOnClickListener(buttonClicked);
    }
}