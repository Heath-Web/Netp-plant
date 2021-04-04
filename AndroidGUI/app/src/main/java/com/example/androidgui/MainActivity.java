package com.example.androidgui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final static String TAG = "MainActivity"; // TAG of this activity
    private TextView tv_humidity_value; // air humidity Textview
    private TextView tv_temperature_value; // Temperature Textview
    private TextView tv_moisture_value; // soil moisture Textview
    private TextView tv_light_value; // Light intensity Textview
    private TextView tv_quantity_value; // water quantity  Textview
    private TextView tv_duration_value; // water duration Textview
    private Button bt_water; // button used to open and close pump
    private Switch sw_auto_irrigation; // auto irrigation switch button

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadData();
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

    private void LoadData(){
        //recieve data from  Pi here through UDP


        //set value
        MainApplication.getInstance().data.humidity = Float.valueOf(10);
        MainApplication.getInstance().data.moisture = Float.valueOf(10);
        MainApplication.getInstance().data.light = Float.valueOf(10);
        MainApplication.getInstance().data.temperature = Float.valueOf(10);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_water){
            if (((Button)v).getText().equals("Water!")){
                ((Button)v).setText("Stop");
                //open pump

                // refresh quantity and duration

            }else if (((Button)v).getText().equals("Stop")){
                ((Button)v).setText("Water!");
                //close pump

                // refresh quantity and duration

            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // auto
    }
}
