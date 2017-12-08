package cn.com.pyc.suizhi.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.suizhi.bean.DrmFile;

/**
 * Created by hudaqiang on 2017/8/28.
 */

public class BaseSZFragment extends Fragment {

    public static final String MUSIC_ALBUM_NAME = "music_album_name";
    public static final String MUSIC_IMAGE_URL = "music_image_url";
    public static final String MUSIC_CONTENT_LIST = "music_content_list";

    public static final String MUSIC_MYPRO_ID = "music_myPro_id";
    public static final String MUSIC_LRC_ID = "music_lrc_id";

    protected static final int TAG_IMAGE = 0;   //图片
    protected static final int TAG_LRC = 1;     //歌词
    protected static final int TAG_LIST = 2;    //列表

    /**
     * 专辑图片
     *
     * @param albumName
     * @param imageUrl
     * @return
     */
    public static Fragment newInstanceImage(String albumName, String imageUrl) {
        FragmentSZMusicImg fragmentMusicImg = new FragmentSZMusicImg();
        Bundle bundle = new Bundle();
        bundle.putString(MUSIC_ALBUM_NAME, albumName);
        //bundle.putString(MUSIC_NAME, musicName);
        bundle.putString(MUSIC_IMAGE_URL, imageUrl);
        fragmentMusicImg.setArguments(bundle);
        return fragmentMusicImg;
    }

    /**
     * 歌词
     *
     * @param myProId
     * @param lrcId
     * @return
     */
    public static Fragment newInstanceLrc(String myProId, String lrcId) {
        FragmentSZMusicLrc fragmentMusicLrc = new FragmentSZMusicLrc();
        Bundle bundle = new Bundle();
        bundle.putString(MUSIC_MYPRO_ID, myProId);
        bundle.putString(MUSIC_LRC_ID, lrcId);
        fragmentMusicLrc.setArguments(bundle);
        return fragmentMusicLrc;
    }

    /**
     * 列表
     *
     * @param contents
     * @return
     */
    public static Fragment newInstanceList(List<DrmFile> contents) {
        FragmentSZMusicList fragmentMusicList = new FragmentSZMusicList();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MUSIC_CONTENT_LIST, (ArrayList<? extends Parcelable>)
                contents);
        fragmentMusicList.setArguments(bundle);
        return fragmentMusicList;
    }

    protected Fragment newInstance(int tag, Bundle bundle) {
        if (tag == TAG_IMAGE) {
            FragmentSZMusicImg fragmentMusicImg = new FragmentSZMusicImg();
            fragmentMusicImg.setArguments(bundle);
            return fragmentMusicImg;
        } else if (tag == TAG_LRC) {
            FragmentSZMusicLrc fragmentMusicLrc = new FragmentSZMusicLrc();
            fragmentMusicLrc.setArguments(bundle);
            return fragmentMusicLrc;
        } else if (tag == TAG_LIST) {
            FragmentSZMusicList fragmentMusicList = new FragmentSZMusicList();
            fragmentMusicList.setArguments(bundle);
            return fragmentMusicList;
        } else {
            throw new IllegalArgumentException("params 'tag' not right.");
        }
    }

}
