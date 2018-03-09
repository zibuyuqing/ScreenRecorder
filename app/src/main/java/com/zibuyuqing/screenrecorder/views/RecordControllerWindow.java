package com.zibuyuqing.screenrecorder.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zibuyuqing.screenrecorder.R;
import com.zibuyuqing.screenrecorder.settings.Config;
import com.zibuyuqing.screenrecorder.settings.SharedPreferenceHelper;
import com.zibuyuqing.screenrecorder.utils.Utilities;
import com.zibuyuqing.screenrecorder.utils.ViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/02/27
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RecordControllerWindow extends LinearLayout {
    private static final String LOG_TAG = RecordControllerWindow.class.getSimpleName();
    private static final long TRANSLUCENT_DELAY = 3000;
    private static final long FADE_OUT_DELAY = 3000;
    private static final String PREF_KEY_INITIAL_POS_X = "key_initial_pos_x";
    private static final String PREF_KEY_INITIAL_POS_Y = "key_initial_pos_y";
    private static final int DEFAULT_INITIAL_POS_X = 0;
    private static final int DEFAULT_INITIAL_POS_Y = 100;

    private static final int RECORD_STATE_RUNNING = 1;
    private static final int RECORD_STATE_STOP = -1;
    private static final int RECORD_STATE_PREPARING = 2;

    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private View mRecorderControlView;
    private Chronometer mTimer;
    private View mRecordSwitchView;
    private ImageView mIvClose;
    private ImageView mIvSwitchStart;
    private ImageView mIvSwitchStop;
    private int mWidth;
    private int mHeight;
    private int mRecordState = RECORD_STATE_STOP;
    private boolean isSwitchBtnTouched = false;
    private boolean isCloseBtnTouched = false;
    private boolean isMoving = false;
    private boolean isControllerReset = false;
    private boolean isAttached = true;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private float mLastMotionX;
    private float mLastMotionY;
    private int mTouchSlop;
    private int mScreenWidth;
    private int mScreenHeight;
    private float animateProgress;
    private OnRecordStateChangeListener mListener;
    private Handler mHandler;
    private List<ObjectAnimator> mRunningAnimatorList = new ArrayList<>();
    private SharedPreferenceHelper mPreferenceHelper;
    private int mInitPosX = 0;
    private int mInitPosY = 0;
    private final Runnable mTranslucentRunnable = new Runnable() {
        @Override
        public void run() {
            isControllerReset = false;
            final ObjectAnimator translucentAnim = ObjectAnimator.ofFloat(
                    RecordControllerWindow.this,"alpha",1.0f,0.3f);
            translucentAnim.setDuration(300);
            translucentAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if(!mRunningAnimatorList.contains(translucentAnim)){
                        mRunningAnimatorList.add(translucentAnim);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    mRunningAnimatorList.remove(translucentAnim);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mRunningAnimatorList.remove(translucentAnim);
                }
            });
            translucentAnim.start();
        }
    };

    private final Runnable mFadeOutRunnable = new Runnable() {
        @Override
        public void run() {
            isControllerReset = false;
            int halfScreen = mScreenWidth >> 1;
            final boolean toLeft = Math.abs(mLastMotionX) < halfScreen;
            final int toX = toLeft ? 0 : mScreenWidth;
            Log.e(LOG_TAG,"toLeft =:" + toLeft +",toX =:" + toX +",mLastMotionX =:" + mLastMotionX +",halfScreen =:" + halfScreen);

            // 隐藏悬浮窗
            final int toTransX = toLeft ? (int) (- mWidth) : (int) (mWidth);
            PropertyValuesHolder transX = PropertyValuesHolder.ofFloat("translationX",0,toTransX);
            final int toDegree = toLeft ? -90 : 90;
            PropertyValuesHolder rotate = PropertyValuesHolder.ofFloat("rotation",0,toDegree);
            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha",1.0f,0.3f);
            final ObjectAnimator windowAnimator = ObjectAnimator.ofPropertyValuesHolder(RecordControllerWindow.this,rotate,transX,alpha);
            windowAnimator.setStartDelay(500);
            windowAnimator.setDuration(500);
            windowAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if(!isControllerReset) {
                        if (!mRunningAnimatorList.contains(windowAnimator)) {
                            mRunningAnimatorList.add(windowAnimator);
                        }
                    } else {
                        if(windowAnimator.isRunning()) {
                            windowAnimator.cancel();
                        }
                    }
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mRunningAnimatorList.remove(windowAnimator);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    mRunningAnimatorList.remove(windowAnimator);
                }
            });
            // 将悬浮窗靠边
            PropertyValuesHolder animateProgress = PropertyValuesHolder.ofFloat("animateProgress",1.0f,0.0f);
            final ObjectAnimator viewAnimator = ObjectAnimator.ofPropertyValuesHolder(RecordControllerWindow.this,animateProgress);
            viewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float rate = ((Float) animation.getAnimatedValue()).floatValue();
                    Log.e(LOG_TAG,"rate =:" + rate);
                    if(toLeft) {
                        mParams.x = (int) (toX + Math.abs(toX - mLastMotionX) * rate);
                    } else {
                        mParams.x = (int) (mLastMotionX + Math.abs(toX - mLastMotionX) * (1.0f - rate));
                    }
                    if(isAttached) {
                        mWindowManager.updateViewLayout(RecordControllerWindow.this, mParams);
                    }
                }
            });
            viewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if(!mRunningAnimatorList.contains(viewAnimator)){
                        mRunningAnimatorList.add(viewAnimator);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    Log.e(LOG_TAG,"onAnimationEnd :: viewAnimator =:" + viewAnimator.isRunning());
                    if(!isControllerReset) {
                        windowAnimator.start();
                    }
                    mRunningAnimatorList.remove(viewAnimator);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    Log.e(LOG_TAG,"onAnimationCancel :: viewAnimator =:" + viewAnimator.isRunning());
                    mRunningAnimatorList.remove(viewAnimator);
                }
            });
            viewAnimator.setDuration(500);
            viewAnimator.start()                          ;
        }
    };
    private void setAnimateProgress(float progress){
        animateProgress = progress;
    }

    public RecordControllerWindow(Context context) {
        this(context, null);
    }

    public RecordControllerWindow(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordControllerWindow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mPreferenceHelper = SharedPreferenceHelper.from(getContext(), Config.PREFERENCE_FILE_NAME);
        mInitPosX = mPreferenceHelper.getIntValue(PREF_KEY_INITIAL_POS_X,DEFAULT_INITIAL_POS_X);
        mInitPosY = mPreferenceHelper.getIntValue(PREF_KEY_INITIAL_POS_Y,DEFAULT_INITIAL_POS_Y);
        mLastMotionX = mInitPosX;
        mLastMotionY = mInitPosY;
        mHandler = new Handler(Looper.getMainLooper());
        ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop() / 2;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mRecorderControlView = LayoutInflater.from(mContext).inflate(R.layout.layout_recorder_control_view, this);
        mRecordSwitchView = mRecorderControlView.findViewById(R.id.fl_record_switch_wrapper);
        mRecordSwitchView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRecordState == RECORD_STATE_STOP) {
                    startRecord();
                    mRecordState = RECORD_STATE_PREPARING;
                } else if(mRecordState == RECORD_STATE_RUNNING){
                    stopRecord();
                }
            }
        });
        mIvClose = mRecorderControlView.findViewById(R.id.iv_close_controller);
        mIvClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mIvSwitchStart = mRecorderControlView.findViewById(R.id.iv_record_switch_start);
        mIvSwitchStop = mRecorderControlView.findViewById(R.id.iv_record_switch_stop);
        mIvSwitchStop.setVisibility(GONE);
        mTimer = mRecorderControlView.findViewById(R.id.recorder_timer);
        mTimer.setVisibility(GONE);
        mWidth = mContext.getResources().getDimensionPixelSize(R.dimen.dimen_40dp);
        mHeight = mContext.getResources().getDimensionPixelSize(R.dimen.dimen_76dp);
        mScreenWidth = ViewUtil.getScreenSize(mContext).x;
        mScreenHeight = ViewUtil.getScreenSize(mContext).y;
        mParams = new WindowManager.LayoutParams();
        if (Utilities.canUseToastType()) {
            mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN // 全屏
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS // 覆盖到status bar
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION // 覆盖到导航栏

                // 以下属性设置加载我们圆角window 不抢焦点,不拦截事件
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.width = mWidth;
        mParams.height = mHeight;
        mParams.x = mInitPosX;
        mParams.y = mInitPosY;
        setLayoutParams(mParams);
        setOrientation(LinearLayout.VERTICAL);
        setRotation(0.0f);
        fadeOutController();
        isAttached = true;
    }

    private void dismiss() {
        if(isAttached) {
            if(mListener != null){
                mListener.onClose();
            }
            isAttached = false;
        }
    }


    public void setRecordStateChangedListener(OnRecordStateChangeListener listener){
        mListener = listener;
    }
    public WindowManager.LayoutParams getWindowLayoutParams(){
        return mParams;
    }
    public WindowManager getWindowManager(){
        return mWindowManager;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        // 获取相对屏幕的坐标，即以屏幕左上角为原点
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // 获取相对View的坐标，即以此View左上角为原点
                mInitialMotionX  = event.getX();
                mInitialMotionY  = event.getY();
                Log.e(LOG_TAG,"onTouchEvent action = ACTION_DOWN :" + action+",mInitialMotionX =:"+ mInitialMotionX+",mInitialMotionY =:" + mInitialMotionY);
                isMoving = false;
                Rect switchBtnRect = new Rect();
                Rect closeBtnRect = new Rect();
                mRecordSwitchView.getHitRect(switchBtnRect);
                mIvClose.getHitRect(closeBtnRect);
                if(switchBtnRect.contains((int)mInitialMotionX,(int)mInitialMotionY)){
                    isSwitchBtnTouched = true;
                }
                if(closeBtnRect.contains((int)mInitialMotionX,(int)mInitialMotionY)){
                    isCloseBtnTouched = true;
                }
                mHandler.removeCallbacks(mTranslucentRunnable);
                mHandler.removeCallbacks(mFadeOutRunnable);
                resetController();
                break;
            case MotionEvent.ACTION_MOVE:
                int xMove = (int) Math.abs(x - mLastMotionX - mInitialMotionX);
                int yMove = (int)Math.abs(y - mLastMotionY - mInitialMotionY);
                if(xMove > mTouchSlop || yMove > mTouchSlop){
                    isMoving = true;
                    mParams.x = (int) Math.abs(x - mInitialMotionX);
                    mParams.y = (int) Math.abs(y - mInitialMotionY);
                    mWindowManager.updateViewLayout(this,mParams);
                    mLastMotionX = mParams.x;
                    mLastMotionY = mParams.y;
                }
                break;
            case MotionEvent.ACTION_UP:
                mInitialMotionX = mInitialMotionY = 0;
                mWindowManager.updateViewLayout(this,mParams);
                Log.e(LOG_TAG,"onTouchEvent action = ACTION_UP isMoving =:" + isMoving +",isSwitchBtnTouched =:" + isSwitchBtnTouched +",isCloseBtnTouched =:" + isCloseBtnTouched);
                if(!isMoving){
                    if(isSwitchBtnTouched) {
                        if (mRecordState == RECORD_STATE_STOP) {
                            startRecord();
                            mRecordState = RECORD_STATE_PREPARING;
                        } else if (mRecordState == RECORD_STATE_RUNNING) {
                            stopRecord();
                        }
                    }
                    if(isCloseBtnTouched){
                        dismiss();
                    }
                }
                if(mRecordState == RECORD_STATE_STOP){
                    fadeOutController();
                }
                if(mRecordState == RECORD_STATE_RUNNING){
                    mHandler.postDelayed(mTranslucentRunnable,TRANSLUCENT_DELAY);
                }
                resetTouchState();
                updateControllerPosition();
                break;
        }
        return true;
    }
    private void updateControllerPosition(){
        mPreferenceHelper.editorIntValue(PREF_KEY_INITIAL_POS_X, (int) mLastMotionX);
        mPreferenceHelper.editorIntValue(PREF_KEY_INITIAL_POS_Y, (int) mLastMotionY);
    }
    private void resetTouchState(){
        isMoving = false;
        isSwitchBtnTouched = false;
        isCloseBtnTouched = false;
    }
    public void resetRecordState(){
        mRecordState = RECORD_STATE_STOP;
        fadeOutController();
    }
    private void resetController(){
        isControllerReset = true;
        clearViewAnimation();
        setRotation(0);
        setAlpha(1.0f);
        setTranslationX(0);
    }
    private void clearViewAnimation(){
        Log.e(LOG_TAG,"clearViewAnimation");
        for(ObjectAnimator animator : mRunningAnimatorList){
            Log.e(LOG_TAG,"animator clearViewAnimation animator =:" + animator);
            animator.cancel();
        }
    }
    private void fadeInController(){
        // TODO: 2018/2/28
    }

    private void fadeOutController(){
        mHandler.postDelayed(mFadeOutRunnable,FADE_OUT_DELAY);
    }
    private void translucentController(){
        mHandler.postDelayed(mTranslucentRunnable,TRANSLUCENT_DELAY);
    }

    private void stopRecord(){
        if(mListener != null){
            mListener.onStop();
        }
        switchToNormalState();
    }
    private void startRecord(){
        if(mListener != null){
            mListener.onStart();
        }
    }

    private void switchToNormalState(){
        mRecordState = RECORD_STATE_STOP;
        mIvClose.setVisibility(VISIBLE);
        mTimer.setVisibility(GONE);
        mTimer.setBase(SystemClock.elapsedRealtime());
        mTimer.stop();
        mHandler.removeCallbacks(mTranslucentRunnable);
        mIvSwitchStart.setVisibility(VISIBLE);
        mIvSwitchStart.setScaleX(1.0f);
        mIvSwitchStart.setScaleY(1.0f);
        mIvSwitchStop.setVisibility(GONE);
    }
    public void switchToRecordingState() {
        mRecordState = RECORD_STATE_RUNNING;
        mIvClose.setVisibility(GONE);
        mTimer.setVisibility(VISIBLE);
        mTimer.setBase(SystemClock.elapsedRealtime());
        mTimer.start();
        mHandler.removeCallbacks(mFadeOutRunnable);
        translucentController();
        animateSwitchToRecordingState();
    }

    private void animateSwitchToRecordingState(){
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX",1.0f,0.2f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY",1.0f,0.2f);
        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(mIvSwitchStart,scaleX,scaleY);
        scaleAnimator.setDuration(300);
        scaleAnimator.start();
        mIvSwitchStop.setVisibility(VISIBLE);
        scaleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIvSwitchStart.setVisibility(GONE);
            }
        });
    }

    public interface OnRecordStateChangeListener {
        void onStart();
        void onStop();
        void onClose();
    }
}
