package cn.com.pyc.xcoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.com.pyc.bean.OfflineInfo;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.conn.OfflineManager;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.reader.PlayFile;
import cn.com.pyc.utils.Dirs;

import com.qlk.util.global.GlobalTask;
import com.qlk.util.tool.Util.IOUtil;
import com.qlk.util.tool._SysoXXX;
import com.sz.mobilesdk.common.SZApplication;

public class XCoder
{
	private static final String USER_DATA_KEY = "1111111111111111"; // 解密uid和PC自动登录文件用

	private byte[] uid;
	private byte[] oldKey; // 对应PYC1，前期是用它加密的
	private byte[] newKey; // 对应PYC2，后期加密用它
	private Context context;

	private static final Object mLock = new Object();

	static
	{
		System.loadLibrary("pyc_safe");
	}

	public XCoder(Context context)
	{
		this.context = context;
		uid = UserDao.getDB(context).getUserInfo().getUid();
		oldKey = uidToKey(uid, 1);
		newKey = uidToKey(uid, 0);
	}

	public PlayFile getPlayFileInfo(String path)
	{
		FlagData flagData = new FlagData();
		XCoderResult xr = flagData.analysisFlagFromFile(path, uid);	// 检查文件的合法性
		if (xr.succeed())
		{
			return new PlayFile(path, flagData.isOldKey() ? oldKey : newKey, flagData.getCodeLen());
		}
		else
		{
			return new PlayFile(path);
		}
	}

	// 解密自动登录文件
	public static String decryptTempFile(Context context, String path)
	{
		File file = new File(path);
		String fileName = file.getName();
		final int fileLen = (int) file.length();
		String cachePath = context.getFilesDir().getAbsolutePath() + File.separator + fileName;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try
		{
			fis = new FileInputStream(file);
			fos = new FileOutputStream(cachePath);
			setEncryptKey(USER_DATA_KEY.getBytes(), 1);
			byte[] src = new byte[fileLen];
			fis.read(src);
			decodeBuffer(src, src, fileLen);
			fos.write(src);
			fos.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			cachePath = null;
		}
		finally
		{
			IOUtil.close(fis, fos);
			file.delete();
		}

		return cachePath;
	}

	// 与服务器传输协议加解密算法
	public static byte[] encrypt(byte[] buf)
	{
		setEncryptKey(USER_DATA_KEY.getBytes(), 1);
		int length = buf.length - 4;
		byte[] srcData = new byte[length];
		for (int i = 0; i < length; i++)
		{
			srcData[i] = buf[i + 4];
		}
		codeBuffer(srcData, srcData, length);

		for (int i = 0; i < length; i++)
		{
			buf[i + 4] = srcData[i];
		}

		return buf;
	}

	// 与服务器传输协议加解密算法
	public static byte[] decrypt(byte[] buf)
	{
		setEncryptKey(USER_DATA_KEY.getBytes(), 1);
		int length = buf.length - 4;
		byte[] srcData = new byte[length];
		for (int i = 0; i < length; i++)
		{
			srcData[i] = buf[i + 4];
		}
		decodeBuffer(srcData, srcData, length);

		for (int i = 0; i < length; i++)
		{
			buf[i + 4] = srcData[i];
		}

		return buf;
	}

	// 网页的加解密算法
	public static String getHttpEncryptText(String text)
	{
		byte[] src = text.getBytes();
		byte[] des = new byte[1000];
		getHttpEncryptText(src, des, src.length);
		return new String(des).trim();
	}
	
	// xiong
	public static String getHttpDecryptText(String text)
		{
			byte[] src = text.getBytes();
			byte[] des = new byte[1000];
			getHttpDecryptText(src, des, src.length);
			return new String(des).trim();
		}

	private static native void uidToEncryptKeyNet(byte[] uid, byte[] key, int oldKey);

	public static native void decodeBuffer(byte[] data, byte[] des, int length);

	public static native void codeBuffer(byte[] data, byte[] des, int length);

	public static native void setEncryptKey(byte[] srcData, int key);

	private static native void getHttpEncryptText(byte[] srcData, byte[] desData, int length);
	
	private static native void getHttpDecryptText(byte[] srcData, byte[] desData, int length);

	// **********************************************************
	// * 外发外发外发外发外发外发外发外发外发外发外发外发外发外发
	// * ********************************************************

