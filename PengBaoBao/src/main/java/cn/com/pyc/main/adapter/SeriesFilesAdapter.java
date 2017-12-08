package cn.com.pyc.main.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qlk.util.tool.Util;
import com.sz.mobilesdk.util.TimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.pbb.R;

/**
 * Created by 熊 on 2017/9/8.
 * <p>
 * 系列文件的列表适配器
 */

public class SeriesFilesAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mPaths = new ArrayList<>();

    public SeriesFilesAdapter(Context context, List<String> paths) {
        mContext = context;
        mPaths = paths;
    }

    @Override
    public int getCount() {
        return mPaths != null ? mPaths.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mPaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_all_file_types_reader_list, null);
        }
        TextView name = com.qlk.util.tool.ViewHolder.get(convertView, R.id.item_search_name);
        TextView author = com.qlk.util.tool.ViewHolder.get(convertView, R.id.item_search_ower);
        TextView time = com.qlk.util.tool.ViewHolder.get(convertView, R.id.item_time);
        ImageView image = com.qlk.util.tool.ViewHolder.get(convertView, R.id.item_search_img);

        String path = mPaths.get(position);
        String fileName_ = Util.FileUtil.getFileName(path);
        String fileName = fileName_.substring(0, fileName_.length() - 4);
        name.setText(fileName);
        author.setText("");
        File file = new File(path);
        time.setText(TimeUtil.getDateStringFromMills(
                file.lastModified() + "", "yyyy-MM-dd HH:mm"));

        ReaderFilesAdapter.showImageByExt(mContext, image, fileName);

        return convertView;
    }


}
