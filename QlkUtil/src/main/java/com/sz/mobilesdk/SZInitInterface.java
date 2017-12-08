package com.sz.mobilesdk;

import android.content.Context;

import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SPUtil;

/**
 * 初始数据操作接口
 * 
 * @author hudq
 * 
 */
public final class SZInitInterface
{

	public static boolean isDebugMode = true;
	public static boolean isDBEncrypt = true;

	/**
	 * 设置是否是调试状态。true时打印日志信息；false反之。<br/>
	 * 
	 * 默认状态开启；线上请设置false。
	 * 
	 * @param isDebugMode
	 */
	public static void setDebugMode(boolean isDebugMode)
	{
		SZInitInterface.isDebugMode = isDebugMode;
	}

	/**
	 * 设置数据库是否加密。默认加密true
	 * 
	 * @param isDBEncrypt
	 */
	public static void setDBEncrypt(boolean isDBEncrypt)
	{
		SZInitInterface.isDBEncrypt = isDBEncrypt;
	}

	/**
	 * 初始化操作,在Application创建时调用。
	 * 
	 * @param mContext
	 */
	public static void init(Context mContext)
	{
		Constant.init(mContext);
		PathUtil.createCacheDirectory();
	}

	/**
	 * 保存用户名 <br/>
	 * 
	 * 登录成功后调用,用来保存用户名。
	 * 
	 * @param name
	 */
	public static void saveUserName(String name)
	{
		if (name == null)
			throw new IllegalArgumentException("name is not allow null");
		SPUtil.save(Fields.FIELDS_USER_NAME, name);
	}

	/**
	 * 获取用户名 <br/>
	 * 
	 * 读取保存的用户名。
	 * 
	 * @param defaultName
	 */
	public static String getUserName(String defaultName)
	{
		if (defaultName == null)
			throw new IllegalArgumentException("defaultName is not allow null");
		return (String) SPUtil.get(Fields.FIELDS_USER_NAME, defaultName);
	}

	/**
	 * 清除所有保存在sp中的数据
	 */
	public static void clear()
	{
		SPUtil.clear();
	}

	/**
	 * 创建文件保存目录<br/>
	 * 
	 * eg：name = Fields.GUEST_PBB + Fields._ + shareId
	 */
	public static void createFilePath(String name)
	{
		PathUtil.createSaveFilePath(name);
	}

	/**
	 * 检验文件存储路径
	 */
	public static void checkFilePath()
	{
		String shareId = (String) SPUtil.get(Fields.FIELDS_ID, "");
		String userName = getUserName("");
		PathUtil.checkSaveFilePath(userName + Fields._LINE + shareId);
	}

	/**
	 * 销毁保存文件的目录值<br/>
	 * 
	 * 注销、切换账号时需要调用
	 */
	public static void destoryFilePath()
	{
		PathUtil.destorySaveFilePath();
	}

}