	/**
	 * 返回时已是最新的信息（新增codeLen），包括离线结构的
	 * sessionKey,fid,filePath,fileVersion(1则是三个结构）
	 * 
	 * @param smPath
	 * @return
	 */
	public static XCoderResult analysisSmFile(String smPath)
	{
		SmHeadExtraData headExtraData = new SmHeadExtraData();
		XCoderResult xr = headExtraData.analysisFlagFromFile(smPath);
		if (xr.succeed())
		{
			SmInfo smInfo = xr.getSmInfo();
			// info里面有：sk，fid，filePath，fileVersion
			if (smInfo.getFileVersion() == 1)
			{
				xr = OfflineManager.analysisFlagFromFile(smInfo);
			}
			try
			{
				smInfo.setHash(getSmHashValue(smPath, smInfo.getFileVersion() == 1));
			}
			catch (IOException e)
			{
				e.printStackTrace();
				xr.setSuc(XCoderResult.FILE_ERROR);
			}

			SmHeadData headData = new SmHeadData();
			XCoderResult xr2 = headData.analysisFlagFromFile(smPath);
			if (xr2.succeed())
			{
				smInfo.setCodeLen(headData.getCodeLen());
				_SysoXXX.message("1文件长度1：" + headData.getFileLen() + " 头的offset："
						+ headData.getSelfOffset());
				smInfo.setOffset(headData.getSelfOffset() - format16(headData.getFileLen()));
				smInfo.setFileLen(headData.getFileLen());
			}
			else
			{
				// 去codeLen最多尝试两次，若还不成功，则不再取了（在打开文件时可能会出问题）
				xr2 = headData.analysisFlagFromFile(smPath);
				if (xr2.succeed())
				{
					smInfo.setCodeLen(headData.getCodeLen());
					smInfo.setOffset(headData.getSelfOffset() - format16(headData.getFileLen()));
					smInfo.setFileLen(headData.getFileLen());
				}
			}
			xr.setSmInfo(smInfo);		// 其实，此时的xr中已是最新的SmInfo了，可以不用重新设置
		}

		return xr;
	}

