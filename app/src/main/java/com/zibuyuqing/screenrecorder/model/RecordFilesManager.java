package com.zibuyuqing.screenrecorder.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.zibuyuqing.screenrecorder.model.bean.ScreenRecordVideoInfo;
import com.zibuyuqing.screenrecorder.services.FilesObserver;
import com.zibuyuqing.screenrecorder.utils.Utilities;
import com.zibuyuqing.screenrecorder.utils.ViewUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/03/06
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RecordFilesManager implements FilesObserver.FileListener {
    private static final String TAG = RecordFilesManager.class.getSimpleName();
    private FilesObserver mFilesObserver;
    private static volatile RecordFilesManager sInstance;
    private Context mContext;
    private String mDstPath;
    private FilesLoadTask mFilesLoader;
    private ArrayList<ScreenRecordVideoInfo> mVideoInfoList = new ArrayList<>();
    private ArrayList<String> mFilesWaitingForFlush = new ArrayList<>();
    private static final Object sLock = new Object();
    private FilesLoadStateListener mLoadListener;
    private FilesDeleteStateListener mDeleteListener;
    private int mScreenWidth,mScreenHeight;
    private boolean isDeleteFilesProactively = false;
    private RecordFilesManager(Context context,String dstPath){
        mContext = context;
        mDstPath = dstPath;
        mFilesObserver = new FilesObserver(mContext,mDstPath,this);
        mScreenWidth = ViewUtil.getScreenSize(context).x;
        mScreenHeight = ViewUtil.getScreenSize(context).y;
    }

    public void registerFilesLoadStateListener(FilesLoadStateListener listener){
        mLoadListener = listener;
    }
    public void registerFilesDeleteStateListener(FilesDeleteStateListener listener){
        mDeleteListener = listener;
    }
    public void deleteFiles(ArrayList<ScreenRecordVideoInfo> fileInfos,boolean proactive){
        isDeleteFilesProactively = proactive;
        new FilesDeleteTask(fileInfos).execute();
    }
    public void loadFiles() {
        if(mFilesLoader == null){
            mFilesLoader = new FilesLoadTask();
        }
        mFilesLoader.execute();
    }
    public void cancelLoad(){
        if(mFilesLoader != null && !mFilesLoader.isCancelled()){
            mFilesLoader.cancel(false);
            mFilesLoader = null;
        }
    }
    public static RecordFilesManager getInstance(Context context,String dstPath) {
        if(sInstance == null){
            synchronized (RecordFilesManager.class){
                if(sInstance == null){
                    sInstance = new RecordFilesManager(context,dstPath);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onFileCreate(String path) {
        Log.e(TAG,"onFileCreate :: path = :" + path);
        mFilesWaitingForFlush.add(Utilities.getFileDir() + path);
    }

    public void flushFiles(){
        for(String filePath : mFilesWaitingForFlush){
            File file = new File(filePath);
            mVideoInfoList.add(new ScreenRecordVideoInfo(file,mScreenWidth  / 2,mScreenHeight / 2));
        }
        mFilesWaitingForFlush.clear();
        if(mLoadListener != null){
            mLoadListener.finishLoad(mVideoInfoList);
        }
    }
    public void startWatchingFiles(){
        mFilesObserver.startWatching();
    }
    public void stopWatchingFiles(){
        mFilesObserver.stopWatching();
    }
    @Override
    public void onFileDelete(String path) {
        Log.e(TAG,"onFileDelete :: path = :" + path +", isDeleteFilesProactively =:" +isDeleteFilesProactively);
        if(!isDeleteFilesProactively) {
            removeVideoInfoByPath(Utilities.getFileDir() + path);
        }
    }

    private void removeVideoInfoByPath(String path){
        for(ScreenRecordVideoInfo info : mVideoInfoList){
            if(path.equals(info.getFilePath())){
                mVideoInfoList.remove(info);
                break;
            }
        }
    }
    @Override
    public void onLowMemory() {

    }
    private void clearFileInfoList(){
        mVideoInfoList.clear();
    }
    private class FilesDeleteTask extends AsyncTask<Void,Void,Void>{
        ArrayList<ScreenRecordVideoInfo> deleteFileInfos;
        public FilesDeleteTask(ArrayList<ScreenRecordVideoInfo> infos){
            deleteFileInfos = new ArrayList<>(infos);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(mDeleteListener != null){
                mDeleteListener.startDelete();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            File file;
            for(ScreenRecordVideoInfo info :deleteFileInfos){
                file = info.getFile();
                if(file.exists()){
                    file.delete();
                }
                mVideoInfoList.remove(info);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(mDeleteListener != null){
                mDeleteListener.finishDelete(mVideoInfoList);
            }
            isDeleteFilesProactively = false;
        }
    }
    private class FilesLoadTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "start load");
            if(mLoadListener != null){
                mLoadListener.startLoad();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            synchronized (sLock) {
                File[] files = null;
                int total = 0;
                File dir = new File(mDstPath);
                if (dir.exists()) {
                    files = dir.listFiles();
                    if (files == null) {
                        return null;
                    }
                } else {
                    return null;
                }
                Log.e(TAG,"doInBackground :: files =:" + files);
                total = files.length;
                clearFileInfoList();
                for (int i = 0; i < total; i++) {
                    if (isCancelled()) {
                        Log.e(TAG, "cancel");
                        clearFileInfoList();
                        return null;
                    }
                    ScreenRecordVideoInfo recordVideoInfo = new ScreenRecordVideoInfo(files[i],mScreenWidth / 2,mScreenHeight / 2);
                    Log.e(TAG,"recordVideoInfo =:" + recordVideoInfo.toString());
                    mVideoInfoList.add(recordVideoInfo);
                }

                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(mLoadListener != null){
                mLoadListener.finishLoad(mVideoInfoList);
            }
        }
    }
    public static abstract interface FilesLoadStateListener{
        void startLoad();
        void finishLoad(ArrayList<ScreenRecordVideoInfo> data);
    }
    public static abstract interface FilesDeleteStateListener{
        void startDelete();
        void onDelete(float progress);
        void finishDelete(ArrayList<ScreenRecordVideoInfo> data);
    }
}
