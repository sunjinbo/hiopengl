package com.hiopengl.android.graphics.producer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.Surface;

import java.io.IOException;

public class MediaPlayerProducer implements ISurfaceProducer,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private MediaPlayer mMediaPlayer;
    private Context mContext;

    public MediaPlayerProducer(Context context) {
        try {
            mContext = context;
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setLooping(true);
            AssetFileDescriptor afd = mContext.getResources().getAssets().openFd("sample.mp4");
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void stop() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    @Override
    public void setSurface(Surface surface) {
        mMediaPlayer.setSurface(surface);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }
}
