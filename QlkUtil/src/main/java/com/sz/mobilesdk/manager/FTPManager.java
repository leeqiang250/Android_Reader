package com.sz.mobilesdk.manager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.sz.mobilesdk.util.SZLog;

/**
 * 支持断点续传的FTP实用类
 */
public class FTPManager
{

	private String TAG = FTPManager.class.getSimpleName();

	public static final String ftpName = "anonymous";
	public static final String ftpPassword = "";

	/**
	 * 连接到FTP服务器
	 * 
	 * @param hostname
	 *            主机名
	 * @param port
	 *            端口
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * 
	 * @return 是否连接成功
	 */
	public synchronized boolean connect(FTPClient ftpClient, String hostname, int port, String username, String password)
	{
		try
		{
			ftpClient.connect(hostname, port);
			SZLog.w(TAG, "host: " + hostname);
			SZLog.w(TAG, "port: " + port);
			ftpClient.setControlEncoding("UTF-8");
			if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode()) && ftpClient.login(username, password))
			{
				// 设置被动模式
				ftpClient.enterLocalPassiveMode();
				// 设置以二进制方式传输
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				return true;
			} else
			{
				disconnect(ftpClient);
				return false;
			}

		} catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 断开与远程服务器的连接
	 * 
	 * @throws java.io.IOException
	 */
	public void disconnect(FTPClient ftpClient)
	{
		try
		{
			if (ftpClient.isConnected()) ftpClient.disconnect();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 上传文件到FTP服务器，支持断点续传
	 * 
	 * @param local
	 *            本地文件名称，绝对路径
	 * @param remote
	 *            远程文件路径，使用/home/directory1/subdirectory/file.ext或是
	 *            http://www.guihua.org /subdirectory/file.ext
	 *            按照Linux上的路径指定方式，支持多级目录嵌套，支持递归创建不存在的目录结构
	 * @return 上传结果
	 * @throws java.io.IOException
	 */
	public int upload(FTPClient ftpClient, String local, String remote) throws IOException
	{
		// 设置PassiveMode传输
		ftpClient.enterLocalPassiveMode();
		// 设置以二进制流的方式传输
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.setControlEncoding("GBK");
		int result;
		// 对远程目录的处理
		String remoteFileName = remote;

		if (remote.contains("/"))
		{
			remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
			// 创建服务器远程目录结构，创建失败直接返回
			if (CreateDirecroty(remote, ftpClient) == UploadStatus.Create_Directory_Fail)
			{
				return UploadStatus.Create_Directory_Fail;
			}
		}

		// 检查远程是否存在文件
		FTPFile[] files = ftpClient.listFiles(new String(remoteFileName.getBytes("GBK"), "iso-8859-1"));
		if (files.length == 1)
		{
			long remoteSize = files[0].getSize();
			File f = new File(local);
			long localSize = f.length();
			if (remoteSize == localSize)
			{
				return UploadStatus.File_Exits;
			} else if (remoteSize > localSize)
			{
				return UploadStatus.Remote_Bigger_Local;
			}

			// 尝试移动文件内读取指针,实现断点续传
			result = uploadFile(remoteFileName, f, ftpClient, remoteSize);

			// 如果断点续传没有成功，则删除服务器上文件，重新上传
			if (result == UploadStatus.Upload_From_Break_Failed)
			{
				if (!ftpClient.deleteFile(remoteFileName))
				{
					return UploadStatus.Delete_Remote_Faild;
				}
				result = uploadFile(remoteFileName, f, ftpClient, 0);
			}
		} else
		{
			result = uploadFile(remoteFileName, new File(local), ftpClient, 0);
		}
		return result;
	}

	/**
	 * 递归创建远程服务器目录
	 * 
	 * @param remote
	 *            远程服务器文件绝对路径
	 * @param ftpClient
	 *            FTPClient对象
	 * @return 目录创建是否成功
	 * @throws java.io.IOException
	 */
	public int CreateDirecroty(String remote, FTPClient ftpClient) throws IOException
	{
		int status = UploadStatus.Create_Directory_Success;
		String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
		if (!directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(new String(directory.getBytes("GBK"), "iso-8859-1")))
		{
			// 如果远程目录不存在，则递归创建远程服务器目录
			int start = 0;
			int end = 0;
			if (directory.startsWith("/"))
			{
				start = 1;
			} else
			{
				start = 0;
			}
			end = directory.indexOf("/", start);
			while (true)
			{
				String subDirectory = new String(remote.substring(start, end).getBytes("GBK"), "iso-8859-1");
				if (!ftpClient.changeWorkingDirectory(subDirectory))
				{
					if (ftpClient.makeDirectory(subDirectory))
					{
						ftpClient.changeWorkingDirectory(subDirectory);
					} else
					{
						SZLog.w(TAG, "创建目录失败");
						return UploadStatus.Create_Directory_Fail;
					}
				}

				start = end + 1;
				end = directory.indexOf("/", start);

				// 检查所有目录是否创建完毕
				if (end <= start)
				{
					break;
				}
			}
		}
		return status;
	}

	/**
	 * 上传文件到服务器,新上传和断点续传
	 * 
	 * @param remoteFile
	 *            远程文件名，在上传之前已经将服务器工作目录做了改变
	 * @param localFile
	 *            本地文件File句柄，绝对路径 需要显示的处理进度步进值
	 * @param ftpClient
	 *            FTPClient引用
	 * @return
	 * @throws java.io.IOException
	 */
	public int uploadFile(String remoteFile, File localFile, FTPClient ftpClient, long remoteSize) throws IOException
	{
		int status;
		// 显示进度的上传
		long step = localFile.length() / 100;
		long process = 0;
		long localreadbytes = 0L;
		RandomAccessFile raf = new RandomAccessFile(localFile, "r");
		OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes("GBK"), "iso-8859-1"));
		// 断点续传
		if (remoteSize > 0)
		{
			ftpClient.setRestartOffset(remoteSize);
			process = remoteSize / step;
			raf.seek(remoteSize);
			localreadbytes = remoteSize;
		}
		byte[] bytes = new byte[1024];
		int c;
		while ((c = raf.read(bytes)) != -1)
		{
			out.write(bytes, 0, c);
			localreadbytes += c;
			if (localreadbytes / step != process)
			{
				process = localreadbytes / step;
				System.out.println("上传进度:" + process);
				// TODO 汇报上传状态
			}
		}
		out.flush();
		raf.close();
		out.close();
		boolean result = ftpClient.completePendingCommand();
		if (remoteSize > 0)
		{
			status = result ? UploadStatus.Upload_From_Break_Success : UploadStatus.Upload_From_Break_Failed;
		} else
		{
			status = result ? UploadStatus.Upload_New_File_Success : UploadStatus.Upload_New_File_Failed;
		}
		return status;
	}

	public class UploadStatus
	{
		static final int Create_Directory_Fail = 0x01a0; // 远程服务器相应目录创建失败
		static final int Create_Directory_Success = 0x01a1; // 远程服务器闯将目录成功
		static final int Upload_New_File_Success = 0x01a2; // 上传新文件成功
		static final int Upload_New_File_Failed = 0x01a3; // 上传新文件失败
		static final int File_Exits = 0x01a4; // 文件已经存在
		static final int Remote_Bigger_Local = 0x01a5; // 远程文件大于本地文件
		static final int Upload_From_Break_Success = 0x01a5; // 断点续传成功
		static final int Upload_From_Break_Failed = 0x01a7; // 断点续传失败
		static final int Delete_Remote_Faild = 0x01a8; // 删除远程文件失败
	}
}