package cn.com.pyc.bean;

import java.util.Random;

/**
 * 与服务器交互的协议信息
 * 
 * @author QiLiKing 2015-3-24
 */
public class ProtocolInfo
{
	public static final int SHORT_LEN = 2;
	public static final int INT_LEN = 4;
	public static final int SYSTEM_ANDROID = 28;

	// user type
	public static final int TYPE_REGISTER = 220; // 注册
	public static final int TYPE_FIND_KEY_BACK = 221; // 找回钥匙
	public static final int TYPE_FIND_PSD_BACK = 222; // 找回密码
	public static final int TYPE_MODIFY_PSD = 223; // 修改密码
	public static final int TYPE_BIND_EMAIL = 225; // 绑定邮箱
	public static final int TYPE_MODIFY_NICK = 234; // 修改昵称
	public static final int TYPE_QQ_REGISTER = 260; // QQ注册
	public static final int TYPE_QQ_LOGIN = 262; // QQ登录
	public static final int TYPE_BIND_QQ = 263; // QQ绑定
	public static final int TYPE_QQ_FIND_KEY_BACK = 265; // 通过QQ找回密钥
	public static final int TYPE_SYNCHRONIZE_PSD = 266; // 同步服务器密码（可能由其他端更改）
	public static final int TYPE_GET_USER_INFO = 267; // 获取用户信息
	public static final int TYPE_GET_NOTICE = 268; // 获取通知个数（信封）

	// user length
	public static final int USERNAME_LEN = 51;
	public static final int PASSWORD_LEN = 21;
	public static final int MAC_LEN = 18;
	public static final int NICK_LEN = 51;
	public static final int UID_LEN = 5;
	public static final int VERSION_LEN = 32;
	public static final int EMAIL_LEN = 51;
	public static final int PHONE_LEN = 21;
	public static final int OPENID_LEN = 41;
	public static final int MONEY_LEN = 13;
	public static final int USER_NOT_USED_LEN = 420;

	// sm type
	public static final int TYPE_MAKE_FREE_FILE = 400; // 制作自由传播文件
	public static final int TYPE_UPLOAD_HASH = 401;	// 上传Hash
	public static final int TYPE_OPEN_FILE = 402;	// 打开文件
	public static final int TYPE_SCAN_APPLY_INFO = 403; // 获取申请信息
	public static final int TYPE_STOP_READ = 404;	// 终止阅读
	public static final int TYPE_OFFLINE_VERIFY = 405; // 离线验证
	public static final int TYPE_GET_SM_INFOS = 416; // 获取外发文件信息（批量刷新）
	public static final int TYPE_MAKE_PAY_FILE = 407; // 制作付费文件（非自由传播）
	public static final int TYPE_APPLY_ACTIVATE = 408; // 申请激活
	public static final int TYPE_REAPPLY = 409; // 重新申请（由服务器控制）
	public static final int TYPE_GET_ACTIVATE_INFO = 411; // 获取激活信息
	public static final int TYPE_PLAY_TIME = 412; // 文件播放时长（后台业务，不重要）
	public static final int TYPE_GET_SM_INFO = 413; // 获取外发文件信息（单个刷新）
	public static final int TYPE_MODIFY_LIMIT = 415;	// 修改限制条件
	public static final int TYPE_GET_SECURITY_CODE = 420; // 获取验证码
	public static final int TYPE_GET_PHONE_SECURITY_CODE = 422; // 获取手机验证码
	public static final int TYPE_SEND_PHONE_SECURITY_CODE = 421; // 发送手机验证码

	// sm length
	public static final int REMARK_LEN = 201; // 备注
	public static final int FILE_NAME_LEN = 261; // 文件名，包含.pbb
	public static final int SM_FILE_PATH_LEN = 264;
	public static final int LIMIT_TIME_LEN = 11;
	public static final int HASH_VALUE_LEN = 32;
	public static final int KEY_LEN = 16;
	public static final int QQ_LEN = 21;
	public static final int HARDNO_LEN = 51;
	public static final int SYSINFO_LEN = 31;
	public static final int TIME_LEN = 20;
	public static final int MAKE_TIME_LEN = TIME_LEN;
	public static final int ORDERNO_LEN = 30;
	public static final int SELF_DEFINE_LEN = 25;
	public static final int SHOW_INFO_LEN = 101;
	public static final int MESSAGE_ID_LEN = 25;
	public static final int SERIESNAME_LEN = 101;

	public static final int USER_TOTAL_LEN = 804;
	public static final short SM_TOTAL_LEN = 1448 + 8;	//0x0A的2个pos,共8个字节
	public static final int OFFLINE_TOTAL_LEN = 680;

	public static int random()
	{
		return new Random(System.currentTimeMillis()).nextInt();
	}
}
