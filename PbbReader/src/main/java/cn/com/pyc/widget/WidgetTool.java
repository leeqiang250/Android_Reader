package cn.com.pyc.widget;

import android.text.InputType;
import android.widget.TextView;

public class WidgetTool
{
	public static void changVisible(TextView view, boolean show)
	{
		if (show)
		{
			// 第一种方法光标会移动，应该有解决办法，暂不研究
			// view.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			view.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		}
		else
		{
			// view.setTransformationMethod(PasswordTransformationMethod.getInstance());
			view.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		}
	}

	private static long clickTime;

	public static boolean isEffectiveClick()
	{
		long time = System.currentTimeMillis();
		if (Math.abs(time - clickTime) > 500)
		{
			clickTime = time;
			return true;
		}
		else
		{
			return false;
		}
	}

	//	public static SpannableString getColorText(String text, int color)
	//	{
	//		return getColorText(text, color, false);
	//	}
	//
	//	public static SpannableString getColorText(String text, int color,
	//			String assign, int assignColor, boolean colorAll)
	//	{
	//		SpannableString spannable = getColorText(text, color, colorAll);
	//		final int index = text.indexOf(assign);
	//		if (index >= 0)
	//		{
	//			spannable.setSpan(new ForegroundColorSpan(assignColor), index,
	//					index + assign.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	//		}
	//		return spannable;
	//	}
	//
	//	public static SpannableString getColorText(String text, int color,
	//			boolean colorAll)
	//	{
	//		SpannableString spannable = new SpannableString(text);
	//		if (colorAll)
	//		{
	//			spannable.setSpan(new ForegroundColorSpan(color), 0, text.length(),
	//					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	//		}
	//		else
	//		{
	//			final char[] chars = text.toCharArray();
	//			for (int i = 0; i < chars.length; i++)
	//			{
	//				if ((chars[i] >= '0' && chars[i] <= '9') || chars[i] == '.'
	//						|| chars[i] == '-')
	//				{
	//					spannable.setSpan(new ForegroundColorSpan(color), i, i + 1,
	//							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	//				}
	//			}
	//		}
	//		return spannable;
	//	}
}
