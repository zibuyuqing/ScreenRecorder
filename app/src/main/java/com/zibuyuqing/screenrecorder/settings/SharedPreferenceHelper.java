package com.zibuyuqing.screenrecorder.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/03/05
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class SharedPreferenceHelper {
    private static volatile SharedPreferenceHelper sHelp;
    private SharedPreferences mPreferences;
    private SharedPreferenceHelper(Context context,String spName){
        mPreferences = context.getApplicationContext().getSharedPreferences(spName,Context.MODE_PRIVATE);
    }
    public static SharedPreferenceHelper from (Context context,String spName){
        if(sHelp == null){
            synchronized (SharedPreferenceHelper.class){
                if(sHelp == null){
                    sHelp = new SharedPreferenceHelper(context,spName);
                }
            }
        }
        return sHelp;
    }
    public int getIntValue(String key,int defaultValue){
        return mPreferences.getInt(key,defaultValue);
    }
    public long getLongValue(String key,int defaultValue){
        return mPreferences.getLong(key,defaultValue);
    }
    public float getFloatValue(String name, float defaultValue) {
        return mPreferences.getFloat(name, defaultValue);
    }
    public boolean getBooleanValue(String key, boolean defaultValue){
        return mPreferences.getBoolean(key,defaultValue);
    }
    public String getStringValue(String name, String defaultValue) {
        return mPreferences.getString(name, defaultValue);
    }
    public void editorIntValue(String name, int value) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putInt(name, value);
        edit.commit();
    }
    public void editorLongValue(String name, long value) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putLong(name, value);
        edit.commit();
    }
    public void editorFloatValue(String name, float value) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putFloat(name, value);
        edit.commit();
    }

    public void editorBooleanValue(String name, boolean value) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putBoolean(name, value);
        edit.commit();
    }

    public void editorStringValue(String name, String value) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString(name, value);
        edit.commit();
    }
}
