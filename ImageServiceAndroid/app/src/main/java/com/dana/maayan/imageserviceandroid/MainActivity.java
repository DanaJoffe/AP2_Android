package com.dana.maayan.imageserviceandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * called when button is pressed - starts ImageServiceService
     * @param view a view
     */
    public void startService(View view){
        Intent intent = new Intent(this, ImageServiceService.class);
        startService(intent);
    }

    /**
     * called when button is pressed - destroys ImageServiceService
     * @param view a view
     */
    public void stopService(View view){
        Intent intent = new Intent(this, ImageServiceService.class);
        stopService(intent);
    }
}