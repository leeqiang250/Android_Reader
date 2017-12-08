package cn.com.pyc.user.key;

import com.qlk.util.base.BaseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import cn.com.pyc.pbb.R;
import cn.com.pyc.user.Pbb_Fields;

public class QqFailureFragment extends BaseFragment
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_key_qqfailure, null);
		v.findViewById(R.id.fkq_btn_email).setOnClickListener(
				new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						changeFragment(Pbb_Fields.TAG_KEY_LOGIN);
					}
				});
		return v;
	}

}
