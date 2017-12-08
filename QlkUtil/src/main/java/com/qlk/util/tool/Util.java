package com.qlk.util.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.Xml;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;

public class Util
{
	/**
	 * @param context
	 * @param intent
	 * @param selfKey
	 *            自定义存储路径的key值
	 * @return
	 */
	public static String getPathFromIntent(Context context, Intent intent,
			String selfKey)
	{
		if (intent == null) { return null; }
		Uri uri = intent.getData();
		if (uri == null)
		{
			uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
		}
		String path = null;
		if (uri != null)
		{
			String scheme = uri.getScheme();
			if (scheme.equals("file"))
			{
				path = uri.getPath();
			} else if (scheme.equals("content"))
			{
				Cursor c = context.getContentResolver().query(uri,
						new String[] { MediaColumns.DATA }, null, null, null);
				if (c != null && c.moveToFirst())
				{
					path = c.getString(0);
				}
			}
		} else
		{
			path = intent.getStringExtra(selfKey);
		}

		return path;
	}

	public static class NetUtil
	{
		public static boolean isNetInUse(Context ctx)
		{
			ConnectivityManager cm = (ConnectivityManager) ctx
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			return info != null && info.isConnected();
		}

		/**
		 * @param defaultValue
		 *            If not success, will return the defaultValue.
		 */
		public static String connectHttpNet(Context context, String url)
		{
			if (isNetInUse(context))
			{
				try
				{
					HttpGet httpGet = new HttpGet(url);
					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(httpGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200) { return EntityUtils
							.toString(httpResponse.getEntity()); }
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			return null;
		}
	}

	// TODO CipherUtil
	public static class CipherUtil
	{
		private static final String CIPHER_KEY = "QWERTYUIOPASDFGHJKLZXCVBNM0987654321";

		public static byte[] encrypt(byte[] srcData)
		{
			try
			{
				return createCipher(true).doFinal(srcData);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			return new byte[1];
		}

		public static byte[] decrypt(byte[] srcData)
		{
			try
			{
				return createCipher(false).doFinal(srcData);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			return new byte[1];
		}

		public static Cipher createCipher(boolean isEncrypt) throws Exception
		{
			DESKeySpec desKey = new DESKeySpec(CIPHER_KEY.getBytes("UTF-8"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(desKey);
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,
					secretKey);
			return cipher;
		}
	}

	// TODO FormatUtil
	public static class FormatUtil
	{
		public static boolean isEmailFormat(String email)
		{
			return !TextUtils.isEmpty(email)
					&& email.matches("^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$");
		}

		/**
		 * 以1开头11位数字即可
		 * 
		 * @param phone
		 * @return
		 */
		public static boolean isPhoneFormat(String phone)
		{
			return !TextUtils.isEmpty(phone) && phone.matches("1\\d{10}");
		}
	}

	// TODO FormatUtil
	public static final class AnimationUtil
	{
		public enum Location
		{
			Left, Top, Right, Bottom;
		}

		public static final int DURATION = 150;

		public static Animation translate(View animView, boolean autoStart,
				boolean show, Location location)
		{
			float fromX = 0.0f;
			float fromY = 0.0f;
			float toX = 0.0f;
			float toY = 0.0f;
			switch (location)
			{
				case Left:
					fromX = show ? -1.0f : 0.0f;
					toX = show ? 0.0f : -1.0f;
					break;

				case Top:
					fromY = show ? -1.0f : 0.0f;
					toY = show ? 0.0f : -1.0f;
					break;

				case Right:
					fromX = show ? 1.0f : 0.0f;
					toX = show ? 0.0f : 1.0f;
					break;

				case Bottom:
					fromY = show ? 1.0f : 0.0f;
					toY = show ? 0.0f : 1.0f;
					break;

				default:
					break;
			}

			final int typeSelf = Animation.RELATIVE_TO_SELF;
			TranslateAnimation ta = new TranslateAnimation(typeSelf, fromX,
					typeSelf, toX, typeSelf, fromY, typeSelf, toY);
			ta.setInterpolator(new AccelerateInterpolator());
			ta.setDuration(DURATION);
			ta.setFillAfter(true);

			if (animView != null)
			{
				ta.setAnimationListener(new GoneViewListener(animView, show));
				if (show)
				{
					animView.setVisibility(View.VISIBLE);
				}
				if (autoStart)
				{
					animView.startAnimation(ta);
				}
			}
			return ta;
		}

		public static Animation alpha(View animView, boolean autoStart,
				boolean show)
		{
			AlphaAnimation aa = null;
			if (show)
			{
				aa = new AlphaAnimation(0.0f, 1.0f);
			} else
			{
				aa = new AlphaAnimation(1.0f, 0.0f);
			}

			aa.setDuration(DURATION);
			aa.setFillAfter(true);
			if (animView != null)
			{
				aa.setAnimationListener(new GoneViewListener(animView, show));
				if (show)
				{
					animView.setVisibility(View.VISIBLE);
				}
				if (autoStart)
				{
					animView.startAnimation(aa);
				}
			}
			return aa;
		}

		public static Animation scale(View animView, boolean autoStart,
				boolean show, Location location, Pair<Float, Float> pivot)
		{
			float fromX = 0.0f;
			float fromY = 0.0f;
			float toX = 0.0f;
			float toY = 0.0f;
			// float pivotX = 0.0f;
			// float pivotY = 0.0f;

			if (show)
			{
				toX = 1.0f;
				toY = 1.0f;
			} else
			{
				fromX = 1.0f;
				fromY = 1.0f;
			}

			// switch (location)
			// {
			// case Left:
			// pivotX = 0.0f;
			// pivotY = pivot;
			// break;
			//
			// case Top:
			// pivotX = pivot;
			// pivotY = 0.0f;
			// break;
			//
			// case Right:
			// pivotX = 1.0f;
			// pivotY = pivot;
			// break;
			//
			// case Bottom:
			// pivotX = pivot;
			// pivotY = 1.0f;
			// break;
			// default:
			// break;
			// }

			final int typeSelf = Animation.RELATIVE_TO_SELF;
			ScaleAnimation sa = new ScaleAnimation(fromX, toX, fromY, toY,
					typeSelf, pivot.first, typeSelf, pivot.second);
			sa.setDuration(DURATION);
			sa.setFillAfter(true);
			sa.setInterpolator(new AccelerateInterpolator());

			if (animView != null)
			{
				sa.setAnimationListener(new GoneViewListener(animView, show));
				if (show)
				{
					animView.setVisibility(View.VISIBLE);
				}
				if (autoStart)
				{
					animView.startAnimation(sa);
				}
			}
			return sa;
		}

		public static Animation rotate(View animView, boolean autoStart,
				boolean show, int edge)
		{
			return null;
		}

		public static void group(View animView, boolean show,
				Animation... anims)
		{
			if (animView == null || anims == null || anims.length == 0) { return; }

			AnimationSet as = new AnimationSet(false);
			for (Animation a : anims)
			{
				as.addAnimation(a);
			}
			as.setDuration(DURATION);
			if (!show)
			{
				as.setAnimationListener(new GoneViewListener(animView, show));
			}
			animView.startAnimation(as);
		}

		private static class GoneViewListener implements
				android.view.animation.Animation.AnimationListener
		{
			private final View goneView;
			private final boolean show;

			public GoneViewListener(View goneView, boolean show)
			{
				this.goneView = goneView;
				this.show = show;
			}

			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				if (!show)
				{
					goneView.setVisibility(View.GONE);
				}
				goneView.clearAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
		}

	}

	// TODO ViewUtil
	public static final class ViewUtil
	{
		public static boolean isShown(View v)
		{
			return v.getVisibility() == View.VISIBLE;
		}

		public static void visible(View v)
		{
			if (v.getVisibility() == View.GONE
					|| v.getVisibility() == View.INVISIBLE)
			{
				v.setVisibility(View.VISIBLE);
			}
		}

		public static void gone(View v)
		{
			// if (isShown(v))
			v.setVisibility(View.GONE);
		}

		public static void invisible(View v)
		{
			v.setVisibility(View.INVISIBLE);
		}
	}

	// TODO ScreenUtil
	public static final class ScreenUtil
	{
		public static int getStatusBarHeight(Context context)
		{
			if (context instanceof Activity)
			{
				Rect frame = new Rect();
				((Activity) context).getWindow().getDecorView()
						.getWindowVisibleDisplayFrame(frame);
				return frame.top;
			} else
			{
				return 0;
			}
		}

		public static DisplayMetrics getScreenRect(Context context)
		{
			DisplayMetrics dm = new DisplayMetrics();
			((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay().getMetrics(dm);
			if (context instanceof Activity)
			{
				Rect frame = new Rect();
				((Activity) context).getWindow().getDecorView()
						.getWindowVisibleDisplayFrame(frame);
				int statusBarHeight = frame.top;
				dm.heightPixels -= statusBarHeight;
			}
			return dm;
		}

		public static int getScreenWidth(Context context)
		{
			return getScreenRect(context).widthPixels;
		}

		public static int getScreenHeight(Context context)
		{
			return getScreenRect(context).heightPixels;
		}

		public static boolean isScreenVertical(Activity activity)
		{
			return activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		}
	}

	// TODO IOUtil
	public static class IOUtil
	{
		public static void close(RandomAccessFile... streams)
		{
			try
			{
				for (RandomAccessFile stream : streams)
				{
					stream.close();
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		public static void close(InputStream is, OutputStream os)
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
				if (os != null)
				{
					os.close();
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		public static void close(Reader reader, Writer writer)
		{
			try
			{
				if (reader != null)
				{
					reader.close();
				}
				if (writer != null)
				{
					writer.close();
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		public static void close(Socket socket)
		{
			try
			{
				if (socket != null)
				{
					socket.close();
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

	}

	// TODO FileUtil
	public static final class FileUtil
	{

		public static boolean exists(String path)
		{
			return path != null && new File(path).exists();
		}

		public static boolean fileCanModified(String path)
		{
			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream(path, true);
				return true;
			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
				return false;
			} finally
			{
				IOUtil.close(null, fos);
			}

		}

		public static boolean fileCanExecute(String path)
		{
			File file = new File(path);
			return file.exists() && file.canExecute() && file.canRead()
					&& file.canWrite();
		}

		public static boolean fileCanCreate(String path)
		{
			File file = new File(getParentDir(path));
			return file.exists() && file.canExecute() && file.canRead()
					&& file.canWrite();
		}

		public static String getFileName(String filePath)
		{
			return TextUtils.isEmpty(filePath) ? "未知文件" : filePath
					.substring(filePath.lastIndexOf(File.separator) + 1);
		}

		public static String getParentDir(String filePath)
		{
			return filePath.substring(0, filePath.lastIndexOf(File.separator));
		}

		public static String getFolder(String filePath)
		{
			return getFileName(getParentDir(filePath));
		}

		/**
		 * 删除目录下所有文件及子文件
		 * 
		 * @param path
		 * @return
		 */
		public static boolean deleteFile(String path)
		{
			File file = new File(path);
			if (file.isDirectory())
			{
				File[] ff = file.listFiles();
				if (ff != null)
				{
					for (int i = 0; i < ff.length; i++)
					{
						deleteFile(ff[i].getPath());
					}
				}
			}
			return file.delete();
		}

		public static void deleteFiles(Collection<String> paths)
		{
			Iterator<String> iterator = paths.iterator();
			while (iterator.hasNext())
			{
				deleteFile(iterator.next());
			}
		}

		public static boolean copyFile(String from, String to)
		{
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try
			{
				fis = new FileInputStream(from);
				fos = new FileOutputStream(to);
				byte[] buf = new byte[1024];
				int real = 0;
				while ((real = fis.read(buf)) > 0)
				{
					fos.write(buf, 0, real);
				}
				fos.flush();
				return true;
			} catch (IOException e)
			{
				e.printStackTrace();
				return false;
			} finally
			{
				IOUtil.close(fis, fos);
			}
		}

		public static String needRename(String desPath)
		{
			File file = new File(desPath);
			final String dir = file.getParent();
			if (file.exists())
			{
				String name = file.getName();
				final int split = name.indexOf(".");
				String left = name.substring(0, split);
				String type = name.substring(split);
				if (left.matches(".*\\(\\d\\)"))
				{
					left = left.substring(0, left.length() - 3);
				}
				int i = 1;
				while (true)
				{
					desPath = dir + File.separator + left + "(" + i + ")"
							+ type;
					file = new File(desPath);
					if (!file.exists())
					{
						break;
					}
					i++;
				}
			}
			return desPath;
		}
	}

	// TODO ArrayUtil
	public static final class ArrayUtil
	{
		/**
		 * 和String的indexOf类似
		 * 
		 * @param srcData
		 * @param find
		 * @return -1 没找着
		 */
		public static int indexOf(byte[] srcData, byte[] find)
		{
			int index = -1;
			for (int i = 0; i < srcData.length; i++)
			{
				if (srcData[i] == find[0])
				{
					int j = 0;
					for (; j < find.length; j++)
					{
						if (srcData[i + j] != find[j])
						{
							break;
						}
					}
					if (j == find.length)
					{
						// 是因为j < find.length跳出循环的
						index = i;
					}
				}
			}
			return index;

		}

		public static boolean isEmpty(byte[] bytes)
		{
			if (bytes == null || bytes.length == 0) { return true; }
			for (byte b : bytes)
			{
				if (b != 0) { return false; }
			}
			return true;
		}

	}

	// TODO ParserUtil
	public static class ParserUtil
	{
		/**
		 * @param content
		 * @param tags
		 * @return null 失败
		 */
		public static Map<String, String> pullParse(String content,
				List<String> tags)
		{
			Map<String, String> desMap = null;
			XmlPullParser parser = Xml.newPullParser();
			try
			{
				parser.setInput(new StringReader(content));
				int event = parser.getEventType();
				while (event != XmlPullParser.END_DOCUMENT)
				{
					switch (event)
					{
						case XmlPullParser.START_DOCUMENT:
							desMap = new HashMap<String, String>();
							break;
						case XmlPullParser.START_TAG:
							for (int i = 0; i < tags.size(); i++)
							{
								if (tags.get(i).equalsIgnoreCase(
										parser.getName()))
								{
									desMap.put(tags.get(i), parser.nextText());
								}
							}
							break;
						case XmlPullParser.END_TAG:
							break;
					}
					event = parser.next();
				}

			} catch (Exception e)
			{
				e.printStackTrace();
			}
			return desMap;
		}
	}

	// TODO KeyBoardUtil
	public static class KeyBoardUtil
	{
		public static void showKeyboard(final Activity activity)
		{
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					InputMethodManager imm = (InputMethodManager) activity
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					View view = activity.getWindow().peekDecorView();
					if (view != null && imm != null)
					{
						imm.showSoftInput(view, 0);
					}
				}
			}, 500);
		}

		public static void dismissKeyboard(Activity activity)
		{
			InputMethodManager imm = (InputMethodManager) activity
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			View view = activity.getWindow().peekDecorView();
			if (view != null && imm != null)
			{
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}

		/**
		 * Fragment跳转时，传入Activity不管用，调用这个
		 * 
		 * @param curFocusView
		 */
		public static void showKeyboard(final View curFocusView)
		{
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					InputMethodManager imm = (InputMethodManager) curFocusView
							.getContext().getSystemService(
									Context.INPUT_METHOD_SERVICE);
					if (imm != null)
					{
						imm.showSoftInput(curFocusView, 0);
					}
				}
			}, 500);
		}

		public static void dismissKeyboard(View curFocusView)
		{
			InputMethodManager imm = (InputMethodManager) curFocusView
					.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null)
			{
				imm.hideSoftInputFromWindow(curFocusView.getWindowToken(), 0);
			}
		}
	}

}
