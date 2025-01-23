package com.vapid_software.prodigy.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.helpers.OnSwipeTouchListener;
import com.vapid_software.prodigy.helpers.Utils;

import java.util.List;

public class MediaPlayFragment extends BaseExtraFragment {
    private List<String> urlList;
    private int current;
    private View forward, reply, delete, backText, wrapper;
    private VideoView video;
    private ImageView image;
    private TextView of;
    private OnDeleteClickedListener onDeleteClickedListener;
    private OnForwardClickedListener onForwardClickedListener;
    private OnReplyClickedListener onReplyClickedListener;

    public interface OnDeleteClickedListener {
        void onDeleteClicked();
    }

    public interface OnReplyClickedListener {
        void onReplyClicked();
    }

    public interface OnForwardClickedListener {
        void onForwardClicked();
    }

    private View.OnClickListener backClicked = (View v) -> {
        back.performClick();
    };

    public void setUrlList(List<String> urlList) {
        this.urlList = urlList;
    }

    public void setOnDeleteClickedListener(OnDeleteClickedListener onDeleteClickedListener) {
        this.onDeleteClickedListener = onDeleteClickedListener;
    }

    public void setOnReplyClickedListener(OnReplyClickedListener onReplyClickedListener) {
        this.onReplyClickedListener = onReplyClickedListener;
    }

    public void setOnForwardClickedListener(OnForwardClickedListener onForwardClickedListener) {
        this.onForwardClickedListener = onForwardClickedListener;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    private View.OnClickListener onDeleteClicked = (View v) -> {
        if(onDeleteClickedListener != null) {
            onDeleteClickedListener.onDeleteClicked();
        }
    };

    private View.OnClickListener onForwardClicked = (View v) -> {
        if(onForwardClickedListener != null) {
            onForwardClickedListener.onForwardClicked();
        }
    };

    private View.OnClickListener replyClicked = (View v) -> {
        if(onReplyClickedListener != null) {
            onReplyClickedListener.onReplyClicked();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = getLayoutInflater().inflate(R.layout.fragment_media_play, container, false);
        back = root.findViewById(R.id.back);
        backText = root.findViewById(R.id.back_text);
        reply = root.findViewById(R.id.reply);
        forward = root.findViewById(R.id.forward);
        delete = root.findViewById(R.id.delete);
        of = root.findViewById(R.id.of);
        video = root.findViewById(R.id.video);
        image = root.findViewById(R.id.image);
        wrapper = root.findViewById(R.id.wrapper);
        backText.setOnClickListener(backClicked);
        delete.setOnClickListener(onDeleteClicked);
        forward.setOnClickListener(onForwardClicked);
        reply.setOnClickListener(replyClicked);
        wrapper.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            @Override
            public void onSwipeRight() {
                if(current == 0) return;
                current--;
                String url = String.join("/", ApiBuilder.ADDONS_PATH, urlList.get(current));
                String mime = Utils.getMimeType(url);
                if(mime.startsWith("video")) {
                    image.setVisibility(View.GONE);
                    video.setVisibility(View.VISIBLE);
                    MediaController controller = new MediaController(getContext());
                    video.setVideoURI(Uri.parse(url));
                    video.setMediaController(controller);
                    video.setVisibility(View.VISIBLE);
                    video.start();
                    controller.show();
                }
                else if(mime.startsWith("image")) {
                    image.setVisibility(View.VISIBLE);
                    video.setVisibility(View.GONE);
                    Picasso.get().load(url).into(image);
                    if(video.isPlaying()) {
                        video.stopPlayback();
                    }
                }
                of.setText(String.format("%d of %d", current + 1, urlList.size()));
            }

            @Override
            public void onSwipeLeft() {
                if(current == urlList.size() - 1) return;
                current++;
                String url = String.join("/", ApiBuilder.ADDONS_PATH, urlList.get(current));
                String mime = Utils.getMimeType(url);
                if(mime.startsWith("video")) {
                    image.setVisibility(View.GONE);
                    video.setVisibility(View.VISIBLE);
                    MediaController controller = new MediaController(getContext());
                    video.setVideoURI(Uri.parse(url));
                    video.setMediaController(controller);
                    video.setVisibility(View.VISIBLE);
                    video.start();
                    controller.show();
                }
                else if(mime.startsWith("image")) {
                    image.setVisibility(View.VISIBLE);
                    video.setVisibility(View.GONE);
                    Picasso.get().load(url).into(image);
                    if(video.isPlaying()) {
                        video.stopPlayback();
                    }
                }
                of.setText(String.format("%d of %d", current + 1, urlList.size()));
            }
        });
        init();
        return root;
    }

    @Override
    protected void init() {
        super.init();
        String url = String.join("/", ApiBuilder.ADDONS_PATH, urlList.get(current));
        String mime = Utils.getMimeType(url);
        if(mime.startsWith("video")) {
            MediaController controller = new MediaController(getContext());
            video.setVideoURI(Uri.parse(url));
            video.setMediaController(controller);
            video.setVisibility(View.VISIBLE);
            video.start();
            controller.show();
        }
        else {
            image.setVisibility(View.VISIBLE);
            Picasso.get().load(url).into(image);
        }
        of.setText(String.format("%d of %d", current + 1, urlList.size()));
    }
}