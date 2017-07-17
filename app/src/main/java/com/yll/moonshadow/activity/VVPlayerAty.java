package com.yll.moonshadow.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yll.moonshadow.R;
import com.yll.moonshadow.vvview.VVVideoView;
import com.yll.moonshadow.beans.VideoItem;
import com.yll.moonshadow.utils.Utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.vov.vitamio.MediaPlayer;

/**
 * Created by yelelen on 7/14/2017.
 */

public class VVPlayerAty extends Activity implements View.OnClickListener {
    private static final int UPDATE_VIDEO_PROGRESS = 0;
    private static final int HIDE_VIDEO_PLAYER_CONTROL = 1;
    private static final int NET_SPEED = 2;

    private VVVideoView mVideoView;
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
    private TextView mNetSpeed;

    private RelativeLayout mBufferProgress;  // 缓冲进度条

    private RelativeLayout mVideoPlayerControl;
    private boolean isControlSee;   // 视频控制器是否可见
    private boolean isFullscreen = true;    // 是否处于全屏状态
    private boolean isWebUri;   // 是否是网络地址
    private boolean isShowNetSpeed;
    boolean isVoiceAdd = true;

    private RelativeLayout.LayoutParams mVideoViewFirstParams = null;
    private int mLastPlayedDuration = 0;        // 记录上次已经播放该视频的时长
    private SharedPreferences mSharedPreferences;

    private ArrayList<VideoItem> mVideoList;    //    传进来的视频列表
    private int mSelectedVideo;     //  用户在视频列表中点击的视频位置

    private PowerReceiver mPowerReceiver;   // 监听电源变化的广播的接收器

    private GestureDetector mGestureDetector; // 手势识别器

    private AudioManager mAudioManager; // 声音服务
    private int mVolume; // 当前的音量
    private int mMaxVolume;


    private Uri mUri;   // 从外部调用本视频播放器时传入的uri
    private int mLastUriPlayedDuration = 0;

    private long mLastGetTotalNetBytes = 0;
    private long mLastTimestamp = 0;

    // 屏幕宽和高
    private int mScreenWidth;
    private int mScreenHeight;
    //    视频大小
    private int mVideoWidth;
    private int mVideoHeight;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VIDEO_PROGRESS:
                    int currentPos = (int) mVideoView.getCurrentPosition();
                    mVideoSeekBar.setProgress(currentPos);

                    if (isWebUri) {
                        mVideoSeekBar.setSecondaryProgress(getSecondProgress());
                    }

                    mPlayed.setText(Utils.formatDuration(currentPos));
                    mSystemTime.setText(getSystemTime());

                    mHandler.removeMessages(msg.what);
                    mHandler.sendEmptyMessageDelayed(UPDATE_VIDEO_PROGRESS, 1000);
                    break;

                case HIDE_VIDEO_PLAYER_CONTROL:
                    mVideoPlayerControl.setVisibility(View.INVISIBLE);
                    break;

                case NET_SPEED:
                    long currentTime = SystemClock.currentThreadTimeMillis();
                    long currentBytes = getNetBytes();
                    long deltaBytes = currentBytes - mLastGetTotalNetBytes;
                    long deltaTime = (currentTime == mLastTimestamp) ? currentTime : currentTime - mLastTimestamp;

                    long speed = deltaBytes * 1000 / deltaTime; // 每秒的网速，kb/s
                    DecimalFormat df = new DecimalFormat("#.00"); // 保留两位小数
                    mNetSpeed.setText( String.valueOf(speed > 1024 ? df.format(speed * 1.0 / 1024) : speed)+ ((speed >= 1024) ? "  Mb/s" : "  Kb/s"));
//                    Log.e("netspeed", String.valueOf(speed));

                    mLastGetTotalNetBytes = currentBytes;
                    mLastTimestamp = currentTime;

                    mHandler.removeMessages(NET_SPEED);
                    if (isShowNetSpeed) {
                        mHandler.sendEmptyMessageDelayed(NET_SPEED, 1000);
                    }

