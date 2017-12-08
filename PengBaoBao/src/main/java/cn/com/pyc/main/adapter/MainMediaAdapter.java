package cn.com.pyc.main.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cn.com.pyc.pbb.R;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.plain.record.MainMediaItemEnum;

/**
 * 
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-11-7 下午6:13:41
 * @version V1.0
 */
@Deprecated
public class MainMediaAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private Context mctx;
	public MainMediaAdapter(Context ctx) {
		this.mctx=ctx;
		inflater = LayoutInflater.from(ctx);
	}

	@Override
	public int getCount() {
		return MainMediaItemEnum.TOTAL_COUNT;
	}

	@Override
	public Integer getItem(int position) {
		return 0;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		v = inflater.inflate(R.layout.adapter_media_main, parent, false);
		TextView name = (TextView) v.findViewById(R.id.amm_txt_content);
		MainMediaItemEnum item = MainMediaItemEnum.getItem(position);
		name.setText(item.name);
		name.setCompoundDrawablesWithIntrinsicBounds(item.drawable, 0, 0, 0);
		v.findViewById(R.id.amm_imv_divider1).setVisibility(item.dvd1);
		v.findViewById(R.id.amm_imv_divider2).setVisibility(item.dvd2);
		v.findViewById(R.id.amm_imv_divider3).setVisibility(item.dvd3);

		initView(v, position);
		return v;
	}

	private void initView(View v, int position) {
		TextView count = (TextView) v.findViewById(R.id.amm_txt_count);
		View newflag = v.findViewById(R.id.amm_imv_new);

		switch (position) {
		case MainMediaItemEnum.ITEM_SM_SEND:
			// GlobalData.Sm.instance(mctx).getCopyPaths(true) true说明只有Ciphers 调用，才有数据。
			int sendcount = GlobalData.Sm.instance(mctx).getCopyPaths(true).size();

			if (sendcount == 0) {
				v.findViewById(R.id.amm_lyt_1).setVisibility(View.GONE);
			}else {
				count.setText(String.valueOf(sendcount));
			}
			break;

		/*case MainMediaItemEnum.ITEM_SM_RECEIVE:
			// GlobalData.Sm.instance(mctx).getCopyPaths(false) false说明SortedPaths 调用，才有数据。
			int recount = GlobalData.Sm.instance(mctx).getCopyPaths(false).size();

			if (recount == 0) {
				v.findViewById(R.id.amm_lyt_1).setVisibility(View.GONE);
			}else {
				count.setText(String.valueOf(recount));
			}
			break;*/

		/*case MainMediaItemEnum.ITEM_CIPHER:

			count.setText(String.valueOf(GlobalData.getTotalCount(mctx, true)));
			if (GlobalData.Image.instance(mctx).changeNewNum(0) > 0 || GlobalData.Pdf.instance(mctx).changeNewNum(0) > 0 || GlobalData.Video.instance(mctx).changeNewNum(0) > 0) {
				newflag.setVisibility(View.VISIBLE);
			}
			break;*/

		default:
			v.findViewById(R.id.amm_lyt_1).setVisibility(View.GONE);
			v.findViewById(R.id.amm_lyt_2).setVisibility(View.VISIBLE);
			break;
		}
/*
		String str = count.getText().toString();
		int intcount = Integer.parseInt(str);
		if (intcount == 0) {
			v.findViewById(R.id.amm_lyt_1).setVisibility(View.GONE);
		}*/
	}

}
