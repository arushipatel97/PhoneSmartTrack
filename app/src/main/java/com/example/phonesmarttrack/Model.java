package com.example.phonesmarttrack;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.LWL;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.core.Instances;

public class Model {
    // Examples of how the .arff format:
    // https://www.programcreek.com/2013/01/a-simple-machine-learning-example-in-java/
    // https://www.cs.waikato.ac.nz/~ml/weka/arff.html
    boolean DEBUG_MODE = false;

    private Map<String, ArrayList<String[]>> trainingData;
    private String[] testData;
    public Map<String, String> featureNames;
    public String trainDataFilepath = "trainData.arff";
    private String testDataFilepath = "testData.arff";

    private Classifier model;
    private Context context;

    // TODO optional: give your gestures more informative names
    public String[] outputClasses = {"FULL", "EMP", "25","50", "75"};


    public Model(Context context) {
        this.context = context;
        resetTrainingData();

        // Specify the features
        featureNames = new TreeMap<>();
        // TODO optional: create more features with more informative names
        featureNames.put("Feature1", "numeric");
        featureNames.put("Feature2", "numeric");
        featureNames.put("Feature3", "numeric");
        featureNames.put("Feature4", "numeric");
        featureNames.put("Feature5", "numeric");
        featureNames.put("Feature6", "numeric");
        featureNames.put("Feature7", "numeric");
        featureNames.put("Feature8", "numeric");
        featureNames.put("Feature9", "numeric");
        featureNames.put("Feature10", "numeric");
        featureNames.put("Feature11", "numeric");
        featureNames.put("Feature12", "numeric");
        featureNames.put("Feature13", "numeric");
        featureNames.put("Feature14", "numeric");
        featureNames.put("Feature15", "numeric");
        featureNames.put("Feature16", "numeric");
//        featureNames.put("Feature17", "numeric");
//        featureNames.put("Feature18", "numeric");
//        featureNames.put("Feature19", "numeric");
//        featureNames.put("Feature20", "numeric");
//        featureNames.put("Feature21", "numeric");
//        featureNames.put("Feature22", "numeric");
//        featureNames.put("Feature23", "numeric");
//        featureNames.put("Feature24", "numeric");
//        featureNames.put("Feature25", "numeric");
//        featureNames.put("Feature26", "numeric");
//        featureNames.put("Feature27", "numeric");
//        featureNames.put("Feature28", "numeric");
    }

    /**
     * Add a sample to the training or testing set with the corresponding label
     * @param atime: the time for the accelerometer data
     * @param ax: the x-acceleration data
     * @param ay: the y-acceleration data
     * @param az: the z-acceleration data
     * @param gtime: the time for the accelerometer data
     * @param gx: the x-gyroscope data
     * @param gy: the y-gyroscope data
     * @param gz: the z-gyroscope data
     * @param outputLabel: the label for the data
     * @param isTraining: whether the sample should go into the train or test set
     */

    public Double[] computeFeatures(DescriptiveStatistics atime, DescriptiveStatistics ax, DescriptiveStatistics ay, DescriptiveStatistics az,
                                    DescriptiveStatistics gtime, DescriptiveStatistics gx, DescriptiveStatistics gy, DescriptiveStatistics gz,
                                    DescriptiveStatistics otime, DescriptiveStatistics od)
    {
        Double[] data = new Double[featureNames.keySet().size()];
//        data[0] = az.getVariance(); // good
//        data[1] = ay.getMax(); // good
//        data[2] = ay.getMean(); // good
//        data[3] = gy.getMax(); // good
//        data[4] = gx.getMin(); // good
//        data[5] = gx.getMax(); // good

        data[0] = az.getMean(); //cereal
        data[1] = az.getVariance(); //good
        data[2] = ay.getMean(); //good
        data[3] = ay.getMax();//good //cereal
        data[4] = ay.getVariance(); //good
        data[5] = ax.getMean(); //cereal
        data[6] = gz.getMax(); //good
        data[7] = gz.getVariance(); //good
        data[8] = gy.getMean(); //good
        data[9] = gx.getMin(); //good
        data[10] = gx.getMax(); //good //cereal
        data[11] = gx.getVariance(); //cereal
        data[12] = od.getMean(); //good
        data[13] = od.getMin(); //good
        data[14] = od.getMax(); //cereal
        data[15] = od.getVariance(); //good



//        data[0] = az.getMean(); //cereal
//        data[1] = az.getMax();
//        data[2] = az.getMin();
//        data[3] = az.getVariance(); //good
//
//        data[4] = ay.getMean(); //good
//        data[5] = ay.getMax();//good //cereal
//        data[6] = ay.getMin();
//        data[7] = ay.getVariance(); //good
//
//        data[8] = ax.getMean(); //cereal
//        data[9] = ax.getMax();
//        data[10] = ax.getMin();
//        data[11] = ax.getVariance();
//
//        data[12] = gz.getMean();
//        data[13] = gz.getMax(); //good
//        data[14] = gz.getMin();
//        data[15] = gz.getVariance(); //good
//
//        data[16] = gy.getMean(); //good
//        data[17] = gy.getMax();
//        data[18] = gz.getMin();
//        data[19] = gy.getVariance();
//
//        data[20] = gx.getMean();
//        data[21] = gx.getMin(); //good
//        data[22] = gx.getMax(); //good //cereal
//        data[23] = gx.getVariance(); //cereal
//
//        data[24] = od.getMean(); //good
//        data[25] = od.getMin(); //good
//        data[26] = od.getMax(); //cereal
//        data[27] = od.getVariance(); //good

        return data;
    }

