package com.yll.moonshadow.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.yll.moonshadow.R;
import com.yll.moonshadow.fragment.BaseFragment;
import com.yll.moonshadow.fragment.CloudMusicFragment;
import com.yll.moonshadow.fragment.CloudVideoFragment;
import com.yll.moonshadow.fragment.LocalMusicFragment;
import com.yll.moonshadow.fragment.LocalVideoFragment;
import com.yll.moonshadow.utils.Utils;

import java.util.ArrayList;

/**
 * Created by yelelen on 7/13/2017.
 */

public class MainAty extends FragmentActivity {
    private static final int WRITE_SD_PERMISSION_CODE = 0;
    private static final int READ_SD_PERMISSION_CODE = 1;

    private boolean isWrite = false;
    private boolean isRead = false;
    private RadioGroup mRadioGroup;
    //    保存当前被选中的页面位置
    private int mPosition = 0;

    private ArrayList<BaseFragment> mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mFragments = new ArrayList<BaseFragment>();

        mFragments.add(new LocalVideoFragment());
        mFragments.add(new LocalMusicFragment());
        mFragments.add(new CloudVideoFragment());
        mFragments.add(new CloudMusicFragment());

        mRadioGroup = (RadioGroup)findViewById(R.id.radio_group);

        isWrite = Utils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_SD_PERMISSION_CODE);
        isRead = Utils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, READ_SD_PERMISSION_CODE);


        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.local_video:
                        mPosition = 0;
                        break;
                    case R.id.local_music:
                        mPosition = 1;
                        break;
                    case R.id.cloud_video:
                        mPosition = 2;
                        break;
                    case R.id.cloud_music:
                        mPosition = 3;
                        break;
                }
                refreshFragment();

            }
        });
//        初始化时选中本地视频页面
        mRadioGroup.check(R.id.local_video);

    }

    private void refreshFragment(){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_main, mFragments.get(mPosition));
        ft.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case WRITE_SD_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                     isWrite = true;
                else
                    Toast.makeText(this, "没有写SD卡的权限", Toast.LENGTH_SHORT).show();

                break;
            case READ_SD_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    isRead = true;
                else
                    Toast.makeText(this, "没有读取SD卡的权限", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        Log.d(MainAty.class.getName(), "onStart");
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        Log.d(MainAty.class.getName(), "onResume");
//
//    }
}