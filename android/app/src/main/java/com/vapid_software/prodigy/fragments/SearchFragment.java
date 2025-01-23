package com.vapid_software.prodigy.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.vapid_software.prodigy.R;

public class SearchFragment extends Fragment {
    private EditText search;
    private OnSearchChangeListener onSearchChangeListener;

    public interface OnSearchChangeListener {
        void onSearchChange(String text);
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(onSearchChangeListener != null) {
                onSearchChangeListener.onSearchChange(s.toString().trim());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void setOnSearchChangeListener(OnSearchChangeListener onSearchChangeListener) {
        this.onSearchChangeListener = onSearchChangeListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        search = root.findViewById(R.id.search);
        search.addTextChangedListener(watcher);
        return root;
    }
}