package com.zibuyuqing.screenrecorder.services;

import android.content.Context;
import android.os.FileObserver;
import android.support.annotation.Nullable;

import com.zibuyuqing.screenrecorder.utils.Utilities;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/03/06
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class FilesObserver extends FileObserver{

    private String mDstPath;
    private FileListener mListener;
    private Context mContext;
    public FilesObserver(Context context,String path,FileListener listener){
        super(path);
        mContext = context;
        mDstPath = path;
        mListener = listener;
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        switch (event){
            case CREATE:
                if(mListener != null){
                    mListener.onFileCreate(path);
                }
                break;
            case DELETE:
                if(mListener != null){
                    mListener.onFileDelete(path);
                }
                break;
            case MODIFY:
                if(!Utilities.isAvailableMemory(mContext)){
                    if(mListener != null){
                        mListener.onLowMemory();
                    }
                }
                break;
        }
    }
    public abstract static interface FileListener{
        void  onFileCreate(String path);
        void  onFileDelete(String path);
        void onLowMemory();
    }
}
