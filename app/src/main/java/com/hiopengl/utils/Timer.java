package com.hiopengl.utils;

import android.os.SystemClock;

public class Timer extends Thread {
    private int mSeconds;
    private Callback mCallback;

    public Timer(int seconds, Callback callback) {
        super();
        mSeconds = seconds;
        mCallback = callback;
    }

    @Override
    public void run() {
        SystemClock.sleep(mSeconds * 1000L);
        if (mCallback != null) {
            mCallback.onTimerExpired();
        }
    }

    public interface Callback {
        void onTimerExpired();
    }
}
