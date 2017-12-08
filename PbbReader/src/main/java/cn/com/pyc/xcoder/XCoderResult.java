package cn.com.pyc.xcoder;

import cn.com.pyc.base.Result;
import cn.com.pyc.bean.SmInfo;

/*-
 * 参见其父类Result
 */
public class XCoderResult extends Result
{
	public static final int SUCCESS = FIRST;
	public static final int FILE_ERROR = SECOND;	//文件读写错误IO
	public static final int PYC_ERROR = THIRD;	//文件标识错误 不是PYC文件
	public static final int PRMT_ERROR = FOURTH;	//参数错误 文件长度或者加密长度
	public static final int SPACE_ERROR = FIFTH;	//空间不足
	public static final int OWNER_ERROR = SIXTH;	//文件不是本人的
	public static final int COPY_ERROR = SEVENTH;	//文件复制失败，此时不应删除原文件
	
	private SmInfo smInfo;

	/**
	 * 默认操作成功
	 */
	public XCoderResult()
	{
		super(0);
		setSuc(SUCCESS);	//默认成功
	}
	
	public int getError()
	{
		return suc;
	}
	

	@Override
	public String getFailureReason()
	{
		if (second())
		{
			failureReason += "读写文件出错";
		}

		if (third())
		{
//			failureReason += "文件标识错误";
			failureReason+="读取文件失败。可能错误原因：文件下载不完整，请重新下载。";

		}

		if (fourth())
		{
//			failureReason += "密文参数错误";
			failureReason+="读取文件失败。可能错误原因：文件下载不完整，请重新下载。";

		}

		if (fifth())
		{
			failureReason += "空间不足";
		}

		if (sixth())
		{
			failureReason += "文件不是你的";
		}

		if (seventh())
		{
			failureReason += "文件生成失败";
		}

		return failureReason;
	}

	@Override
	public boolean succeed()
	{
		return suc == SUCCESS;
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
