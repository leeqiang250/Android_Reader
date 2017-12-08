package cn.com.pyc.pbbonline.bean.event;

public class LoginSuccessRefeshRecordEvent extends BaseOnEvent
{

	private boolean isLogin;

	public LoginSuccessRefeshRecordEvent(boolean isLogin)
	{
		super();
		this.isLogin = isLogin;
	}

	public boolean isLogin()
	{
		return isLogin;
	}
}
