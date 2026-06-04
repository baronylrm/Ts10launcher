package com.carlauncher;

import android.graphics.drawable.Drawable;

public class AppItem {
    public String name;
    public String packageName;
    public Drawable icon;

    public AppItem(String name, String packageName, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
    }
}
