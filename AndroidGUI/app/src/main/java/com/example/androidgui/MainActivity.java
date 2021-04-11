package com.example.androidgui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final static String TAG = "MainActivity"; // TAG of this activity
    private TextView tv_humidity_value; // air humidity Textview
    private TextView tv_temperature_value; // Temperature Textview
    private TextView tv_moisture_value; // soil moisture Textview
    private TextView tv_light_value; // Light intensity Textview
    private TextView tv_quantity_value; // water quantity  Textview
    private TextView tv_duration_value; // water duration Textview
    private Button bt_water; // button used to open and close pump
    private Switch sw_auto_irrigation; // auto irrigation switch button

    private String serverip = "10.0.2.2"; //server ip address
    private int serverport = 8800; // server port number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // find all components here
        tv_humidity_value = findViewById(R.id.tv_humidity_value);
        tv_temperature_value = findViewById(R.id.tv_temperature_value);
        tv_moisture_value = findViewById(R.id.tv_moisture_value);
        tv_light_value = findViewById(R.id.tv_light_value);
        tv_quantity_value = findViewById(R.id.tv_quantity_value);
        tv_duration_value = findViewById(R.id.tv_duration_value);
        bt_water = findViewById(R.id.bt_water);
        sw_auto_irrigation = findViewById(R.id.sw_auto_irrigation);
        //  initialize all global value
        MainApplication.getInstance().data.humidity = (float) 0;
        MainApplication.getInstance().data.moisture = (float) 0;
        MainApplication.getInstance().data.light = (float) 0;
        MainApplication.getInstance().data.temperature = (float) 0;
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable(){
            @Override
            public void run() {
                //Request environment data
                String res = sendRequest("get environment",serverip,serverport);
                update_environmentData(res);
            }
        }).start();

        //  initialize all components here
        tv_humidity_value.setText(String.valueOf(MainApplication.getInstance().data.humidity));
        tv_temperature_value.setText(String.valueOf(MainApplication.getInstance().data.temperature));
        tv_moisture_value.setText(String.valueOf(MainApplication.getInstance().data.moisture));
        tv_light_value.setText(String.valueOf(MainApplication.getInstance().data.light));
        tv_quantity_value.setText("0 ml");
        tv_duration_value.setText("0 s");
        bt_water.setOnClickListener(this); // add  click listener
        sw_auto_irrigation.setOnCheckedChangeListener(this); // add checked changed listener
    }

    private void update_environmentData(String env_str){
        try{
            //  update global value
            MainApplication.getInstance().data = new Gson().fromJson(env_str,MainApplication.Data.class);
            //update components
            tv_humidity_value.setText(String.valueOf(MainApplication.getInstance().data.humidity));
            tv_temperature_value.setText(String.valueOf(MainApplication.getInstance().data.temperature));
            tv_moisture_value.setText(String.valueOf(MainApplication.getInstance().data.moisture));
            tv_light_value.setText(String.valueOf(MainApplication.getInstance().data.light));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String sendRequest(String request,String ip, int port){
        /*
         * This function is used to send a UDP request and receive response
         */
        String reply = null;
        try {

            // Define the address of the server
            InetAddress address = InetAddress.getByName(ip);
            byte[] data = request.getBytes();
            // Create a datagram that contains the data information to be sent
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            //Instantiate UDP socket
            DatagramSocket socket = new DatagramSocket();
            // send datagram to server
            socket.send(packet);
            Log.d(TAG,"--sendRequest " + "Request \"" + request + "\" send to " + ip + ":" + port);

            // Create a datagram to receive data from server
            byte[] data2 = new byte[1024];
            DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
            // receive the response from server
            socket.receive(packet2);
            // read data
            reply = new String(data2, 0, packet2.getLength());
            Log.d(TAG,"--sendRequest " + "Receive response \"" + reply + "\" from " + ip + ":" + port);
            //close socket
            socket.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        return reply;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_water){
            if (((Button)v).getText().equals("Water!")){
                ((Button)v).setText("Stop");
                Log.d(TAG,"Click on Water!");
                //open pump
                new  Thread(new Runnable() {
                    @Override
                    public void run() {
                        String res = sendRequest("open pump",serverip,serverport);

                    }
                }).start();

                // refresh quantity and duration

            }else if (((Button)v).getText().equals("Stop")){
                ((Button)v).setText("Water!");
                Log.d(TAG,"Click on Stop");
                //close pump
                //open pump
                new  Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendRequest("close pump",serverip,serverport);
                    }
                }).start();

                // refresh quantity and duration

            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // auto
        Log.d(TAG,"Switch button checked changed");
    }
}