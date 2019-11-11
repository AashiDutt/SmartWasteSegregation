package com.example.smartwastesegregation;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpRequestTask extends AsyncTask<String, Void, String> {

    private String serverAddress;
    private String serverResponse = "";
    private AlertDialog dialog;

    // Request for Data from HTTP Request
    public HttpRequestTask(String serverAddress, Context context){
        // Set server address
        this.serverAddress = serverAddress;
        // Set Alert Dialog
        dialog = new AlertDialog.Builder(context)
                .setTitle("HTTP Response from IP Address:")
                .setCancelable(true)
                .create();
    }

    @Override
    protected String doInBackground(String... params) {
        // Send Data
        dialog.setMessage("Data sent , waiting response from server...");

        // Show Dialog
        if (!dialog.isShowing())
            dialog.show();

        String val = params[0];
        // URL Address
        final String url = "http://" + serverAddress + "/motor/" + val;

        try{
            HttpClient client = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet();
            getRequest.setURI(new URI(url));
            HttpResponse response = client.execute(getRequest);

            InputStream inputStream = null;
            inputStream = response.getEntity().getContent();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            serverResponse = bufferedReader.readLine();
            inputStream.close();
        }
        catch (URISyntaxException e){
            e.printStackTrace();
            serverResponse = e.getMessage();
        }
        catch (ClientProtocolException e){
            e.printStackTrace();
            serverResponse = e.getMessage();
        }
        catch (IOException e){
            e.printStackTrace();
            serverResponse = e.getMessage();
        }
        return serverResponse;
    }

    @Override
    protected void onPostExecute(String s) {
        dialog.setMessage(serverResponse);
        if (!dialog.isShowing())
            dialog.show();
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Sending Data to Server, please wait....");
        if (!dialog.isShowing())
            dialog.show();
    }
}
