package com.vapid_software.prodigy.helpers;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.vapid_software.prodigy.R;

public class ConfirmDialog extends Dialog {
    private View root;
    private Context context;
    private String body, title, yesText, noText;
    private OnPositiveConfirmListener onPositiveConfirmListener;
    private OnNegativeConfirmListener onNegativeConfirmListener;
    public interface OnPositiveConfirmListener {
        void onPositiveConfirm();
    }
    public interface OnNegativeConfirmListener {
        void onNegativeConfirm();
    }

    public ConfirmDialog(Context context, String body, String title, String yesText, String noText) {
        super(context);
        this.context = context;
        this.body = body;
        this.title = title;
        this.yesText = yesText;
        this.noText = noText;
        init();
    }

    public void setOnPositiveConfirmListener(OnPositiveConfirmListener listener) {
        onPositiveConfirmListener = listener;
    }

    public void setOnNegativeConfirmListener(OnNegativeConfirmListener listener) {
        onNegativeConfirmListener = listener;
    }

    private void init() {
        root = LayoutInflater.from(context).inflate(R.layout.confirm_dialog, null);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        View topWrp = root.findViewById(R.id.top_wrp);
        TextView yes = root.findViewById(R.id.yes);
        TextView no = root.findViewById(R.id.no);
        TextView titleV = root.findViewById(R.id.title);
        TextView bodyV = root.findViewById(R.id.body);
        View close = root.findViewById(R.id.close);

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        if(title != null) {
            topWrp.setVisibility(View.VISIBLE);
            titleV.setText(title);
        }

        if(yesText != null) {
            yes.setText(yesText);
        }

        if(noText != null) {
            no.setText(noText);
        }

        yes.setOnClickListener((View v) -> {
            if(onPositiveConfirmListener != null) onPositiveConfirmListener.onPositiveConfirm();
            else dismiss();
        });

        close.setOnClickListener((View v) -> {
            dismiss();
        });

        no.setOnClickListener((View v) -> {
            if(onNegativeConfirmListener != null) onNegativeConfirmListener.onNegativeConfirm();
            else dismiss();
        });

        bodyV.setText(body);

        getWindow().setAttributes(lp);
    }

    @Override
    public void show() {
        getWindow().setContentView(root);
        super.show();
    }
}
