package com.zibuyuqing.screenrecorder.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zibuyuqing.screenrecorder.R;
import com.zibuyuqing.screenrecorder.adapter.RecordVideoListAdapter;
import com.zibuyuqing.screenrecorder.model.RecordFilesManager;
import com.zibuyuqing.screenrecorder.model.bean.ScreenRecordVideoInfo;
import com.zibuyuqing.screenrecorder.services.RecordService;
import com.zibuyuqing.screenrecorder.settings.SettingsActivity;
import com.zibuyuqing.screenrecorder.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener, RecordFilesManager.FilesLoadStateListener, RecordVideoListAdapter.OnStateChangedListener, RecordFilesManager.FilesDeleteStateListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final int SYSTEM_ALERT_WINDOW_REQUEST_CODE = 123;
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private final int ACTION_MODE_NORMAL = 0;
    private final int ACTION_MODE_MULTI_SELECT_AND_DELETE = 1;
    private FloatingActionButton mFabBtn;
    private RecordVideoListAdapter mAdapter;
    private RecordFilesManager mFilesManager;
    public ActionMode mActionMode;
    private SelectActionMode mSelectActionMode;
    private boolean mIsSelectedAll = false;
    private int mActionModeType = 0;
    private TextView mSelectionAll;
    private TextView mSelectionCount;
    private ImageView mIvSettings;
    private RecyclerView mRvVideoList;
    private View mEmptyView;
    private RelativeLayout mRlBottomBar;
    private ImageView mIvShare;
    private ImageView mIvDelete;
    private ProgressBar mPbProgress;
    private boolean isAbleAlertWindow = false;
    @Override
    public void init() {
        initViews();
        mFilesManager = RecordFilesManager.getInstance(this, Utilities.getFileDir());
        mFilesManager.registerFilesLoadStateListener(this);
        mFilesManager.registerFilesDeleteStateListener(this);
        boolean enableAccessStorage = checkStoragePermission();
        Log.e(TAG,"init enableAccessStorage :" + enableAccessStorage);
        if(enableAccessStorage){
            RecordService.loadFiles(this);
        }
    }

    @Override
    protected boolean canBack() {
        return false;
    }

    @Override
    public void onBackPressed() {
        if(mActionModeType == ACTION_MODE_MULTI_SELECT_AND_DELETE){
            exitSelectMode();
            return;
        }
        finish();
    }
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission)){
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                if(grantResults == null || grantResults.length <=0){
                    return;
                }
                for(int i = 0; i<grantResults.length; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, getString(R.string.storage_permission_denied), Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                RecordService.loadFiles(this);
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private boolean checkAlertWindowPermission() {
        return Utilities.checkFloatWindowPermission(this);
    }
    private boolean checkStoragePermission() {
        final List<String> permissionsList = new ArrayList<String>();
        addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionsList.size() > 0) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void requestAlertWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SYSTEM_ALERT_WINDOW_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                isAbleAlertWindow = true;
                //showRecordController();
            }
        }
    }

    private void initViews(){
        mRlBottomBar = findViewById(R.id.rl_bottom_bar);
        mIvDelete = findViewById(R.id.iv_delete);
        mIvDelete.setOnClickListener(this);
        mIvShare = findViewById(R.id.iv_share);
        mIvShare.setOnClickListener(this);
        mPbProgress = findViewById(R.id.pb_progress);
        mEmptyView = findViewById(R.id.empty_view);
        mFabBtn = findViewById(R.id.fab_start_record);
        mFabBtn.setOnClickListener(this);
        mIvSettings = findViewById(R.id.iv_settings);
        mIvSettings.setOnClickListener(this);
        mRvVideoList = findViewById(R.id.rv_record_video_list);
        if(mAdapter == null){
            mAdapter = new RecordVideoListAdapter(this);
        }
        RecyclerView.LayoutManager manager = new GridLayoutManager(this,2);
        mRvVideoList.setLayoutManager(manager);
        mAdapter.setOnActionModeChangedListener(this);
        mRvVideoList.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        isAbleAlertWindow = checkAlertWindowPermission();
        if(!isAbleAlertWindow){
            RecordService.dismissController(this);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mFilesManager.cancelLoad();
        super.onDestroy();
    }

    @Override
    protected int providedLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean customToolbar() {
        return true;
    }

    @Override
    protected View getToolbarContent() {
        return getLayoutInflater().inflate(R.layout.layout_main_activity_toolbar,null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_start_record:
                showRecordController();
                break;
            case R.id.iv_settings:
                SettingsActivity.start(MainActivity.this);
                break;
            case R.id.iv_delete:
                if(mActionModeType == ACTION_MODE_MULTI_SELECT_AND_DELETE) {
                    deleteVideoFiles();
                }
                break;
            case R.id.iv_share:
                shareVideo();
                break;
        }
    }

    private void shareVideo() {
        Intent shareIntent = new Intent();
        if(mAdapter.getSelectInfos().size() > 1){
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            ArrayList<Uri> uriList = new ArrayList<>();
            for(ScreenRecordVideoInfo info : mAdapter.getSelectInfos()){
                uriList.add(Uri.fromFile(info.getFile()));
            }
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);//发送多个文件
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uriList);//Intent.EXTRA_STREAM同于传输文件流
            intent.setType("*/*");//多个文件格式
        } else if(mAdapter.getSelectInfos().size() > 0){
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("audio/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(mAdapter.getSelectInfos().get(0).getFile()));
        }
        exitSelectMode();
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    private void animatedShowBottomBar(final boolean show, final View bottomBar, final View fab,boolean animated) {
        if (bottomBar == null || fab == null) {
            return;
        }
        final int height = bottomBar.getHeight();
        if (animated) {
            fab.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
            float startY = show ? 0 : - height;
            float finalY = show ? - height : 0;
            ObjectAnimator bottomBarDrift = ObjectAnimator.ofFloat(bottomBar, "translationY", -finalY, - startY);
            ObjectAnimator fabDrift = ObjectAnimator.ofFloat(fab, "translationY", startY, finalY);
            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(200);
            animatorSet.play(bottomBarDrift).with(fabDrift);
            animatorSet.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (show) {
                        bottomBar.setTranslationY(0);
                        mRvVideoList.setPadding(0,0,0,height);
                    } else {
                        bottomBar.setVisibility(View.GONE);
                        mRvVideoList.setPadding(0,0,0,0);
                        fab.setTranslationY(0);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (show) {
                        bottomBar.setTranslationY(0);
                    } else {
                        fab.setTranslationY(0);
                    }
                }
            });
            animatorSet.start();
        } else {
            if (show) {
                bottomBar.setTranslationY(0);
            } else {
                bottomBar.setVisibility(View.GONE);
                fab.setTranslationY(0);
            }
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }
    private void showRecordController(){
        if(isAbleAlertWindow) {
            RecordService.showController(this);
            mFilesManager.cancelLoad();
            finish();
        } else {
            requestAlertWindowPermission();
        }
    }

    private void deleteVideoFiles(){
        mFilesManager.deleteFiles(mAdapter.getSelectInfos(),true);
    }
    @Override
    public void startLoad() {
        mPbProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void finishLoad(ArrayList<ScreenRecordVideoInfo> data) {
        mPbProgress.setVisibility(View.GONE);
        if(data.size() > 0) {
            mEmptyView.setVisibility(View.GONE);
            mRvVideoList.setVisibility(View.VISIBLE);
            mAdapter.refreshList(data);
        } else {
            mRvVideoList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void startSelect() {
        enterSelectMode();
    }
    public void enterSelectMode() {
        if(mSelectActionMode == null){
            mSelectActionMode = new SelectActionMode();
        }
        animatedShowBottomBar(true,mRlBottomBar,mFabBtn,true);
        mActionMode = startActionMode(mSelectActionMode);
        mActionModeType = ACTION_MODE_MULTI_SELECT_AND_DELETE;
    }
    private void exitSelectMode(){
        if(mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
        animatedShowBottomBar(false,mRlBottomBar,mFabBtn,true);
        mAdapter.exitSelectState();
        mActionModeType = ACTION_MODE_NORMAL;
    }
    @Override
    public void select() {
        updateSelectCount();
    }
    public void updateSelectCount() {
        int selectCount =  mAdapter.getSelectInfos().size();
        if(selectCount == mAdapter.getItemCount()){
            mSelectionAll.setText("全不选");
            mIsSelectedAll = true;
        } else {
            mSelectionAll.setText("全选");
            mIsSelectedAll = false;
        }
        mSelectionCount.setText(selectCount+" ");
    }

    @Override
    public void startDelete() {
        mPbProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDelete(float progress) {

    }

    @Override
    public void finishDelete(ArrayList<ScreenRecordVideoInfo> infos) {
        mPbProgress.setVisibility(View.GONE);
        if(infos.size() > 0) {
            mEmptyView.setVisibility(View.GONE);
            mRvVideoList.setVisibility(View.VISIBLE);
            mAdapter.refreshList(infos);
        } else {
            mRvVideoList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
        exitSelectMode();
    }

    private class SelectActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(final ActionMode mode, Menu menu) {
            ViewGroup v = (ViewGroup) LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_select_action_bar, null);
            mode.setCustomView(v);
            mSelectionAll = (TextView) v.findViewById(R.id.select_all);
            mSelectionCount = (TextView) v.findViewById(R.id.select_count);
            mSelectionAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mIsSelectedAll){
                        mSelectionAll.setText("全不选");
                        mAdapter.clearAllSelectedItems();
                        mIsSelectedAll = false;
                    } else {
                        mSelectionAll.setText("全选");
                        mAdapter.selectAllItems();
                        mIsSelectedAll = true;
                    }
                    updateSelectCount();
                }
            });
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return false;
        }
        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            if(mActionModeType == ACTION_MODE_MULTI_SELECT_AND_DELETE){
                exitSelectMode();
            }
        }
    }
}
