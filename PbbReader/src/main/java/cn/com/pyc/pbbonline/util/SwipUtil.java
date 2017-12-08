package cn.com.pyc.pbbonline.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import cn.com.pyc.pbb.reader.R;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.baoyz.swipemenulistview.SwipeMenuListView.OpenOrCloseListener;
import com.sz.mobilesdk.util.CommonUtil;

public class SwipUtil
{
	//
	public static void initSwipItem(SwipeMenuListView mListView, OnMenuItemClickListener l)
	{
		final Context context = mListView.getContext();
		// step 1. create a MenuCreator
		SwipeMenuCreator creator = new SwipeMenuCreator()
		{
			@Override
			public void create(SwipeMenu menu)
			{
				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(context);
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
				// set item width
				deleteItem.setWidth(CommonUtil.dip2px(context, 65));
				// set a icon
				deleteItem.setIcon(R.drawable.ic_delete);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};
		// set creator
		mListView.setMenuCreator(creator);
		// step 2. listener item click event
		mListView.setOnMenuItemClickListener(l);

		mListView.setOnOpenOrCloseListener(new OpenOrCloseListener()
		{
			// isOpen =false 为打开，=true 为关闭
			@Override
			public void isOpen(boolean isOpen)
			{
			}
		});
	}
}
