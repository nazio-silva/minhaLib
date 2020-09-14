package com.example.sdkexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.BulbMain;
import com.example.sdklibrary.ToastMessage;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToastMessage.s(MainActivity.this, "Minha biblioteca...");

        BulbMain obj = new BulbMain(MainActivity.this);
        obj.onStartScan();

    }

    protected String getMainComponentName() {
        return "sdkexample";
    }
}
