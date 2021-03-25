package com.hiopengl.examples;

import android.text.TextUtils;

public class ExampleInfo extends ExampleItem {
    private String mActivityFrom;

    public ExampleInfo(String exampleName, String activityFrom, boolean isDone) {
        super(exampleName, isDone);
        mActivityFrom = activityFrom;
    }

    public ExampleInfo(String exampleName, String activityFrom) {
        super(exampleName, !TextUtils.isEmpty(activityFrom));
        mActivityFrom = activityFrom;
    }

    public String getActivityFrom() {
        return mActivityFrom;
    }
}
