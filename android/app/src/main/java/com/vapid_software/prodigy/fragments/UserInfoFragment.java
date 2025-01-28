package com.vapid_software.prodigy.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.data.OptionsData;
import com.vapid_software.prodigy.models.UserModel;

public class UserInfoFragment extends Fragment {
    private UserModel user;
    private View back, backText;
    private CoreActivity activity;
    private RecyclerView optionsRv;
    private TabLayout tabLayout;
    private ViewPager pager;
    private String[] options;

    private class Adapter extends RecyclerView.Adapter {
        private OptionsData options[] = new OptionsData[]{
                new OptionsData(
                        getContext().getResources().getString(R.string.call_option),
                        R.drawable.call,
                        "call"
                ),
                new OptionsData(
                        getContext().getResources().getString(R.string.mute_option),
                        R.drawable.mute,
                        "mute"
                ),
                new OptionsData(
                        getContext().getResources().getString(R.string.block_option),
                        R.drawable.block,
                        "block"
                ),
                new OptionsData(
                        getContext().getResources().getString(R.string.search_option),
                        R.drawable.search,
                        "search"
                )
        };
        private OptionsData.OnOptionsDataSelectedListener onOptionsDataSelectedListener;

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView icon;
            private TextView name;
            public CustomViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.name);
                icon = v.findViewById(R.id.icon);
                v.setOnClickListener((View v1) -> {
                    if(onOptionsDataSelectedListener != null) {
                        onOptionsDataSelectedListener.onOptionsDataSelected(options[getAdapterPosition()]);
                    }
                });
            }

            public ImageView getIcon() {
                return icon;
            }

            public TextView getName() {
                return name;
            }
        }

        public void setOnOptionsDataSelectedListener(OptionsData.OnOptionsDataSelectedListener onOptionsDataSelectedListener) {
            this.onOptionsDataSelectedListener = onOptionsDataSelectedListener;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CustomViewHolder(getLayoutInflater().inflate(R.layout.user_info_options_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
            CustomViewHolder holder = (CustomViewHolder) h;
            OptionsData data = options[position];
            holder.getName().setText(data.getText());
            holder.getIcon().setImageResource(data.getIcon());
            if(data.getAction().equals("block")) {
                holder.getName().setTextColor(Color.parseColor("#bb0000"));
                holder.getIcon().setColorFilter(Color.parseColor("#bb0000"));
            }
            else {
                holder.getName().setTextColor(Color.parseColor("#336699"));
                holder.getIcon().setColorFilter(Color.parseColor("#336699"));
            }
        }

        @Override
        public int getItemCount() {
            return options.length;
        }
    }

    private class TabAdapter extends FragmentPagerAdapter {

        public TabAdapter(FragmentManager manager) {
            super(manager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: return new AboutFragment();
                case 1: return new PostsFragment();
                case 2: return new PhotosFragment();
            }
            return null;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return options[position];
        }

        @Override
        public int getCount() {
            return options.length;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    private View.OnClickListener backClicked = (View v) -> {
        activity.getSupportFragmentManager().popBackStack();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_info, container, false);
        back = root.findViewById(R.id.back);
        tabLayout = root.findViewById(R.id.tab_layout);
        backText = root.findViewById(R.id.back_text);
        pager = root.findViewById(R.id.pager);
        optionsRv = root.findViewById(R.id.options_rv);
        back.setOnClickListener(backClicked);
        backText.setOnClickListener(backClicked);
        init();
        return root;
    }

    private void init() {
        options = getContext().getResources().getStringArray(R.array.profile_options);
        pager.setAdapter(new TabAdapter(activity.getSupportFragmentManager()));
        tabLayout.setupWithViewPager(pager);
        optionsRv.setLayoutManager(new GridLayoutManager(getContext(), 4));
        Adapter optionsAdapter = new Adapter();
        optionsAdapter.setOnOptionsDataSelectedListener((OptionsData data) -> {
            Log.i("action", data.getAction());
        });
        optionsRv.setAdapter(optionsAdapter);
    }
}