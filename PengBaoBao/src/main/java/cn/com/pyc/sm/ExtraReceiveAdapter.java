package cn.com.pyc.sm;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import cn.com.pyc.pbb.R;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.media.ExtraDelSndFile;
import cn.com.pyc.receive.ReceiveAdapter;

public class ExtraReceiveAdapter extends ReceiveAdapter
{

	public ExtraReceiveAdapter(Context context, ArrayList<String> paths,
			HashMap<String, SmInfo> datas)
	{
		super(context, paths, datas);
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View v,
			ViewGroup parent)
	{
		v = super.getChildView(groupPosition, childPosition, isLastChild, v, parent);

		String filePath = mPaths.get(groupPosition);
		SmInfo info = mDatas.get(filePath);
		if (info != null && info.valid())
		{
			// 发送
			ImageButton send = (ImageButton) v.findViewById(R.id.asc_imb_send);
			send.setVisibility(View.VISIBLE);
			send.setTag(filePath);
			send.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					ExtraDelSndFile.sendFile(mContext, (String) v.getTag());
				}
			});
		}
		return v;
	}
}
