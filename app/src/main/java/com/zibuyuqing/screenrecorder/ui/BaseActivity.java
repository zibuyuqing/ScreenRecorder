package com.zibuyuqing.screenrecorder.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.zibuyuqing.screenrecorder.R;

/**
 * Created by lingy on 2017-10-21.
 */

public abstract class BaseActivity extends AppCompatActivity {
    protected abstract void init();

    protected abstract boolean canBack();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.BassActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(providedLayoutId());
        AppBarLayout appBar;
        Toolbar toolbar;
        appBar = (AppBarLayout) findViewById(R.id.app_bar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null && appBar != null) {
            setSupportActionBar(toolbar); //把Toolbar当做ActionBar给设置
            if(customToolbar()) {
                toolbar.addView(getToolbarContent());
            }
            if (canBack()) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null)
                    actionBar.setDisplayHomeAsUpEnabled(true);//设置ActionBar一个返回箭头，主界面没有，次级界面有
            }
            if (Build.VERSION.SDK_INT >= 21) {
                appBar.setElevation(10.6f);//Z轴浮动
            }
        }
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, null));
        init();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showTips(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected abstract int providedLayoutId();

    protected abstract boolean customToolbar();

    protected abstract View getToolbarContent();

}
