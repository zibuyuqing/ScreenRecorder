package com.zibuyuqing.screenrecorder.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zibuyuqing.screenrecorder.utils.ViewUtil;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/03/09
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public abstract class  MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    int mScreenWidth;
    int mScreenHeight;
    SurfaceHolder surfaceHolder;
    public MySurfaceView(Context context) {
        this(context,null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth = ViewUtil.getScreenSize(getContext()).x;
        mScreenHeight = ViewUtil.getScreenSize(getContext()).y;
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(800,800);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(new MyThread()).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    class MyThread implements Runnable {
        @Override
        public void run() {
            Canvas canvas = surfaceHolder.lockCanvas(null);//获取画布
            doDraw(canvas);
            surfaceHolder.unlockCanvasAndPost(canvas);//解锁画布，提交画好的图像

        }
    }
    //将绘制图案的方法抽象出来，让子类实现，调用getBitmap方法时就会调用此方法
    protected abstract void doDraw(Canvas canvas);

    //调用该方法将doDraw绘制的图案绘制在自己的canvas上
    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        doDraw(canvas);
        return bitmap;
    }
}