	/**
	 * 将密文图片转成byte[]
	 * 
	 * @param path
	 * @param key
	 * @return null 解密失败
	 */
	public byte[] readCipherImage(String path)
	{
		FlagData flagData = new FlagData();
		XCoderResult xr = flagData.analysisFlagFromFile(path, uid);
		byte[] data = null;
		if (xr.succeed())
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(path);
				long codeLen = format16(flagData.getCodeLen());
				data = new byte[(int) format16(flagData.getFileLen())];
				fis.read(data);
				setEncryptKey(flagData.isOldKey() ? oldKey : newKey, 1);
				decodeBuffer(data, data, (int) codeLen);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				data = null;
			}
			finally
			{
				IOUtil.close(fis, null);
			}
		}

		return data;
	}

	/**
	 * 将外发图片解密成二进制数据
	 * 
	 * @param path
	 * @param key
	 * @return null 解密失败
	 */
	public static byte[] readSmImage(String path, byte[] key)
	{
		SmHeadData headData = new SmHeadData();
		XCoderResult xr = headData.analysisFlagFromFile(path);
		byte[] data = null;
		if (xr.succeed())
		{
			FileInputStream fis = null;
			int codeLen = (int) headData.getCodeLen();
			try
			{
				fis = new FileInputStream(path);
				codeLen = (int) format16(codeLen);
				long start = headData.getSelfOffset() - format16(headData.getFileLen());
				data = new byte[(int) format16(headData.getFileLen())];
				fis.skip(start);
				fis.read(data);
				setEncryptKey(wrapEncodeKey(key), 1);
				decodeBuffer(data, data, codeLen);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				data = null;
			}
			finally
			{
				IOUtil.close(fis, null);
			}
		}
		return data;
	}

	public static byte[] wrapEncodeKey(byte[] encodekey)
	{
		byte[] wrapKey = new byte[16];
		for (int i = 0; i < 16; i++)
		{
			wrapKey[i] = (byte) (encodekey[i] - '0' + 'A');
		}
		return wrapKey;
	}

	/**
	 * @param ss
	 * @param data
	 *            文件尾部数据
	 * @param len
	 * @param data2
	 *            哈希值存储器
	 */
	private static native void SM3_XXX(SmFileStruct ss, byte[] data, int len, byte[] data2);

	static final class SmFileStruct
	{
		final int[] stateIV = new int[8];
		final int[] count = new int[2];
		final int[] T = new int[64];
		final char[] buffer = new char[64];
	}

	/*-*************************************************************
	 *  TODO The third arithmetic.
	 ************************************************************/
	private static final int BIG_FILE_LEN = 20 << 20;	// Each thread handle 20 MB data.

	private long timeTest;
	private IXCodeListener mListener;

	public void setXcodeListener(IXCodeListener xcodeListener)
	{
		mListener = xcodeListener;
	}

	/**
	 * Include: DecryptFile EncryptFile MakeSmFile. <br>
	 * It will analysis the "desPath" to ensure which type you want.
	 * <p>
	 * Only for one file.
	 * 
	 * @param srcPath
	 *            May be a cipher file
	 * @param smPath
	 * @param smInfo
	 *            If MakeSmFile, it must be not empty.
	 */
	public void xcodeFileAsync(String srcPath, String desPath, SmInfo smInfo)
	{
		new FileXcode(srcPath, desPath, smInfo).startXcodeAsync();
	}

	/**
	 * Operate one file every time.
	 * 
	 * @author QiLiKing 2015-8-20 下午4:52:34
	 */
	public class FileXcode
	{
		private static final int DATA_LEN = 2 << 20;

		private static final int MSG_PROGRESS = 0x1;
		private static final int MSG_FINISHED = 0x2;
		private static final int MSG_ERROR = 0x3;

		private final String mSrcPath;
		private final String mDesPath;
		private final SmInfo mSmInfo;

		private byte[] mDecryptUidKey;
		private byte[] mEncryptUidKey;
		private byte[] mSmKey;
		/** the source file length */
		private long fileLen;
		/** a format16 length */
		private long decryptLen;
		/** a format16 length */
		private long encryptLen;
		private boolean isSrcFileCipher;	//the srcPath is cipher file or not. It is opposite to "isEncrypt".
		private boolean isFromSm;	//want to make smFile, if it is true, "isEncrypt" is ignored.
		private boolean useLock;
		private boolean isImage; //判断是否是图片文件，制作图片不需要在结构中加2M

		public FileXcode(String srcPath, String desPath, SmInfo smInfo)
		{
			mSrcPath = srcPath;
			mDesPath = desPath;
			mSmInfo = smInfo;
		}

		public void startXcodeAsync()
		{
			timeTest = System.currentTimeMillis();
			analysis();
			if (isFromSm)
			{
				//如果是图片，制作时候调用不加2M的制作方法
				if(isImage){
					new SingleXCodeFile().startXcodeAsync();
				}else {
					//制作外发文件-新算法就只使用这一个
 					new SingleXCodeFileV2().startXcodeAsync();
				}

			}
			else
			{
				if (fileLen > BIG_FILE_LEN && !useLock)
				{
					if (Dirs.isOnInternalDisk(mDesPath))
					{
						new FastXCode().startXcodeAsync();
					}
					else
					{
						new NormalXCodeFile().startXcodeAsync();
					}
				}
				else
				{
 					new SingleXCodeFile().startXcodeAsync();
				}
			}
		}

		private void analysis()
		{
			isImage = mSrcPath.endsWith(".jpg") || mSrcPath.endsWith(".jpeg") || mSrcPath.endsWith(".png");
			isFromSm = mDesPath.endsWith(".pbb");
			if (isFromSm)
			{
				mSmKey = wrapEncodeKey(mSmInfo.getEncodeKey());
			}
			final FlagData flagData = new FlagData();
			isSrcFileCipher = flagData.isPYCFile(mSrcPath);
			useLock = isFromSm && isSrcFileCipher;
			fileLen = new File(mSrcPath).length();
			encryptLen = format16(fileLen);
			mEncryptUidKey = newKey;
			if (isSrcFileCipher)
			{
				XCoderResult xr = flagData.analysisFlagFromFile(mSrcPath, uid);	// 检查文件的合法性
				if (xr.succeed())
				{
					fileLen = flagData.getFileLen();
					encryptLen = format16(fileLen);
					decryptLen = format16(flagData.getCodeLen());	//May be 4KB、10MB or bigger.
					mDecryptUidKey = flagData.isOldKey() ? oldKey : newKey;
				}
			}
		}

		private void xcodeData(byte[] data, long cur, int read, boolean useLock)
		{
			if (isSrcFileCipher)
			{
				//decode
				int decodeLen = caculateXcodeLen(cur, decryptLen, read);
				if (decodeLen > 0)
				{
					decryptData(data, decodeLen, mDecryptUidKey, useLock);
				}
			}
			/* code */
			boolean isEncrypt = isFromSm || !isSrcFileCipher;
			if (isEncrypt)
			{
				int codeLen = caculateXcodeLen(cur, encryptLen, read);
				if (codeLen > 0)
				{
					byte[] key = isFromSm ? mSmKey : mEncryptUidKey;
					encryptData(data, codeLen, key, useLock);
				}
			}
		}

		/**
		 * @param cur
		 *            The stream's current position
		 * @param maxLen
		 *            The file's code length or decode length
		 * @param validLen
		 *            valid read length
		 * @return
		 */
		private int caculateXcodeLen(long cur, long maxLen, int validLen)
		{
			int len = 0;

			if (cur + DATA_LEN <= maxLen)
			{
				len = (int) format16(validLen);	//all data needs to code or decode
			}
			else if (maxLen > cur)	//maxLen is between cur and "cur+DATA_LEN"
			{
				len = (int) format16(maxLen - cur);	//part data needs to code or decode
			}
			else
			{
				len = 0;		//no data needs to code or decode
			}

			return len;
		}

		private int getWriteLen(long cur, int read)
		{
			int writeLen = read;
			boolean isEncrypt = isFromSm || !isSrcFileCipher;
			if (isEncrypt)
			{
				if (cur + DATA_LEN > encryptLen)	//The end of file
				{
					writeLen = (int) (encryptLen - cur);
				}
			}
			else
			{
				if (cur + DATA_LEN > fileLen)
				{
					writeLen = (int) (fileLen - cur);		//write the real file length
				}
			}
			return writeLen;
		}

		private long getMaxSize()
		{
			long max = Math.max(fileLen, encryptLen);	//srcFile is plain
			max = Math.max(max, decryptLen);	//srcFile is cipher, In fact, encryptLen is always the biggest.
			return max;
		}

		class XCodeFileThread extends Thread
		{
			protected boolean canFinish = false;
			protected boolean forceClose = false;

			public void canFinish()
			{
				canFinish = true;
			}

			public void forceClose()
			{
				forceClose = true;
			}
		}

		/**
		 * TODO The destination path is on internal disk.
		 * SVMSUNG note2---700M---35s
		 * MOTO X---5.7G---171s
		 * 
		 * @author QiLiKing 2015-8-21 上午9:43:27
		 */
		class FastXCode
		{
			private static final int MAX_THREAD_SIZE = 10;
			private int threadNums;
			private ArrayList<FastXcodeThread> mThreads = new ArrayList<>();

			public void startXcodeAsync()
			{
				_SysoXXX.message("--------------FAST XCODE----------------");
				fixFileLen(mDesPath, fileLen);
				long already = 0;
				int tag = 0;
				int maxPeer = (int) (fileLen / MAX_THREAD_SIZE);
				maxPeer -= maxPeer % DATA_LEN;
				_SysoXXX.message("maxPeer:" + maxPeer);
				final int peer = maxPeer > BIG_FILE_LEN ? maxPeer : BIG_FILE_LEN;
				_SysoXXX.message("peer:" + peer);
				final long max = getMaxSize();
				while(already < max)
				{
					long remain = max - already;
					int size = (int) (remain < peer ? remain : peer);
					FastXcodeThread thread = new FastXcodeThread(++tag, already, size);
					mThreads.add(thread);
					thread.start();
					already += size;
				}
				threadNums = tag;	//May be bigger than MAX_THREAD_SIZE because we cut "maxPeer".
			}

			private void fixFileLen(String path, long len)
			{
				RandomAccessFile raf = null;
				try
				{
					raf = new RandomAccessFile(mDesPath, "rw");
					raf.setLength(fileLen);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					IOUtil.close(raf);
				}
			}

			private final Handler mFastHandler = new Handler(Looper.getMainLooper())
			{

				@Override
				public void handleMessage(Message msg)
				{
					super.handleMessage(msg);
					switch (msg.what)
					{
						case MSG_PROGRESS:
							if (mListener != null)
							{
								mListener.onPiece(msg.arg1);
							}
							break;

						case MSG_FINISHED:
							_SysoXXX.message("线程 " + msg.arg1 + " finish");
							threadNums--;
							if (threadNums == 0)
							{
								afterXcode();
								if (mListener != null)
								{
									mListener.onFinished(ReturnInfo.putInfo(mSrcPath, mDesPath));
								}
								_SysoXXX.message("fast xcode算法耗时："
										+ (System.currentTimeMillis() - timeTest));
							}
							break;

						case MSG_ERROR:
							for (FastXcodeThread thread : mThreads)
							{
								thread.forceClose();
							}
							if (mListener != null)
							{
								mListener.onError(mSrcPath, msg.arg1);
							}
							break;

						default:
							break;
					}
				}

			};

			class FastXcodeThread extends XCodeFileThread
			{
				private final int tag;
				private final long start;
				private final int size;

				public FastXcodeThread(int tag, long start, int size)
				{
					this.tag = tag;
					this.start = start;
					this.size = size;
				}

				@Override
				public void run()
				{
					RandomAccessFile srcStream = null;
					RandomAccessFile desStream = null;
					try
					{
						srcStream = new RandomAccessFile(mSrcPath, "r");
						desStream = new RandomAccessFile(mDesPath, "rw");
						srcStream.seek(start);
						desStream.seek(start);
						byte[] data = new byte[DATA_LEN];
						long cur = start;
						final long max = start + size;
						while(!forceClose && cur < max)
						{
							int read = srcStream.read(data);
							if (read == -1)
							{
								break;
							}
							xcodeData(data, cur, read, useLock);
							int writeLen = getWriteLen(cur, read);
							desStream.write(data, 0, writeLen);

							cur += read;

							sendMsg(mFastHandler, MSG_PROGRESS, read);
						}

						if (!forceClose)
						{
							//exit normally
							sendMsg(mFastHandler, MSG_FINISHED, tag);
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();

						sendMsg(mFastHandler, MSG_ERROR, XCoderResult.FILE_ERROR);
					}
					finally
					{
						IOUtil.close(srcStream, desStream);
					}
				}
			}

		}	//End of FastXCodeFile class

		/**
		 * 新算法
		 * 
		 * @author QiLiKing 2015-8-21 下午2:53:38
		 */
		class SingleXCodeFileV2
		{

			public void startXcodeAsync()
			{
				_SysoXXX.message("--------------SINGLE XCODE----------------");
				GlobalTask.executeBackground(new SingleTask());
			}

			private final Handler mSingleHandler = new Handler(Looper.getMainLooper())
			{

				@Override
				public void handleMessage(Message msg)
				{
					super.handleMessage(msg);
					switch (msg.what)
					{
						case MSG_PROGRESS:
							if (mListener != null)
							{
								mListener.onPiece(msg.arg1);
							}
							break;

						case MSG_FINISHED:
							afterXcode();
							if (mListener != null)
							{
								mListener.onFinished(ReturnInfo.putInfo(mSrcPath, mDesPath));
							}
							break;

						case MSG_ERROR:
							if (mListener != null)
							{
								mListener.onError(mSrcPath, msg.arg1);
							}
							break;

						default:
							break;
					}
				}

			};

			class SingleTask implements Runnable
			{

				@Override
				public void run()
				{
					FileInputStream fis = null;
					FileOutputStream fos = null;
					try
					{
						fis = new FileInputStream(mSrcPath);
						fos = new FileOutputStream(mDesPath);
						//先写入字节
						Random random = new Random(System.currentTimeMillis());
						byte[] randomData = new byte[(2 << 20) + (random.nextInt(15) + 1)];	//2M ＋ 1~15个字节
						_SysoXXX.message("加密随机长度：" + randomData.length);
						random.nextBytes(randomData);
						fos.write(randomData);

						byte[] data = new byte[DATA_LEN];
						long cur = 0;
						final long max = getMaxSize();
						long time = System.currentTimeMillis();
						while(cur < max)
						{
							int read = fis.read(data);
							if (read == -1)
							{
								break;
							}
							xcodeData(data, cur, read, false);	//Single thread, not use lock.
							int writeLen = getWriteLen(cur, read);
							fos.write(data, 0, writeLen);

							cur += read;

							sendMsg(mSingleHandler, MSG_PROGRESS, read);
						}
						_SysoXXX.message("single 算法耗时：" + (System.currentTimeMillis() - time));

						//exit normally
						sendMsg(mSingleHandler, MSG_FINISHED, 0);
					}
					catch (IOException e)
					{
						e.printStackTrace();

						sendMsg(mSingleHandler, MSG_ERROR, XCoderResult.FILE_ERROR);
					}
					finally
					{
						IOUtil.close(fis, fos);
					}
				}
			}
		}

		/**
		 * TODO XCode in single thread.
		 * * SVMSUNG note2---700M---252s
		 * 
		 * @author QiLiKing 2015-8-21 下午2:53:38
		 */
		class SingleXCodeFile
		{

			public void startXcodeAsync()
			{
				_SysoXXX.message("--------------SINGLE XCODE----------------");
				GlobalTask.executeBackground(new SingleTask());
			}

			private final Handler mSingleHandler = new Handler(Looper.getMainLooper())
			{

				@Override
				public void handleMessage(Message msg)
				{
					super.handleMessage(msg);
					switch (msg.what)
					{
						case MSG_PROGRESS:
							if (mListener != null)
							{
								mListener.onPiece(msg.arg1);
							}
							break;

						case MSG_FINISHED:
							afterXcode();
							if (mListener != null)
							{
								mListener.onFinished(ReturnInfo.putInfo(mSrcPath, mDesPath));
							}
							break;

						case MSG_ERROR:
							if (mListener != null)
							{
								mListener.onError(mSrcPath, msg.arg1);
							}
							break;

						default:
							break;
					}
				}

			};

			class SingleTask implements Runnable
			{

				@Override
				public void run()
				{
					FileInputStream fis = null;
					FileOutputStream fos = null;
					try
					{
						fis = new FileInputStream(mSrcPath);
						fos = new FileOutputStream(mDesPath);
						byte[] data = new byte[DATA_LEN];
						long cur = 0;
						final long max = getMaxSize();
						long time = System.currentTimeMillis();
						while(cur < max)
						{
							int read = fis.read(data);
							if (read == -1)
							{
								break;
							}
							xcodeData(data, cur, read, false);	//Single thread, not use lock.
							int writeLen = getWriteLen(cur, read);
							fos.write(data, 0, writeLen);

							cur += read;

							sendMsg(mSingleHandler, MSG_PROGRESS, read);
						}
						_SysoXXX.message("single 算法耗时：" + (System.currentTimeMillis() - time));

						//exit normally
						sendMsg(mSingleHandler, MSG_FINISHED, 0);
					}
					catch (IOException e)
					{
						e.printStackTrace();

						sendMsg(mSingleHandler, MSG_ERROR, XCoderResult.FILE_ERROR);
					}
					finally
					{
						IOUtil.close(fis, fos);
					}
				}
			}
		}

		/**
		 * TODO The destination path is on other disk.
		 * SVMSUNG note2---700M---137s---255(separate read and write operation)
		 * 
		 * @author QiLiKing 2015-8-21 上午9:49:17
		 */
		class NormalXCodeFile
		{
			private static final int MSG_READ_FINISH = 0x11;
			private static final int MSG_XCODE_FINISH = 0x12;
			private static final int MSG_WRITE_FINISH = 0x13;

			private static final int QUEUE_SIZE = 5;
			private final ArrayBlockingQueue<byte[]> mReadQueue = new ArrayBlockingQueue<>(
					QUEUE_SIZE);
			private final ArrayBlockingQueue<byte[]> mWriteQueue = new ArrayBlockingQueue<>(
					QUEUE_SIZE);
			private final Stack<byte[]> mCacheData = new Stack<>();	//It's item's size must be "DATA_SIZE".

			private ReadWriteThread mReadWriteThread;
			private XCodeThread mXCodeThread;

			public void startXcodeAsync()
			{
				_SysoXXX.message("---------NORMAL XCODE-------------");
				timeTest = System.currentTimeMillis();
				mReadWriteThread = new ReadWriteThread();
				mXCodeThread = new XCodeThread();
				mReadWriteThread.start();
				mXCodeThread.start();
			}

			class ReadWriteThread extends XCodeFileThread
			{

				@Override
				public void run()
				{
					FileInputStream fis = null;
					FileOutputStream fos = null;
					final Handler handler = mNormalHandler;
					try
					{
						fis = new FileInputStream(mSrcPath);
						fos = new FileOutputStream(mDesPath);
						final ArrayBlockingQueue<byte[]> readQueue = mReadQueue;
						final ArrayBlockingQueue<byte[]> writeQueue = mWriteQueue;
						final Stack<byte[]> cacheData = mCacheData;
						long remain = fileLen;
						boolean readFinish = false;
						while(!forceClose)
						{
							/* read */
							if (!readFinish && readQueue.size() < QUEUE_SIZE)
							{
								if (remain > 0)
								{
									byte[] data = getCacheData(cacheData, remain);
									fis.read(data);
									remain -= data.length;
									if (!readQueue.offer(data))
									{
										sendMsg(handler, MSG_ERROR, XCoderResult.FILE_ERROR);
										break;
									}
								}
								else
								{
									readFinish = true;
									sendMsg(handler, MSG_READ_FINISH, 0);
								}
							}

							/* write */
							byte[] data = writeQueue.poll();
							if (data != null)
							{
								int len = data.length;
								if (!isFromSm && isSrcFileCipher && len != DATA_LEN)
								{
									//decode and reach to the last part of data
									len = (int) (fileLen % DATA_LEN);
								}
								fos.write(data, 0, len);
								fos.flush();
								cacheData(cacheData, data);
								sendMsg(handler, MSG_PROGRESS, data.length);
							}
							else
							{
								if (canFinish)
								{
									sendMsg(handler, MSG_WRITE_FINISH, 0);
									break;
								}
							}

							/* wait */
							if (!readFinish && readQueue.size() == QUEUE_SIZE
									&& writeQueue.isEmpty())
							{
								try
								{
									Thread.sleep(200);
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
						sendMsg(handler, MSG_ERROR, XCoderResult.FILE_ERROR);
					}
					finally
					{
						IOUtil.close(fis, fos);
					}
				}
			}

			class XCodeThread extends XCodeFileThread
			{
				@Override
				public void run()
				{
					long cur = 0;
					ArrayBlockingQueue<byte[]> readQueue = mReadQueue;
					ArrayBlockingQueue<byte[]> writeQueue = mWriteQueue;
					while(!forceClose)
					{
						try
						{
							byte[] data = readQueue.poll(1, TimeUnit.SECONDS);
							if (data != null)
							{
								xcodeData(data, cur, data.length, useLock);
								cur += data.length;
								while(true)
								{
									try
									{
										if (writeQueue.offer(data, 1, TimeUnit.SECONDS))
										{
											break;
										}
									}
									catch (InterruptedException e)
									{
										e.printStackTrace();
										sendMsg(mNormalHandler, MSG_ERROR, XCoderResult.FILE_ERROR);
										break;
									}
								}
							}
							else
							{
								if (canFinish)
								{
									sendMsg(mNormalHandler, MSG_XCODE_FINISH, 0);
									break;
								}
							}
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
							sendMsg(mNormalHandler, MSG_ERROR, XCoderResult.FILE_ERROR);
							break;
						}
					}
				}
			}

			private final Handler mNormalHandler = new Handler(Looper.getMainLooper())
			{

				@Override
				public void handleMessage(Message msg)
				{
					super.handleMessage(msg);
					switch (msg.what)
					{
						case MSG_PROGRESS:
							if (mListener != null)
							{
								mListener.onPiece(msg.arg1);
							}
							break;

						case MSG_FINISHED:

							break;

						case MSG_ERROR:
							mReadWriteThread.forceClose();
							mXCodeThread.forceClose();
							if (mListener != null)
							{
								mListener.onError(mSrcPath, msg.arg1);
							}
							break;

						case MSG_READ_FINISH:
							mXCodeThread.canFinish();
							break;
						case MSG_XCODE_FINISH:
							mReadWriteThread.canFinish();
							break;

						case MSG_WRITE_FINISH:
							afterXcode();
							_SysoXXX.message(
									"normal xcode 耗时：" + (System.currentTimeMillis() - timeTest));
							if (mListener != null)
							{
								mListener.onFinished(ReturnInfo.putInfo(mSrcPath, mDesPath));
							}
							break;
						default:
							break;
					}
				}
			};

			/**
			 * If has no cache data, will create it.
			 * 
			 * @param cacheData
			 * @param maxSize
			 * @return a format16 data, length<=DATA_LEN
			 */
			private byte[] getCacheData(Stack<byte[]> cacheData, long maxSize)
			{
				byte[] data = null;
				if (!cacheData.isEmpty())
				{
					data = cacheData.pop();
				}
				if (data == null)
				{
					data = new byte[maxSize > DATA_LEN ? DATA_LEN : (int) format16(maxSize)];
				}
				if (data.length > maxSize)
				{
					data = new byte[(int) maxSize];
				}

				return data;
			}

			private void cacheData(Stack<byte[]> cacheData, byte[] data)
			{
				if (data.length == DATA_LEN)
				{
					if (cacheData.size() < QUEUE_SIZE)
					{
						cacheData.push(data);
					}
				}
			}

		}	//End of NormalXCodeFile class

		private void sendMsg(Handler handler, int what, int arg1)
		{
			Message msg = Message.obtain();
			msg.what = what;
			msg.arg1 = arg1;
			handler.sendMessage(msg);
		}

		private void afterXcode()
		{
			if (!isFromSm && isSrcFileCipher)
			{
				return;
			}
			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream(mDesPath, true);
				if (isFromSm)
				{
//					fos.write(new SmHeadData().writeBuffer(fileLen, -1)); // 添加头结构
					//制作图片用PYC0结构  制作其他用PYC1结构
					if(isImage){
						fos.write(new SmHeadData().writeBuffer0(fileLen, format16(fileLen))); // 添加头结构
					}else{
						fos.write(new SmHeadData().writeBuffer1(fileLen, format16(fileLen))); // 添加头结构
					}

					fos.write(OfflineManager.packageBuffer(new OfflineInfo(),
							mSmInfo.getSessionKey())); // 添加离线结构（目前为空）
					fos.write(new SmHeadExtraData().writeBuffer(mSmInfo.getFid(),
							mSmInfo.getSessionKey())); // 添加头扩展结构
					mSmInfo.setHash(XCoder.getSmHashValue(mDesPath, true)); // 默认3个结构
				}
				else
				{
//					fos.write(new FlagData().writeBuffer(fileLen, -1, uid));
					fos.write(new FlagData().writeBuffer(fileLen, format16(fileLen), uid));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				IOUtil.close(null, fos);
			}
		}

	}

	/**
	 * Our arithmetics don't allow two keys at the same time. So we use lock to
	 * avoid it.
	 */
	private static void encryptData(byte[] data, int len, byte[] key, boolean useLock)
	{
		if (useLock)
		{
			synchronized (mLock)
			{
				setEncryptKey(key, 1);
				codeBuffer(data, data, len);
			}
		}
		else
		{
			setEncryptKey(key, 1);
			codeBuffer(data, data, len);
		}
	}

	private static void decryptData(byte[] data, int len, byte[] key, boolean useLock)
	{
		if (useLock)
		{
			synchronized (mLock)
			{
				decode(data, len, key);
			}
		}
		else
		{
			decode(data, len, key);
		}
	}

	/**
	 * Some file's key may be changed when decoding, so verify it.
	 * 
	 * @param data
	 * @param len
	 */
	private static void decode(byte[] data, int len, byte[] key)
	{
		if (len <= 0)
		{
			return;
		}
		while(true)
		{
			byte[] b = new byte[data.length];
			System.arraycopy(data, 0, b, 0, b.length);
			setEncryptKey(key, 1);
			decodeBuffer(b, b, len);

			byte[] c = new byte[data.length];
			System.arraycopy(b, 0, c, 0, c.length);
			setEncryptKey(key, 1);
			codeBuffer(c, c, len);

			if (Arrays.equals(data, c))
			{
				System.arraycopy(b, 0, data, 0, b.length);
				break;
			}
		}
	}

	// 计算文件的hash值
	private static byte[] getSmHashValue(String path, boolean isThreeStruct) throws IOException
	{
		SmFileStruct struct = new SmFileStruct();
		File file = new File(path);
		byte[] hash = new byte[32];
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(file);
			final int size = isThreeStruct ? SmHeadExtraData.FLAG_LEN : 1024 * 1024 * 2; // 离线结构取hash方式不同
			final long fileLen = file.length();
			if (fileLen >= size)
			{
				fis.skip(fileLen - size);
			}
			byte[] b = new byte[size];
			int real = fis.read(b);
			SM3_XXX(struct, b, real, hash);
		}
		finally
		{
			IOUtil.close(fis, null);
		}

		return hash;
	}

	/**
	 * @param nowLen
	 * @return a format16 number
	 */
	static long format16(long nowLen)
	{
		int less = (int) (nowLen % 16);
		return less > 0 ? (nowLen + 16 - less) : nowLen;
	}

	private static byte[] uidToKey(byte[] uid, int oldKey)
	{
		byte[] key = new byte[16];
		uidToEncryptKeyNet(uid, key, oldKey);
		return key;
	}

	/**
	 * It's methods are running in UI thread.
	 * 
	 * @author QiLiKing 2015-8-20 下午4:03:50
	 */
	public interface IXCodeListener
	{
		/**
		 * a piece of completion
		 * 
		 * @param piece
		 */
		void onPiece(int piece);

		/**
		 * one of "desPath" and "desBuffer" is null.
		 * 
		 * @param srcPath
		 * @param desPath
		 * @param desBuffer
		 */
		void onFinished(ReturnInfo info);

		void onError(String srcPath, int errorCode);
	}

	public static class ReturnInfo
	{
		public String srcPath;
		public String desPath;
		public byte[] desBuffer;

		public static ReturnInfo putInfo(String srcPath, String desPath)
		{
			ReturnInfo info = new ReturnInfo();
			info.srcPath = srcPath;
			info.desPath = desPath;
			return info;
		}

		public static ReturnInfo putInfo(String srcPath, byte[] desBuffer)
		{
			ReturnInfo info = new ReturnInfo();
			info.srcPath = srcPath;
			info.desBuffer = desBuffer;
			return info;
		}
	}

	// 与服务器传输协议加解密算法
	public static byte[] encryptV2(byte[] buf, int offset)
	{
		setEncryptKey(USER_DATA_KEY.getBytes(), 1);
		int length = buf.length - offset;
		byte[] srcData = new byte[length];
		for (int i = 0; i < length; i++)
		{
			srcData[i] = buf[i + offset];
		}
		codeBuffer(srcData, srcData, length);

		for (int i = 0; i < length; i++)
		{
			buf[i + offset] = srcData[i];
		}

		return buf;
	}

	// 与服务器传输协议加解密算法
	public static byte[] decryptV2(byte[] buf, int offset)
	{
		setEncryptKey(USER_DATA_KEY.getBytes(), 1);
		int length = buf.length - offset;
		byte[] srcData = new byte[length];
		for (int i = 0; i < length; i++)
		{
			srcData[i] = buf[i + offset];
		}
		decodeBuffer(srcData, srcData, length);

		for (int i = 0; i < length; i++)
		{
			buf[i + offset] = srcData[i];
		}

		return buf;
	}

}
