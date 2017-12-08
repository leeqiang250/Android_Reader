package cn.com.pyc.xcoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util.IOUtil;

import cn.com.pyc.bean.SmInfo;

/*-
 * 头文件扩展结构（需要加密，密钥：SM_HEAD_EXTRA_KEY）
 * 
 */
public class SmHeadExtraData
{
	private static final String SM_HEAD_EXTRA_KEY = "PYCAdminabcdefghijklmopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ12345!@#$%^&";
	private static final short UNIT_LEN = 4;
	public static final short FLAG_LEN = 268;
	private static final short SESSION_KEY_LEN = 256;
	private final byte[] flagBuffer;

	private int id;

	SmHeadExtraData()
	{
		flagBuffer = new byte[FLAG_LEN];
	}

	/*-
	 * 这个方法会自动解密数据
	 */
	XCoderResult analysisFlagFromFile(String filePath)
	{
		FileInputStream fis = null;
		XCoderResult xr = new XCoderResult();
		try
		{
			File file = new File(filePath);
			fis = new FileInputStream(file);
			fis.skip(file.length() - FLAG_LEN);
			fis.read(flagBuffer);
			XCoder.setEncryptKey(SM_HEAD_EXTRA_KEY.getBytes(), 1);
			XCoder.decodeBuffer(flagBuffer, flagBuffer, FLAG_LEN); // 解密数据
			if (isPycFile())
			{
				byte[] b = new byte[UNIT_LEN];
				System.arraycopy(flagBuffer, UNIT_LEN, b, 0, UNIT_LEN);
				id = DataConvert.bytesToInt(b);
				SmInfo info = new SmInfo();
				info.setFileVersion(flagBuffer[3]);
				info.setSessionKey(peelSessionKey()); // sessionKey
				info.setFilePath(filePath);
				info.setFid(id);
				xr.setSmInfo(info);
			}
			else
			{
				xr.setSuc(XCoderResult.PYC_ERROR);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			xr.setSuc(XCoderResult.FILE_ERROR);
		}
		finally
		{
			IOUtil.close(fis, null);
		}
		return xr;
	}

	private boolean isPycFile()
	{
		return flagBuffer[0] == 'P' && flagBuffer[1] == 'Y' && flagBuffer[2] == 'C';
	}

	/**
	 * 数据已加密
	 * 
	 * @param id
	 * @param skey
	 *            16位的
	 */
	byte[] writeBuffer(int id, byte[] skey)
	{
		flagBuffer[0] = 'P';
		flagBuffer[1] = 'Y';
		flagBuffer[2] = 'C';
		flagBuffer[3] = 1;
		System.arraycopy(DataConvert.intToBytes(id), 0, flagBuffer, UNIT_LEN, UNIT_LEN);
		System.arraycopy(wrapSessionKey(skey), 0, flagBuffer, 2 * UNIT_LEN, SESSION_KEY_LEN);
		XCoder.setEncryptKey(SM_HEAD_EXTRA_KEY.getBytes(), 1);
		XCoder.codeBuffer(flagBuffer, flagBuffer, SmHeadExtraData.FLAG_LEN); // 加密数据
		return flagBuffer;
	}

	private byte[] wrapSessionKey(byte[] sKey)
	{
		final short len = SESSION_KEY_LEN;
		final byte[] wrapKey = new byte[len];
		// 初始化sessionKey
		for (int i = 0; i < len; i++)
		{
			wrapKey[i] = (byte) (new Random().nextInt(100));
		}
		for (int j = 0; j < 16; j++)
		{
			wrapKey[j * 16] = sKey[j];
		}

		return wrapKey;
	}

	private byte[] peelSessionKey()
	{
		final byte[] sessionKey = new byte[SESSION_KEY_LEN];
		System.arraycopy(flagBuffer, 2 * UNIT_LEN, sessionKey, 0, SESSION_KEY_LEN);
		byte[] realKey = new byte[16];
		for (int i = 0; i < 16; i++)
		{
			realKey[i] = sessionKey[i * 16];
		}
		return realKey;
	}

	int getID()
	{
		return id;
	}
}
