package com.zibuyuqing.screenrecorder.model.bean;

import android.graphics.Bitmap;

import com.zibuyuqing.screenrecorder.utils.Utilities;
import com.zibuyuqing.screenrecorder.utils.ViewUtil;

import java.io.File;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/03/06
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ScreenRecordVideoInfo {
    String name;
    String time;
    String duration;
    String size;
    String filePath;
    Bitmap thumbnail;
    File file;
    public ScreenRecordVideoInfo(File file,int previewWidth,int previewHeight){
        this.file = file;
        this.name = file.getName();
        this.filePath = file.getPath();
        this.time = file.lastModified() + "";
        this.thumbnail = Utilities.getVideoThumbnail(filePath,previewWidth,previewHeight);
        this.duration = Utilities.getVideoDuration(filePath);
        this.size = Utilities.getFileSize(file);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("ScreenRecordVideoInfo =: [ ");
        builder.append("name : " + name)
                .append(",time : " + time)
                .append(",duration : " + duration)
                .append(",size : " + size)
                .append(",filePath : " + filePath)
                .append(",thumbnail :" + thumbnail)
                .append(" ]");
        return builder.toString();
    }

}
