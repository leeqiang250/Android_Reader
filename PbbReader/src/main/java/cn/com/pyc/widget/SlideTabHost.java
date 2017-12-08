package cn.com.pyc.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 滑动tabHost
 *
 * @author hudq
 */
public class SlideTabHost extends RelativeLayout {
    private static final String TAG = "SlideTabHost";
    //private static final int tabWidgetID = 0x11;
    //private SlideTabWidget mTabWidget;
    private ViewPager mViewPager;
    private List<FrameLayout> mFrameLayoutList = new ArrayList<FrameLayout>();
    private List<GenerateViewListener> mGenerateViewListenerList = new
            ArrayList<GenerateViewListener>();
    private List<Boolean> mHasGeneratedView = new ArrayList<Boolean>();
    private OnTabChangedListener mOnTabChangedListener;

    public void setOnTabChangedListener(OnTabChangedListener listener) {
        mOnTabChangedListener = listener;
    }

    public SlideTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlideTabHost(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setGravity(Gravity.CENTER_HORIZONTAL);
        //setOrientation(LinearLayout.VERTICAL);
    }

    /**
     * 初始化tab和显示的页面
     *
     * @param isSelected
     * @param listener
     */
    public void addTabAndContentGenerateListener(boolean isSelected, GenerateViewListener listener) {
        //update
        //		if (mTabWidget == null)
        //		{
        //			int height = CommonUtil.dip2px(getContext(), 44f);
        //			mTabWidget = new SlideTabWidget(getContext());
        //			mTabWidget.setId(tabWidgetID);
        //			RelativeLayout.LayoutParams params = new LayoutParams(
        //					RelativeLayout.LayoutParams.MATCH_PARENT, height);
        //			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        //			mTabWidget.setLayoutParams(params);
        //			//mTabWidget.setListener(this);
        //
        //			mViewPager = new ViewPager(getContext());
        //			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
        //					RelativeLayout.LayoutParams.MATCH_PARENT,
        //					RelativeLayout.LayoutParams.MATCH_PARENT);
        //			lp.addRule(RelativeLayout.ABOVE, tabWidgetID);
        //			mViewPager.setAdapter(mAdapter);
        //			mViewPager.setOnPageChangeListener(mOnPageChangeListener);
        //			addView(mViewPager, lp);
        //			//addView(mTabWidget);  	//不显示底部导航点
        //			mTabWidget.setVisibility(View.GONE);
        //		}
        if (mViewPager == null) {
            mViewPager = new NoScrollViewPager(getContext());
            mViewPager.setOffscreenPageLimit(3);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            ////lp.addRule(RelativeLayout.ABOVE, tabWidgetID);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setOnPageChangeListener(mOnPageChangeListener);
            addView(mViewPager, lp);
        }

        ////mTabWidget.addTab(isSelected);
        FrameLayout newFrameLayout = new FrameLayout(getContext());
        newFrameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFrameLayoutList.add(newFrameLayout);
        mHasGeneratedView.add(false);
        mGenerateViewListenerList.add(listener);
        if (isSelected) {
            generateView(mFrameLayoutList.size() - 1);
        }

        mAdapter.notifyDataSetChanged();
        if (isSelected) {
            mViewPager.setCurrentItem(mFrameLayoutList.size() - 1);
        }
        Log.v(TAG, "addGenerateView: " + mFrameLayoutList.size());
    }

    private PagerAdapter mAdapter = new PagerAdapter() {

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return mFrameLayoutList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.d(TAG, "destroyItem:" + position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.d(TAG, "instantiateItem:" + position);
            FrameLayout layout = mFrameLayoutList.get(position);
            if (layout.getParent() == null)
                container.addView(layout);
            return layout;
        }
    };

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int arg0) {
            Log.d(TAG, "onPageSelected: " + arg0);
            ////mTabWidget.selectTab(arg0, false);

            FrameLayout layout = mFrameLayoutList.get(arg0);
            if (layout.getChildCount() == 0) {
                generateView(arg0);
                if (mOnTabChangedListener != null) {
                    mOnTabChangedListener.onTabChanged(arg0, true);
                }
            } else {
                layout.requestLayout();
                if (mOnTabChangedListener != null) {
                    mOnTabChangedListener.onTabChanged(arg0, false);
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    //		@Override
    //		public void onTabChanged(int oldIndex, int newIndex)
    //		{
    //			mViewPager.setCurrentItem(newIndex);
    //		}

    /**
     * 获得当前的tabIndex
     *
     * @return
     */
    public int getCurrentTabIndex() {
        return mViewPager.getCurrentItem();
    }

    /**
     * 设置显示的View,在addTabAndContentGenerateListener调用后使用
     *
     * @param newIndex
     */
    public void setCurrentTabIndex(int newIndex) {
        setCurrentTabIndex(newIndex, false);
    }

    /**
     * 设置显示的View，在addTabAndContentGenerateListener调用后使用
     *
     * @param newIndex
     * @param smoothScroll
     */
    public void setCurrentTabIndex(int newIndex, boolean smoothScroll) {
        if (newIndex < 0 || newIndex > mFrameLayoutList.size() - 1)
            throw new IllegalArgumentException("illegal arguments 'newIndex' ");

        mViewPager.setCurrentItem(newIndex, smoothScroll);
    }

    /**
     * 获得当前的tabContent（可能为null）
     *
     * @return
     */
    public View getCurrentTabContentView() {
        return mFrameLayoutList.get(getCurrentTabIndex()).getChildAt(0);
    }

    private void generateView(final int index) {
        if (mHasGeneratedView.get(index)) {
            return;
        }
        mHasGeneratedView.set(index, true);
        postDelayed(new Runnable() {

            @Override
            public void run() {
                GenerateViewListener listener = mGenerateViewListenerList.get(index);
                View view = listener.generateView();
                if (view.getLayoutParams() == null) {
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT);
                    view.setLayoutParams(lp);
                }

                mFrameLayoutList.get(index).addView(view);
            }
        }, 100);
    }

    public interface GenerateViewListener {
        View generateView();
    }

    public interface OnTabChangedListener {
        /**
         * 切换tab
         *
         * @param newTabIndex
         * @param isFirstBuild 是否是第一次显示tab（第一次显示tab的时候，调用生成view的方法）
         */
        void onTabChanged(int newTabIndex, boolean isFirstBuild);
    }
}
