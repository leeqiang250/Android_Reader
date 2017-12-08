package cn.com.pyc.pbbonline.util;

import com.sz.mobilesdk.util.UIHelper;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.pyc.pbb.reader.R;

/**
 * view控件设置或显示工具类
 */
public class ViewHelp
{

	/**
	 * 设置MyAPP沉浸式状态栏，颜色和最顶部标题title颜色相同。<br/>
	 * 必须在setContentView之后调用
	 * 
	 * @param ac
	 */
	public static void showAppTintStatusBar(Activity ac)
	{
		showAppTintStatusBar(ac, R.color.frame_bg);
	}

	/**
	 * 设置MyAPP沉浸式状态栏，颜色和最顶部标题title颜色相同。<br/>
	 * 必须在setContentView之后调用
	 * 
	 * @param ac
	 * @param colorId
	 *            资源color.xml中id，eg: R.color.title_bg
	 */
	public static void showAppTintStatusBar(Activity ac, int colorId)
	{
		UIHelper.showTintStatusBar(ac, ac.getResources().getColor(colorId));
	}

	/**
	 * 设置listview的EmptyView，在加载数据为空或没有数据的时候使用
	 * 
	 * @param lv
	 * @param emptyView
	 * @param res
	 *            资源string.xml中id
	 */
	public static final void setEmptyViews(ListView lv, View emptyView, int res)
	{
		setEmptyViews(lv, emptyView, lv.getContext().getResources().getString(res));
	}

	/**
	 * 设置listview的EmptyView，在加载数据为空或没有数据的时候使用
	 * 
	 * @param lv
	 * @param emptyView
	 * @param tips
	 */
	public static final void setEmptyViews(ListView lv, View emptyView, String tips)
	{
		if (lv == null)
			return;

		TextView tipTextView = ((TextView) emptyView.findViewById(R.id.vep_txt_prompt));
		tipTextView.setTextColor(Color.parseColor("#666666"));
		tipTextView.setText(tips);
		lv.setEmptyView(emptyView);
	}

}
