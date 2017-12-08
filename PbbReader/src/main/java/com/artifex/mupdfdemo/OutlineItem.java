package com.artifex.mupdfdemo;

import java.io.Serializable;

public class OutlineItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final int    level;
	public final String title;
	public final int    page;

	OutlineItem(int _level, String _title, int _page) {
		level = _level;
		title = _title;
		page  = _page;
	}

}
