package com.yll.moonshadow.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yll.moonshadow.R;
import com.yll.moonshadow.beans.VideoItem;
import com.yll.moonshadow.utils.Utils;

import java.util.ArrayList;

/**
 * Created by yelelen on 7/14/2017.
 */

public class LocalVideoAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<VideoItem> mVideoItems;

    public LocalVideoAdapter(Context context, ArrayList<VideoItem> videoItems) {
        mContext = context;
        mVideoItems = videoItems;
    }

    @Override
    public int getCount() {
        return mVideoItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mVideoItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.lv_list_item, null);
            ImageView iv_video = (ImageView)convertView.findViewById(R.id.lv_item_video);
            TextView tv_name = (TextView)convertView.findViewById(R.id.lv_item_name);
            TextView tv_size = (TextView)convertView.findViewById(R.id.lv_item_size);
            TextView tv_duration = (TextView)convertView.findViewById(R.id.lv_item_duration);


            viewHolder.iv_video = iv_video;
            viewHolder.tv_name = tv_name;
            viewHolder.tv_size = tv_size;
            viewHolder.tv_duration = tv_duration;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        VideoItem videoItem = mVideoItems.get(position);
        viewHolder.iv_video.setImageResource(R.drawable.video_item);
        viewHolder.tv_name.setText(videoItem.getName());
        viewHolder.tv_size.setText(Formatter.formatFileSize(mContext, videoItem.getSize()));
        viewHolder.tv_duration.setText(Utils.formatDuration(videoItem.getDuration()));

        return convertView;
    }

    static class ViewHolder{
        ImageView iv_video;
        TextView tv_name;
        TextView tv_size;
        TextView tv_duration;
    }
}
