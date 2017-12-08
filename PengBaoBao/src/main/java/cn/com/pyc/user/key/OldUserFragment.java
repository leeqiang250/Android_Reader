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

/**
 * @Description: (QQ登陆，邮箱登陆)
 * @author 李巷阳
 * @date 2016-11-9 下午5:29:03
 * @version V1.0
 */
public class OldUserFragment extends BaseFragment implements OnClickListener {

	private Button m_fko_btn_email;
	private Button m_fko_btn_qq;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_key_olduser, null);
		init_view(v);
		init_listener();
		return v;
	}

	/**
	 * @author 李巷阳
	 * @date 2016-11-9 下午5:29:32
	 */
	private void init_view(View v) {
		m_fko_btn_email = (Button) v.findViewById(R.id.fko_btn_email);
		m_fko_btn_qq = (Button) v.findViewById(R.id.fko_btn_qq);
	}

	/**
	 * @author 李巷阳
	 * @date 2016-11-9 下午5:29:33
	 */
	private void init_listener() {
		m_fko_btn_email.setOnClickListener(this);
		m_fko_btn_qq.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fko_btn_qq) {
			Bundle bundle = new Bundle();
			bundle.putString(GlobalIntentKeys.BUNDLE_DATA_TYPE, QqFragment.TYPE_FIND_KEY);
			changeFragment(Pbb_Fields.TAG_KEY_QQ, bundle);
		} else {
			changeFragment(Pbb_Fields.TAG_KEY_LOGIN);
		}

	}

}
