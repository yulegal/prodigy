package com.vapid_software.prodigy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.vapid_software.prodigy.helpers.DBHelper;

public class ConfirmNumberActivity extends AppCompatActivity {
    private RecyclerView rv;
    private String codes[] = new String[6];
    private View button, go;
    private TextView error;
    private boolean clickable = true;
    private String code, login, userId, accessToken;
    private boolean newUser;

    private OnNumberChangeListener numberChangeListener = (int position, String value) -> {
        codes[position] = value;
        boolean allFilled = true;
        for(int i = 0;i < 6; ++i) {
            if(codes[i] == null || codes[i].equals("")) {
                allFilled = false;
                break;
            }
        }
        button.setVisibility(allFilled ? View.VISIBLE : View.GONE);
        if(value.equals("") || position == 5) return;
        EditText editText = (EditText) rv.findViewHolderForAdapterPosition(position + 1).itemView;
        editText.requestFocus();
    };

    private interface OnNumberChangeListener {
        void OnNumberChanged(int position, String value);
    }

    private RecyclerView.Adapter adapter = new RecyclerView.Adapter() {

        class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View v) {
                super(v);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(viewType != 0 ? R.layout.confirm_number_input : R.layout.confirm_number_input_m, parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            EditText editText = (EditText) holder.itemView;
            if(position == 0) {
                editText.requestFocus();
            }
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    numberChangeListener.OnNumberChanged(holder.getAdapterPosition(), s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            return position != 5 ? 0 : 1;
        }

        @Override
        public int getItemCount() {
            return 6;
        }
    };

    private View.OnClickListener buttonClicked = (View v) -> {
        if(!clickable) return;
        clickable = false;
        error.setVisibility(View.GONE);
        if(!code.equals(String.join("", codes))) {
            error.setVisibility(View.VISIBLE);
            error.setText(getResources().getString(R.string.incorrect_code));
            clickable = true;
            return;
        }
        if(newUser) {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra("login", login);
            startActivity(intent);
            return;
        }
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if(userId != null && accessToken != null) {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("access_token", accessToken);
            db.insert(DBHelper.USERS_TABLE, null, values);
        }
        db.close();
        dbHelper.close();
        startActivity(new Intent(this, CoreActivity.class));
    };

    private View.OnClickListener goClicked = (View v) -> {
        startActivity(new Intent(this, CoreActivity.class));
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_number);
        code = getIntent().getStringExtra("code");
        userId = getIntent().getStringExtra("user_id");
        go = findViewById(R.id.go);
        error = findViewById(R.id.error);
        accessToken = getIntent().getStringExtra("access_token");
        login = getIntent().getStringExtra("login");
        newUser = getIntent().getBooleanExtra("newUser", false);
        rv = findViewById(R.id.rv);
        button = findViewById(R.id.button);
        button.setOnClickListener(buttonClicked);
        go.setOnClickListener(goClicked);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);
    }
}