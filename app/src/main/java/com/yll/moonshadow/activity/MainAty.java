package com.yll.moonshadow.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.widget.RadioGroup;

import com.yll.moonshadow.R;
import com.yll.moonshadow.fragment.BaseFragment;
import com.yll.moonshadow.fragment.CloudMusicFragment;
import com.yll.moonshadow.fragment.CloudVideoFragment;
import com.yll.moonshadow.fragment.LocalMusicFragment;
import com.yll.moonshadow.fragment.LocalVideoFragment;

import java.util.ArrayList;

/**
 * Created by yelelen on 7/13/2017.
 */

public class MainAty extends FragmentActivity {
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

}