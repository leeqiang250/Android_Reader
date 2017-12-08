package cn.com.pyc.suizhi.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sz.mobilesdk.manager.ImageLoadHelp;

import cn.com.pyc.pbb.R;
import cn.com.pyc.suizhi.bean.event.MusicChangeNameEvent;
import de.greenrobot.event.EventBus;

/**
 * 音乐的图片展现界面
 */
public class FragmentSZMusicImg extends BaseSZFragment {

    private static final String TAG = "FragmentMusicImg";

    private TextView textName;

    private String imageUrl;
    private String albumName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.albumName = bundle.getString(BaseSZFragment.MUSIC_ALBUM_NAME);
            this.imageUrl = bundle.getString(BaseSZFragment.MUSIC_IMAGE_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_img, container, false);
        ((TextView) rootView.findViewById(R.id.music_albumName)).setText(albumName);
        ImageView ivImage = (ImageView) rootView.findViewById(R.id.music_albumImage);
        //ImageLoader.getInstance().displayImage(imageUrl, ivImage);
        ImageLoadHelp.loadImage(ivImage,imageUrl);
        textName = (TextView) rootView.findViewById(R.id.music_name);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 修改歌曲名称
     *
     * @param event
     */
    public void onEventMainThread(MusicChangeNameEvent event) {
        if (textName != null) {
            textName.setText(event.getMusicName());
        }
    }


    /*
     * 刷新专辑图片
     *
     * @param event
     */
//    public void onEventMainThread(MusicRefreshImgEvent event) {
//        if (ivImage != null) {
//            String path = event.getPictureUrl();
//            if (path == null) return;
//            DRMLog.d(TAG, "path: " + path);
//            if (path.startsWith("http://")) {
//                //url
//                if (!isImgLoaded)
//                    ImageLoader.getInstance().displayImage(path, ivImage);
//            } else {
//                //本地路径
//                if (!isImgLoaded)
//                    ImageLoader.getInstance().displayImage("file://" + path, ivImage);
//            }
//        }
//    }
}
