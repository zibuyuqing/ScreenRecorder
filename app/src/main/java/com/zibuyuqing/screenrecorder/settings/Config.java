package com.zibuyuqing.screenrecorder.settings;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/03/02
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class Config {

    public static final String PREFERENCE_FILE_NAME = "screen_recorder";
    public static final String PREF_KEY_RESOLUTION = "key_resolution";
    public static final String PREF_KEY_VIDEO_QUALITY = "key_video_quality";
    public static final String PREF_KEY_FRAME_RATE = "key_frame_rate";
    public static final String PREF_KEY_AUDIO_SOURCE = "key_audio_source";
    public static final String PREF_KEY_STOP_RECORD_WHEN_LOCK = "key_stop_record_when_lock";
    public static final String PREF_KEY_SHOW_TOUCH_POSITION = "key_show_touch_position";
    public static final String PREF_KEY_STEP_SIDE_WHEN_NO_OPERATION = "key_step_side_when_no_operation";
    public static final String PREF_KEY_SHOW_COUNTDOWN_PRE_RECORD = "key_show_countdown_pre_record";
    public static final String PREF_KEY_SHOW_VIDEO_LIST_WEHN_STOP = "key_show_video_list_when_stop";
    public static final int CONFIG_DEFAULT_RESOLUTION = 0;
    public static final int CONFIG_DEFAULT_VIDEO_QUALITY = 2;
    public static final int CONFIG_DEFAULT_FRAME_RATE = 1;
    public static final int CONFIG_DEFAULT_AUDIO_SOURCE = 0;
    public static final boolean CONFIG_DEFAULT_STOP_WHEN_LOCK = true;
    public static final boolean CONFIG_DEFAULT_SHOW_TOUCH_POSITION = false;
    public static final boolean CONFIG_DEFAULT_STEP_SIDE_WHEN_NO_OPERATION = true;
    public static final boolean CONFIG_DEFAULT_SHOW_COUNTDOWN_PRE_RECORD = true;
    public static final boolean CONFIG_DEFAULT_SHOW_VIDEO_LIST_WEHN_STOP = true;
    private String savePath;
    private int videoEncodingBitRate;
    private int videoFrameRate;
    private int recordWidth;
    private int recordHeight;
    private int audioSource;

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public int getVideoEncodingBitRate() {
        return videoEncodingBitRate;
    }

    public void setVideoEncodingBitRate(int videoEncodingBitRate) {
        this.videoEncodingBitRate = videoEncodingBitRate;
    }

    public int getVideoFrameRate() {
        return videoFrameRate;
    }

    public void setVideoFrameRate(int videoFrameRate) {
        this.videoFrameRate = videoFrameRate;
    }

    public int getRecordWidth() {
        return recordWidth;
    }

    public void setRecordWidth(int recordWidth) {
        this.recordWidth = recordWidth;
    }

    public int getRecordHeight() {
        return recordHeight;
    }

    public void setRecordHeight(int recordHeight) {
        this.recordHeight = recordHeight;
    }

    public int getAudioSource() {
        return audioSource;
    }

    public void setAudioSource(int audioSource) {
        this.audioSource = audioSource;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Config =: [ ");
        builder.append("recordWidth : " + recordWidth)
                .append(",recordHeight : " + recordHeight)
                .append(",videoEncodingBitRate : " + videoEncodingBitRate)
                .append(",videoFrameRate : " + videoFrameRate)
                .append(",savePath : " + savePath)
        .append(" ]");
        return builder.toString();
    }
}
