package cn.com.pyc.user.key;

import com.qlk.util.base.BaseFragment;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import cn.com.pyc.pbb.R;
import cn.com.pyc.conn.UserConnect;
import cn.com.pyc.conn.UserResult;
/**
* @Description: (输入昵称，继续) 
* @author 李巷阳
* @date 2016-11-9 下午5:16:41 
* @version V1.0
 */
public class Nick2Fragment extends BaseFragment implements OnClickListener
{

	private EditText edtNick;
	private Button m_fkn_btn_continue;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_key_nick2, null);
		init_view(v);
		init_listener();
		
		((KeyActivity) getActivity()).showKeyboard();
		return v;
	}

	/**   
	* @author 李巷阳
	* @date 2016-11-9 下午5:24:11 
	*/
	private void init_view(View v ) {
		edtNick = (EditText) v.findViewById(R.id.fkn_edt_nick);
		m_fkn_btn_continue = (Button) v.findViewById(R.id.fkn_btn_continue);
	}

	/**   
	* @author 李巷阳
	* @date 2016-11-9 下午5:24:14 
	*/
	private void init_listener() {
		m_fkn_btn_continue.setOnClickListener(this);
	}
	
	/**   
	* @author 李巷阳
	* @date 2016-11-9 下午5:26:15 
	*/
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fkn_btn_continue:
			setNick(edtNick.getText().toString().trim());
			break;

		default:
			break;
		}
	}


	private void setNick(final String nick)
	{
		if (TextUtils.isEmpty(nick) || nick.length() > 10)
		{
			GlobalToast.toastCenter(getActivity(), "昵称不合法（1-10位）！");
			return;
		}

		GlobalTask.executeNetTask(getActivity(), new Runnable()
		{
			@Override
			public void run()
			{
				UserResult ur = new UserConnect(getActivity()).modifyNick(nick, true);
				if (ur.succeed())
				{
					GlobalToast.toastShort(getActivity(), "创建昵称成功");
				}
				else
				{
					if (!ur.isBusinessSucceed())
					{
						GlobalToast.toastShort(getActivity(), "创建昵称失败");
					}
					else
					{
						GlobalToast.toastShort(getActivity(), ur.getFailureReason());
					}
				}
			}
		});
	}


}
