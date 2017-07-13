package com.yll.moonshadow.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.Window;

import com.yll.moonshadow.R;

public class SplashAty extends Activity {
    private boolean isStartMainAty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainAty();
            }
        }, 2000);
    }

    private void startMainAty(){

        if (!isStartMainAty) {
            isStartMainAty = true;
            Intent intent = new Intent(this, MainAty.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        startMainAty();
        return true;
    }
}
