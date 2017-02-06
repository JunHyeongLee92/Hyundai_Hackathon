package com.example.lyn.hyundai2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Lyn on 2016-08-23.
 */
public class AlarmActivity extends AppCompatActivity {
    String address = "";
    String tel = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AlertDialog dialog = createDialogBox();
        dialog.show();
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        tel = intent.getStringExtra("tel");
    }
    private AlertDialog createDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AlarmActivity.this);
        builder.setTitle("Warning");
        builder.setMessage("Your baby or animal still is in the car! ");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                startActivity(intent);
                intent.putExtra("address", address);
                intent.putExtra("tel", tel);
                finish();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.exit(0);
            }
        });
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
