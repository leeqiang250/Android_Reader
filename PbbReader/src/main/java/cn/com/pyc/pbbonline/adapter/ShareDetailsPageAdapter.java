package cn.com.pyc.pbbonline.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.qlk.util.tool.Util.ViewUtil;
import com.sz.mobilesdk.SZAlbumInterface;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.database.bean.Downdata;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.database.practice.AlbumDAOImpl;
import com.sz.mobilesdk.database.practice.DowndataDAOImpl;
import com.sz.mobilesdk.manager.DownloadTaskManager;
import com.sz.mobilesdk.models.FolderInfo;
import com.sz.mobilesdk.receiver.DownloadReceiver;
import com.sz.mobilesdk.util.APIUtil;
import com.sz.mobilesdk.util.ConvertToUtil;
import com.sz.mobilesdk.util.DownloadTaskUtil;
import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.TimeUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.mobilesdk.util.UIHelper.DialogCallBack;
import com.sz.view.widget.ToastShow;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.ShareDetailsPageActivity;
import cn.com.pyc.pbbonline.bean.event.DownloadFolderEvent;
import cn.com.pyc.pbbonline.bean.event.RefreshDeviceNumEvent;
import cn.com.pyc.pbbonline.bean.event.UpdateFolderEvent;
import cn.com.pyc.pbbonline.common.Code;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.common.ShareMode;
import cn.com.pyc.pbbonline.model.CheckResultBean;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.SendMsgShareUtil;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.widget.ProgressButton;
import cn.com.pyc.user.CheckLoginActivity;
import cn.com.pyc.user.LoginVerifyCodeActivity;
import de.greenrobot.event.EventBus;

public class ShareDetailsPageAdapter extends BaseAdapter
{
	private List<FolderInfo> mList;
	private PbbBaseActivity context;
	private ListView mListView;
	private String theme;			//分享主题
	private String owner;			//分享者
	private HashSet<String> mDownloadSize = new HashSet<String>();
	private Cancelable mCheckHttp;

	String OPENText;			//打开
	String DOWNLOADText;		//下载
	String UPDATEText;			//更新
	String PARSERText;			//解析..
	String CONNECTText;			//连接
	String AGAINText;			//继续
	String WAITText;			//等待...

	public void setListView(ListView mListView)
	{
		this.mListView = mListView;
	}

