package cn.com.pyc.media;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.qlk.util.base.BaseFragment;
import com.qlk.util.global.GlobalObserver;
import com.sz.mobilesdk.util.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.util.HashMap;
import java.util.Locale;

import cn.com.pyc.pbb.R;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.xcoder.XCoder;

import static android.content.Context.TELEPHONY_SERVICE;


/**
 * @Description: (计算隐私空间的条数)
 * @author 李巷阳
 * @date 2016-11-14 下午3:29:48
 * @version V1.0
 */
public class CipherTotalFragment extends BaseFragment {
	private GridView mGridView;
	private CipherTotalAdapter mAdapter;

	private int size;
	private String tokenString;

	private UserDao db = UserDao.getDB(getActivity());
	private UserInfo userInfo;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_cipher_total, container, false);

		userInfo = db.getUserInfo();
		init_view(v);
		init_listener();
		init_adapter();
//		refreshUI();
		return v;
	}


	private void init_view(View v) {
		mGridView = ((GridView) v.findViewById(R.id.fct_grv_total));
	}

	private void getUserToken(final UserInfo userInfo) {

		String userTokenUrl = Constant.UserTokenHost;

		// 请求参数
		Bundle bundle = new Bundle();
		bundle.putString("grant_type", "password");
		bundle.putString("username", userInfo.getUserName());
		if (!TextUtils.isEmpty(userInfo.getPsd())) {
			bundle.putString("password", userInfo.getPsd());
		} else {
			bundle.putString(
					"password",
					"n|"
							+ XCoder.getHttpEncryptText(userInfo
							.getUserName()));
		}

		// 请求头
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(
				"Authorization",
				"Basic "
						+ SecurityUtil.encryptBASE64(PhoneInfo.testID
						+ ":" + PhoneInfo.testPSD));
		headers.put("Content-Type", "application/x-www-form-urlencoded");

		GlobalHttp.post(userTokenUrl, bundle, headers,
				new Callback.CommonCallback<String>() {

					@Override
					public void onSuccess(String arg0) {
						// 解析Json
						try {
							JSONObject object = new JSONObject(arg0);
							tokenString = (String) object
									.get("access_token");

							method(tokenString);

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFinished() {
					}

					@Override
					public void onError(Throwable arg0, boolean arg1) {
//						hideLoading();
					}

					@Override
					public void onCancelled(CancelledException arg0) {
					}
				});
	}

	/*public void hideLoading() {
		if (loadingDlg != null) {
			loadingDlg.dismiss();
			loadingDlg = null;
		}
	}*/
	private void method(String tokenString) {
		// 请求参数
		Bundle bundle = new Bundle();
		bundle.putString("appid", 28+"");
		bundle.putString("appversion","6.2.0");
		bundle.putString("logtype", "Info");
//		bundle.putString("logremark", size+"");
		bundle.putInt("logremark", size);
		TelephonyManager TelephonyMgr = (TelephonyManager)getActivity().getSystemService(TELEPHONY_SERVICE);
		String szImei = TelephonyMgr.getDeviceId();
		bundle.putString("devhdid", szImei);

		// 请求头
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization","Bearer " + tokenString);

		headers.put("Content-Type", "application/x-www-form-urlencoded");
//        headers.put("Content-Type", "application/json");

		GlobalHttp.post(Constant.UserSourceHost+"api/v1/pbbprivacy",bundle ,headers, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String s) {

			}

			@Override
			public void onError(Throwable throwable, boolean b) {

			}

			@Override
			public void onCancelled(CancelledException e) {

			}

			@Override
			public void onFinished() {

			}
		});
	}

	private void init_listener() {
		// gridview事件处理
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case CipherTotalAdapter.ITEM_PIC:
					// 如果图片为-1，就设置为0;
					GlobalData.Image.instance(getActivity()).changeNewNum(-1);
					changeFragment(Pbb_Fields.TAG_CIPHER_IMAGE);
					break;

				case CipherTotalAdapter.ITEM_FILE:
					// 如果pdf为-1，就设置为0;
					GlobalData.Pdf.instance(getActivity()).changeNewNum(-1);
					changeFragment(Pbb_Fields.TAG_CIPHER_FILE);
					break;

				case CipherTotalAdapter.ITEM_VIDEO:
					// 如果vodeo为-1，就设置为0;
					GlobalData.Video.instance(getActivity()).changeNewNum(-1);
					changeFragment(Pbb_Fields.TAG_CIPHER_VIDEO);
					break;

				default:
					break;
				}
				mAdapter.notifyDataSetChanged(); // 消除列表对应条目“new”字样
				GlobalObserver.getGOb().notifyObservers(ObTag.Refresh); // 主要用于主界面“隐私空间”条目“new”字样的现实与否
			}
		});
	}

	/**   
	* @author 李巷阳
	* @date 2016-11-14 下午4:44:05 
	*/
	private void init_adapter() {
		mAdapter = new CipherTotalAdapter(getActivity());
		mGridView.setAdapter(mAdapter);

		/*“appid”:”30”
“appversion”:”8.3.3.18”,
“logtype”:”Info”
“devhdid”:”xxxx!!xxxx”,
“logremark”:”6”,//隐私空间本地加密数量
*/

		getUserToken(userInfo);


	}


	@Override
	protected boolean isObserverEnabled() {
		return false;
	}


	@Override
	public void refreshUI() {
		mAdapter.notifyDataSetChanged();
	}

	private class CipherTotalAdapter extends BaseAdapter {
		private static final short TOTAL_COUNT = 3;// 总数
		private static final short ITEM_PIC = 0;// 图片
		private static final short ITEM_VIDEO = 1;// 视频
		private static final short ITEM_FILE = 2;// 文档

		private LayoutInflater mInflater;
		private Context mContext;

		public CipherTotalAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return TOTAL_COUNT;
		}

		@Override
		public Integer getItem(int position) {
			return 0;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			ViewHolder vh;
			if (v == null) {
				v = mInflater.inflate(R.layout.adapter_cipher_total, parent, false);
				vh = new ViewHolder();
				vh.newTxt = (TextView) v.findViewById(R.id.act_txt_new);
				vh.num = (TextView) v.findViewById(R.id.act_txt_num);
				v.setTag(vh);
			} else {
				vh = (ViewHolder) v.getTag();
			}
			// position等于0  为图片
			// position等于1  为视频
			// position等于2  为文档
			// position等于3  为总数
			GlobalData type = position == ITEM_PIC ? GlobalData.Image : position == ITEM_FILE ? GlobalData.Pdf : GlobalData.Video;
			// 获取对应类型的总数量size
			size = type.instance(mContext).getCopyPaths(true).size();


			// 获取新增数量大于0 则显示,否则则不显示。
			int newNum = type.instance(mContext).changeNewNum(0);
			vh.newTxt.setVisibility(newNum > 0 ? View.VISIBLE : View.GONE);
			vh.newTxt.setText(String.valueOf(newNum));
			switch (position) {
			case ITEM_PIC:
				v.setBackgroundResource(R.drawable.xml_cipher_image);
				vh.num.setText(String.format(Locale.CHINA, "图片（%d）", size));
				break;

			case ITEM_FILE:
				v.setBackgroundResource(R.drawable.xml_cipher_pdf);
				vh.num.setText(String.format(Locale.CHINA, "文档（%d）", size));
				break;

			case ITEM_VIDEO:
				v.setBackgroundResource(R.drawable.xml_cipher_video);
				vh.num.setText(String.format(Locale.CHINA, "视频（%d）", size));
				break;

			default:
				break;
			}
			return v;
		}

		class ViewHolder {
			TextView newTxt;
			TextView num;
		}

	}

}
