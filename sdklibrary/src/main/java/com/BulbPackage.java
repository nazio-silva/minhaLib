package com;

import android.content.Context;
import android.os.Build;
import android.view.ViewManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.bulb.support.beacon.BulbSupport;

import java.lang.annotation.Native;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BulbPackage {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    //@Override
    public List<Native> createNativeModules(@NonNull Context context) {
        List<Native> modules = new ArrayList<Native>();
        modules.add((Native) new BulbMain(context));

        return modules;
    }

    @NonNull
    public List<ViewManager> createViewManagers(@NonNull Context context) {

        BulbSupport.getInstance().init(context);

        return Collections.emptyList();
    }
}