	public void setTheme(String theme)
	{
		this.theme = theme;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	/** 取消下载检验（一般在页面ondestory时执行） */
	public void cancelCheckHttp()
	{
		GlobalHttp.cancelHttp(mCheckHttp);
	}

	public ShareDetailsPageAdapter(PbbBaseActivity atc, List<FolderInfo> lists)
	{
		super();
		mList = lists;
		context = atc;

		OPENText = context.getString(R.string.look);
		DOWNLOADText = context.getString(R.string.download);
		UPDATEText = context.getString(R.string.update);
		PARSERText = context.getString(R.string.parser);
		WAITText = context.getString(R.string.wait);
		CONNECTText = context.getString(R.string.connect);
		AGAINText = context.getString(R.string.again);

		mDownloadSize.clear();
	}

	public List<FolderInfo> getInfos()
	{
		return mList;
	}

	@Override
	public int getCount()
	{
		return mList != null ? mList.size() : 0;
	}

	@Override
	public FolderInfo getItem(int position)
	{
		return mList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (null == convertView)
		{
			convertView = View.inflate(context, R.layout.pbbonline_item_share_details, null);
		}
		ViewHolder holder = new ViewHolder(convertView);
		convertView.setTag(position);
		FolderInfo o = mList.get(position);
		o.setPosition(position);
		o.setTheme(this.theme);

		holder.album_name.setText(o.getProductName());
		holder.album_time.setText(TimeUtil.getDateStringFromMills(o.getPublishDate()));
		holder.album_size.setText(FormatterUtil.formatSize(o.getFolderSize()));
		setBtnByData(context, holder, o);
		////showStateByData(context, holder, o);
		return convertView;
	}

	private void setBtnByData(final PbbBaseActivity atc, ViewHolder holder, final FolderInfo o)
	{
		checkFileUpdate(holder, o);
		setNormalButton(holder.download_btn, OPENText);
		holder.download_btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//打开单集文件列表
				OpenPageUtil.openFileListPage(atc, o.getMyProId(), o.getProductName(),
						o.getPublishDate(), theme, owner, o.getDelFileIds());
			}
		});
	}

	/**
	 * 检查文件id,判断是否有更新
	 * 
	 * @param holder
	 * @param o
	 */
	private void checkFileUpdate(ViewHolder holder, FolderInfo o)
	{
		if (holder.dot_img.getVisibility() == View.VISIBLE)
			ViewUtil.gone(holder.dot_img);
		String publishDate = o.getPublishDate();
		String savePublishDate = AlbumDAOImpl.getInstance()
				.findPublishDateByMyProId(o.getMyProId());
		SZLog.d("folder_publishDate = " + publishDate);
		SZLog.d("savess_publishDate = " + savePublishDate);
		if (TextUtils.isEmpty(publishDate))	//发布时间和保存的发布时间为空，不处理
			return;
		if (TextUtils.isEmpty(savePublishDate))
			return;
		if (TextUtils.equals(publishDate, savePublishDate)) //发布时间没有更改，不更新
			return;
		if (o.getFileIds() == null || o.getFileIds().isEmpty())
			return;
		StringBuilder builder = new StringBuilder();
		for (String id : o.getFileIds().values())
		{
			builder.append(id).append(";");
		}
		SZLog.i("----------------------------------------------------------");
		String fileIdString = builder.toString();
		SZLog.d("effect fileIds", fileIdString);
		List<String> contentIds = AlbumContentDAOImpl.getInstance().findContentIdByMyProId(
				o.getMyProId());
		SZLog.d("effect saveIds", contentIds.toString());
		if (contentIds == null || contentIds.isEmpty())
			return;
		List<String> delFileIds = new ArrayList<String>();
		for (Iterator<String> it = contentIds.iterator(); it.hasNext();)
		{
			String contentId = it.next(); //保存的id
			if (!fileIdString.contains(contentId))
			{
				delFileIds.add(contentId);
			}
		}
		o.setDelFileIds(delFileIds);
		SZLog.w("delete fileIds", delFileIds.toString());
		SZLog.i("----------------------------------------------------------");
		holder.dot_img.setVisibility(delFileIds.isEmpty() ? View.GONE : View.VISIBLE);
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////

	@Deprecated
	private void showStateByData(final PbbBaseActivity atc, final ViewHolder holder,
			final FolderInfo o)
	{
		String myProId = o.getMyProId();
		final Album a_ = SZAlbumInterface.findAlbumByMyProId(myProId);
		if (null == a_)
		{
			//网络数据
			if (holder.album_size.getVisibility() == View.GONE)
				holder.album_size.setVisibility(View.VISIBLE);
			setNormalButton(holder.download_btn, DOWNLOADText);
			Downdata downData = DowndataDAOImpl.getInstance().findDowndataById(myProId);
			int taskState = o.getTaskState();
			changeView(atc, taskState, holder.download_btn);
			//存在下载记录，显示下载进度，继续
			if (downData != null)
			{
				if (taskState == DownloadTaskManager.INIT)
				{
					//有下载记录初始化进入，暂停状态，显示继续
					setNormalButton(holder.download_btn, AGAINText);
				}
				if (taskState == DownloadTaskManager.PARSER)
				{
					//完成下载，正在解析
					setHandleButton(holder.download_btn, PARSERText);
				}
				if (taskState == DownloadTaskManager.CONNECTING)
				{
					//连接中..
					setHandleButton(holder.download_btn, CONNECTText);
				}
			}
			// 正在下载中...显示更新进度
			if (o.getTaskState() == DownloadTaskManager.DOWNLOADING)
			{
				setDownloadButton(holder.download_btn, o.getProgress());
			}
			holder.download_btn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					//校验下载
					download(atc, o);
				}
			});
			return;
		}
		//本地数据,打开专辑。
		SZLog.d("Local：" + a_.getItem_number() + "，《" + o.getProductName() + "》");
		if (holder.album_size.getVisibility() == View.VISIBLE)
			holder.album_size.setVisibility(View.GONE);
		setNormalButton(holder.download_btn, OPENText);
		holder.download_btn.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				//打开专辑
				OpenPageUtil.openMultipleMedia(atc, a_);
			}
		});
		checkAlbumHasFresh(a_, o, holder);
	}

	@Deprecated
	public void download(PbbBaseActivity atc, final FolderInfo o)
	{
		String myProId = o.getMyProId();
		int position = o.getPosition();
		if (DownloadReceiver.sTaskIdSet.contains(myProId))
		{
			// 任务已存在，暂停下载任务
			DownloadTaskUtil.stopDownloadTask(atc, myProId);
			o.setTaskState(DownloadTaskManager.PAUSE);
			updateItemViewWhenDownload(atc, position, o);
		}
		else
		{
			int taskSize = DownloadReceiver.sTaskIdSet.size();
			if (taskSize >= Constant.sTaskCount)
			{
				// 限制任务个数
				atc.showToast(atc.getString(R.string.please_waiting_n_download, taskSize));
				return;
			}
			// 任务不存在,校验下载
			checkDownload(atc, o);
		}
	}

	/**
	 * 更新进度
	 * 
	 * @param atc
	 * @param position
	 * @param o
	 */
	@Deprecated
	public void updateProgress(PbbBaseActivity atc, int position, FolderInfo o)
	{
		View itemView = mListView.findViewWithTag(position);
		if (itemView == null)
			return;

		atc.hideBgLoading();
		ProgressButton mBtn = (ProgressButton) itemView.findViewById(R.id.album_downloadbtn);
		setDownloadButton(mBtn, o.getProgress());
	}

	/**
	 * 下载任务过程，单个item状态变化更新
	 * 
	 * @param context
	 * @param position
	 * @param info
	 */
	@Deprecated
	public void updateItemViewWhenDownload(PbbBaseActivity atc, int position, FolderInfo o)
	{
		SZLog.i("update item " + position);
		View itemView = mListView.findViewWithTag(position);
		if (itemView == null)
			return;

		ProgressButton mBtn = (ProgressButton) itemView.findViewById(R.id.album_downloadbtn);
		Downdata downData = DowndataDAOImpl.getInstance().findDowndataById(o.getMyProId());
		int taskState = o.getTaskState();

		changeView(atc, taskState, mBtn);

		//保存进度数据不为空,连接中..显示已存在下载进度
		if (downData != null && taskState == DownloadTaskManager.CONNECTING)
		{
			int progress = ConvertToUtil.toInt(downData.getProgress());
			setHandleButton(mBtn, progress + "%");
		}
	}

	/**
	 * 下载解析文件完成。
	 * 
	 * @param context
	 * @param position
	 *            位置
	 * @param infos
	 *            数据源
	 */
	@Deprecated
	public void updateItemViewParserOver(final PbbBaseActivity atc, final int position,
			final FolderInfo o)
	{
		int visiblePosition = mListView.getFirstVisiblePosition();
		int offset = position - visiblePosition;
		// 只有在可见区域才更新
		if (offset < 0 && offset > 10)
			return;
		View itemView = mListView.findViewWithTag(position);
		if (itemView == null)
			return;

		ProgressButton mPbtn = (ProgressButton) itemView.findViewById(R.id.album_downloadbtn);
		TextView mSizetv = (TextView) itemView.findViewById(R.id.album_size);
		final Album _a = SZAlbumInterface.findAlbumByMyProId(o.getMyProId());
		if (null != _a)
		{
			// 本地
			if (mSizetv.getVisibility() == View.VISIBLE)
				mSizetv.setVisibility(View.GONE);
			setNormalButton(mPbtn, OPENText);
			mPbtn.setOnClickListener(null);
			mPbtn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					OpenPageUtil.openMultipleMedia(atc, _a);
				}
			});
		}
	}

	/**
	 * 修改Button显示状态<br/>
	 * <br/>
	 * 按钮状态：正常；进度比；解析中..
	 * 
	 * @param context
	 * @param taskState
	 * @param mButton
	 */
	@Deprecated
	private void changeView(PbbBaseActivity atc, int taskState, ProgressButton mButton)
	{
		switch (taskState)
		{
			case DownloadTaskManager.WAITING:				//等待
				setNormalButton(mButton, WAITText);
				break;
			case DownloadTaskManager.CONNECTING:			//连接
				break;
			case DownloadTaskManager.PAUSE:					//暂停
				setNormalButton(mButton, AGAINText);
				break;
			case DownloadTaskManager.PARSER:				//开始解析
				atc.hideBgLoading();
				setHandleButton(mButton, PARSERText);
				break;
			case DownloadTaskManager.INIT:					//初始
				setNormalButton(mButton, DOWNLOADText);
				break;
			case DownloadTaskManager.DOWNLOAD_ERROR:		//下载错误
			case DownloadTaskManager.CONNECT_ERROR:			//连接错误
			default:										//非正常状态
				atc.hideBgLoading();
				setNormalButton(mButton, DOWNLOADText);
				break;
		}
	}

	/**
	 * 检查专辑文件是否有更新
	 * 
	 * @param a
	 * @param o
	 * @param holder
	 */
	@Deprecated
	private void checkAlbumHasFresh(final Album a, final FolderInfo o, final ViewHolder holder)
	{
		// 产品发布的时间
		String publishDate = o.getPublishDate();
		// 解析后保存的发布时间
		String savePublishDate = a.getPublishDate();
		SZLog.d("album_publishDate = " + publishDate);
		SZLog.d("saves_publishDate = " + savePublishDate);
		//发布时间和保存的发布时间为空，不处理
		if (TextUtils.isEmpty(publishDate))
			return;
		if (TextUtils.isEmpty(savePublishDate))
			return;
		//发布时间没有更改，不更新
		if (TextUtils.equals(publishDate, savePublishDate))
			return;
		final String myProId = a.getMyproduct_id();
		setNormalButton(holder.download_btn, UPDATEText);
		holder.dot_img.setVisibility(View.VISIBLE);
		if (holder.album_size.getVisibility() == View.VISIBLE)
			holder.album_size.setVisibility(View.GONE);
		holder.download_btn.setOnClickListener(null);
		holder.download_btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// 发送通知：1.删除此前下载的专辑，2.删除权限，3.重新下载
				holder.dot_img.setVisibility(View.GONE);
				EventBus.getDefault().post(new UpdateFolderEvent(o, myProId));
			}
		});
	}

	private static class ViewHolder
	{
		TextView album_name;
		TextView album_time;
		TextView album_size;
		ProgressButton download_btn;
		ImageView dot_img;

		public ViewHolder(View convertView)
		{
			if (convertView == null)
				throw new IllegalArgumentException("Illegal args convertView!");

			album_name = (TextView) convertView.findViewById(R.id.album_name);
			album_time = (TextView) convertView.findViewById(R.id.album_time);
			album_size = (TextView) convertView.findViewById(R.id.album_size);
			download_btn = (ProgressButton) convertView.findViewById(R.id.album_downloadbtn);
			dot_img = (ImageView) convertView.findViewById(R.id.album_dot_img);
		}
	}

	//正常状态
	private void setNormalButton(ProgressButton btn, String text)
	{
		btn.setState(ProgressButton.NORMAL);
		btn.setProgress(0);
		btn.setCurrentText(text);
	}

	//下载状态
	private void setDownloadButton(ProgressButton btn, int progress)
	{
		btn.setState(ProgressButton.DOWNLOADING);
		btn.setProgressText("", progress);
	}

	//处理状态(主要是在解析文件时)
	private void setHandleButton(ProgressButton btn, String text)
	{
		btn.setState(ProgressButton.IN_HANDING);
		btn.setCurrentText(text);
	}

	/**
	 * 下载校验 <br/>
	 * 
	 * @param atc
	 * @param o
	 * @param btn
	 */
	@Deprecated
	private void checkDownload(final PbbBaseActivity atc, final FolderInfo o)
	{
		if (o.getTaskState() == DownloadTaskManager.PARSER)
		{
			ToastShow.getToast().showBusy(atc.getApplicationContext(),
					atc.getString(R.string.please_waiting_now_parser));
			return;
		}
		if (!mDownloadSize.isEmpty())
		{
			UIHelper.showToast(atc.getApplicationContext(),
					atc.getString(R.string.please_waiting_now_checking));
			return;
		}
		mDownloadSize.add(o.getMyProId());
		atc.showBgLoading(atc, atc.getString(R.string.checking));
		Bundle bundle = new Bundle();
		bundle.putString("shareFolderId", o.getMyProId());
		bundle.putString("shareId", (String) SPUtil.get(Fields.FIELDS_ID, ""));
		bundle.putString("myToken", (String) SPUtil.get(Fields.FIELDS_LOGIN_TOKEN, ""));
		bundle.putString("receiveID", (String) SPUtil.get(Fields.FIELDS_RECEIVE_ID, ""));
		bundle.putString("deviceIdentifier", Constant.TOKEN);
		bundle.putString("deviceType", K.platform);
		bundle.putString("accountId", "");
		mCheckHttp = GlobalHttp.get(APIUtil.getDownloadCheckUrl(), bundle,
				new Callback.CommonCallback<String>()
				{
					@Override
					public void onCancelled(CancelledException arg0)
					{
					}

					@Override
					public void onError(Throwable arg0, boolean arg1)
					{
						SZLog.v("check", "fail: " + arg0.getMessage());
						UIHelper.showToast(atc, atc.getString(R.string.load_server_failed));
					}

					@Override
					public void onFinished()
					{
						mDownloadSize.clear();
						atc.hideBgLoading();
					}

					@Override
					public void onSuccess(String arg0)
					{
						SZLog.d("check", "success: " + arg0);
						CheckResultBean c = JSON.parseObject(arg0, CheckResultBean.class);
						judgeCode(atc, c, o);
						if (c != null && String.valueOf(Code._SUCCESS).equals(c.getCode()))
						{
							//已经登录成功，（后台已领取成功），发送消息保存记录\
							//已经绑定成功过
							SendMsgShareUtil.sendMsg2SaveRecord(o.getTheme(), owner);
						}
					}
				});
	}

	/**
	 * 校验绑定
	 * 
	 * @param atc
	 * @param oo
	 */
	@Deprecated
	private void checkBindDevice(final PbbBaseActivity atc, final FolderInfo oo)
	{
		UIHelper.showCommonDialog(atc, oo.getProductName(),
				atc.getString(R.string.ask_bind_device), atc.getString(R.string.bind),
				new DialogCallBack()
				{
					@Override
					public void onConfirm()
					{
						bindDevice(atc, oo);
					}
				});
	}

	@Deprecated
	private void bindDevice(final PbbBaseActivity atc, final FolderInfo o)
	{
		atc.showBgLoading(atc, atc.getString(R.string.checking));
		Bundle bundle = new Bundle();
		bundle.putString("deviceIdentifier", Constant.TOKEN);
		bundle.putString("shareId", (String) SPUtil.get(Fields.FIELDS_ID, ""));
		bundle.putString("myToken", (String) SPUtil.get(Fields.FIELDS_LOGIN_TOKEN, ""));
		bundle.putString("receiveID", (String) SPUtil.get(Fields.FIELDS_RECEIVE_ID, ""));//绑定按身份创建的分享必传，绑定按设备创建的分享不必传
		bundle.putString("registrationid", (String) SPUtil.get(Fields.FIELDS_JPUSH_REGISTERID, ""));
		bundle.putString("shareFolderId", o.getMyProId());

		GlobalHttp.getOn(APIUtil.bindDevicesUrl(), bundle, new Callback.CommonCallback<String>()
		{

			@Override
			public void onCancelled(CancelledException arg0)
			{
			}

			@Override
			public void onError(Throwable arg0, boolean arg1)
			{
				UIHelper.showToast(atc, atc.getString(R.string.bind_device_fail));
				atc.hideBgLoading();
			}

			@Override
			public void onFinished()
			{
			}

			@Override
			public void onSuccess(String arg0)
			{
				SZLog.d("bind:" + arg0);
				CheckResultBean c = JSON.parseObject(arg0, CheckResultBean.class);
				judgeCode(atc, c, o);
				if (c != null && c.isSuccess())
				{
					//绑定成功，刷新绑定数
					EventBus.getDefault().post(new RefreshDeviceNumEvent());
					//按设备分享绑定成功，发送通知保存记录
					SendMsgShareUtil.sendMsg2SaveRecordByDevice(o.getTheme(), owner);
				}
			}
		});
	}

	/**
	 * 登录校验（按身份和人数分享时）
	 * 
	 * @param atc
	 * @param o
	 */
	@Deprecated
	private void checkLogin(final PbbBaseActivity atc, final FolderInfo o)
	{
		atc.hideBgLoading();
		if (ShareMode.SHARECOUNT.equals(ShareMode.Mode.value))
		{
			//按人数分享，未登录就直接去登录
			Intent intent = new Intent(atc, LoginVerifyCodeActivity.class);
			atc.startActivityForResult(intent, ShareDetailsPageActivity.CODE_VERIFY);
			return;
		}
		Intent intent = new Intent(atc, CheckLoginActivity.class);
		atc.startActivityForResult(intent, ShareDetailsPageActivity.CODE_VERIFY);
	}

	/**
	 * 领取分享 (按身份分享时) <br/>
	 * 1.未登录：验证手机号时领取 <br/>
	 * 2.已登录：校验下载中已领取<br/>
	 * 3.按身份分享指定其他手机号，先登录，后领取<br/>
	 * 
	 * @param atc
	 * @param o
	 */
	@Deprecated
	private void receiveShared(final PbbBaseActivity atc, final FolderInfo o)
	{
		atc.showBgLoading(atc, atc.getString(R.string.receiving));
		String phone = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
		Util_.receiveVerifyShared(phone, new Callback.CommonCallback<String>()
		{

			@Override
			public void onCancelled(CancelledException arg0)
			{
			}

			@Override
			public void onError(Throwable arg0, boolean arg1)
			{
				UIHelper.showToast(atc, atc.getString(R.string.receive_share_fail));
			}

			@Override
			public void onFinished()
			{
				atc.hideBgLoading();
			}

			@Override
			public void onSuccess(String arg0)
			{
				SZLog.d("adapter receive:" + arg0);
				CheckResultBean c = JSON.parseObject(arg0, CheckResultBean.class);
				judgeCode(atc, c, o);
				if (String.valueOf(Code._SUCCESS).equals(c.getCode()))
				{
					//领取分享成功，提示绑定设备
					checkBindDevice(atc, o);
					SendMsgShareUtil.sendMsg2SaveRecordByUser(o.getTheme(), owner);
					UIHelper.showToast(atc.getApplicationContext(),
							atc.getString(R.string.receive_share_success));
				}
			}
		});
	}

	/**
	 * 登录失效，重新登录
	 * 
	 * @param atc
	 * @param name
	 * @param msg
	 */
	@Deprecated
	private void loginAgain(PbbBaseActivity atc, String msg)
	{
		atc.hideBgLoading();
		Intent intent = new Intent(atc, LoginVerifyCodeActivity.class);
		intent.putExtra("phone_number", (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, ""));
		atc.startActivityForResult(intent, ShareDetailsPageActivity.CODE_LOGIN_AGAIN);
		UIHelper.showToast(atc.getApplicationContext(), msg);
	}

	////////////////////////////////////////////////////
	//////////////////状态码Code.java/////////////////////
	////////////////////////////////////////////////////

	/**
	 * 状态码。 {@link Code}
	 * 
	 * @param atc
	 * @param c
	 *            返回数据模型
	 * @param o
	 *            下载信息模型
	 */
	@Deprecated
	private void judgeCode(PbbBaseActivity atc, CheckResultBean c, FolderInfo o)
	{
		switch (c.getCode())
		{
			case Code._SUCCESS:
				if (!TextUtils.isEmpty(c.getFtpUrl()))
				{
					o.setFtpUrl(c.getFtpUrl());								//初始化ftpUrl
					EventBus.getDefault().post(new DownloadFolderEvent(o));	//发送通知下载
				}
				break;
			case Code._8001:
				//已登录，请先领取分享,接收分享操作
				receiveShared(atc, o);
				break;
			case Code._8002:
				//请先绑定分享设备
				checkBindDevice(atc, o);
				break;
			case Code._8006:
				//登录失效，请重新登录
				loginAgain(atc, c.getMsg());
				break;
			case Code._9007:
				//您尚未登录
				checkLogin(atc, o);
				break;
			case Code._9002:
			case Code._9003:
			case Code._9004:
			case Code._9005:
			case Code._9006:
			case Code._9008:
			case Code._9010:
			case Code._9011:
			case Code._9012:
			case Code._9013:
			case Code._9014:
			case Code._9016:
			case Code._9017:
			case Code._9022:
			{
				atc.hideBgLoading();
				UIHelper.showSingleCommonDialog(atc, o.getProductName(), c.getMsg(),
						atc.getString(R.string.i_know), null);
			}
				break;
			case Code._9001:
			case Code._9009:
			case Code._9015:
			case Code._9018:
			case Code._9019:
			case Code._9020:
			case Code._9021:
			{
				atc.hideBgLoading();
				ToastShow.getToast().showFail(atc, c.getMsg());
			}
				break;
			default:
				UIHelper.showToast(atc.getApplicationContext(), "服务器校验错误");
				break;
		}
	}

}
