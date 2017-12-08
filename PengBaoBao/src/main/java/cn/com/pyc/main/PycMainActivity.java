package cn.com.pyc.main;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qlk.util.tool.Util.ScreenUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.main.adapter.MainMediaAdapter;
import cn.com.pyc.media.MediaActivity;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.plain.record.MainMediaItemEnum;
import cn.com.pyc.plain.record.MusicRecordActivity;
import cn.com.pyc.sm.PayLimitConditionActivity;
import cn.com.pyc.sm.SendActivity2;
import cn.com.pyc.user.InsertPsdActivity;
import cn.com.pyc.user.ModifyPsdActivity;
import cn.com.pyc.user.PayInfoActivity;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.user.UserInfoActivity;
import cn.com.pyc.user.key.KeyActivity;
import cn.com.pyc.user.key.RegisterFragment2;
import cn.com.pyc.user.key.RollBackKey;
import cn.com.pyc.utils.Dirs;
import cn.com.pyc.web.WebActivity;
import cn.com.pyc.wifi.WifiServer;
import cn.com.pyc.wifi.WifiTools;

/**
 * @author 李巷阳
 * @version V1.0
 *          <p>
 *          GlobalData.searchTotal(this, true); 	   // 搜索存储卡文件夹获取已经发送,已经接收,隐私空间数量。
 *          GlobalData.searchPlainsFromSysDB(this); // 查询本地数据库获取各个多媒体文件的数量
 * @Description: (主界面)
 * @date 2016-11-7 下午4:47:26
 */
@Deprecated
public class PycMainActivity extends ExtraBaseActivity implements OnClickListener,
        OnItemClickListener {

    private String g_strMediaPath;

    private UserInfo userInfo; // 为了取得notice数目
    private UserDao db;

    private TextView g_txtKeysay;
    private TextView g_txtCheck;
    private ImageButton ib_setting;
    private ImageButton g_imbSetting;
    private ImageButton g_imbWeixin;
    private RelativeLayout g_lytNotices;
    private TextView g_txtNotices;

    private MainMediaAdapter g_adptTotal;
    private ListView lv;
    private boolean canShowSettingNew = true;// 如果用户已经按下了新的设置按钮，我们不显示它再一次.
    private TextView tv_cz;
    private Button bt_photo;
    private Button bt_medio;
    private Button bt_chat;
    private Button bt_cord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init_view();
        init_listener();
        init_data();
        //		showGuide(); // app第一次启动，打开模板界面。
        registerReceivers();
    }

    /**
     * @Description: (初始化view)
     * @author 李巷阳
     * @date 2016-11-7 下午5:37:43
     */
    private void init_view() {
        setContentView(R.layout.activity_main);
        ViewHelp.showAppTintStatusBar(this);
        ib_setting = (ImageButton) findViewById(R.id.apa_imb_setting);// 个人中心
        tv_cz = (TextView) findViewById(R.id.apa_imb_chongzhi);
        bt_photo = (Button) findViewById(R.id.apa_imb_photo);
        bt_medio = (Button) findViewById(R.id.apa_imb_medio);
        bt_chat = (Button) findViewById(R.id.apa_imb_chat);
        bt_cord = (Button) findViewById(R.id.apa_imb_cord);
        lv = (ListView) findViewById(R.id.apa_lv_main);

    }

    /**
     * @Description: (事件处理)
     * @author 李巷阳
     * @date 2016-11-7 下午5:38:26
     */
    private void init_listener() {
        tv_cz.setOnClickListener(this);
        bt_photo.setOnClickListener(this);
        bt_medio.setOnClickListener(this);
        ib_setting.setOnClickListener(this);
        bt_chat.setOnClickListener(this);
        bt_cord.setOnClickListener(this);
        lv.setOnItemClickListener(this);

    }

    /**
     * @Description: (初始化数据)
     * @author 李巷阳
     * @date 2016-11-7 下午5:36:54
     */
    private void init_data() {
        GlobalData.searchTotal(this, true);    // 搜索存储卡文件夹获取已经发送,已经接收,隐私空间数量。
        GlobalData.searchPlainsFromSysDB(this); // 查询本地数据库获取各个多媒体文件的数量
        db = UserDao.getDB(this);// 初始化数据库
        userInfo = db.getUserInfo();// 获取用户的信息
        init_adapter();

    }

	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		*//*if (resultCode == RESULT_OK&&requestCode == 101)
		{
			startActivity(data);
		}*//*
		startActivity(data);
	}
