package com.yll.moonshadow.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by yelelen on 7/15/2017.
 */

public class VideoView extends android.widget.VideoView {


    public VideoView(Context context) {
        this(context, null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
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
