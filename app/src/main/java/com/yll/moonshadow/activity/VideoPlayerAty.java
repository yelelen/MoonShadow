package com.yll.moonshadow.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yll.moonshadow.R;
import com.yll.moonshadow.beans.VideoItem;
import com.yll.moonshadow.utils.Utils;
import com.yll.moonshadow.view.VideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by yelelen on 7/14/2017.
 */

public class VideoPlayerAty extends Activity implements View.OnClickListener{
    private static final int UPDATE_VIDEO_PROGRESS = 0;
    private static final int HIDE_VIDEO_PLAYER_CONTROL = 1;

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

    private RelativeLayout mVideoPlayerControl;
    private boolean isControlSee;   // 视频控制器是否可见
    private boolean isFullscreen = true;    // 是否处于全屏状态

    private RelativeLayout.LayoutParams mVideoViewFirstParams = null;
    private int mLastPlayedDuration = 0;        // 记录上次已经播放该视频的时长
    private SharedPreferences mSharedPreferences;

    private ArrayList<VideoItem> mVideoList;    //    传进来的视频列表
    private int mSelectedVideo;     //  用户在视频列表中点击的视频位置

    private PowerReceiver mPowerReceiver;   // 监听电源变化的广播的接收器

    private GestureDetector mGestureDetector; // 手势识别器

    private AudioManager mAudioManager; // 声音服务
    private int mVolume; // 当前的音量
    boolean isVoiceAdd = true;

    // 屏幕宽和高
    private int mScreenWidth;
    private int mScreenHeight;
//    视频大小
    private int mVideoWidth;
    private int mVideoHeight;

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
                case HIDE_VIDEO_PLAYER_CONTROL:
                    mVideoPlayerControl.setVisibility(View.INVISIBLE);
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

        initData();

    }

    private void initData() {
        Bundle bundle = getIntent().getBundleExtra("video_data");
        mVideoList = bundle.getParcelableArrayList("video_list");
        mSelectedVideo = bundle.getInt("position");
//设置系统自带的控制器
//        mVideoView.setMediaController(new MediaController(this));
        mVideoViewFirstParams = (RelativeLayout.LayoutParams) mVideoView.getLayoutParams();
        mVideoView.setVideoPath(mVideoList.get(mSelectedVideo).getPath());


        // 获得屏幕的宽和高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVoiceSeekBar.setMax(maxVolume);
        setVoiceIconAndSeekBar();

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                setFullscreenAndDefault();
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
//                mVideoPlayerControl.setVisibility((isControlSee ? View.INVISIBLE : View.VISIBLE));
//                isControlSee =  !isControlSee;
//                if (isControlSee) {
//                    mHandler.sendEmptyMessageDelayed(HIDE_VIDEO_PLAYER_CONTROL, 5000);
//                }

                mVideoPlayerControl.setVisibility((isControlSee ? View.INVISIBLE : View.VISIBLE));
                isControlSee = !isControlSee;
                if (isControlSee) {
                    mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    setVoiceIconAndSeekBar();
                    mHandler.sendEmptyMessageDelayed(HIDE_VIDEO_PLAYER_CONTROL, 5000);
                } else {
                    mHandler.removeMessages(HIDE_VIDEO_PLAYER_CONTROL);
                }
                return super.onSingleTapConfirmed(e);
            }
        });
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
        mHandler.removeMessages(HIDE_VIDEO_PLAYER_CONTROL);

        switch (v.getId()){
            case R.id.voice_voice:
                if (mVolume < 15 && isVoiceAdd) {
                    mVolume += 5;
                    if (mVolume >= 15) {
                        mVolume = 15;
                        isVoiceAdd = false;
                    }
                } else if (mVolume < 15 && !isVoiceAdd) {
                    mVolume -= 5;
                    if (mVolume <= 0) {
                        mVolume = 0;
                        isVoiceAdd = true;
                    }
                } else if (mVolume == 15) {
                    mVolume -= 5;
                    isVoiceAdd = false;
                }

                setVoiceIconAndSeekBar();
                setVolume();


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

                setFullscreenAndDefault();
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

       mHandler.sendEmptyMessageDelayed(HIDE_VIDEO_PLAYER_CONTROL, 5000);
    }

    private void setVoiceIconAndSeekBar() {
        setVoiceIcon();
        mVoiceSeekBar.setProgress(mVolume);
    }

    private void setVolume() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolume, AudioManager.FLAG_PLAY_SOUND);
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


    private void setFullscreenAndDefault() {
//        RelativeLayout.LayoutParams params;
//        params = (!isFullscreen) ? mVideoViewFirstParams :
//                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        mVideoView.setLayoutParams(params);
//        isFullscreen = !isFullscreen;

        int width = mScreenWidth;
        int height = mScreenHeight;

        if (isFullscreen) {
//            恢复到默认的状态
            // for compatibility, we adjust size based on aspect ratio
            if ( mVideoWidth * height  < width * mVideoHeight ) {
                //Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else if ( mVideoWidth * height  > width * mVideoHeight ) {
                //Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            }
            isFullscreen = false;
        } else {
//            切换到全屏状态
           isFullscreen = true;
        }
        mVideoView.setVideoSize(width, height);

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
                mHandler.removeMessages(HIDE_VIDEO_PLAYER_CONTROL);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.sendEmptyMessageDelayed(HIDE_VIDEO_PLAYER_CONTROL, 5000);
            }
        });

        mVoiceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    mVolume = progress;
                    setVolume();
                    setVoiceIconAndSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(HIDE_VIDEO_PLAYER_CONTROL);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.sendEmptyMessageDelayed(HIDE_VIDEO_PLAYER_CONTROL, 5000);
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
                mVideoPlayerControl.setVisibility(View.INVISIBLE);
                isControlSee = false;

                mPlay.setImageResource(R.mipmap.stop);

                if (mLastPlayedDuration > 0) {
                    Toast.makeText(VideoPlayerAty.this, "从上次记录播放", Toast.LENGTH_SHORT).show();

                }
//              获得视频的原始大小
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();

                readLastPlayedDuration();
                mVideoView.setVideoSize(mScreenWidth, mScreenHeight);
                mVideoView.seekTo(mLastPlayedDuration);
                mVideoView.start();

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
                mSelectedVideo = (++mSelectedVideo) % mVideoList.size();
                mVideoView.setVideoPath(mVideoList.get(mSelectedVideo).getPath());
            }
        });

//        mVideoView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        mVideoPlayerControl.setVisibility((isControlSee ? View.INVISIBLE : View.VISIBLE));
//                        isControlSee = !isControlSee;
//                        if (isControlSee) {
//                            mHandler.sendEmptyMessageDelayed(HIDE_VIDEO_PLAYER_CONTROL, 3500);
//                        } else {
//                            mHandler.removeMessages(HIDE_VIDEO_PLAYER_CONTROL);
//                        }
//                        break;
//                }
//                return true;
//            }
//        });




    }

    private void setVoiceIcon() {
        int icon = R.mipmap.voice_mute_normal;

        if (mVolume == 0)
            icon = R.mipmap.voice_mute_normal;
        else if (mVolume <= 5)
            icon = R.mipmap.voice1_normal;
        else if (mVolume <= 10)
            icon = R.mipmap.voice2_normal;
        else if (mVolume <= 15)
            icon = R.mipmap.voice3_normal;

        mVoice.setImageResource(icon);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);   // 把手势传递给手势识别器
        return super.onTouchEvent(event);
    }

    class PowerReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0); // level 表示电量的级别 0~100
            setBatteryLevel(level);
        }
    }
}