*/

    /**
     * 初始化adapter
     *
     * @author 李巷阳
     * @date 2016-11-8 下午5:41:40
     */
    private void init_adapter() {
        g_adptTotal = new MainMediaAdapter(this);
        lv.setAdapter(g_adptTotal);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 拍摄按钮
            case R.id.apa_imb_photo:
                takePhoto();
                break;
            //视频按钮
            case R.id.apa_imb_medio:
                takeVideo();
				/*Intent intent = new Intent();
				intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, true);
				intent.setClass(this, CameraTakerActivity.class);
				intent.putExtra(GlobalIntentKeys.BUNDLE_OBJECT_MEDIA_TYPE, v.getId() == R.id
				.apa_imb_photo ? GlobalData.Image : GlobalData.Video);
				startActivity(intent);*/
//				startActivityForResult(intent,101);
                break;
            // 录音按钮
            case R.id.apa_imb_chat:
                startActivity(new Intent().setClass(this, MusicRecordActivity.class));
                break;
            //个人中心
            case R.id.apa_imb_setting:
                //新需求：改成返回按钮
                finish();
				/*// 打开设置界面
				if (canShowSettingNew && UpdateTool.isAnyNew(this)) {
					canShowSettingNew = false;
				}
//				ib_setting.setBackgroundResource(R.drawable.xml_setting_normal);
				startActivity(new Intent(this, SettingActivity.class));*/
                break;
            // 充值按钮
            case R.id.apa_imb_chongzhi:
                startActivity(new Intent(this, WebActivity.class).putExtra(GlobalIntentKeys
						.BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.Recharge));
                break;
            // 选择文件按钮
            case R.id.apa_imb_cord:
                Query_local_file();
                break;

            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 如果还有其他类型的request，则再细分
            Intent intent = new Intent(PycMainActivity.this, PayLimitConditionActivity.class);
            intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, g_strMediaPath);
            intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
            startActivity(intent);
