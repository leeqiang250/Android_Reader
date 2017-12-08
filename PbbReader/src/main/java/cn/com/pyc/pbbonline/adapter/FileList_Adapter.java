package cn.com.pyc.pbbonline.adapter;

import java.util.List;
import java.util.Random;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.util.ValidDateUtil;

import com.qlk.util.tool.Util.ViewUtil;
import com.sz.mobilesdk.authentication.SZContent;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.manager.DownloadTaskManager2;
import com.sz.mobilesdk.manager.db.DownData2DBManager;
import com.sz.mobilesdk.models.DownData2;
import com.sz.mobilesdk.models.FileData;
import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.StringUtil;
import com.sz.view.widget.RoundProgressBar;

public class FileList_Adapter extends BaseAdapter
{
	private static final String TAG = "FileList_Adapter";
	private PbbBaseActivity mContext;
	private List<FileData> list;
	private ListView listView;
	private String publishDate;
	private String curFileId;
	private RotateAnimation arcRotation;
	private Random random = new Random();

	private int selectColorId;
	private int unSelectNameColorId;
	private int unSelectTimeColorId;
	private Drawable selectDrawable;
	private Drawable unSelectDrawable;

	// 下载相关
	int roundColor;
	int progressColor;
	int toumingColor;
	Drawable downloadDrawable;
	Drawable pauseDrawable;
	Drawable loadingDrawable;

	String waitingStr;
	String connectStr;
	String checkingStr;
	String waitCheckingStr;
	String pauseDownloadStr;

	String unDownloadLable;
	String unDownloadStatus;
	String downloadStatus;

	public List<FileData> getList()
	{
		return list;
	}

	public void setListViewDate(ListView listView, String publishDate)
	{
		this.listView = listView;
		this.publishDate = publishDate;
	}

	public void setFileId(String curFileId)
	{
		this.curFileId = curFileId;
	}

	public FileList_Adapter(PbbBaseActivity mContext, List<FileData> list)
	{
		this.mContext = mContext;
		this.list = list;

		Resources resources = this.mContext.getResources();
		this.selectColorId = resources.getColor(R.color.blue_bar_color);
		this.unSelectNameColorId = resources.getColor(R.color.black_bb);
		this.unSelectTimeColorId = resources.getColor(R.color.gray);
		this.selectDrawable = resources.getDrawable(R.drawable.ic_validate_time_select);
		this.unSelectDrawable = resources.getDrawable(R.drawable.ic_validate_time_nor);

		roundColor = resources.getColor(R.color.round_color);
		progressColor = resources.getColor(R.color.progress_color);
		toumingColor = resources.getColor(R.color.transparent);
		downloadDrawable = resources.getDrawable(R.drawable.download_button);
		pauseDrawable = resources.getDrawable(R.drawable.download_button);
		loadingDrawable = resources.getDrawable(R.drawable.ic_circle_loading);

		waitingStr = resources.getString(R.string.Waiting);
		connectStr = resources.getString(R.string.Connecting);
		checkingStr = resources.getString(R.string.Checking);
		waitCheckingStr = resources.getString(R.string.Wait_checking);

		unDownloadLable = resources.getString(R.string.downloaditem_status_lable);
		unDownloadStatus = resources.getString(R.string.downloaditem_no);
		downloadStatus = resources.getString(R.string.downloaditem_yeah);
		pauseDownloadStr = resources.getString(R.string.Pause_download);

		arcRotation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		arcRotation.setDuration(800);
		arcRotation.setRepeatCount(Animation.INFINITE);
		arcRotation.setRepeatMode(Animation.RESTART);
		arcRotation.setStartOffset(50);
		arcRotation.setInterpolator(new LinearInterpolator());
	}

	@Override
	public int getCount()
	{
		return list != null ? list.size() : 0;
	}

