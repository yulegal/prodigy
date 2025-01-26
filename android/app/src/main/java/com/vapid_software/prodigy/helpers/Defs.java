package com.vapid_software.prodigy.helpers;


import android.content.Context;
import android.graphics.Color;

import com.vapid_software.prodigy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Defs {
    public final static String SOCKET_INFO_LABEL = "Socket info";
    public final static String TOKEN_ACTION_LABEL = "Token action";
    public final static String LOG_ERROR_LABEL = "[error]";
    public final static String LAUNCH_FRAGMENT = "[launch_fragment]";

    public static class WS_MESSAGES {
        public static class SERVER {
            public final static String WHO_ARE_YOU = "WHO_ARE_YOU";
            public final static String ROLE_CHANGED = "ROLE_CHANGED";
            public final static String MESSAGE_READ = "SERVER_MESSAGE_READ";
            public final static String USER_OFFLINE = "USER_OFFLINE";
            public final static String USER_ONLINE = "USER_ONLINE";
            public final static String MESSAGE_DELETED = "MESSAGE_DELETED";
            public final static String CHAT_REMOVED = "CHAT_REMOVED";
            public final static String MESSAGE_EDITED = "MESSAGE_EDITED";
            public final static String NEW_MESSAGE = "NEW_MESSAGE";
            public final static String USER_ONLINE_STATUS = "USER_ONLINE_STATUS";
            public final static String LOGGED_OUT = "LOGGED_OUT";
            public final static String CONTACT_LOGGED_OUT = "CONTACT_LOGGED_OUT";
            public final static String MESSAGE_TOGGLE_RATING = "MESSAGE_TOGGLE_RATING";
        }
        public static class CLIENT {
            public final static String I_AM = "I_AM";
            public final static String MESSAGE_READ = "CLIENT_MESSAGE_READ";
            public final static String CHECK_USER_ONLINE = "CHECK_USER_ONLINE";
            public final static String LOGOUT = "LOGOUT";
            public final static String MESSAGE_RATED = "MESSAGE_RATED";
        }
    }

    public static class NotificationType {
        public final static String NEW_BOOKING = "NEW_BOOKING";
        public final static String BOOKING_CANCELED = "BOOKING_CANCELED";
        public final static String USER_ADDED_TO_BRANCH = "USER_ADDED_TO_BRANCH";
        public final static String USER_REMOVED_FROM_BRANCH = "USER_REMOVED_FROM_BRANCH";
        public final static String REBOOKED = "REBOOKED";
        public final static String FEE_CHARGED = "FEE_CHARGED";
        public final static String PAYMENT_PERIOD_APPROACHES = "PAYMENT_PERIOD_APPROACHES";
        public final static String SERVICE_BLOCKED_DUE_TO_LACK_BALANCE = "SERVICE_BLOCKED_DUE_TO_LACK_BALANCE";
        public final static String TRIAL_PERIOD_END_APPROACHES = "TRIAL_PERIOD_END_APPROACHES";
        public final static String BOOKING_FINISHED = "BOOKING_FINISHED";
        public final static String NEW_BROADCAST = "NEW_BROADCAST";
    }

    public static class Role {
        public final static String PROVIDER = "529b2630-8905-4f47-a46c-6b58f9aef84c";
        public final static String HELPER = "529b2630-8905-4f47-a46c-6b58f9aef84d";
        public final static String USER = "529b2630-8905-4f47-a46c-6b58f9aef84b";
    }

    public static class SessionUnit {
        public final static String MINUTES = "minutes";
        public final static String HOURS = "hours";
        private static String[][] translations;
        public final static List<String> UNITS;

        static {
            UNITS = new ArrayList<>();
            UNITS.add(MINUTES);
            UNITS.add(HOURS);
        }

        public static String[][] getTranslations(Context context) {
            if(translations == null) {
                translations = new String[][] {
                        new String[]{
                                MINUTES,
                                context.getResources().getString(R.string.session_unit_minutes)
                        },
                        new String[]{
                                HOURS,
                                context.getResources().getString(R.string.session_unit_hours)
                        }
                };
            }
            return translations;
        }
    }

    public static class CategoryType {
        public final static String HAIRCUT = "HAIRCUT";
        public final static String CAFE = "CAFE";
        public final static Map<String, Integer> ICONS = new HashMap<>();

        static {
            ICONS.put(HAIRCUT, R.drawable.haircut);
            ICONS.put(CAFE, R.drawable.cafe);
        }
    }

    public static class BookingStatus {
        public final static String ACTIVE = "ACTIVE";
        public final static String CANCELED = "CANCELED";
        public final static String DONE = "DONE";

        public static String getTranslationByType(Context context, String type) {
            switch(type) {
                case ACTIVE: return context.getResources().getString(R.string.booking_status_active);
                case CANCELED: return context.getResources().getString(R.string.booking_status_canceled);
                case DONE: return context.getResources().getString(R.string.booking_status_done);
            }
            return null;
        }

        public static int getStatusColorByType(String type) {
            switch(type) {
                case ACTIVE: return Color.parseColor("#228B22");
                case CANCELED: return Color.parseColor("#ff0000");
                case DONE: return Color.parseColor("#104E8B");
            }
            return 0;
        }
    }

    public static class WeekDay {
        public final static String MONDAY = "MONDAY";
        public final static String TUESDAY = "TUESDAY";
        public final static String WEDNESDAY = "WEDNESDAY";
        public final static String THURSDAY = "THURSDAY";
        public final static String FRIDAY = "FRIDAY";
        public final static String SATURDAY = "SATURDAY";
        public final static String SUNDAY = "SUNDAY";

        public static String getWeekDayByPosition(int position) {
            switch(position) {
                case 0: return MONDAY;
                case 1: return TUESDAY;
                case 2: return WEDNESDAY;
                case 3: return THURSDAY;
                case 4: return FRIDAY;
                case 5: return SATURDAY;
                case 6: return SUNDAY;
            }
            return null;
        }

        public static int getWeekdayPositionByName(String name) {
            switch(name) {
                case MONDAY: return 0;
                case TUESDAY: return 1;
                case WEDNESDAY: return 2;
                case THURSDAY: return 3;
                case FRIDAY: return 4;
                case SATURDAY: return 5;
                case SUNDAY: return 6;
            }
            return -1;
        }

        public static int getWeekdayDatePositionByName(String name) {
            switch(name) {
                case SUNDAY: return 1;
                case MONDAY: return 2;
                case TUESDAY: return 3;
                case WEDNESDAY: return 4;
                case THURSDAY: return 5;
                case FRIDAY: return 6;
                case SATURDAY: return 7;
            }
            return -1;
        }

        public static String getWeekDayTranslation(Context context, String weekday) {
            String[] trans = context.getResources().getStringArray(R.array.week_days);
            switch(weekday) {
                case MONDAY: return trans[0];
                case TUESDAY: return trans[1];
                case WEDNESDAY: return trans[2];
                case THURSDAY: return trans[3];
                case FRIDAY: return trans[4];
                case SATURDAY: return trans[5];
                case SUNDAY: return trans[6];
            }
            return null;
        }
    }

    public static class PermissionCode {
        public final static int GALLERY_CODE = 1000;
        public final static int CONTACTS_CODE = 1100;
        public final static int LOCATION_CODE = 1200;
    }

    public static class ResultCode {
        public final static int GALLERY_CODE = 1000;
    }

    public static class FavoritesOptions {
        public final static String DELETE = "DELETE";
    }

    public static class BroadcastActions {
        public final static String CANCEL_BOOKING = "CANCEL_BOOKING";
    }

    public static class ChatOptions {
        public final static String DELETE = "DELETE";
    }
}
