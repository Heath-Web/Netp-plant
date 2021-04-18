package com.example.androidgui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

public class IPconfigActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "MainActivity"; // TAG of this activity
    private EditText et_IP_address;
    private Button bt_confirm;

    private String serverip; //server ip address

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipconfig);

        et_IP_address = findViewById(R.id.et_IP_ADDRESS);
        bt_confirm = findViewById(R.id.bt_confirm);

        serverip = String.valueOf(et_IP_address.getText());
        bt_confirm.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_confirm){
            serverip = String.valueOf(et_IP_address.getText());

            Intent intent = new Intent(this,MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("serverip",serverip);
            intent.putExtras(bundle);
            this.startActivity(intent);
        }
    }
}
