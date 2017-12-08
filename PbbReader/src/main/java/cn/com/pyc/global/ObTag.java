package cn.com.pyc.global;

public enum ObTag
{
	/**
	 * sd卡可以使用
	 */
	SdCardOn,

	/**
	 * sd卡不能使用
	 */
	SdCardOff,

	/**
	 * 正在打电话
	 */
	PhoneOn,

	/**
	 * 电话已结束
	 */
	PhoneOff,

	/** 通知，包含个数 */
	Notice,

	/** 解密，包括来自reader的 */
	Decrypt,

	/** 刷新。如果是外发刷新则有单个和批量（单个应该有smInfo，通过fid来辨认） */
	Refresh,

	/** 更新 */
	Update,
	
	Apply,	// 申请成功
	Delete,	// 删除，只用于reader的删除判断
	Encrypt, // 加密
	ChangeLimit,		// 修改限制条件
	Make, // 制作外发文件
	Key, // 钥匙变化
	Psd,	// 隐私空间
	ScreenLockOff,	// 锁屏
	ScreenLockOn,	// 锁屏
	Home;	// home键

	// 采用Message的写法
	public int arg1;
}