	@Override
	public FileData getItem(int position)
	{
		return list.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
			convertView = View.inflate(mContext, R.layout.pbbonline_item_files_list, null);
		convertView.setTag(position);
		ViewHolder holder = new ViewHolder(convertView);
		final FileData fileData = list.get(position);
		fileData.setPosition(position); // 赋值position
		fileData.setSharefolder_publish_date(publishDate);//赋值文件夹publishDate
		holder.txtTitle.setText(fileData.getName());

		//被收回的文件,ListFileUI中文件id设置为-1；
		if ("-1".equals(fileData.getFiles_id()))
		{
			ViewUtil.visible(holder.txtRevoke);
			ViewUtil.gone(holder.bar);
			ViewUtil.gone(holder.txtSize);
			ViewUtil.gone(holder.arrow);
			ViewUtil.visible(holder.txtLable);
			holder.txtStatus.setText(downloadStatus);
			holder.txtTitle.setTextColor(unSelectTimeColorId);
		}
		else
		{
			resetViewColor(holder);
			ViewUtil.gone(holder.txtRevoke);
			AlbumContent ac = AlbumContentDAOImpl.getInstance().findAlbumContentByContentId(
					fileData.getFiles_id());
			if (ac == null)
			{
				ViewUtil.visible(holder.bar);
				ViewUtil.visible(holder.txtSize);
				ViewUtil.visible(holder.txtLable);
				ViewUtil.gone(holder.arrow);
				holder.txtSize.setText(FormatterUtil.formatSize(fileData.getFileSize()));
				holder.txtLable.setText(unDownloadLable);
				DownData2 data2 = DownData2DBManager.Builder().findByFileId(fileData.getFiles_id());
				holder.txtStatus.setText(data2 == null ? unDownloadStatus : pauseDownloadStr);
				holder.bar.setProgress(0);
				holder.bar.setBackgroundDrawable(downloadDrawable);
				holder.bar.setTextColor(toumingColor);
				holder.bar.setCricleColor(toumingColor);

				if (fileData.getTaskState() != DownloadTaskManager2.FINISHED) //下载后删除数据，不执行parserOver();
					changeView(mContext, fileData, holder);
				if (data2 != null && fileData.getTaskState() == DownloadTaskManager2.CONNECTING)
				{
					fileData.setTaskState(DownloadTaskManager2.INIT);
					init(fileData, holder);
				}
			}
			else
			{
				SZLog.v(TAG, "本地已下载：" + ac.getName());
				if (StringUtil.isEmptyOrNull(ac.getMyProId())) // 更新老版本保存的数据
				{
					ac.setMyProId(fileData.getSharefolder_id());
					AlbumContentDAOImpl.getInstance().updateAlbumContent(ac);
				}
				ViewUtil.gone(holder.bar);
				ViewUtil.gone(holder.txtSize);
				ViewUtil.gone(holder.txtLable);
				ViewUtil.visible(holder.arrow);
				SZContent szcont = new SZContent(ac.getAsset_id());
				holder.txtStatus.setText(ValidDateUtil.getValidTime(mContext,
						szcont.getAvailbaleTime(), szcont.getOdd_datetime_end()));

				setPressSelector(ac.getContent_id(), holder);
			}
		}
		return convertView;
	}

	private void setPressSelector(String id, ViewHolder holder)
	{
		if (TextUtils.equals(id, curFileId))
		{
			holder.txtTitle.setTextColor(selectColorId);
			holder.txtStatus.setTextColor(selectColorId);
			holder.txtLable.setTextColor(selectColorId);
			holder.ivTime.setImageDrawable(selectDrawable);
		}
		else
		{
			resetViewColor(holder);
		}
	}

	private void resetViewColor(ViewHolder holder)
	{
		holder.txtTitle.setTextColor(unSelectNameColorId);
		holder.txtStatus.setTextColor(unSelectTimeColorId);
		holder.txtLable.setTextColor(unSelectTimeColorId);
		holder.ivTime.setImageDrawable(unSelectDrawable);
	}

	/**
	 * 设置item对象状态
	 * 
	 * @param location
	 * @param state
	 */
	public void setItemDataState(int location, int state)
	{
		if (list == null)
			return;
		FileData o = this.list.get(location);
		if (o.getTaskState() == state)
			return;
		o.setTaskState(state);
	}

	/**
	 * 下载任务开始，单个item状态变化更新
	 * 
	 * @param o
	 *            FileData
	 */
	public void updateItemView(PbbBaseActivity acty, FileData o)
	{
		int position = o.getPosition();
		//int visiblePosition = listView.getFirstVisiblePosition();
		//int offset = position - visiblePosition;
		////if (offset < 0 && offset > 10)
		//	return;

		SZLog.d(TAG, "updateItemView，position = " + position);
		View itemView = listView.findViewWithTag(position);
		if (itemView == null)
			return;

		itemView.setTag(position);
		ViewHolder holder = new ViewHolder(itemView);
		ViewUtil.gone(holder.arrow);
		ViewUtil.visible(holder.bar);
		ViewUtil.visible(holder.txtSize);

		changeView(acty, o, holder);
	}

