package com.qlk.util.base;

import java.util.Observable;
import java.util.Observer;

import com.qlk.util.global.GlobalObserver;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

/**
 * 默认开启“观察者”模式，不需要可以重写isObserverEnabled()
 * <p>
 * 该类有一个接口：OnChangeFragmentListener，可以瞧瞧。
 * 
 * @author QiLiKing 2015-7-29 上午11:35:51
 */
public class BaseFragment extends Fragment implements Observer
{
	/**
	 * 是否开启“观察者”模式
	 * 
	 * @return
	 */
	protected boolean isObserverEnabled()
	{
		return true;
	}
	
	@Override
	public void onHiddenChanged(boolean hidden)
	{
		super.onHiddenChanged(hidden);
		if (!hidden)
		{
			refreshUI();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (isObserverEnabled())
		{
			GlobalObserver.getGOb().addObserver(this);
		}
	}

	protected void findViewAndSetListeners(View v)
	{
	}
	
	protected void initUI()
	{
	}
	
	public void refreshUI()
	{
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		GlobalObserver.getGOb().deleteObserver(this);
	}

	/**
	 * 观察者的回调方法
	 */
	@Override
	public void update(Observable observable, Object data)
	{

	}

	/**
	 * 切换Fragment
	 * 
	 * @param tag
	 */
	protected void changeFragment(String tag)
	{
		changeFragment(tag, null);
	}

	/**
	 * 切换Fragment，带参数
	 * 
	 * @param tag
	 * @param data
	 *            需要传入下一个Fragment的参数
	 */
	protected void changeFragment(String tag, Bundle data)
	{
		if (getActivity() instanceof OnChangeFragmentListener)
		{
			((OnChangeFragmentListener) getActivity()).onChangeFragment(tag, data);
		}
	}

	/**
	 * 具体用法：宿主Activity实现该接口，Fragment中调用((OnChangeFragmentListener)getActivity()
	 * ).onChangeFragment(String tag, Bundle data);来实现Fragment之间的跳转
	 * <p>
	 * 该文件类BaseFragment已封装了部分功能，具体见changeFragment方法
	 * 
	 * @author QiLiKing 2015-7-29 上午11:40:37
	 */
	public interface OnChangeFragmentListener
	{
		void onChangeFragment(String tag, Bundle data);
	}

}
