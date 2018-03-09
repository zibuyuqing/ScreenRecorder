package com.zibuyuqing.screenrecorder.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.zibuyuqing.screenrecorder.R;
import com.zibuyuqing.screenrecorder.model.bean.ScreenRecordVideoInfo;

import java.util.ArrayList;

/**
 * <pre>
 *     author : Xijun.Wang
 *     e-mail : zibuyuqing@gmail.com
 *     time   : 2018/03/06
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RecordVideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context mContext;
    private ArrayList<ScreenRecordVideoInfo> mVideoInfoList = new ArrayList<>();
    private ArrayList<ScreenRecordVideoInfo> mSelectInfos = new ArrayList<>();
    private SparseBooleanArray mSelectedPositions = new SparseBooleanArray();
    private boolean isOnSelectState = false;
    private int mFirstSelectItem = -1;
    private OnStateChangedListener mListener;
    public RecordVideoListAdapter(Context context){
        mContext = context;
    }
    public void refreshList(ArrayList<ScreenRecordVideoInfo> infos){
        mVideoInfoList = infos;
        notifyDataSetChanged();
    }
    public void setOnActionModeChangedListener(OnStateChangedListener listener){
        mListener = listener;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_video_preview_item,null);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        buildItemCard(viewHolder,position);
    }
    private boolean isItemChecked(int position) {
        return mSelectedPositions.get(position);
    }
    private void buildItemCard(final ItemViewHolder holder, final int position){
        final ScreenRecordVideoInfo info = mVideoInfoList.get(position);
        holder.ivThumbnail.setImageBitmap(info.getThumbnail());
        holder.tvFileSize.setText(info.getSize());
        holder.tvFilename.setText(info.getName());
        holder.tvDuration.setText(info.getDuration());
        if(isOnSelectState){
            holder.cbSelect.setVisibility(View.VISIBLE);
            holder.ivPlay.setVisibility(View.GONE);
            if(isItemChecked(position)){
                holder.cbSelect.setChecked(true);
            } else {
                holder.cbSelect.setChecked(false);
            }
        } else {
            holder.cbSelect.setChecked(false);
            holder.cbSelect.setVisibility(View.GONE);
            holder.ivPlay.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                isOnSelectState = true;
                mFirstSelectItem = position;
                if(mListener != null){
                    mListener.startSelect();
                }
                setItemChecked(mFirstSelectItem,true);
                notifyDataSetChanged();
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnSelectState){
                    CheckBox select = holder.cbSelect;
                    if(select.isChecked()){
                        select.setChecked(false);
                        setItemChecked(position,false);
                    } else {
                        select.setChecked(true);
                        setItemChecked(position,true);
                    }
                } else {
                    openFile(info.getFilePath());
                }
            }
        });
    }
    public void setItemChecked(int position, boolean isChecked) {
        mSelectedPositions.put(position, isChecked);
        selectFile(position,isChecked);
        if(mListener != null) {
            mListener.select();
        }
    }
    public void selectFile(int position,boolean checked){
        ScreenRecordVideoInfo info = mVideoInfoList.get(position);
        if(checked){
            if(!mSelectInfos.contains(info)) {
                mSelectInfos.add(info);
            }
        } else {
            if(mSelectInfos.contains(info)){
                mSelectInfos.remove(info);
            }
        }
    }
    public boolean isOnSelectState(){
        return isOnSelectState;
    }
    private void openFile(String path){
        try {
            Uri uri = Uri.parse(path);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri,"video/mp4");
            mContext.startActivity(intent);
        } catch (Exception e){
            e.printStackTrace();
        }

    }
    @Override
    public int getItemCount() {
        return mVideoInfoList.size();
    }
    public void clearAllSelectedItems(){
        mSelectedPositions = new SparseBooleanArray();
        mSelectInfos.clear();
        mFirstSelectItem = -1;
        notifyDataSetChanged();
    }
    public ArrayList<ScreenRecordVideoInfo> getSelectInfos(){
        return mSelectInfos;
    }
    public void selectAllItems(){
        for (int i = 0; i <getItemCount(); i++) {
            setItemChecked(i,true);
        }
        notifyDataSetChanged();
    }
    public void exitSelectState() {
        isOnSelectState = false;
        clearAllSelectedItems();
        notifyDataSetChanged();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView tvDuration,tvFileSize,tvFilename;
        private ImageView ivPlay,ivThumbnail;
        private CheckBox cbSelect;
        public ItemViewHolder(View itemView) {
            super(itemView);
            tvDuration = itemView.findViewById(R.id.tv_video_duration);
            tvFilename = itemView.findViewById(R.id.tv_filename);
            tvFileSize = itemView.findViewById(R.id.tv_file_size);
            ivPlay = itemView.findViewById(R.id.iv_play);
            ivThumbnail = itemView.findViewById(R.id.iv_video_thumbnail);
            cbSelect = itemView.findViewById(R.id.cb_select);
        }
    }
    public interface OnStateChangedListener{
        void startSelect();
        void select();
    }
}
