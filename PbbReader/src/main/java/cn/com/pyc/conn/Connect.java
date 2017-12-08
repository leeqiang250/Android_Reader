package cn.com.pyc.conn;

import android.content.Context;

import com.qlk.util.tool.Util.IOUtil;
import com.qlk.util.tool.Util.NetUtil;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import cn.com.pyc.base.Result;
import cn.com.pyc.bean.ProtocolInfo;
import cn.com.pyc.utils.Constant;

/**
 * 与服务器交互类
 * 输入byte[]，输出byte[]
 *
 * @author QiLiKing 2015-7-29 下午3:09:49
 */
public abstract class Connect
{
	protected Context mContext;

	/**
	 * 服务器业务端口：user是5003；sm是5004
	 */
	protected abstract int getPort(int type);

	public Connect(Context context)
	{
		mContext = context;
	}

	/**
	 * 业务号默认为-1
	 *
	 * @param snd
	 * @param rcv
	 * @return
	 */
	protected int commit(byte[] snd, byte[] rcv)
	{
		return commit(snd, rcv, -1);
	}

	/**
	 * @param snd
	 * @param rcv
	 * @param type
	 *            本次交互的业务号
	 * @return
	 */
	protected int commit(byte[] snd, byte[] rcv, int type)
	{
		if (!NetUtil.isNetInUse(mContext))
		{
			return Result.NET_OFFLINE;
		}
		Socket socket = null;
		OutputStream os = null;
		DataInputStream is = null;
		try
		{
			socket = new Socket();
			InetSocketAddress isa = new InetSocketAddress(Constant.ServerHost,
					getPort(type));
			socket.connect(isa, 6000);
			is = new DataInputStream(socket.getInputStream());
			os = socket.getOutputStream();
			os.write(snd);
			os.flush();
			socket.setSoTimeout(50000);
			is.readFully(rcv);
		}
		catch (EOFException eof)
		{
			// 批量刷新时，服务器返回数据长度<=1448（看ids的个数）
			// 服务器是C++写的，他们有效数据最后一个字符是'\0'，而到了Java读取流时，遇到'\0'就直接返回-1了
			// 所以，即使服务器返回了1448个，客户端读到的仍然是截止到'\0'处。故这里如此解决
			if (type != ProtocolInfo.TYPE_GET_SM_INFOS)
			{
				eof.printStackTrace();
				return Result.NET_ERROR;	// 没有收全，则该数据无效
			}
		}
		catch (IOException e)
		{
            //联网超时,CodeAndReadUI中接收通知
			e.printStackTrace();
			return Result.NET_ERROR;	// 包括联网超时和服务器响应超时
		}
		finally
		{
			IOUtil.close(socket);
			IOUtil.close(is, os);
		}

		return Result.NET_NORMAL;
	}

}
