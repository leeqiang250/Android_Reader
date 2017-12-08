package cn.com.pyc.sm;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util;
import com.qlk.util.tool.Util.FileUtil;

import java.util.Observable;

import cn.com.pyc.pbb.R;
import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.media.MediaFile;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.user.key.KeyActivity;
import cn.com.pyc.user.key.RollBackKey;
import cn.com.pyc.widget.LimitEditText;
import cn.com.pyc.widget.PycEditText;


/*-
 * 和FreeLimitConditionActivity差不多，就不赘述了
 */

/**
 * @author 李巷阳
 * @Description: (手动激活界面)
 * @date 2016/11/30 14:32
 * 1.限制设备手动激活 服务
 * 买家阅读时需要向卖家申请，经过卖家同意并激活后，买家只能在首次打开的设备上阅读，无法进行二次传播。
 * (包括视频，音频，文档)。这里买家在申请激活的时候，会把自己的设备号发送给卖家，卖家点击同意后，方可激活。遵循了我们的设计"一机一码"的原则。
 * Util.getPathFromIntent(this, getIntent(), GlobalIntentKeys.BUNDLE_DATA_PATH) 获取要制作的文件的path
 *
 *
 */
public class PayLimitConditionActivity extends ExtraBaseActivity implements OnCheckedChangeListener {

    //要制作文件的路径
    private String path;

    private TextView g_txtFileName;

    private CheckBox g_cbxCount;
    private EditText g_edtCount;
    private TextView g_txtCountUnlimited;
    private View g_lytCount;

    private CheckBox g_cbxData;
    private TextView g_txtDataUnlimited;
    private EditText g_edtData;
    private View g_lytData;

    private Spinner g_spnData;
    private String[] units = {"天", "年"};

    private PycEditText g_edtQQ, g_edtEmail, g_edtPhone;
    private LimitEditText g_describe;

    private TextView et_day;
    private TextView et_count;
    private SeekBar count;
    private SeekBar daycount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_limit);
        ViewHelp.showAppTintStatusBar(this);

        init_value();
        init_view();
        init_listener();
