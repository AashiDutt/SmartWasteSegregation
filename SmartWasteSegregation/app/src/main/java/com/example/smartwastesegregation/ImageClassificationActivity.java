package com.example.smartwastesegregation;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class ImageClassificationActivity extends AppCompatActivity {

    // ImageView
    private ImageView imageView;
    // Waste TextView
    private TextView wasteSuperClass;
    // Waste SubClass TextView
    private TextView wasteSubClass;
    // Motor-1 TextView
    private TextView motor1ControlLabel;
    // Motor-2 TextView
    private TextView motor2ControlLabel;
    // Uri for the captured image
    private Uri photoURI;
    // Server Address
    private String serverAddress = MainActivity.serverAddress;

    // Permission requests
    public static final int REQUEST_PERMISSION = 300;

    // Input Image dimensions for the Inception Model
    private int IMAGE_WIDTH = 299;
    private int IMAGE_HEIGHT = 299;
    private int IMAGE_CHANNELS = 3;

    // Create Mapping from Outputs to Actual Labels
    Map<String, List<String>> wasteTypeLabelMap = new HashMap<>();

    // -------------- TFLite Variables ----------------
    // Presets for RGB conversion
    private static final int RESULTS_TO_SHOW = 3;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    // TFLite model interpreter Options
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();
    // TFLite graph
    private Interpreter tflite;
    // List to hold all the possible labels for model
    private List<String> labelList;
    // holds the selected image data as bytes
    private ByteBuffer imgData = null;
    // holds the probabilities of each label for non-quantized graphs
    private float[][] labelProbArray = null;
    // holds the probabilities of each label for quantized graphs
    private byte[][] labelProbArrayB = null;
    // array that holds the labels with the highest probabilities
    private String[] topLables = null;
    // array that holds the highest probabilities
    private String[] topConfidence = null;
    // Int array to hold image data
    private int[] intValues;
    // Is quantized model supported ?
    private boolean quant = false;
    // ------------------------------------------------

    // priority queue that will hold the top results from the CNN
    private PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // initialize array that holds image data
        intValues = new int[IMAGE_HEIGHT * IMAGE_WIDTH];

        super.onCreate(savedInstanceState);

        // ----------------- TFLIte Code ------------------
        // Initialize graph and labels
        try{
            tflite = new Interpreter(loadModelFile(), tfliteOptions);
            labelList = loadLabelList();
        } catch (Exception ex){
            ex.printStackTrace();
        }

        // Initialize byte array. The size depends if the input data needs to be quantized or not
        if(quant){
            imgData = ByteBuffer.allocateDirect(IMAGE_HEIGHT * IMAGE_WIDTH * IMAGE_CHANNELS);
        } else {
            imgData = ByteBuffer.allocateDirect(4 * IMAGE_HEIGHT * IMAGE_WIDTH * IMAGE_CHANNELS);
        }
        imgData.order(ByteOrder.nativeOrder());

        // initialize probabilities array. The datatypes that array holds depends if the input data needs to be quantized or not
        if(quant){
            labelProbArrayB= new byte[1][labelList.size()];
        } else {
            //labelProbArray = new float[1][labelList.size()];
            labelProbArray = new float[1][8];
        }
        // --------------------------------------------------

        setContentView(R.layout.activity_image_classification);

        // Image View
        imageView = findViewById(R.id.wasteImageView);
        // Image capture Button
        Button button = findViewById(R.id.captureImageButton);
        // Waste Superclass TextView
        wasteSuperClass = findViewById(R.id.wasteTextView);
        // Waste Subclass TextView
        wasteSubClass = findViewById(R.id.wasteSubclassTextView);
        // Motor 1 Control TextView
        motor1ControlLabel = findViewById(R.id.motor1TextView);
        motor1ControlLabel.setText("OFF");
        // Motor 2 Control TextView
        motor2ControlLabel = findViewById(R.id.motor2TextView);
        motor2ControlLabel.setText("OFF");

        // request permission to use the camera on the user's phone
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }

        // request permission to write data (aka images) to the user's external storage of their phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }

        // request permission to read data (aka images) from the user's external storage of their phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }

        // initialize array to hold top labels
        topLables = new String[RESULTS_TO_SHOW];
        // initialize array to hold top probabilities
        topConfidence = new String[RESULTS_TO_SHOW];

        // Populate the List with Values
        List<String> biodegradableLabels = new ArrayList<>();
        biodegradableLabels.add("food waste");
        biodegradableLabels.add("leaf waste");
        biodegradableLabels.add("paper waste");
        biodegradableLabels.add("wood waste");

        // Populate the List with Values
        List<String> nonBiodegradableLabels = new ArrayList<>();
        nonBiodegradableLabels.add("metal cans");
        nonBiodegradableLabels.add("plastic bottles");
        nonBiodegradableLabels.add("ewaste");
        nonBiodegradableLabels.add("plastic bags");

        // Create final Dictionary with Label Mappings
        wasteTypeLabelMap.put("Biodegradable", biodegradableLabels);
        wasteTypeLabelMap.put("Non-Biodegradable", nonBiodegradableLabels);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchPictureCaptureAction();
            }
        });
    }

    // Function to define Image capture intent and save image
    private void dispatchPictureCaptureAction(){
        // Define a set of values for the ContentResolver
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");

        // URI for the Photo
        photoURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Setup image capture intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        // Set camera screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Start image capture activity
        startActivityForResult(intent, 100);
    }

    // When the image capture is complete, show the image.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Original Bitmap
        Bitmap bitmap = null;

        // Check of result code is OK
        if (resultCode == RESULT_OK) {
            // If request code matches our request code
            if (requestCode == 100) {
                // Decode the file at filePath
                try {
                    // If image is shown as rotated, correct is using this function
                    RotateBitmap rotateBitmap = new RotateBitmap();
                    try {
                        // Original Image Bitmap
                        bitmap = rotateBitmap.HandleSamplingAndRotationBitmap(this, photoURI);
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.e("Error", e.toString());
                    }

                    try {
                        // resize the bitmap to the required input size to the CNN
                        Bitmap scaledBitmap = getResizedBitmap(bitmap, IMAGE_HEIGHT, IMAGE_WIDTH);
                        // convert bitmap to byte array
                        convertBitmapToByteBuffer(scaledBitmap);
                        // pass byte data to the graph
                        if(quant){
                            tflite.run(imgData, labelProbArrayB);
                        } else {
                            tflite.run(imgData, labelProbArray);
                        }
                        // Print Outputs
                        printTopKLabels();
                    } catch (Error e){
                        Log.e("Error", e.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // checks that the user has allowed all the required permission of read and write and camera. If not, notify the user and close the application
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(),"This application needs read, write, and camera permissions to run. Application now closing.",Toast.LENGTH_LONG);
                System.exit(0);
            }
        }
    }

    // Function to Load TFLite model graph from file
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor;
        if (quant){
            fileDescriptor = getAssets().openFd("optimized_model_quantized.tflite");
        }
        else {
            fileDescriptor = getAssets().openFd("optimized_model.tflite");
        }
        //AssetFileDescriptor fileDescriptor = getAssets().openFd("optimized_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // converts bitmap to byte array which is passed in the tflite graph
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // loop through all pixels
        int pixel = 0;
        for (int i = 0; i < IMAGE_HEIGHT; ++i) {
            for (int j = 0; j < IMAGE_WIDTH; ++j) {
                final int val = intValues[pixel++];
                // get rgb values from intValues where each int holds the rgb values for a pixel.
                // if quantized, convert each rgb value to a byte, otherwise to a float
                if(quant){
                    imgData.put((byte) ((val >> 16) & 0xFF));
                    imgData.put((byte) ((val >> 8) & 0xFF));
                    imgData.put((byte) (val & 0xFF));
                } else {
                    imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                }

            }
        }
    }

    // loads the labels from the label txt file in assets into a string array
    private List<String> loadLabelList() throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getAssets().open("retrained_labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    // resizes bitmap to given dimensions
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    // print the top labels and respective confidences
    private void printTopKLabels() {
        // add all results to priority queue
        for (int i = 0; i < labelList.size(); ++i) {
            //for (int i = 0; i < 8; ++i) {
            if(quant){
                sortedLabels.add(
                        new AbstractMap.SimpleEntry<>(labelList.get(i), (labelProbArrayB[0][i] & 0xff) / 255.0f));
            } else {
                sortedLabels.add(
                        new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
            }
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }

        // get top results from priority queue
        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            topLables[i] = label.getKey();
            topConfidence[i] = String.format("%.0f%%",label.getValue()*100);
        }

        // set the corresponding textviews with the results
        String debugMsg = topLables[2] + ", " + topConfidence[2];
        wasteSubClass.setText(debugMsg);
        //Log.i("Output:", debugMsg);

        String value = topLables[2];

        for (Map.Entry<String, List<String>> entry: wasteTypeLabelMap.entrySet()){
            System.out.println(entry.getValue().toString());
            if (entry.getValue().contains(value)){
                wasteSuperClass.setText(entry.getKey());
                controlMotor(entry.getKey());
            }
        }

        // DEBUG
//        label1.setText("1. "+topLables[2]);
//        label2.setText("2. "+topLables[1]);
//        label3.setText("3. "+topLables[0]);
//        Confidence1.setText(topConfidence[2]);
//        Confidence2.setText(topConfidence[1]);
//        Confidence3.setText(topConfidence[0]);
    }

    // Function to define which motor to control
    private void controlMotor(String wasteClass){
        String motorControl;

        if (wasteClass.equals("Biodegradable")){
            motorControl = "1/1";
            motor1ControlLabel.setText("ON");
            motor2ControlLabel.setText("OFF");
            sendMotorControls(motorControl);
        }
        else if (wasteClass.equals("Non-Biodegradable")){
            motorControl = "2/1";
            motor2ControlLabel.setText("ON");
            motor1ControlLabel.setText("OFF");
            sendMotorControls(motorControl);
        }
    }

    // Function to send motor controls to ESP8266
    private void sendMotorControls(String motorControlStatus){
        HttpRequestTask requestTask = new HttpRequestTask(serverAddress, this);
        requestTask.execute(motorControlStatus);
        //Log.i("Motor-Status:", "Sending: " + motorControlStatus);
    }
}
