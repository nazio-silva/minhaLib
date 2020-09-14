package com.example.sdklibrary;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.InvocationTargetException;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //SoLoader.init(this, /* native exopackage */ false);

    }

}
