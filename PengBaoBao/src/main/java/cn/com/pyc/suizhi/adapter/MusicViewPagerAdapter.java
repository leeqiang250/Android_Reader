package cn.com.pyc.suizhi.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.List;

import cn.com.pyc.suizhi.bean.DrmFile;
import cn.com.pyc.suizhi.fragment.BaseSZFragment;

public class MusicViewPagerAdapter extends FragmentPagerAdapter {

    // 图片,歌词,列表
    private static final int FRAGMENT_NUM = 3;

    private String myProId;
    private String albumName;
    private String imageUrl;
    private String lrcId;
    private List<DrmFile> contents;

    public MusicViewPagerAdapter(FragmentManager fm, Bundle bundle) {
        super(fm);
        if (bundle == null)
            throw new IllegalArgumentException("bundle required init.");

        this.albumName = bundle.getString(BaseSZFragment.MUSIC_ALBUM_NAME);
        this.imageUrl = bundle.getString(BaseSZFragment.MUSIC_IMAGE_URL);
        this.contents = bundle.getParcelableArrayList(BaseSZFragment.MUSIC_CONTENT_LIST);

        this.myProId = bundle.getString(BaseSZFragment.MUSIC_MYPRO_ID);
        this.lrcId = bundle.getString(BaseSZFragment.MUSIC_LRC_ID);
    }

    @Override
    public Fragment getItem(int arg0) {
        switch (arg0) {
            case 0:
                return BaseSZFragment.newInstanceImage(albumName, imageUrl);
            case 1:
                return BaseSZFragment.newInstanceLrc(myProId, lrcId);
            case 2:
                return BaseSZFragment.newInstanceList(contents);
            default:
                Log.e("", "Fragment getItem error");
                break;
        }
        return null;
    }

    @Override
    public int getCount() {
        return FRAGMENT_NUM;
    }

}