	/**
	 * 修改view状态
	 * 
	 * @param acty
	 * @param o
	 * @param holder
	 */
	private void changeView(PbbBaseActivity acty, FileData o, ViewHolder holder)
	{
		int taskState = o.getTaskState();
		switch (taskState)
		{
			case DownloadTaskManager2.INIT:
				init(o, holder);
				break;
			case DownloadTaskManager2.WAITING:
				waiting(o, holder);
				break;
			case DownloadTaskManager2.CONNECTING:
				connecting(o, holder);
				break;
			case DownloadTaskManager2.PAUSE:
				pause(o, holder);
				break;
			case DownloadTaskManager2.DOWNLOAD_ERROR:
				downloadError(o, holder);
				break;
			case DownloadTaskManager2.CONNECT_ERROR:
				connectError(o, holder);
				break;
			case DownloadTaskManager2.DOWNLOADING:
				downloading(o, holder);
				break;
			case DownloadTaskManager2.PARSERING:
				parsering(o, holder);
				break;
			case DownloadTaskManager2.FINISHED:
				parserOver(o, holder);
				break;
			case DownloadTaskManager2.CHECKING:
				checking(o, holder);
				break;
			case DownloadTaskManager2.WAITING_CHECKING:
				waitChecking(o, holder);
				break;
			default:
				break;
		}
		acty.hideBgLoading();
	}

	/**
	 * 开始状态
	 * 
	 * @param o
	 * @param holder
	 */
	private void init(FileData o, ViewHolder holder)
	{
		animStop(holder.bar);

		holder.txtSize.setText(FormatterUtil.formatSize(o.getFileSize()));
		holder.bar.setProgress(0);
		holder.bar.setBackgroundDrawable(downloadDrawable);
		holder.bar.setTextColor(toumingColor);
		holder.bar.setCricleColor(toumingColor);
	}

	/**
	 * 等待验证
	 * 
	 * @param o
	 * @param holder
	 */
	private void waitChecking(FileData o, ViewHolder holder)
	{
		animStop(holder.bar);

		holder.txtSize.setText(waitCheckingStr);
		holder.bar.setBackgroundDrawable(loadingDrawable);
		holder.bar.setCricleColor(toumingColor);
		holder.bar.setTextColor(progressColor);
	}

	/**
	 * 正在验证
	 * 
	 * @param o
	 * @param holder
	 */
	private void checking(FileData o, ViewHolder holder)
	{
		holder.txtSize.setText(checkingStr);
		holder.bar.setBackgroundDrawable(loadingDrawable);
		holder.bar.setCricleColor(toumingColor);
		holder.bar.setTextColor(progressColor);

		animPlay(holder.bar);
	}

	/**
	 * 等待下载
	 * 
	 * @param o
	 * @param holder
	 */
	private void waiting(FileData o, ViewHolder holder)
	{
		animStop(holder.bar);

		holder.txtSize.setText(waitingStr);
		holder.bar.setBackgroundDrawable(loadingDrawable);
		holder.bar.setCricleColor(toumingColor); // roundColor
		holder.bar.setTextColor(progressColor);
		holder.txtStatus.setText(unDownloadStatus);
	}

	/**
	 * 连接中
	 * 
	 * @param o
	 * @param holder
	 */
	private void connecting(FileData o, ViewHolder holder)
	{
		holder.txtSize.setText(connectStr);
		holder.bar.setBackgroundDrawable(loadingDrawable);
		holder.bar.setCricleColor(toumingColor); // progressColor
		holder.bar.setTextColor(progressColor);

		animPlay(holder.bar);

		// DownData2 data2 = DownData2DBManager.Builder().findByFileId(fileId);
		// holder.bar.setProgress(data2 != null ? data2.getProgress() : 0);
	}

	/**
	 * 下载中
	 * 
	 * @param o
	 * @param holder
	 */
	private void downloading(FileData o, ViewHolder holder)
	{
		animStop(holder.bar);

		if (holder.bar.getBackground() != null)
			holder.bar.setBackgroundDrawable(null);
		holder.bar.setCricleColor(roundColor);
		holder.bar.setTextColor(progressColor);
		holder.bar.setProgress(o.getProgress());
		ViewUtil.gone(holder.txtSize);
	}

