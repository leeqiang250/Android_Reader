package cn.com.pyc.main.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sz.mobilesdk.manager.ImageLoadHelp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.pyc.bean.RZListBean;
import cn.com.pyc.pbb.R;

/**
 * by 熊
 * <p>
 * 混合PBB，SERIES两种种文件的列表适配器(带复选框，用来删除文件)
 */
public class DeleteFilesAdapter extends BaseAdapter {
    private Context mContext;
    private List<RZListBean> mCommonBeanList = new ArrayList<>();
    private Drawable seriesDrawable;//系列图标显示
    private Set<String> mSelectPath = new LinkedHashSet<>(); //存储选中的元素。
    private Map<String, Boolean> mSelectState;                //存儲選中的狀態

    //获取选中的文件路径
    public Set<String> getSelectPath() {
        return mSelectPath;
    }

    //获取填充的数据集合
    public List<RZListBean> getCommonBeanList() {
        return mCommonBeanList;
    }

    public DeleteFilesAdapter(Context context, List<RZListBean> commonBeanList) {
        mContext = context;
        mCommonBeanList = commonBeanList;

        initCheckBoxState(commonBeanList);
        seriesDrawable = mContext.getResources().getDrawable(R.drawable.ic_file_series);
    }

    //初始化checkbox状态，默认false
    private void initCheckBoxState(List<RZListBean> commonBeanList) {
        mSelectState = new HashMap<>();
        mSelectState.clear();
        mSelectPath.clear();
        int size_ = commonBeanList.size();
        for (int i = 0; i < size_; i++) {
            mSelectState.put(commonBeanList.get(i).getFilePath(), false);
        }
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewCache cache;
        if (convertView == null) {
            cache = new ViewCache();
            convertView = View.inflate(mContext, R.layout.item_all_file_types_delete_list, null);
            convertView.setTag(cache);
        } else {
            cache = (ViewCache) convertView.getTag();
        }
        cache.name = (TextView) convertView.findViewById(R.id.item_search_name);
        cache.author = (TextView) convertView.findViewById(R.id.item_search_ower);
        cache.time = (TextView) convertView.findViewById(R.id.item_time);
        cache.image = (ImageView) convertView.findViewById(R.id.item_search_img);
        cache.checkBox = (CheckBox) convertView.findViewById(R.id.item_cbx_del);

        final RZListBean bean = mCommonBeanList.get(position);
        String fileName = bean.getName();
        cache.name.setText(fileName);
        cache.author.setText(bean.getOwner());
        cache.time.setText(bean.getTime());

        if (bean.getSource() == RZListBean.Source.S_SZ) {
            //随知数据显示图片
            ImageLoadHelp.loadImage(cache.image, bean.getProductInfo().getPicture_url());
        } else if (bean.getSource() == RZListBean.Source.S_PBB) {
            ReaderFilesAdapter.showImageByExt(mContext, cache.image, fileName);
        } else if (bean.getSource() == RZListBean.Source.S_SERIES) {
            cache.image.setImageDrawable(seriesDrawable);
        }
        cache.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (bean.getSource() == RZListBean.Source.S_SERIES) {
                        Map<String, List<String>> mapPaths = bean.getSeriesMap();
                        List<String> paths = mapPaths.get(bean.getName());
                        for (String p : paths) {
                            mSelectPath.add(p);
                        }
                    } else {
                        mSelectPath.add(bean.getFilePath());
                    }
                    mSelectState.put(bean.getFilePath(), true); //记录选中后状态
                } else {
                    if (bean.getSource() == RZListBean.Source.S_SERIES) {
                        Map<String, List<String>> mapPaths = bean.getSeriesMap();
                        List<String> paths = mapPaths.get(bean.getName());
                        for (String p : paths) {
                            mSelectPath.remove(p);
                        }
                    } else {
                        mSelectPath.remove(bean.getFilePath());
                    }
                    mSelectState.put(bean.getFilePath(), false); //清除记录的状态
                }
            }
        });
        cache.checkBox.setChecked(mSelectState.get(bean.getFilePath()));
        return convertView;
    }

    //全选(反选)
    public void selectAll(List<RZListBean> data, boolean selected) {
        if (data == null) return;
        //selectAll = selected;
        if (selected) {
            for (RZListBean bean : data) {
                if (bean.getSource() == RZListBean.Source.S_SERIES) {
                    Map<String, List<String>> infos = bean.getSeriesMap();
                    List<String> paths = infos.get(bean.getName());
                    for (String p : paths) {
                        mSelectPath.add(p);
                    }
                } else {
                    mSelectPath.add(bean.getFilePath());
                }
                //全选，记录所有的选中后状态
                mSelectState.put(bean.getFilePath(), true);
            }
        } else {
            mSelectState.clear(); //清除所有状态
            mSelectPath.clear();
        }
        notifyDataSetChanged();
    }


    private static class ViewCache {
        TextView name;
        TextView author;
        TextView time;
        ImageView image;
        CheckBox checkBox;
    }

}
