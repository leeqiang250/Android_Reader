package cn.com.pyc.pbbonline.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import cn.com.pyc.pbb.reader.R;

/**
 * 歌词的展现界面
 * 
 * @author lixiangyang
 * 
 */
public class FragmentMusicLyc extends BaseFragment
{

//	private LyricShow lyricShow;
	private View view;
	private String imageUrl;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getArguments();
		if (bundle != null)
			this.imageUrl = bundle.getString(MusicViewPagerAdaper.IMGPATH_TAG);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		view = inflater.inflate(R.layout.pbbonline_fragment_music_lyc, container, false);

		// lyricShow = (LyricShow) view.findViewById(R.id.LyricShow);
		return view;
	}

	// public void sync(Vector<Timelrc> list, int currentPos) {
	//
	// if (lyricShow != null) {
	// lyricShow.SetTimeLrc(list);
	// lyricShow.SetNowPlayIndex(currentPos);
	// }
	// }
}
