package com.zibuyuqing.screenrecorder.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.zibuyuqing.screenrecorder.R;
import com.zibuyuqing.screenrecorder.utils.Utilities;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/02/27
 *     desc   : 倒计时窗口
 *     version: 1.0
 * </pre>
 */
public class CountTimerWindow {
    private Context mContext;
    private View mCountViewWrapper;
    private TextView mTvCountTimer;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private CountTimer mCountTimer;
    private OnCountFinishListener mListener;

    public CountTimerWindow(Context context, OnCountFinishListener listener) {
        mContext = context;
        mListener = listener;
        init();
    }

    private void init() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
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
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mCountViewWrapper = LayoutInflater.from(mContext).inflate(R.layout.layout_count_timer, null);
        mTvCountTimer = mCountViewWrapper.findViewById(R.id.tv_count_timer);
    }

    public void show() {
        mWindowManager.addView(mCountViewWrapper, mParams);
        mCountTimer = new CountTimer(4000, 1000, mTvCountTimer);
        mCountTimer.start();
    }

    public void hide() {
        mWindowManager.removeView(mCountViewWrapper);
    }

    public void destroy() {
        mWindowManager.removeView(mCountViewWrapper);
    }

    private class CountTimer extends CountDownTimer {
        private static final long DEFAULT_COUNT_DOWN_INTERVAL = 1000;
        private static final int DEFAULT_TIME_COUNT = 3;

        private TextView tvCountTimer;

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public CountTimer(long millisInFuture, long countDownInterval, TextView textView) {
            super(millisInFuture, countDownInterval);
            tvCountTimer = textView;
        }

        public CountTimer(long millisInFuture, TextView textView) {
            this(millisInFuture, DEFAULT_COUNT_DOWN_INTERVAL, textView);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            String time = millisUntilFinished / 1000 + "";
            animateShow(time);
        }

        @Override
        public void onFinish() {
            if (mListener != null) {
                mListener.onCountFinish();
            }
        }

        private void animateShow(String time) {
            tvCountTimer.setText(time);
            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.5f, 1.0f);
            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1.0f);
            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1.0f);
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(tvCountTimer, alpha, scaleX, scaleY);
            animator.setDuration(400);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animateHide();
                }
            });
            animator.start();
        }

        private void animateHide() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(tvCountTimer, "alpha", 1.0f, 0.0f);
            animator.setDuration(400);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    tvCountTimer.setText("");
                }
            });
            animator.start();
        }
    }

    public interface OnCountFinishListener {
        void onCountFinish();
    }
}
