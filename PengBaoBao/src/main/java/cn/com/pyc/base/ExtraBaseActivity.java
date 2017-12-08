package cn.com.pyc.base;

import java.util.Observable;

import cn.com.pyc.global.ObTag;
import cn.com.pyc.main.PycMainActivity;
import cn.com.pyc.user.InsertPsdActivity;

public class ExtraBaseActivity extends PbbBaseActivity
{
	@Override
	public void update(Observable observable, Object data)
	{
		if (data.equals(ObTag.Psd))
		{
			// 此步操作由CipherTotalActivity引起,如果仍在隐私空间则会发送此通知
			if (this instanceof PycMainActivity || this instanceof InsertPsdActivity)
			{
				return;
			}
			finish();
		} 
	}

}
