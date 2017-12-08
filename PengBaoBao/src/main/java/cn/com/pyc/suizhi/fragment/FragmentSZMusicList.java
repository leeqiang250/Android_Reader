package cn.com.pyc.suizhi.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qlk.util.tool.ViewHolder;
import com.sz.help.KeyHelp;
import com.sz.view.widget.AVLoadingIndicatorView;
import com.sz.view.widget.ToastShow;

import java.util.List;

import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ValidDateUtil;
import cn.com.pyc.suizhi.bean.DrmFile;
import cn.com.pyc.suizhi.bean.event.MusicCurrentPlayEvent;
import cn.com.pyc.suizhi.common.MusicMode;
import cn.com.pyc.suizhi.help.MusicHelp;
import cn.com.pyc.suizhi.service.MusicPlayService;
import de.greenrobot.event.EventBus;

/**
 * fragment music 列表
 */
public class FragmentSZMusicList extends BaseSZFragment implements AdapterView.OnItemClickListener {

    private Activity mActivity;
    private List<DrmFile> contents;

    private ListView mListView;
    private MusicListAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            contents = bundle.getParcelableArrayList(BaseSZFragment.MUSIC_CONTENT_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_music_list, container, false);
        mListView = (ListView) rootView.findViewById(R.id.music_listview);
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
        mListView.setOnItemClickListener(null);
        if (contents != null)
            contents.clear();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new MusicListAdapter(mActivity, contents);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser && mAdapter != null) {
//            mAdapter.setCurrentItemId(MusicHelp.getCurrentPlayId(mActivity));
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private class MusicListAdapter extends BaseAdapter {

        private Context ctx;
        private List<DrmFile> contents;
        private String currentItemId;

        private int selectColorId;
        private int unSelectNameColorId;
        private int unSelectTimeColorId;
        private Drawable selectDrawable;
        private Drawable unSelectDrawable;

        private void setCurrentItemId(String currentItemId) {
            if (TextUtils.equals(this.currentItemId, currentItemId))
                return;
            this.currentItemId = currentItemId;
            notifyDataSetChanged();
        }

        private MusicListAdapter(Context ctx, List<DrmFile> contents) {
            this.ctx = ctx;
            this.contents = contents;

            Resources resources = this.ctx.getResources();
            this.selectColorId = resources.getColor(R.color.brilliant_blue);
            this.unSelectNameColorId = resources.getColor(R.color.white);
            this.unSelectTimeColorId = resources.getColor(R.color.gray);
            this.selectDrawable = resources
                    .getDrawable(R.drawable.ic_validate_time_select);
            this.unSelectDrawable = resources
                    .getDrawable(R.drawable.ic_validate_time_nor);
        }

        @Override
        public int getCount() {
            return contents != null ? contents.size() : 0;
        }

        @Override
        public DrmFile getItem(int position) {
            return contents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, android.view.View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(ctx, R.layout.item_music_list, null);
            }
            TextView tvName = ViewHolder.get(convertView, R.id.music_list_name);
            TextView tvTime = ViewHolder.get(convertView, R.id.music_list_text_status);
            ImageView ivTime = ViewHolder.get(convertView, R.id.music_list_img_status);
            AVLoadingIndicatorView avLoading = ViewHolder.get(convertView, R.id.music_list_working);
            DrmFile ac = contents.get(position);
            tvName.setText(ac.getFileName());
            //SZContent szCont = new SZContent(ac.getAsset_id());
            tvTime.setText(ValidDateUtil.getValidTime(ctx, ac.getValidityTime(), ac
                    .getEndDatetime()));

            boolean equals = TextUtils.equals(currentItemId, ac.getFileId());
            tvName.setTextColor(equals ? selectColorId : unSelectNameColorId);
            tvTime.setTextColor(equals ? selectColorId : unSelectTimeColorId);
            ivTime.setImageDrawable(equals ? selectDrawable : unSelectDrawable);
            avLoading.setVisibility(equals ? View.VISIBLE : View.GONE);

            return convertView;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mAdapter == null) return;
        DrmFile ac = mAdapter.getItem(position);
        if (!ac.isCheckOpen()) {
            ToastShow.getToast().showBusy(mActivity, getString(R.string.file_expired));
            return;
        }
        if (ac.isInEffective()) {
            ToastShow.getToast().showBusy(mActivity, getString(R.string.file_ineffective));
            return;
        }
        if (MusicHelp.isSameMusic(ac.getFileId())) return;

        MusicHelp.stop(mActivity);
        mAdapter.setCurrentItemId(ac.getFileId());

        Intent intent = new Intent(mActivity, MusicPlayService.class);
        intent.putExtra(KeyHelp.MPS_FILE_ID, ac.getFileId());
        intent.putExtra(KeyHelp.MPS_OPTION, MusicMode.Status.PLAY);
        mActivity.startService(intent);
    }

    public void onEventMainThread(MusicCurrentPlayEvent event) {
        if (mAdapter != null) {
            mAdapter.setCurrentItemId(event.getFileId());
        }
    }
}
