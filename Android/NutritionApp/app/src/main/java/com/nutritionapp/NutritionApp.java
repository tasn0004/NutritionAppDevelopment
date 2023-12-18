package com.nutritionapp;

import android.app.Application;
import com.parse.Parse;

/**
 * This class is setting up Parse, analytics libraries, crash reporting, etc.
 * This ensures that these libraries are initialized once and are available throughout the app's lifecycle.
 */
public class NutritionApp extends Application {
    /**
     * Initialize Parse here:
     * The Application's `onCreate` is the earliest point of entry in the app lifecycle.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());
    }
}