    //adds a training instance to the model
    public void addTrainingSample(Double[] features, String outputLabel) {


        // Convert the feature vector to Strings
        String[] stringData = new String[featureNames.keySet().size()];
        for (int i=0; i<featureNames.keySet().size(); i++) {
            stringData[i] = Double.toString(features[i]);
        }

        // Add to the list of feature samples as strings to the training set
        ArrayList<String[]> currentSamples = trainingData.get(outputLabel);
        currentSamples.add(stringData);
        trainingData.put(outputLabel, currentSamples);

    }

    public void assignTestSample(Double[] features)
    {
        //Convert the feature vector to Strings
        String[] stringData = new String[featureNames.keySet().size()];
        for (int i=0; i<featureNames.keySet().size(); i++) {
            stringData[i] = Double.toString(features[i]);
        }
        //Assign the data to the test condition (We are testing only one instance at a time)
        testData = stringData;
    }

    /**
     * Clears all of the data for the model
     */
    public void resetTrainingData() {
        // Create a blank list for each gesture
        trainingData = new LinkedHashMap<>();
        for (String s: outputClasses) {
            trainingData.put(s, new ArrayList<String[]>());
        }
    }

    /**
     * Returns the number of training samples for the given class index
     * @param index: the class index
     * @return the number of samples for the given class index
     */
    public int getNumTrainSamples(int index) {
        String className = outputClasses[index];
        return trainingData.get(className).size();
    }

    /**
     * Create an .arff file for the dataset
     * @param isTraining: whether the data is training or testing data
     */
    private void createDataFile(boolean isTraining, String filename) {
        PrintWriter writer;
        // Setup the file writer depending on whether it is training or testing data
        if (isTraining)
            writer = createPrintWriter(filename);
        else
            writer = createPrintWriter(filename);

        // Name the dataset
        writer.println("@relation gestures");
        writer.println("");

        // Define the features
        for (String s: featureNames.keySet()) {
            writer.println("@attribute "+s+" "+featureNames.get(s));
        }

        // Define the possible output classes
        String outputOptions = "@attribute gestureName {";
        for (String s: outputClasses) {
            outputOptions += s+", ";
        }
        outputOptions = outputOptions.substring(0, outputOptions.length()-2);
        outputOptions += "}";
        writer.println(outputOptions);
        writer.println("");

        // Write the data
        writer.println("@data");
        if (isTraining) {
            // Go through each category of possible outputs and save their samples
            for (String s: outputClasses) {
                ArrayList<String[]> gestureSamples = trainingData.get(s);
                for (String[] sampleData: gestureSamples) {
                    String sample = "";
                    for (String x: sampleData) {
                        sample += x+",";
                    }
                    sample += s;
                    writer.println(sample);
                }
            }
        }
        else {
            // Write the new sample with a blank label
            String sample = "";
            for (String x: testData) {
                sample += x+",";
            }
            sample += "?";
            writer.println(sample);
        }
        writer.close();
    }

    /**
     * Trains a model for the training data
     */
    public void train(boolean isAddTrainingSamples, String filename) {

        // Create the file for training
        if (isAddTrainingSamples)
            createDataFile(true, filename);

        // Read the file and specify the last index as the class
        Instances trainInstances = createInstances(filename);
        if (trainInstances == null) {
            return;
        }
        trainInstances.setClassIndex(trainInstances.numAttributes()-1);
        if(DEBUG_MODE)
            Toast.makeText(context,
                    "Number of total training samples: "+trainInstances.size(),
                    Toast.LENGTH_SHORT).show();

        // Define the classifier
        // TODO optional: try out different classifiers provided by Weka
        //model = new IBk(1);
//        model = new LMT();
        model = new LWL();
        try {
            model.buildClassifier(trainInstances);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * Returns the string label for the recently tested gesture
     * @return the string label
     */
    public String test(String filename) {
        // Create the file for testing
        createDataFile(false, filename);

        // Read the file and specify the last index as the class
        Instances testInstances = createInstances(filename);
        testInstances.setClassIndex(testInstances.numAttributes()-1);

        // Predict
        String classLabel = null;
        try {
            double classIndex = model.classifyInstance(testInstances.instance(0));
            classLabel = testInstances.classAttribute().value((int) classIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classLabel;
    }

    /**
     * Reads the .arff file and converts it into an Instances object
     * @param filename the filepath for the .arff file
     * @return a newly created Instances object
     */
    private Instances createInstances(String filename) {
        // Read the file
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        String fullFileName = SDFile.getAbsolutePath() + File.separator + filename;
        BufferedReader dataReader;
        try {
            FileReader fileReader = new FileReader(fullFileName);
            dataReader = new BufferedReader(fileReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        // Create the training instance
        Instances instances;
        try {
            instances = new Instances(dataReader);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    "Something is wrong with your .arff file!",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        return instances;
    }

    /**
     * Creates the file at the location
     * @param filename: the filename that appears at the root of external storage
     * @return writer: the PrintWriter object to be used
     */
    public PrintWriter createPrintWriter(String filename) {
        // Create the file
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        String fullFileName = SDFile.getAbsolutePath() + File.separator + filename;
        PrintWriter writer;
        try {
            writer = new PrintWriter(fullFileName);
        } catch(FileNotFoundException e) {
            return null;
        }
        return writer;
    }
}
