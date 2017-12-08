package com.qlk.util.tool;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;

public class ColorText
{
	private final String dftRegular;	//default regular
	private final int dftColor;	//default color

	/**
	 * @param dftRegular
	 *            If null, will use a empty string instead.
	 * @param dftColor
	 */
	public ColorText(String dftRegular, int dftColor)
	{
		if (dftRegular == null)
		{
			dftRegular = "";
		}
		this.dftRegular = dftRegular;
		this.dftColor = dftColor;
	}

	/**
	 * Draw a part-color text
	 * 
	 * @param desText
	 *            The text to be drawn.
	 * @return
	 */
	public SpannableString getPartColor(String desText)
	{
		return getColorText(desText, false);
	}

	/**
	 * Draw a full-color text.
	 * 
	 * @param desText
	 * @return
	 */
	public SpannableString getAllColor(String desText)
	{
		return getColorText(desText, true);
	}

	/**
	 * Draw text defined by the pairs.
	 * 
	 * @param text
	 * @param pairs
	 * @return
	 */
	public SpannableString getAssignColor(String text, ColorPair... pairs)
	{
		SpannableString spannable = getColorText(text, false);
		for (ColorPair pair : pairs)
		{
			final int index = text.indexOf(pair.first);
			if (index >= 0)
			{
				spannable.setSpan(new ForegroundColorSpan(pair.second), index,
						index + pair.first.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spannable;
	}

	/**
	 * @param text
	 * @param colorAll
	 *            full-color or part-color
	 * @return
	 */
	private SpannableString getColorText(String text, boolean colorAll)
	{
		SpannableString spannable = new SpannableString(text);
		if (colorAll)
		{
			spannable.setSpan(new ForegroundColorSpan(dftColor), 0, text.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		else
		{
			final char[] org = text.toCharArray();
			for (int i = 0; i < org.length; i++)
			{
				if (dftRegular.indexOf(org[i]) >= 0)
				{
					spannable.setSpan(new ForegroundColorSpan(dftColor), i, i + 1,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					continue;
				}
			}
		}
		return spannable;
	}

	public static class ColorPair extends Pair<String, Integer>
	{
		public ColorPair(String assign, Integer color)
		{
			super(assign, color);
		}
	}
}
