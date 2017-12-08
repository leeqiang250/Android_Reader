package com.artifex.mupdfdemo;

import android.graphics.RectF;

public class TextWord extends RectF {
	public String w;

	public TextWord() {
		super();
		w = new String();
	}

	public void Add(TextChar tc) {
		super.union(tc);
		w = w.concat(new String(new char[]{tc.c}));
	}
	
	public void setW(String w)
	{
		this.w = w;
	}

	public String getW()
	{
		return w;
	}
}
