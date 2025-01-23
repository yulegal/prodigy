package com.vapid_software.prodigy.data;

public class OptionsData {
    private String action;
    private int icon;
    private String text;

    public interface OnOptionsDataSelectedListener {
        void onOptionsDataSelected(OptionsData data);
    }

    public OptionsData(String text, int icon, String action) {
        this.action = action;
        this.icon = icon;
        this.text = text;
    }

    public String getAction() {
        return action;
    }

    public int getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }
}
