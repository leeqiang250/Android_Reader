package cn.com.pyc.suizhi.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ValidDateUtil;
import cn.com.pyc.suizhi.bean.DrmFile;

public class VideoItemAdapter extends BaseAdapter {
    private Context context;
    private List<DrmFile> drmFiles;
    private int curPos;
    // //private _ColorText colorText;

    private int selectColorId;
    private int unSelectColorId;
    private Drawable selectDrawable;
    private Drawable unSelectDrawable;

    public VideoItemAdapter(Context context, List<DrmFile> drmFiles) {
        this.context = context;
        this.drmFiles = drmFiles;

        this.selectColorId = context.getResources().getColor(
                R.color.brilliant_blue);
        this.unSelectColorId = context.getResources().getColor(
                R.color.white);
        this.selectDrawable = context.getResources().getDrawable(
                R.drawable.ic_validate_time_select);
        this.unSelectDrawable = context.getResources().getDrawable(
                R.drawable.ic_validate_time_nor);
    }

    @Override
    public int getCount() {

        return drmFiles != null ? drmFiles.size() : 0;
    }

    @Override
    public DrmFile getItem(int position) {

        return drmFiles.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_video_lists, null);
            holder.videoName = (TextView) convertView
                    .findViewById(R.id.alt_txt_title);
            holder.videoTime = (TextView) convertView
                    .findViewById(R.id.alt_txt_validity);
            holder.icon = (ImageView) convertView
                    .findViewById(R.id.img_validate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        DrmFile df = drmFiles.get(position);
        if (position == curPos) {
            holder.videoName.setTextColor(selectColorId);
            holder.videoTime.setTextColor(selectColorId);
            holder.icon.setImageDrawable(selectDrawable);
        } else {
            holder.videoName.setTextColor(unSelectColorId);
            holder.videoTime.setTextColor(unSelectColorId);
            holder.icon.setImageDrawable(unSelectDrawable);
        }
        holder.videoName.setText(df.getFileName());
        if (df.isInEffective()) {
            //文件未生效
            holder.videoTime.setText(context.getString(R.string.file_ineffective));
        } else {
            holder.videoTime.setText(ValidDateUtil.getValidTime(context,
                    df.getValidityTime(), df.getEndDatetime()));
        }
        return convertView;
    }

    /**
     * 当前位置
     *
     * @param position
     */
    public void setCurPosition(int position) {
        curPos = position;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView videoName;
        TextView videoTime;
        ImageView icon;
    }
}