//        showKeyboard();
    }

    private void init_value() {
        // 获取发送需要传播的文件路径
        path = Util.getPathFromIntent(this, getIntent(), GlobalIntentKeys.BUNDLE_DATA_PATH);
        MediaFile mediaFile = GlobalData.ensure(this, path);
        // 如果文件不属于其中一类或属于加密文件。则不支持该类型文件外发。
        if (mediaFile == null || mediaFile.equals(GlobalData.Sm)) {
            // 是从第三方文件管理器进来的，要甄别类型
            GlobalToast.toastShort(this, "暂不支持该类型的文件外发");
            finish();
            //return;
        }
    }


    /**
     * @author 李巷阳
     * @date 2016/11/30 14:47
     */
    private void init_view() {
        g_cbxCount = ((CheckBox) findViewById(R.id.apl_cbx_count));// CheckBox 能看几次
        g_edtCount = (EditText) findViewById(R.id.apl_edt_count);// EditText 能看几次
        g_txtCountUnlimited = (TextView) findViewById(R.id.apl_txt_count_unlimited);// 能看几次 不限制
        g_lytCount = findViewById(R.id.apl_lyt_count);// 能看几次LinearLayout栏
        g_cbxData = (CheckBox) findViewById(R.id.apl_cbx_data);// CheckBox 能看多久
        g_edtData = (EditText) findViewById(R.id.apl_edt_data); // EditText 能看多久
        g_lytData = findViewById(R.id.apl_lyt_data);// 能看多久 LinearLayout栏
        g_txtDataUnlimited = (TextView) findViewById(R.id.apl_txt_data_unlimited);// 能看多久 不限制
        g_txtFileName = (TextView) findViewById(R.id.apl_txt_name); // 文件名字
        g_spnData = (Spinner) findViewById(R.id.apl_spn_data);// 能看多少天,下拉框
        g_edtQQ = (PycEditText) findViewById(R.id.apl_edt_QQ);// QQ
        g_edtPhone = (PycEditText) findViewById(R.id.apl_edt_phone);// 手机号
        g_edtEmail = (PycEditText) findViewById(R.id.apl_edt_email);// 邮箱
        g_describe = (LimitEditText) findViewById(R.id.apl_edt_describe);// 摘要

        et_day = (TextView) findViewById(R.id.et_day);
        et_count = (TextView) findViewById(R.id.et_count);
        daycount = (SeekBar) findViewById(R.id.sb_day);
        count = (SeekBar) findViewById(R.id.sb_count);

        et_count.setInputType(InputType.TYPE_NULL);
        et_day.setInputType(InputType.TYPE_NULL);
//        et_count.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
//        et_day.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        // 能看多久 设置天数
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, units);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_item);
        g_spnData.setAdapter(ad);
        // 设置文件名字
        g_txtFileName.setText(FileUtil.getFileName(Util.getPathFromIntent(this, getIntent(), GlobalIntentKeys.BUNDLE_DATA_PATH)));

    }


    /**
     * @author 李巷阳
     * @date 2016/11/30 14:47
     */
    private void init_listener() {
        g_cbxCount.setOnCheckedChangeListener(this);
        g_cbxData.setOnCheckedChangeListener(this);

        daycount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,  int progress, boolean fromUser) {

                progress++;
                if (progress == 51) {
                    et_day.setText("无限");
                }else {
                    et_day.setText(" "+progress+" ");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        count.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,  int progress, boolean fromUser) {

                progress++;
                if (progress == 51) {
                    et_count.setText("无限");
                }else {
                    et_count.setText(" "+progress+" ");
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // 制作
        findViewById(R.id.apl_btn_sure).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //新业务
                checkCorrect();

                //旧业务
//                checkInputCorrect();
            }
        });
    }


    private void checkCorrect(){
        String days = et_day.getText().toString().trim();
        String counts = et_count.getText().toString().trim();

        if (days.equals("0")||counts.equals("0")||days.equals("") || counts.equals("")) {
//            startActivity(new Intent(PayLimitConditionActivity.this, PycMainActivity.class));
            Toast.makeText(PayLimitConditionActivity.this,"请输入天数和次数",Toast.LENGTH_SHORT).show();
            return;
        }

        int dayss;
        int countss;

        if (days.equals("无限")){
            dayss =0;
        }else {
            dayss = Integer.valueOf(days);
        }

        if (counts.equals("无限")){
            countss = 0;
        }else{
            countss = Integer.valueOf(counts);
        }

//        dayss = TextUtils.isEmpty(days) ? 0 : Integer.valueOf(days);
//        countss = TextUtils.isEmpty(counts) ? 0 : Integer.valueOf(counts);

        if (countss < 0 || countss > 999) {
            GlobalToast.toastShort(getApplicationContext(), "请输入1~999之间的整数");
            return;
        }

        if (dayss < 0 || dayss > 999) {
            GlobalToast.toastShort(getApplicationContext(), "请输入1~999之间的整数");
            return;
        }

        commit(dayss,countss);
    }

    private void commit(int days,int counts){
        // 获取发送的对象
        SmInfo info = (SmInfo) getIntent().getSerializableExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);
        if (info == null) {
            // 更改条件的话需要保留部分字段
            info = new SmInfo();
        }
        info.setOpenCount(counts);
        info.setDays(days);
        info.setYears(0);
        info.setRemark("");
        info.setQq("");
        info.setEmail("");
        info.setPhone("");

