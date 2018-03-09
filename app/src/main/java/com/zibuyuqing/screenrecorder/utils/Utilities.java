package com.zibuyuqing.screenrecorder.utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceFragment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/02/27
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class Utilities {
    final static String FILE_DIR = "/Movies/ScreenRecord/";

    final static long ONE_KB = 1024;
    final static long ONE_MB = 1024 * 1024;
    final static long ONE_GB = 1024 * 1024 * 1024;
    final static long MIN_MEMARY_RANGE = 100 * ONE_MB;

    public static boolean canUseToastType() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.N;
    }

    public static boolean checkFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(context, 24); //OP_SYSTEM_ALERT_WINDOW = 24;
        }
        return true;
    }

    private static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class clazz = AppOpsManager.class;
                Method method = clazz.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception e) {
                Log.e("", Log.getStackTraceString(e));
            }
        } else {
            Log.e("Utilities", "Below API 19 cannot invoke!");
        }
        return false;
    }

    public static String getFileDir() {
        String strRootDir = getExternalStorageDir();
        return strRootDir + FILE_DIR;
    }

    public static String getExternalStorageDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getFileName() {
        String strPrefix = "screenrecord_";
        String strDate = getDateStringFormat(System.currentTimeMillis());
        return strPrefix + strDate + ".mp4";
    }

    public static String getDateStringFormat(long curtime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return sdf.format(new Date(curtime));
    }

    public static long getAvailableInternalMemorySize(Context context) {
        File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        long blockSizeLong = statFs.getBlockSizeLong();
        return availableBlocksLong * blockSizeLong;
    }

    public static long getAvailableExternalMemorySize(Context context) {
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        long blockSizeLong = statFs.getBlockSizeLong();
        return availableBlocksLong * blockSizeLong;
    }

    public static boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean isAvailableMemory(Context context) {
        boolean isExternalStorageAvailable = isExternalStorageAvailable();
        long availableMemorySize;
        if (isExternalStorageAvailable) {
            availableMemorySize = getAvailableExternalMemorySize(context);
        } else {
            availableMemorySize = getAvailableInternalMemorySize(context);
        }
        return availableMemorySize > MIN_MEMARY_RANGE;
    }

    public static Bitmap getVideoThumbnail(String videoPath, int width, int height) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static String getVideoDuration(String videoPath) {
        long duration = 0;
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mediaPlayer.release();
        }
        int second = (int) ((duration / 1000) % 60);
        int minute = second / 60;
        String time = (minute < 10 ? "0" + minute : "" + minute) + ":" + (second < 10 ? "0" + second : "" + second);
        return time;
    }

    public static String getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                size = fis.available();
                return formatFileSize(size);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "0 kb";
    }


    /**
     * 将文件大小转换成字节
     */

    public static String formatFileSize(long fSize) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if(fSize > ONE_GB){
            fileSizeString = df.format((double) fSize / ONE_GB) + "G";
        } else if(fSize > ONE_MB){
            fileSizeString = df.format((double) fSize / ONE_MB) + "M";
        } else if (fSize >= ONE_KB){
            fileSizeString = df.format((double) fSize / ONE_KB) + "K";
        } else {
            fileSizeString = df.format((double) fSize) + "B";
        }
        return fileSizeString;

    }
    public static Bitmap screenShot() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, FileNotFoundException {

        System.out.println("started screenshot");
        Point size = new Point();
        size.x = 1080;
        size.y = 1920;
        String surfaceClassName;
        if (Build.VERSION.SDK_INT <= 17) {
            surfaceClassName = "android.view.Surface";
        } else {
            surfaceClassName = "android.view.SurfaceControl";
        }
        Bitmap b = (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(size.x), Integer.valueOf(size.y)});
        return b;
    }
}
