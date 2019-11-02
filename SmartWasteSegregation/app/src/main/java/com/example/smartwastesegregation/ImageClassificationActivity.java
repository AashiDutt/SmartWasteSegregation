package com.example.smartwastesegregation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ImageClassificationActivity extends AppCompatActivity {

    // Motor Status -> "1" => ON, "0" => OFF
    private String motorStatus;
    // Log Text View
    private TextView textView;
    // Timestamp
    private Long tsLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_classification);

        // TEST
        final String serverAddress = MainActivity.serverAddress;
        final Context context = this;

        // Log Text View
        textView = findViewById(R.id.motorStatusTextView);

        // Timestamp
        tsLong = System.currentTimeMillis()/1000;

        // Motor ON/OFF Buttons
        Button motor1On, motor1Off, motor2On, motor2Off;

        // Motor Control Buttons
        motor1On = findViewById(R.id.motor1OnButton);
        motor1Off = findViewById(R.id.motor1OffButton);
        motor2On = findViewById(R.id.motor2OnButton);
        motor2Off = findViewById(R.id.motor2OffButton);

        motor1On.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                motorStatus = "1/1";
                // Server address
                HttpRequestTask requestTask = new HttpRequestTask(serverAddress, context);
                requestTask.execute(motorStatus);
                textView.setText(tsLong.toString() + ":  " + "Turning Motor 1 ON" + "\n");
            }
        });

        motor1Off.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                motorStatus = "1/0";
                // Server address
                HttpRequestTask requestTask = new HttpRequestTask(serverAddress, context);
                requestTask.execute(motorStatus);
                textView.setText(tsLong.toString() + ":  " + "Turning Motor 1 OFF" + "\n");
            }
        });

        motor2On.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                motorStatus = "2/1";
                // Server address
                HttpRequestTask requestTask = new HttpRequestTask(serverAddress, context);
                requestTask.execute(motorStatus);
                textView.setText(tsLong.toString() + ":  " + "Turning Motor 2 ON" + "\n");
            }
        });

        motor2Off.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                motorStatus = "2/0";
                // Server address
                HttpRequestTask requestTask = new HttpRequestTask(serverAddress, context);
                requestTask.execute(motorStatus);
                textView.setText(tsLong.toString() + ":  " + "Turning Motor 2 OFF" + "\n");
            }
        });
    }
}
