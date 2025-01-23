package com.vapid_software.prodigy.helpers;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.models.WorkScheduleModel;

import java.util.Calendar;

public class WorkScheduleWindow extends PopupWindow {
    private Context context;
    private WorkScheduleModel schedules[];

    private interface OnTimeSelectedListener {
        void onTimeSelected(long time);
    }

    private class WeekdayAdapter extends RecyclerView.Adapter {
        private String wds[];

        public WeekdayAdapter(String wds[]) {
            this.wds = wds;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.weekday_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            View root = holder.itemView;
            TextView name = root.findViewById(R.id.name);
            TextView time = root.findViewById(R.id.time);
            CheckBox cb = root.findViewById(R.id.cb);
            ImageView timeIcon = root.findViewById(R.id.time_icon);
            ImageView allDayIcon = root.findViewById(R.id.all_day);
            name.setText(wds[position]);
            allDayIcon.setOnClickListener((View v) -> {
                if(cb.isChecked()) {
                    schedules[position] = new WorkScheduleModel(0, 0, Defs.WeekDay.getWeekDayByPosition(position), true);
                    notifyItemChanged(position);
                }
            });
            timeIcon.setOnClickListener((View v) -> {
                if(cb.isChecked()) {
                    selectTime((long st) -> {
                        selectTime((long et) -> {
                            if(st >= et) {
                                Toast.makeText(context, context.getResources().getString(R.string.start_end_time_wrong), Toast.LENGTH_LONG).show();
                            }
                            else {
                                schedules[position] = new WorkScheduleModel(st, et, Defs.WeekDay.getWeekDayByPosition(position), false);
                                notifyItemChanged(position);
                            }
                        }, context.getResources().getString(R.string.end_time_title));
                    }, context.getResources().getString(R.string.start_time_title));
                }
            });
            cb.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                if(!isChecked && schedules[position] != null) {
                    schedules[position] = null;
                    notifyItemChanged(position);
                }
                name.setTextColor(Color.parseColor(isChecked ? "#232425" : "#999999"));
                timeIcon.setColorFilter(Color.parseColor(isChecked ? "#232425" : "#999999"));
                allDayIcon.setColorFilter(Color.parseColor(isChecked ? "#232425" : "#999999"));
            });
            if(schedules[position] != null) {
                if(schedules[position].isAllDay()) {
                    time.setText(context.getResources().getString(R.string.all_day));
                    time.setTextColor(Color.parseColor("#458B00"));
                }
                else {
                    long h = (schedules[position].getStartTime() / 1000) / 3600;
                    long m = (schedules[position].getStartTime() / 1000) / 60 % 60;
                    String s = String.join(":", String.valueOf(h < 10 ? "0" + h : h), String.valueOf(m < 10 ? "0" + m : m));
                    h = (schedules[position].getEndTime() / 1000) / 3600;
                    m = (schedules[position].getEndTime() / 1000) / 60 % 60;
                    String e = String.join(":", String.valueOf(h < 10 ? "0" + h : h), String.valueOf(m < 10 ? "0" + m : m));
                    time.setText(String.join(" - ", s, e));
                    time.setTextColor(Color.parseColor("#666666"));
                }
                time.setVisibility(View.VISIBLE);
            }
            else {
                time.setVisibility(View.GONE);
            }
            cb.setChecked(schedules[position] != null);
            name.setTextColor(Color.parseColor(cb.isChecked() ? "#232425" : "#999999"));
            timeIcon.setColorFilter(Color.parseColor(cb.isChecked() ? "#232425" : "#999999"));
            allDayIcon.setColorFilter(Color.parseColor(cb.isChecked() ? "#232425" : "#999999"));
        }

        private void selectTime(OnTimeSelectedListener onTimeSelectedListener, String title) {
            Calendar c = Calendar.getInstance();
            TimePickerDialog dialog = new TimePickerDialog(context, R.style.TimePickerDialogStyle, (TimePicker view, int hourOfDay, int minute) -> {
                Calendar c1 = Calendar.getInstance();
                c1.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c1.set(Calendar.MINUTE, minute);
                long timeInMillis = hourOfDay * 3600 * 1000 + minute * 60 * 1000;
                if(onTimeSelectedListener != null) {
                    onTimeSelectedListener.onTimeSelected(timeInMillis);
                }
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
            dialog.setTitle(title);
            dialog.show();
        }

        @Override
        public int getItemCount() {
            return wds.length;
        }
    }

    public WorkScheduleWindow(Context context, WorkScheduleModel[] schedules) {
        this.context = context;
        this.schedules = schedules;
        init();
    }

    private void init() {
        View root = LayoutInflater.from(context).inflate(R.layout.schedule_select_dialog, null);
        RecyclerView rv = root.findViewById(R.id.rv);
        View close = root.findViewById(R.id.close);
        rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(new WeekdayAdapter(context.getResources().getStringArray(R.array.week_days)));
        setContentView(root);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        close.setOnClickListener((View view) -> {
            dismiss();
        });
        setOutsideTouchable(true);
    }

    public void show() {
        showAtLocation(getContentView(), Gravity.NO_GRAVITY, 0, 0);
    }
}
