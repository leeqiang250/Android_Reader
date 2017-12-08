package cn.com.pyc.user.key;

import com.qlk.util.base.BaseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import cn.com.pyc.pbb.R;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.widget.PycUnderLineTextView;

/**
 * 
 * @Description: (绑定QQ，手动输入昵称 界面)
 * @author 李巷阳
 * @date 2016-11-9 下午5:12:23
 * @version V1.0
 */
public class NickFragment extends BaseFragment implements OnClickListener {

	private Button m_fkn_btn_qq;
	private PycUnderLineTextView m_fkn_txt_nick;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_key_nick, null);
		init_view(v);
		init_listener();
		return v;
	}

	/**
	 * @author 李巷阳
	 * @date 2016-11-9 下午5:11:29
	 */
	private void init_view(View v) {
		m_fkn_btn_qq = (Button) v.findViewById(R.id.fkn_btn_qq);
		m_fkn_txt_nick = (PycUnderLineTextView) v.findViewById(R.id.fkn_txt_nick);

	}

	/**
	 * @author 李巷阳
	 * @date 2016-11-9 下午5:12:39
	 */
	private void init_listener() {
		m_fkn_btn_qq.setOnClickListener(this);
		m_fkn_txt_nick.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fkn_btn_qq) {
			Bundle bundle = new Bundle();
			bundle.putString(GlobalIntentKeys.BUNDLE_DATA_TYPE, QqFragment.TYPE_BIND);
			changeFragment(Pbb_Fields.TAG_KEY_QQ, bundle);
		} else {
			changeFragment(Pbb_Fields.TAG_KEY_NICK2);
		}

	}

}
