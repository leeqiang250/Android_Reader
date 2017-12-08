package cn.com.pyc.xcoder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qlk.util.global.GlobalObserver;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool._SysoXXX;
import com.qlk.util.tool.Util.FileUtil;

import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.db.PathDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.utils.Dirs;
import cn.com.pyc.xcoder.XCoder.IXCodeListener;
import cn.com.pyc.xcoder.XCoder.ReturnInfo;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 只管界面的展示、具体操作在XCodeTask中
 * 
 * @author QiLiKing
 */
public class XCodeView
{
	public enum XCodeType
	{
		Encrypt, Decrypt, MakeFree, MakePay;
	}

	private ProgressBar mProgressBar;
	private TextView mTitleView;
	private TextView mPercentView;
	private TextView mSizeView;
	private Dialog mDialog;
	private View mRootLayout;
	private Context mContext;
	private long totalSize;
	private long progress;
	private int totalNum;
	private int curNum;
	private XCoder mXCoder;
	private XCodeViewListener mViewListener;
	private HashMap<String, String> mXCodePaths = new HashMap<>();
	private XCodeType mType;
	private boolean showView;
	private Stack<String> mPaths = new Stack<>();

	public XCodeView(Context context)
	{
		mContext = context;
	}

	public void setXCodeViewListener(XCodeViewListener listener)
	{
		mViewListener = listener;
	}

	public View getShowView()
	{
		return mRootLayout;
	}

	/**
	 * @param type
	 * @param smInfo
	 *            Maybe null
	 * @param showView
	 * @param paths
	 */
	public void startXCode(XCodeType type, SmInfo smInfo, boolean showView, String... paths)
	{
		curNum = 1;
		totalNum = paths.length;
		mType = type;
		this.showView = showView;
		mXCoder = new XCoder(mContext);
		mXCoder.setXcodeListener(mListener);

		if (showView)
		{
			/* 初始化 */
			mRootLayout = LayoutInflater.from(mContext).inflate(R.layout.view_media_xcoder, null);
			mProgressBar = (ProgressBar) mRootLayout.findViewById(R.id.vmx_pb_progress);
			mTitleView = (TextView) mRootLayout.findViewById(R.id.vmx_txt_title);
			mPercentView = (TextView) mRootLayout.findViewById(R.id.vmx_txt_percent);
			mSizeView = (TextView) mRootLayout.findViewById(R.id.vmx_txt_size);

			initTitle(type);
			for (String path : paths)
			{
				totalSize += new File(path).length();
			}
			mProgressBar.setMax((int) (totalSize >> 10));

			if (smInfo == null)
			{
				mDialog = new Dialog(mContext, R.style.no_frame_small);
				mDialog.setCancelable(false);
				mDialog.setContentView(mRootLayout);
				mDialog.setCanceledOnTouchOutside(false);
				mDialog.show();
			}
		}

		mPaths.clear();
		for (String path : paths)
		{
			String desPath = getDesXCodePath(mContext, path, type);
			mXCodePaths.put(path, desPath);
			mPaths.push(path);
		}

		xcode(smInfo);
	}

	private void xcode(SmInfo smInfo)
	{
		String path = mPaths.pop();
		mXCoder.xcodeFileAsync(path, mXCodePaths.get(path), smInfo);
	}

	private IXCodeListener mListener = new IXCodeListener()
	{
		@Override
		public void onPiece(int piece)
		{
			if (showView)
			{
				progress += piece;
				if (progress > totalSize)
				{
					progress = totalSize;	//MakeSmFile or Encrypt can bigger
				}
				mProgressBar.setProgress((int) (progress >> 10));
				mPercentView.setText(getPercent(progress, totalSize));
				mSizeView.setText(getSize(progress, totalSize));
			}
		}

		@Override
		public void onFinished(final ReturnInfo info)
		{
			curNum++;
			if (mTitleView != null)
			{
				String title = mTitleView.getText().toString();
				Pattern pattern = Pattern.compile("[0-9]+");
				Matcher matcher = pattern.matcher(title);
				mTitleView.setText(matcher.replaceFirst(String.valueOf(curNum)));
			}
			/* Remember that the curNum's initial value is 1. */
			if (curNum > totalNum)
			{
				if (mDialog != null)
				{
					mDialog.dismiss();
				}
				if (mViewListener != null)
				{
					mViewListener.onFinished(info);
				}

				finishXCode(info.srcPath);
			}
			else
			{
				xcode(null);
			}
		}

		@Override
		public void onError(String srcPath, int errorCode)
		{
			if (mType.equals(XCodeType.Decrypt) || mType.equals(XCodeType.Encrypt))
			{
				curNum++;
				mXCodePaths.remove(srcPath);
				if (curNum <= totalNum)
				{
					/* if in code or decode mode , we go to next. */
					return;
				}
				finishXCode(srcPath);
			}

			if (mDialog != null)
			{
				mDialog.dismiss();
			}
			if (mViewListener != null)
			{
				mViewListener.onError(errorCode);
			}
		}
	};

