package cn.com.pyc.suizhi.adapter;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sz.mobilesdk.util.FormatterUtil;

import java.util.List;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ValidDateUtil;
import cn.com.pyc.suizhi.bean.DrmFile;

public class ListFileLocalAdapter extends BaseAdapter {
    private ExtraBaseActivity mContext;
    private List<DrmFile> list;
    private String contentId;

    private int selectColorId;
    private int unSelectNameColorId;
    private int unSelectTimeColorId;
    //private Drawable selectDrawable;
    //private Drawable unSelectDrawable;

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public List<DrmFile> getList() {
        return list;
    }

    public ListFileLocalAdapter(ExtraBaseActivity mContext, List<DrmFile> list) {
        this.mContext = mContext;
        this.list = list;

        Resources resources = this.mContext.getResources();
        this.selectColorId = resources.getColor(cn.com.pyc.pbb.R.color.title_top_color);
        this.unSelectNameColorId = resources.getColor(R.color.black_bb);
        this.unSelectTimeColorId = resources.getColor(R.color.gray);
//        this.selectDrawable = resources
//                .getDrawable(R.drawable.ic_validate_time_select);
//        this.unSelectDrawable = resources
//                .getDrawable(R.drawable.ic_validate_time_nor);
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public DrmFile getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_file_list_offline, null);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_offline_file_name);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tv_offline_file_status);
            holder.tvLabel = (TextView) convertView.findViewById(R.id.tv_offline_file_label);
            holder.tvSize = (TextView) convertView.findViewById(R.id.tv_offline_file_size);
            holder.ivTime = (ImageView) convertView.findViewById(R.id.iv_offline_file_status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        DrmFile ac = list.get(position);
        boolean equals = TextUtils.equals(contentId, ac.getFileId());
        holder.tvName.setTextColor(equals ? selectColorId : unSelectNameColorId);
        holder.tvTime.setTextColor(equals ? selectColorId : unSelectTimeColorId);
        holder.tvLabel.setTextColor(equals ? selectColorId : unSelectTimeColorId);
        //holder.ivTime.setImageDrawable(equals ? selectDrawable : unSelectDrawable);

        holder.tvName.setText(ac.getFileName());
        holder.tvSize.setText(FormatterUtil.formatSize(ac.getFileSize()));
        //holder.tvLabel.setText(mContext.getString(R.string.downloaditem_status_label));
        //SZContent szcont = new SZContent(ac.getAsset_id());
        if (ac.isInEffective()) {
            holder.tvTime.setText(mContext.getString(R.string.file_ineffective));
        } else {
            holder.tvTime.setText(ValidDateUtil.getValidTime(mContext, ac.getValidityTime(), ac
                    .getEndDatetime()));
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvTime;
        TextView tvLabel;
        TextView tvSize;
        ImageView ivTime;
    }

}
