package com.zibuyuqing.screenrecorder.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.zibuyuqing.screenrecorder.NoDisplayActivity;
import com.zibuyuqing.screenrecorder.model.RecordControllerWindowManager;
import com.zibuyuqing.screenrecorder.model.RecordFilesManager;
import com.zibuyuqing.screenrecorder.model.ScreenRecorderManager;
import com.zibuyuqing.screenrecorder.settings.Config;
import com.zibuyuqing.screenrecorder.settings.SharedPreferenceHelper;
import com.zibuyuqing.screenrecorder.ui.MainActivity;
import com.zibuyuqing.screenrecorder.utils.Utilities;
import com.zibuyuqing.screenrecorder.views.CountTimerWindow;
import com.zibuyuqing.screenrecorder.views.RecordControllerWindow;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/02/28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RecordService extends Service implements ScreenRecorderManager.RecordStateCallback {
    public static final String ACTION_LOAD_FILES = "com.zibuyuqing.screenrecorder.LOAD_FILES";
    public static final String ACTION_START_RECORDING = "com.zibuyuqing.screenrecorder.START_RECORDING";
    public static final String ACTION_STOP_RECORDING = "com.zibuyuqing.screenrecorder.STOP_RECORDING";
    public static final String ACTION_SHOW_CONTROLLER = "com.zibuyuqing.screenrecorder.SHOW_CONTROLLER";
    public static final String ACTION_DISMISS_CONTROLLER = "com.zibuyuqing.screenrecorder.DISMISS_CONTROLLER";
    private Context mContext;
    private RecordControllerWindowManager mRecordControllerWinMgr;
    private ScreenRecorderManager mScreenRecordMgr;
    private CountTimerWindow mCountTimerWindow;
    private SharedPreferenceHelper mPreferenceHelper;
    private RecordFilesManager mFilesManager;
    private Config mConfig;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mRecordControllerWinMgr = RecordControllerWindowManager.getInstance(this);
        mScreenRecordMgr = ScreenRecorderManager.getInstance(this);
        mFilesManager = RecordFilesManager.getInstance(this,Utilities.getFileDir());
        mPreferenceHelper = SharedPreferenceHelper.from(this,Config.PREFERENCE_FILE_NAME);
        mCountTimerWindow = new CountTimerWindow(mContext, new CountTimerWindow.OnCountFinishListener() {
            @Override
            public void onCountFinish() {
                mCountTimerWindow.hide();
                startRecord();
            }
        });
    }
    private void initConfig(){
        mConfig = new Config();
        int resolutionSelection = mPreferenceHelper.getIntValue(Config.PREF_KEY_RESOLUTION,Config.CONFIG_DEFAULT_RESOLUTION);
        switch (resolutionSelection){
            case 0:
                mConfig.setRecordWidth(1080);
                mConfig.setRecordHeight(1920);
                break;
            case 1:
                mConfig.setRecordWidth(720);
                mConfig.setRecordHeight(1280);
                break;
            case 2:
                mConfig.setRecordWidth(480);
                mConfig.setRecordHeight(800);
                break;
        }
        int videoQualitySelection = mPreferenceHelper.getIntValue(Config.PREF_KEY_VIDEO_QUALITY,Config.CONFIG_DEFAULT_VIDEO_QUALITY);
        int intMbps = 1024 * 1024;
        switch (videoQualitySelection){
            case 0:
                mConfig.setVideoEncodingBitRate(24 * intMbps);
                break;
            case 1:
                mConfig.setVideoEncodingBitRate(12 * intMbps);
                break;
            case 2:
                mConfig.setVideoEncodingBitRate(6 * intMbps);
                break;
            case 3:
                mConfig.setVideoEncodingBitRate(3 * intMbps);
                break;
            case 4:
                mConfig.setVideoEncodingBitRate(1 * intMbps);
                break;
        }

        int frameRate = mPreferenceHelper.getIntValue(Config.PREF_KEY_FRAME_RATE,Config.CONFIG_DEFAULT_FRAME_RATE);
        switch (frameRate){
            case 0:
                mConfig.setVideoFrameRate(15);
                break;
            case 1:
                mConfig.setVideoFrameRate(24);
                break;
            case 2:
                mConfig.setVideoFrameRate(30);
                break;
        }
        int audioSourceSelection = mPreferenceHelper.getIntValue(Config.PREF_KEY_AUDIO_SOURCE,Config.CONFIG_DEFAULT_AUDIO_SOURCE);
        mConfig.setAudioSource(audioSourceSelection);
        mConfig.setSavePath(Utilities.getFileDir() + Utilities.getFileName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onRecordServiceStart(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(mCountTimerWindow != null){
            mCountTimerWindow.destroy();
            mCountTimerWindow = null;
        }
        if(mRecordControllerWinMgr != null){
            mRecordControllerWinMgr.removeRecordControllerWindow();
            mRecordControllerWinMgr = null;
        }
        if(mFilesManager != null){
            mFilesManager.stopWatchingFiles();
            mFilesManager = null;
        }
        if(mScreenRecordMgr != null){
            mScreenRecordMgr.stopRecord(true);
        }
        super.onDestroy();
    }

    public static void loadFiles(Context context){
        Intent starter = new Intent(context, RecordService.class);
        starter.setAction(ACTION_LOAD_FILES);
        context.startService(starter);
    }
    public static void showController(Context context) {
        Intent starter = new Intent(context, RecordService.class);
        starter.setAction(ACTION_SHOW_CONTROLLER);
        context.startService(starter);
    }
    public static void dismissController(Context context){
        Intent starter = new Intent(context, RecordService.class);
        starter.setAction(ACTION_DISMISS_CONTROLLER);
        context.startService(starter);
    }
    public static void startRecord(Context context){
        Intent starter = new Intent(context, RecordService.class);
        starter.setAction(ACTION_START_RECORDING);
        context.startService(starter);
    }
    public static void stopRecord(Context context){
        Intent starter = new Intent(context, RecordService.class);
        starter.setAction(ACTION_STOP_RECORDING);
        context.startService(starter);
    }
    private void onRecordServiceStart(Intent intent){
        if(intent != null){
            String action = intent.getAction();
            if(ACTION_START_RECORDING.equals(action)){
                boolean showCountdown = mPreferenceHelper.getBooleanValue(
                        Config.PREF_KEY_SHOW_COUNTDOWN_PRE_RECORD,Config.CONFIG_DEFAULT_SHOW_COUNTDOWN_PRE_RECORD);
                if(showCountdown) {
                    startCountDown();
                } else {
                    startRecord();
                }
            } else if(ACTION_SHOW_CONTROLLER.equals(action)){
                showRecordController();
            } else if(ACTION_STOP_RECORDING.equals(action)){
                stopRecordIfNecessary();
            } else if(ACTION_LOAD_FILES.equals(action)){
                loadVideoFiles();
            } else if(ACTION_DISMISS_CONTROLLER.equals(action)){
                dismissControllerIfNecessary();
            }
        }
    }

    private void dismissControllerIfNecessary() {
        if(mRecordControllerWinMgr != null){
            mRecordControllerWinMgr.removeRecordControllerWindow();
        }
    }

    private void loadVideoFiles() {
        if(mScreenRecordMgr.isRecording()){
            return;
        }
        mFilesManager.loadFiles();
        mFilesManager.startWatchingFiles();
    }

    private void stopRecordIfNecessary(){
        mRecordControllerWinMgr.resetRecordState();
        mScreenRecordMgr.stopRecord(false);
    }
    private void startCountDown(){
        mCountTimerWindow.show();
    }
    private void checkPermission(){
        NoDisplayActivity.start(this);
    }
    private void startRecord(){
        initConfig();
        mScreenRecordMgr.startRecord(mConfig,this);
    }
    private void stopRecord(){
        mScreenRecordMgr.stopRecord(false);
        mFilesManager.flushFiles();
        boolean showVideoList = mPreferenceHelper.getBooleanValue(
                Config.PREF_KEY_SHOW_VIDEO_LIST_WEHN_STOP,Config.CONFIG_DEFAULT_SHOW_VIDEO_LIST_WEHN_STOP);
        if(showVideoList){
            MainActivity.start(this);
        }
    }

    private void showRecordController(){
        if(mRecordControllerWinMgr.hasShown()){
            return;
        }
        mRecordControllerWinMgr.showRecordControllerWindow(mContext,
                new RecordControllerWindow.OnRecordStateChangeListener() {
            @Override
            public void onStart() {
                checkPermission();
            }

            @Override
            public void onStop() {
                stopRecord();
            }

                    @Override
                    public void onClose() {
                        mRecordControllerWinMgr.removeRecordControllerWindow();
                    }
                });
    }

    @Override
    public void onRecordStart() {
        mRecordControllerWinMgr.switchToRecordingState();
    }

    @Override
    public void onRecordStop(long duration) {

    }

    @Override
    public void onError(String msg) {

    }
}
