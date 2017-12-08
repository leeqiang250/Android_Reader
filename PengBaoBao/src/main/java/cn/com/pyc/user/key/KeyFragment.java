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
* @Description: (新用户注册，老用户找回    界面) 
* @author 李巷阳
* @date 2016-11-9 下午5:11:36 
* @version V1.0
 */
public class KeyFragment extends BaseFragment implements OnClickListener {

	private Button m_fk_btn_new_rigster;
	private Button m_fk_btn_old_user;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_key, null);
		init_view(v);
		init_listener();
		return v;
	}

	/**
	 * @author 李巷阳
	 * @date 2016-11-9 下午5:06:18
	 */
	private void init_view(View v) {
		m_fk_btn_new_rigster = (Button) v.findViewById(R.id.fk_btn_new_rigster);
		m_fk_btn_old_user = (Button) v.findViewById(R.id.fk_btn_old_user);

	}

	/**
	 * @author 李巷阳
	 * @date 2016-11-9 下午5:07:49
	 */
	private void init_listener() {
		m_fk_btn_new_rigster.setOnClickListener(this);
		m_fk_btn_old_user.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		// 老用户找回
		case R.id.fk_btn_old_user:
			changeFragment(Pbb_Fields.TAG_KEY_OLD_USER);
			break;
		// 新用户注册
		case R.id.fk_btn_new_rigster:
			bundle.putString(GlobalIntentKeys.BUNDLE_DATA_TYPE, RegisterFragment2.TYPE_NICK);
			changeFragment(Pbb_Fields.TAG_KEY_REGISTER, bundle);
			break;

		default:
			break;
		}

	}
	// @Override
	// public void onClick(View v)
	// {
	// Bundle bundle = new Bundle();
	// switch (v.getId())
	// {
	// /*case R.id.fk_btn_qq:
	// bundle.putString(GlobalIntentKeys.BUNDLE_DATA_TYPE,
	// QqFragment.TYPE_REGISTER);
	// changeFragment(KeyActivity.TAG_KEY_QQ, bundle);
	// break;*/
	//
	// case R.id.fk_btn_old_user:
	// changeFragment(Pbb_Fields.TAG_KEY_OLD_USER);
	// // changeFragment(KeyActivity.TAG_KEY_EMAIL);
	// break;
	//
	// case R.id.fk_btn_new_rigster:
	// bundle.putString(GlobalIntentKeys.BUNDLE_DATA_TYPE,
	// RegisterFragment.TYPE_NICK);
	// changeFragment(Pbb_Fields.TAG_KEY_REGISTER, bundle);
	// break;
	//
	// default:
	// break;
	// }
	//
	// }

}
