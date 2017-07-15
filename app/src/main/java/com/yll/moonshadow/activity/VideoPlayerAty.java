package com.yll.moonshadow.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.yll.moonshadow.R;
import com.yll.moonshadow.beans.VideoItem;
import com.yll.moonshadow.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by yelelen on 7/14/2017.
 */

public class VideoPlayerAty extends Activity implements View.OnClickListener{
    private static final int UPDATE_VIDEO_PROGRESS = 0;

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
    private ImageView mPrevious;
    private ImageView mNext;
    private TextView mTitle;
    private ImageView mBattery;
    private TextView mSystemTime;

    private boolean isControlSee = true;
    private boolean isFullscreen = true;

    private RelativeLayout.LayoutParams mVideoViewFirstParams = null;
    private int mLastPlayedDuration = 0;
    private SharedPreferences mSharedPreferences;
    private ArrayList<VideoItem> mVideoList;
    private int mSelectedVideo;


    private RelativeLayout mVideoPlayerControl;
    private PowerReceiver mPowerReceiver;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_VIDEO_PROGRESS:
                    int currentPos = mVideoView.getCurrentPosition();
                    mVideoSeekBar.setProgress(currentPos);
                    mPlayed.setText(Utils.formatDuration(currentPos));
                    mSystemTime.setText(getSystemTime());

                    mHandler.removeMessages(msg.what);
                    mHandler.sendEmptyMessageDelayed(UPDATE_VIDEO_PROGRESS, 1000);
                    break;
                default:
                    break;
            }
            return true;
        }
    });


    private String getSystemTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        return sdf.format(new Date());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.video_player);

        findViews();

        setListeners();
        registerPowerListener();

        Bundle bundle = getIntent().getBundleExtra("video_data");
        mVideoList = bundle.getParcelableArrayList("video_list");
        mSelectedVideo = bundle.getInt("position");
