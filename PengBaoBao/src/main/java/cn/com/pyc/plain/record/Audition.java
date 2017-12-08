package cn.com.pyc.plain.record;

import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * 试听
 * 
 * @author QiLiKing
 */
public class Audition extends RecordBase
{
	private long seek;		// 当前进度（二进制流）

	private AudioTrack audioTrack;

	public Audition(Context context, String filePath)
	{
		super(context, filePath);
	}

	@Override
	protected void start()
	{
		if (audioTrack == null)
		{
			audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					RecordParameter.sampleRateInHz,
					RecordParameter.channelConfig, RecordParameter.audioFormat,
					BUFFER_SIZE, AudioTrack.MODE_STREAM);
			seek = RecordParameter.headSize;		// 初始化
		}
		audioTrack.play();

		final File recordFile = new File(filePath);
		if (seek >= recordFile.length() || seek <= 0)
		{
			seek = RecordParameter.headSize;		// 从头播
			curProgress = 0;
		}	// seek的定位要放在startTimer前面
		startTimer();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					FileInputStream fis = new FileInputStream(recordFile);
					fis.skip(seek);
					byte[] buffer = new byte[BUFFER_SIZE];
					int read = 0;
					while((read = fis.read(buffer)) > 0 && isWorking())
					{
						audioTrack.write(buffer, 0, read);
						seek += read;
						if (listener != null)
						{
							listener.onVolumeChanged(getVolume(buffer));
						}
						// if (listener != null)
						// {
						// listener.onVolumeChanged(getVolume(buffer));
						// }
					}
					// 非人为终止试听
					if (isWorking())
					{
						pause();
					}

					fis.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					release();
					if (listener != null)
					{
						listener.onError("试听过程中遇到麻烦！");
					}
				}
			}
		}).start();
	}

	@Override
	protected void pause()
	{
		if (audioTrack != null)
		{
			audioTrack.stop();
			stopTimer();
		}
	}

	@Override
	protected void release()
	{
		if (audioTrack != null)
		{
			pause();
			audioTrack.release();
			audioTrack = null;
		}
	}

	@Override
	protected void seek()
	{
		// TODO Auto-generated method stub

	}

}
