package cn.com.pyc.user;

/**
 * @Description: (存放鹏宝宝的常量)
 * @author 李巷阳
 * @date 2016-11-7 下午2:17:14
 * @version V1.0
 */
public class Pbb_Fields {
	// KeyActivity 用到。
	public static final String TAG_KEY_CURRENT = "key_current";// 获取要开启fragment的标志。
	public static final String TAG_KEY_KEY = "key_key";// 已有账号登录
	public static final String TAG_KEY_LOGIN = "key_login"; // 邮箱登录
	public static final String TAG_KEY_PSD = "key_psd";// 找回密码
	public static final String TAG_KEY_NICK = "key_nick";// 告诉好友你是谁
	public static final String TAG_KEY_NICK2 = "key_nick2"; // 告诉好友你是谁
	public static final String TAG_KEY_OLD_USER = "key_old_user";// 已有账号登录
	public static final String TAG_KEY_REGISTER = "key_register";// 注册
	public static final String TAG_KEY_QQ = "key_qq";// QQ登录
	public static final String TAG_KEY_QQ_FAILURE = "key_qq_failure";// 需要注意
	// InsertPsdActivity 用到。
	public static final String TYPE_INSERT_CIPHER = "insert_cipher"; // 进入隐私空间
	public static final String TYPE_INSERT_HOME = "insert_home"; // 后台后再进入
	public static final String TYPE_INSERT_START = "insert_start"; // 程序启动
	// MediaActivity 用到。
	public static final String TAG_CIPHER_TOTAL = "tag_cipher_total";// 计算隐私空间的条数
	public static final String TAG_CIPHER_IMAGE = "tag_cipher_image";// 隐私空间的图片
	public static final String TAG_CIPHER_FILE = "tag_cipher_file";// 隐私空间的文档
	public static final String TAG_CIPHER_VIDEO = "tag_cipher_video";// 隐私空间的视频
	
	public static final String TAG_PLAIN_TOTAL = "tag_plain_total";// 计算本地文件的条数
	public static final String TAG_PLAIN_IMAGE_SORT = "tag_plain_image_sort";// 本地排序的图片
	public static final String TAG_PLAIN_IMAGE = "tag_plain_image";// 本地图片
	public static final String TAG_PLAIN_FILE = "tag_plain_file";// 本地文档
	public static final String TAG_PLAIN_VIDEO = "tag_plain_video";// 本地视频
	public static final String TAG_PLAIN_MUSIC = "tag_plain_music";// 本地音乐
	public static final String BACK_STACK_NAME = "media";// 添加fragment栈的key的常量值
	public static final String[] TAGS = { TAG_CIPHER_TOTAL, TAG_CIPHER_IMAGE, TAG_CIPHER_FILE, TAG_CIPHER_VIDEO, TAG_PLAIN_TOTAL, TAG_PLAIN_IMAGE_SORT, TAG_PLAIN_IMAGE, TAG_PLAIN_FILE, TAG_PLAIN_VIDEO, TAG_PLAIN_MUSIC };

}
