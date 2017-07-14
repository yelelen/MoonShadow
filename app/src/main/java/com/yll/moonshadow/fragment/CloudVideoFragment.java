package com.yll.moonshadow.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yll.moonshadow.R;

/**
 * Created by yelelen on 7/13/2017.
 */

public class CloudVideoFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.from(getActivity()).inflate(R.layout.cloud_video, null);
    }

}