package cn.com.pyc.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.qlk.util.base.BaseFragment;
import com.qlk.util.global.GlobalLruCache;
import com.qlk.util.tool.Util.FileUtil;
import com.qlk.util.widget.PullRefreshView;
import com.qlk.util.widget.PullRefreshView.OnRefreshListener;

import java.util.ArrayList;

import cn.com.pyc.pbb.R;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.user.Pbb_Fields;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (多媒体图片第一层ui)
 * @date 2016/11/30 10:54
 *
 * 展示图片分类,获取所有分类的第一张图片显示，并显示子图片的数量。
 * 通过ImageSort.getFirsts()获取所有分类的图片路径mPaths。
 * 通过ImageSort.getSort().get(folder).size()获取所有分类的字图片的个数。
 * 通过getBitmap(position)显示默认图片。
 */
public class PlainImageSortFragment extends BaseFragment {
    private final ArrayList<String> mPaths = new ArrayList<String>();
    private ViewStub mEmptyStub;
    private View mEmptyLayout;
    private GridView mGridView;
    private View mDivider;
    private PullRefreshView mPullRefreshView;
    private PlainImageSortAdapter mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_plain_image_sort, container, false);
        init_view(v);
        init_listener(v);
        initUI();
        refreshUI();
        return v;
    }


    private void init_view(View v) {
        mDivider = v.findViewById(R.id.fpis_imv_divider);
        mGridView = (GridView) v.findViewById(R.id.fpis_grv_sort);// 展示图片GridView
        mEmptyStub = (ViewStub) v.findViewById(R.id.fpis_lyt_empty);// 空空如也显示
        mPullRefreshView = (PullRefreshView) v.findViewById(R.id.pull_down_refresh);// 上下拉刷新控件
    }

    private void init_listener(View v) {
        mPullRefreshView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                GlobalData.Image.instance(getActivity()).search(false);
            }
        });
        // 点击按钮,刷新全部
        v.findViewById(R.id.ipt_imb_refresh).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                GlobalData.Image.instance(getActivity()).search(false);
            }
        });
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 跳转至子图片view,发送图片文件夹中第一张图片的路径给子界面。
                Bundle data = new Bundle();
                data.putString(GlobalIntentKeys.BUNDLE_DATA_FOLDER, FileUtil.getFolder(mPaths.get(position)));
                changeFragment(Pbb_Fields.TAG_PLAIN_IMAGE, data);
            }
        });
        mGridView.setOnScrollListener(new OnScrollListener() {
            private boolean isFirstScroll = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // The adapter will refresh thumbs when user press and scroll.
                mAdapter.changeScrollState(view, scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (isFirstScroll && visibleItemCount > 0) {
                    isFirstScroll = false;
                    mAdapter.refresh(view);
                }
            }
        });
    }




    @Override
    protected void initUI() {
        mAdapter = new PlainImageSortAdapter(getActivity(), mPaths);
        mGridView.setAdapter(mAdapter);
    }


    @Override
    public void refreshUI() {
        mPaths.clear();
        // 获取所有的第一层图片
        mPaths.addAll(ImageSort.getFirsts());
        if (mPaths.isEmpty()) {
            if (mEmptyLayout == null) {
                mEmptyLayout = mEmptyStub.inflate();
            }
            mEmptyLayout.setVisibility(View.VISIBLE);
            mDivider.setVisibility(View.INVISIBLE);
        } else {
            if (mEmptyLayout != null) {
                mEmptyLayout.setVisibility(View.GONE);
            }
            mDivider.setVisibility(View.VISIBLE);
        }
        mAdapter.refresh(mGridView);
    }

    private class PlainImageSortAdapter extends MediaBaseAdapter {

        private PlainImageSortAdapter(Context context, ArrayList<String> paths) {
            super(context, paths);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.adapter_plain_image_sort, parent, false);
                vh = new ViewHolder();
                vh.pic = (ImageView) convertView.findViewById(R.id.apis_imv_image);
                vh.name = (TextView) convertView.findViewById(R.id.apis_txt_name);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            vh.pic.setImageBitmap(getBitmap(position));
            String folder = FileUtil.getFolder(mPaths.get(position));
            vh.name.setText(folder + "(" + ImageSort.getSort().get(folder).size() + ")");

            return convertView;
        }

        class ViewHolder {
            ImageView pic;
            TextView name;
        }

        @Override
        protected boolean isSupportThumbView() {
            return true;
        }

        @Override
        protected Runnable getThumbTask(final String path) {
            return new Runnable() {
                @Override
                public void run() {
                    Bitmap bmp = GlobalLruCache.getGLC().get(path);
                    if (bmp == null || bmp.isRecycled()) {
                        bmp = MyBitmapFactory.getImageThumbnail(path, mThumbSize);
                        if (bmp != null) {
                            GlobalLruCache.getGLC().put(path, bmp);
                        }
                    }
                }
            };
        }

    }

    @Override
    protected boolean isObserverEnabled() {
        return false;
    }


}
