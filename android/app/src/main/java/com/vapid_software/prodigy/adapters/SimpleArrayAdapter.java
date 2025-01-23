package com.vapid_software.prodigy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vapid_software.prodigy.R;

public class SimpleArrayAdapter extends ArrayAdapter {
    private String payload[];
    public SimpleArrayAdapter(Context context, String[] payload) {
        super(context, 0);
        this.payload = payload;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.option_tv, parent, false);
        tv.setText(payload[position]);
        return tv;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.option_dd_tv, parent, false);
        tv.setText(payload[position]);
        return tv;
    }

    @Override
    public int getCount() {
        return payload.length;
    }
}
