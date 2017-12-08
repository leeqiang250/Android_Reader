package cn.com.pyc.media;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.qlk.util.base.BaseFragment;

import cn.com.pyc.pbb.R;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.user.Pbb_Fields;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (明本本地文件总列表)
 * @date 2016/11/30 10:37
 */
public class PlainTotalFragment extends BaseFragment implements OnClickListener {
    private ListView mListView;
    private PlainTotalAdapter mAdapter;
    private ImageButton m_ipt_imb_refresh;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_plain_total, container, false);
        init_view(v);
        init_listener();
        init_adapter();
        return v;
    }


    /**
     * @author 李巷阳
     * @date 2016-11-14 下午6:12:34
     */
    private void init_view(View v) {
        mListView = (ListView) v.findViewById(R.id.fpt_lsv_total);
        m_ipt_imb_refresh = (ImageButton) v.findViewById(R.id.ipt_imb_refresh);
    }


    /**
     * @author 李巷阳
     * @date 2016-11-14 下午6:12:36
     */
    private void init_listener() {
        m_ipt_imb_refresh.setOnClickListener(this);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case PlainTotalAdapter.ITEM_IMAGE:
                        changeFragment(Pbb_Fields.TAG_PLAIN_IMAGE_SORT);
                        break;

                    case PlainTotalAdapter.ITEM_PDF:
                        changeFragment(Pbb_Fields.TAG_PLAIN_FILE);
                        break;

                    case PlainTotalAdapter.ITEM_VIDEO:
                        changeFragment(Pbb_Fields.TAG_PLAIN_VIDEO);
                        break;

                    case PlainTotalAdapter.ITEM_MUSIC:
                        changeFragment(Pbb_Fields.TAG_PLAIN_MUSIC);
                        break;

                    default:
                        break;
                }
            }
        });
    }

    @Override
    protected boolean isObserverEnabled() {
        return false;
    }

    /**
     * @author 李巷阳
     * @date 2016/11/30 10:45
     */
    private void init_adapter() {
        if (mAdapter == null) {
            mAdapter = new PlainTotalAdapter(getActivity());
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }


    private class PlainTotalAdapter extends BaseAdapter {
        private static final short TOTAL_COUNT = 4;
        private static final short ITEM_IMAGE = 0;
        private static final short ITEM_VIDEO = 1;
        private static final short ITEM_MUSIC = 2;
        private static final short ITEM_PDF = 3;

        private LayoutInflater mInflater;

        public PlainTotalAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return TOTAL_COUNT;
        }

        @Override
        public Integer getItem(int position) {
            return 0;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder vh;
            if (v == null) {
                v = mInflater.inflate(R.layout.adapter_plain_total, parent, false);
                vh = new ViewHolder();
                vh.name = (TextView) v.findViewById(R.id.apt_txt_content);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }
            switch (position) {
                case ITEM_IMAGE:
                    vh.name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.media_plain_image, 0, 0, 0);
                    vh.name.setText("图片（" + GlobalData.Image.instance(getActivity()).getCopyPaths(false).size() + "）");
                    break;

                case ITEM_VIDEO:
                    vh.name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.media_plain_video, 0, 0, 0);
                    vh.name.setText("视频（" + GlobalData.Video.instance(getActivity()).getCopyPaths(false).size() + "）");
                    break;

                case ITEM_MUSIC:
                    vh.name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.media_plain_music, 0, 0, 0);
                    vh.name.setText("音频（" + GlobalData.Music.instance(getActivity()).getCopyPaths(false).size() + "）");
                    break;

                case ITEM_PDF:
                    vh.name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.media_plain_pdf, 0, 0, 0);
                    vh.name.setText("文档（" + GlobalData.Pdf.instance(getActivity()).getCopyPaths(false).size() + "）");
                    break;

                default:
                    break;
            }

            return v;
        }

        class ViewHolder {
            TextView name;
        }
    }

    /**
     * @author 李巷阳
     * @date 2016-11-14 下午6:14:03
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ipt_imb_refresh:
                GlobalData.searchTotal(getActivity(), false);
                break;

            default:
                break;
        }
    }
}
