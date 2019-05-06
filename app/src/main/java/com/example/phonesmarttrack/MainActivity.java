package com.example.phonesmarttrack;
import android.content.Intent;


import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class MainActivity extends WearableActivity{
    ImageButton milkButton;
    ImageButton cerealButton;

    TextView milkLevel;
    TextView cerealLevel;

    String milkFileName = "milk_counts.txt";
    String cerealFileName = "cereal_counts.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        milkButton = (ImageButton)findViewById(R.id.milkButton);
        cerealButton = (ImageButton)findViewById(R.id.cerealButton);
        milkLevel = findViewById(R.id.milkLevel);
        cerealLevel = findViewById(R.id.cerealLevel);

    }

    @Override
    protected void onResume(){
        super.onResume();
        updateLevelsMilk();
        updateLevelsCereal();
    }

    void updateLevelsMilk(){
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        String fullFileName = SDFile.getAbsolutePath() + File.separator + milkFileName;
        File countFile = new File(fullFileName);
        if (countFile.exists()) {
            BufferedReader dataReader;
            try {
                FileReader fileReader = new FileReader(fullFileName);
                dataReader = new BufferedReader(fileReader);

                try {
                    dataReader.readLine();
                    dataReader.readLine();
                    dataReader.readLine();
                    dataReader.readLine();
                    String level = dataReader.readLine();
                    milkLevel.setText(level);
                    dataReader.close();
                }
                catch(Exception e){
                    Log.e("Exception", "File read failed: " + e.toString());
                }
            }
            catch(Exception e){
                Log.e("Exception", "File read failed: " + e.toString());

            }
        }
        else{
            Log.e("Exception", "File does not exist ");
        }
    }

    void updateLevelsCereal(){
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        String fullFileName = SDFile.getAbsolutePath() + File.separator + cerealFileName;
        File countFile = new File(fullFileName);
        if (countFile.exists()) {
            BufferedReader dataReader;
            try {
                FileReader fileReader = new FileReader(fullFileName);
                dataReader = new BufferedReader(fileReader);

                try {
                    dataReader.readLine();
                    dataReader.readLine();
                    dataReader.readLine();
                    dataReader.readLine();
                    String level = dataReader.readLine();
                    cerealLevel.setText(level);
                    dataReader.close();
                }
                catch(Exception e){
                    Log.e("Exception", "File read failed: " + e.toString());
                }
            }
            catch(Exception e){
                Log.e("Exception", "File read failed: " + e.toString());

            }
        }
        else{
            Log.e("Exception", "File does not exist ");
        }
    }


    void startApp(View view){
        Intent appIntent = new Intent(MainActivity.this, MLActivity.class);
        switch(view.getId()){
            case R.id.milkButton:
                appIntent.putExtra("object", milkButton.getTag().toString());
                startActivity(appIntent);
                break;
            case R.id.cerealButton:
                appIntent.putExtra("object", cerealButton.getTag().toString());
                startActivity(appIntent);
                break;
        }
    }

}
