package com.hiopengl.examples;

public abstract class ExampleItem {
    protected String mExampleName;
    protected boolean mIsDone;

    public ExampleItem(String exampleName, boolean isDone) {
        mExampleName = exampleName;
        mIsDone = isDone;
    }

    public String getExampleName() {
        return mExampleName;
    }

    public boolean isDone() {
        return mIsDone;
    }
}