	/**
	 * 暂停中
	 * 
	 * @param o
	 * @param holder
	 */
	private void pause(FileData o, ViewHolder holder)
	{
		animStop(holder.bar);

		holder.bar.setProgress(0);
		holder.bar.setBackgroundDrawable(pauseDrawable);
		holder.bar.setTextColor(toumingColor);
		holder.bar.setCricleColor(toumingColor);
		holder.txtSize.setText(FormatterUtil.formatSize(o.getFileSize()));
		holder.txtStatus.setText(pauseDownloadStr);
	}

	/**
	 * 解析文件
	 * 
	 * @param o
	 * @param holder
	 */
	private void parsering(FileData o, ViewHolder holder)
	{
		animStop(holder.bar);

		ViewUtil.gone(holder.txtSize);
		if (holder.bar.getBackground() != null)
			holder.bar.setBackgroundDrawable(null);
		holder.bar.setProgress((96 + random.nextInt(4)));
		holder.bar.setTextColor(progressColor);
		holder.bar.setCricleColor(roundColor);
		holder.txtStatus.setText("下载中，请勿退出^-^");
	}

	/**
	 * 下载异常，ftpPath: (shutdown, 404, -1等等)
	 * 
	 * @param o
	 * @param holder
	 */
	private void downloadError(FileData o, ViewHolder holder)
	{
		animStop(holder.bar);

		holder.bar.setProgress(0);
		holder.bar.setBackgroundDrawable(downloadDrawable);
		holder.bar.setCricleColor(toumingColor);
		holder.txtSize.setText(FormatterUtil.formatSize(o.getFileSize()));
		ViewUtil.visible(holder.bar);
		ViewUtil.visible(holder.txtSize);
	}

	/**
	 * 连接异常，一般服务端文件错误
	 * 
	 * @param o
	 * @param holder
	 */
	private void connectError(FileData o, ViewHolder holder)
	{
		animStop(holder.bar);

		holder.bar.setProgress(0);
		holder.bar.setBackgroundDrawable(downloadDrawable);
		holder.bar.setCricleColor(toumingColor);
		holder.txtSize.setText(FormatterUtil.formatSize(o.getFileSize()));
		ViewUtil.visible(holder.bar);
		ViewUtil.visible(holder.txtSize);
	}

	/**
	 * 解析包完成！
	 * 
	 * @param fileId
	 * @param holder
	 */
	private void parserOver(FileData o, ViewHolder holder)
	{
		animStop(holder.bar);

		ViewUtil.visible(holder.arrow);
		ViewUtil.gone(holder.bar);
		ViewUtil.gone(holder.txtSize);
		ViewUtil.gone(holder.txtLable);
		holder.txtStatus.setText(null);

		// 查询存储的数值
		AlbumContent ac = AlbumContentDAOImpl.getInstance().findAlbumContentByContentId(
				o.getFiles_id());
		if (ac != null)
		{
			SZContent szcont = new SZContent(ac.getAsset_id());
			holder.txtStatus.setText(ValidDateUtil.getValidTime(mContext,
					szcont.getAvailbaleTime(), szcont.getOdd_datetime_end()));
			holder.txtTitle.setText(ac.getName());
		}
	}

	private static class ViewHolder
	{
		TextView txtTitle;
		TextView txtLable;
		TextView txtStatus;
		RoundProgressBar bar;
		TextView txtSize;
		ImageView arrow;
		ImageView ivTime;
		TextView txtRevoke;

		public ViewHolder(View convertView)
		{
			txtTitle = (TextView) convertView.findViewById(R.id.tv_file_name);
			txtLable = (TextView) convertView.findViewById(R.id.tv_file_status_lable);
			txtStatus = (TextView) convertView.findViewById(R.id.tv_file_status);
			bar = (RoundProgressBar) convertView.findViewById(R.id.rpb_file);
			txtSize = (TextView) convertView.findViewById(R.id.tv_file_size);
			arrow = (ImageView) convertView.findViewById(R.id.iv_arrow_right);
			ivTime = (ImageView) convertView.findViewById(R.id.iv_file_status);
			txtRevoke = (TextView) convertView.findViewById(R.id.tv_file_state);
		}
	}

	/**
	 * 旋转动画开始
	 * 
	 * @param view
	 */
	private void animPlay(View view)
	{
		if (view.getAnimation() == null)
		{
			view.startAnimation(arcRotation);
		}
		else
		{
			view.getAnimation().reset();
			view.getAnimation().start();
		}
	}

	/**
	 * 动画停止结束
	 * 
	 * @param view
	 */
	private void animStop(View view)
	{
		if (view.getAnimation() != null)
		{
			view.getAnimation().cancel();
		}
		view.clearAnimation();
	}
}
