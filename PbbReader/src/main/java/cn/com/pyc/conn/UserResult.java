package cn.com.pyc.conn;

import cn.com.pyc.base.Result;
import cn.com.pyc.bean.ProtocolInfo;
import cn.com.pyc.bean.UserInfo;
import android.text.TextUtils;

public class UserResult extends Result
{
	private UserInfo userInfo;

	public UserResult(int type)
	{
		super(type);
	}

	@Override
	public String getFailureReason()
	{
		super.getFailureReason();
		if (suc < 0 || !TextUtils.isEmpty(failureReason))
		{
			return failureReason;
		}

		// 通用提示
		if (suc == 0)
		{
			return "操作不成功";
		}

		if (tenth())
		{
			return "需下载新版本";
		}

		failureReason = "服务器繁忙，请稍后再试。(errCode：suc=" + suc + ")";		// 设定默认返回

		switch (type)
		{
			case ProtocolInfo.TYPE_BIND_EMAIL:
				if (fourth())
				{
					failureReason = "邮箱已验证";
				}
				if (seventh())
				{
					failureReason = "邮箱已被使用";
				}
				break;

			case ProtocolInfo.TYPE_BIND_QQ:
				if (sixth())
				{
					failureReason = "QQ已验证";
				}
				if (eighth())
				{
					failureReason = "QQ已被使用";
				}
				break;

			case ProtocolInfo.TYPE_FIND_KEY_BACK:
				if (second())
				{
					failureReason = "用户名不存在或者密码错误";
				}
				if (third())
				{
					failureReason = "用户没有开通";
				}
				break;

			case ProtocolInfo.TYPE_FIND_PSD_BACK:
				if (second())
				{
					failureReason = "没有该用户";
				}
				else
				{
					if (fourth())
					{
						failureReason = "该邮箱未验证，不能找回密码";
					}
				}
				break;

			case ProtocolInfo.TYPE_GET_USER_INFO:
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_MODIFY_NICK:
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_MODIFY_PSD:
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_QQ_FIND_KEY_BACK:
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_QQ_LOGIN:
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_QQ_REGISTER:
				if (eighth())
				{
					failureReason = "该QQ已使用";
				}
				break;

			case ProtocolInfo.TYPE_REGISTER:
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_SYNCHRONIZE_PSD:
				// 无失败提示
				break;

			default:
				break;
		}

		return failureReason;
	}

	public boolean isEmailBinded()
	{
		return fourth();
	}

	public boolean isPhoneBinded()
	{
		return fifth();
	}

	public boolean isQQBinded()
	{
		return sixth();
	}

	public UserInfo getUserInfo()
	{
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo)
	{
		this.userInfo = userInfo;
	}

}
