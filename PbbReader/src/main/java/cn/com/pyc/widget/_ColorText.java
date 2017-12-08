package cn.com.pyc.widget;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

public class _ColorText
{
	private String regular;
	private int rglColor;

	public _ColorText(String regular, int rglColor)
	{
		if (regular == null)
		{
			regular = "";
		}
		this.regular = regular;
		this.rglColor = rglColor;
	}

	public SpannableString getPartColor(String text)
	{
		return getColorText(text, false);
	}

	public SpannableString getAllColor(String text)
	{
		return getColorText(text, true);
	}

	public SpannableString getAssignColor(String text, ColorPair... pairs)
	{
		SpannableString spannable = getColorText(text, false);
		for (ColorPair pair : pairs)
		{
			final int index = text.indexOf(pair.assign);
			if (index >= 0)
			{
				spannable.setSpan(new ForegroundColorSpan(pair.asnColor),
						index, index + pair.assign.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spannable;
	}

	private SpannableString getColorText(String text, boolean colorAll)
	{
		SpannableString spannable = new SpannableString(text);
		if (colorAll)
		{
			spannable.setSpan(new ForegroundColorSpan(rglColor), 0, text
					.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		else
		{
			final char[] org = text.toCharArray();
			for (int i = 0; i < org.length; i++)
			{
				if (regular.indexOf(org[i]) >= 0)
				{
					spannable.setSpan(new ForegroundColorSpan(rglColor), i,
							i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					continue;
				}
			}
		}
		return spannable;
	}

	public static class ColorPair
	{
		public ColorPair(String assign, int asnColor)
		{
			this.assign = assign;
			this.asnColor = asnColor;
		}

		private String assign;
		private int asnColor;
	}
}