//        info.setOpenCount(counts);
//        info.setDays(days);
//        info.setYears(0);
//        info.setRemark(g_describe.getText().toString().trim());
//        info.setQq(g_edtQQ.getText().toString().trim());
//        info.setEmail(g_edtEmail.getText().toString().trim());
//        info.setPhone(g_edtPhone.getText().toString().trim());
        // 判断是否登陆和是否设置名称
        if (isKeyCanMake()) {
            RollBackKey.curRollBackKey = null;
//            Toast.makeText(PayLimitConditionActivity.this,"days = "+days+" counts = "+ counts,Toast.LENGTH_SHORT).show();
            Intent i = GlobalIntentKeys.reUseIntent(this, MakeSmFileActivity.class);
            i.putExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO, info);// 传递设置的文件条件
            i.putExtra(GlobalIntentKeys.BUNDLE_FLAG_PAY_MODE, true);// 制作
            startActivity(i);
        }
    }

    /**
    *
    * @Description: (制作)
    * @author 李巷阳
    * @date 2016/11/30 15:02 
    */
    private void checkInputCorrect() {
        // 能看几次栏
        final boolean numChecked = g_lytCount.getVisibility() == View.VISIBLE;
        // 能看多久栏
        final boolean timeChecked = g_lytData.getVisibility() == View.VISIBLE;
        // 获取能看几次
        String strCount = g_edtCount.getText().toString().trim();
        int count = TextUtils.isEmpty(strCount) ? 0 : Integer.valueOf(strCount);
        // 获取能看多久
        String strDays = g_edtData.getText().toString().trim();
        int days = TextUtils.isEmpty(strDays) ? 0 : Integer.valueOf(strDays);
        int years = days;
        boolean correct = true;
        if (numChecked) {
            if (count <= 0 || count > 9999) {
                correct = false;
                GlobalToast.toastShort(getApplicationContext(), "请输入1~9999之间的整数");
            }
        } else {
            count = 0; // 不限次数
        }

        if (timeChecked) {
            if (days <= 0) {
                correct = false;
                GlobalToast.toastShort(getApplicationContext(), "能看多久 不能为负值");
            }
            if (g_spnData.getSelectedItem().toString().equals("天")) {
                years = 0;
            } else {
                days = 0;
            }

            if (days > 999 || years > 999) {
                correct = false;
                GlobalToast.toastShort(getApplicationContext(), "请输入1-999之间的整数");
            }
        } else {
            days = 0;
            years = 0;
        }
        // 判断
        if (g_edtQQ.getText().toString().trim().equals("") && g_edtPhone.getText().toString().trim().equals("")) {
            correct = false;
//            GlobalToast.toastShort(getApplicationContext(), "QQ和手机至少填写一种");
        }
        // 数据没错,进行发送。
        if (correct) {
            commitLimit(count, days, years);
        }

    }
    /**   
    * @Description: (准备发送)
    * @author 李巷阳
    * @date 2016/11/30 15:06 
    */
    private void commitLimit(int count, int days, int years) {
        // 获取发送的对象
        SmInfo info = (SmInfo) getIntent().getSerializableExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);
        if (info == null) {
            // 更改条件的话需要保留部分字段
            info = new SmInfo();
        }
        info.setOpenCount(count);
        info.setDays(days);
        info.setYears(years);
        info.setRemark(g_describe.getText().toString().trim());
        info.setQq(g_edtQQ.getText().toString().trim());
        info.setEmail(g_edtEmail.getText().toString().trim());
        info.setPhone(g_edtPhone.getText().toString().trim());
        // 判断是否登陆和是否设置名称
        if (isKeyCanMake()) {
            RollBackKey.curRollBackKey = null;
            Intent i = GlobalIntentKeys.reUseIntent(this, MakeSmFileActivity.class);
            i.putExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO, info);// 传递设置的文件条件
            i.putExtra(GlobalIntentKeys.BUNDLE_FLAG_PAY_MODE, true);// 制作
            startActivity(i);
        }
    }

    /*-
     * 制作的时候对钥匙有要求：
     * 不为空；有昵称
     */
    private boolean isKeyCanMake() {
        UserInfo info = UserDao.getDB(this).getUserInfo();
        if(info == null) return false;
        // 如果username和userid为空,则进入登陆界面。
        if (info.isKeyNull()) {
            GlobalToast.toastShort(this, "制作需要登录");
            Intent intent = new Intent(this, KeyActivity.class);
            intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_LOGIN);
            startActivity(intent);
            RollBackKey.curRollBackKey = RollBackKey.FromMakePay;
            return false;
        }
        // 如果昵称为空,就进入名称设置界面。
        if (info.isNickNull()) {
            Intent intent = new Intent(this, KeyActivity.class);
            intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_NICK);
            startActivity(intent);
            RollBackKey.curRollBackKey = RollBackKey.FromMakePay;
            return false;
        }
        return true;
    }

    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        switch ((ObTag) data) {
            case Key:
                if (RollBackKey.FromMakePay.equals(RollBackKey.curRollBackKey)) {
                    checkInputCorrect();    // 注意，这里调用方法的全部代码是必要的，因为方法中的sminfo有可能是内部new出来的，不会被存储
                }
                break;

            case Make:
                finish();
                break;

            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.apl_cbx_count:
                switchCountVisible(isChecked);
                break;

            case R.id.apl_cbx_data:
                switchDataVisible(isChecked);
                break;

            default:
                break;
        }
    }

    private void switchDataVisible(boolean visible) {
        if (visible) {
            g_lytData.setVisibility(View.VISIBLE);
            g_txtDataUnlimited.setVisibility(View.GONE);
        } else {
            g_lytData.setVisibility(View.GONE);
            g_txtDataUnlimited.setVisibility(View.VISIBLE);
        }
    }

    private void switchCountVisible(boolean visible) {
        if (visible) {
            g_lytCount.setVisibility(View.VISIBLE);
            g_edtCount.requestFocus();
            g_txtCountUnlimited.setVisibility(View.GONE);
        } else {
            g_lytCount.setVisibility(View.GONE);
            g_txtCountUnlimited.setVisibility(View.VISIBLE);
        }
    }

}
