package com.vapid_software.prodigy.helpers;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.vapid_software.prodigy.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FilterDialog extends Dialog {
    private long filterDateFrom, filterDateTo;
    private Runnable onFilterClearListener;
    private OnDateSelectedListener onDateSelectedListener;
    private Runnable onFilterUseListener;
    private OnDatePickListener onFromDatePickListener;
    private OnDatePickListener onToDatePickListener;

    public interface OnDatePickListener {
        void onDatePick(DatePicker datePicker, long dateFrom, long dateTo);
    }

    public interface OnDateSelectedListener {
        void onDateSelected(long dateFrom, long dateTo);
    }

    public FilterDialog(Context context, long filterDateFrom, long filterDateTo) {
        super(context);
        this.filterDateFrom = filterDateFrom;
        this.filterDateTo = filterDateTo;
    }

    public void setOnFilterClearListener(Runnable onFilterClearListener) {
        this.onFilterClearListener = onFilterClearListener;
    }

    public void setOnFromDatePickListener(OnDatePickListener onFromDatePickListener) {
        this.onFromDatePickListener = onFromDatePickListener;
    }

    public void setOnToDatePickListener(OnDatePickListener onToDatePickListener) {
        this.onToDatePickListener = onToDatePickListener;
    }

    public void setOnDateSelectedListener(OnDateSelectedListener onDateSelectedListener) {
        this.onDateSelectedListener = onDateSelectedListener;
    }

    public void setOnFilterUseListener(Runnable onFilterUseListener) {
        this.onFilterUseListener = onFilterUseListener;
    }

    private void init() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        View root = getLayoutInflater().inflate(R.layout.filter_dialog, null);
        View button = root.findViewById(R.id.button);
        EditText dateFrom = root.findViewById(R.id.date_from);
        EditText dateTo = root.findViewById(R.id.date_to);
        View clear = root.findViewById(R.id.clear);
        View fromSelect = root.findViewById(R.id.select_date_from_btn);
        View toSelect = root.findViewById(R.id.select_date_to_btn);
        View close = root.findViewById(R.id.close);

        if(filterDateFrom != 0) {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY");
            dateFrom.setText(format.format(new Date(filterDateFrom)));
        }

        if(filterDateTo != 0) {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY");
            dateTo.setText(format.format(new Date(filterDateTo)));
        }

        close.setOnClickListener((View view) -> {
            dismiss();
        });

        button.setOnClickListener((View view) -> {
            if(filterDateTo != 0 && filterDateFrom != 0 && filterDateTo < filterDateFrom) {
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.invalid_date), Toast.LENGTH_LONG).show();
                return;
            }
            if(onFilterUseListener != null) {
                onFilterUseListener.run();
            }
            dismiss();
        });

        fromSelect.setOnClickListener((View view) -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog d = new DatePickerDialog(getContext(), R.style.TimePickerDialogStyle, (DatePicker dv, int year, int month, int dayOfMonth) -> {
                Calendar c1 = Calendar.getInstance();
                c1.set(Calendar.YEAR, year);
                c1.set(Calendar.MONTH, month);
                c1.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                filterDateFrom = c1.getTimeInMillis();
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY");
                dateFrom.setText(format.format(new Date(filterDateFrom)));
                clear.setVisibility(View.VISIBLE);
                if(onDateSelectedListener != null) {
                    onDateSelectedListener.onDateSelected(filterDateFrom, filterDateTo);
                }
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            if(onFromDatePickListener != null) {
                onFromDatePickListener.onDatePick(d.getDatePicker(), filterDateFrom, filterDateTo);
            }
            d.show();
        });

        toSelect.setOnClickListener((View view) -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog d = new DatePickerDialog(getContext(), R.style.TimePickerDialogStyle, (DatePicker dv, int year, int month, int dayOfMonth) -> {
                Calendar c1 = Calendar.getInstance();
                c1.set(Calendar.YEAR, year);
                c1.set(Calendar.MONTH, month);
                c1.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                filterDateTo = c1.getTimeInMillis();
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY");
                dateTo.setText(format.format(new Date(filterDateTo)));
                clear.setVisibility(View.VISIBLE);
                if(onDateSelectedListener != null) {
                    onDateSelectedListener.onDateSelected(filterDateFrom, filterDateTo);
                }
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            if(onToDatePickListener != null) {
                onToDatePickListener.onDatePick(d.getDatePicker(), filterDateFrom, filterDateTo);
            }
            d.show();
        });

        clear.setOnClickListener((View view) -> {
            filterDateFrom = 0;
            filterDateTo = 0;
            dateTo.setText("");
            dateFrom.setText("");
            if(onFilterClearListener != null) {
                onFilterClearListener.run();
            }
        });

        if(filterDateFrom != 0 || filterDateTo != 0) {
            clear.setVisibility(View.VISIBLE);
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
