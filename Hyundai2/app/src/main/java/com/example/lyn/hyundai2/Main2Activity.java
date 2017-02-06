package com.example.lyn.hyundai2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {

    Handler handler = new Handler();
    Button btn;
    TextView txt;
    ArrayList<String> address_list = new ArrayList<String>();
    ArrayList<String> tel_list = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Intent intent = getIntent();
        String address = intent.getStringExtra("address");
        String tel = intent.getStringExtra("tel");
        token(address, tel);
        printToken();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ImageView iv = (ImageView)findViewById(R.id.imgbtn);
                    URL url = new URL("http://52.78.88.51:8080/SaveUsServer/resources/common/img/detect_img.png");
                    InputStream is = url.openStream();
                    final Bitmap bm = BitmapFactory.decodeStream(is);
                    handler.post(new Runnable(){

                        public void run() {
                            iv.setImageBitmap(bm);
                        }
                    });

                    iv.setImageBitmap(bm);
                } catch (Exception e) {

                }

            }
        });
        t.start();

        ListView listview ;
        ListViewAdapter adapter;

        // Adapter 생성
        adapter = new ListViewAdapter() ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.listview1);
        listview.setAdapter(adapter);
        for (int i = 0 ; i < address_list.size(); i++)  {
            adapter.addItem(ContextCompat.getDrawable(this, R.drawable.call),
                    ""+address_list.get(i), tel_list.get(i)) ;
        }


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                ListViewItem item = (ListViewItem) parent.getItemAtPosition(position) ;

                String titleStr = item.getTitle() ;
                String descStr = item.getDesc() ;
                Drawable iconDrawable = item.getIcon() ;
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+descStr));
                startActivity(intent);

                // TODO : use item data.
            }
        }) ;
    }


    private void token(String address , String tel) {
        String address_str[] = address.split("/");
        String tel_str[] = tel.split("/");
        for (int i = 0; i < address_str.length; i++) {
            address_list.add(address_str[i]);
            tel_list.add(tel_str[i]);
        }


    }
    private void printToken() {
        for (int i = 0 ; i < address_list.size(); i++) {
            Log.d("saveus", "print : " + address_list.get(i) + " / tel : " + tel_list.get(i));
        }
    }

}
