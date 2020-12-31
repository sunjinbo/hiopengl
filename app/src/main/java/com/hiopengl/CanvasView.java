package com.hiopengl;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CanvasView extends View {

    private CanvasDrawer mDrawer;

    public CanvasView(Context context) {
        super(context);
        initView();
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mDrawer.draw(canvas);
    }

    private void initView() {
        mDrawer = new CanvasDrawer();
    }
}
