package com.vapid_software.prodigy.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.adapters.OptionsAdapter;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.data.OptionsData;
import com.vapid_software.prodigy.helpers.ConfirmDialog;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.models.ChatModel;
import com.vapid_software.prodigy.models.MessageModel;
import com.vapid_software.prodigy.models.UserModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Response;

public class ChatsFragment extends BaseExtraFragment {
    private View empty, loader, add, searchWrp, mainWrp;
    private RecyclerView rv;
    private int page = 1;
    private int total;
    private List<ChatModel> chats;
    private CoreActivity activity;
    private UserModel loggedUser;
    private Socket socket;
    private String searchText;
    private final static int LIMIT = 20;

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageView avatar;
        private TextView body;
        private TextView date;
        private View photoIcon;
        private View check1, check;
        public CustomViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            body = v.findViewById(R.id.body);
            avatar = v.findViewById(R.id.avatar);
            date = v.findViewById(R.id.date);
            photoIcon = v.findViewById(R.id.photo_icon);
            check = v.findViewById(R.id.check);
            check1 = v.findViewById(R.id.check1);
        }

        public TextView getName() {
            return name;
        }

        public ImageView getAvatar() {
            return avatar;
        }

        public TextView getBody() {
            return body;
        }

        public TextView getDate() {
            return date;
        }

        public View getPhotoIcon() {
            return photoIcon;
        }

        public View getCheck1() {
            return check1;
        }

        public View getCheck() {
            return check;
        }
    }

    private class Adapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = getLayoutInflater().inflate(R.layout.chats_item, parent, false);
            CustomViewHolder holder = new CustomViewHolder(root);
            root.setOnLongClickListener((View v) -> {
                View view = getLayoutInflater().inflate(R.layout.chat_options_dialog, null);
                RecyclerView rv = view.findViewById(R.id.rv);
                int w = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        130,
                        getContext().getResources().getDisplayMetrics()
                );
                int coords[] = new int[2];
                int sw = getContext().getResources().getDisplayMetrics().widthPixels;
                int x = (sw - w) / 2;
                root.getLocationOnScreen(coords);
                PopupWindow win = new PopupWindow(view, w, WindowManager.LayoutParams.WRAP_CONTENT);
                rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                OptionsAdapter optionsAdapter = new OptionsAdapter(new OptionsData[]{
                        new OptionsData(
                                getContext().getResources().getString(R.string.chat_option_delete),
                                R.drawable.delete,
                                Defs.ChatOptions.DELETE
                        )
                });
                optionsAdapter.setOnOptionsSelectedListener((OptionsData data) -> {
                    if(data.getAction().equals(Defs.ChatOptions.DELETE)) {
                        ConfirmDialog confirmDialog = new ConfirmDialog(
                                getContext(),
                                getContext().getResources().getString(R.string.remove_chat_confirm_title),
                                getContext().getResources().getString(R.string.remove_chat_confirm_body),
                                null,
                                null
                        );
                        confirmDialog.setOnPositiveConfirmListener(() -> {
                            ApiBuilder builder = ApiBuilder.getInstance(getContext());
                            builder.setOnInitializedListener(() -> {
                                builder.send(builder.getApi(ApiService.class).deleteChatById(chats.get(holder.getAdapterPosition()).getId()));
                            });
                            builder.setResponseListener(new ApiBuilder.ResponseListener() {
                                @Override
                                public void onResponse(Call call, Response response) {
                                    if(response.code() == 200) {
                                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.deleted), Toast.LENGTH_LONG).show();
                                        chats.remove(chats.get(holder.getAdapterPosition()).getId());
                                        notifyItemRemoved(holder.getAdapterPosition());
                                        if(chats.size() == 0) {
                                            rv.setVisibility(View.GONE);
                                            empty.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    else {
                                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            confirmDialog.dismiss();
                        });
                        confirmDialog.show();
                    }
                    win.dismiss();
                });
                rv.setAdapter(optionsAdapter);
                win.setOutsideTouchable(true);
                win.showAtLocation(view, Gravity.NO_GRAVITY, x, coords[1] + v.getHeight());
                return true;
            });
            root.setOnClickListener((View v) -> {
                ChatFragment fragment = new ChatFragment();
                ChatModel chat = chats.get(holder.getAdapterPosition());
                UserModel user = chat.getUser1().getId().equals(loggedUser.getId()) ? chat.getUser2() : chat.getUser1();
                fragment.setCurrentUser(user);
                fragment.setOnBackPressedListener(() -> {
                    activity.getSupportFragmentManager().popBackStack();
                    page = 1;
                    chats = null;
                    rv.setAdapter(null);
                    load();
                });
                activity.loadExtra(fragment, true);
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
            CustomViewHolder holder = (CustomViewHolder) h;
            Calendar c = Calendar.getInstance();
            ChatModel chat = chats.get(holder.getAdapterPosition());
            MessageModel message = chat.getMessage();
            long dayHours = 3600 * 24 * 1000;
            long diff = c.getTimeInMillis() - message.getCreatedAt();
            SimpleDateFormat dateFormat = new SimpleDateFormat(diff / dayHours == 0 ? "HH:mm" : "dd.MM.YYYY");
            UserModel user = chat.getUser1().getId().equals(loggedUser.getId()) ? chat.getUser2() : chat.getUser1();
            if(user.getIcon() != null) {
                Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, user.getIcon())).into(holder.getAvatar());
            }
            if(message.getBody() != null) {
                holder.getBody().setText(message.getBody());
            }
            else if(message.getAddons() != null) {
                holder.getPhotoIcon().setVisibility(View.VISIBLE);
                holder.getBody().setText(String.join(" ", String.valueOf(message.getAddons().size()), getContext().getResources().getString(R.string.photos)));
            }
            if(message.getFrom().getId().equals(loggedUser.getId())) {
                holder.getCheck().setVisibility(View.VISIBLE);
                if(message.isRead()) {
                    holder.getCheck1().setVisibility(View.VISIBLE);
                }
            }
            holder.getName().setText(user.getName());
            holder.getDate().setText(dateFormat.format(new Date(message.getCreatedAt())));
        }

        @Override
        public int getItemCount() {
            return chats.size();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
        loggedUser = activity.getLoggedUser();
        socket = activity.getSocket();
    }

    private Emitter.Listener chatRemovedListener = (Object ...args) -> {
        if(chats == null) return;
        String id = (String) args[0];
        activity.runOnUiThread(() -> {
            for(ChatModel chat : chats) {
                if(chat.getId().equals(id)) {
                    int i = chats.indexOf(chat);
                    chats.remove(i);
                    rv.getAdapter().notifyItemRemoved(i);
                    if(chats.size() == 0) {
                        empty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    }
                    break;
                }
            }
        });
    };

    private Emitter.Listener messageReadListener = (Object ...args) -> {
        if(chats == null) return;
        Gson gson = new Gson();
        MessageModel message = gson.fromJson((String) args[0], MessageModel.class);
        activity.runOnUiThread(() -> {
            for(ChatModel chat : chats) {
                if(chat.getId().equals(message.getChat().getId())) {
                    int i = chats.indexOf(chat);
                    chat.getMessage().setRead(true);
                    rv.getAdapter().notifyItemChanged(i);
                    break;
                }
            }
        });
    };

    private Emitter.Listener messageDeletedListener = (Object ...args) -> {
        if(chats == null) return;
        String msgId = (String) args[0];
        activity.runOnUiThread(() -> {
            for(ChatModel chat : chats) {
                if(chat.getMessage().getId().equals(msgId)) {
                    ApiBuilder builder = ApiBuilder.getInstance(getContext());
                    builder.setResponseListener(new ApiBuilder.ResponseListener<ChatModel>() {
                        @Override
                        public void onResponse(Call<ChatModel> call, Response<ChatModel> response) {
                            if(response.code() == 200) {
                                int i = chats.indexOf(chat);
                                chats.set(i, response.body());
                                rv.getAdapter().notifyItemChanged(i);
                            }
                            else {
                                Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    builder.setOnInitializedListener(() -> {
                        builder.send(builder.getApi(ApiService.class).getChatById(chat.getId()));
                    });
                    break;
                }
            }
        });
    };

    private Emitter.Listener newMessageListener = (Object ...args) -> {
        activity.runOnUiThread(() -> {
            page = 1;
            chats = null;
            load();
        });
    };

    private void initSockets() {
        socket.on(Defs.WS_MESSAGES.SERVER.CHAT_REMOVED, chatRemovedListener);
        socket.on(Defs.WS_MESSAGES.SERVER.MESSAGE_READ, messageReadListener);
        socket.on(Defs.WS_MESSAGES.SERVER.MESSAGE_DELETED, messageDeletedListener);
        socket.on(Defs.WS_MESSAGES.SERVER.NEW_MESSAGE, newMessageListener);
    }

    private void finishSockets() {
        socket.off(Defs.WS_MESSAGES.SERVER.CHAT_REMOVED, chatRemovedListener);
        socket.off(Defs.WS_MESSAGES.SERVER.MESSAGE_READ, messageReadListener);
        socket.off(Defs.WS_MESSAGES.SERVER.MESSAGE_DELETED, messageDeletedListener);
        socket.off(Defs.WS_MESSAGES.SERVER.NEW_MESSAGE, newMessageListener);
    }

    private View.OnClickListener addClicked = (View v) -> {
        mainWrp.setVisibility(View.GONE);
        searchWrp.setVisibility(View.VISIBLE);
        SelectContactDialogFragment contactFragment = new SelectContactDialogFragment();
        contactFragment.setTitle(getContext().getResources().getString(R.string.new_message_title));
        contactFragment.setOnBackPressedListener(() -> {
            activity.getSupportFragmentManager().popBackStack();
            mainWrp.setVisibility(View.VISIBLE);
            searchWrp.setVisibility(View.GONE);
        });
        contactFragment.setOnContactSelectedListener((UserModel contact) -> {
            activity.getSupportFragmentManager().popBackStack();
            ChatFragment fragment = new ChatFragment();
            fragment.setCurrentUser(contact);
            fragment.setOnBackPressedListener(() -> {
                activity.getSupportFragmentManager().popBackStack();
                mainWrp.setVisibility(View.VISIBLE);
                searchWrp.setVisibility(View.GONE);
                page = 1;
                chats = null;
                load();
            });
            activity.loadExtra(fragment, true);
        });
        activity.loadExtra(contactFragment, true);
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chats, container, false);
        empty = root.findViewById(R.id.empty);
        loader = root.findViewById(R.id.loader);
        add = root.findViewById(R.id.add);
        rv = root.findViewById(R.id.rv);
        searchWrp = root.findViewById(R.id.search_wrp);
        mainWrp = root.findViewById(R.id.main_wrp);
        back = root.findViewById(R.id.back);
        add.setOnClickListener(addClicked);
        init();
        return root;
    }

    @Override
    protected void init() {
        super.init();
        load();
    }

    private FilterQueryOptions getFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        return new FilterQueryOptions(page, LIMIT, items);
    }

    private void load() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterChats(getFilterQueryOptions()));
        });
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<ChatModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<ChatModel>> call, Response<FilterResponse<ChatModel>> response) {
                if(response.code() == 201) {
                    total = response.body().getCount();
                    loader.setVisibility(View.GONE);
                    if(response.body().getTotal() == 0 && total == 0) {
                        empty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    }
                    else {
                        empty.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        if(chats == null) {
                            chats = new ArrayList<>();
                        }
                        chats.addAll(response.body().getData());
                        if(rv.getLayoutManager() == null) {
                            rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        }
                        if(rv.getAdapter() == null || page == 1) {
                            rv.setAdapter(new Adapter());
                        }
                        else {
                            rv.getAdapter().notifyItemRangeInserted(chats.size() - response.body().getTotal(), response.body().getTotal());
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        initSockets();
        super.onStart();
    }

    @Override
    public void onStop() {
        finishSockets();
        super.onStop();
    }
}