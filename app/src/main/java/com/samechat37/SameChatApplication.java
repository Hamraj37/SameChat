package com.samechat37;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class SameChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // This applies dynamic colors (Monet) to all activities in the app.
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}