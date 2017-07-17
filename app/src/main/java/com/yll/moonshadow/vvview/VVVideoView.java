package com.yll.moonshadow.vvview;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import io.vov.vitamio.widget.VideoView;

/**
 * Created by yelelen on 7/17/2017.
 */

public class VVVideoView extends VideoView {

    public VVVideoView(Context context) {
        this(context, null);
    }

    public VVVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VVVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    public void setVideoSize(int width, int height){
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = height;
        params.width = width;
        setLayoutParams(params);
    }
}
