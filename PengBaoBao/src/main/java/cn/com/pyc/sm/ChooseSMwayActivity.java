package cn.com.pyc.sm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util;

import java.util.Observable;

import cn.com.pyc.pbb.R;
import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.media.MediaFile;
import cn.com.pyc.web.WebActivity;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (传播控制界面)
 * @date 2016/11/30 14:13
 * 功能概述:
 * 1.限制设备手动激活 服务
 * 买家阅读时需要向卖家申请，经过卖家同意并激活后，买家只能在首次打开的设备上阅读，无法进行二次传播。
 * (包括视频，音频，文档)。这里买家在申请激活的时候，会把自己的设备号发送给卖家，卖家点击同意后，方可激活。遵循了我们的设计"一机一码"的原则。
 * 2.自由传播
 * 买家只能在卖方预设的可读天数，次数内阅读，也可转发给其他人。卖家可随时终止，延长，缩短预设的阅读条件。
 * <p>
 * 通过鹏保宝加密制作的文件，可以实现的效果为：
 * 1.限制买家阅读数字内容的次数
 * 2.限制买家阅读数字内容的有效时间
 * 3.随时终止或重开买家的阅读权限
 * 4.文件内容不允许被编辑.被另存
 * 5.文件只能在授权的设备上阅读
 * 6.随时查看并管理买家的阅读时间.阅读次数等。
 * 通过Util.getPathFromIntent(this, getIntent(), GlobalIntentKeys.BUNDLE_DATA_PATH);获取要制作的文件path
 */
public class ChooseSMwayActivity extends ExtraBaseActivity implements OnClickListener {

    private String path;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        init_value();
        init_view();
        init_listener();
    }

    /**
     * @author 李巷阳
     * @date 2016/11/30 14:21
     */
    private void init_value() {
        // 获取发送需要传播的文件路径
        path = Util.getPathFromIntent(this, getIntent(), GlobalIntentKeys.BUNDLE_DATA_PATH);
        MediaFile mediaFile = GlobalData.ensure(this, path);
        // 如果文件不属于其中一类或属于加密文件。则不支持该类型文件外发。
        if (mediaFile == null || mediaFile.equals(GlobalData.Sm)) {
            // 是从第三方文件管理器进来的，要甄别类型
            GlobalToast.toastShort(this, "暂不支持该类型的文件外发");
            finish();
            return;
        }
    }

    /**
     * @author 李巷阳
     * @date 2016/11/30 14:21
     */
    private void init_view() {
        setContentView(R.layout.activity_sm_choose_way);

    }

    /**
     * @author 李巷阳
     * @date 2016/11/30 14:22
     */
    private void init_listener() {
        findViewById(R.id.txt_pay_introduce).setOnClickListener(this);
        findViewById(R.id.btn_hand_activition).setOnClickListener(this);
        findViewById(R.id.btn_hand_free_spread).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 资费介绍网页
            case R.id.txt_pay_introduce:
                startActivity(new Intent(this, WebActivity.class).putExtra(GlobalIntentKeys.BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.ChargeIntroduce));
                break;
            // 手动激活
            case R.id.btn_hand_activition:
                startActivity(GlobalIntentKeys.reUseIntent(this, PayLimitConditionActivity.class));
                break;
            // 自由传播
            case R.id.btn_hand_free_spread:
                startActivity(GlobalIntentKeys.reUseIntent(this, FreeLimitConditionActivity.class));
                break;

            default:
                break;
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        if (data.equals(ObTag.Make)) {
            finish();
        }
    }
}
