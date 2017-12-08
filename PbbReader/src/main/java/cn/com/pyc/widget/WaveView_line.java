package cn.com.pyc.widget;

import java.util.ArrayList;
import java.util.Locale;

import cn.com.pyc.pbb.reader.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class WaveView_line extends View
{
	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Path wavePath = new Path();

	private static final int POINT_SPACE = 4;		// 两个点之间的间隔
	private static final int X_INCREMENT = POINT_SPACE / 2;

	private boolean hasInit = false;
	private int recordProgress;
	private int width;
	private int halfWidth;
	private int height;
	private int halfHeight;
	private int centerLineX;		// 中间线的X轴坐标
	private int translate;		// 原点偏移量

	private boolean isAudition = false;

	private final MyPoint curRecord = new MyPoint();		// 原点
	private final MyPoint curAudition = new MyPoint();		// 原点

	private final ArrayList<Float> ratios = new ArrayList<Float>();
	private int recordMaxShowRatios;
	private int auditionMaxShowRatios;
	private int shownRatios;

	public WaveView_line(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mPaint.setColor(getResources().getColor(R.color.green));
		mPaint.setStyle(Style.FILL);
		mPaint.setTextSize(20);
	}

	public void setCurState(boolean isAudition)
	{
		// 切换状态时才清空
		if (isAudition != this.isAudition)
		{
			this.isAudition = isAudition;
			shownRatios = 0;
			translate = 0;
			wavePath.reset();
		}
	}

	public void updateWave(byte[] buffer)
	{
		updateWave(getVolume(buffer), 0);
	}

	/**
	 * 可以在线程中直接调用
	 * 
	 * @param data
	 * @param progress
	 */
	public void updateWave(float volume, int progress)
	{
		if (!isAudition)
		{
			recordProgress = progress;
			ratios.add(volume);
		}
		caculate();
		postInvalidate();
	}

	private void caculate()
	{
		if (isAudition)
		{
			if (wavePath.isEmpty() || shownRatios > auditionMaxShowRatios
					|| curAudition.x > curRecord.x)
			{
				wavePath.reset();
				restoreAuditionPath();
			}

			// 只要调用了，就坐标+1
			curAudition.x += POINT_SPACE;

			// 左移量
			if (curAudition.x > halfWidth
					&& curAudition.x + halfWidth > curRecord.x)
			{
				translate = halfWidth - curAudition.x;
			}
			// 中间线
			centerLineX = curAudition.x;		// 跟随试听位置移动
		}
		else
		{
			// 只要调用了，就坐标+1
			curRecord.x += POINT_SPACE;

			// record时波纹只显示1/2，所以最大值保持在1即可
			if (wavePath.isEmpty() || shownRatios > recordMaxShowRatios)
			{
				wavePath.reset();
				restoreRecordPath();
			}
			else
			{
				float dy = ratios.get(ratios.size() - 1) * halfHeight;
				getRhombusPath(curRecord, X_INCREMENT, dy);
			}

			// 左移量
			if (curRecord.x > halfWidth)
			{
				translate = halfWidth - curRecord.x;
			}
			// 中间线
			centerLineX = Math.abs(translate) + halfWidth;
		}
	}

	/**
	 * 重新绘制Record，从左绘到中间
	 */
	private void restoreRecordPath()
	{
		MyPoint temp = new MyPoint();
		temp.y = halfHeight;
		int index = (int) ((curRecord.x - halfWidth) / POINT_SPACE);
		if (index < 0)
		{
			index = 0;	// 还没有走到屏幕中间
		}
		temp.x = index * POINT_SPACE;		// 定位起始点
		for (float dy = 0; index < ratios.size(); index++)
		{
			dy = ratios.get(index) * halfHeight;
			getRhombusPath(temp, X_INCREMENT, dy);
			shownRatios++;
			temp.x += POINT_SPACE;
		}
	}

	/**
	 * 重新绘制Audition，从左绘到右
	 */
	private void restoreAuditionPath()
	{
		MyPoint temp = new MyPoint();
		temp.y = halfHeight;
		if (curAudition.x >= curRecord.x)
		{
			curAudition.x = 0;		// 从头试听
		}

		int startPos = (int) ((curAudition.x - halfWidth) / POINT_SPACE);
		if (startPos < 0)
		{
			startPos = 0;	// 还没试听过或者没有走到中间或者又从头开始
		}
		temp.x = startPos * POINT_SPACE;		// 定位temp起始位置
		int endPos = startPos + auditionMaxShowRatios;
		if (endPos >= ratios.size())
		{
			endPos = ratios.size() - 1;
		}
		// 注意，是<=
		for (float dy = 0; startPos <= endPos; startPos++)
		{
			dy = ratios.get(startPos) * halfHeight;
			getRhombusPath(temp, X_INCREMENT, dy);
			shownRatios++;
			temp.x += POINT_SPACE;
		}
	}

	private static int getVolume(byte[] data)
	{
		long total = 0;
		for (int i = 0; i < data.length; i++)
		{
			total += data[i] * data[i];
		}
		// 平方和除以数据总长度，得到音量大小。
		double mean = total / (double) data.length;
		int db = (int) (10 * Math.log10(mean)); // 分贝
		return (db - 20) / 2;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if (!hasInit)
		{
			int w = getWidth() / POINT_SPACE;
			w = w / 2 * 2;	// 这样可以保证halfWidth仍是POINT_SPACE的倍数
			width = w * POINT_SPACE;
			halfWidth = width / 2;

			height = getHeight();
			halfHeight = height / 2;

			int maxScreenRatio = width / POINT_SPACE + 1;
			recordMaxShowRatios = maxScreenRatio;		// n段 n+1个点
			auditionMaxShowRatios = 2 * maxScreenRatio;

			curRecord.y = halfHeight;
			curAudition.y = halfHeight;

			hasInit = true;
		}
		// 平移
		canvas.translate(translate, 0);

		// 中心线
		canvas.drawLine(centerLineX, 0, centerLineX, getHeight(), mPaint);

		// 画频谱
		canvas.drawPath(wavePath, mPaint);

		// 画文字
		if (isAudition && curAudition.smaller(curRecord))
		{
			canvas.drawText(formatTime(), curAudition.x + 10, 20, mPaint);
		}
	}

	private final MyPoint left = new MyPoint();		// 菱形左点
	private final MyPoint top = new MyPoint();		// 菱形上点
	private final MyPoint right = new MyPoint();		// 菱形右点
	private final MyPoint bottom = new MyPoint();	// 菱形下点

	/**
	 * 获得菱形路径
	 * 
	 * @param refer
	 *            参照坐标点：（curX，getHeight()/2）
	 * @param dx
	 *            x增量
	 * @param dy
	 *            y增量
	 */
	private void getRhombusPath(MyPoint refer, int dx, float dy)
	{
		// x坐标右移
		left.x = refer.x - dx;
		top.x = refer.x;
		right.x = refer.x + dx;
		bottom.x = refer.x;

		// y坐标不变
		left.y = curRecord.y;
		top.y = curRecord.y - dy;
		right.y = curRecord.y;
		bottom.y = curRecord.y + dy;

		wavePath.moveTo(left.x, left.y);
		wavePath.lineTo(top.x, top.y);
		wavePath.lineTo(right.x, right.y);
		wavePath.lineTo(bottom.x, bottom.y);
		wavePath.close(); // 使这些点构成封闭的多边形
	}

	private final class MyPoint
	{
		private int x;		// 一定是POINT_SIZE的倍数
		private float y;

		public boolean smaller(MyPoint point)
		{
			return this.x < point.x;
		}
	}

	// 格式化显示的时间
	private String formatTime()
	{
		int progress = (int) (1.0f * curAudition.x / POINT_SPACE
				/ ratios.size() * recordProgress);
		progress /= 1000;
		int minute = progress / 60;
		int hour = minute / 60;
		int second = progress % 60;
		minute %= 60;
		String time = null;
		if (hour > 0)
		{
			time = String.format(Locale.CHINESE, "%02d:%02d:%02d", hour,
					minute, second);
		}
		else
		{
			time = String.format(Locale.CHINESE, "%02d:%02d", minute, second);
		}
		return time;
	}
}
