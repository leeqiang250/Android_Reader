package cn.com.pyc.pbbonline;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artifex.mupdfdemo.OutlineItem;
import com.indicators.view.indicator.IndicatorViewPager;
import com.indicators.view.indicator.IndicatorViewPager.IndicatorFragmentPagerAdapter;
import com.indicators.view.indicator.IndicatorViewPager.OnIndicatorPageChangeListener;
import com.indicators.view.indicator.ScrollIndicatorView;
import com.indicators.view.indicator.slidebar.ColorBar;
import com.indicators.view.indicator.transition.OnTransitionTextListener;
import com.sz.mobilesdk.util.DeviceUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.fragment.FragmentPDFBookMark;
import cn.com.pyc.pbbonline.fragment.FragmentPDFOutline;
import cn.com.pyc.pbbonline.util.ViewHelp;

/**
 * 目录，书签home-activity
 *
 * @author hudq
 */
public class MuPDFOutlineHomeActivity extends FragmentActivity {
    private IndicatorViewPager indicatorViewPager;
    private String[] tabname;
    private List<Fragment> muluList = new ArrayList<Fragment>();
    private int tabWidth;
    private MyAdapter adapter;
    /**
     * 当前选中页位置
     */
    private int mCurrentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //CommonUtil.fullScreen(true, this);
        setContentView(R.layout.pbbonline_activity_outline_tab);
        ViewHelp.showAppTintStatusBar(this);
        initTabs();
        getValue();
        initViews();
    }

    private void initViews() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.moretab_viewPager);
        ScrollIndicatorView indicator = (ScrollIndicatorView) findViewById(R.id.moretab_indicator);
        int selectColorId = R.color.tab_top_text_2;
        int unSelectColorId = R.color.tab_top_text_1;
        indicator.setScrollBar(new ColorBar(this, getResources().getColor(selectColorId), 5));
        //indicator.setBackgroundColor(Color.parseColor("#312d33"));
        indicator.setOnTransitionListener(new OnTransitionTextListener().setColorId(this,
                selectColorId, unSelectColorId));
        viewPager.setOffscreenPageLimit(2);
        indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
        adapter = new MyAdapter(getSupportFragmentManager());
        indicatorViewPager.setAdapter(adapter);

        indicatorViewPager.setOnIndicatorPageChangeListener(new OnIndicatorPageChangeListener() {

            @Override
            public void onIndicatorPageChange(int preItem, int currentItem) {
                mCurrentPosition = currentItem;
            }
        });

        findViewById(R.id.back_img).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //UIHelper.finishActivity(MuPDFMuHomeActivity.this);
                finish();
            }
        });
    }

    //	@Override
    //	public void onConfigurationChanged(Configuration newConfig)
    //	{
    //		super.onConfigurationChanged(newConfig);
    //		int mCurrentOrientation = getResources().getConfiguration().orientation;
    //		if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
    //		{
    //			getWidths();
    //			// // ReaderView.mScale = MuPDFActivity.sScaleVertical;
    //			// ReaderView.HORIZONTAL_SCROLLING = true;
    //			adapter.notifyDataSetChanged();
    //		}
    //		else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
    //		{
    //			getWidths();
    //			// // ReaderView.mScale = MuPDFActivity.sScaleHorizontal;
    //			// ReaderView.HORIZONTAL_SCROLLING = false;
    //			adapter.notifyDataSetChanged();
    //		}
    //		indicatorViewPager.setCurrentItem(mCurrentPosition, true);
    //	}

    private void initTabs() {
        Point outSize = DeviceUtil.getScreenSize(this);
        tabWidth = outSize.x / 2;
        String directory = getString(R.string.directory_lable);
        String bookmark = getString(R.string.bookmark_lable);
        tabname = new String[]
                {directory, bookmark};
    }

    private void getValue() {
        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        List<OutlineItem> outline = (ArrayList<OutlineItem>) intent
                .getSerializableExtra("outline_list");
        String titleName = intent.getStringExtra("title_name");
        String contentId = intent.getStringExtra("content_id");

        ((TextView) findViewById(R.id.title_tv)).setText(titleName);
        //目录
        FragmentPDFOutline fol = new FragmentPDFOutline();
        Bundle args = new Bundle();
        args.putSerializable(FragmentPDFOutline.KEY_OUTLINES, (Serializable) outline);
        fol.setArguments(args);
        muluList.add(fol);
        //书签
        FragmentPDFBookMark fbm = new FragmentPDFBookMark();
        Bundle arg_ = new Bundle();
        arg_.putString(FragmentPDFBookMark.KEY_CONTENT_ID, contentId);
        fbm.setArguments(arg_);
        muluList.add(fbm);
    }

    private class MyAdapter extends IndicatorFragmentPagerAdapter {
        LayoutInflater inflater;

        private MyAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            inflater = LayoutInflater.from(getApplicationContext());
        }

        @Override
        public int getCount() {
            return tabname.length;
        }

        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.pbbonline_tab_top, container, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(tabname[position]);
            textView.setWidth(tabWidth);
            return convertView;
        }

        @Override
        public Fragment getFragmentForPage(int position) {
            return muluList.get(position);
        }
    }

    /**
     * 监听Back键按下事件,方法2: 注意: 返回值表示:是否能完全处理该事件 在此处返回false,所以会继续传播该事件.
     * 在具体项目中此处的返回值视情况而定.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
