package cn.com.pyc.xcoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util.ArrayUtil;
import com.qlk.util.tool.Util.IOUtil;

import cn.com.pyc.bean.ProtocolInfo;

/*-
 * 加解密时文件尾部的结构
 * 
 * 分老版和新版结构
 * 老版标志：PYC1
 * 新版标志: PYC2
 */
class FlagData
{
	private final short UNIT_LEN = 8;	// 结构以8字节为一个单元
	private final short FLAG_LEN = 48;	// 结构总长度
	private final byte[] flagBuffer;	// 结构的byte[]数组形式

	private long fileLen;
	private long codeLen;

	private final byte[] uid;

	public FlagData()
	{
		flagBuffer = new byte[FLAG_LEN];
		uid = new byte[ProtocolInfo.UID_LEN];		// 注意长度一致，否则一会比较就出错analysisFlagFromFile
	}

	boolean isOldKey()
	{
		return flagBuffer[3] == 1;
	}

	public boolean isPYCFile(String path)
	{
		FileInputStream fis = null;
		try
		{
			File file = new File(path);
			fis = new FileInputStream(file);
			fis.skip(file.length() - FLAG_LEN); // 不能用fis.available()代替，它是int类型的
			fis.read(flagBuffer);
			return isPycFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			IOUtil.close(fis, null);
		}
		return false;
	}

	/*-
	 * 分析文件尾部的结构
	 */
	public XCoderResult analysisFlagFromFile(String filePath, byte[] u)
	{
		FileInputStream fis = null;
		XCoderResult xr = new XCoderResult();
		try
		{
			File file = new File(filePath);
			fis = new FileInputStream(file);
			fis.skip(file.length() - FLAG_LEN); // 不能用fis.available()代替，它是int类型的
			fis.read(flagBuffer);
			if (isPycFile())
			{
				if (!analysisBuffer())
				{
					xr.setSuc(XCoderResult.PRMT_ERROR);
				}
				else
				{
					if (!Arrays.equals(u, uid))
					{
						xr.setSuc(XCoderResult.OWNER_ERROR);
					}
				}
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

	private boolean analysisBuffer()
	{
		byte[] b = new byte[UNIT_LEN];
		System.arraycopy(flagBuffer, UNIT_LEN, b, 0, UNIT_LEN);
		fileLen = DataConvert.byteToLong(b);
		System.arraycopy(flagBuffer, 2 * UNIT_LEN, b, 0, UNIT_LEN);
		codeLen = DataConvert.byteToLong(b);
		System.arraycopy(flagBuffer, 5 * UNIT_LEN, uid, 0, uid.length);
		if (codeLen == -1 || fileLen <= codeLen)
		{
			codeLen = fileLen; 	// 实际加密长度
		}

		// 检查参数---uid不能为空---codeLen <= fileLen + 16是16位补齐的情况
		return fileLen > 0 && codeLen > 0 && codeLen <= Long.MAX_VALUE && fileLen <= Long.MAX_VALUE
				&& codeLen <= fileLen + 16 && !ArrayUtil.isEmpty(uid);
	}

	/*-
	 * flagBuffer[3] == 1表示老版加解密；
	 * flagBuffer[3] == 2表示新版加解密。
	 * 不同之处在uid
	 */
	private boolean isPycFile()
	{
		return flagBuffer[0] == 'P' && flagBuffer[1] == 'Y' && flagBuffer[2] == 'C'
				&& (flagBuffer[3] == 1 || flagBuffer[3] == 2);
	}

	/*
	 * 注意一点：不管是加密还是解密flagBuffer都是同一个对象，执行完解密后，标志PYC2有可能就变成了PYC1 所以要重新赋值
	 */
	byte[] writeBuffer(long fileLen, long codeLen, byte[] uid)
	{
		flagBuffer[0] = 'P';
		flagBuffer[1] = 'Y';
		flagBuffer[2] = 'C';
		flagBuffer[3] = 2; // 新key
		System.arraycopy(DataConvert.longToByte(fileLen), 0, flagBuffer, UNIT_LEN, UNIT_LEN);
		if (fileLen <= codeLen)
		{
			codeLen = -1; // 由于有16位格式化，则会出现codeLen>fileLen的情况；-1是和pc端同步。这里codeLen只有三种情况：4Kb,10Mb,-1.
		}
		System.arraycopy(DataConvert.longToByte(codeLen), 0, flagBuffer, 2 * UNIT_LEN, UNIT_LEN);
		System.arraycopy(uid, 0, flagBuffer, UNIT_LEN * 5, uid.length);

		return flagBuffer;
	}

	long getFileLen()
	{
		return fileLen;
	}

	/**
	 * 注意：是真正的加密长度，不是16位格式化后的（全文加密时是文件长度，可能不是16的）
	 * 
	 * @return
	 */
	long getCodeLen()
	{
		return codeLen;
	}

	byte[] getUid()
	{
		return uid;
	}
}
