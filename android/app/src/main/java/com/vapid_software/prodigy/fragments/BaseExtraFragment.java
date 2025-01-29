package com.vapid_software.prodigy.fragments;

import android.view.View;

import androidx.fragment.app.Fragment;

public abstract class BaseExtraFragment extends Fragment {
    protected OnBackPressedListener onBackPressedListener;
    protected View back;
    protected final View.OnClickListener backOnclickListener = (View v) -> {
        if(onBackPressedListener != null) {
            onBackPressedListener.OnExtraBackPressed();
        }
    };

    public interface OnBackPressedListener {
        void OnExtraBackPressed();
    }

    public void setOnBackPressedListener(ServicesFragment.OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    protected void init() {
        if(back != null) {
            back.setOnClickListener(backOnclickListener);
        }
    }
}
