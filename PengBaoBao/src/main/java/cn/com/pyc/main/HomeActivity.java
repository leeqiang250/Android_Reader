package cn.com.pyc.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.qlk.util.base.BaseApplication;
import com.sz.mobilesdk.util.UIHelper;

import java.util.UUID;

import cn.com.pyc.bean.event.BaseEvent;
import cn.com.pyc.bean.event.ConductUIEvent;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.suizhi.SZDiscoverActivity;
import cn.com.pyc.user.PayInfoActivity;
import cn.com.pyc.widget.SlideTabHost;
import de.greenrobot.event.EventBus;

/**
 * Created by 熊大 on 2017/7/10.
 */

public class HomeActivity extends BaseActivityGroup {

    private SlideTabHost mTabHost;
    private ImageView mContentImg;
    private ImageView mDiscoverImg;
    private ImageView mPersonalImg;

    //发送通知:切换到到tab1,2;
    public void onEventMainThread(ConductUIEvent event) {
        if (event.getType() == BaseEvent.Type.UI_HOME_TAB_1) {
            selectTab(TAB_1);
        }
        if (event.getType() == BaseEvent.Type.UI_HOME_TAB_2) {
            selectTab(TAB_2);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initConfig();
        initView();
        initUI();
    }

    private void initConfig() {
        ViewHelp.showAppTintStatusBar(this);
        EventBus.getDefault().register(this);
    }

    private void initView() {
        mTabHost = ((SlideTabHost) findViewById(R.id.home_container));
        mContentImg = ((ImageView) findViewById(R.id.home_menu_content_img));
        mDiscoverImg = ((ImageView) findViewById(R.id.home_menu_discover_img));
        mPersonalImg = ((ImageView) findViewById(R.id.home_menu_personal_img));

        findViewById(R.id.home_menu_content_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTab(TAB_2);
            }
        });

        findViewById(R.id.home_menu_discover_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTab(TAB_1);
            }
        });
        findViewById(R.id.home_menu_personal_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTab(TAB_3);
            }
        });

//        mTabHost.setOnTabChangedListener(new SlideTabHost.OnTabChangedListener() {
//            @Override
//            public void onTabChanged(int newTabIndex, boolean isFirstBuild) {
//                selectTab(newTabIndex);
//            }
//        });
    }

    public void initUI() {
        final Context ctx = HomeActivity.this;
        mTabHost.addTabAndContentGenerateListener(false, new SlideTabHost.GenerateViewListener() {
            @Override
            public View generateView() {
                //Intent intent = new Intent(ctx, WebActivity.class).putExtra(GlobalIntentKeys
                // .BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.Discover);
                Intent intent = new Intent(ctx, SZDiscoverActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return getLocalActivityManager().startActivity(UUID.randomUUID().toString(),
                        intent).getDecorView();
            }
        });
        mTabHost.addTabAndContentGenerateListener(true, new SlideTabHost.GenerateViewListener() {
            @Override
            public View generateView() {
                Intent intent = new Intent(ctx, CodeAndReadActivity2.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return getLocalActivityManager().startActivity(UUID.randomUUID().toString(),
                        intent).getDecorView();
            }
        });
        mTabHost.addTabAndContentGenerateListener(false, new SlideTabHost.GenerateViewListener() {
            @Override
            public View generateView() {
                Intent intent = new Intent(ctx, PayInfoActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return getLocalActivityManager().startActivity(UUID.randomUUID().toString(),
                        intent).getDecorView();
            }
        });
        selectTab(TAB_2);
    }

    private void resetMenuStatus() {
        mContentImg.setSelected(false);
        mDiscoverImg.setSelected(false);
        mPersonalImg.setSelected(false);
    }


    private void selectTab(int tabIndex) {
        resetMenuStatus();
        if (tabIndex == TAB_1) {
            mDiscoverImg.setSelected(true);
        } else if (tabIndex == TAB_2) {
            mContentImg.setSelected(true);
        } else if (tabIndex == TAB_3) {
            mPersonalImg.setSelected(true);
        }
        mTabHost.setCurrentTabIndex(tabIndex);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            UIHelper.showExitTips(HomeActivity.this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((BaseApplication) getApplication()).safeExit();
        EventBus.getDefault().unregister(this);
    }
}
