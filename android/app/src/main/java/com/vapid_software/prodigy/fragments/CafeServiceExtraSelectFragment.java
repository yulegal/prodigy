package com.vapid_software.prodigy.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.vapid_software.prodigy.R;

public class CafeServiceExtraSelectFragment extends Fragment {
    private Spinner options;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private SpinnerAdapter adapter;
    private View button;
    private OnCustomSeatsDefinedListener onCustomSeatsDefinedListener;

    public interface OnCustomSeatsDefinedListener {
        void onCustomSeatsDefined(int seats);
    }

    public void setOnCustomSeatsDefinedListener(OnCustomSeatsDefinedListener onCustomSeatsDefinedListener) {
        this.onCustomSeatsDefinedListener = onCustomSeatsDefinedListener;
    }

    public void setAdapter(SpinnerAdapter adapter) {
        this.adapter = adapter;
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    private View.OnClickListener buttonClicked = (View v) -> {
        Dialog dialog = new Dialog(getContext());
        View root = getLayoutInflater().inflate(R.layout.custom_seats_dialog, null);
        View close = root.findViewById(R.id.close);
        View button = root.findViewById(R.id.button);
        EditText amount = root.findViewById(R.id.amount);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        TextView error = root.findViewById(R.id.error);

        close.setOnClickListener((View view) -> {
            dialog.dismiss();
        });

        button.setOnClickListener((View view) -> {
            error.setVisibility(View.GONE);
            amount.setBackgroundResource(R.drawable.auth_input_wrp);
            String amountValue = amount.getText().toString();
            if(amountValue.isEmpty()) {
                error.setVisibility(View.VISIBLE);
                error.setText(getContext().getResources().getString(R.string.fill_in_the_amount));
                amount.setBackgroundResource(R.drawable.standard_input_e);
                return;
            }
            if(onCustomSeatsDefinedListener != null) {
                onCustomSeatsDefinedListener.onCustomSeatsDefined(Integer.parseInt(amountValue));
            }
            options.setSelection(0);
            dialog.dismiss();
        });

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.setContentView(root);
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cafe_service_extra_select, container, false);
        options = root.findViewById(R.id.options);
        button = root.findViewById(R.id.button);
        options.setOnItemSelectedListener(onItemSelectedListener);
        options.setAdapter(adapter);
        button.setOnClickListener(buttonClicked);
        return root;
    }
}