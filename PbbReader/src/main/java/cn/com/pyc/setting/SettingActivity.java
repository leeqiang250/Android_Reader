package cn.com.pyc.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util.NetUtil;

import java.util.Observable;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.update.UpdateActivity;
import cn.com.pyc.update.UpdateTool;
import cn.com.pyc.web.WebActivity;
import cn.com.pyc.wxapi.WXTool;


@Deprecated
public class SettingActivity extends PbbBaseActivity
{
	private SettingsAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		initUI();
	}

	@Override
	protected void initUI()
	{
		ListView listView = (ListView) findViewById(R.id.as_lsv_setting);
		mAdapter = new SettingsAdapter(getApplicationContext());
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(onItemClickListener);
	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			if (!NetUtil.isNetInUse(getApplicationContext())
					&& (position == 3 || position == 4 || position == 7 || position == 8))
			{
				GlobalToast.toastShort(SettingActivity.this, "没有可用网络");
				return;
			}

			switch (position)
			{
				case 0:
					//个人中心
//					SettingActivity.this.startActivity(new Intent(SettingActivity.this, PayInfoActivity.class));
					PbbSP.getGSP(SettingActivity.this).putValue(PbbSP.SP_GUIDE_USER_FIRST_CLICK, false);
//					g_imbUser.setBackgroundResource(R.drawable.xml_user);
//					checkAndIn(RollBackKey.FromMainUser);
					break;
				case 1:
					//隐私空间
					break;
				case 2:
					//PPC版
					break;
				case 3:
				{
					WXTool.payAttentionToWeiXin(SettingActivity.this);
				}
					break;

				case 4:
				{
					if (UpdateTool.isApkNew(SettingActivity.this))
					{

						View v = getLayoutInflater().inflate(R.layout.dialog_delete, null);

						final Dialog dialog = new Dialog(SettingActivity.this,
								R.style.no_frame_small);
						dialog.setContentView(v);
						dialog.show();

						TextView t = (TextView) v.findViewById(R.id.dd_txt_content);
						Button b1 = (Button) v.findViewById(R.id.dd_btn_sure);
						Button b2 = (Button) v.findViewById(R.id.dd_btn_cancel);

						t.setText("是否现在升级?");
						b1.setOnClickListener(new OnClickListener()
						{

							@Override
							public void onClick(View v)
							{
								startActivity(new Intent(SettingActivity.this, UpdateActivity.class));
								dialog.dismiss();
							}
						});
						b2.setOnClickListener(new OnClickListener()
						{

							@Override
							public void onClick(View v)
							{
								dialog.dismiss();
							}
						});
					}
					else
					{

						GlobalToast.toastShort(SettingActivity.this, "已是最新版本");
					}
				}
					break;

				case 5:
					startActivity(new Intent(SettingActivity.this, WebActivity.class).putExtra(
							GlobalIntentKeys.BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.Idea));
					break;

				case 6:
					startActivity(new Intent(SettingActivity.this, AboutActivity.class));
					break;

				default:
					break;
			}
		}
	};


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
	/*private void checkAndIn(RollBackKey rollBackKey) {

		UserDao.getDB(getApplicationContext());// 初始化数据库
		UserInfo userInfo = UserDao.getDB(this).getUserInfo();// 获取用户信息
		// 没钥匙
		if (userInfo.isKeyNull()) {
			RollBackKey.curRollBackKey = rollBackKey;
			// 没钥匙：点击隐私空间按钮：进入隐私空间注册界面
			if (rollBackKey.equals(RollBackKey.FromMainCipher)) {
				Intent intent = new Intent(this, KeyActivity.class);
				intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_REGISTER);
				intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, RegisterFragment.TYPE_PASSWORD);
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
				intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PROGRESS, Integer.valueOf(g_txtNotices.getText().toString().trim()));
				startActivity(intent);
			} else {
				// 有钥匙，没密码：点击隐私空间 ： 进入新密码设置界面
				if (userInfo.isPsdNull()) {
					Intent intent = new Intent(this, ModifyPsdActivity.class);
					intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, ModifyPsdActivity.TYPE_FROM_CIPHER);
					startActivity(intent);
				}
				// 有钥匙，有密码：点击隐私空间 ： 进入隐私空间输入密码界面
				else {
					Intent intent = new Intent(this, InsertPsdActivity.class);
					intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, Pbb_Fields.TYPE_INSERT_CIPHER);
					startActivity(intent);
				}
			}

		}

	}*/

	private boolean isFromUserCheck = false;

	@Override
	public void update(Observable observable, Object data)
	{
		super.update(observable, data);
		if (data.equals(ObTag.Update))
		{
			mAdapter.notifyDataSetChanged();
			if (isFromUserCheck)
			{
				if (UpdateTool.isApkNew(SettingActivity.this))
				{
					startActivity(new Intent(SettingActivity.this, UpdateActivity.class));
				}
				else
				{
					GlobalToast.toastShort(this, "已是最新版本");
				}
				isFromUserCheck = false;
			}
		}

	}

	private class SettingsAdapter extends BaseAdapter
	{
		private Context mContext;

		private final int[] PICS = new int[]
		{ R.drawable.weixin, R.drawable.update, R.drawable.setting_idea,R.drawable.weixin, R.drawable.update, R.drawable.setting_idea, R.drawable.setting_about };
		private final String[] NAMES = new String[]
		{ "个人中心","隐私空间","了解PC版","微信客服", "检查更新", "意见反馈", "关于" };

		public SettingsAdapter(Context context)
		{
			this.mContext = context;
		}

		@Override
		public int getCount()
		{
			return NAMES.length;
		}

		@Override
		public Object getItem(int position)
		{
			return NAMES[position];
		}

		@Override
		public long getItemId(int position)
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder vh;
			if (convertView == null)
			{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.adpter_setting,
						parent, false);
				vh = new ViewHolder();
				vh.pic = (ImageView) convertView.findViewById(R.id.as_imv_pic);
				vh.name = (TextView) convertView.findViewById(R.id.as_txt_name);
//				vh.count = (TextView) convertView.findViewById(R.id.as_txt_count);
				convertView.setTag(vh);
			}
			else
			{
				vh = (ViewHolder) convertView.getTag();
			}
			vh.pic.setBackgroundResource(PICS[position]);
			vh.name.setText(NAMES[position]);

			if ((position == 4 && UpdateTool.isApkNew(mContext)))
			{
				vh.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.circle_red, 0);
			}
			else
			{
				vh.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}

			/*if (position == 1){
				vh.count.setVisibility(View.VISIBLE);
			}else {
				vh.count.setVisibility(View.GONE);
			}*/


			return convertView;
		}

		class ViewHolder
		{
			ImageView pic;
			TextView name;
//			TextView count;
		}
	}
}
