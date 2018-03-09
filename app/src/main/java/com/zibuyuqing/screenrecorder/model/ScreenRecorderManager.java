package com.zibuyuqing.screenrecorder.model;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.Log;
import android.view.Surface;

import com.zibuyuqing.screenrecorder.settings.Config;
import com.zibuyuqing.screenrecorder.utils.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/03/02
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ScreenRecorderManager {
    private static final String TAG = ScreenRecorderManager.class.getSimpleName();
    private static final int MAX_DURATION = 60 * 60 * 1000;
    private volatile static ScreenRecorderManager sInstance;
    private Context mContext;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private MediaRecorder mMediaRecorder;
    private Surface mSurface;

    private long mStartTime; //开始时间
    private int mDpi = 1; // 密度
    private Config mConfig;
    private boolean isMediaRecorderReady = false;
    private RecordStateCallback mCallback;

    private VirtualDisplay mVirtualDisplay;

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private ScreenRecorderManager(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        if (mProjectionManager == null) {
            mProjectionManager =
                    (MediaProjectionManager) mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }
    }

    public MediaProjectionManager getMediaProjectionManager() {
        return mProjectionManager;
    }

    public void setMediaProject(MediaProjection mediaProjection) {
        Log.e(TAG, "setMediaProject ;; mediaProjection =:" + mediaProjection);
        mMediaProjection = mediaProjection;
    }

    private void initRecorder() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        File dstFile = new File(Utilities.getFileDir());
        if (!dstFile.exists()) {
            dstFile.mkdirs();
        }
        mMediaRecorder.reset();
        Log.e(TAG, "initRecorder = :" + mConfig.toString());
        // source
        int audioSource = mConfig.getAudioSource();
        if (audioSource == 1) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        }
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        // output
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setOutputFile(mConfig.getSavePath());

        // video config
        mMediaRecorder.setVideoSize(mConfig.getRecordWidth(), mConfig.getRecordHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoEncodingBitRate(mConfig.getVideoEncodingBitRate());
        mMediaRecorder.setVideoFrameRate(mConfig.getVideoFrameRate());

        // audio config
        if (audioSource == 1) {
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setAudioSamplingRate(44100);
            mMediaRecorder.setAudioEncodingBitRate(96000);
        }
        // other

        mMediaRecorder.setMaxDuration(MAX_DURATION);
        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {

            }
        });
        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {

            }
        });

        try {
            mMediaRecorder.prepare();
            if (mSurface == null) {
                mSurface = mMediaRecorder.getSurface();
            }
            isMediaRecorderReady = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord(boolean hasError) {
        if (isRunning.get()) {
            releaseRecorder(hasError);
        }
    }

    public void startRecord(Config config, RecordStateCallback callback) {
        Log.e(TAG, "mMediaProjection =:" + mMediaProjection);
        if (mMediaProjection == null) {
            return;
        }
        mConfig = config;
        mCallback = callback;
        if (!isMediaRecorderReady) {
            initRecorder();
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        isRunning.set(true);
        mStartTime = System.currentTimeMillis();
        if (mCallback != null) {
            mCallback.onRecordStart();
        }
    }

    public boolean isRecording() {
        return isRunning.get();
    }

    public void requestPermission(Activity activity, int requestCode) {
        Intent intent = new Intent(mProjectionManager.createScreenCaptureIntent());
        activity.startActivityForResult(intent, requestCode);
    }

    private void releaseRecorder(boolean hasError) {

        if (this.mSurface != null) {
            this.mSurface.release();
            this.mSurface = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (!hasError && mCallback != null) {
            mCallback.onRecordStop(System.currentTimeMillis() - mStartTime);
        }
        isMediaRecorderReady = false;
        isRunning.set(false);
    }

    private VirtualDisplay createVirtualDisplay() {

        return mMediaProjection.createVirtualDisplay(
                TAG + "-display",
                mConfig.getRecordWidth(),
                mConfig.getRecordHeight(),
                mDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mSurface,
                null,
                null);
    }

    public static ScreenRecorderManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ScreenRecorderManager.class) {
                if (sInstance == null) {
                    sInstance = new ScreenRecorderManager(context);
                }
            }
        }
        return sInstance;
    }

    public interface RecordStateCallback {
        void onRecordStart();

        void onRecordStop(long duration);

        void onError(String msg);
    }
}
