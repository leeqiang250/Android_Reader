package com.artifex.mupdfdemo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import cn.com.pyc.global.GlobalIntentKeys;

public class OutlineActivity extends ListActivity
{
	static OutlineItem mItems[];

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		int curPage = getIntent().getIntExtra(
				GlobalIntentKeys.BUNDLE_DATA_PROGRESS, 0);
		setListAdapter(new OutlineAdapter(getLayoutInflater(), mItems));
		// Restore the position within the list from last viewing
		int selection = 0;
		for (; selection < mItems.length; selection++)
		{
			if (curPage == mItems[selection].page)
			{
				break;
			}
			else if (curPage < mItems[selection].page)
			{
				selection--;	// ��λ��������һҳ��Ŀ¼
				break;
			}
		}
		getListView().setSelection(selection);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent();
		intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PROGRESS,
				mItems[position].page);
		setResult(RESULT_OK, intent);
		finish();
	}
}
