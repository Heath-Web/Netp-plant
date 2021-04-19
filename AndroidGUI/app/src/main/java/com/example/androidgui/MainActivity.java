package com.example.androidgui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {
    private final static String TAG = "MainActivity"; // TAG of this activity
    private TextView tv_humidity_value; // air humidity Textview
    private TextView tv_temperature_value; // Temperature Textview
    private TextView tv_moisture_value; // soil moisture Textview
    private TextView tv_light_value; // Light intensity Textview
    private TextView tv_quantity_value; // water quantity  Textview
    private TextView tv_duration_value; // water duration Textview
    private Button bt_water; // button used to open and close pump
    private ImageButton bt_refresh; //button used to update environment data

    private String serverip ; //server ip address
    private int serverport = 8000; // server port number

    private final int SUCCEED = 1; // return status code 1 success
    private final int FAILED = -1; // return status code -1 failed
    private final int UDPFAILED = 0; // something wrong when doing udp communication or the response does not in json format
    private final int SENSOROFFLINE = 2;

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
        //sw_auto_irrigation = findViewById(R.id.sw_auto_irrigation);
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
        // start a new thread to acquire environment data
        new Thread(new Runnable(){
            @Override
            public void run() {
                //send Request to get environment data and receive response
                final udp_response res = sendRequest("get environment",serverip,serverport);
                switch (res.status) { // handle status code
                    case SUCCEED: // success
                        Log.d(TAG ,"udp status code: " + String.valueOf(SUCCEED));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update_environmentData(res); // update android components (refresh UI)
                            }
                        });
                        break;// update value on UI
                    case FAILED: // failed told by server
                        Log.d(TAG ,"udp status code: " + String.valueOf(FAILED));
                        // give feedback if failed
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"Get Environment data failed",Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case UDPFAILED: // udp communication failed
                        Log.d(TAG ,"udp status code: " + String.valueOf(UDPFAILED));
                        // give feedback if failed
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"UDP Communication wrong",Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case SENSOROFFLINE:// sensor dose not online
                        Log.d(TAG ,"udp status code: " + String.valueOf(UDPFAILED));
                        // give feedback if failed
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"Sensor offline Please Check your sensor!",Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    default: // unexpected status code
                        Log.e(TAG ,"Receive other unpredictable status code ");
                        // give feedback if failed
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"unexpected status code",Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                }
            }
        }).start();

        // get server  ip address
        Intent intent = getIntent();
        serverip = intent.getExtras().getString("serverip");

        //  initialize all components here
        tv_humidity_value.setText(String.valueOf(MainApplication.getInstance().data.humidity));
        tv_temperature_value.setText(String.valueOf(MainApplication.getInstance().data.temperature));
        tv_moisture_value.setText(String.valueOf(MainApplication.getInstance().data.moisture));
        tv_light_value.setText(String.valueOf(MainApplication.getInstance().data.light));
        tv_quantity_value.setText("0 ml");
        tv_duration_value.setText("0 s");
        bt_water.setOnClickListener(this); // add  click listener
        bt_refresh.setOnClickListener(this); // add  on click listener
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void update_environmentData(udp_response response){
        /*
         * This function refresh the value of humidity, temperature, moisture and light in the user
         * interface, according to the response. The input response is the udp response received
         * from server.
         */
        try{
            //  update global value
            MainApplication.getInstance().data.humidity = response.humidity;
            MainApplication.getInstance().data.temperature = response.temperature;
            MainApplication.getInstance().data.light = response.light;
            MainApplication.getInstance().data.moisture = response.moisture;
            //refresh corresponding android components and keep two decimals
            tv_humidity_value.setText(String.format("%.2f",MainApplication.getInstance().data.humidity/(float)100).toString());
            tv_temperature_value.setText(String.format("%.2f",MainApplication.getInstance().data.temperature/(float)100).toString());
            tv_moisture_value.setText(String.format("%.2f",MainApplication.getInstance().data.moisture/(float)100).toString());
            tv_light_value.setText(String.format("%.2f",MainApplication.getInstance().data.light/(float)100).toString());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private udp_response sendRequest(String request,String ip, int port){
        /*
         * This function is used to send a UDP request and receive response
         * Input request is the command including 'get environment', 'open pump', 'close pump'
         * Input ip is the server ip address
         * Input port is the port number of the server
         * this function will return a udp_response object
         */
        String str_response = null; //response string format
        udp_response response = new udp_response(); // create a udp_response object
        response.status = 0; // initialize status code
        try {
            // send request to server
            Log.d(TAG,"//sendRequest " + "Aim server: " + ip + ":" + port);
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

            // receive response from server
            // Create a datagram to receive data from server
            byte[] data2 = new byte[1024];
            DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
            // receive the response from server
            socket.receive(packet2);
            // read data
            str_response = new String(data2, 0, packet2.getLength());
            // Selecting valid characters form string
            str_response = str_response.split(";")[0];
            Log.d(TAG,"//sendRequest " + "Receive response \"" + str_response + "\" from " + ip + ":" + port);
            //close socket
            socket.close();

            //convert string to udp response object
            response = new Gson().fromJson(str_response, udp_response.class);
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
                Log.d(TAG,"Click on Water!");
                //open pump
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //send Request to open pump and receive response
                        udp_response response = sendRequest("open pump",serverip,serverport);
                        switch (response.status){
                            case SUCCEED: // success
                                Log.d(TAG ,"udp status code: " + String.valueOf(SUCCEED));
                                // counting time and quantity
                                final long baseTimer = SystemClock.elapsedRealtime(); // Now time
                                final Timer timer = new Timer("Timer");
                                timer.scheduleAtFixedRate(new TimerTask() {
                                    @Override
                                    public void run() {
                                        // calculate time duration in seconds
                                        int time = (int)((SystemClock.elapsedRealtime() - baseTimer) / 1000);
                                        String mm = new DecimalFormat("00").format(time % 3600 / 60);
                                        String ss = new DecimalFormat("00").format(time % 60);
                                        String timeFormat = new String( mm + ":" + ss);
                                        // Create Message and send to UI Thread. Message include timer object and time duration
                                        Message msg = new Message();
                                        msg.obj = timer;
                                        Bundle bundle = new Bundle();
                                        bundle.putString("timeFormat", timeFormat);
                                        bundle.putInt("time",time);
                                        bundle.putBoolean("stop",false);
                                        msg.setData(bundle);
                                        Timehandler.sendMessage(msg);
                                    }
                                }, 0, 1000L);
                                // change text on button and give feedback
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"Open pump successfully", Toast.LENGTH_LONG).show();
                                        bt_water.setText("Stop");
                                    }
                                });
                                break;
                            case FAILED: // failed told by server
                                Log.d(TAG ,"udp status code: " + String.valueOf(FAILED));
                                // give feedback if failed
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"Open Pump failed",Toast.LENGTH_LONG).show();
                                    }
                                });
                                break;
                            case UDPFAILED: // udp communication failed
                                Log.d(TAG ,"udp status code: " + String.valueOf(UDPFAILED));
                                // give feedback if failed
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"UDP Communication wrong",Toast.LENGTH_LONG).show();
                                    }
                                });
                                break;
                            case SENSOROFFLINE:// sensor dose not online
                                Log.d(TAG ,"udp status code: " + String.valueOf(UDPFAILED));
                                // give feedback if failed
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"Sensor offline Please Check your sensor!",Toast.LENGTH_LONG).show();
                                    }
                                });
                                break;
                            default: // unexpected status code
                                Log.e(TAG ,"Receive other unpredictable status code ");
                                // give feedback if failed
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"unexpected status code",Toast.LENGTH_LONG).show();
                                    }
                                });
                                break;
                        }
                    }
                }).start();
            }else if (((Button)v).getText().equals("Stop")){
                Log.d(TAG,"Click on Stop");
                //close pump
                new  Thread(new Runnable() {
                    @Override
                    public void run() {
                        //send Request to close pump and receive response
                        udp_response response = sendRequest("close pump",serverip,serverport);
                        switch (response.status) {
                            case SUCCEED: // success
                                Log.d(TAG, "udp status code: " + String.valueOf(SUCCEED));
                                // Stop counting quantity and time duration
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("stop", true);
                                Message msg = new Message(); // Create message with true flag "stop"
                                msg.setData(bundle);
                                Timehandler.sendMessage(msg);
                                // change text on button and give feed back
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        bt_water.setText("Water!");
                                        Toast.makeText(MainActivity.this, "Close pump successfully", Toast.LENGTH_LONG).show();
                                    }
                                });
                                break;
                            case FAILED: // failed told by server
                                Log.d(TAG, "udp status code: " + String.valueOf(FAILED));
                                // give feedback if failed
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Close Pump failed", Toast.LENGTH_LONG).show();
                                    }
                                });
                                break;
                            case SENSOROFFLINE:// sensor dose not online
                                Log.d(TAG ,"udp status code: " + String.valueOf(UDPFAILED));
                                // give feedback if failed
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"Sensor offline Please Check your sensor!",Toast.LENGTH_LONG).show();
                                    }
                                });
                                break;
                            case UDPFAILED: // udp communication failed
                                Log.d(TAG, "udp status code: " + String.valueOf(UDPFAILED));
                                // give feedback if failed
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "UDP Communication wrong", Toast.LENGTH_LONG).show();
                                    }
                                });
                                break;
                            default: // unexpected status code
                                Log.e(TAG, "Receive other unpredictable status code ");
                                // give feedback if failed
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "unexpected status code", Toast.LENGTH_LONG).show();
                                    }
                                });
                                break;
                        }
                    }
                }).start();
            }
        }
        else if (v.getId() == R.id.bt_refresh){
            // click on the refresh button
            new Thread(new Runnable(){
                @Override
                public void run() {
                    //send Request to get environment data and receive response
                    final udp_response res = sendRequest("get environment",serverip,serverport);
                    switch (res.status) { // handle status code
                        case SUCCEED: // success
                            Log.d(TAG ,"udp status code: " + String.valueOf(SUCCEED));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    update_environmentData(res); // update android components (refresh UI)
                                }
                            });
                            break;
                        case FAILED: // failed told by server
                            Log.d(TAG ,"udp status code: " + String.valueOf(FAILED));
                            // give feedback if failed
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"Get Environment data failed",Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case SENSOROFFLINE:// sensor dose not online
                            Log.d(TAG ,"udp status code: " + String.valueOf(UDPFAILED));
                            // give feedback if failed
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"Sensor offline Please Check your sensor!",Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case UDPFAILED: // udp communication failed
                            Log.d(TAG ,"udp status code: " + String.valueOf(UDPFAILED));
                            // give feedback if failed
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"UDP Communication wrong",Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        default: // unexpected status code
                            Log.e(TAG ,"Receive other unpredictable status code ");
                            // give feedback if failed
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"unexpected status code",Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                    }
                }
            }).start();
        }
    }

    // UDP response class
    class udp_response{
        public int status; // status code
        public Float humidity; // humidity
        public Float temperature; // temperature
        public Float moisture; // moisture
        public Float light; // light intensity
    }

    // Handler to handle message from timer
    private Timer timer; // timer
    @SuppressLint("HandlerLeak")
    final Handler Timehandler = new Handler(){
        @SuppressLint("SetTextI18n")
        public void handleMessage(android.os.Message msg) {
            if (msg.getData().getBoolean("stop")){
                timer.cancel(); // stop timer
            } else{
                timer = (Timer)msg.obj; // store timer
                if (null != tv_duration_value && null != tv_quantity_value) {
                    float speed = (float) 0.27; // speed of pump 100L per hour equals to 0.27 dl per seconds approximately
                    tv_duration_value.setText((String) msg.getData().getString("timeFormat"));
                    if (msg.getData().getInt("time") > 1 ){
                        tv_quantity_value.setText(String.valueOf(((Integer) msg.getData().getInt("time")-1) * speed ) + "dl");
                    }
                }
            }
        }
    };
}
