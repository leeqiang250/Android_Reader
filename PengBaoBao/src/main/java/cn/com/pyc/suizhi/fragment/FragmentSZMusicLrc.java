package cn.com.pyc.suizhi.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sz.mobilesdk.util.SZLog;

import java.io.File;

import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.widget.WaveView;
import cn.com.pyc.suizhi.bean.event.MusicLrcEvent;
import cn.com.pyc.suizhi.manager.LrcEngine;
import cn.com.pyc.suizhi.widget.lrc.LrcView;
import de.greenrobot.event.EventBus;

/**
 * 歌词的展现界面
 */
public class FragmentSZMusicLrc extends BaseSZFragment {

    private LrcView mLrcView;
    private WaveView mWaveView;

    private String myProId;
    private String lrcId;

    private String lrcPath;
    private boolean hasLoadLrc = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.myProId = bundle.getString(BaseSZFragment.MUSIC_MYPRO_ID);
            this.lrcId = bundle.getString(BaseSZFragment.MUSIC_LRC_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_music_lyc, container, false);
        mWaveView = (WaveView) rootView.findViewById(R.id.lrc_wave_view);
        mLrcView = (LrcView) rootView.findViewById(R.id.lrc_view);
        //initLrcView(mLrcView);
        if (LrcEngine.existLyric(myProId, lrcId)) {
            lrcPath = LrcEngine.getLyricPath(myProId, lrcId);
            loadLrc(lrcPath);
        }
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mLrcView != null) {
            if (!hasLoadLrc) {
                hasLoadLrc = true;
                lrcPath = LrcEngine.getLyricPath(myProId, lrcId);
                mLrcView.loadLrc(new File(lrcPath));
            }
            //ViewUtil.showWidget(mWaveView);
        }
    }

//    private void initLrcView(LyricView mLrcView) {
//        mLrcView.setHighLightTextColor(getResources().getColor(R.color.brilliant_blue));
//        mLrcView.setLineSpace(16f);
//        mLrcView.setTextSize(16f);
//    }

    private void loadLrc(String lrcPath) {
        if (mLrcView != null && !hasLoadLrc) {
            hasLoadLrc = true;
            mLrcView.loadLrc(new File(lrcPath));
            SZLog.d("loadLrc: " + lrcPath);
        }
    }

    private void resetLrc() {
        if (hasLoadLrc) {
            hasLoadLrc = false;
        }
    }

    /**
     * 歌词处理
     *
     * @param event
     */
    public void onEventMainThread(MusicLrcEvent event) {
        MusicLrcEvent.Way way = event.getWay();
        Object obj = event.getObj();
        if (way == MusicLrcEvent.Way.LRC_LOAD) {                //加载歌词
            lrcPath = ((String) obj);
            loadLrc(lrcPath);
        } else if (way == MusicLrcEvent.Way.LRC_UPDATE) {       //更新歌词
            long time = ((Long) obj);
            mLrcView.updateTime(time);
        } else if (way == MusicLrcEvent.Way.LRC_CHANGE) {       //切换歌词
            String lrcId = ((String) obj);
            //if (!TextUtils.equals(lrcId, this.lrcId)) { //同一首歌歌词需要重新播放，不能判重
            this.lrcId = lrcId;
            resetLrc();
            LrcEngine.getLyric(myProId, lrcId);
            //}
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
