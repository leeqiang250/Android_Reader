package cn.com.pyc.conn;

import cn.com.pyc.base.Result;
import cn.com.pyc.bean.ProtocolInfo;
import cn.com.pyc.bean.SmInfo;
import android.text.TextUtils;

public class SmResult extends Result
{
	private OpenFailure failure = OpenFailure.Unknown;
	public static final int NEED_VERIFY = 1 << 20;		// 需要离线验证
	public static final int NEED_SECURITY = 1 << 21;		// 需要验证码
	public static final int DEVICE_CHANGED = 1 << 22;		// 离线文件移动到其他设备上了
	public static final int PERMISSION_DENIED_WRITE = 1 << 23;	// 没有写权限
	public static final int CAN_OPEN = FIRST;

	public SmResult(int type)
	{
		super(type);
	}

	private SmInfo smInfo;

	public int getSuc()
	{
		return suc;
	}
	
	public void setFailure(OpenFailure failure)
	{
		this.failure = failure;
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
			failure = OpenFailure.NeedUpdate;
			return "需下载新版本";
		}

		failureReason = "服务器繁忙，请稍后再试。(errCode：suc=" + suc + ")"; // 设定默认返回。（没有意义，就去掉了）
		// 专用提示
		switch (type)
		{
			case ProtocolInfo.TYPE_MODIFY_LIMIT:
				if (fifth())
				{
					failureReason = "文件不是您的";
				}
				else if (thirteenth())
				{
					failureReason = "文件已被删除";
				}
				break;

			case ProtocolInfo.TYPE_APPLY_ACTIVATE:
				if (thirteenth())
				{
					failureReason = "文件已被删除";
				}
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_GET_ACTIVATE_INFO:
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_GET_SM_INFO:
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_MAKE_FREE_FILE:
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_MAKE_PAY_FILE:
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_OPEN_FILE:
				getScanFileFailedReason();
				break;

			case ProtocolInfo.TYPE_STOP_READ:
				// 无失败提示
				break;

			case ProtocolInfo.TYPE_UPLOAD_HASH:
				// 无失败提示
				break;
			case ProtocolInfo.TYPE_SEND_PHONE_SECURITY_CODE:
				// 无失败提示
				break;
			case ProtocolInfo.TYPE_GET_PHONE_SECURITY_CODE:
				if (suc == 256)
				{
					failureReason = "验证失败，该手机号已使用";
				}
				break;

			default:
				failureReason = "未知请求类型(" + type + ")";
				break;
		}

		return failureReason;
	}

	private void getScanFileFailedReason()
	{
		if (and(NEED_SECURITY))
		{
			failure = OpenFailure.NeedPhone;
			failureReason = "需要验证码";
			return;
		}

		if (and(NEED_VERIFY))
		{
			failure = OpenFailure.NeedVerify;
			failureReason = "";
			// //已经跳到验证界面了，没必要再提示，而且这么长描述，也看不过来
			// failureReason = "本地时间改动过，需要重新验证阅读条件！";
			return;
		}

		if (and(DEVICE_CHANGED))
		{
			failure = OpenFailure.DeviceChanged;
			failureReason = "绑定设备不符，不能阅读";
			return;
		}

		if (and(PERMISSION_DENIED_WRITE))
		{
			failure = OpenFailure.PermissionDenied;
			failureReason = "该离线文件没有写的权限";
			return;
		}

		if (eighth())
		{
			// 自由传播，首先看作者是否禁止
			if (fifth())
			{
				failureReason = "制作者不允许查看";
			}
			else
			{
				if (third() || fourth())
				{
					failureReason = "文件时效已过";
				}
			}
		}
		else
		{
			if (seventh())
			{
				failureReason = "等待卖家激活";
				failure = OpenFailure.WaitForActive;
				if(twelfth())
				{
					//自动激活
					failureReason = null;
					failure = OpenFailure.AutoActive;
				}
			}
			else
			{
				failureReason = "需要申请激活";
				failure = OpenFailure.NeedApply;
			}
		}
	}

	public enum OpenFailure
	{
		NeedPhone, NeedVerify, DeviceChanged, PermissionDenied, NeedApply, WaitForActive, NeedUpdate, LimitOut,AutoActive,
		/** Unknown直接toast即可 */
		Unknown
	}

	public OpenFailure whyOpenFailed()
	{
		getFailureReason();
		return failure;
	}

	// 起个别名
	public boolean canOpen()
	{
		return succeed();
	}

	public boolean needApply()
	{
		return seventh() || (isPayFile() && !canOpen());	// 其实第二个判断条件就足够了
	}

	public boolean svn()
	{
		return seventh();
	}

	public boolean isPayFile()
	{
		return !eighth();
	}

	// 制作者是否禁止阅读
	public boolean isUserStopRead()
	{
		return fifth();
	}

	public SmInfo getSmInfo()
	{
		return smInfo;
	}

	public void setSmInfo(SmInfo smInfo)
	{
		this.smInfo = smInfo;
	}

}
