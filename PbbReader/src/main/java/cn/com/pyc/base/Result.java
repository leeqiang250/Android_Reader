package cn.com.pyc.base;

import com.qlk.util.global.GlobalToast;

import android.content.Context;
import android.text.TextUtils;

/**
 * 服务器返回的suc分解为二进制位，每一位在不同业务里代表不同的含义，详见文档
 * <p>
 * 该类会初步分析错误原因或者成功提示，调用showResult可Toast出这些信息
 * 
 * @author QiLiKing 2015-7-29 下午1:16:33
 */
public abstract class Result
{
	public static final int NET_OFFLINE = -2;	// 没有可用网络1234567
	public static final int NET_ERROR = -1; // 网络连接错误
	public static final int NET_NORMAL = 0; // 网络正常连接
	protected static final int FIRST = 1;
	protected static final int SECOND = 1 << 1;
	protected static final int THIRD = 1 << 2;
	protected static final int FOURTH = 1 << 3;
	protected static final int FIFTH = 1 << 4;
	protected static final int SIXTH = 1 << 5;
	protected static final int SEVENTH = 1 << 6;
	protected static final int EIGHTH = 1 << 7;
	protected static final int NINTH = 1 << 8;
	protected static final int TENTH = 1 << 9;
	protected static final int ELEVENTH = 1 << 10;
	protected static final int TWELFTH = 1 << 11;
	protected static final int THIRTEENTH = 1 << 12;

	protected String failureReason = ""; // 业务失败的原因
	protected String successReason = "";	// 业务成功时的提示

	protected int suc;

	/** 协议中定义的业务号，特殊业务可根据此值区分 */
	protected final int type;

	public Result(int type)
	{
		this.type = type;
	}

	/**
	 * @return 网络是否正常
	 */
	public boolean valid()
	{
		return suc != NET_ERROR && suc != NET_OFFLINE;
	}

	public void setSuc(int suc)
	{
		this.suc = suc;
	}

	/**
	 * 业务是否成功（从二进制位上判断）
	 * 
	 * @return
	 */
	public boolean succeed()
	{
		return valid() && first();
	}

	/**
	 * 业务是否成功（从错误信息上判断）
	 * 
	 * @return
	 */
	public boolean isBusinessSucceed()
	{
		return TextUtils.isEmpty(getFailureReason());
	}

	/**
	 * 优先返回网络原因：“网络连接失败”，“没有可用网络”
	 */
	public String getFailureReason()
	{
		if (!TextUtils.isEmpty(failureReason))
		{
			return failureReason;
		}
		if (suc == NET_ERROR)
		{
			failureReason = "网络连接失败";
		}
		else if (suc == NET_OFFLINE)
		{
			failureReason = "没有可用网络";
		}
		return failureReason;
	}

	public void setFailureReason(String reason)
	{
		failureReason = reason;
	}

	public String getSuccessReason()
	{
		return successReason;
	}

	public void setSuccessReason(String successReason)
	{
		this.successReason = successReason;
	}

	/**
	 * 第1位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	protected boolean first()
	{
		return and(FIRST);
	}

	/**
	 * 第2位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	protected boolean second()
	{
		return and(SECOND);
	}

	/**
	 * 第3位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	protected boolean third()
	{
		return and(THIRD);
	}

	/**
	 * 第4位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	protected boolean fourth()
	{
		return and(FOURTH);
	}

	/**
	 * 第5位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	protected boolean fifth()
	{
		return and(FIFTH);
	}

	/**
	 * 第6位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	protected boolean sixth()
	{
		return and(SIXTH);
	}

	/**
	 * 第7位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	public boolean seventh()
	{
		return and(SEVENTH);
	}

	/**
	 * 第8位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	protected boolean eighth()
	{
		return and(EIGHTH);
	}

	/**
	 * 第9位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	protected boolean ninth()
	{
		return and(NINTH);
	}

	/**
	 * 第10位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	protected boolean tenth()
	{
		return and(TENTH);
	}

	/**
	 * 第11位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	protected boolean eleventh()
	{
		return and(ELEVENTH);
	}

	/**
	 * 第12位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	public boolean twelfth()
	{
		return and(TWELFTH);
	}

	/**
	 * 第13位是否是1（二进制从右往左数）
	 * 
	 * @return
	 */
	protected boolean thirteenth()
	{
		return and(THIRTEENTH);
	}

	/**
	 * 判断suc是否包含cmp（位运算）
	 */
	public boolean and(int cmp)
	{
		return suc > 0 && (cmp & suc) == cmp;
	}

	/**
	 * 将SuccessReason或者FailureReason以Toast形式显示
	 * 
	 * @param context
	 */
	public void showResult(Context context)
	{
		String show = "";
		if (succeed())
		{
			show = getSuccessReason();
		}
		else
		{
			show = getFailureReason();
		}
		if (!TextUtils.isEmpty(show))
		{
			GlobalToast.toastShort(context, show);
		}
	}
}
