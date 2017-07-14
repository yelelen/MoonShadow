package com.yll.moonshadow.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.yll.moonshadow.R;

/**
 * Created by yelelen on 7/14/2017.
 */

public class VideoPlayerAty extends Activity implements View.OnClickListener{
    private VideoView mVideoView;
    private ImageView mVoice;
    private SeekBar mVoiceSeekBar;
    private ImageView mSwitch;
    private TextView mPlayed;
    private SeekBar mVideoSeekBar;
    private TextView mDuration;
    private ImageView mReturn;
    private ImageView mBack;
    private ImageView mForward;
    private ImageView mPlay;
    private ImageView mFullscreen;
    private TextView mTitle;
    private ImageView mBattery;
    private TextView mSystemTime;

    private boolean isControlSee = true;

    private RelativeLayout mVideoPlayerControl;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.video_player);

        findViews();
        setListeners();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoView.start();
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(VideoPlayerAty.this, "视频播放失败", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });

        Uri uri = getIntent().getData();
        if (uri != null){
//            Log.d("URI", uri.toString());
            mVideoView.setVideoURI(uri);

        }
//设置系统自带的控制器
//        mVideoView.setMediaController(new MediaController(this));


        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (isControlSee){
                        mVideoPlayerControl.setVisibility(View.INVISIBLE);
                        isControlSee = false;
                    } else {
                        mVideoPlayerControl.setVisibility(View.VISIBLE);
                        isControlSee = true;
                    }
                }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        沉浸式模式
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void setListeners() {
        mVoice.setOnClickListener(this);
        mVoiceSeekBar.setOnClickListener(this);
        mSwitch.setOnClickListener(this);
        mPlayed.setOnClickListener(this);
        mVideoSeekBar.setOnClickListener(this);
        mReturn.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mPlay.setOnClickListener(this);
        mForward.setOnClickListener(this);
        mFullscreen.setOnClickListener(this);
        mTitle.setOnClickListener(this);
        mBattery.setOnClickListener(this);
        mSystemTime.setOnClickListener(this);

    }

    private void findViews(){
        mVoice = (ImageView)findViewById(R.id.voice_voice);
        mVoiceSeekBar = (SeekBar)findViewById(R.id.voice_progress);
        mSwitch = (ImageView)findViewById(R.id.voice_switch);
        mPlayed = (TextView)findViewById(R.id.video_played);
        mVideoSeekBar = (SeekBar)findViewById(R.id.video_progress);
        mReturn = (ImageView)findViewById(R.id.video_return);
        mBack = (ImageView)findViewById(R.id.video_back);
        mPlay = (ImageView)findViewById(R.id.video_play);
        mForward = (ImageView)findViewById(R.id.video_forward);
        mFullscreen = (ImageView)findViewById(R.id.video_fullscreen);
        mDuration = (TextView)findViewById(R.id.video_duration);
        mTitle = (TextView)findViewById(R.id.video_title);
        mBattery = (ImageView)findViewById(R.id.battery);
        mSystemTime = (TextView)findViewById(R.id.system_time);

        mVideoPlayerControl = (RelativeLayout)findViewById(R.id.video_player_control);

        mVideoView = (VideoView)findViewById(R.id.video_view);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.voice_voice:

                break;
            case R.id.voice_progress:

                break;
            case R.id.voice_switch:

                break;
            case R.id.video_played:

                break;
            case R.id.video_progress:

                break;
            case R.id.video_return:
                finish();

                break;
            case R.id.video_back:

                break;
             case R.id.video_play:
                 if (mVideoView.isPlaying()){
                     mVideoView.pause();
                     mPlay.setImageResource(R.mipmap.play);
                 } else {
                     mVideoView.start();
                     mPlay.setImageResource(R.mipmap.stop);
                 }


                break;
             case R.id.video_forward:

                break;
             case R.id.video_fullscreen:

                break;
            case R.id.video_title:

                break;
            case R.id.battery:

                break;
            case R.id.system_time:

                break;
             default:

                break;

        }
    }
}
