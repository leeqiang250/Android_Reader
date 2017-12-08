package cn.com.pyc.wifi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import com.qlk.util.tool.DataConvert;

import android.content.Intent;
import android.os.Looper;
import android.os.StatFs;
import android.widget.Toast;

import cn.com.pyc.bean.ProtocolInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.utils.Dirs;

class InstructionManager
{
	private static final int COMMAND_SCAN = 0x0001;
	private static final int IAM_PBB_PHONE = 0x0002;
	// private static final int COMMAND_IDENTIFY = 0x0003;
	// private static final int COMMAND_IDENTIFY_SUCCESS = 0x0004;
	private static final int COMMAND_ENCRYPT_FILES = 0x0005;
	private static final int COMMAND_GET_ENCRYPT_FILES = 0x0006;
	private static final int COMMAND_ENCRYPT_FILES_LENGTH = 0x0007;
	private static final int COMMAND_IMPORT_FILE = 0x0008;
	private static final int COMMAND_EXPORT_FILE = 0x0009;
	private static final int COMMAND_DELETE_FILE = 0x0010;
	private static final int COMMAND_STATUS_SUCCESS = 0x0011;
	private static final int COMMAND_STATUS_FAILURE = 0x0012;
	private static final int COMMAND_STATUS_READY = 0x0013;
	// private static final int COMMAND_IMPORT_FILE_DATA = 0x0014;
	private static final int COMMAND_EXPORT_FILE_LENGTH = 0x0015;
	// private static final int COMMAND_EXPORT_FILE_DATA = 0x0016;
	private static final int COMMAND_STATUS_ERROR = 0x0017;

	private static final int LENGTH_COMMAND_TOTAL = 512;
	// private static final int LENGTH_BRS_INIT = 1024;
	private static final int LENGTH_INT = 4;

	private Socket socket;
	private BufferedInputStream bis;
	private BufferedOutputStream bos;

	private byte[] bufReceive;
	private byte[] bufSend;
	// private int realRcvSize; // 接收有效值

	private final byte[] FLAG;
	private WifiServer server;

	InstructionManager(WifiServer server, Socket socket)
	{
		this.server = server;
		this.socket = socket;
		FLAG = "PYC1".getBytes();
		bufReceive = new byte[LENGTH_COMMAND_TOTAL];
		initSendBuf(LENGTH_COMMAND_TOTAL);
	}

	boolean startCommand() throws IOException
	{
		bis = new BufferedInputStream(socket.getInputStream());
		bos = new BufferedOutputStream(socket.getOutputStream());

		while(true)
		{
			for (int i = 0; i < bufReceive.length; i++)
			{
				bufReceive[i] = 0;
			}
			if (!isSocketInUse())
			{
				return false;
			}
			if (!received())
			{
				continue;
			}

			switch (getCommand())
			{
				case COMMAND_SCAN:
					return managerScan();

				case COMMAND_GET_ENCRYPT_FILES:
					managerGetCiphers();
					break;

				case COMMAND_DELETE_FILE:
					managerDelete();
					break;

				case COMMAND_IMPORT_FILE:
					managerImport();
					break;

				case COMMAND_EXPORT_FILE:
					managerExport();
					break;

				default:
					break;
			}
		}
	}

	/***************** 指令集 **************************/

	private boolean managerScan() throws IOException
	{
		byte[] bufUser = new byte[ProtocolInfo.USERNAME_LEN];
		System.arraycopy(bufReceive, LENGTH_INT * 2, bufUser, 0, ProtocolInfo.USERNAME_LEN);
		String netUser = new String(bufUser).trim();
		String localUser = UserDao.getDB(server).getUserInfo().getUserName();
		if (netUser.equals(localUser))
		{
			writeCommand(IAM_PBB_PHONE);
			send();
			return true;
		}
		return false;
	}

