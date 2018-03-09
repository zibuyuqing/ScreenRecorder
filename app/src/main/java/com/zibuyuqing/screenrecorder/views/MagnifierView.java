package com.zibuyuqing.screenrecorder.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zibuyuqing.screenrecorder.R;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/03/08
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MagnifierView extends View {
    private Bitmap bitmap;
    private Paint paint;
    private int width;
    private int height;
    private Bitmap scaleBitmap;
    private int SCALE = 2;//缩放的倍数
    private ShapeDrawable drawable;
    private int radius = 200;//圆的半径
    private Matrix matrix;
    boolean hasBmp = false;
    public MagnifierView(Context context) {
        this(context, null);
    }

    public MagnifierView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagnifierView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {


    }

    public void setBitmap(Bitmap bitmap){
        Log.e("hahaha","bitmap =:" + bitmap);
        if(bitmap == null){
            return;
        }
        hasBmp = true;
        matrix = new Matrix();
        paint = new Paint();
        paint.setStrokeWidth(8);
        this.bitmap = bitmap;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        scaleBitmap = Bitmap.createScaledBitmap(bitmap, width * SCALE, height * SCALE, true);
        BitmapShader shader = new BitmapShader(scaleBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setShader(shader);
        //设置drawable显示的区域,这个上面已经说过,不设置区域 图片是显示不出来的
        drawable.setBounds(0, 0, radius * 2, radius * 2);
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(hasBmp && bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
            drawable.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        matrix.setTranslate(radius - x * SCALE, radius - y * SCALE);
        //平移到绘制shader的起始位置
        drawable.getPaint().getShader().setLocalMatrix(matrix);
        drawable.setBounds(x - radius, y - radius, x + radius, y + radius);
        invalidate();
        return true;
    }
}