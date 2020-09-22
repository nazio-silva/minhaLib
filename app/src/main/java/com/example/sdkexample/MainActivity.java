package com.example.sdkexample;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.BulbMain;
import com.example.sdklibrary.ToastMessage;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        super.onStart();

        ToastMessage.s(MainActivity.this, "Minha biblioteca bulb.");

        try {
            System.out.println("Receiver start");
            Context mContext = MainActivity.this;

            if (ActivityCompat.checkSelfPermission((Activity)mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity)mContext, new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, 10);

                System.out.println("Comece a escanear os Bulbs");

            }

        } catch (Exception e) {
            System.out.println("Erro de Exception: " + e);
            e.printStackTrace();
        }

        BulbMain bm = new BulbMain(MainActivity.this);
        bm.startscan();

       /* BulbSupport bs = new BulbSupport();
        bs.init(MainActivity.this);*/
    }

}