//			complet();
        }
		/*else
		{
			finish();
		}*/
    }

    private void takeVideo() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date())
                + ".mp4";
        g_strMediaPath = Dirs.getCameraDir(Dirs.getDefaultBoot()) + "/pbb_" + name;
        Uri uri = Uri.parse("file://" + g_strMediaPath);
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, 1);
    }

    private void takePhoto() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date())
                + ".jpg";
        g_strMediaPath = Dirs.getCameraDir(Dirs.getDefaultBoot()) + "/pbb_" + name;
        Uri uri = Uri.parse("file://" + g_strMediaPath);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 0);
    }

    private void Query_local_file() {
        // 判断是否需要验证true:不需要;false:需要.
        // 不需要直接跳转MediaActivity
        if (isVerification()) {
            OpenMediaActivity();
        } else {
            Query_local_file_Verification();
        }
    }

    /**
     * @Description: (用户点击本地文件去验证)
     * @author 李巷阳
     * @date 2016/11/29 16:15
     */
    private void Query_local_file_Verification() {
        // 提示用户验证身体，以及绑定
        final Dialog dialog = new Dialog(this, R.style.no_frame_small);
        View v = getLayoutInflater().inflate(R.layout.dialog_click_limit, null);
        dialog.setContentView(v);
        dialog.show();
        // 用户不想验证,则跳转MediaActivity。
        v.findViewById(R.id.dcl_btn_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                OpenMediaActivity();
            }
        });
        // 用户同意验证，跳转个人中心去绑定验证。
        v.findViewById(R.id.dcl_btn_goto_check).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                startActivity(new Intent(PycMainActivity.this, UserInfoActivity.class));
            }
        });
    }

    private boolean isVerification() {
        UserInfo userInfo = UserDao.getDB(PycMainActivity.this).getUserInfo();

        if (userInfo.isEmailBinded() || userInfo.isPhoneBinded() || userInfo.isQqBinded() ||
				GlobalData.getTotalCount(PycMainActivity.this, true) < 11) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @Description: (打开多媒体列表)
     * @author 李巷阳
     * @date 2016/11/29 16:25
     */
    private void OpenMediaActivity() {
        Intent intent = new Intent(this, MediaActivity.class);
        // Pbb_Fields.TAG_PLAIN_TOTAL 显示本地数量列表.
        intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, Pbb_Fields.TAG_PLAIN_TOTAL);
        // GlobalIntentKeys.BUNDLE_FLAG_CIPHER 密文环境.
        intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
        intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, true);
        startActivity(intent);
    }

    /**
     * listview ：item点击事件
     *
     * @author 李巷阳
     * @date 2016-11-8 下午5:18:19
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            // 已发送
            case MainMediaItemEnum.ITEM_SM_SEND:
                startActivity(new Intent(this, SendActivity2.class));
                break;
            // 已接收 (现在已不支持查看，直接弹出对话框提示。)
            case MainMediaItemEnum.ITEM_SM_RECEIVE:
//				View v = getLayoutInflater().inflate(R.layout.dialog_receive, null);
//				final Dialog dialog = new Dialog(this, R.style.no_frame_small);
//				dialog.setContentView(v);
//				dialog.show();
//				TextView t = (TextView) v.findViewById(R.id.dr_txt_content);
//				Button b1 = (Button) v.findViewById(R.id.dr_btn_sure);
//				t.setText(getResources().getString(R.string.received_prompt));
//				b1.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						dialog.dismiss();
//					}
//				});

                break;
            // 隐私空间
		/*case MainMediaItemEnum.ITEM_CIPHER:
			checkAndIn(RollBackKey.FromMainCipher);
			break;*/

            default:
                break;
        }
    }

    /**
     * @author 李巷阳
     * @date 2016-11-8 下午4:53:30
     */
    //
    // 当点击人头按钮或者想进入隐私空间前需要检查钥匙的完整性
    // 有钥匙，有密码：点击隐私空间 ： 进入隐私空间输入密码界面
    // 有钥匙，没密码：点击隐私空间 ： 进入新密码设置界面
    // 有钥匙：点击：人头：进入个人中心界面
    // 没钥匙：点击隐私空间按钮：进入隐私空间注册界面
    // 没钥匙：点击人头：进入登陆按钮 *
    private void checkAndIn(RollBackKey rollBackKey) {

        UserDao.getDB(getApplicationContext());// 初始化数据库
        UserInfo userInfo = UserDao.getDB(this).getUserInfo();// 获取用户信息
        // 没钥匙
        if (userInfo.isKeyNull()) {
            RollBackKey.curRollBackKey = rollBackKey;
            // 没钥匙：点击隐私空间按钮：进入隐私空间注册界面
            if (rollBackKey.equals(RollBackKey.FromMainCipher)) {
                Intent intent = new Intent(this, KeyActivity.class);
                intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_REGISTER);
                intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, RegisterFragment2.TYPE_PASSWORD);
                startActivity(intent);
            }
            // 没钥匙：点击人头：进入登陆按钮
            else {
                Intent intent = new Intent(this, KeyActivity.class);
                intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_LOGIN);
                startActivity(intent);
            }
        }
        // 有钥匙
        else {
            RollBackKey.curRollBackKey = null;
            // 有钥匙：点击：人头：进入个人中心界面
            if (rollBackKey.equals(RollBackKey.FromMainUser)) {
                Intent intent = new Intent(this, PayInfoActivity.class);
                intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PROGRESS, Integer.valueOf
						(g_txtNotices.getText().toString().trim()));
                startActivity(intent);
            } else {
                // 有钥匙，没密码：点击隐私空间 ： 进入新密码设置界面
                if (userInfo.isPsdNull()) {
                    Intent intent = new Intent(this, ModifyPsdActivity.class);
                    intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, ModifyPsdActivity
							.TYPE_FROM_CIPHER);
                    startActivity(intent);
                }
                // 有钥匙，有密码：点击隐私空间 ： 进入隐私空间输入密码界面
                else {
                    Intent intent = new Intent(this, InsertPsdActivity.class);
                    intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, Pbb_Fields
							.TYPE_INSERT_CIPHER);
                    startActivity(intent);
                }
            }

        }

    }

	/*-****************************************
	 * TODO receiver
	 *****************************************/

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiTools.isWifiInUse(context)) {
                startService(new Intent(PycMainActivity.this, WifiServer.class));
            } else {
                stopService(new Intent(PycMainActivity.this, WifiServer.class));
            }
        }
    };

    /**
     * @Description: (模板：弹层)
     * @author 李巷阳
     * @date 2016-11-8 下午5:17:23
     */
    private void showGuide() {
        if (!((Boolean) PbbSP.getGSP(PycMainActivity.this).getValue(PbbSP.SP_GUIDE_CODE_CLICK,
				false)))// 获取是否打开模板的状态。
        {
            ImageView view = new ImageView(this);
            view.setBackgroundResource(R.drawable.guide_main);
            final Dialog dialog = new Dialog(this, R.style.no_bkg_pyc);
            dialog.setContentView(view);
            LayoutParams lay = dialog.getWindow().getAttributes();
            lay.width = ScreenUtil.getScreenWidth(this);
            lay.height = ScreenUtil.getScreenHeight(this);
            dialog.show();
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    PbbSP.getGSP(PycMainActivity.this).putValue(PbbSP.SP_GUIDE_CODE_CLICK, true);
					// 关闭模板后，把变量设置为true。
                }
            });
        }
    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(receiver, filter);
    }


    //
    //	@Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        switch ((ObTag) data) {
            //		case Key:
            //			afterGetKey();
            //			GlobalData.searchTotal(this, true); // 领取钥匙后，搜索该用户过往制作的和加密的文件
            //			break;

            //		case Make: // 放在MakeSmFileDoneActivity中了
            //			// startActivity(new Intent(this, SendActivity.class).putExtra(
            //			// SendActivity.EXPAND, true));
            //			// overridePendingTransition(android.R.anim.fade_in,
            //			// android.R.anim.fade_out);
            //			break;

            case Delete:
            case Refresh:
            case Encrypt: // 隐私空间的“new”字样
                g_adptTotal.notifyDataSetChanged();
                break;

            //		case Update:
            //			if (canShowSettingNew && UpdateTool.isAnyNew(this)) {
            //				g_imbSetting.setBackgroundResource(R.drawable.xml_setting_new);
            //			} else {
            //				g_imbSetting.setBackgroundResource(R.drawable.xml_setting_normal);
            //			}
            //			break;
            //
            //		case Notice:
            //			final int notice = ObTag.Notice.arg1;
            //			g_txtNotices.setVisibility(notice > 0 ? View.VISIBLE : View.INVISIBLE);
            //			g_txtNotices.setText(String.valueOf(notice));
            //			// g_txtNotices.setText(userInfo.getNotice());
            //			break;

            default:
                break;
        }
    }

    // 领取钥匙后继续之前的操作
    //	private void afterGetKey() {
    //		if (RollBackKey.FromMainCipher.equals(RollBackKey.curRollBackKey)) // 直接调用checkAndIn()还需要输入密码
    //		{
    //			/*-
    //			 * 如果领取钥匙操作是隐私空间引起的，且新钥匙没有密码，则必须设置密码以保证隐私安全
    //			 * 但是有密码的话就无需输入了，可直接进入
    //			 */
    //			if (UserDao.getDB(this).getUserInfo().isPsdNull()) {
    //				Intent intent = new Intent(this, ModifyPsdActivity.class);
    //				intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, ModifyPsdActivity.TYPE_FROM_CIPHER);
    //				startActivity(intent);
    //			} else {
    //				Intent intent = new Intent(this, MediaActivity.class);
    //				intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, MediaActivity.TAG_CIPHER_TOTAL);
    //				intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, true);
    //				intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, false);
    //				startActivity(intent);
    //			}
    //		} else if (RollBackKey.FromMainUser.equals(RollBackKey.curRollBackKey)) {
    //			checkAndIn(RollBackKey.FromMainUser);
    //		} else {
    //			// do nothing
    //		}
    //	}
    private void unregisterReceivers() {
        unregisterReceiver(receiver);
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        stopService(new Intent(this, WifiServer.class));
    }

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			((ExtraBaseApplication) getApplication()).exitWithPrompt(this);
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}

}
