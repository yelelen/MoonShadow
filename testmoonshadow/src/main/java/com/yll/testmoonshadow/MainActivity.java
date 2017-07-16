package com.yll.testmoonshadow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void testMSFromInternet(View view) {
        Intent intent = new Intent();
        intent.setDataAndType(Uri.parse("http://10.0.2.101:80/king_island.mkv"), "video/*");
        startActivity(intent);
    }
}
