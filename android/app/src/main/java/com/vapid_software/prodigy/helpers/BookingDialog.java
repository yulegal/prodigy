package com.vapid_software.prodigy.helpers;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.models.BookingModel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BookingDialog extends Dialog {
    private BookingModel booking;
    private Runnable onBookingCanceledListener;
    private Runnable onBookingDoneListener;

    public BookingDialog(Context context, BookingModel booking) {
        super(context);
        this.booking = booking;
    }

    public void setOnBookingCanceledListener(Runnable onBookingCanceledListener) {
        this.onBookingCanceledListener = onBookingCanceledListener;
    }

    public void setOnBookingDoneListener(Runnable onBookingDoneListener) {
        this.onBookingDoneListener = onBookingDoneListener;
    }

    private void init() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.booking_info_dialog, null);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        View statusWrp = root.findViewById(R.id.status_wrp);
        TextView status = root.findViewById(R.id.status);
        TextView date = root.findViewById(R.id.date);
        TextView type = root.findViewById(R.id.type);
        TextView extra = root.findViewById(R.id.extra);
        TextView branch = root.findViewById(R.id.branch);
        TextView name = root.findViewById(R.id.name);
        String locale = getContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.YYYY HH:mm");
        View branchWrp = root.findViewById(R.id.branch_wrp);
        View extraWrp = root.findViewById(R.id.extra_wrp);
        View close = root.findViewById(R.id.close);
        View doneBtn = root.findViewById(R.id.done_btn);
        View cancelBtn = root.findViewById(R.id.cancel_btn);

        doneBtn.setOnClickListener((View v) -> {
            if(onBookingDoneListener != null) {
                onBookingDoneListener.run();
            }
        });

        cancelBtn.setOnClickListener((View v) -> {
            if(onBookingCanceledListener != null) {
                onBookingCanceledListener.run();
            }
        });

        close.setOnClickListener((View v) -> {
            dismiss();
        });

        name.setText(booking.getService().getName());
        type.setText(booking.getService().getCategory().getName().get(locale));
        date.setText(dateFormat.format(new Date(booking.getBookDate())));

        if(!booking.getStatus().equals(Defs.BookingStatus.ACTIVE)) {
            statusWrp.setVisibility(View.VISIBLE);
            status.setText(Defs.BookingStatus.getTranslationByType(getContext(), booking.getStatus()));
            cancelBtn.setVisibility(View.GONE);
            doneBtn.setVisibility(View.GONE);
        }

        if(booking.getBranch() != null) {
            branchWrp.setVisibility(View.VISIBLE);
            branch.setText(booking.getBranch().getAddress().getAddress());
        }

        if(booking.getExtra() != null) {
            extraWrp.setVisibility(View.VISIBLE);
            extra.setText(
                    getContext().getResources().getString(R.string.table,
                            booking.getExtra().getTable() + 1,
                            booking.getService().getExtra().getTables().get(booking.getExtra().getTable()))
            );
        }

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        setContentView(root);
        getWindow().setAttributes(lp);
    }

    @Override
    public void show() {
        init();
        super.show();
    }
}
