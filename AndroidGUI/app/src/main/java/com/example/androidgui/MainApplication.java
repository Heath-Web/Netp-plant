package com.example.androidgui;

import android.app.Application;

public class MainApplication extends Application {
    private static MainApplication mApp;
    public Data data = new Data();

    public static MainApplication getInstance(){
        return mApp;
    }

    public class Data{
        public Float humidity;
        public Float temperature;
        public Float moisture;
        public Float light;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }
}
