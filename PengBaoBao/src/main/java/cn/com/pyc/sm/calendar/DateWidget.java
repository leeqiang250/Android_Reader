package cn.com.pyc.sm.calendar;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.com.pyc.pbb.R;

public class DateWidget extends Activity
{
	private ArrayList<DateWidgetDayCell> days = new ArrayList<DateWidgetDayCell>();
	private Calendar calStartDate = Calendar.getInstance();
	private Calendar calToday = Calendar.getInstance();
	private Calendar calCalendar = Calendar.getInstance();
	private Calendar calSelected = Calendar.getInstance();

	LinearLayout layContent = null;
	Button btnToday = null;

	private int iFirstDayOfWeek = Calendar.SUNDAY;
	private int iMonthViewCurrentMonth = 0;
	private int iMonthViewCurrentYear = 0;
	public static final int SELECT_DATE_REQUEST = 111;
	private int iDayCellSize = 50;
	private int iDayHeaderHeight = 80;
	private int iTotalWidth = (iDayCellSize * 7);
	private TextView tv, monthTextView, yearTextView;
	private int mYear;
	private int mMonth;
	private int mDay;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
		iFirstDayOfWeek = Calendar.SUNDAY;
		mYear = calSelected.get(Calendar.YEAR);
		mMonth = calSelected.get(Calendar.MONTH);
		mDay = calSelected.get(Calendar.DAY_OF_MONTH);
		setContentView(generateContentView());
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.activity_data_widget);// 自定义布局赋值
		calStartDate = getCalendarStartDate();
		DateWidgetDayCell daySelected = updateCalendar();
		updateControlsState();
		if (daySelected != null)
			daySelected.requestFocus();
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	private LinearLayout createLayout(int iOrientation)
	{
		LinearLayout lay = new LinearLayout(this);
		lay.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		lay.setOrientation(iOrientation);
		return lay;
	}

	private Button createButton(String sText, int iWidth, int iHeight)
	{
		Button btn = new Button(this);
		btn.setText(sText);
		btn.setLayoutParams(new LayoutParams(iWidth, iHeight));
		return btn;
	}

	private void generateTopButtons(LinearLayout layTopControls)
	{

		final int iSmallButtonWidth = 90;
		btnToday = createButton("", iTotalWidth - iSmallButtonWidth * 4,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

		monthTextView = new TextView(this);
		monthTextView.setPadding(8, 8, 8, 8);
		monthTextView.setText(mYear + "");
		monthTextView.setTextColor(Color.GRAY);
		monthTextView.setShadowLayer(2, 2, 2, Color.GRAY);
		// monthTextView.setWidth(55);
		monthTextView.setSingleLine(true);

		yearTextView = new TextView(this);
		yearTextView.setPadding(20, 8, 8, 8);
		yearTextView.setTextColor(Color.GRAY);
		yearTextView.setShadowLayer(2, 2, 2, Color.GRAY);
		yearTextView.setText(format(mMonth + 1));
		// yearTextView.setWidth(55);
		yearTextView.setSingleLine(true);

		Button btnPrevMonth = new Button(this);
		btnPrevMonth.setLayoutParams(new LayoutParams(iSmallButtonWidth,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		btnPrevMonth.setBackgroundResource(R.drawable.data_last_month);

		Button btnPrevYear = new Button(this);
		btnPrevYear.setLayoutParams(new LayoutParams(iSmallButtonWidth,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		btnPrevYear.setBackgroundResource(R.drawable.data_last_year);

		Button btnNextMonth = new Button(this);
		btnNextMonth.setLayoutParams(new LayoutParams(iSmallButtonWidth,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		btnNextMonth.setBackgroundResource(R.drawable.data_next_month);

		Button btnNextYear = new Button(this);
		btnNextYear.setLayoutParams(new LayoutParams(iSmallButtonWidth,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		btnNextYear.setBackgroundResource(R.drawable.data_next_year);

		// set events
		btnPrevMonth.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View arg0)
			{
				setPrevMonthViewItem();
			}
		});

		btnNextMonth.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View arg0)
			{
				setNextMonthViewItem();
			}
		});

		btnPrevYear.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View arg0)
			{
				setPrevYearViewItem();
			}
		});

		btnNextYear.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View arg0)
			{
				setNextYearViewItem();
			}
		});

		layTopControls.setGravity(Gravity.CENTER_HORIZONTAL);
		layTopControls.addView(btnPrevYear);
		layTopControls.addView(btnPrevMonth);
		layTopControls.addView(monthTextView);
		layTopControls.addView(yearTextView);
		layTopControls.addView(btnNextMonth);
		layTopControls.addView(btnNextYear);
	}

	private View generateContentView()
	{
		LinearLayout layMain = new LinearLayout(this);
		layMain.setLayoutParams(new LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		// layMain.setGravity(Gravity.CENTER);
		layMain.setPadding(8, 8, 8, 8);
		layMain.setOrientation(LinearLayout.VERTICAL);
		layMain.setBackgroundResource(R.drawable.screen_gray);

		LinearLayout layTopControls = new LinearLayout(this);
		layTopControls.setLayoutParams(new LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		layTopControls.setOrientation(LinearLayout.HORIZONTAL);

		layContent = createLayout(LinearLayout.VERTICAL);
		// layContent.setBackgroundResource(R.drawable.style_bkg_screen_gray);
		layContent.setPadding(20, 5, 20, 10);
		layContent.setGravity(Gravity.CENTER_HORIZONTAL);

		layContent.setLayoutParams(new LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		generateTopButtons(layTopControls);
		generateCalendar(layContent);
		layMain.addView(layTopControls);
		layMain.addView(layContent);

		tv = new TextView(this);
		tv.setPadding(20, 0, 20, 0);
		layMain.addView(tv);
		return layMain;
	}

	private View generateCalendarRow()
	{
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		for (int iDay = 0; iDay < 7; iDay++)
		{
			DateWidgetDayCell dayCell = new DateWidgetDayCell(this,
					iDayCellSize, iDayCellSize);
			dayCell.setItemClick(mOnDayCellClick);
			days.add(dayCell);
			layRow.addView(dayCell);
		}
		return layRow;
	}

	private View generateCalendarHeader()
	{
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		iDayCellSize = (width - 40) / 7;
		for (int iDay = 0; iDay < 7; iDay++)
		{
			DateWidgetDayHeader day = new DateWidgetDayHeader(this,
					iDayCellSize, iDayHeaderHeight);
			final int iWeekDay = DayStyle.getWeekDay(iDay, iFirstDayOfWeek);
			day.setData(iWeekDay);
			layRow.addView(day);
		}

		return layRow;
	}

	private void generateCalendar(LinearLayout layContent)
	{
		layContent.addView(generateCalendarHeader());
		days.clear();
		for (int iRow = 0; iRow < 6; iRow++)
		{
			layContent.addView(generateCalendarRow());
		}
	}

	private Calendar getCalendarStartDate()
	{
		calToday.setTimeInMillis(System.currentTimeMillis());
		calToday.setFirstDayOfWeek(iFirstDayOfWeek);

		if (calSelected.getTimeInMillis() == 0)
		{
			calStartDate.setTimeInMillis(System.currentTimeMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		}
		else
		{
			calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		}
		updateStartDateForMonth();

		return calStartDate;
	}

	private DateWidgetDayCell updateCalendar()
	{
		DateWidgetDayCell daySelected = null;
		boolean bSelected = false;
		final boolean bIsSelection = (calSelected.getTimeInMillis() != 0);
		final int iSelectedYear = calSelected.get(Calendar.YEAR);
		final int iSelectedMonth = calSelected.get(Calendar.MONTH);
		final int iSelectedDay = calSelected.get(Calendar.DAY_OF_MONTH);
		calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());
		for (int i = 0; i < days.size(); i++)
		{
			final int iYear = calCalendar.get(Calendar.YEAR);
			final int iMonth = calCalendar.get(Calendar.MONTH);
			final int iDay = calCalendar.get(Calendar.DAY_OF_MONTH);
			final int iDayOfWeek = calCalendar.get(Calendar.DAY_OF_WEEK);
			DateWidgetDayCell dayCell = days.get(i);
			// check today
			boolean bToday = false;
			if (calToday.get(Calendar.YEAR) == iYear)
				if (calToday.get(Calendar.MONTH) == iMonth)
					if (calToday.get(Calendar.DAY_OF_MONTH) == iDay)
						bToday = true;
			// check holiday
			boolean bHoliday = false;
			if ((iDayOfWeek == Calendar.SATURDAY)
					|| (iDayOfWeek == Calendar.SUNDAY))
				bHoliday = true;
			if ((iMonth == Calendar.JANUARY) && (iDay == 1))
				bHoliday = true;

			dayCell.setData(iYear, iMonth, iDay, bToday, bHoliday,
					iMonthViewCurrentMonth, iDayOfWeek);
			bSelected = false;
			if (bIsSelection)
				if ((iSelectedDay == iDay) && (iSelectedMonth == iMonth)
						&& (iSelectedYear == iYear))
				{
					bSelected = true;
				}
			dayCell.setSelected(bSelected);
			if (bSelected)
				daySelected = dayCell;
			calCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		layContent.invalidate();
		return daySelected;
	}

	private void updateStartDateForMonth()
	{
		iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);
		iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		UpdateCurrentMonthDisplay();
		// update days for week
		int iDay = 0;
		int iStartDay = iFirstDayOfWeek;
		if (iStartDay == Calendar.MONDAY)
		{
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
			if (iDay < 0)
				iDay = 6;
		}
		if (iStartDay == Calendar.SUNDAY)
		{
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
			if (iDay < 0)
				iDay = 6;
		}
		calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
	}

	private void UpdateCurrentMonthDisplay()
	{
		String s = calCalendar.get(Calendar.YEAR) + "/"
				+ (calCalendar.get(Calendar.MONTH) + 1);// dateMonth.format(calCalendar.getTime());
		btnToday.setText(s);
		mYear = calCalendar.get(Calendar.YEAR);
	}

	private void setPrevMonthViewItem()
	{
		iMonthViewCurrentMonth--;
		if (iMonthViewCurrentMonth == -1)
		{
			iMonthViewCurrentMonth = 11;
			iMonthViewCurrentYear--;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		updateDate();
		updateCenterTextView(iMonthViewCurrentMonth, iMonthViewCurrentYear);
	}

	private void setNextMonthViewItem()
	{
		iMonthViewCurrentMonth++;
		if (iMonthViewCurrentMonth == 12)
		{
			iMonthViewCurrentMonth = 0;
			iMonthViewCurrentYear++;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		updateDate();
		updateCenterTextView(iMonthViewCurrentMonth, iMonthViewCurrentYear);
	}

	private void setPrevYearViewItem()
	{
		iMonthViewCurrentYear--;
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		updateDate();
		updateCenterTextView(iMonthViewCurrentMonth, iMonthViewCurrentYear);
	}

	private void setNextYearViewItem()
	{
		iMonthViewCurrentYear++;
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		updateDate();
		updateCenterTextView(iMonthViewCurrentMonth, iMonthViewCurrentYear);
	}

	private DateWidgetDayCell.OnItemClick mOnDayCellClick = new DateWidgetDayCell.OnItemClick()
	{
		public void OnClick(DateWidgetDayCell item)
		{
			calSelected.setTimeInMillis(item.getDate().getTimeInMillis());
			item.setSelected(true);
			updateCalendar();
			updateControlsStateByClick();
		}
	};

	private void updateCenterTextView(int iMonthViewCurrentMonth,
			int iMonthViewCurrentYear)
	{
		monthTextView.setText(iMonthViewCurrentYear + "");
		yearTextView.setText(format(iMonthViewCurrentMonth + 1) + "");
	}

	private void updateDate()
	{
		updateStartDateForMonth();
		updateCalendar();
	}

	private void updateControlsState()
	{
		mYear = calSelected.get(Calendar.YEAR);
		mMonth = calSelected.get(Calendar.MONTH);
		mDay = calSelected.get(Calendar.DAY_OF_MONTH);
		tv.setText("您当前的日期是："
				+ new StringBuilder().append(mYear).append("-")
						.append(format(mMonth + 1)).append("-")
						.append(format(mDay)));
		tv.setTextColor(Color.GRAY);
		tv.setShadowLayer(3, 3, 3, Color.GRAY);
		tv.setHorizontallyScrolling(true);
	}

	private void updateControlsStateByClick()
	{
		mYear = calSelected.get(Calendar.YEAR);
		mMonth = calSelected.get(Calendar.MONTH);
		mDay = calSelected.get(Calendar.DAY_OF_MONTH);
		tv.setText("您当前的日期是："
				+ new StringBuilder().append(mYear).append("-")
						.append(format(mMonth + 1)).append("-")
						.append(format(mDay)));
		tv.setTextColor(Color.GRAY);
		tv.setShadowLayer(3, 3, 3, Color.GRAY);
		tv.setHorizontallyScrolling(true);

		System.out.println("sdfdsfdsfdsagdsgdgsgdsgdsgds");
		setResult(
				RESULT_OK,
				new Intent().putExtra("time", mYear + "-" + format(mMonth + 1)
						+ "-" + format(mDay)));
		finish();
	}

	private String format(int x)
	{
		String s = "" + x;
		if (s.length() == 1)
			s = "0" + s;
		return s;
	}
}
