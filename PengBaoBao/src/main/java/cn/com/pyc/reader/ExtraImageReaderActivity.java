package cn.com.pyc.reader;

import java.util.ArrayList;

import android.content.Intent;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.media.ExtraDelSndFile;
import cn.com.pyc.reader.image.ImageReaderActivity;
import cn.com.pyc.sm.PayLimitConditionActivity;
import cn.com.pyc.xcoder.XCodeView;
import cn.com.pyc.xcoder.XCodeView.XCodeType;

public class ExtraImageReaderActivity extends ImageReaderActivity
{
	protected void send(String path)
	{
		ArrayList<String> paths = new ArrayList<String>();
		paths.add(path);
		new ExtraDelSndFile().sendFiles(this, paths, GlobalData.Image);
	}

	protected void shareSmFile(String path)
	{
//		Intent intent = new Intent(this, ChooseSMwayActivity.class);
		Intent intent = new Intent(this, PayLimitConditionActivity.class);
		intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, path);
		intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, isCipher);
		startActivity(intent);
	}

	protected void decrypt(String path)
	{
		new XCodeView(this).startXCode(XCodeType.Decrypt, null, true, path);
	}

}
