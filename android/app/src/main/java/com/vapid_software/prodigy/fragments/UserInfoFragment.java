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
import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.data.OptionsData;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.models.UserModel;

public class UserInfoFragment extends BaseExtraFragment {
    private UserModel user, loggedUser;
    private View backText, editBtn;
    private CoreActivity activity;
    private RecyclerView optionsRv;
    private TabLayout tabLayout;
    private ViewPager pager;
    private TextView name, status;
    private ImageView avatar;
    private String[] options;

    private class Adapter extends RecyclerView.Adapter {
        private final OptionsData options[] = new OptionsData[]{
                new OptionsData(
                        getContext().getResources().getString(R.string.call_option),
                        R.drawable.call,
                        Defs.ProfileOptionActions.CALL
                ),
                new OptionsData(
                        getContext().getResources().getString(R.string.mute_option),
                        R.drawable.mute,
                        Defs.ProfileOptionActions.MUTE
                ),
                new OptionsData(
                        getContext().getResources().getString(R.string.block_option),
                        R.drawable.block,
                        Defs.ProfileOptionActions.BLOCK
                ),
                new OptionsData(
                        getContext().getResources().getString(R.string.search_option),
                        R.drawable.search,
                        Defs.ProfileOptionActions.SEARCH
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
            if(data.getAction().equals(Defs.ProfileOptionActions.BLOCK)) {
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
        loggedUser = activity.getLoggedUser();
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    private View.OnClickListener editBtnClicked = (View v) -> {

    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_info, container, false);
        back = root.findViewById(R.id.back);
        tabLayout = root.findViewById(R.id.tab_layout);
        backText = root.findViewById(R.id.back_text);
        pager = root.findViewById(R.id.pager);
        avatar = root.findViewById(R.id.avatar);
        editBtn = root.findViewById(R.id.edit);
        status = root.findViewById(R.id.status);
        name = root.findViewById(R.id.name);
        optionsRv = root.findViewById(R.id.options_rv);
        editBtn.setOnClickListener(editBtnClicked);
        init();
        return root;
    }

    protected void init() {
        super.init();
        backText.setOnClickListener(backOnclickListener);
        options = getContext().getResources().getStringArray(R.array.profile_options);
        pager.setAdapter(new TabAdapter(activity.getSupportFragmentManager()));
        tabLayout.setupWithViewPager(pager);
        optionsRv.setLayoutManager(new GridLayoutManager(getContext(), 4));
        Adapter optionsAdapter = new Adapter();
        optionsAdapter.setOnOptionsDataSelectedListener((OptionsData data) -> {
            if(data.getAction().equals(Defs.ProfileOptionActions.CALL)) {

            }
            else if(data.getAction().equals(Defs.ProfileOptionActions.MUTE)) {

            }
            else if(data.getAction().equals(Defs.ProfileOptionActions.BLOCK)) {

            }
            else if(data.getAction().equals(Defs.ProfileOptionActions.SEARCH)) {

            }
        });
        optionsRv.setAdapter(optionsAdapter);
        name.setText(user.getName());
        if(user.getIcon() != null) {
            Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, user.getIcon())).into(avatar);
        }
        if(user.getStatus() != null) {
            status.setText(user.getStatus());
        }
        if(!loggedUser.getId().equals(user.getId())) {
            editBtn.setVisibility(View.GONE);
        }
        else {
            optionsRv.setVisibility(View.GONE);
        }
    }
}