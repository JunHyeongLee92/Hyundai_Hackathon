package com.example.lyn.hyundai2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

public class Main3Activity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                finish();
            }
        };
        handler.sendEmptyMessageDelayed(0, 4000);
    }
}
