package com.example.phonesmarttrack;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends WearableActivity implements SensorEventListener {
    int numDataCycles = 1;

    Double[] prevFeatures;
    Double[] features;
    private String countFilepath = "count.txt";
    int prevAverage = -1;
    // GLOBALS
    // Accelerometer
    private LineGraphSeries<DataPoint> timeAccelX = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> timeAccelY = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> timeAccelZ = new LineGraphSeries<>();

    // Gyroscope
    private LineGraphSeries<DataPoint> timeGyroX = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> timeGyroY = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> timeGyroZ = new LineGraphSeries<>();

    // Graph
    private GraphView graph;
    private int graphXBounds = 30;
    private int graphYBounds = 30;
    private int graphColor[] = {Color.argb(255,244,170,50),
            Color.argb(255, 60, 175, 240),
            Color.argb(225, 50, 220, 100)};
    private static final int MAX_DATA_POINTS_UI_IMU = 100; // Adjust to show more points on graph
    public int accelGraphXTime = 0;
    public int gyroGraphXTime = 0;
    public boolean isPlotting = false;

    // UI elements
    private TextView resultText;
    private TextView gesture1CountText;
    private Button gesture1Button;

    // Machine learning
    private Model model;
    private boolean isRecording;
    private DescriptiveStatistics accelTime, accelX, accelY, accelZ;
    private DescriptiveStatistics gyroTime, gyroX, gyroY, gyroZ;
    private DescriptiveStatistics orientTime, orientX, orientY, orientZ;

    private static final int GESTURE_DURATION_SECS = 4;

    boolean isAnyNewGestureRecorded = false;
//    int count = 0;
//    int numSamples = -1;

    int totalCount = 0;
    int totalSamples = -1;
    int currCount = 0;
    int average = 0;
    int lastEmpty = 0;
    int sawEmpty = 0;

    SensorManager sensorManager;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();


        //ACTUAL OUR CODE
        // Get the UI elements
        resultText = findViewById(R.id.resultText);
        gesture1Button = findViewById(R.id.gesture1Button);
        gesture1CountText = findViewById(R.id.gesture1TextView);
        // Initialize data structures for gesture recording
        accelTime = new DescriptiveStatistics();
        accelX = new DescriptiveStatistics();
        accelY = new DescriptiveStatistics();
        accelZ = new DescriptiveStatistics();
        gyroTime = new DescriptiveStatistics();
        gyroX = new DescriptiveStatistics();
        gyroY = new DescriptiveStatistics();
        gyroZ = new DescriptiveStatistics();
        orientTime = new DescriptiveStatistics();
        orientX = new DescriptiveStatistics();
        orientY = new DescriptiveStatistics();
        orientZ = new DescriptiveStatistics();

        // Initialize the model
        model = new Model(this);


        //add text to the buttons
        gesture1Button.setText(model.outputClasses[0]);

        // Get the sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor accelerometer2 = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometer2, SensorManager.SENSOR_DELAY_GAME);

        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);



        // Check permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


    }

    @Override
    protected void onResume(){
        super.onResume();
        restoreData();
        train();
    }

    @Override
    protected void onStop(){
        super.onStop();
        saveData();
    }

    private void restoreData(){
        Log.d("data", "in restore data");
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        String fullFileName = SDFile.getAbsolutePath() + File.separator + countFilepath;
        File countFile = new File(fullFileName);
        if (countFile.exists()) {
            BufferedReader dataReader;
            try {
                FileReader fileReader = new FileReader(fullFileName);
                String text = null;
                dataReader = new BufferedReader(fileReader);
                try {
                    totalCount = Integer.valueOf(dataReader.readLine());
                    totalSamples = Integer.valueOf(dataReader.readLine());
                    currCount = Integer.valueOf(dataReader.readLine());
                    lastEmpty = Integer.valueOf(dataReader.readLine());
                    if (totalSamples != 0) {
                        average = totalCount / totalSamples;
                    }
                    Log.d("data reading totalCount", String.valueOf(totalCount));
                    Log.d("data reading totalSamp", String.valueOf(totalSamples));
                    Log.d("data reading currCount", String.valueOf(currCount));
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

    private void saveData(){
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        String fullFileName = SDFile.getAbsolutePath() + File.separator + countFilepath;
        File countFile = new File(fullFileName);
        if (countFile.exists()) {
            BufferedWriter writer;
            try {
                writer = new BufferedWriter(new FileWriter(fullFileName, false));
                Log.d("data saving totalCount", String.valueOf(totalCount));
                Log.d("data saving totalSamp", String.valueOf(totalSamples));
                Log.d("data saving currCount", String.valueOf(currCount));

                writer.write(Integer.toString(totalCount));
                writer.newLine();
                writer.write(Integer.toString(totalSamples));
                writer.newLine();
                writer.write(Integer.toString(currCount));
                writer.newLine();
                writer.write(Integer.toString(lastEmpty));
                writer.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }else{
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fullFileName));
                writer.write(Integer.toString(totalCount));
                writer.newLine();
                writer.write(Integer.toString(totalSamples));
                writer.newLine();
                writer.write(Integer.toString(currCount));
                writer.newLine();
                writer.write(Integer.toString(lastEmpty));
                writer.close();
                Log.d("data saving totalCount", String.valueOf(totalCount));
                Log.d("data saving totalSamp", String.valueOf(totalSamples));
                Log.d("data saving currCount", String.valueOf(currCount));
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }

    float[] mGravity;
    float[] mGeomagnetic;
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelGraphXTime += 1;

            // Get the data from the event
            long timestamp = event.timestamp;
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];

            // Add the original data to the graph
            DataPoint dataPointAccX = new DataPoint(accelGraphXTime, ax);
            DataPoint dataPointAccY = new DataPoint(accelGraphXTime, ay);
            DataPoint dataPointAccZ = new DataPoint(accelGraphXTime, az);
            timeAccelX.appendData(dataPointAccX, true, MAX_DATA_POINTS_UI_IMU);
            timeAccelY.appendData(dataPointAccY, true, MAX_DATA_POINTS_UI_IMU);
            timeAccelZ.appendData(dataPointAccZ, true, MAX_DATA_POINTS_UI_IMU);

            // Advance the graph
            if (isPlotting) {
                graph.getViewport().setMinX(accelGraphXTime - graphXBounds);
                graph.getViewport().setMaxX(accelGraphXTime);
            }

            // Add to gesture recorder, if applicable
            if (isRecording) {
                accelTime.addValue(timestamp);
                accelX.addValue(ax);
                accelY.addValue(ay);
                accelZ.addValue(az);
            }
        }
        else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroGraphXTime += 1;

            // Get the data from the event
            long timestamp = event.timestamp;
            float gx = event.values[0];
            float gy = event.values[1];
            float gz = event.values[2];

            // Add the original data to the graph
            DataPoint dataPointGyroX = new DataPoint(gyroGraphXTime, gx);
            DataPoint dataPointGyroY = new DataPoint(gyroGraphXTime, gy);
            DataPoint dataPointGyroZ = new DataPoint(gyroGraphXTime, gz);
            timeGyroX.appendData(dataPointGyroX, true, MAX_DATA_POINTS_UI_IMU);
            timeGyroY.appendData(dataPointGyroY, true, MAX_DATA_POINTS_UI_IMU);
            timeGyroZ.appendData(dataPointGyroZ, true, MAX_DATA_POINTS_UI_IMU);

            // Save to file, if applicable
            if (isRecording) {
                gyroTime.addValue(timestamp);
                gyroX.addValue(event.values[0]);
                gyroY.addValue(event.values[1]);
                gyroZ.addValue(event.values[2]);
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }


        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = orientation[0];
                float pitch = orientation[1];

                float roll = orientation[2];
                if (isRecording) {
                    orientTime.addValue(event.timestamp);
                    orientX.addValue(azimuth);
                    orientY.addValue(pitch);
                    orientZ.addValue(roll);
                }
            }
        }






    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void computeFeaturesAndAddSamples(boolean isTraining, String label, View v2)
    {
        isAnyNewGestureRecorded = true;
        // Add the recent gesture to the train or test set
        isRecording = false;

        if (features != null){
            prevFeatures = features;
        }
        //TODO: Replace this function to receive features from Particle
        features = model.computeFeatures(accelTime, accelX, accelY, accelZ,
                gyroTime, gyroX, gyroY, gyroZ, orientTime, orientX, orientY, orientZ);

        if ((isTraining) && (label.compareTo("FULL") == 0) ) {
            model.addTrainingSample(features, label);
            if (prevFeatures != null) {
                model.addTrainingSample(prevFeatures, "EMP");
            }
            train();
        }
        else
            model.assignTestSample(features);

        // Predict if the recent sample is for testing
        if (!isTraining) {
            String result = model.test();

            if (result != null) {
                Log.d("UGHH  Bef lastEmpty", String.valueOf(lastEmpty));
                Log.d("UGHH  Bef sawEmpty", String.valueOf(sawEmpty));
                if (result.compareTo("EMP") != 0){ //NOT EMPTY
                    lastEmpty = 0;
                    if(sawEmpty == 1){
                        updateCounts();
                        //updateCountsFile(true);
                        currCount = 0;
                        sawEmpty = 0;
                    }
                    if((currCount < (0.125*average))){
                        resultText.setText("Result: " + "100%" + "Count:" + String.valueOf(currCount) + "Normal:" + String.valueOf(average));
                    }
                    else if((currCount < (0.375*average) && (currCount >= (0.125*average)))){
                        resultText.setText("Result: " + "75%" + "Count:" + String.valueOf(currCount) + "Normal:" + String.valueOf(average));
                    }
                    else if(currCount < (0.625*average) && (currCount >= (0.375*average))){
                        resultText.setText("Result: " + "50%" + "Count:" + String.valueOf(currCount) + "Normal:" + String.valueOf(average));
                    }
                    else if((currCount >= (0.635*average))&&(currCount < (0.875*average))){
                        resultText.setText("Result: " + "25%" + "Count:" + String.valueOf(currCount) + "Normal:" + String.valueOf(average));
                    }
                    else{
                        resultText.setText("REALLY UNSURE: Count:" + String.valueOf(currCount) + "Normal:" + String.valueOf(average));
                    }
                }
                else{ //EMPTY
                    if (lastEmpty == 1){
                        resultText.setText("Result: " + "0%" + "Count:" + String.valueOf(currCount) + "Normal:" + String.valueOf(average));
                        lastEmpty = 0;
                        sawEmpty = 1;
                    }
                    else if (currCount > (0.75*average)) {
                        resultText.setText("Result: " + "0%" + "Count:" + String.valueOf(currCount) + "Normal:" + String.valueOf(average));
                        sawEmpty = 1;
                    } else{
                        resultText.setText("Unsure redo");
                        lastEmpty = 1;
                    }
                }

                if(totalSamples <= numDataCycles){
                    lastEmpty = 0;
                    sawEmpty = 0;
                }
                Log.d("UGHH  Af lastEmpty", String.valueOf(lastEmpty));
                Log.d("UGHH  Af sawEmpty", String.valueOf(sawEmpty));
            }
        }

        // Update number of samples shown
        updateTrainDataCount();
        v2.setEnabled(true);
    }

    public void updateCounts(){
        totalSamples++;
        totalCount = totalCount + currCount;
        if(totalSamples > 0) {
            average = totalCount / totalSamples;
            Log.d("data", "upated average to" + average + "count: " + totalCount + "samples" + totalSamples);
        }
    }

   /**
     * Records a gesture that is GESTURE_DURATION_SECS long
     */
    public void recordGesture(View v) {
        final View v2 = v;

        // Figure out which button got pressed to determine label
        final String label;
        final boolean isTraining;
        switch (v.getId()) {
            case R.id.gesture1Button: //FULL
                label = model.outputClasses[0];
                isTraining = true;
                if (totalSamples <= numDataCycles) {
                    updateCounts();
                    //updateCountsFile(true);
                    currCount = 0;
                }
                break;
            default:
                label = "?";
                isTraining = false;
                currCount++;
                break;
        }



        //TODO: When you stop using Android sensors, you might want to remove the timers and directly add features from Particle to the set
        // Create the timer to start data collection from the Android sensors
        Timer startTimer = new Timer();
        TimerTask startTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        accelTime.clear(); accelX.clear(); accelY.clear(); accelZ.clear();
                        gyroTime.clear(); gyroX.clear(); gyroY.clear(); gyroZ.clear();
                        orientTime.clear(); orientX.clear(); orientY.clear(); orientZ.clear();
                        isRecording = true;
                        v2.setEnabled(false);
                    }
                });
            }
        };

        // Create the timer to stop data collection
        Timer endTimer = new Timer();
        TimerTask endTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        computeFeaturesAndAddSamples(isTraining,label, v2);
                    }
                });
            }
        };

        // Start the timers
        startTimer.schedule(startTask, 0);
        endTimer.schedule(endTask, GESTURE_DURATION_SECS*1000);
    }

    public void train() {
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        String fullFileName = SDFile.getAbsolutePath() + File.separator + model.trainDataFilepath;
        File trainingFile = new File(fullFileName);
        if (trainingFile.exists() && !isAnyNewGestureRecorded)
//        if (trainingFile.exists())
        {
            Log.d("TAG", "Training file exists: " + fullFileName);
            model.train(false);

        } else {
            Log.d("TAG", "Need to create training file: " + fullFileName);
            model.train(true);
        }
    }

    /**
     * Trains the model as long as there is at least one sample per class
     */
    public void trainModel(View v) {
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        String fullFileName = SDFile.getAbsolutePath() + File.separator + model.trainDataFilepath;
        File trainingFile = new File(fullFileName);
        if (trainingFile.exists() && !isAnyNewGestureRecorded)
//        if (trainingFile.exists())
        {
            Log.d("TAG","Training file exists: "+fullFileName);
            model.train(false);

        }
        else
        {
            Log.d("TAG","Need to create training file: "+fullFileName);
            model.train(true);
        }
    }

    /**
     * Resets the training data of the model
     */
    public void clearModel(View v) {
        model.resetTrainingData();
        updateTrainDataCount();
        resultText.setText("Result: ");
        isAnyNewGestureRecorded = false;
    }

    //Deletes the training file
    public void deleteTrainingFile (View v)
    {
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        String fullFileName = SDFile.getAbsolutePath() + File.separator + model.trainDataFilepath;
        File trainingFile = new File(fullFileName);
        trainingFile.delete();
    }

    public void updateTrainDataCount() {
        gesture1CountText.setText("Num samples: "+model.getNumTrainSamples(0));
    }
}
