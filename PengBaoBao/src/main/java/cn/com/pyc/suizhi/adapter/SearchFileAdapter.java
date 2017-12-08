package cn.com.pyc.suizhi.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qlk.util.tool.Util;
import com.qlk.util.tool.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.pbb.R;

/**
 * Created by hudaqiang on 2017/8/31.
 */

public class SearchFileAdapter extends BaseAdapter {

    private Context mContext;
    private List<SmInfo> mSmInfos = new ArrayList<>();
    private Drawable pdfDrawable, audioDrawable, videoDrawable;

    public SearchFileAdapter(Context context, List<SmInfo> smInfos) {
        mContext = context;
        mSmInfos = smInfos;

        pdfDrawable = mContext.getResources().getDrawable(R.drawable.ic_file_pdf);
        audioDrawable = mContext.getResources().getDrawable(R.drawable.ic_file_audio);
        videoDrawable = mContext.getResources().getDrawable(R.drawable.ic_file_video);
    }

    public void setSmInfos(List<SmInfo> smInfos) {
        mSmInfos = smInfos;
    }

    public List<SmInfo> getSmInfos() {
        return mSmInfos;
    }

    @Override
    public int getCount() {
        return mSmInfos != null ? mSmInfos.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mSmInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_search_new, null);
        }
        View label = ViewHolder.get(convertView, R.id.item_search_label);
        TextView name = ViewHolder.get(convertView, R.id.item_search_name);
        TextView author = ViewHolder.get(convertView, R.id.item_search_ower);
        ImageView image = ViewHolder.get(convertView, R.id.item_search_img);
        label.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        SmInfo smInfo = mSmInfos.get(position);
        String fileName_ = Util.FileUtil.getFileName(smInfo.getFilePath());
        String fileName = fileName_.substring(0, fileName_.length() - 4); // remove".pbb"
        name.setText(fileName);
        author.setText(smInfo.getNick());
        //TODO:
        if (fileName.endsWith(".mp3")) {
            image.setImageDrawable(audioDrawable);
        } else if (fileName.endsWith(".mp4")) {
            image.setImageDrawable(videoDrawable);
        } else if (fileName.endsWith(".pdf")) {
            image.setImageDrawable(pdfDrawable);
        }
        return convertView;
    }
}
