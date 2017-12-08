package cn.com.pyc.reader;

import java.util.Arrays;

public class PlayFile
{
	private String filePath;
	private byte[] key;
	private long codeLen;
	private long memoryPos;
	private long offset;
	private long fileLen;
	
	
	

	public long getFileLen()
	{
		return fileLen;
	}

	public void setFileLen(long fileLen)
	{
		this.fileLen = fileLen;
	}

	public long getOffset()
	{
		return offset;
	}

	public void setOffset(long offset)
	{
		this.offset = offset;
	}

	/**
	 * 明文
	 * 
	 * @param filePath
	 */
	public PlayFile(String filePath)
	{
		this.filePath = filePath;
	}

	/**
	 * 密文
	 * 
	 * @param filePath
	 * @param key
	 *            注意：外发文件时，该key是“-'0'+'A'”之后的，而不是原始的encodeKey
	 * @param codeLen
	 */
	public PlayFile(String filePath, byte[] key, long codeLen)
	{
		this.filePath = filePath;
		this.key = key;
		this.codeLen = codeLen;
	}

	public long getMemoryPos()
	{
		return memoryPos;
	}

	public void setMemoryPos(long memoryPos)
	{
		this.memoryPos = memoryPos;
	}

	public String getFilePath()
	{
		return filePath;
	}

	public byte[] getKey()
	{
		return key;
	}

	public long getCodeLen()
	{
		return codeLen;
	}

	@Override
	public String toString()
	{
		return "PlayFile [filePath=" + filePath + ", key="
				+ Arrays.toString(key) + ", codeLen=" + codeLen
				+ ", memoryPos=" + memoryPos + "]";
	}

}
