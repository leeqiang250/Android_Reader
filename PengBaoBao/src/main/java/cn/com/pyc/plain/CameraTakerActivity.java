package cn.com.pyc.plain;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.qlk.util.tool.Util.ScreenUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;

import cn.com.pyc.pbb.R;
import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.media.MyBitmapFactory;
import cn.com.pyc.media.PycImage;
import cn.com.pyc.sm.PayLimitConditionActivity;
import cn.com.pyc.utils.Dirs;
import cn.com.pyc.xcoder.XCodeView;
import cn.com.pyc.xcoder.XCodeView.XCodeType;
import cn.com.pyc.xcoder.XCodeView.XCodeViewListener;
import cn.com.pyc.xcoder.XCoder.ReturnInfo;
/*-
 * 本程序拍照录像皆由此类完成
 * 
 * 拍照和录像的路径由自己定义
 */
public class CameraTakerActivity extends ExtraBaseActivity
{
	private String g_strMediaPath;
	private boolean isFromSm;
	private boolean isTakePhoto;

	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		isFromSm = getIntent().getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, true);		//默认外发
		isTakePhoto = GlobalData.Image.equals(getIntent().getSerializableExtra(
				GlobalIntentKeys.BUNDLE_OBJECT_MEDIA_TYPE));
		if (isTakePhoto)
		{
			takePhoto();
		}
		else
		{
			takeVideo();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK)
		{
			// 如果还有其他类型的request，则再细分
			completed();
//			complet();
		}
		else
		{
			finish();
		}
	}

	/*private void complet() {
		*//*GlobalData.ensure(CameraTakerActivity.this, g_strMediaPath).getCopyPaths(false)
				.add(0, g_strMediaPath);*//*


		Intent intent = new Intent(CameraTakerActivity.this, PayLimitConditionActivity.class);
		intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, g_strMediaPath);
		intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
		setResult(102,intent);

	}*/

	private void completed()
	{
		GlobalData.ensure(CameraTakerActivity.this, g_strMediaPath).getCopyPaths(false)
				.add(0, g_strMediaPath);


		setContentView(R.layout.activity_camera_preview);
		if (isFromSm)
		{
			// 如果是从sm类过来的，则不能加密，只能发送
			findViewById(R.id.acp_imb_encrypt).setVisibility(View.GONE);
		}
		else
		{
			// 如果是从加解密类过来的，则不能发送，只能加密
			findViewById(R.id.acp_imb_send).setVisibility(View.GONE);
		}
		ImageView imvBitmap = (ImageView) findViewById(R.id.acp_imv_bm);

		// 外发
		findViewById(R.id.acp_imb_send).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
//				Intent intent = new Intent(CameraTakerActivity.this, ChooseSMwayActivity.class);
				Intent intent = new Intent(CameraTakerActivity.this, PayLimitConditionActivity.class);
				intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, g_strMediaPath);
				intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
				startActivity(intent);

			}
		});


		// 显示预览
		if (PycImage.isSameType1(g_strMediaPath))
		{
			// 拍照
			Bitmap bm = MyBitmapFactory.getBitmap(g_strMediaPath,
					ScreenUtil.getScreenWidth(CameraTakerActivity.this),
					ScreenUtil.getScreenHeight(CameraTakerActivity.this));
			imvBitmap.setImageBitmap(bm);
		}
		else
		{
			// 录像
			Bitmap bm = MyBitmapFactory.getVideoThumbnail(g_strMediaPath,
					ScreenUtil.getScreenWidth(CameraTakerActivity.this));
			imvBitmap.setImageBitmap(bm);
			findViewById(R.id.acp_imv_play).setVisibility(View.VISIBLE);
		}

		findViewById(R.id.acp_imb_encrypt).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				encrypt();
			}
		});
	}

	private void encrypt()
	{
		XCodeView codeView = new XCodeView(this);
		codeView.startXCode(XCodeType.Encrypt, null, true, g_strMediaPath);
		codeView.setXCodeViewListener(new XCodeViewListener()
		{
			@Override
			public void onFinished(ReturnInfo info)
			{
				finish();
			}

			@Override
			public void onError(int error)
			{
			}
		});
	}

	@Override
	public void update(Observable observable, Object data)
	{
		super.update(observable, data);
		if (data.equals(ObTag.Encrypt) || data.equals(ObTag.Make))
		{
			finish();
		}
	}

	private void takePhoto()
	{
		String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date())
				+ ".jpg";
		g_strMediaPath = Dirs.getCameraDir(Dirs.getDefaultBoot()) + "/pbb_" + name;
		Uri uri = Uri.parse("file://" + g_strMediaPath);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, 0);
	}

	private void takeVideo()
	{
		String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date())
				+ ".mp4";
		g_strMediaPath = Dirs.getCameraDir(Dirs.getDefaultBoot()) + "/pbb_" + name;
		Uri uri = Uri.parse("file://" + g_strMediaPath);
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(intent, 1);
	}

}
