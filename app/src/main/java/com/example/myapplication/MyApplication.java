package com.example.myapplication;

import android.app.Application;



public class MyApplication extends Application {


    private LelinkHelper mLelinkHelper;
    private static MyApplication sMyApplication;

    public static MyApplication getMyApplication() {
        return sMyApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sMyApplication = this;
        mLelinkHelper = LelinkHelper.getInstance(getApplicationContext());
    }

    public LelinkHelper getLelinkHelper() {
        return mLelinkHelper;
    }
}
