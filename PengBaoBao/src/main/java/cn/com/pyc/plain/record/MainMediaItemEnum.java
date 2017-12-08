package cn.com.pyc.plain.record;

import android.view.View;
import cn.com.pyc.pbb.R;

/*-
 * 这个主要是实现发送、接受列表以及隐私空间条目的界面
 * 
 * 用xml的形式也可以实现，但在刷新等操作的时候有点麻烦
 * 另外也是为了熟悉enum，故这样做
 */
public enum MainMediaItemEnum {

	SEND(" 已发送", R.drawable.item_sm_send_icon, View.VISIBLE, View.VISIBLE, View.GONE), //
	RECEIVE(" 已接收", R.drawable.item_sm_receive_icon, View.GONE, View.GONE, View.VISIBLE), //
	BLANK("", 0, View.GONE, View.GONE, View.GONE); //
//	CIPHER(" 隐私空间", R.drawable.item_cipher_icon, View.VISIBLE, View.GONE, View.VISIBLE);

	public static final int ITEM_SM_SEND = 0;
	public static final int ITEM_SM_RECEIVE = 1;
	public static final int ITEM_BLANK = 2;
//	public static final int ITEM_CIPHER = 3;
	public static final int TOTAL_COUNT = 3;

	public String name;
	public int drawable;
	public int dvd1;
	public int dvd2;
	public int dvd3;

	private MainMediaItemEnum(String name, int drawable, int dvd1, int dvd2, int dvd3) {
		this.name = name;
		this.drawable = drawable;
		this.dvd1 = dvd1;
		this.dvd2 = dvd2;
		this.dvd3 = dvd3;
	}

	public static MainMediaItemEnum getItem(int pos) {
		MainMediaItemEnum item = null;
		switch (pos) {
		case ITEM_SM_SEND:
			item = SEND;
			break;

		case ITEM_SM_RECEIVE:
			item = RECEIVE;
			break;

		case ITEM_BLANK:
			item = BLANK;
			break;

		/*case ITEM_CIPHER:
			item = CIPHER;
			break;*/

		default:
			break;
		}
		return item;
	}

}
