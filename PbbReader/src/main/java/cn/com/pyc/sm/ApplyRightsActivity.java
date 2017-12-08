package cn.com.pyc.sm;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.ColorText;
import com.qlk.util.tool.Util.FormatUtil;

import java.io.UnsupportedEncodingException;
import java.util.Observable;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.widget.WidgetTool;
/**
*
* @Description: (没有写权限)
* @author 李巷阳
* @date 2016/12/12 11:12
*/
public class ApplyRightsActivity extends PbbBaseActivity implements
		OnClickListener
{
	private SmInfo smInfo;
	private boolean hasMustItem; // 是否有必填项
	private boolean isQQMust;
	private boolean isPhoneMust;
	private boolean isEmailMust;
	private boolean hasDefine1; // 只要有自定义项，那么它就为必填项
	private boolean hasDefine2;
	private String selfDefine1;
	private String selfDefine2;
	private EditText g_edtQQ;
	private EditText g_edtEmail;
	private EditText g_edtPhone;
	private EditText g_edtSelf1;
	private EditText g_edtSelf2;
	private TextView g_txtSelf1;
	private TextView g_txtSelf2;

	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setContentView(R.layout.activity_apply_rights);
		ViewHelp.showAppTintStatusBar(this);

		findViewAndSetListeners();

		smInfo = (SmInfo) getIntent().getSerializableExtra(
				GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);
		/*-
		 * 设置文件名，去掉路径和.pbb
		 */
		String path = (String) getIntent().getStringExtra(
				GlobalIntentKeys.BUNDLE_DATA_PATH);
		path = path.substring(path.lastIndexOf('/'));
		path = path.substring(1, path.lastIndexOf("."));
		((TextView) findViewById(R.id.aar_txt_file_name)).setText(path);

		// 限制条件
		TextView limits = (TextView) findViewById(R.id.aar_txt_limits);
		initLimits(limits);

		// 初始化联系方式
		initMustContact();

		g_edtSelf1.setText(smInfo.getSelfDefineValue1());
		g_edtSelf2.setText(smInfo.getSelfDefineValue2());

		showKeyboard();
	}

	@Override
	protected void findViewAndSetListeners()
	{
		g_edtQQ = (EditText) findViewById(R.id.aar_edt_qq);
		g_edtEmail = (EditText) findViewById(R.id.aar_edt_email);
		g_edtPhone = (EditText) findViewById(R.id.aar_edt_phone);
		g_edtSelf1 = (EditText) findViewById(R.id.aar_edt_selfdefine1);
		g_edtSelf2 = (EditText) findViewById(R.id.aar_edt_selfdefine2);
		g_txtSelf1 = (TextView) findViewById(R.id.aar_txt_selfdefine1);
		g_txtSelf2 = (TextView) findViewById(R.id.aar_txt_selfdefine2);

		findViewById(R.id.aar_btn_next).setOnClickListener(this);
	}

	private void qqVisible()
	{
		findViewById(R.id.aar_txt_qq).setVisibility(View.VISIBLE);
		g_edtQQ.setVisibility(View.VISIBLE);
		g_edtQQ.setText(smInfo.getQqBuyer());
	}

	private void emailVisible()
	{
		findViewById(R.id.aar_txt_email).setVisibility(View.VISIBLE);
		g_edtEmail.setVisibility(View.VISIBLE);
		g_edtEmail.setText(smInfo.getEmailBuyer());
	}

	private void phoneVisible()
	{
		findViewById(R.id.aar_txt_phone).setVisibility(View.VISIBLE);
		g_edtPhone.setVisibility(View.VISIBLE);
		g_edtPhone.setText(smInfo.getPhoneBuyer());
	}

	private void self1Visible()
	{
		g_txtSelf1.setVisibility(View.VISIBLE);
		g_edtSelf1.setVisibility(View.VISIBLE);
		g_txtSelf1.setText(smInfo.getSelfDefineKey1() + "：");
		g_edtSelf1.setText(smInfo.getSelfDefineValue1());
	}

	private void self2Visible()
	{
		g_txtSelf2.setVisibility(View.VISIBLE);
		g_edtSelf2.setVisibility(View.VISIBLE);
		g_txtSelf2.setText(smInfo.getSelfDefineKey2() + "：");
		g_edtSelf2.setText(smInfo.getSelfDefineValue2());
	}

	private void initMustContact()
	{
		hasMustItem = smInfo.hasMustItem();

		if (!hasMustItem)
		{
			qqVisible();
			emailVisible();
			phoneVisible();
			return;
		}

		/*
		 * 分析必须项
		 */
		isQQMust = smInfo.isQQMust();
		isPhoneMust = smInfo.isPhoneMust();
		isEmailMust = smInfo.isEmailMust();
		hasDefine1 = smInfo.getSelfMust() > 0;
		hasDefine2 = smInfo.getSelfMust() > 1;
		if (isQQMust)
		{
			qqVisible();
		}
		if (isPhoneMust)
		{
			phoneVisible();
		}
		if (isEmailMust)
		{
			emailVisible();
		}

		if (hasDefine1)
		{
			self1Visible();
			if (smInfo.isSelfDefineSecret1())
			{
				WidgetTool.changVisible(g_edtSelf1, false);
			}
		}
		if (hasDefine2)
		{
			self2Visible();
			if (smInfo.isSelfDefineSecret2())
			{
				WidgetTool.changVisible(g_edtSelf2, false);
			}
		}

	}

	private void initLimits(TextView limits)
	{
		if (!smInfo.canShowLimit())
		{
			return;
		}

		int counts = smInfo.getOpenCount();
		int days = smInfo.getDays();
		int years = smInfo.getYears();

		String str = "";
		if (counts > 0)
		{
			if (days > 0)
			{
				str += "你" + days + "天内能看" + counts + "次";
			}
			else if (years > 0)
			{
				str += "你" + years + "年内能看" + counts + "次";
			}
			else
			{
				str += "你能看" + counts + "次";
			}
		}
		else
		{
			if (days > 0)
			{
				str += "你能看" + days + "天";
			}
			else if (years > 0)
			{
				str += "你能看" + years + "年";
			}
			else
			{
				str += "";
			}
		}
		ColorText ct = new ColorText("0123456789", getResources().getColor(
				R.color.green));
		limits.setText(ct.getPartColor(str));
	}

	@Override
	public void onClick(View v)
	{
		apply();
	}

	private void apply()
	{
		final String email = g_edtEmail.getText().toString().trim();
		final String qq = g_edtQQ.getText().toString().trim();
		final String phone = g_edtPhone.getText().toString().trim();
		final String selfDefineValue1 = g_edtSelf1.getText().toString().trim();
		final String selfDefineValue2 = g_edtSelf2.getText().toString().trim();

		// 先复原，再显示红色
		g_edtQQ.setBackgroundResource(R.drawable.xml_edt_small);
		g_edtEmail.setBackgroundResource(R.drawable.xml_edt_small);
		g_edtPhone.setBackgroundResource(R.drawable.xml_edt_small);
		g_edtSelf1.setBackgroundResource(R.drawable.xml_edt_small);
		g_edtSelf2.setBackgroundResource(R.drawable.xml_edt_small);

		boolean correct = false; // 初始化为填写的条件不合格
		if (hasMustItem)
		{
			int strlen1 = 0;
			int strlen2 = 0;
			try
			{
				strlen1 = selfDefineValue1.getBytes("utf-8").length;
				strlen2 = selfDefineValue2.getBytes("utf-8").length;
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}

			if (isQQMust && TextUtils.isEmpty(qq))
			{
				GlobalToast.toastShort(this, "QQ为必填项");
				g_edtQQ.setBackgroundResource(R.drawable.xml_edt_red);
				g_edtQQ.setOnFocusChangeListener(new OnFocusChangeListener()
				{

					@Override
					public void onFocusChange(View v, boolean hasFocus)
					{
						if (!g_edtQQ.hasFocus())
						{
							g_edtQQ.setBackgroundResource(R.drawable.xml_edt_small);
						}
					}
				});
			}
			else if (isPhoneMust && !FormatUtil.isPhoneFormat(phone))
			{
				GlobalToast.toastShort(this, "请输入正确的手机号");
				g_edtPhone.setBackgroundResource(R.drawable.xml_edt_red);
				g_edtPhone.setOnFocusChangeListener(new OnFocusChangeListener()
				{

					@Override
					public void onFocusChange(View v, boolean hasFocus)
					{
						if (!g_edtPhone.hasFocus())
						{
							g_edtPhone
									.setBackgroundResource(R.drawable.xml_edt_small);
						}
					}
				});
			}
			else if (isEmailMust && !FormatUtil.isEmailFormat(email))
			{
				GlobalToast.toastShort(this, "请输入正确的邮箱地址");
				g_edtEmail.setBackgroundResource(R.drawable.xml_edt_red);
				g_edtEmail.setOnFocusChangeListener(new OnFocusChangeListener()
				{

					@Override
					public void onFocusChange(View v, boolean hasFocus)
					{
						if (!g_edtEmail.hasFocus())
						{
							g_edtEmail
									.setBackgroundResource(R.drawable.xml_edt_small);
						}
					}
				});
			}
			else if (hasDefine1 && TextUtils.isEmpty(selfDefineValue1))
			{
				String selfKey1 = g_txtSelf1.getText().toString()
						.replaceAll(":", "");
				GlobalToast.toastShort(this, selfKey1 + "为必填项");
				g_edtSelf1.setBackgroundResource(R.drawable.xml_edt_red);
				g_edtSelf1.setOnFocusChangeListener(new OnFocusChangeListener()
				{
					@Override
					public void onFocusChange(View v, boolean hasFocus)
					{
						if (!g_edtSelf1.hasFocus())
						{
							g_edtSelf1
									.setBackgroundResource(R.drawable.xml_edt_small);
						}
					}
				});
			}
			else if (hasDefine2 && TextUtils.isEmpty(selfDefineValue2))
			{
				String selfKey2 = g_txtSelf2.getText().toString()
						.replaceAll(":", "");
				GlobalToast.toastShort(this, selfKey2 + "为必填项");
				g_edtSelf2.setBackgroundResource(R.drawable.xml_edt_red);
				g_edtSelf2.setOnFocusChangeListener(new OnFocusChangeListener()
				{
					@Override
					public void onFocusChange(View v, boolean hasFocus)
					{
						if (!g_edtSelf2.hasFocus())
						{
							g_edtSelf2
									.setBackgroundResource(R.drawable.xml_edt_small);
						}
					}
				});
			}
			else if (hasDefine1 && strlen1 > 24)
			{
				GlobalToast.toastShort(this, selfDefine1 + " 信息长度1-24个字符");
			}
			else if (hasDefine2 && strlen2 > 24)
			{
				GlobalToast.toastShort(this, selfDefine2 + " 信息长度1-24个字符");
			}
			else
			{
				correct = true; // 各个条件都满足
				g_edtEmail.setBackgroundResource(R.drawable.xml_edt_small);
				g_edtQQ.setBackgroundResource(R.drawable.xml_edt_small);
				g_edtPhone.setBackgroundResource(R.drawable.xml_edt_small);
				g_edtSelf1.setBackgroundResource(R.drawable.xml_edt_small);
				g_edtSelf2.setBackgroundResource(R.drawable.xml_edt_small);
			}
		}
		else
		{
			// 没有必填项则使用默认规则
			if (TextUtils.isEmpty(qq + phone))
			{
				GlobalToast.toastShort(this, "QQ和手机至少填写一个");
				findViewById(R.id.aar_edt_qq).setBackgroundResource(
						R.drawable.xml_edt_red);
				findViewById(R.id.aar_edt_phone).setBackgroundResource(
						R.drawable.xml_edt_red);
			}
			else
			{
				correct = true; // 各个条件都满足
			}
		}

		if (correct)
		{
			smInfo.setEmailBuyer(email);
			smInfo.setPhoneBuyer(phone);
			smInfo.setQqBuyer(qq);
			smInfo.setSelfDefineValue1(selfDefineValue1);
			smInfo.setSelfDefineValue2(selfDefineValue2);

			startActivity(GlobalIntentKeys.reUseIntent(
					ApplyRightsActivity.this, ApplyConfirmActivity.class));
		}
	}

	@Override
	public void update(Observable observable, Object data)
	{
		super.update(observable, data);
		if (data.equals(ObTag.Apply))
		{
			finish();
		}
	}

}
