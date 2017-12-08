package cn.com.pyc.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import cn.com.pyc.pbb.R;

public class LoginEditText extends EditText
{
	private Drawable dRight;
	private Rect rBounds;

	public LoginEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	protected void init()
	{
		setTextColor(getResources().getColor(R.color.black));
		setTextSize(16);
		setHintTextColor(getResources().getColor(R.color.gray_stroke));
		setBackgroundResource(R.drawable.xml_edt_normal1);

		/*dRight = getResources().getDrawable(R.drawable.xml_clear);
		rBounds = dRight.getBounds();*/
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if (!enabled)
		{
			dRight = null;
			rBounds = new Rect();
			setCompoundDrawables(null, null, null, null);
		}
	}

	@Override
	public void setGravity(int gravity)
	{
		super.setGravity(Gravity.CENTER);
	}

	@Override
	public void setPadding(int left, int top, int right, int bottom)
	{
		super.setPadding(10, top, right, bottom);
	}

	/*
	 * 注释部分不要删除(non-Javadoc)
	 * 
	 * @see android.widget.TextView#onSelectionChanged(int, int)
	 */

	// @Override
	// public Parcelable onSaveInstanceState()
	// {
	// System.out.println("onSaveInstanceState");
	// return super.onSaveInstanceState();
	// }
	//
	// @Override
	// public void onRestoreInstanceState(Parcelable state)
	// {
	// System.out.println("onRestoreInstanceState");
	// super.onRestoreInstanceState(state);
	// }
	//
	// @Override
	// public void onEditorAction(int actionCode)
	// {
	// System.out.println("onEditorAction");
	// super.onEditorAction(actionCode);
	// }
	//
	// @Override
	// public boolean onPreDraw()
	// {
	// System.out.println("onPreDraw");
	// return super.onPreDraw();
	// }
	//
	// @Override
	// protected void onAttachedToWindow()
	// {
	// System.out.println("onAttachedToWindow");
	// super.onAttachedToWindow();
	// }
	//
	// @Override
	// protected void onDetachedFromWindow()
	// {
	// System.out.println("onDetachedFromWindow");
	// super.onDetachedFromWindow();
	// }

	// @Override
	// protected void onDraw(Canvas canvas)
	// {
	// System.out.println("onDraw");
	// super.onDraw(canvas);
	// }

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event)
	// {
	// System.out.println("onKeyDown");
	// return super.onKeyDown(keyCode, event);
	// }
	//
	// @Override
	// public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent
	// event)
	// {
	// System.out.println("onKeyMultiple");
	// return super.onKeyMultiple(keyCode, repeatCount, event);
	// }
	//
	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event)
	// {
	// System.out.println("onKeyUp");
	// return super.onKeyUp(keyCode, event);
	// }

	// @Override
	// public boolean onCheckIsTextEditor()
	// {
	// System.out.println("onCheckIsTextEditor");
	// return super.onCheckIsTextEditor();
	// }

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs)
	{
		// System.out.println("onCreateInputConnection");
		setSelection(getText().length());
		return super.onCreateInputConnection(outAttrs);
	}

	//
	// @Override
	// public void onCommitCompletion(CompletionInfo text)
	// {
	// System.out.println("onCommitCompletion");
	// super.onCommitCompletion(text);
	// }
	//
	// @Override
	// public void onBeginBatchEdit()
	// {
	// System.out.println("onBeginBatchEdit");
	// super.onBeginBatchEdit();
	// }
	//
	// @Override
	// public void onEndBatchEdit()
	// {
	// System.out.println("onEndBatchEdit");
	// super.onEndBatchEdit();
	// }
	//
	// @Override
	// public boolean onPrivateIMECommand(String action, Bundle data)
	// {
	// System.out.println("onPrivateIMECommand");
	// return super.onPrivateIMECommand(action, data);
	// }
	//
	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	// {
	// System.out.println("onMeasure");
	// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	// }
	//
	// @Override
	// protected void onSelectionChanged(int selStart, int selEnd)
	// {
	// System.out.println("onSelectionChanged");
	// setSelection(getText().length());
	// // System.out.println(selStart + " " + selEnd);
	// super.onSelectionChanged(selStart, selEnd);
	// }
	//
	// @Override
	// public void onStartTemporaryDetach()
	// {
	// System.out.println("onStartTemporaryDetach");
	// super.onStartTemporaryDetach();
	// }
	//
	// @Override
	// public void onFinishTemporaryDetach()
	// {
	// System.out.println("onFinishTemporaryDetach");
	// super.onFinishTemporaryDetach();
	// }
	//
	// @Override
	// public void onWindowFocusChanged(boolean hasWindowFocus)
	// {
	// System.out.println("onWindowFocusChanged");
	// super.onWindowFocusChanged(hasWindowFocus);
	// }
	//
	// @Override
	// public boolean onTrackballEvent(MotionEvent event)
	// {
	// System.out.println("onTrackballEvent");
	// return super.onTrackballEvent(event);
	// }
	//
	// @Override
	// public boolean onKeyShortcut(int keyCode, KeyEvent event)
	// {
	// System.out.println("onKeyShortcut");
	// return super.onKeyShortcut(keyCode, event);
	// }
	//
	// @Override
	// protected void onCreateContextMenu(ContextMenu menu)
	// {
	// System.out.println("onCreateContextMenu");
	// super.onCreateContextMenu(menu);
	// }
	//
	// @Override
	// public boolean onTextContextMenuItem(int id)
	// {
	// System.out.println("onTextContextMenuItem");
	// return super.onTextContextMenuItem(id);
	// }

	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect)
	{
		if (!focused)
		{
			setCompoundDrawables(null, null, null, null);
		}
		else
		{
			if ("".equals(getText().toString().trim()))
			{
				setCompoundDrawables(null, null, null, null);
			}
			else
			{
				// 注意With...不用设定边框
				setCompoundDrawablesWithIntrinsicBounds(null, null, dRight,
						null);
			}
		}
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int before,
			int after)
	{
		// text不能判断 是不是等于""
		if (!"".equals(getText().toString().trim()) && isFocusable())
		{
			setCompoundDrawablesWithIntrinsicBounds(null, null, dRight, null);
		}
		else
		{
			setCompoundDrawables(null, null, null, null);
		}
		super.onTextChanged(text, start, before, after);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		performClick();
		if (event.getAction() == MotionEvent.ACTION_UP && dRight != null )
		{
			// 从左上角算起
			final int x = (int) event.getX() + getLeft();
			final int y = (int) event.getY() + getTop();
			if (x >= getRight() - rBounds.width() - 20 && x <= getRight()
					&& y >= getTop() && y <= getBottom())
			{
				this.setText("");
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void finalize() throws Throwable
	{
		dRight = null;
		rBounds = null;
		super.finalize();
	}

}