	/**
	 * @param context
	 * @param srcPath
	 * @param codeType
	 * @return null 空间不足
	 */
	private String getDesXCodePath(Context context, String srcPath, XCodeType codeType)
	{
		String desPath = null;
		String fileName = FileUtil.getFileName(srcPath);
		final long needSize = new File(srcPath).length();
		switch (codeType)
		{
			case Decrypt:
				desPath = PathDao.getInstance(context).queryPlainPath(srcPath);
				if (Dirs.isSpaceNotEnough(desPath, needSize))
				{
					String boot = Dirs.getBootDir(desPath);
					// 换盘符
					if (boot.startsWith(Dirs.getDefaultBoot()))
					{
						desPath = desPath.replaceFirst(boot, Dirs.getExtraBoot());
					}
					else
					{
						desPath = desPath.replaceFirst(boot, Dirs.getDefaultBoot());
					}
				}
				break;

			case Encrypt:
				desPath = Dirs.getUserDir(context, srcPath) + File.separator + fileName;
				if (Dirs.isSpaceNotEnough(desPath, needSize))
				{
					// 换盘符
					if (Dirs.getBootDir(desPath).startsWith(Dirs.getDefaultBoot()))
					{
						desPath = Dirs.getUserDir(context, Dirs.getExtraBoot()) + File.separator
								+ fileName;
					}
					else
					{
						desPath = Dirs.getUserDir(context, Dirs.getDefaultBoot()) + File.separator
								+ fileName;
					}
				}
				break;

			case MakeFree:
				fileName += ".pbb";
				desPath = Dirs.getSendDir(context, srcPath) + File.separator + fileName;
				if (Dirs.isSpaceNotEnough(desPath, needSize))
				{
					// 换盘符
					if (Dirs.getBootDir(desPath).startsWith(Dirs.getDefaultBoot()))
					{
						desPath = Dirs.getSendDir(context, Dirs.getExtraBoot()) + File.separator
								+ fileName;
					}
					else
					{
						desPath = Dirs.getSendDir(context, Dirs.getDefaultBoot()) + File.separator
								+ fileName;
					}
				}
				break;

			case MakePay:
				fileName += ".pbb";
				desPath = Dirs.getSendPayDir(context, srcPath) + File.separator + fileName;
				if (Dirs.isSpaceNotEnough(desPath, needSize))
				{
					// 换盘符
					if (Dirs.getBootDir(desPath).startsWith(Dirs.getDefaultBoot()))
					{
						desPath = Dirs.getSendPayDir(context, Dirs.getExtraBoot()) + File.separator
								+ fileName;
					}
					else
					{
						desPath = Dirs.getSendPayDir(context, Dirs.getDefaultBoot())
								+ File.separator + fileName;
					}
				}
				break;

			default:
				break;
		}

		if (desPath != null)
		{
			if (Dirs.isSpaceNotEnough(desPath, needSize))
			{
				desPath = null;
			}
			else
			{
				desPath = FileUtil.needRename(desPath);
			}
		}

		return desPath;
	}

	private void finishXCode(final String srcPath)
	{
		if (mType.equals(XCodeType.Decrypt) || mType.equals(XCodeType.Encrypt))
		{
			/* code or decode */
			GlobalTask.executeBackground(new Runnable()
			{
				@Override
				public void run()
				{
					GlobalData.ensure(mContext, srcPath).xCode(mContext, mXCodePaths);
					Iterator<String> iterator = mXCodePaths.keySet().iterator();
					while(iterator.hasNext())
					{
						FileUtil.deleteFile(iterator.next());
					}
					if (mType.equals(XCodeType.Encrypt))
					{
						GlobalObserver.getGOb().postNotifyObservers(ObTag.Encrypt);
						GlobalData.ensure(mContext, srcPath).changeNewNum(mXCodePaths.size());
					}
					else
					{
						GlobalObserver.getGOb().postNotifyObservers(ObTag.Decrypt);
					}
				}
			});
		}
	}

	private void initTitle(XCodeType type)
	{
		String title = "";
		switch (type)
		{
			case Encrypt:
				title = "正在加密[1" + "/" + totalNum + "]";
				break;
			case Decrypt:
				title = "正在解密[1" + "/" + totalNum + "]";
				break;
			case MakeFree:
			case MakePay:
				title = "正在制作限时阅读文件...";
				break;

			default:
				break;
		}

		if (mTitleView != null)
		{
			mTitleView.setText(title);
		}
	}

	private static String getPercent(long cur, long total)
	{
		double percent = (double) cur / total;
		percent *= 100;
		return String.format("%.1f", percent) + "%";
	}

	private String getSize(long cur, long total)
	{
		return DataConvert.toSize(mContext, cur) + "/" + DataConvert.toSize(mContext, total);
	}

	public interface XCodeViewListener
	{
		void onFinished(ReturnInfo info);

		void onError(int error);
	}
}
