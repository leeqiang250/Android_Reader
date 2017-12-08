package cn.com.pyc.plain.record;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.media.AudioRecord;

/**
 * 录音
 * 
 * @author QiLiKing
 */
public class Record extends RecordBase
{
	private AudioRecord audioRecord;

	/**
	 * 一个实例只能录一个文件
	 */
	public Record(Context context, String filePath)
	{
		super(context, filePath);
	}

	@Override
	protected void start()
	{
		if (audioRecord == null)
		{
			// 缓冲区字节大小
			// RecordParameter.bufferSize = AudioTrack.getMinBufferSize(
			// RecordParameter.sampleRateInHz,
			// RecordParameter.channelConfig, RecordParameter.audioFormat);
			audioRecord = new AudioRecord(RecordParameter.audioSource,
					RecordParameter.sampleRateInHz,
					RecordParameter.channelConfig, RecordParameter.audioFormat,
					BUFFER_SIZE);
			try
			{
				resetFileHead(filePath);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				release();
				if (listener != null)
				{
					listener.onError("创建录音文件失败！");
				}
				return;
			}
		}
		audioRecord.startRecording();
		startTimer();

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					writeDateToFile();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					release();
					if (listener != null)
					{
						listener.onError("录音过程中遇到麻烦！");
					}
				}
			}
		}).start();
	}

	@Override
	protected void pause()
	{
		if (audioRecord != null)
		{
			audioRecord.stop();
			stopTimer();
		}
	}

	@Override
	protected void release()
	{
		if (audioRecord != null)
		{
			pause();
			audioRecord.release();
			audioRecord = null;
		}
	}

	private void writeDateToFile() throws IOException
	{
		final int size = BUFFER_SIZE;
		byte[] buffer = new byte[size];
		FileOutputStream fos = new FileOutputStream(filePath, true);
		while(isWorking())
		{
			int readsize = audioRecord.read(buffer, 0, size);
			if (readsize > 0)
			{
				fos.write(buffer, 0, readsize);
				if (listener != null)
				{
					listener.onVolumeChanged(getVolume(buffer));
				}
			}
		}
		fos.close();
		resetFileHead(filePath);
	}

	private void resetFileHead(String path) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(path, "rw");
		long totalAudioLen = raf.getChannel().size();
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = RecordParameter.sampleRateInHz;
		int channels = 2;
		long byteRate = 16 * longSampleRate * channels / 8;
		byte[] header = new byte[RecordParameter.headSize];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		raf.seek(0);
		raf.write(header);
		raf.close();
	}

	@Override
	protected void seek()
	{
		// TODO Auto-generated method stub

	}
}
