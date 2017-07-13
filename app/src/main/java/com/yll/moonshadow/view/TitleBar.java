package com.yll.moonshadow.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yll.moonshadow.R;

/**
 * Created by yelelen on 7/13/2017.
 */

public class TitleBar extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private View mHome;
    private View mSearch;
    private View mRl;
    private View mHistory;

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mHome = findViewById(R.id.tb_home);
        mSearch = findViewById(R.id.tb_search);
        mRl  = findViewById(R.id.tb_rl);
        mHistory = findViewById(R.id.tb_history);

        mHistory.setOnClickListener(this);
        mRl.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        mHome.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tb_home:
                Toast.makeText(mContext, "首页", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tb_search:
                Toast.makeText(mContext, "搜索", Toast.LENGTH_SHORT).show();

                break;
            case R.id.tb_rl:
                Toast.makeText(mContext, "游戏", Toast.LENGTH_SHORT).show();

                break;
            case R.id.tb_history:
                Toast.makeText(mContext, "搜索记录", Toast.LENGTH_SHORT).show();

                break;
        }
    }
}
