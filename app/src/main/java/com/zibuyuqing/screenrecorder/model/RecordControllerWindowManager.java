package com.zibuyuqing.screenrecorder.model;

import android.content.Context;
import android.view.WindowManager;

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
public class RecordControllerWindowManager {
    private volatile static RecordControllerWindowManager sInstance;
    private Context mContext;
    private WindowManager mWindowManager;
    private RecordControllerWindow mRecordControllerWindow;
    private boolean isShowing = false;
    private RecordControllerWindowManager(Context context){
        mContext = context;
    }
    public static RecordControllerWindowManager getInstance(Context context){
        if(sInstance == null){
            synchronized (RecordControllerWindowManager.class){
                if(sInstance == null){
                    sInstance = new RecordControllerWindowManager(context);
                }
            }
        }
        return sInstance;
    }
    public static void startRecordService(Context context,
                                          RecordControllerWindow.OnRecordStateChangeListener listener){
        getInstance(context).showRecordControllerWindow(context,listener);

    }
    public boolean hasShown(){
        return isShowing;
    }
    public void showRecordControllerWindow(Context context,
                                          RecordControllerWindow.OnRecordStateChangeListener listener){
        if(mRecordControllerWindow == null){
            mRecordControllerWindow = new RecordControllerWindow(context);
        }
        mRecordControllerWindow.setRecordStateChangedListener(listener);
        getWindowManager().addView(mRecordControllerWindow,mRecordControllerWindow.getWindowLayoutParams());
        isShowing = true;
    }
    public void resetRecordState(){
        mRecordControllerWindow.resetRecordState();
    }
    public void switchToRecordingState(){
        mRecordControllerWindow.switchToRecordingState();
    }
    public void removeRecordControllerWindow(){
        if(mRecordControllerWindow != null){
            getWindowManager().removeView(mRecordControllerWindow);
            mRecordControllerWindow = null;
            isShowing = false;
        }
    }

    private WindowManager getWindowManager(){
        mWindowManager = mRecordControllerWindow.getWindowManager();
        if(mWindowManager == null){
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

}
