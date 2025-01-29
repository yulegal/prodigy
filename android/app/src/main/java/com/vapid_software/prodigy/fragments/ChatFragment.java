package com.vapid_software.prodigy.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.emoji2.emojipicker.EmojiPickerView;
import androidx.emoji2.emojipicker.EmojiViewItem;
import androidx.emoji2.widget.EmojiTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.CoreActivity;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.api.ApiService;
import com.vapid_software.prodigy.data.BaseData;
import com.vapid_software.prodigy.helpers.ConfirmDialog;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.FilterItem;
import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.helpers.OnSwipeTouchListener;
import com.vapid_software.prodigy.helpers.Utils;
import com.vapid_software.prodigy.helpers.ViewHolder;
import com.vapid_software.prodigy.models.ForwardMessageModel;
import com.vapid_software.prodigy.models.MessageModel;
import com.vapid_software.prodigy.models.MessageRatingResponseModel;
import com.vapid_software.prodigy.models.UserModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class ChatFragment extends BaseExtraFragment {
    private UserModel currentUser, loggedUser;
    private ImageView avatar;
    private TextView name, status, newCount;
    private View send, add, mediaWrp, replyClose, replyWrp, buttonDown;
    private RecyclerView rv, selectedAddonsRv, replyAddonsRv;
    private CoreActivity activity;
    private Socket socket;
    private List<MessageModel> messages;
    private EditText message;
    private List<AddonData> addons, selectedAddons = new ArrayList<>();
    private int page = 1;
    private int optionsMessagePosition = -1;
    private MediaRecorder recorder;
    private Runnable recordRunnable;
    private Handler recordHandler;
    private TextView recordTime;
    private View recordIcon, recordWrp, recordStop;
    private String recordFileName;
    private int recordedTime;
    private int total;
    private View optionsWrp, bottomWrp, topMainWrp, topOptionsWrp, topOptionsClose, mic;
    private MessageModel replyMessage, editMessage;
    private View optionsDeleteWrp, optionsDelete, optionsForward, optionsReply, optionsEmoji, optionsEdit, optionsEditWrp;
    private TextView replyName, replyBody, currentDate;
    private final static int SWIPE_ANIMATION_OFFSET = 100;
    private final static int SWIPE_ANIMATION_DURATION = 100;
    private final static int LIMIT = 20;

    private class MediaAdapter extends RecyclerView.Adapter {
        private List<String> payload;
        private OnSwipeTouchListener onSwipeTouchListener;
        private int position;

        private class MediaViewHolder extends RecyclerView.ViewHolder {
            private ImageView image;
            private View play;
            public MediaViewHolder(View v) {
                super(v);
                play = v.findViewById(R.id.play);
                image = v.findViewById(R.id.image);
            }

            public ImageView getImage() {
                return image;
            }

            public View getPlay() {
                return play;
            }
        }

        private class AudioViewHolder extends RecyclerView.ViewHolder {
            private ImageView play;
            private ProgressBar pb;
            private TextView time, currentTime;
            public AudioViewHolder(View v) {
                super(v);
                play = v.findViewById(R.id.play);
                pb = v.findViewById(R.id.pb);
                time = v.findViewById(R.id.time);
                currentTime = v.findViewById(R.id.current_time);
                play.setOnClickListener((View v1) -> {
                    MediaPlayer player = new MediaPlayer();
                    String url = String.join("/", ApiBuilder.ADDONS_PATH, payload.get(getAdapterPosition()));
                    player.setOnCompletionListener((MediaPlayer mp) -> {
                        mp.release();
                        play.setImageResource(R.drawable.play);
                    });
                    try {
                        player.setDataSource(url);
                        player.prepare();
                        player.start();
                        play.setImageResource(R.drawable.pause);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            public ProgressBar getPb() {
                return pb;
            }

            public TextView getTime() {
                return time;
            }

            public ImageView getPlay() {
                return play;
            }

            public TextView getCurrentTime() {
                return currentTime;
            }
        }

        public MediaAdapter(List<String> payload, int position) {
            this.payload = payload;
            this.position = position;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = getLayoutInflater().inflate(viewType == 1 ? R.layout.message_row_media_audio : R.layout.message_row_media, parent, false);
            RecyclerView.ViewHolder holder = viewType == 1 ? new AudioViewHolder(root) : new MediaViewHolder(root);
            root.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
                @Override
                public void onSwipeLeft() {
                    if(onSwipeTouchListener != null) {
                        onSwipeTouchListener.onSwipeLeft();
                    }
                }

                @Override
                public void onSwipeRight() {
                    if(onSwipeTouchListener != null) {
                        onSwipeTouchListener.onSwipeRight();
                    }
                }

                @Override
                public void onClick(View v) {
                    if(viewType == 0) {
                        openMedia(holder.getAdapterPosition());
                    }
                }

                @Override
                public void onLongPress(View v) {
                    if(onSwipeTouchListener != null) {
                        onSwipeTouchListener.onLongPress(v);
                    }
                }
            });
            return holder;
        }

        public void setOnSwipeTouchListener(OnSwipeTouchListener onSwipeTouchListener) {
            this.onSwipeTouchListener = onSwipeTouchListener;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
            String url = String.join("/", ApiBuilder.ADDONS_PATH, payload.get(h.getAdapterPosition()));
            String mime = Utils.getMimeType(url);
            if(!mime.endsWith("3gpp")) {
                MediaViewHolder holder = (MediaViewHolder) h;
                if(mime.startsWith("image")) {
                    Picasso.get().load(url).into(holder.getImage());
                }
                if(mime.startsWith("video")) {
                    Glide.with(getContext()).load(url).into(holder.getImage());
                    holder.getPlay().setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            String url = String.join("/", ApiBuilder.ADDONS_PATH, payload.get(position));
            String mime = Utils.getMimeType(url);
            return mime.endsWith("3gpp") ? 1 : 0;
        }

        private void openMedia(int current) {
            mediaWrp.setVisibility(View.VISIBLE);
            getChildFragmentManager().addOnBackStackChangedListener(() -> {
                if(getChildFragmentManager().getFragments().size() == 0) {
                    mediaWrp.setVisibility(View.GONE);
                }
            });
            optionsMessagePosition = position;
            MediaPlayFragment playFragment = new MediaPlayFragment();
            playFragment.setUrlList(payload);
            playFragment.setCurrent(current);
            playFragment.setOnForwardClickedListener(() -> {
                activity.getSupportFragmentManager().popBackStack();
                forwardMessage();
            });
            playFragment.setOnReplyClickedListener(() -> {
                activity.getSupportFragmentManager().popBackStack();
                reply();
            });
            playFragment.setOnDeleteClickedListener(() -> {
                deleteMessage(() -> {
                    activity.getSupportFragmentManager().popBackStack();
                });
            });
            playFragment.setOnBackPressedListener(() -> {
                optionsMessagePosition = -1;
                activity.getSupportFragmentManager().popBackStack();
            });
            activity.loadExtra(playFragment, true);
        }

        @Override
        public int getItemCount() {
            return payload.size();
        }
    }

    private class RatingAdapter extends RecyclerView.Adapter {
        private List<BaseData> ratings;

        public RatingAdapter(List<BaseData> ratings) {
            this.ratings = ratings;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.chat_item_rating, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            EmojiTextView tv = (EmojiTextView) holder.itemView;
            BaseData data = ratings.get(holder.getAdapterPosition());
            tv.setText(data.getName());
        }

        @Override
        public int getItemCount() {
            return ratings.size();
        }
    }

    private class Adapter extends RecyclerView.Adapter {
        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private View forwardWrp, container, box;
            private TextView forwardName, body, date;
            private ImageView forwardAvatar, avatar;
            private View replyWrp, edited, check;
            private TextView replyName, replyBody;
            private RecyclerView mediaRv, replyAddonsRv, emojisRv;
            private OnSwipeTouchListener onSwipeTouchListener;
            public CustomViewHolder(View v) {
                super(v);
                forwardWrp = v.findViewById(R.id.forward_wrp);
                container = v.findViewById(R.id.container);
                box = v.findViewById(R.id.box);
                forwardAvatar = v.findViewById(R.id.forward_avatar);
                emojisRv = v.findViewById(R.id.emojis_rv);
                body = v.findViewById(R.id.body);
                forwardName = v.findViewById(R.id.forward_name);
                avatar = v.findViewById(R.id.avatar);
                replyWrp = v.findViewById(R.id.reply_wrp);
                mediaRv = v.findViewById(R.id.media_rv);
                check = v.findViewById(R.id.check);
                replyAddonsRv = v.findViewById(R.id.reply_addons_rv);
                replyBody = v.findViewById(R.id.reply_body);
                replyName = v.findViewById(R.id.reply_name);
                date = v.findViewById(R.id.date);
                edited = v.findViewById(R.id.edited);
                onSwipeTouchListener = new OnSwipeTouchListener(getContext()) {
                    @Override
                    public void onSwipeLeft() {
                        if(messages.get(getAdapterPosition()).getFrom().getId().equals(loggedUser.getId())) {
                            ObjectAnimator animator = ObjectAnimator.ofFloat(getBox(), "translationX", -SWIPE_ANIMATION_OFFSET);
                            animator.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(@NonNull Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(@NonNull Animator animation) {
                                    animator.removeListener(this);
                                    animator.setDuration(0);
                                    animator.setFloatValues(0);
                                    animator.start();
                                    showReplyOptions(getAdapterPosition());
                                }

                                @Override
                                public void onAnimationCancel(@NonNull Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(@NonNull Animator animation) {

                                }
                            });
                            animator.setDuration(SWIPE_ANIMATION_DURATION);
                            animator.start();
                        }
                    }

                    @Override
                    public void onSwipeRight() {
                        if(messages.get(getAdapterPosition()).getFrom().getId().equals(currentUser.getId())) {
                            ObjectAnimator animator = ObjectAnimator.ofFloat(getBox(), "translationX", SWIPE_ANIMATION_OFFSET);
                            animator.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(@NonNull Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(@NonNull Animator animation) {
                                    animator.removeListener(this);
                                    animator.setDuration(0);
                                    animator.setFloatValues(0);
                                    animator.start();
                                    showReplyOptions(getAdapterPosition());
                                }

                                @Override
                                public void onAnimationCancel(@NonNull Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(@NonNull Animator animation) {

                                }
                            });
                            animator.setDuration(SWIPE_ANIMATION_DURATION);
                            animator.start();
                        }
                    }

                    @Override
                    public void onLongPress(View v) {
                        showMessageOptions(getAdapterPosition());
                    }
                };
                replyWrp.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
                    @Override
                    public void onSwipeRight() {
                        onSwipeTouchListener.onSwipeRight();
                    }

                    @Override
                    public void onSwipeLeft() {
                        onSwipeTouchListener.onSwipeLeft();
                    }

                    @Override
                    public void onClick(View v) {
                        scrollToMessage(getAdapterPosition());
                    }

                    @Override
                    public void onLongPress(View v) {
                        if(onSwipeTouchListener != null) {
                            onSwipeTouchListener.onLongPress(v);
                        }
                    }
                });
                mediaRv.setOnTouchListener(onSwipeTouchListener);
                container.setOnTouchListener(onSwipeTouchListener);
            }

            public RecyclerView getEmojisRv() {
                return emojisRv;
            }

            public OnSwipeTouchListener getOnSwipeTouchListener() {
                return onSwipeTouchListener;
            }

            public View getCheck() {
                return check;
            }

            public ImageView getAvatar() {
                return avatar;
            }

            public View getReplyWrp() {
                return replyWrp;
            }

            public View getEdited() {
                return edited;
            }

            public TextView getReplyName() {
                return replyName;
            }

            public TextView getReplyBody() {
                return replyBody;
            }

            public RecyclerView getMediaRv() {
                return mediaRv;
            }

            public RecyclerView getReplyAddonsRv() {
                return replyAddonsRv;
            }

            public View getForwardWrp() {
                return forwardWrp;
            }

            public View getContainer() {
                return container;
            }

            public View getBox() {
                return box;
            }

            public TextView getForwardName() {
                return forwardName;
            }

            public TextView getBody() {
                return body;
            }

            public TextView getDate() {
                return date;
            }

            public ImageView getForwardAvatar() {
                return forwardAvatar;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parentVg, int viewType) {
            View view = getLayoutInflater().inflate(viewType == 0 ? R.layout.message_item_to : R.layout.message_item_from, parentVg, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
            CustomViewHolder holder = (CustomViewHolder) h;
            MessageModel message = messages.get(holder.getAdapterPosition());
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            OnSwipeTouchListener onSwipeTouchListener = holder.getOnSwipeTouchListener();
            RecyclerView emojisRv = holder.getEmojisRv();
            if(message.getRating() == null) {
                emojisRv.setVisibility(View.GONE);
            }
            else {
                emojisRv.setVisibility(View.VISIBLE);
                emojisRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                emojisRv.setAdapter(new RatingAdapter(message.getRating()));
            }
            if(message.isEdited()) {
                holder.getEdited().setVisibility(View.VISIBLE);
            }
            if(message.getForwardedFrom() != null) {
                holder.getForwardWrp().setVisibility(View.VISIBLE);
                holder.getForwardName().setText(message.getForwardedFrom().getName());
                if(message.getForwardedFrom().getIcon() != null) {
                    Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, message.getForwardedFrom().getIcon())).into(holder.getForwardAvatar());
                }
            }
            else {
                holder.getForwardWrp().setVisibility(View.GONE);
            }
            if(message.getBody() != null) {
                holder.getBody().setText(message.getBody());
                holder.getBody().setVisibility(View.VISIBLE);
            }
            else {
                holder.getBody().setVisibility(View.GONE);
            }
            if(message.getFrom().getId().equals(loggedUser.getId()) && loggedUser.getIcon() != null) {
                Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, loggedUser.getIcon())).into(holder.getAvatar());
            }
            else if(message.getFrom().getId().equals(currentUser.getId()) && currentUser.getIcon() != null) {
                Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, currentUser.getIcon())).into(holder.getAvatar());
            }
            else {
                holder.getAvatar().setImageResource(R.drawable.avatar);
            }
            if(message.getAddons() != null) {
                holder.getMediaRv().setLayoutManager(message.getAddons().size() > 1 ? new GridLayoutManager(getContext(), 2) : new LinearLayoutManager(getContext()));
                MediaAdapter adapter = new MediaAdapter(message.getAddons(), holder.getAdapterPosition());
                adapter.setOnSwipeTouchListener(onSwipeTouchListener);
                holder.getMediaRv().setAdapter(adapter);
                holder.getMediaRv().setVisibility(View.VISIBLE);
            }
            else {
                holder.getMediaRv().setVisibility(View.GONE);
            }
            if(message.getParent() != null) {
                MessageModel parent = message.getParent();
                if(parent.getAddons() != null) {
                    ReplyParentAddonsAdapter adapter = new ReplyParentAddonsAdapter(parent.getAddons());
                    adapter.setOnReplyParentAddonClickedListener((int pos) -> {
                        scrollToMessage(holder.getAdapterPosition());
                    });
                    adapter.setOnSwipeTouchListener(onSwipeTouchListener);
                    holder.getReplyAddonsRv().setVisibility(View.VISIBLE);
                    holder.getReplyAddonsRv().setLayoutManager(new GridLayoutManager(getContext(), 2));
                    holder.getReplyAddonsRv().setAdapter(adapter);
                }
                else {
                    holder.getReplyAddonsRv().setVisibility(View.GONE);
                }
                UserModel user = parent.getFrom().getId().equals(loggedUser.getId()) ? parent.getTo() : parent.getFrom();
                holder.getReplyWrp().setVisibility(View.VISIBLE);
                holder.getReplyName().setText(user.getId().equals(loggedUser.getId()) ? getContext().getResources().getString(R.string.you) : user.getName());
                if(parent.getBody() != null) {
                    holder.getReplyBody().setVisibility(View.VISIBLE);
                    holder.getReplyBody().setText(parent.getBody());
                }
                else {
                    holder.getReplyBody().setVisibility(View.GONE);
                }
            }
            else {
                holder.getReplyWrp().setVisibility(View.GONE);
            }
            if(message.getFrom().getId().equals(loggedUser.getId())) {
                if(message.isRead()) {
                    holder.getCheck().setVisibility(View.VISIBLE);
                }
            }
            if(optionsMessagePosition >= 0 && optionsMessagePosition == holder.getAdapterPosition()) {
                holder.itemView.setBackgroundColor(Color.parseColor("#232425"));
            }
            else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
            holder.getDate().setText(dateFormat.format(new Date(message.getCreatedAt())));
        }

        private void showMessageOptions(int position) {
            topMainWrp.setVisibility(View.GONE);
            topOptionsWrp.setVisibility(View.VISIBLE);
            bottomWrp.setVisibility(View.GONE);
            optionsWrp.setVisibility(View.VISIBLE);
            int oldPos = optionsMessagePosition;
            optionsMessagePosition = position;
            MessageModel message = messages.get(optionsMessagePosition);
            if(!message.getFrom().getId().equals(loggedUser.getId())) {
                optionsEditWrp.setVisibility(View.GONE);
                optionsDeleteWrp.setVisibility(View.GONE);
            }
            else {
                optionsDeleteWrp.setVisibility(View.VISIBLE);
                optionsEditWrp.setVisibility(View.VISIBLE);
            }
            notifyItemChanged(optionsMessagePosition);
            if(oldPos >= 0) {
                notifyItemChanged(oldPos);
            }
        }

        private void scrollToMessage(int from) {
            String id = messages.get(from).getParent().getId();
            for(int i = from;i >= 0; --i) {
                if(messages.get(i).getId().equals(id)) {
                    rv.scrollToPosition(i);
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).getFrom().getId().equals(loggedUser.getId()) ? 0 : 1;
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }

    private interface OnReplyParentAddonClickedListener {
        void onReplyParentAddonClicked(int pos);
    }

    private class ReplyParentAddonsAdapter extends RecyclerView.Adapter {
        private List<String> addons;
        private OnReplyParentAddonClickedListener onReplyParentAddonClickedListener;
        private OnSwipeTouchListener onSwipeTouchListener;

        public ReplyParentAddonsAdapter(List<String> addons) {
            this.addons = addons;
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView image;
            private View play;
            public CustomViewHolder(View v) {
                super(v);
                image = v.findViewById(R.id.image);
                play = v.findViewById(R.id.play);
            }

            public View getPlay() {
                return play;
            }

            public ImageView getImage() {
                return image;
            }
        }

        public void setOnSwipeTouchListener(OnSwipeTouchListener onSwipeTouchListener) {
            this.onSwipeTouchListener = onSwipeTouchListener;
        }

        public void setOnReplyParentAddonClickedListener(OnReplyParentAddonClickedListener onReplyParentAddonClickedListener) {
            this.onReplyParentAddonClickedListener = onReplyParentAddonClickedListener;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = getLayoutInflater().inflate(R.layout.message_parent_addons, parent, false);
            CustomViewHolder viewHolder = new CustomViewHolder(root);
            root.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
                @Override
                public void onSwipeLeft() {
                    if(onSwipeTouchListener != null) {
                        onSwipeTouchListener.onSwipeLeft();
                    }
                }

                @Override
                public void onSwipeRight() {
                    if(onSwipeTouchListener != null) {
                        onSwipeTouchListener.onSwipeRight();
                    }
                }

                @Override
                public void onClick(View v) {
                    if(onReplyParentAddonClickedListener != null) {
                        onReplyParentAddonClickedListener.onReplyParentAddonClicked(viewHolder.getAdapterPosition());
                    }
                }

                @Override
                public void onLongPress(View v) {
                    if(onReplyParentAddonClickedListener != null) {
                        onSwipeTouchListener.onLongPress(v);
                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
            CustomViewHolder holder = (CustomViewHolder) h;
            String url = String.join("/", ApiBuilder.ADDONS_PATH, addons.get(position));
            String mime = Utils.getMimeType(url);
            View play = holder.getPlay().findViewById(R.id.play);
            if(mime.startsWith("image")) {
                Picasso.get().load(url).into(holder.getImage());
            }
            else if(mime.startsWith("video")) {
                if(!mime.endsWith("3gpp")) {
                    Glide.with(getContext()).load(url).into(holder.getImage());
                    play.setVisibility(View.VISIBLE);
                }
            }
            Picasso.get().load(url).into(holder.getImage());
        }

        @Override
        public int getItemCount() {
            return addons.size();
        }
    }

    private class ReplyAddonsAdapter extends RecyclerView.Adapter {
        private List<String> addons;

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView image;

            public CustomViewHolder(@NonNull View v) {
                super(v);
                image = v.findViewById(R.id.image);
            }

            public ImageView getImage() {
                return image;
            }
        }

        public ReplyAddonsAdapter(List<String> addons) {
            this.addons = addons;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CustomViewHolder(getLayoutInflater().inflate(R.layout.reply_addon, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
            CustomViewHolder holder = (CustomViewHolder) h;
            ImageView image = holder.getImage();
            Picasso.get().load(String.join("/", ApiBuilder.ADDONS_PATH, addons.get(position))).into(image);
        }

        @Override
        public int getItemCount() {
            return addons.size();
        }
    }

    private class AddonData {
        private Uri uri;
        private String mime;
        private int duration;

        public AddonData(Uri uri, String mime, int duration) {
            this.uri = uri;
            this.mime = mime;
            this.duration = duration;
        }

        public int getDuration() {
            return duration;
        }

        public Uri getUri() {
            return uri;
        }

        public String getMime() {
            return mime;
        }
    }

    private class AddonAdapter extends RecyclerView.Adapter {
        private int height;

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView image;
            private View bg, check;
            private TextView duration;
            public CustomViewHolder(View v) {
                super(v);
                image = v.findViewById(R.id.image);
                bg = v.findViewById(R.id.check_bg);
                check = v.findViewById(R.id.check);
                duration = v.findViewById(R.id.duration);
            }

            public ImageView getImage() {
                return image;
            }

            public View getBg() {
                return bg;
            }

            public View getCheck() {
                return check;
            }

            public TextView getDuration() {
                return duration;
            }
        }

        public AddonAdapter() {
            height = getResources().getDisplayMetrics().widthPixels / 3;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = getLayoutInflater().inflate(R.layout.media_addon_item, parent, false);
            CustomViewHolder holder = new CustomViewHolder(root);
            root.setOnClickListener((View v) -> {
                AddonData uri = addons.get(holder.getAdapterPosition());
                if(selectedAddons.indexOf(uri) >= 0) {
                    selectedAddons.remove(uri);
                }
                else {
                    selectedAddons.add(uri);
                }
                notifyItemChanged(holder.getAdapterPosition());
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
            CustomViewHolder holder = (CustomViewHolder) h;
            ImageView image = holder.getImage();
            View bg = holder.getBg();
            View check = holder.getCheck();
            AddonData uri = addons.get(holder.getAdapterPosition());
            TextView duration = holder.getDuration();
            holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            try {
                if(uri.getMime().startsWith("image")) {
                    duration.setVisibility(View.GONE);
                    image.setImageBitmap(Utils.decodeBitmap(getActivity().getContentResolver(), uri.getUri()));
                }
                else if(uri.getMime().startsWith("video")) {
                    int total = uri.getDuration();
                    int hours =  total / 3600;
                    int mins = total % 3600 / 60;
                    int secs = total % 60;
                    duration.setText(String.format("%02d:%02d:%02d", hours, mins, secs));
                    duration.setVisibility(View.VISIBLE);
                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(new File(uri.getUri().getPath()), new Size(640, 480), null);
                    image.setImageBitmap(bitmap);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            if(selectedAddons.indexOf(uri) >= 0) {
                bg.setVisibility(View.VISIBLE);
                check.setVisibility(View.VISIBLE);
            }
            else {
                bg.setVisibility(View.GONE);
                check.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return addons.size();
        }
    }

    private class SelectedAddonAdapter extends RecyclerView.Adapter {

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private View delete;
            private ImageView image;
            public CustomViewHolder(View v) {
                super(v);
                delete = v.findViewById(R.id.delete);
                image = v.findViewById(R.id.image);
            }

            public ImageView getImage() {
                return image;
            }

            public View getDelete() {
                return delete;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = getLayoutInflater().inflate(R.layout.media_selected_addon, parent, false);
            CustomViewHolder holder = new CustomViewHolder(root);
            holder.getDelete().setOnClickListener((View v) -> {
                selectedAddons.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
            CustomViewHolder holder = (CustomViewHolder) h;
            AddonData uri = selectedAddons.get(position);
            try {
                if(uri.getMime().startsWith("image")) {
                    holder.getImage().setImageBitmap(Utils.decodeBitmap(activity.getContentResolver(), uri.getUri()));
                }
                else if(uri.getMime().startsWith("video")) {
                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(new File(uri.getUri().getPath()), new Size(640, 480), null);
                    holder.getImage().setImageBitmap(bitmap);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return selectedAddons.size();
        }
    }

    public void setCurrentUser(UserModel currentUser) {
        this.currentUser = currentUser;
    }

    private View.OnClickListener sendClicked = (View v) -> {
        String messageValue = message.getText().toString().trim();
        if(messageValue.isEmpty() && selectedAddons.size() == 0) return;
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<MessageModel>() {
            @Override
            public void onResponse(Call<MessageModel> call, Response<MessageModel> response) {
                int code = response.code();
                if(code == 201 || code == 200) {
                    if(code == 201) {
                        if(messages == null) {
                            messages = new ArrayList<>();
                        }
                        messages.add(response.body());
                        ++total;
                        if(rv.getLayoutManager() == null) {
                            rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        }
                        if(rv.getAdapter() == null) {
                            rv.setAdapter(new Adapter());
                        }
                        else {
                            rv.getAdapter().notifyItemInserted(messages.size() - 1);
                        }
                        message.setText("");
                        if(selectedAddons.size() != 0) {
                            selectedAddons.clear();
                            selectedAddonsRv.getAdapter().notifyDataSetChanged();
                        }
                        if(replyMessage != null) {
                            replyMessage = null;
                            replyWrp.setVisibility(View.GONE);
                        }
                        rv.scrollToPosition(messages.size() - 1);
                    }
                    else {
                        int pos = optionsMessagePosition;
                        optionsMessagePosition = -1;
                        editMessage = null;
                        messages.set(pos, response.body());
                        message.setText("");
                        selectedAddons.clear();
                        mic.setVisibility(View.VISIBLE);
                        rv.getAdapter().notifyItemChanged(pos);
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            MultipartBody.Part[] parts = null;
            if(selectedAddons.size() != 0) {
                List<MultipartBody.Part> addons = new ArrayList<>();
                for(int i = 0;i < selectedAddons.size(); ++i) {
                    File file = new File(selectedAddons.get(i).getUri().getPath());
                    RequestBody rf = RequestBody.create(MultipartBody.FORM, file);
                    addons.add(MultipartBody.Part.createFormData("files", file.getName(), rf));
                }
                parts = new MultipartBody.Part[addons.size()];
                addons.toArray(parts);
            }
            RequestBody parentId = null;
            if(replyMessage != null) {
                parentId = RequestBody.create(MultipartBody.FORM, replyMessage.getId());
            }
            builder.send(
                    editMessage == null ?
                            builder.getApi(ApiService.class).sendMessage(
                                    RequestBody.create(MultipartBody.FORM, currentUser.getId()),
                                    messageValue.isEmpty() ? null : RequestBody.create(MultipartBody.FORM, messageValue),
                                    parentId,
                                    parts
                            ) :
                            builder.getApi(ApiService.class).updateMessage(
                                    RequestBody.create(MultipartBody.FORM, editMessage.getId()),
                                    RequestBody.create(MultipartBody.FORM, currentUser.getId()),
                                    messageValue.isEmpty() ? null : RequestBody.create(MultipartBody.FORM, messageValue),
                                    parentId,
                                    parts
                            )
            );
        });
    };

    private void showReplyOptions(int position) {
        MessageModel message = (MessageModel) messages.get(position);
        replyMessage = (MessageModel) message;
        replyWrp.setVisibility(View.VISIBLE);
        replyName.setText(message.getFrom().getId().equals(loggedUser.getId()) ? getContext().getResources().getString(R.string.you) : currentUser.getName());
        if(message.getBody() != null) {
            replyAddonsRv.setVisibility(View.GONE);
            replyBody.setText(message.getBody());
            replyBody.setVisibility(View.VISIBLE);
        }
        else if(message.getAddons() != null) {
            replyAddonsRv.setVisibility(View.VISIBLE);
            replyBody.setVisibility(View.GONE);
            replyAddonsRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            replyAddonsRv.setAdapter(new ReplyAddonsAdapter(message.getAddons()));
        }
    }

    private View.OnClickListener addClicked = (View v) -> {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, Defs.PermissionCode.GALLERY_CODE);
            return;
        }
        selectFromGallery();
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == Defs.PermissionCode.GALLERY_CODE && grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            selectFromGallery();
        }
        else if(requestCode == Defs.PermissionCode.RECORD_AUDIO_CODE && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            record();
        }
    }

    private void selectFromGallery() {
        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,null,null,null);
        addons = new ArrayList<>();
        while(cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
            int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE));
            if(size > 15000000) continue;
            addons.add(new AddonData(Uri.fromFile(new File(path)), cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE)), 0));
        }
        cursor.close();
        cursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null, null);
        while(cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));
            int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.SIZE));
            if(size > 15000000) continue;
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)) / 1000;
            addons.add(new AddonData(Uri.fromFile(new File(path)), cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE)), duration));
        }
        cursor.close();
        View root = getLayoutInflater().inflate(R.layout.media_addons_win, null);
        View close = root.findViewById(R.id.close);
        RecyclerView rv = root.findViewById(R.id.rv);
        int h = getResources().getDisplayMetrics().heightPixels / 2;
        PopupWindow win = new PopupWindow(root, WindowManager.LayoutParams.MATCH_PARENT, h);
        close.setOnClickListener((View view) -> {
            win.dismiss();
        });
        if(rv.getLayoutManager() == null) {
            rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }
        rv.setAdapter(new AddonAdapter());
        win.setOutsideTouchable(true);
        win.setOnDismissListener(() -> {
            if(selectedAddons.size() != 0) {
                if(selectedAddonsRv.getLayoutManager() == null) {
                    selectedAddonsRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                }
                selectedAddonsRv.setAdapter(new SelectedAddonAdapter());
            }
        });
        win.showAtLocation(root, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CoreActivity) context;
        loggedUser = activity.getLoggedUser();
        socket = activity.getSocket();
    }

    @Override
    public void onStart() {
        initSockets();
        socket.emit(Defs.WS_MESSAGES.CLIENT.CHECK_USER_ONLINE, currentUser.getId());
        super.onStart();
    }

    private View.OnClickListener replyCloseClicked = (View v) -> {
        replyWrp.setVisibility(View.GONE);
        replyMessage = null;
        replyAddonsRv.setVisibility(View.GONE);
        replyBody.setVisibility(View.GONE);
    };

    private View.OnClickListener topOptionsCloseClicked = (View v) -> {
        hideMessageOptions(() -> {
            int pos = optionsMessagePosition;
            optionsMessagePosition = -1;
            rv.getAdapter().notifyItemChanged(pos);
        });
    };

    private Emitter.Listener messageReadListener = (Object ...args) -> {
        Gson gson = new Gson();
        MessageModel message = gson.fromJson((String) args[0], MessageModel.class);
        activity.runOnUiThread(() -> {
            for(int i = messages.size() - 1;i >= 0; --i) {
                if(messages.get(i).getId().equals(message.getId())) {
                    messages.get(i).setRead(true);
                    rv.getAdapter().notifyItemChanged(i);
                    break;
                }
            }
        });
    };

    private Emitter.Listener userOnlineListener = (Object ...args) -> {
        Gson gson = new Gson();
        UserModel user = gson.fromJson((String) args[0], UserModel.class);
        if(user.getId().equals(currentUser.getId())) {
            activity.runOnUiThread(() -> {
                status.setVisibility(View.VISIBLE);
                status.setText(getContext().getResources().getString(R.string.online_status));
            });
        }
    };

    private Emitter.Listener userOfflineListener = (Object ...args) -> {
        Gson gson = new Gson();
        UserModel user = gson.fromJson((String) args[0], UserModel.class);
        if(user.getId().equals(currentUser.getId())) {
            activity.runOnUiThread(() -> {
                status.setVisibility(View.GONE);
            });
        }
    };

    private Emitter.Listener messageDeletedListener = (Object ...args) -> {
        String msgId = (String) args[0];
        activity.runOnUiThread(() -> {
            for(int i = messages.size() - 1;i >= 0; --i) {
                if(messages.get(i).getId().equals(msgId)) {
                    messages.remove(i);
                    rv.getAdapter().notifyItemRemoved(i);
                    if(messages.size() == 0) {
                        activity.getSupportFragmentManager().popBackStack();
                    }
                    break;
                }
            }
        });
    };

    private Emitter.Listener chatRemovedListener = (Object ...args) -> {
        activity.runOnUiThread(() -> {
            back.performClick();
        });
    };

    private Emitter.Listener messageEditedListener = (Object ...args) -> {
        Gson gson = new Gson();
        MessageModel message = gson.fromJson((String) args[0], MessageModel.class);
        if(message.getTo().getId().equals(currentUser.getId())) {
            activity.runOnUiThread(() -> {
                for(int i = messages.size() - 1;i >= 0; --i) {
                    if(messages.get(i).getId().equals(message.getId())) {
                        messages.set(i, message);
                        rv.getAdapter().notifyItemChanged(i);
                        break;
                    }
                }
            });
        }
    };

    private Emitter.Listener newMessageListener = (Object ...args) -> {
        Gson gson = new Gson();
        MessageModel message = gson.fromJson((String) args[0], MessageModel.class);
        if(message.getFrom().getId().equals(currentUser.getId())) {
            activity.runOnUiThread(() -> {
                if(messages == null) {
                    messages = new ArrayList<>();
                }
                messages.add(message);
                ++total;
                if(rv.getLayoutManager() == null) {
                    rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                }
                if(rv.getAdapter() == null) {
                    rv.setAdapter(new Adapter());
                }
                else {
                    rv.getAdapter().notifyItemInserted(messages.size() - 1);
                }
            });
        }
    };

    private Emitter.Listener userOnlineStatusListener = (Object ...args) -> {
        boolean isOnline = (boolean) args[0];
        activity.runOnUiThread(() -> {
            status.setVisibility(isOnline ? View.VISIBLE : View.GONE);
            if(isOnline) {
                status.setText(getContext().getResources().getString(R.string.online_status));
            }
        });
    };

    private Emitter.Listener loggedOutListener = (Object ...args) -> {
        String id = (String) args[0];
        if(!id.equals(currentUser.getId())) return;
        activity.runOnUiThread(() -> {
            status.setVisibility(View.GONE);
        });
    };

    private Emitter.Listener currentLoggedOut = (Object ...args) -> {
        String id = (String) args[0];
        if(id.equals(currentUser.getId())) {
            activity.runOnUiThread(() -> {
                status.setVisibility(View.GONE);
            });
        }
    };

    private Emitter.Listener messageToggledRatingListener = (Object ...args) -> {
        Gson gson = new Gson();
        MessageModel message = gson.fromJson((String) args[0], MessageModel.class);
        activity.runOnUiThread(() -> {
            for(int i = messages.size() - 1;i >= 0; --i) {
                MessageModel msg = messages.get(i);
                if(msg.getId().equals(message.getId())) {
                    msg.setRating(message.getRating());
                    rv.getAdapter().notifyItemChanged(i);
                    break;
                }
            }
        });
    };

    private void initSockets() {
        socket.on(Defs.WS_MESSAGES.SERVER.MESSAGE_TOGGLE_RATING, messageToggledRatingListener);
        socket.on(Defs.WS_MESSAGES.SERVER.CONTACT_LOGGED_OUT, currentLoggedOut);
        socket.on(Defs.WS_MESSAGES.SERVER.LOGGED_OUT, loggedOutListener);
        socket.on(Defs.WS_MESSAGES.SERVER.MESSAGE_READ, messageReadListener);
        socket.on(Defs.WS_MESSAGES.SERVER.USER_ONLINE, userOnlineListener);
        socket.on(Defs.WS_MESSAGES.SERVER.USER_OFFLINE, userOfflineListener);
        socket.on(Defs.WS_MESSAGES.SERVER.MESSAGE_DELETED, messageDeletedListener);
        socket.on(Defs.WS_MESSAGES.SERVER.CHAT_REMOVED, chatRemovedListener);
        socket.on(Defs.WS_MESSAGES.SERVER.MESSAGE_EDITED, messageEditedListener);
        socket.on(Defs.WS_MESSAGES.SERVER.NEW_MESSAGE, newMessageListener);
        socket.on(Defs.WS_MESSAGES.SERVER.USER_ONLINE_STATUS, userOnlineStatusListener);
    }

    private void finishSockets() {
        socket.off(Defs.WS_MESSAGES.SERVER.MESSAGE_TOGGLE_RATING, messageToggledRatingListener);
        socket.off(Defs.WS_MESSAGES.SERVER.CONTACT_LOGGED_OUT, currentLoggedOut);
        socket.off(Defs.WS_MESSAGES.SERVER.LOGGED_OUT, loggedOutListener);
        socket.off(Defs.WS_MESSAGES.SERVER.MESSAGE_READ, messageReadListener);
        socket.off(Defs.WS_MESSAGES.SERVER.USER_ONLINE, userOnlineListener);
        socket.off(Defs.WS_MESSAGES.SERVER.USER_OFFLINE, userOfflineListener);
        socket.off(Defs.WS_MESSAGES.SERVER.MESSAGE_DELETED, messageDeletedListener);
        socket.off(Defs.WS_MESSAGES.SERVER.CHAT_REMOVED, chatRemovedListener);
        socket.off(Defs.WS_MESSAGES.SERVER.MESSAGE_EDITED, messageEditedListener);
        socket.off(Defs.WS_MESSAGES.SERVER.NEW_MESSAGE, newMessageListener);
        socket.off(Defs.WS_MESSAGES.SERVER.USER_ONLINE_STATUS, userOnlineStatusListener);
    }

    @Override
    public void onStop() {
        finishSockets();
        super.onStop();
    }

    private void hideMessageOptions(Runnable runnable) {
        topMainWrp.setVisibility(View.VISIBLE);
        topOptionsWrp.setVisibility(View.GONE);
        bottomWrp.setVisibility(View.VISIBLE);
        optionsWrp.setVisibility(View.GONE);
        if(runnable != null) {
            runnable.run();
        }
    }

    private void deleteMessage(Runnable runnable) {
        ConfirmDialog dialog = new ConfirmDialog(
                getContext(),
                getContext().getResources().getString(R.string.delete_message_confirm_body),
                getContext().getResources().getString(R.string.delete_message_confirm_title),
                null,
                null
        );
        dialog.setOnPositiveConfirmListener(() -> {
            dialog.dismiss();
            ApiBuilder builder = ApiBuilder.getInstance(getContext());
            builder.setResponseListener(new ApiBuilder.ResponseListener<MessageModel>() {
                @Override
                public void onResponse(Call<MessageModel> call, Response<MessageModel> response) {
                    if(response.code() == 200) {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.message_deleted), Toast.LENGTH_LONG).show();
                        hideMessageOptions(() -> {
                            int pos = optionsMessagePosition;
                            optionsMessagePosition = -1;
                            messages.remove(pos);
                            rv.getAdapter().notifyItemRemoved(pos);
                            if(messages.size() == 0) {
                                back.performClick();
                            }
                        });
                        if(runnable != null) {
                            runnable.run();
                        }
                    }
                    else {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).deleteMessageById(messages.get(optionsMessagePosition).getId()));
            });
        });
        dialog.show();
    }

    private void forwardMessage() {
        SelectContactDialogFragment fragment = new SelectContactDialogFragment();
        fragment.setOnContactSelectedListener((UserModel contact) -> {
            String msgId = messages.get(optionsMessagePosition).getId();
            int pos = optionsMessagePosition;
            optionsMessagePosition = -1;
            hideMessageOptions(() -> {
                rv.getAdapter().notifyItemChanged(pos);
            });
            activity.getSupportFragmentManager().popBackStack();
            if(contact.getId().equals(currentUser.getId())) {
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.cannot_forward_same_contact), Toast.LENGTH_LONG).show();
                return;
            }
            ApiBuilder builder = ApiBuilder.getInstance(getContext());
            builder.setResponseListener(new ApiBuilder.ResponseListener<MessageModel>() {
                @Override
                public void onResponse(Call<MessageModel> call, Response<MessageModel> response) {
                    if(response.code() == 201) {
                        ChatFragment chatFragment = new ChatFragment();
                        chatFragment.setCurrentUser(contact);
                        chatFragment.setOnBackPressedListener(() -> {
                            activity.getSupportFragmentManager().popBackStack();
                        });
                        activity.loadExtra(chatFragment, true);
                    }
                    else {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).forwardMessage(new ForwardMessageModel(msgId, contact.getId())));
            });
        });
        fragment.setOnBackPressedListener(() -> {
            activity.getSupportFragmentManager().popBackStack();
        });
        activity.loadExtra(fragment, true);
    }

    private void reply() {
        hideMessageOptions(() -> {
            int pos = optionsMessagePosition;
            optionsMessagePosition = -1;
            rv.getAdapter().notifyItemChanged(pos);
            showReplyOptions(pos);
        });
    }

    private View.OnClickListener optionsDeleteClicked = (View v) -> {
        deleteMessage(null);
    };

    private View.OnClickListener optionsForwardClicked = (View v) -> {
        forwardMessage();
    };

    private View.OnClickListener optionsReplyClicked = (View v) -> {
        reply();
    };

    private View.OnClickListener buttonDownClicked = (View v) -> {
        if(messages.size() != 0) {
            rv.scrollToPosition(messages.size() - 1);
            for(int i = messages.size() - 1;i >= 0; --i) {
                MessageModel message = messages.get(i);
                if(!message.getTo().getId().equals(loggedUser.getId())) continue;
                if(message.isRead()) break;
                message.setRead(true);
                socket.emit(Defs.WS_MESSAGES.CLIENT.MESSAGE_READ, message.getId());
            }
            newCount.setText("");
        }
    };

    private View.OnClickListener optionsEmojiClicked = (View v) -> {
        EmojiPickerView root = (EmojiPickerView) getLayoutInflater().inflate(R.layout.emojis_dialog, null);
        int h = getContext().getResources().getDisplayMetrics().heightPixels / 3;
        root.setOnEmojiPickedListener((EmojiViewItem emojiViewItem) -> {
            ApiBuilder builder = ApiBuilder.getInstance(getContext());
            MessageModel message = messages.get(optionsMessagePosition);
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).toggleMessageRating(new BaseData(message.getId(), emojiViewItem.getEmoji())));
            });
            builder.setResponseListener(new ApiBuilder.ResponseListener<MessageRatingResponseModel>() {
                @Override
                public void onResponse(Call<MessageRatingResponseModel> call, Response<MessageRatingResponseModel> response) {
                    if(response.code() == 201) {
                        MessageRatingResponseModel res = response.body();
                        if(res.getAction() == 0) {
                            if(message.getRating() != null) {
                                for(BaseData bd : message.getRating()) {
                                    if(loggedUser.getId().equals(bd.getId())) {
                                        message.getRating().remove(message.getRating().indexOf(bd));
                                        if(message.getRating().size() == 0) {
                                            message.setRating(null);
                                        }
                                        rv.getAdapter().notifyItemChanged(optionsMessagePosition);
                                        break;
                                    }
                                }
                            }
                        }
                        else {
                            List<BaseData> data = message.getRating();
                            if(data != null) {
                                boolean has = false;
                                for(BaseData bd: data) {
                                    if(bd.getId().equals(loggedUser.getId())) {
                                        has = true;
                                        data.set(data.indexOf(bd), new BaseData(loggedUser.getId(), res.getFace()));
                                        break;
                                    }
                                }
                                if(!has) {
                                    data.add(new BaseData(loggedUser.getId(), res.getFace()));
                                }
                            }
                            else {
                                data = new ArrayList<>();
                                data.add(new BaseData(loggedUser.getId(), res.getFace()));
                                message.setRating(data);
                            }
                            rv.getAdapter().notifyItemChanged(messages.indexOf(message));
                        }
                        Gson gson = new Gson();
                        socket.emit(Defs.WS_MESSAGES.CLIENT.MESSAGE_RATED, gson.toJson(message, new TypeToken<MessageModel>(){}.getType()));
                    }
                    else {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
        PopupWindow win = new PopupWindow(root, WindowManager.LayoutParams.MATCH_PARENT, h);
        win.setOutsideTouchable(true);
        win.setOnDismissListener(() -> {
            hideMessageOptions(() -> {
                int pos = optionsMessagePosition;
                optionsMessagePosition = -1;
                rv.getAdapter().notifyItemChanged(pos);
            });
        });
        win.showAtLocation(root, Gravity.BOTTOM, 0, 0);
    };

    private View.OnClickListener optionsEditClicked = (View v) -> {
        hideMessageOptions(() -> {
            editMessage = messages.get(optionsMessagePosition);
            mic.setVisibility(View.GONE);
            if(editMessage.getBody() != null) {
                message.setText(editMessage.getBody());
            }
        });
    };

    private View.OnClickListener avatarClicked = (View v) -> {
        UserInfoFragment fragment = new UserInfoFragment();
        fragment.setUser(currentUser);
        fragment.setOnBackPressedListener(() -> {
            activity.getSupportFragmentManager().popBackStack();
        });
        activity.loadExtra(fragment, true);
    };

    private View.OnClickListener micClicked = (View v) -> {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, Defs.PermissionCode.RECORD_AUDIO_CODE);
        }
        else {
            record();
        }
    };

    private void record() {
        recordHandler = new Handler();
        recordRunnable = () -> {
            if(recordedTime == 3600) {
                stopRecord();
                return;
            }
            recordedTime++;
            int minutes = recordedTime % 3600 / 60;
            int secs = recordedTime % 60;
            recordTime.setText(String.format("%02d:%02d", minutes, secs));
            recordHandler.postDelayed(recordRunnable, 1000);
        };
        recorder = new MediaRecorder();
        recorder = new MediaRecorder();
        recordFileName = getActivity().getExternalCacheDir().getAbsolutePath() + "/recording.3gp";
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordFileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
            recorder.start();
            recordTime.setText(String.format("%02d:%02d", 0, 0));
            recordHandler.postDelayed(recordRunnable, 1000);
        }
        catch (IOException e) {
            Toast.makeText(getContext(), getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        recordWrp.setVisibility(View.VISIBLE);
        bottomWrp.setVisibility(View.GONE);
        optionsWrp.setVisibility(View.GONE);
        recordIcon.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.record_blink));
    }

    private void stopRecord() {
        if(recordedTime > 0) {
            ApiBuilder builder = ApiBuilder.getInstance(getContext());
            builder.setResponseListener(new ApiBuilder.ResponseListener<MessageModel>() {
                @Override
                public void onResponse(Call<MessageModel> call, Response<MessageModel> response) {
                    Log.i("res", String.valueOf(response.body()));
                }
            });
            MultipartBody.Part addons[] = new MultipartBody.Part[1];
            File file = new File(recordFileName);
            RequestBody rf = RequestBody.create(MultipartBody.FORM, file);
            addons[0] = MultipartBody.Part.createFormData("files", file.getName(), rf);
            builder.setOnInitializedListener(() -> {
                builder.send(builder.getApi(ApiService.class).sendMessage(
                        RequestBody.create(MultipartBody.FORM, currentUser.getId()),
                        null,
                        null,
                        addons
                ));
            });
        }
        recordIcon.clearAnimation();
        recordHandler.removeCallbacks(recordRunnable);
        recorder.stop();
        recorder.release();
        recorder = null;
        bottomWrp.setVisibility(View.VISIBLE);
        optionsWrp.setVisibility(View.GONE);
        recordWrp.setVisibility(View.GONE);
        recordedTime = 0;
        recordTime.setText("");
    }

    private View.OnClickListener stopRecordClicked = (View v) -> {
        stopRecord();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        back = root.findViewById(R.id.back);
        name = root.findViewById(R.id.name);
        recordIcon = root.findViewById(R.id.record_icon);
        buttonDown = root.findViewById(R.id.button_down);
        recordStop = root.findViewById(R.id.stop);
        replyBody = root.findViewById(R.id.reply_body);
        recordTime = root.findViewById(R.id.recorded_time);
        newCount = root.findViewById(R.id.new_count);
        replyClose = root.findViewById(R.id.reply_close);
        replyName = root.findViewById(R.id.reply_name);
        recordWrp = root.findViewById(R.id.record_wrp);
        replyWrp = root.findViewById(R.id.reply_wrp);
        currentDate = root.findViewById(R.id.current_date);
        mic = root.findViewById(R.id.mic);
        optionsEdit = root.findViewById(R.id.options_edit);
        optionsEditWrp = root.findViewById(R.id.options_edit_wrp);
        topMainWrp = root.findViewById(R.id.top_main_wrp);
        topOptionsWrp = root.findViewById(R.id.top_options_wrp);
        topOptionsClose = root.findViewById(R.id.top_options_close);
        optionsWrp = root.findViewById(R.id.options_wrp);
        bottomWrp = root.findViewById(R.id.bottom_wrp);
        replyAddonsRv = root.findViewById(R.id.reply_addons_rv);
        message = root.findViewById(R.id.message);
        avatar = root.findViewById(R.id.avatar);
        status = root.findViewById(R.id.status);
        send = root.findViewById(R.id.send);
        mediaWrp = root.findViewById(R.id.media_wrapper);
        optionsDelete = root.findViewById(R.id.options_delete);
        optionsForward = root.findViewById(R.id.options_forward);
        optionsDeleteWrp = root.findViewById(R.id.options_delete_wrp);
        optionsEmoji = root.findViewById(R.id.options_emoji);
        optionsReply = root.findViewById(R.id.options_reply);
        rv = root.findViewById(R.id.rv);
        selectedAddonsRv = root.findViewById(R.id.selected_addons_rv);
        add = root.findViewById(R.id.add);
        add.setOnClickListener(addClicked);
        send.setOnClickListener(sendClicked);
        replyClose.setOnClickListener(replyCloseClicked);
        topOptionsClose.setOnClickListener(topOptionsCloseClicked);
        optionsDelete.setOnClickListener(optionsDeleteClicked);
        optionsForward.setOnClickListener(optionsForwardClicked);
        optionsReply.setOnClickListener(optionsReplyClicked);
        buttonDown.setOnClickListener(buttonDownClicked);
        optionsEmoji.setOnClickListener(optionsEmojiClicked);
        optionsEdit.setOnClickListener(optionsEditClicked);
        avatar.setOnClickListener(avatarClicked);
        mic.setOnClickListener(micClicked);
        recordStop.setOnClickListener(stopRecordClicked);
        init();
        return root;
    }

    @Override
    protected void init() {
        super.init();
        name.setText(currentUser.getName());
        if(currentUser.getIcon() != null) {
            Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, currentUser.getIcon())).into(avatar);
        }
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(recyclerView.canScrollVertically(1)) {
                    buttonDown.setVisibility(View.VISIBLE);
                }
                else {
                    buttonDown.setVisibility(View.GONE);
                    newCount.setText("");
                }
                int pos = ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
                int endPos = ((LinearLayoutManager) rv.getLayoutManager()).findLastVisibleItemPosition();
                int unreadCount = 0;
                if(pos != RecyclerView.NO_POSITION) {
                    for(int i = pos; i < messages.size(); ++i) {
                        MessageModel message = messages.get(i);
                        if(!message.isRead() && message.getTo().getId().equals(loggedUser.getId())) {
                            if(endPos != RecyclerView.NO_POSITION) {
                                if(i > endPos) {
                                    ++unreadCount;
                                }
                                else {
                                    message.setRead(true);
                                    socket.emit(Defs.WS_MESSAGES.CLIENT.MESSAGE_READ, message.getId());
                                }
                            }
                            else {
                                message.setRead(true);
                                socket.emit(Defs.WS_MESSAGES.CLIENT.MESSAGE_READ, message.getId());
                            }
                        }
                    }
                }
                newCount.setText(unreadCount == 0 ? "" : String.valueOf(unreadCount));
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(newState != RecyclerView.SCROLL_STATE_IDLE) {
                    if(!recyclerView.canScrollVertically(1) && total != 0 && total != messages.size()) {
                        ++page;
                        load();
                    }
                    int itemPos = ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
                    Calendar mc = Calendar.getInstance();
                    Calendar c = Calendar.getInstance();
                    mc.setTimeInMillis(messages.get(itemPos).getCreatedAt());
                    String[] months = getContext().getResources().getStringArray(R.array.months);
                    if(c.get(Calendar.YEAR) != mc.get(Calendar.YEAR)) {
                        currentDate.setText(mc.get(Calendar.DATE) + " " + months[mc.get(Calendar.MONTH)] + " " + mc.get(Calendar.YEAR));
                    }
                    else if(c.get(Calendar.MONTH) != mc.get(Calendar.MONTH)) {
                        currentDate.setText(mc.get(Calendar.DATE) + " " + months[mc.get(Calendar.MONTH)]);
                    }
                    else if(c.get(Calendar.DATE) == mc.get(Calendar.DATE)) {
                        currentDate.setText(getContext().getResources().getString(R.string.today));
                    }
                    else if(c.get(Calendar.DATE) - mc.get(Calendar.DATE) == 1) {
                        currentDate.setText(getContext().getResources().getString(R.string.yesterday));
                    }
                    else {
                        currentDate.setText(mc.get(Calendar.DATE) + " " + months[mc.get(Calendar.MONTH)]);
                    }
                    currentDate.setVisibility(View.VISIBLE);
                }
                else if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentDate.setVisibility(View.GONE);
                }
            }
        });
        load();
    }

    private FilterQueryOptions getFilterQueryOptions() {
        List<FilterItem> items = new ArrayList<>();
        return new FilterQueryOptions(page, LIMIT, items);
    }

    private void load() {
        ApiBuilder builder = ApiBuilder.getInstance(getContext());
        builder.setResponseListener(new ApiBuilder.ResponseListener<FilterResponse<MessageModel>>() {
            @Override
            public void onResponse(Call<FilterResponse<MessageModel>> call, Response<FilterResponse<MessageModel>> response) {
                if(response.code() == 201) {
                    total = response.body().getCount();
                    if(response.body().getTotal() != 0) {
                        if(messages == null) {
                            messages = new ArrayList<>();
                        }
                        messages.addAll(response.body().getData());
                        if(rv.getLayoutManager() == null) {
                            rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        }
                        if(rv.getAdapter() == null) {
                            rv.setAdapter(new Adapter());
                        }
                        else {
                            rv.getAdapter().notifyItemRangeInserted(messages.size() - response.body().getTotal(), response.body().getTotal());
                        }
                    }
                }
                else if(response.code() != 400) {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setOnInitializedListener(() -> {
            builder.send(builder.getApi(ApiService.class).filterMessages(getFilterQueryOptions(), currentUser.getId()));
        });
    }
}