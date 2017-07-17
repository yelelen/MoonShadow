package com.yll.moonshadow.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yll.moonshadow.R;
import com.yll.moonshadow.activity.VideoPlayerAty;
import com.yll.moonshadow.adapter.LocalVideoAdapter;
import com.yll.moonshadow.beans.VideoItem;

import java.util.ArrayList;

import static com.yll.moonshadow.utils.Constants.MSG_TAG;

/**
 * Created by yelelen on 7/13/2017.
 */

public class LocalVideoFragment extends BaseFragment{



    private ListView mVideoListView;
    private TextView mNoMediaTextView;
    private ProgressBar mProgressBar;

    private ArrayList<VideoItem> mVideoItems;
    private ContentResolver mResolver;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MSG_TAG:
                    if (mVideoItems != null && mVideoItems.size() > 0) {
//                        Toast.makeText(getActivity(), "VideoItems size " + mVideoItems.size(), Toast.LENGTH_SHORT).show();
                        mVideoListView.setAdapter(new LocalVideoAdapter(getContext(), mVideoItems));
                        mNoMediaTextView.setVisibility(View.GONE);
                    } else {
                        mNoMediaTextView.setVisibility(View.VISIBLE);
                    }
                    break;
            }
            mProgressBar.setVisibility(View.GONE);
            return true;
        }
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVideoItems = new ArrayList<VideoItem>();

        mResolver = getActivity().getContentResolver();
        getDataFromLocal();
    }


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(LocalVideoFragment.class.getName(), "onCreateView");


        View v = inflater.from(getActivity()).inflate(R.layout.local_video, null);
        mVideoListView = (ListView)v.findViewById(R.id.lv_listview);
        mNoMediaTextView = (TextView)v.findViewById(R.id.lv_no_media);
        mProgressBar = (ProgressBar)v.findViewById(R.id.lv_progress_bar);

        mVideoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 隐式Intent调用系统的视频播放器
//                Intent intent = new Intent();
//                intent.setDataAndType(Uri.parse(mVideoItems.get(position).getPath()), "video/*");
//                startActivity(intent);
//                intent.setDataAndType(Uri.parse(mVideoItems.get(position).getPath()), "video/*");
                Intent intent = new Intent(getContext(), VideoPlayerAty.class);

                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putParcelableArrayList("video_list", mVideoItems);
                intent.putExtra("video_data", bundle);
                startActivity(intent);
            }
        });

        return v;
    }

    private void getDataFromLocal(){
        new Thread() {
            @Override
            public void run() {

                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] columns = {
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DATA,    // 视频文件的绝对路径
                        MediaStore.Video.Media.ARTIST
                };
                Cursor cursor = mResolver.query(uri, columns, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        VideoItem videoItem = new VideoItem();
                        videoItem.setName(cursor.getString(0));
                        videoItem.setDuration(cursor.getLong(1));
                        videoItem.setSize(cursor.getLong(2));
                        videoItem.setPath(cursor.getString(3));
                        videoItem.setArtist(cursor.getString(4));

//                        Log.e("11111", videoItem.getPath());

                        mVideoItems.add(videoItem);
                    }
                    cursor.close();
                }
////                加载完成，发送消息
                mHandler.sendEmptyMessage(MSG_TAG);

            }
        }.start();
    }


//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//        Log.d(LocalVideoFragment.class.getName(), "onAttach");
//
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        Log.d(LocalVideoFragment.class.getName(), "onStart");
//
//    }
//
//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        Log.d(LocalVideoFragment.class.getName(), "onActivityCreated");
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        Log.d(LocalVideoFragment.class.getName(), "onResume");
//
//    }
}
