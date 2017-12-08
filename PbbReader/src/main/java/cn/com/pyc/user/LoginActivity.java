package cn.com.pyc.user;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.adapter.LoginFragmentAdapter;
import cn.com.pyc.pbbonline.fragment.FragmentPasswordLogin;
import cn.com.pyc.pbbonline.fragment.FragmentVerifyCodeLogin;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.widget.HighlightImageView;

@Deprecated
public class LoginActivity extends FragmentActivity implements OnClickListener
{
	private LinearLayout[] linearLayouts;
	private TextView[] textViews;
	private ViewPager viewPagers;
	private TextView title_tv;
	private HighlightImageView back_img;
	private String phoneNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ViewHelp.showAppTintStatusBar(this);
		initValue();
		initView();
		setListener();
	}

	private void initValue()
	{
		phoneNumber = getIntent().getStringExtra("phone_number");
	}

	private void setListener()
	{
		title_tv.setText("登录领取所有文件");
		back_img.setOnClickListener(this);
	}

	/** 初始化布局 */
	@SuppressWarnings("deprecation")
	public void initView()
	{
		title_tv = (TextView) findViewById(R.id.title_tv);
		back_img = (HighlightImageView) findViewById(R.id.back_img);
		linearLayouts = new LinearLayout[2];
		linearLayouts[1] = (LinearLayout) findViewById(R.id.lay1);
		linearLayouts[0] = (LinearLayout) findViewById(R.id.lay2);
		linearLayouts[0].setBackgroundResource(R.drawable.linearlayout01s);
		textViews = new TextView[2];
		textViews[1] = (TextView) findViewById(R.id.fratext1);
		textViews[0] = (TextView) findViewById(R.id.fratext2);
		textViews[0].setTextColor(getResources().getColor(R.color.white));
		viewPagers = (ViewPager) findViewById(R.id.viewPager);
		Bundle bundle = new Bundle();
		bundle.putString("phoneNumber", phoneNumber);
		//向detailFragment传入参数
		List<Fragment> totalFragment = new ArrayList<Fragment>();
		//把页面添加到ViewPager里

		totalFragment.add(new FragmentVerifyCodeLogin());
		totalFragment.add(new FragmentPasswordLogin());
		totalFragment.get(0).setArguments(bundle);
		totalFragment.get(1).setArguments(bundle);
		viewPagers.setOffscreenPageLimit(totalFragment.size());
		viewPagers.setAdapter(new LoginFragmentAdapter(getSupportFragmentManager(), totalFragment));
		//设置显示哪页
		viewPagers.setCurrentItem(0);

		viewPagers.setOnPageChangeListener(new OnPageChangeListener()
		{

			@Override
			public void onPageSelected(int arg0)
			{
				resetlaybg();
				linearLayouts[arg0].setBackgroundResource(R.drawable.linearlayout01s);
				textViews[arg0].setTextColor(getResources().getColor(R.color.white));

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0)
			{
				// TODO Auto-generated method stub

			}
		});
		//初始化fragment的手机号
	}

	/** 重置linearLayouts、textViews */
	public void resetlaybg()
	{
		for (int i = 0; i < 2; i++)
		{
			// linearLayouts[i].setBackgroundResource(R.drawable.ai);
			textViews[i].setTextColor(getResources().getColor(R.color.black));
			linearLayouts[i].setBackgroundResource(R.drawable.linearlayout01);
		}

	}

	public void LayoutOnclick(View v)
	{
		// 每次点击都重置linearLayouts的背景、textViews字体颜色
		int id = v.getId();
		if (id == R.id.lay1)
		{
			resetlaybg();
			viewPagers.setCurrentItem(1);
			linearLayouts[1].setBackgroundResource(R.drawable.linearlayout01s);
			textViews[1].setTextColor(getResources().getColor(R.color.white));
		}
		else if (id == R.id.lay2)
		{
			resetlaybg();
			viewPagers.setCurrentItem(0);
			linearLayouts[0].setBackgroundResource(R.drawable.linearlayout01s);
			textViews[0].setTextColor(getResources().getColor(R.color.white));
		}
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.back_img)
		{
			finish();
		}
	}
}
