package com.zibuyuqing.screenrecorder.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.zibuyuqing.screenrecorder.model.ScreenRecorderManager;
import com.zibuyuqing.screenrecorder.services.RecordService;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/03/02
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class NoDisplayActivity extends Activity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    ScreenRecorderManager mScreenRecorderManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        requestPermission();
    }

    /**
     * 一个像素activity
     */
    private void initWindow(){
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PERMISSION_REQUEST_CODE && resultCode == RESULT_OK){
            mScreenRecorderManager.setMediaProject(
                    mScreenRecorderManager.getMediaProjectionManager().getMediaProjection(resultCode,data));
            RecordService.startRecord(NoDisplayActivity.this);
            NoDisplayActivity.this.finish();
        } else {
            RecordService.stopRecord(NoDisplayActivity.this);
            NoDisplayActivity.this.finish();
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, NoDisplayActivity.class);
        context.startActivity(starter);
    }
    private void requestPermission(){
        mScreenRecorderManager = ScreenRecorderManager.getInstance(this);
        mScreenRecorderManager.requestPermission(this,PERMISSION_REQUEST_CODE);
    }
}
