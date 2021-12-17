package com.hiopengl.android.codec;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoDecodeActivity extends ActionBarActivity
        implements Runnable, SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private Surface mSurface;
    private TextView mTipsText;
    private MediaExtractor mMediaExtractor;
    private MediaCodec mMediaCodec;

    private MediaFormat mMediaFormat;
    private int mMediaTrackIndex = -1;
    private String mMediaMime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codec);
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);
        mTipsText = findViewById(R.id.tips);
    }

    @Override
    public void run() {
        try {
            mMediaExtractor.selectTrack(mMediaTrackIndex);
            mMediaCodec = MediaCodec.createDecoderByType(mMediaMime);
            mMediaCodec.configure(mMediaFormat, mSurface, null, 0);

            long timeout = 10000L; // 10ms
            long startMillis = System.currentTimeMillis();
            boolean isEOS = false;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            mMediaCodec.start();
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();

            while (!isEOS) {
                int inIndex = mMediaCodec.dequeueInputBuffer(timeout);
                if (inIndex >= 0) {
                    int size = mMediaExtractor.readSampleData(inputBuffers[inIndex], 0);
                    if (size < 0) {
                        mMediaCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        mMediaCodec.queueInputBuffer(inIndex, 0, size, mMediaExtractor.getSampleTime(), 0);
                        mMediaExtractor.advance();
                        inputBuffers[inIndex].clear();
                    }
                }

                int outIndex;
                do {
                    outIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, timeout);
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEOS = true;
                    }

                    if (outIndex >= 0) {
                        while (bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMillis) {
                            SystemClock.sleep(10);
                        }

                        mMediaCodec.releaseOutputBuffer(outIndex, true);
                    }
                } while (outIndex >= 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startDecode(File videoFile) throws IOException {
        mMediaExtractor = new MediaExtractor();
        mMediaExtractor.setDataSource(videoFile.getAbsolutePath());
        int count = mMediaExtractor.getTrackCount();
        for (int i = 0; i < count; ++i) {
            MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(i);
            String mimeString = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mimeString.startsWith("video/")) {
                mMediaTrackIndex = i;
                mMediaMime = mimeString;
                mMediaFormat = mediaFormat;
                break;
            }
        }

        if (mMediaTrackIndex >= 0) {
            MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(mMediaTrackIndex);
            String language = mediaFormat.getString(MediaFormat.KEY_LANGUAGE); // 获取语言格式内容
            int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH); // 获取高度
            int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT); // 获取高度
            long durationTime = mediaFormat.getLong(MediaFormat.KEY_DURATION); // 总时间

            StringBuffer sb = new StringBuffer();
            sb.append(videoFile.getAbsolutePath() + "\r\n");
            sb.append("lang=" + language + "\r\n");
            sb.append("size=" + width + "x" + height + "\r\n");
            sb.append("duration=" + durationTime + "\r\n");
            mTipsText.setText(sb.toString());

            new Thread(this).start();
        } else {
            mTipsText.setText(videoFile.getAbsolutePath() + " doesn't has video track!!");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurface = holder.getSurface();

        runOnUiThread(() -> {
            File videoFile = new File(this.getExternalCacheDir(), "vid.mp4");
            if (!videoFile.exists()) {
                mTipsText.setText(videoFile.getAbsolutePath() + " is Not Found!!");
            } else {
                try {
                    startDecode(videoFile);
                } catch (IOException e) {
                    mTipsText.setText("Decode " + videoFile.getAbsolutePath() + " Failed!!");
                }
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
