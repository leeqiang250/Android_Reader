package cn.com.pyc.pbbonline.fragment;

import java.io.File;

import org.xutils.x;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.event.MusicCircleEvent;
import cn.com.pyc.pbbonline.util.BitmapHelp;

import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.StringUtil;

import de.greenrobot.event.EventBus;

/**
 * 音乐的图片展现界面
 * 
 * @author lixiangyang
 */
public class FragmentMusicImg extends BaseFragment
{

	private static ImageView ivImage;
	//private static TextView tvName;

	//private String name;
	private String imgUrl;
	private boolean isPermit;
	private boolean isImgLoaded = false;
	private ObjectAnimator anim;

	public boolean isImgLoaded()
	{
		return isImgLoaded;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getArguments();
		if (bundle != null)
		{
			this.imgUrl = bundle.getString(MusicViewPagerAdaper.IMGPATH_TAG);
			this.isPermit = bundle.getBoolean(MusicViewPagerAdaper.PERMIT_TAG);
			//this.name = bundle.getString(MusicViewPagerAdaper.NAME_TAG);
		}
		SZLog.i("imgUrl = " + imgUrl);
		SZLog.i("isPermit = " + isPermit);
		//SZLog.i("name = " + name);
		EventBus.getDefault().register(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.pbbonline_fragment_music_img, null, false);
		ivImage = (ImageView) rootView.findViewById(R.id.albumImageView);
		//tvName = (TextView) rootView.findViewById(R.id.textMusic_name);
		//tvName.setText(name);
		if (StringUtil.isEmptyOrNull(imgUrl))
		{
			ivImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_music_play_circle));
			animPlay(ivImage, isPermit);
		}
		else
		{
			String fileName = FileUtil.getNameFromFilePath(imgUrl);
			String filePath = PathUtil.getSZFuzzyCachePath() + File.separator + fileName;
			File file = new File(filePath);
			if (file.exists())
			{
				isImgLoaded = true;
				x.image().bind(ivImage, filePath, BitmapHelp.getImageOptions());
			}
		}
		return rootView;
	}

	/**
	 * ImageUtils.getGaussambiguity()下载图片成功后调用<br>
	 * 通过url设置音乐专辑图片
	 * 
	 * @param image
	 */
	public void setImageWithUrl(String imageUrl)
	{
		String fileName = FileUtil.getNameFromFilePath(imageUrl);
		String filePath = PathUtil.getSZFuzzyCachePath() + File.separator + fileName;
		File file = new File(filePath);
		if (ivImage != null && !isImgLoaded)
		{
			if (file.exists())
			{
				x.image().bind(ivImage, filePath, BitmapHelp.getImageOptions());
			}
			else
			{
				x.image().bind(ivImage, imageUrl, BitmapHelp.getImageOptions());
			}
		}
	}

	/**
	 * 本地加载音乐专辑图片
	 * 
	 * @param filePath
	 *            本地缓存图片路径
	 */
	public void setImageWithFilePath(String filePath)
	{
		if (ivImage != null && !isImgLoaded)
		{
			x.image().bind(ivImage, filePath, BitmapHelp.getImageOptions());
		}
	}

	/**
	 * 设置音乐名称
	 * 
	 * @param name
	 */
	//	public void switchMusicName(String name)
	//	{
	//		if (tvName != null)
	//			tvName.setText(name);
	//	}

	private void animPlay(View targetView, boolean isPermit)
	{
		if (!isPermit)
			return;
		if (anim != null && anim.isRunning())
		{
			//empty code ,running...
		}
		else
		{
			anim = ObjectAnimator.ofFloat(targetView, "rotation", 0, 360);
			anim.setDuration(8000);
			anim.setRepeatCount(ValueAnimator.INFINITE);
			anim.setStartDelay(800);
			anim.setRepeatMode(ObjectAnimator.RESTART);
			anim.setInterpolator(new LinearInterpolator());
			anim.start();
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public void onEventMainThread(MusicCircleEvent event)
	{
		boolean isPlay = event.isPlay();
		if (isPlay)
		{
			if (anim != null && anim.isPaused())
			{
				anim.resume();
			}
			else if (anim == null && ivImage != null)
			{
				animPlay(ivImage, isPermit);
			}
		}
		else
		{
			if (anim != null && anim.isStarted())
			{
				anim.pause();
			}
		}
	}

}