                default:
                    break;
            }
            return true;
        }
    });


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.vvvideo_player);

        findViews();

        setListeners();

        registerPowerListener();

        initData();

    }

    private void initData() {

        mUri = getIntent().getData();
        isWebUri = Utils.isWebUri(mUri);

        if (mUri != null) {      // 视频来自网络或者从外部传进来
            mLastUriPlayedDuration = getIntent().getIntExtra("played_position", 0);

            mVideoView.setVideoURI(mUri);

            // 隐藏播放下一个和上一个按键
            mPrevious.setVisibility(View.GONE);
            mNext.setVisibility(View.GONE);
        } else {        // 获得自己内部的视频列表
            Bundle bundle = getIntent().getBundleExtra("video_data");
            mVideoList = bundle.getParcelableArrayList("video_list");
            mSelectedVideo = bundle.getInt("position");
            mVideoView.setVideoPath(mVideoList.get(mSelectedVideo).getPath());
        }


//设置系统自带的控制器
//        mVideoView.setMediaController(new MediaController(this));

        mVideoViewFirstParams = (RelativeLayout.LayoutParams) mVideoView.getLayoutParams();
        // 获得屏幕的宽和高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVoiceSeekBar.setMax(mMaxVolume);
        setVoiceIconAndSeekBar();

        // 得到当前的数据总量和时间，发送消息
        mLastTimestamp = SystemClock.currentThreadTimeMillis();
        mLastGetTotalNetBytes = getNetBytes();
        mHandler.sendEmptyMessage(NET_SPEED);
        isShowNetSpeed = true;

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
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
        if (mUri == null) {
            mLastPlayedDuration = (int)mVideoView.getCurrentPosition();
            saveLastPlayedDuration();
        } else
            mLastUriPlayedDuration = (int)mVideoView.getCurrentPosition();
    }


    @Override
    protected void onDestroy() {
        if (mPowerReceiver != null) {
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

        switch (v.getId()) {
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
                Toast.makeText(this, "切换到VideoPlayer", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, VideoPlayerAty.class);
                if (mUri == null) {
                    mUri = Uri.parse(mVideoList.get(mSelectedVideo).getPath());
                }
                intent.setData(mUri);
                intent.putExtra("played_position", (int) mVideoView.getCurrentPosition());
                startActivity(intent);

                finish();

                break;
            case R.id.video_return:
                saveLastPlayedDuration();
                finish();
                break;
            case R.id.video_previous:
                saveLastPlayedDuration();
                if (--mSelectedVideo < 0) mSelectedVideo = mVideoList.size() - 1;
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
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        mVideoPlayerControl.setVisibility(View.VISIBLE);
    }

    private void readLastPlayedDuration() {
        if (mUri == null) {
            mSharedPreferences = getSharedPreferences(mVideoList.get(mSelectedVideo).getName(), MODE_PRIVATE);
            mLastPlayedDuration = mSharedPreferences.getInt("player_duration", 0);
        }
    }


    private void saveLastPlayedDuration() {
        if (mUri == null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt("player_duration", (int) mVideoView.getCurrentPosition());
            editor.apply();
        }
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
            if (mVideoWidth * height < width * mVideoHeight) {
                //Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else if (mVideoWidth * height > width * mVideoHeight) {
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
        if (mVideoView.isPlaying()) {
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
        pos = (int)mVideoView.getCurrentPosition() + forwardtime * 1000;
        pos = (pos <= (int)mVideoView.getDuration()) ? pos : (int) mVideoView.getDuration();
        mVideoSeekBar.setProgress(pos);
        mVideoView.seekTo(pos);
        mVideoView.start();

        mPlay.setImageResource(R.mipmap.stop);
    }

    private void playBack(int backtime) {
        int pos;//                后退键，每次后退backtime, 单位为秒
        pos = (int) mVideoView.getCurrentPosition() - backtime * 1000;
        pos = (pos >= 0) ? pos : 0;
        mVideoSeekBar.setProgress(pos);
        mVideoView.seekTo(pos);
        mVideoView.start();

        mPlay.setImageResource(R.mipmap.stop);
    }

    private void setBatteryLevel(int level) {
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
                if (fromUser) {
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
                if (fromUser) {
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
                isControlSee = false;
                isShowNetSpeed = false;

                mBufferProgress.setVisibility(View.GONE);
                mHandler.removeMessages(NET_SPEED);

                int duration = (int) mVideoView.getDuration();
                mVideoSeekBar.setMax(duration);

                mDuration.setText(Utils.formatDuration(duration));
                mPlayed.setText(Utils.formatDuration(0));

                mVideoView.setLayoutParams(mVideoViewFirstParams);

                mPlay.setImageResource(R.mipmap.stop);


//              获得视频的原始大小
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();

                mVideoView.setVideoSize(mScreenWidth, mScreenHeight);
                if (mUri == null) {
                    readLastPlayedDuration();
                    mVideoView.seekTo(mLastPlayedDuration);
                    mTitle.setText(mVideoList.get(mSelectedVideo).getName());
                } else {
                    mTitle.setText(mUri.getPath());
                    mVideoView.seekTo(mLastUriPlayedDuration);
                }

                if (mLastPlayedDuration > 0) {
                    Toast.makeText(VVPlayerAty.this, "从上次记录播放", Toast.LENGTH_SHORT).show();
                }

                mVideoView.start();

                mHandler.sendEmptyMessage(UPDATE_VIDEO_PROGRESS);
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(VVPlayerAty.this, "视频播放失败", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mUri == null) {
                    mSelectedVideo = (++mSelectedVideo) % mVideoList.size();
                    mVideoView.setVideoPath(mVideoList.get(mSelectedVideo).getPath());
                }
            }
        });

//        调用系统API监听视频播放卡顿，API >= 17 (Android 4.2.2)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:  // 播放开始卡顿
                            mBufferProgress.setVisibility(View.VISIBLE);
                            isShowNetSpeed = true;
                            mHandler.sendEmptyMessage(NET_SPEED);
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:  // 卡顿停止
                            mBufferProgress.setVisibility(View.GONE);
                            isShowNetSpeed = false;
                            mHandler.sendEmptyMessage(NET_SPEED);
                            break;
                    }

                    return true;
                }
            });
        }

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

    private void findViews() {
        mVoice = (ImageView) findViewById(R.id.voice_voice);
        mVoiceSeekBar = (SeekBar) findViewById(R.id.voice_progress);
        mSwitch = (ImageView) findViewById(R.id.voice_switch);
        mPlayed = (TextView) findViewById(R.id.video_played);
        mVideoSeekBar = (SeekBar) findViewById(R.id.video_progress);
        mReturn = (ImageView) findViewById(R.id.video_return);
        mPrevious = (ImageView) findViewById(R.id.video_previous);
        mBack = (ImageView) findViewById(R.id.video_back);
        mPlay = (ImageView) findViewById(R.id.video_play);
        mForward = (ImageView) findViewById(R.id.video_forward);
        mNext = (ImageView) findViewById(R.id.video_next);
        mFullscreen = (ImageView) findViewById(R.id.video_fullscreen);
        mDuration = (TextView) findViewById(R.id.video_duration);
        mTitle = (TextView) findViewById(R.id.video_title);
        mBattery = (ImageView) findViewById(R.id.battery);
        mSystemTime = (TextView) findViewById(R.id.system_time);
        mNetSpeed = (TextView)findViewById(R.id.loading_speed);


        mVideoPlayerControl = (RelativeLayout) findViewById(R.id.video_player_control);
        mBufferProgress = (RelativeLayout) findViewById(R.id.rl_video_buffer);
//        mBufferProgress.setVisibility(View.GONE);
        mVideoPlayerControl.setVisibility(View.GONE);

        mVideoView = (VVVideoView) findViewById(R.id.video_view);

    }


    float startX, endX;
    float startY, endY;
    boolean isAdjustVolume;
    boolean isAddVolume;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);   // 把手势传递给手势识别器

        mHandler.removeMessages(HIDE_VIDEO_PLAYER_CONTROL);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                isAdjustVolume = (startX > mScreenWidth / 2);
                break;
            case MotionEvent.ACTION_MOVE:
                endX = event.getX();
                endY = event.getY();
                float distanceY = startY - endY;
                isAddVolume = !(distanceY < 0);

                float scale = Math.abs(distanceY) / mScreenHeight;

                if (isAdjustVolume) {     // 调节音量
                    int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    int delta = (int) (scale * mMaxVolume);

                    if (isAddVolume)
                        mVolume = (currentVolume + delta) > mMaxVolume ? mMaxVolume : (currentVolume + delta);
                    else
                        mVolume = (currentVolume - delta) < 0 ? 0 : (currentVolume - delta);

                    setVoiceIconAndSeekBar();
                    setVolume();
                } else {        // 调节亮度

                }
                break;
            case MotionEvent.ACTION_UP:

                break;
        }

        mHandler.sendEmptyMessageDelayed(HIDE_VIDEO_PLAYER_CONTROL, 5000);

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mVolume--;
                setVoiceIconAndSeekBar();
                setVolume();
                mHandler.removeMessages(HIDE_VIDEO_PLAYER_CONTROL);
                mHandler.sendEmptyMessageDelayed(HIDE_VIDEO_PLAYER_CONTROL, 5000);
                return true;

            case KeyEvent.KEYCODE_VOLUME_UP:
                mVolume++;
                setVoiceIconAndSeekBar();
                setVolume();
                mHandler.removeMessages(HIDE_VIDEO_PLAYER_CONTROL);
                mHandler.sendEmptyMessageDelayed(HIDE_VIDEO_PLAYER_CONTROL, 5000);
                return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    class PowerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0); // level 表示电量的级别 0~100
            setBatteryLevel(level);
        }
    }


    private int getSecondProgress() {
        return mVideoView.getBufferPercentage() * mVideoSeekBar.getMax();
    }


    private String getSystemTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        return sdf.format(new Date());
    }

    private long getNetBytes() {
        // 获得当前应用的数据总量
        return TrafficStats.getUidRxBytes(getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0
                : TrafficStats.getUidRxBytes(getApplicationInfo().uid) / 1024;  // 转为kb
//
//        // 获得当前手机的数据总量
//        return TrafficStats.getUidRxBytes(getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0
//                : TrafficStats.getTotalRxBytes() / 1024;  // 转为kb
    }
}