	private void managerGetCiphers() throws IOException
	{
		byte[] list = getCiphers();
		int len = list.length;

		writeCommand(COMMAND_ENCRYPT_FILES_LENGTH);
		writeData(DataConvert.intToBytes(len));
		send();

		if (!reSend())
		{
			return;
		}

		initSendBuf(LENGTH_INT * 2 + len);
		writeCommand(COMMAND_ENCRYPT_FILES);
		writeData(list);
		send();

		reSend();

		initSendBuf(LENGTH_COMMAND_TOTAL);
	}

	private void managerDelete() throws IOException
	{
		File file = getFile();
		boolean suc = file.exists();
		if (suc)
		{
			suc = file.delete();
		}
		if (suc)
		{
			GlobalData.ensure(server, file.getAbsolutePath()).delete(file.getAbsolutePath());
			server.sendBroadcast(new Intent("pyc_refresh"));
		}

		writeCommand(suc ? COMMAND_STATUS_SUCCESS : COMMAND_STATUS_FAILURE);
		send();
	}

	/*-*********************导出*********************************/

	// An established connection was aborted by the software in your host
	// computer, possibly due to a data transmission time-out or protocol error.

	private void managerExport() throws IOException
	{
		File file = getFile();
		if (!file.exists())
		{
			writeCommand(COMMAND_STATUS_FAILURE);
			send();
			return;
		}

		server.export(file.getAbsolutePath());

		writeCommand(COMMAND_EXPORT_FILE_LENGTH);
		writeData(DataConvert.intToBytes((int) file.length()));
		send();
	}

	/*-*********************导入*********************************/

	private void managerImport() throws IOException
	{
		long fileLen = getFileLength();
		String fileName = getFileName();
		Looper.prepare();
		Toast.makeText(server,
				"准备导入文件：" + fileName + "(" + DataConvert.toSize(server, fileLen) + ")",
				Toast.LENGTH_SHORT).show();
		String dir = hasEnoughSpace(fileLen);
		if (dir != null)
		{
			server.import1(dir + "/" + fileName, fileLen);
			writeCommand(COMMAND_STATUS_READY);
			send();
		}
		else
		{
			Toast.makeText(server, "磁盘空间不足！", Toast.LENGTH_SHORT).show();
			writeCommand(COMMAND_STATUS_ERROR);
			send();
		}
		Looper.loop();
	}

	/***************** 接收数据 **************************/

	private int getCommand()
	{
		byte[] cmd = new byte[LENGTH_INT];
		System.arraycopy(bufReceive, 0, cmd, 0, LENGTH_INT);
		return DataConvert.bytesToInt(cmd);
	}

	// private int getBRSBlockSize()
	// {
	// byte[] blockSize = new byte[LENGTH_INT];
	// System.arraycopy(bufReceive, LENGTH_INT * 3, blockSize, 0, LENGTH_INT);
	// return DataConvert.bytesToInt(blockSize);
	// }

	private File getFile() throws IOException
	{
		return new File(getFileParent(), getFileName());
	}

	private int getFileLength()
	{
		byte[] fileLen = new byte[LENGTH_INT];
		System.arraycopy(bufReceive, LENGTH_INT * 3 + ProtocolInfo.FILE_NAME_LEN, fileLen, 0,
				LENGTH_INT);
		return DataConvert.bytesToInt(fileLen);
	}

	private String getFileParent() throws IOException
	{
		byte[] fileFlag = new byte[LENGTH_INT];
		System.arraycopy(bufReceive, LENGTH_INT * 2 + ProtocolInfo.FILE_NAME_LEN, fileFlag, 0,
				LENGTH_INT);
		String boot = DataConvert.bytesToInt(fileFlag) == 0 ? Dirs.getDefaultBoot() : Dirs
				.getExtraBoot();
		return Dirs.getUserDir(server, boot);
	}

	private String getFileName()
	{
		byte[] bufFile = new byte[ProtocolInfo.FILE_NAME_LEN];
		System.arraycopy(bufReceive, LENGTH_INT * 2, bufFile, 0, ProtocolInfo.FILE_NAME_LEN);
		return new String(bufFile).trim();
	}

