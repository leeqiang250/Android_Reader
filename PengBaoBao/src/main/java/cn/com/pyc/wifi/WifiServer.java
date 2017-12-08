package cn.com.pyc.wifi;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import cn.com.pyc.global.GlobalData;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/*-
 * 开启两个线程：一个是命令交互；一个是数据传输
 */
public class WifiServer extends Service
{
	private static final int PORT_COMMAND = 55550;
	private static final int PORT_DATA = 55556;
	private static final short COMMAND_SEND = 0;
	private static final short COMMAND_RECEIVE = 1;
	private static final int BUFFER = 1024 * 1024;

	private ServerSocket commandServer;
	private ServerSocket dataServer;
	private InstructionManager manager;
	private short command;
	private String address;
	private Socket usingSokcet;
	private String commandPath;
	private long fileLen;

	@Override
	public void onCreate()
	{
		super.onCreate();
		acceptAsCommandServer();
		acceptAsDataServer();
	}

	private void acceptAsDataServer()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					dataServer = new ServerSocket();
					dataServer.setReuseAddress(true);
					dataServer.setReceiveBufferSize(BUFFER);
					dataServer.bind(new InetSocketAddress(PORT_DATA));
					while(!dataServer.isClosed())
					{
						Socket socket = dataServer.accept();
						if (socket.getInetAddress().getHostAddress().equals(address))
						{
							new DataSocketTask(socket).start();
						}
						else
						{
							socket.close();
						}
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

			}
		}).start();
	}

	private class DataSocketTask extends Thread
	{
		private Socket socket;

		private DataSocketTask(Socket socket)
		{
			this.socket = socket;
		}

		@Override
		public void run()
		{
			if (command == COMMAND_RECEIVE)
			{
				receive(socket, commandPath);
			}
			else
			{
				send(socket, commandPath);
			}
		}
	}

	private void acceptAsCommandServer()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					commandServer = new ServerSocket();
					commandServer.setReuseAddress(true);
					commandServer.bind(new InetSocketAddress(PORT_COMMAND));
					while(!commandServer.isClosed())
					{
						Socket socket = commandServer.accept();
						new CommandSocketTask(socket).start();
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	private class CommandSocketTask extends Thread
	{
		private Socket socket;

		private CommandSocketTask(Socket socket)
		{
			this.socket = socket;
		}

		@Override
		public void run()
		{
			manager = new InstructionManager(WifiServer.this, socket);
			try
			{
				if (manager.startCommand())
				{
					// 当前用户新的连接
					address = socket.getInetAddress().getHostAddress();
					if (usingSokcet != null)
					{
						usingSokcet.close();
					}
					usingSokcet = socket;
					manager.startCommand();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (socket != null)
				{
					try
					{
						socket.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	void export(String path)
	{
		command = COMMAND_SEND;
		commandPath = path;
	}

	void import1(String path, long fileLen2)
	{
		command = COMMAND_RECEIVE;
		commandPath = path;
		fileLen = fileLen2;
	}

	private void receive(Socket socket, String path)
	{
		try
		{
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			byte[] buf = new byte[BUFFER];
			int already = 0;
			FileOutputStream fos = new FileOutputStream(path);
			while(already < fileLen)
			{
				try
				{
					dis.readFully(buf);
					fos.write(buf);
					already += BUFFER;
				}
				catch (EOFException eof)
				{
					eof.printStackTrace();
					int left = (int) (fileLen % BUFFER);
					byte[] leftBuf = new byte[left];
					System.arraycopy(buf, 0, leftBuf, 0, left);
					fos.write(buf, 0, left);
					already += left;
					break;
				}
			}
			fos.close();
			update(path);
			File f = new File(path);
			boolean suc = f.length() == fileLen;
			if (!suc)
			{
				f.delete();
			}
			manager.importSuc(path, suc);	// 接收成功，通知做后续处理
		}
		catch (Exception e)
		{
			e.printStackTrace();
			new File(path).delete();
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void update(String path)
	{
		// pc端已做了类型过滤，所以此处不用判断
		GlobalData.ensure(this, path).getCopyPaths(true).add(path);
		sendBroadcast(new Intent("pyc_refresh"));
	}

	private void send(Socket socket, String path)
	{
		try
		{
			socket.setSendBufferSize(BUFFER);
			OutputStream os = socket.getOutputStream();
			FileInputStream fis = new FileInputStream(path);
			byte[] buf = new byte[BUFFER];
			int real;
			while((real = fis.read(buf)) != -1)
			{
				os.write(buf, 0, real);
				os.flush();
			}
			fis.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		try
		{
			if (commandServer != null)
			{
				commandServer.close();
			}
			if (dataServer != null)
			{
				dataServer.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}
