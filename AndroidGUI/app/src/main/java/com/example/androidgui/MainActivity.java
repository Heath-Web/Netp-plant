package com.example.androidgui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.google.gson.Gson;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private ImageButton bt_refresh; //button used to update environment data

    private String serverip = "192.168.101.36"; //server ip address
    private int serverport = 8000; // server port number

    private final int NORMAL = 1;
    private final int FAILED = -1;

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
        bt_refresh = findViewById(R.id.bt_refresh);
        //  initialize all global value
        MainApplication.getInstance().data.humidity = (float) 0;
        MainApplication.getInstance().data.moisture = (float) 0;
        MainApplication.getInstance().data.light = (float) 0;
        MainApplication.getInstance().data.temperature = (float) 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // start a new thread to request environment data
        new Thread(new Runnable(){
            @Override
            public void run() {
                //Request environment data
                udp_response res = sendRequest("get environment",serverip,serverport);
                switch (res.status) { // handle status code
                    case NORMAL: //
                        Log.d(TAG ,"udp status code: " + String.valueOf(NORMAL));
                        update_environmentData(res);
                        break;// update value on UI
                    case FAILED:
                        Log.d(TAG ,"udp status code: " + String.valueOf(FAILED));
                        break;
                    default:
                        Log.e(TAG ,"Receive other unpredictable status code ");
                        break;
                }
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
        bt_refresh.setOnClickListener(this);
    }

    private void update_environmentData(udp_response response){
        try{
            //  update global value
            MainApplication.getInstance().data.humidity = response.humidity;
            MainApplication.getInstance().data.temperature = response.temperature;
            MainApplication.getInstance().data.light = response.light;
            MainApplication.getInstance().data.moisture = response.moisture;
            //update components
            tv_humidity_value.setText(String.valueOf(MainApplication.getInstance().data.humidity));
            tv_temperature_value.setText(String.valueOf(MainApplication.getInstance().data.temperature));
            tv_moisture_value.setText(String.valueOf(MainApplication.getInstance().data.moisture));
            tv_light_value.setText(String.valueOf(MainApplication.getInstance().data.light));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private udp_response sendRequest(String request,String ip, int port){
        /*
         * This function is used to send a UDP request and receive response
         */
        String str_reply = null;
        udp_response response = new udp_response();
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
            Log.d(TAG,"//sendRequest " + "Request \"" + request + "\" send to " + ip + ":" + port);

            // Create a datagram to receive data from server
            byte[] data2 = new byte[1024];
            DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
            // receive the response from server
            socket.receive(packet2);
            // read data
            str_reply = new String(data2, 0, packet2.getLength());
            // Selecting valid characters form string by regex
            String pattern = "(\\{)(.*)(\\})";
            Pattern r = Pattern.compile(pattern); // create Pattern object
            Matcher m = r.matcher(str_reply); // create matcher object
            if (m.find( )) {
                str_reply = m.group(0);
            } else {
                Log.e(TAG," Invalid response (the response must be json format)");
            }
            Log.d(TAG,"//sendRequest " + "Receive response \"" + str_reply + "\" from " + ip + ":" + port);
            //close socket
            socket.close();

            //convert string to ude response object
            response = new Gson().fromJson(str_reply, udp_response.class);
        } catch (IOException e){
            e.printStackTrace();
            Log.e(TAG , "//sendRequest, There are something wrong when doing udp communication with server");
        }
        return response;
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
                        udp_response response = sendRequest("open pump",serverip,serverport);
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
                        udp_response response = sendRequest("close pump",serverip,serverport);
                    }
                }).start();

                // refresh quantity and duration

            }
        }
        else if (v.getId() == R.id.bt_refresh){
            new Thread(new Runnable(){
                @Override
                public void run() {
                    //Request environment data
                    udp_response res = sendRequest("get environment",serverip,serverport);
                    switch (res.status) {
                        case NORMAL:
                            update_environmentData(res);
                            break;// update value on UI
                        case FAILED:
                            break;
                    }
                }
            }).start();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // auto
        Log.d(TAG,"Switch button checked changed");
        if (isChecked){
            //open auto
            new  Thread(new Runnable() {
                @Override
                public void run() {
                    sendRequest("open auto",serverip,serverport);
                }
            }).start();
        }else{
            //close auto
            new  Thread(new Runnable() {
                @Override
                public void run() {
                    sendRequest("close auto",serverip,serverport);
                }
            }).start();
        }
    }

    class udp_response{
        public int status; // status code
        public Float humidity;
        public Float temperature;
        public Float moisture;
        public Float light;
    }
}