	private boolean isFlagRight()
	{
		byte[] flag = new byte[LENGTH_INT];
		System.arraycopy(bufReceive, LENGTH_INT, flag, 0, LENGTH_INT);
		for (int i = 0; i < LENGTH_INT; i++)
		{
			if (FLAG[i] != flag[i])
			{
				return false;
			}
		}
		return true;
	}

	/***************** 发送数据 **************************/

	private void send() throws IOException
	{
		bos.write(bufSend);
		bos.flush();
	}

	private boolean reSend() throws IOException
	{
		if (!received())
		{
			return false;
		}
		if (getCommand() == COMMAND_STATUS_FAILURE)
		{
			send();
			received();
			if (getCommand() == COMMAND_STATUS_FAILURE)
			{
				return false; // 只重发一次
			}
		}
		return getCommand() == COMMAND_STATUS_SUCCESS; // 确认是否真的成功
	}

	private boolean received() throws IOException
	{
		int realRcvSize = bis.read(bufReceive);
		return realRcvSize != -1 && isFlagRight();
	}

	private void writeData(byte[] data)
	{
		System.arraycopy(data, 0, bufSend, LENGTH_INT * 2, data.length);
	}

	private void initSendBuf(int length)
	{
		bufSend = new byte[length];
		writeFlag();
	}

	private void writeCommand(int command)
	{
		System.arraycopy(DataConvert.intToBytes(command), 0, bufSend, 0, LENGTH_INT);
		writeFlag(); // 有时客户端收到的flag不对，所以在此处确认一下
	}

	private void writeFlag()
	{
		System.arraycopy(FLAG, 0, bufSend, LENGTH_INT, LENGTH_INT);
	}

	/**************************** Tools **********************************/

	boolean isSocketInUse()
	{
		// 感觉没必要
		try
		{
			socket.sendUrgentData(0xFF);
		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}

	private String hasEnoughSpace(long fileLen)
	{
		ArrayList<String> cardsPaths = Dirs.getCardsPaths();
		for (String dir : cardsPaths)
		{
			StatFs statfs = new StatFs(dir);
			if ((long) statfs.getAvailableBlocks() * statfs.getBlockSize() > fileLen)
			{
				return Dirs.getUserDir(server, dir);
			}
		}
		return null;
	}

	private byte[] getCiphers()
	{
		ArrayList<String> paths = new ArrayList<String>();
		paths.addAll(GlobalData.Image.instance(server).getCopyPaths(true));
		paths.addAll(GlobalData.Video.instance(server).getCopyPaths(true));
		paths.addAll(GlobalData.Pdf.instance(server).getCopyPaths(true));
		paths.addAll(GlobalData.Music.instance(server).getCopyPaths(true));
		File file;
		StringBuilder sb = new StringBuilder();
		for (String path : paths)
		{
			file = new File(path);
			sb.append(file.getName());
			sb.append(",");
			sb.append(file.length());
			sb.append(",");
			sb.append(DataConvert.toDate(file.lastModified()));
			sb.append(",");
			sb.append(getFlag(file.getAbsolutePath()));
			sb.append(";");
		}
		return sb.toString().getBytes();
	}

	// 0:default
	private int getFlag(String absolutePath)
	{
		return absolutePath.startsWith(Dirs.getDefaultBoot()) ? 0 : 1;
	}

	void importSuc(String path, boolean suc)
	{
		Looper.prepare();
		Toast.makeText(server, suc ? "导入成功！" : "导入失败", Toast.LENGTH_SHORT).show();
		Looper.loop();
		writeCommand(suc ? COMMAND_STATUS_SUCCESS : COMMAND_STATUS_FAILURE);
		StringBuilder sb = new StringBuilder();
		File file = new File(path);
		sb.append(file.getName());
		sb.append(",");
		sb.append(file.length());
		sb.append(",");
		sb.append(DataConvert.toDate(file.lastModified()));
		sb.append(",");
		sb.append(getFlag(file.getAbsolutePath()));
		sb.append(";");
		writeData(sb.toString().getBytes());
		if (isSocketInUse())
		{
			try
			{
				send();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
