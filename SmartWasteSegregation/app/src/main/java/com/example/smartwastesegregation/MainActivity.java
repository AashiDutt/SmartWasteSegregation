package com.example.smartwastesegregation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static String serverAddress;
    // IP Address Text
    private EditText ipAddress;
    // Log Text View
    private TextView logText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect Button
        Button connectButton;
        //  Connect Button
        connectButton = findViewById(R.id.buttonMainActivity);

        // WiFi Accessory IP Address
        ipAddress = findViewById(R.id.ipAddressText);
        // Log Text View
        logText = findViewById(R.id.logTextView);

        // On Clicking the button, go to next view
        connectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // Check if IP Address is Empty, return error
                if (ipAddress.getText().toString().isEmpty()){
                    // Print Error
                    logText.setText(R.string.logEmptyIPError);
                    return;
                }
                // Server address
                serverAddress = ipAddress.getText().toString();
                logText.setText(serverAddress);
                moveToImageClassifierActivity();
            }
        });
    }

    // Function to change view to new activity view
    private void moveToImageClassifierActivity(){
        // Define the Intent to move to new activity view
        Intent intent = new Intent(MainActivity.this, ImageClassificationActivity.class);
        // Execute the Intent
        startActivity(intent);
    }
}