//设置系统自带的控制器
//        mVideoView.setMediaController(new MediaController(this));
        mVideoViewFirstParams = (RelativeLayout.LayoutParams) mVideoView.getLayoutParams();
        mVideoView.setVideoPath(mVideoList.get(mSelectedVideo).getPath());

    }

    private void registerPowerListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mPowerReceiver = new PowerReceiver();
        registerReceiver(mPowerReceiver, intentFilter);
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

    @Override
    protected void onPause() {
        super.onPause();
        mLastPlayedDuration = mVideoView.getCurrentPosition();
    }

    @Override
    protected void onDestroy() {
        if (mPowerReceiver != null){
            unregisterReceiver(mPowerReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        saveLastPlayedDuration();
        super.onBackPressed();
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
                saveLastPlayedDuration();
                finish();
                break;
            case R.id.video_previous:
                saveLastPlayedDuration();
                if (--mSelectedVideo < 0) mSelectedVideo = mVideoList.size() -1;
                mVideoView.setVideoPath(mVideoList.get(mSelectedVideo).getPath());
                break;
            case R.id.video_back:
                playBack(15);
                break;
            case R.id.video_play:
                play();
                break;
            case R.id.video_forward:
                playForward(15);
                break;
            case R.id.video_next:
                saveLastPlayedDuration();
                mSelectedVideo = (++mSelectedVideo) % mVideoList.size();
                mVideoView.setVideoPath(mVideoList.get(mSelectedVideo).getPath());
                break;
            case R.id.video_fullscreen:

//                 如果不保存上次的LayoutParams,而直接采取如下办法会有bug
//                 params = (!isFullscreen) ? RelativeLayout.LayoutParams.MATCH_PARENT : RelativeLayout.LayoutParams.WRAP_CONTENT;
//                 mVideoView.setLayoutParams(new RelativeLayout.LayoutParams(params, params));
//                 isFullscreen = !isFullscreen;

                fullscreen();
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

    private void readLastPlayedDuration() {
        mSharedPreferences = getSharedPreferences(mVideoList.get(mSelectedVideo).getName(), MODE_PRIVATE);
        mLastPlayedDuration = mSharedPreferences.getInt("player_duration", 0);
    }


    private void saveLastPlayedDuration() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("player_duration", mVideoView.getCurrentPosition());
        editor.commit();
    }


    private void fullscreen() {
        RelativeLayout.LayoutParams params;
        params = (!isFullscreen) ? mVideoViewFirstParams :
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mVideoView.setLayoutParams(params);
        isFullscreen = !isFullscreen;
    }

    private void play() {
        if (mVideoView.isPlaying()){
            mVideoView.pause();
            mPlay.setImageResource(R.mipmap.play);
        } else {
            mVideoView.start();
            mPlay.setImageResource(R.mipmap.stop);
        }
    }

    private void playForward(int forwardtime) {
        //                快进键，每次快进forwardtime, 单位为秒
        int pos;
        pos = mVideoView.getCurrentPosition() + forwardtime * 1000;
        pos = (pos <= mVideoView.getDuration())? pos : mVideoView.getDuration();
        mVideoSeekBar.setProgress(pos);
        mVideoView.seekTo(pos);
        mVideoView.start();

        mPlay.setImageResource(R.mipmap.stop);
    }

    private void playBack(int backtime) {
        int pos;//                后退键，每次后退backtime, 单位为秒
        pos = mVideoView.getCurrentPosition() - backtime * 1000;
        pos = (pos >= 0)? pos: 0;
        mVideoSeekBar.setProgress(pos);
        mVideoView.seekTo(pos);
        mVideoView.start();

        mPlay.setImageResource(R.mipmap.stop);
    }

    private void setBatteryLevel(int level){
        if (level <= 0)
            mBattery.setImageResource(R.mipmap.battery0);
        else if (level <= 30)
            mBattery.setImageResource(R.mipmap.battery1);
        else if (level <= 60)
            mBattery.setImageResource(R.mipmap.battery2);
        else if (level <= 100)
            mBattery.setImageResource(R.mipmap.battery3);
        else
            mBattery.setImageResource(R.mipmap.battery3);
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
        mPrevious.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mForward.setOnClickListener(this);
        mFullscreen.setOnClickListener(this);
        mTitle.setOnClickListener(this);
        mBattery.setOnClickListener(this);
        mSystemTime.setOnClickListener(this);

        mVideoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    mVideoSeekBar.setProgress(progress);
                    mVideoView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                int duration = mVideoView.getDuration();
                mVideoSeekBar.setMax(duration);

                mDuration.setText(Utils.formatDuration(duration));
                mPlayed.setText(Utils.formatDuration(0));

                mTitle.setText(mVideoList.get(mSelectedVideo).getName());
                mVideoView.setLayoutParams(mVideoViewFirstParams);

                readLastPlayedDuration();
                mVideoView.seekTo(mLastPlayedDuration);
                mVideoView.start();
                if (mLastPlayedDuration > 0) {
                    Toast.makeText(VideoPlayerAty.this, "从上次记录播放", Toast.LENGTH_SHORT).show();

                }

                mHandler.sendEmptyMessage(UPDATE_VIDEO_PROGRESS);
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
                mPlay.setImageResource(R.mipmap.play);
                mSelectedVideo = (++mSelectedVideo) % mVideoList.size();
                mVideoView.setVideoPath(mVideoList.get(mSelectedVideo).getPath());
            }
        });

        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                       mVideoPlayerControl.setVisibility((isControlSee ? View.INVISIBLE : View.VISIBLE));
                        isControlSee =  !isControlSee;
                        break;
                }
                return true;
            }
        });




    }

    private void findViews(){
        mVoice = (ImageView)findViewById(R.id.voice_voice);
        mVoiceSeekBar = (SeekBar)findViewById(R.id.voice_progress);
        mSwitch = (ImageView)findViewById(R.id.voice_switch);
        mPlayed = (TextView)findViewById(R.id.video_played);
        mVideoSeekBar = (SeekBar)findViewById(R.id.video_progress);
        mReturn = (ImageView)findViewById(R.id.video_return);
        mPrevious = (ImageView)findViewById(R.id.video_previous);
        mBack = (ImageView)findViewById(R.id.video_back);
        mPlay = (ImageView)findViewById(R.id.video_play);
        mForward = (ImageView)findViewById(R.id.video_forward);
        mNext = (ImageView)findViewById(R.id.video_next);
        mFullscreen = (ImageView)findViewById(R.id.video_fullscreen);
        mDuration = (TextView)findViewById(R.id.video_duration);
        mTitle = (TextView)findViewById(R.id.video_title);
        mBattery = (ImageView)findViewById(R.id.battery);
        mSystemTime = (TextView)findViewById(R.id.system_time);

        mVideoPlayerControl = (RelativeLayout)findViewById(R.id.video_player_control);

        mVideoView = (VideoView)findViewById(R.id.video_view);

    }

    class PowerReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0); // level 表示电量的级别 0~100
            setBatteryLevel(level);
        }
    }
}
