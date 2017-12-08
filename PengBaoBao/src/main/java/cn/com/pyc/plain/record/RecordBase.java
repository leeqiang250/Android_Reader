package cn.com.pyc.plain.record;

import java.util.ArrayList;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public abstract class RecordBase extends PlayerBase
{
	protected static final class RecordRatio
	{
		protected static final ArrayList<Float> ratios = new ArrayList<Float>();
		protected static final int TimeSpacing = 200;
	}

	protected static final class RecordParameter
	{
		// 文件头大小
		public static final int headSize = 44;
		// 音频获取源
		public static final int audioSource = MediaRecorder.AudioSource.MIC;
		// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
		public static final int sampleRateInHz = 44100;
		// 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
		public static final int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
		// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
		public static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

		// public static final int bufferSize = 20 * 1024; // 直接计算也可以，但这样更好
	}

	protected static int BUFFER_SIZE;

	// private int maxVolume;

	public RecordBase(Context context, String filePath)
	{
		super(context, filePath);
		BUFFER_SIZE = initBufferSize();
	}

	/*
	 * 有点莫名其妙：同样的参数，同样的底层方法，结果却不一样。 而且，必须以最大值作为统一值才正常。 另外可以用一个较大的值来代替这种比较，比如10kb
	 * 出问题的手机，三星s7568：播放速率是录制的两倍，时间是一半
	 */
	private int initBufferSize()
	{
		int atSize = AudioTrack.getMinBufferSize(
				RecordParameter.sampleRateInHz, RecordParameter.channelConfig,
				RecordParameter.audioFormat);
		int arSize = AudioRecord.getMinBufferSize(
				RecordParameter.sampleRateInHz, RecordParameter.channelConfig,
				RecordParameter.audioFormat);
		return Math.max(atSize, arSize);
	}

	protected float getVolume(byte[] data)
	{
		// final String max = "max_volume";
		// if (maxVolume == 0)
		// {
		// maxVolume = context.getSharedPreferences(this.getClass().getName(),
		// Context.MODE_PRIVATE).getInt(max, 3000);
		// }
		//
		// long total = 0;
		// for (int i = 0; i < data.length; i++)
		// {
		// total += data[i] * data[i];
		// }
		// // 平方和除以数据总长度，得到音量大小。
		// double mean = total / (double) data.length;
		// // float db = (float) (10 * Math.log10(mean)); // 分贝
		// // _SysoXXX.message("db=" + db);
		// // return (db - 20) / 2; // 去除环境噪音
		// if (mean > maxVolume)
		// {
		// maxVolume = (int) mean;
		// SharedPreferences sp = context.getSharedPreferences(this.getClass()
		// .getName(), Context.MODE_PRIVATE);
		// Editor editor = sp.edit();
		// editor.putInt(max, maxVolume);
		// editor.commit();
		// }
		// // _SysoXXX.message("mean=" + mean);
		// return (float) ((mean - 1000) / maxVolume); // 减去500，去除环境噪音

		int maxSample = 0;
		if (RecordParameter.audioFormat == AudioFormat.ENCODING_PCM_16BIT)
		{
			for (int i = 0; i < data.length / 2; i++)
			{ // 16bit sample size
				short curSample = getShort(data[i * 2], data[i * 2 + 1]);
				if (curSample > maxSample)
				{ // Check amplitude
					maxSample = curSample;
				}
			}
		}
		else
		{
			for (int i = 0; i < data.length; i++)
			{ // 16bit sample size
				if (data[i] > maxSample)
				{ // Check amplitude
					maxSample = data[i];
				}
			}
		}
		float vuSize = 1.0f * maxSample / 32768;
		if (vuSize >= 1)
		{
			vuSize = 1.0f;
		}
		return vuSize;
	}

	private short getShort(byte argB1, byte argB2)
	{
		return (short) (argB1 | (argB2 << 8));
	}
}
