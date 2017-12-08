package cn.com.pyc.xcoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool._SysoXXX;
import com.qlk.util.tool.Util.IOUtil;

import android.R.bool;
import cn.com.pyc.bean.ProtocolInfo;

/*-
 * 外发文件头结构
 * 
 * 标志: PYC
 */
class SmHeadData
{
	private static final String KEY = "PYCAdminabcdefghijklmopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ12345!@#$%^&";
	private static final short UNIT_LEN = 8; // 结构以8字节为一个单元
	private static final short FLAG_LEN = 32; // 结构总长度
	private final byte[] flagBuffer; // 结构的byte[]数组形式

	private long fileLen;
	private long codeLen;
	private long selfOffset;

	SmHeadData()
	{
		flagBuffer = new byte[FLAG_LEN];
	}

	XCoderResult analysisFlagFromFile(String filePath)
	{
		FileInputStream fis = null;
		XCoderResult xr = new SmHeadExtraData().analysisFlagFromFile(filePath);
		int len = FLAG_LEN + SmHeadExtraData.FLAG_LEN;
		if (xr.getSmInfo().getFileVersion() == 1)
		{
			len += ProtocolInfo.OFFLINE_TOTAL_LEN;
		}
		xr = new XCoderResult();	// 清空xr
		try
		{
			File file = new File(filePath);
			fis = new FileInputStream(file);
			selfOffset = file.length() - len;
			fis.skip(selfOffset);
			fis.read(flagBuffer);
			if (isPycFile())
			{
				if (!analysisBuffer())
				{
					xr.setSuc(XCoderResult.PRMT_ERROR);
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

	private boolean isPycFile()
	{
		boolean isV1 = flagBuffer[0] == 'P' && flagBuffer[1] == 'Y' && flagBuffer[2] == 'C'
				&& flagBuffer[3] == 0;

		if (!isV1)
		{
			//解密
			XCoder.setEncryptKey(KEY.getBytes(), 1);
			XCoder.decodeBuffer(flagBuffer, flagBuffer, FLAG_LEN);
			boolean isV2 = flagBuffer[0] == 'P' && flagBuffer[1] == 'Y' && flagBuffer[2] == 'C'
					&& flagBuffer[3] == 1;
			return isV2;
		}
		return isV1;
	}

	private boolean analysisBuffer()
	{
		byte[] b = new byte[UNIT_LEN];
		System.arraycopy(flagBuffer, UNIT_LEN, b, 0, UNIT_LEN);
		fileLen = DataConvert.byteToLong(b);
		System.arraycopy(flagBuffer, 2 * UNIT_LEN, b, 0, UNIT_LEN);
		codeLen = DataConvert.byteToLong(b);
		if (codeLen == -1 || fileLen <= codeLen)
		{
			codeLen = XCoder.format16(fileLen);		// 实际加密长度
		}

		// 检查参数---codeLen <= fileLen + 16是16位补齐的情况
		return fileLen > 0 && codeLen > 0 && codeLen <= Long.MAX_VALUE && fileLen <= Long.MAX_VALUE
				&& codeLen <= fileLen + 16;
	}

	//制作图片用这个
	byte[] writeBuffer0(long fileLen, long codeLen)
	{
		flagBuffer[0] = 'P';
		flagBuffer[1] = 'Y';
		flagBuffer[2] = 'C';
		flagBuffer[3] = 0;
		System.arraycopy(DataConvert.longToByte(fileLen), 0, flagBuffer, UNIT_LEN, UNIT_LEN);
		System.arraycopy(DataConvert.longToByte(codeLen), 0, flagBuffer, 2 * UNIT_LEN, UNIT_LEN);

		return flagBuffer;
	}

	//制作其他格式pbb文件用这个
	byte[] writeBuffer1(long fileLen, long codeLen)
	{
		flagBuffer[0] = 'P';
		flagBuffer[1] = 'Y';
		flagBuffer[2] = 'C';
		flagBuffer[3] = 1;	//V2算法
		System.arraycopy(DataConvert.longToByte(fileLen), 0, flagBuffer, UNIT_LEN, UNIT_LEN);
		System.arraycopy(DataConvert.longToByte(codeLen), 0, flagBuffer, 2 * UNIT_LEN, UNIT_LEN);

		//加密－V2需要加密
		XCoder.setEncryptKey(KEY.getBytes(), 1);
		XCoder.codeBuffer(flagBuffer, flagBuffer, FLAG_LEN);
		return flagBuffer;
	}

	long getFileLen()
	{
		return fileLen;
	}

	/**
	 * @return 注意，是真正的加密长度，不是16位格式化后的
	 */
	long getCodeLen()
	{
		return codeLen;
	}

	long getSelfOffset()
	{
		return selfOffset;
	}

}
