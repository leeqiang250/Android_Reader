package cn.com.pyc.main.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sz.mobilesdk.manager.ImageLoadHelp;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.bean.RZListBean;
import cn.com.pyc.pbb.R;

/**
 * by 熊
 * <p>
 * 混合PBB，SZ，SERIES三种文件的列表适配器
 */
public class ReaderFilesAdapter extends BaseAdapter {
    private Context mContext;
    private List<RZListBean> mCommonBeanList = new ArrayList<>();//存放混合文件
    private Drawable seriesDrawable;

    /**
     * 根据扩展名显示图标
     */
    public static void showImageByExt(Context ctx, ImageView image, String fileName) {
        Drawable pdfDrawable = ctx.getResources().getDrawable(R.drawable.ic_file_pdf);
        Drawable audioDrawable = ctx.getResources().getDrawable(R.drawable.ic_file_audio);
        Drawable videoDrawable = ctx.getResources().getDrawable(R.drawable.ic_file_video);
        Drawable picDrawable = ctx.getResources().getDrawable(R.drawable.ic_file_pic);
        if (fileName.endsWith(".mp3") || fileName.endsWith(".wav")) {
            image.setImageDrawable(audioDrawable);
        } else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi")
                || fileName.endsWith(".3gp") || fileName.endsWith(".wmv")
                || fileName.endsWith(".flv")) {
            image.setImageDrawable(videoDrawable);
        } else if (fileName.endsWith(".pdf")) {
            image.setImageDrawable(pdfDrawable);
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png")) {
            image.setImageDrawable(picDrawable);
        }
    }

    public ReaderFilesAdapter(Context context, List<RZListBean> commonBeanList) {
        mContext = context;
        mCommonBeanList = commonBeanList;

//        pdfDrawable = mContext.getResources().getDrawable(R.drawable.ic_file_pdf);
//        audioDrawable = mContext.getResources().getDrawable(R.drawable.ic_file_audio);
//        videoDrawable = mContext.getResources().getDrawable(R.drawable.ic_file_video);
//        picDrawable = mContext.getResources().getDrawable(R.drawable.ic_file_pic);
        seriesDrawable = mContext.getResources().getDrawable(R.drawable.ic_file_series);
    }

    public List<RZListBean> getCommonBeanList() {
        return mCommonBeanList;
    }

    public void setCommonBeanList(List<RZListBean> mCommonBeanList) {
        this.mCommonBeanList = mCommonBeanList;
    }

    @Override
    public int getCount() {
        return mCommonBeanList != null ? mCommonBeanList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mCommonBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_all_file_types_reader_list, null);
        }
        TextView name = com.qlk.util.tool.ViewHolder.get(convertView, R.id.item_search_name);
        TextView author = com.qlk.util.tool.ViewHolder.get(convertView, R.id.item_search_ower);
        TextView time = com.qlk.util.tool.ViewHolder.get(convertView, R.id.item_time);
        ImageView image = com.qlk.util.tool.ViewHolder.get(convertView, R.id.item_search_img);

        RZListBean bean = mCommonBeanList.get(position);

        String fileName = bean.getName();
        name.setText(fileName);
        author.setText(bean.getOwner());
        time.setText(bean.getTime());

        if (bean.getSource() == RZListBean.Source.S_SZ) {
            //随知数据显示图片
            ImageLoadHelp.loadImage(image, bean.getProductInfo().getPicture_url());
        } else if (bean.getSource() == RZListBean.Source.S_PBB) {
            showImageByExt(mContext, image, fileName);
        } else if (bean.getSource() == RZListBean.Source.S_SERIES) {
            image.setImageDrawable(seriesDrawable);        //系列图标
        }
        return convertView;
    }
}
