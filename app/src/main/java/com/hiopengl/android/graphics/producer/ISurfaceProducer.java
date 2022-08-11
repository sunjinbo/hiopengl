package com.hiopengl.android.graphics.producer;

import android.view.Surface;

public interface ISurfaceProducer {
    void start();
    void stop();
    void setSurface(Surface surface);
}
