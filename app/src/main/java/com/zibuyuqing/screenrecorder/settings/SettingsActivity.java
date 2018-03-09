package com.zibuyuqing.screenrecorder.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.zibuyuqing.screenrecorder.R;
import com.zibuyuqing.screenrecorder.ui.BaseActivity;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/03/05
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class SettingsActivity extends BaseActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void init() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content,new RecordSettingsFragment())
                .commit();
    }

    @Override
    protected boolean canBack() {
        return false;
    }

    @Override
    protected int providedLayoutId() {
        return R.layout.activity_settings;
    }

    @Override
    protected boolean customToolbar() {
        return true;
    }

    @Override
    protected View getToolbarContent() {
        return getLayoutInflater().inflate(R.layout.layout_settings_activity_toolbar,null);
    }

    public static class RecordSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        SharedPreferenceHelper mPreferenceHelper;
        String[] mResolutionArr, mVideoQualityArr, mFrameRateArr, mAudioSourceArr;
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_settings);
            mPreferenceHelper = SharedPreferenceHelper.from(getContext(),Config.PREFERENCE_FILE_NAME);
            initPreferences();
        }
        private void initPreferences(){
            mResolutionArr = getContext().getResources().getStringArray(R.array.resolution_selection);
            mVideoQualityArr = getContext().getResources().getStringArray(R.array.video_quality_selection);
            mFrameRateArr = getContext().getResources().getStringArray(R.array.video_frame_rate_selection);
            mAudioSourceArr = getContext().getResources().getStringArray(R.array.audio_source_selection);

            int intValue = 0;
            boolean boolValue = false;
            final ListPreference resolutionPrefList =
                    (ListPreference) findPreference(Config.PREF_KEY_RESOLUTION);
            intValue = mPreferenceHelper.getIntValue(
                    Config.PREF_KEY_RESOLUTION,Config.CONFIG_DEFAULT_RESOLUTION);
            resolutionPrefList.setValueIndex(intValue);
            resolutionPrefList.setSummary(mResolutionArr[intValue]);
            resolutionPrefList.setOnPreferenceChangeListener(this);

            final ListPreference videoQualityPrefList =
                    (ListPreference) findPreference(Config.PREF_KEY_VIDEO_QUALITY);
            intValue = mPreferenceHelper.getIntValue(
                    Config.PREF_KEY_VIDEO_QUALITY,Config.CONFIG_DEFAULT_VIDEO_QUALITY);
            videoQualityPrefList.setValueIndex(intValue);
            videoQualityPrefList.setSummary(mVideoQualityArr[intValue]);
            videoQualityPrefList.setOnPreferenceChangeListener(this);

            final ListPreference frameRatePrefList =
                    (ListPreference) findPreference(Config.PREF_KEY_FRAME_RATE);
            intValue = mPreferenceHelper.getIntValue(
                    Config.PREF_KEY_FRAME_RATE,Config.CONFIG_DEFAULT_FRAME_RATE);
            frameRatePrefList.setValueIndex(intValue);
            frameRatePrefList.setSummary(mFrameRateArr[intValue]);
            frameRatePrefList.setOnPreferenceChangeListener(this);

            final ListPreference audioSourcePrefList =
                    (ListPreference) findPreference(Config.PREF_KEY_AUDIO_SOURCE);
            intValue = mPreferenceHelper.getIntValue(
                    Config.PREF_KEY_AUDIO_SOURCE,Config.CONFIG_DEFAULT_AUDIO_SOURCE);
            audioSourcePrefList.setValueIndex(intValue);
            audioSourcePrefList.setSummary(mAudioSourceArr[intValue]);
            audioSourcePrefList.setOnPreferenceChangeListener(this);

            final SwitchPreference stopWhenLockSwitch =
                    (SwitchPreference)findPreference(Config.PREF_KEY_STOP_RECORD_WHEN_LOCK);
            boolValue = mPreferenceHelper.getBooleanValue(
                    Config.PREF_KEY_STOP_RECORD_WHEN_LOCK,Config.CONFIG_DEFAULT_STOP_WHEN_LOCK);
            stopWhenLockSwitch.setChecked(boolValue);
            stopWhenLockSwitch.setOnPreferenceChangeListener(this);

            final SwitchPreference showTouchPositionSwitch =
                    (SwitchPreference)findPreference(Config.PREF_KEY_SHOW_TOUCH_POSITION);
            boolValue = mPreferenceHelper.getBooleanValue(
                    Config.PREF_KEY_SHOW_TOUCH_POSITION,Config.CONFIG_DEFAULT_SHOW_TOUCH_POSITION);
            showTouchPositionSwitch.setChecked(boolValue);
            showTouchPositionSwitch.setOnPreferenceChangeListener(this);

            final SwitchPreference stepSideSwitch =
                    (SwitchPreference)findPreference(Config.PREF_KEY_STEP_SIDE_WHEN_NO_OPERATION);
            boolValue = mPreferenceHelper.getBooleanValue(
                    Config.PREF_KEY_STEP_SIDE_WHEN_NO_OPERATION,Config.CONFIG_DEFAULT_STEP_SIDE_WHEN_NO_OPERATION);
            stepSideSwitch.setChecked(boolValue);
            stepSideSwitch.setOnPreferenceChangeListener(this);

            final SwitchPreference showCountdownSwitch =
                    (SwitchPreference)findPreference(Config.PREF_KEY_SHOW_COUNTDOWN_PRE_RECORD);
            boolValue = mPreferenceHelper.getBooleanValue(
                    Config.PREF_KEY_SHOW_COUNTDOWN_PRE_RECORD,Config.CONFIG_DEFAULT_SHOW_COUNTDOWN_PRE_RECORD);
            showCountdownSwitch.setChecked(boolValue);
            showCountdownSwitch.setOnPreferenceChangeListener(this);

            final SwitchPreference showVideoListSwitch =
                    (SwitchPreference)findPreference(Config.PREF_KEY_SHOW_VIDEO_LIST_WEHN_STOP);
            boolValue = mPreferenceHelper.getBooleanValue(
                    Config.PREF_KEY_SHOW_VIDEO_LIST_WEHN_STOP,Config.CONFIG_DEFAULT_SHOW_VIDEO_LIST_WEHN_STOP);
            showVideoListSwitch.setChecked(boolValue);
            showVideoListSwitch.setOnPreferenceChangeListener(this);
        }

        private int getItemIndexInList(String item,String[] arrays){
            int index = 0;
            for(String s : arrays){
                if(item.equals(s)){
                    return index;
                }
                index ++;
            }
            return index;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.e(TAG,"onPreferenceChange :: key =："+ preference.getKey() +",newValue =:" + newValue);
            String key = preference.getKey();
            switch (key){
                case Config.PREF_KEY_RESOLUTION:

                    int index = getItemIndexInList((String) newValue, mResolutionArr);
                    mPreferenceHelper.editorIntValue(key,index);
                    ListPreference resolutionPrefList = (ListPreference) preference;
                    resolutionPrefList.setValueIndex(index);
                    resolutionPrefList.setSummary((CharSequence) newValue);
                    break;
                case Config.PREF_KEY_VIDEO_QUALITY:

                    index = getItemIndexInList((String) newValue, mVideoQualityArr);
                    mPreferenceHelper.editorIntValue(key,index);
                    ListPreference videoQualityPrefList = (ListPreference) preference;
                    videoQualityPrefList.setValueIndex(index);
                    videoQualityPrefList.setSummary((CharSequence) newValue);
                    break;
                case Config.PREF_KEY_FRAME_RATE:

                    index = getItemIndexInList((String) newValue, mFrameRateArr);
                    mPreferenceHelper.editorIntValue(key,index);
                    ListPreference frameRatePrefList = (ListPreference) preference;
                    frameRatePrefList.setValueIndex(index);
                    frameRatePrefList.setSummary((CharSequence) newValue);
                    break;

                case Config.PREF_KEY_AUDIO_SOURCE:

                    index = getItemIndexInList((String) newValue, mAudioSourceArr);
                    mPreferenceHelper.editorIntValue(key,index);
                    ListPreference audioSourceList = (ListPreference) preference;
                    audioSourceList.setValueIndex(index);
                    audioSourceList.setSummary((CharSequence) newValue);
                    break;
                case Config.PREF_KEY_STOP_RECORD_WHEN_LOCK:
                    mPreferenceHelper.editorBooleanValue(key, (Boolean) newValue);
                    SwitchPreference stopWhenLock = (SwitchPreference) preference;
                    stopWhenLock.setChecked((Boolean) newValue);
                    break;
                case Config.PREF_KEY_SHOW_TOUCH_POSITION:
                    mPreferenceHelper.editorBooleanValue(key, (Boolean) newValue);
                    SwitchPreference showTouchPosition = (SwitchPreference) preference;
                    showTouchPosition.setChecked((Boolean) newValue);
                    break;
                case Config.PREF_KEY_STEP_SIDE_WHEN_NO_OPERATION:
                    mPreferenceHelper.editorBooleanValue(key, (Boolean) newValue);
                    SwitchPreference stepSideSwitch = (SwitchPreference) preference;
                    stepSideSwitch.setChecked((Boolean) newValue);
                    break;
                case Config.PREF_KEY_SHOW_COUNTDOWN_PRE_RECORD:
                    mPreferenceHelper.editorBooleanValue(key, (Boolean) newValue);
                    SwitchPreference showCountdownSwitch = (SwitchPreference) preference;
                    showCountdownSwitch.setChecked((Boolean) newValue);
                    break;
                case Config.PREF_KEY_SHOW_VIDEO_LIST_WEHN_STOP:
                    mPreferenceHelper.editorBooleanValue(key, (Boolean) newValue);
                    SwitchPreference showVideoListSwitch = (SwitchPreference) preference;
                    showVideoListSwitch.setChecked((Boolean) newValue);
                    break;
            }
            return false;
        }
    }
    public static void start(Context context) {
        Intent starter = new Intent(context, SettingsActivity.class);
        context.startActivity(starter);
    }
}
