package cn.com.pyc.user.key;

import java.io.FileOutputStream;

import com.qlk.util.tool.Util.IOUtil;

import android.content.Context;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.utils.Dirs;

public class KeyTool
{
	/*-
	 * 用于由PC端导入钥匙后获取手机上的密文
	 * 规则：
	 * 	第一行：用户名
	 *  第二行：suc
	 *  第三行：内置硬盘
	 *  第四行：外置硬盘1
	 *  第五行：外置硬盘2
	 *  ...
	 */
	public static void makeSucFile(Context context, int suc)
	{
		FileOutputStream fos = null;

		try
		{
			fos = new FileOutputStream(Dirs.getPycSucPath());
			fos.write((UserDao.getDB(context).getUserInfo().getUserName() + "\r\n")
					.getBytes());	// \r\n是pc端要求，我们一般是\n即可
			fos.write((suc + "\r\n").getBytes());
			fos.write((Dirs.getUserDir(context, Dirs.getDefaultBoot())
					.getBytes()));
			String extraDir = Dirs.getExtraBoot();
			if (!extraDir.equals(Dirs.getDefaultBoot()))
			{
				fos.write(("\r\n" + Dirs.getUserDir(context, extraDir))
						.getBytes());
			}
			fos.flush();
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
