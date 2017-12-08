package cn.com.pyc.user;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.qlk.util.global.GlobalToast;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.main.HomeActivity;
import cn.com.pyc.media.MediaActivity;
import cn.com.pyc.pbb.R;
import cn.com.pyc.user.key.KeyActivity;
import cn.com.pyc.user.key.KeyTool;
import cn.com.pyc.utils.Dirs;
import cn.com.pyc.widget.PycUnderLineYellowTextView;
import cn.com.pyc.widget.WidgetTool;

/**
 * 
 * @Description: (隐私空间钥匙输入界面)
 * @author 李巷阳
 * @date 2016-11-7 下午4:09:28
 * @version V1.0
 */
public class InsertPsdActivity extends ExtraBaseActivity implements OnCheckedChangeListener, OnClickListener {
	public static boolean isHome = false; // 为了防止后台时被调用多次

	private String g_strType;// 是否开启主界面，还是隐私空间。
	private EditText g_edtPsd;

	private View psdView;

	private Button insert;

	private CheckBox show;

	private ImageView iv;

	private View cipher;

	private PycUnderLineYellowTextView maip_txt_forget;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		init_value();
		init_view();
		init_listener();
		showKeyboard();
	}
	private void init_value() {
		isHome = true; // 如果本类即为当前页面，则锁屏或者最小化将不启动
		g_strType = getIntent().getStringExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE);// 是否开启主界面，还是隐私空间。
		// 程序启动的情况
		if (g_strType.equals(Pbb_Fields.TYPE_INSERT_START)) {
			isOpenPycMainActivity();
		}
	}
	
	/**
	 * @Description: (初始化view) 隐私空间或者后台的情况
	 * @author 李巷阳
	 * @date 2016-11-7 下午4:18:38
	 */
	private void init_view() {
		setContentView(R.layout.activity_insert_psd);
		psdView = findViewById(R.id.ll_insertPsdAct);
		g_edtPsd = (EditText) findViewById(R.id.aip_edt_psd);
		insert = (Button) findViewById(R.id.aip_btn_insert);
		show = (CheckBox) findViewById(R.id.aip_cbx_show);
		iv = (ImageView) findViewById(R.id.aip_imv_insert);
		cipher = findViewById(R.id.aip_lyt_cipher);
		maip_txt_forget = (PycUnderLineYellowTextView) findViewById(R.id.aip_txt_forget);
		if (g_strType.equals(Pbb_Fields.TYPE_INSERT_CIPHER)) {
			iv.setVisibility(View.GONE);
			psdView.setBackgroundColor(getResources().getColor(R.color.function_bg));
			insert.setTextColor(Color.WHITE);
			insert.setBackgroundResource(R.drawable.xml_imb_green);
			show.setTextColor(getResources().getColor(R.color.black));
		} else {
			cipher.setVisibility(View.GONE);
		}

	}

	/**
	 * @Description: (事件处理)
	 * @author 李巷阳
	 * @date 2016-11-7 下午4:20:50
	 */
	private void init_listener() {
		insert.setOnClickListener(this);
		show.setOnCheckedChangeListener(this);
		maip_txt_forget.setOnClickListener(this);
	}




	/**
	 * @Description: (是否开启main主界面)
	 * @author 李巷阳
	 * @date 2016-11-7 下午4:17:53
	 */
	private void isOpenPycMainActivity() {
		Dirs.reGetCardsPaths(this);// 在领取钥匙成功时调用
		if (UserDao.getDB(this).getUserInfo().isPsdNull() || !((Boolean) PbbSP.getGSP(this).getValue(PbbSP.SP_NEED_PASSWORD, false))) {
			// 没有钥匙，或不需要密码输入。并向手机写入钥匙文件。
			KeyTool.makeSucFile(this, 1);
//			startActivity(new Intent(this, PycMainActivity.class));
//			startActivity(new Intent(this, MainActivity2.class));
			startActivity(new Intent(this, HomeActivity.class));
			finish();
			return;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 进入按钮
		case R.id.aip_btn_insert:
			if (!UserDao.getDB(this).getUserInfo().getPsd().equals(g_edtPsd.getText().toString().trim())) {
				GlobalToast.toastCenter(this, getResources().getString(R.string.password_error));
				KeyTool.makeSucFile(this, 0);
			} else {
				OpenActivity();
			}
			break;
		// 忘记密码
		case R.id.aip_txt_forget:
			Intent intent = new Intent(this, KeyActivity.class);
			intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_PSD);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

	private void  OpenActivity() {
		// true : 进入隐私空间
		if (g_strType.equals(Pbb_Fields.TYPE_INSERT_CIPHER)) {
			Intent intent = new Intent(this, MediaActivity.class);
			intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, Pbb_Fields.TAG_CIPHER_TOTAL);
			intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, true);
			intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, false);
			startActivity(intent);
		}
		// true:后台后再进入
		else if (g_strType.equals(Pbb_Fields.TYPE_INSERT_HOME)) {

		}
		// true:程序启动
		else if (g_strType.equals(Pbb_Fields.TYPE_INSERT_START)) {
			KeyTool.makeSucFile(this, 1);
//			startActivity(new Intent(this, MainActivity2.class));
			startActivity(new Intent(this, HomeActivity.class));
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isHome = false;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		WidgetTool.changVisible(g_edtPsd, isChecked);
	}

}
