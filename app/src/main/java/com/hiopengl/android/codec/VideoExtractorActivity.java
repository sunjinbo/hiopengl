package com.hiopengl.android.codec;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoExtractorActivity extends ActionBarActivity implements Runnable {
    private SurfaceView mSurfaceView;
    private TextView mTipsText;
    private MediaExtractor mMediaExtractor;
    private MediaFormat mVideoFormat;
    private int mVideoTrack = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codec);
        mSurfaceView = findViewById(R.id.surface_view);
        mTipsText = findViewById(R.id.tips);
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
    }

    @Override
    public void run() {
        File videoFile = new File(getExternalCacheDir(), "vid.h264");
        if (videoFile.exists()) {
            videoFile.delete();
        }

        try {
            FileOutputStream videoOutputStream = new FileOutputStream(videoFile);
            int maxVideoBufferCount = mVideoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            ByteBuffer videoByteBuffer = ByteBuffer.allocate(maxVideoBufferCount);
            mMediaExtractor.selectTrack(mVideoTrack);
            int len = 0;
            while ((len = mMediaExtractor.readSampleData(videoByteBuffer, 0)) != -1) {
                byte[] bytes = new byte[len];
                videoByteBuffer.get(bytes);//获取字节
                videoOutputStream.write(bytes);//写入字节
                videoByteBuffer.clear();
                mMediaExtractor.advance();//预先加载后面的数据
            }
            videoOutputStream.flush();
            videoOutputStream.close();
            mMediaExtractor.unselectTrack(mVideoTrack);

            runOnUiThread(() -> {
                StringBuffer sb = new StringBuffer();
                sb.append(videoFile.getAbsolutePath());
                sb.append(" is generated!");
                Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
            });
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
                mVideoTrack = i;
                mVideoFormat = mediaFormat;
                break;
            }
        }

        if (mVideoTrack >= 0) {
            MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(mVideoTrack);
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
}
