package cn.com.pyc.user.key;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.qlk.util.base.BaseApplication;
import com.qlk.util.global.GlobalObserver;
import com.sz.mobilesdk.util.SPUtil;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.event.BaseEvent;
import cn.com.pyc.bean.event.ConductUIEvent;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.main.HomeActivity;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.utils.Dirs;
import de.greenrobot.event.EventBus;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (登陆成功后，跳转此界面)
 * @date 2016/11/17 14:37
 */
public class KeySuccessActivity extends ExtraBaseActivity {
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        if (RollBackKey.curRollBackKey != null) {
            GlobalObserver.getGOb().notifyObservers(ObTag.Key);
            finish();
            return;
        }
        setContentView(R.layout.activity_key_success);
        ViewHelp.showAppTintStatusBar(this);
        findViewById(R.id.aks_btn_continue).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 使用观察者模式finish登陆界面。
                GlobalObserver.getGOb().notifyObservers(ObTag.Key);
                EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_HOME_TAB_3));//通知主界面刷新

               /* for (Activity activity : BaseApplication.ACTIVITIES) {
                    if (activity instanceof HomeActivity) {
                        finish();
                        return;    // PycMainActivity已经建立，则不再start了
                    }
                }*/
                // 把用户信息和硬盘信息写入至本地文件中。
                KeyTool.makeSucFile(KeySuccessActivity.this, 1);
                // 进入主界面。
//                startActivity(new Intent(KeySuccessActivity.this, PycMainActivity.class));
//                startActivity(new Intent(KeySuccessActivity.this, MainActivity.class));
//                startActivity(new Intent(KeySuccessActivity.this, HomeActivity.class));
                finish();
            }
        });

        PbbSP.getGSP(this).putValue(PbbSP.SP_FIRST_LOGIN, false);
        PbbSP.getGSP(this).putValue(PbbSP.SP_FIRST_FUNCTION, false); // 从微信领取钥匙，在进入是不应显示function
        Dirs.reGetCardsPaths(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
