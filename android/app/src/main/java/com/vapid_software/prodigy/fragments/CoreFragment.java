package com.vapid_software.prodigy.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.LoginActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.adapters.CategoryAdapter;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.models.CategoryModel;
import com.vapid_software.prodigy.models.UserModel;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Response;

public class CoreFragment extends Fragment implements CoreActivity.OnUserLoggedOutListener {
    private CoreActivity activity;
    private Toolbar tb;
    private Fragment currentFragment;
    private BottomNavigationView bottomNavigationView;
    private UserModel loggedUser;
    private Socket socket;
    private BadgeDrawable badge;
    private Emitter.Listener notificationsCountListener = (Object ...args) -> {
        getNotificationsCount();
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
        loggedUser = activity.getLoggedUser();
        activity.setOnUserLoggedOutListener(this);
        socket = activity.getSocket();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        boolean isSelected = id == R.id.notifications || id == R.id.profile || id == R.id.categories;
        if(isSelected) {
            boolean needLoad = false;
            if(id == R.id.notifications && !(currentFragment instanceof NotificationsFragment)) {
                NotificationsFragment notificationsFragment = new NotificationsFragment();
                notificationsFragment.setOnBackPressedListener(() -> {
                    activity.hideExtra();
                    getNotificationsCount();
                });
                activity.loadExtra(notificationsFragment);
            }
            else if(id == R.id.categories) {
                Dialog dialog = new Dialog(getContext());
                View root = getLayoutInflater().inflate(R.layout.categories_dialog, null);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                View close = root.findViewById(R.id.close);
                RecyclerView rv = root.findViewById(R.id.rv);
                View loader = root.findViewById(R.id.loader);

                close.setOnClickListener((View v) -> {
                    dialog.dismiss();
                });

                ApiBuilder builder = ApiBuilder.getInstance(getContext());
                builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<CategoryModel>>() {
                    @Override
                    public void onResponse(Call<FilterResponse<CategoryModel>> call, Response<FilterResponse<CategoryModel>> response) {
                        if(response.code() == 201) {
                            rv.setVisibility(View.VISIBLE);
                            loader.setVisibility(View.GONE);
                            rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                            CategoryAdapter adapter = new CategoryAdapter(response.body().getData());
                            adapter.setOnCategoryItemClickedListener((CategoryModel category) -> {
                                dialog.dismiss();
                                ServicesFragment servicesFragment = new ServicesFragment();
                                servicesFragment.setCategory(category);
                                servicesFragment.setOnBackPressedListener(activity);
                                activity.loadExtra(servicesFragment);
                            });
                            rv.setAdapter(adapter);
                        }
                        else {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setOnInitializedListener(() -> {
                    builder.send(builder.getApi(ApiService.class).filterCategories(new FilterQueryOptions(null)));
                });

                lp.width = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        300,
                        getContext().getResources().getDisplayMetrics()
                );
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                dialog.setContentView(root);
                dialog.getWindow().setAttributes(lp);
                dialog.show();
            }
            else if(id == R.id.profile) {
                if(loggedUser == null) {
                    startActivity(new Intent(getContext(), LoginActivity.class));
                }
                else {
                    activity.getDrawer().openDrawer(Gravity.RIGHT);
                }
            }
            if(needLoad) {
                load(false);
            }
        }
        return isSelected;
    }

    private NavigationBarView.OnItemSelectedListener bottomNavigationListener = (MenuItem item) -> {
        int id = item.getItemId();
        boolean isSelected = id == R.id.home || id == R.id.favorites || id == R.id.history || id == R.id.bookings;
        if(isSelected) {
            boolean needLoad = false;
            if(id == R.id.home && !(currentFragment instanceof HomeFragment)) {
                currentFragment = new HomeFragment();
                needLoad = true;
            }
            else if(id == R.id.favorites && !(currentFragment instanceof FavoritesFragment)) {
                currentFragment = new FavoritesFragment();
                needLoad = true;
            }
            else if(id == R.id.history && !(currentFragment instanceof HistoryFragment)) {
                currentFragment = new HistoryFragment();
                needLoad = true;
            }
            else if(id == R.id.bookings && !(currentFragment instanceof BookingsFragment)) {
                currentFragment = new BookingsFragment();
                needLoad = true;
            }
            if(needLoad) {
                load(false);
            }
        }
        return isSelected;
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_core, container, false);
        tb = root.findViewById(R.id.tb);
        bottomNavigationView = root.findViewById(R.id.bottom_nav);
        activity.setSupportActionBar(tb);
        activity.getSupportActionBar().setTitle(getContext().getResources().getString(R.string.app_name));
        bottomNavigationView.setOnItemSelectedListener(bottomNavigationListener);
        bottomNavigationView.setSelectedItemId(R.id.home);
        setHasOptionsMenu(true);
        init();
        return root;
    }

    private void init() {
        if(loggedUser != null) {
            getNotificationsCount();
        }
    }

    public void setCurrentFragment(Fragment currentFragment) {
        this.currentFragment = currentFragment;
    }

    public void load(boolean add) {
        if(currentFragment == null) currentFragment = new HomeFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if(add) {
            transaction.add(R.id.core_wrapper, currentFragment);
        }
        else {
            transaction.replace(R.id.core_wrapper, currentFragment);
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void getNotificationsCount() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<Integer>() {
            @OptIn(markerClass = ExperimentalBadgeUtils.class)
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(response.code() == 200) {
                    if(response.body() != 0) {
                        if(badge == null) {
                            badge = BadgeDrawable.create(getContext());
                        }
                        badge.setNumber(response.body());
                        BadgeUtils.attachBadgeDrawable(badge, tb, R.id.notifications);
                    }
                    else if(badge != null) {
                        BadgeUtils.detachBadgeDrawable(badge, tb, R.id.notifications);
                        badge = null;
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).getNotificationsCount());
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        initSockets();
    }

    private void initSockets() {
        socket.on(Defs.NotificationType.NEW_BROADCAST, notificationsCountListener);
        socket.on(Defs.NotificationType.NEW_BOOKING, notificationsCountListener);
        socket.on(Defs.NotificationType.BOOKING_CANCELED, notificationsCountListener);
        socket.on(Defs.NotificationType.BOOKING_FINISHED, notificationsCountListener);
        socket.on(Defs.NotificationType.FEE_CHARGED, notificationsCountListener);
        socket.on(Defs.NotificationType.REBOOKED, notificationsCountListener);
        socket.on(Defs.NotificationType.PAYMENT_PERIOD_APPROACHES, notificationsCountListener);
        socket.on(Defs.NotificationType.SERVICE_BLOCKED_DUE_TO_LACK_BALANCE, notificationsCountListener);
        socket.on(Defs.NotificationType.USER_ADDED_TO_BRANCH, notificationsCountListener);
        socket.on(Defs.NotificationType.USER_REMOVED_FROM_BRANCH, notificationsCountListener);
        socket.on(Defs.NotificationType.TRIAL_PERIOD_END_APPROACHES, notificationsCountListener);
    }

    private void finishSockets() {
        socket.off(Defs.NotificationType.NEW_BROADCAST, notificationsCountListener);
        socket.off(Defs.NotificationType.NEW_BOOKING, notificationsCountListener);
        socket.off(Defs.NotificationType.BOOKING_CANCELED, notificationsCountListener);
        socket.off(Defs.NotificationType.BOOKING_FINISHED, notificationsCountListener);
        socket.off(Defs.NotificationType.FEE_CHARGED, notificationsCountListener);
        socket.off(Defs.NotificationType.REBOOKED, notificationsCountListener);
        socket.off(Defs.NotificationType.PAYMENT_PERIOD_APPROACHES, notificationsCountListener);
        socket.off(Defs.NotificationType.SERVICE_BLOCKED_DUE_TO_LACK_BALANCE, notificationsCountListener);
        socket.off(Defs.NotificationType.USER_ADDED_TO_BRANCH, notificationsCountListener);
        socket.off(Defs.NotificationType.USER_REMOVED_FROM_BRANCH, notificationsCountListener);
        socket.off(Defs.NotificationType.TRIAL_PERIOD_END_APPROACHES, notificationsCountListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        finishSockets();
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    public void onUserLoggedOut() {
        loggedUser = null;
        if(badge != null) {
            BadgeUtils.detachBadgeDrawable(badge, tb, R.id.notifications);
            badge = null;
        }
    }
}