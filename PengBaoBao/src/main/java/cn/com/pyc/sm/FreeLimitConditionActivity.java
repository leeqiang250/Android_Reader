package cn.com.pyc.sm;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util;
import com.qlk.util.tool.Util.FileUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Observable;

import cn.com.pyc.pbb.R;
import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.sm.calendar.DateWidget;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.user.key.KeyActivity;
import cn.com.pyc.user.key.RollBackKey;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (自由传播)
 * @date 2016/11/30 15:38
 * 1.自由传播
 * 买家只能在卖方预设的可读天数，次数内阅读，也可转发给其他人。卖家可随时终止，延长，缩短预设的阅读条件。
 */
public class FreeLimitConditionActivity extends ExtraBaseActivity implements OnCheckedChangeListener, OnClickListener {
    private static final short REQUEST_START = 0;
    private static final short REQUEST_END = 1;

    private TextView g_txtFileName;

    private EditText g_edtRemark;

    private Button g_btnSure;

    private CheckBox g_cbxCount;
    private EditText g_edtCount;
    private EditText g_edtEmail;
    private EditText g_edtPhone;
    private EditText g_edtQQ;
    private TextView g_txtCountUnlimited;
    private View g_lytCount;

    private CheckBox g_cbxSingle;
    private EditText g_edtSingleTime_minite, g_edtSingleTime_second;
    private TextView g_txtSingleUnlimited;
    private View g_lytSingle;

    private CheckBox g_cbxData;
    private TextView g_txtDataTotal;
    private TextView g_txtDataUnlimited;
    private View g_lytData;
    private Button g_btnStart;
    private Button g_btnEnd;
    private SmInfo smInfo;
    private String filename;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_limit);
        init_value();
        init_view();
        init_listener();
        fillInfo(getIntent());
        showKeyboard();
    }

    private void init_value() {
        filename = Util.getPathFromIntent(this, getIntent(), GlobalIntentKeys.BUNDLE_DATA_PATH);// 获取文件的路径
        smInfo = (SmInfo) getIntent().getSerializableExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);// 获取文件的条件
    }


    /**
    * @author 李巷阳
    * @date 2016/11/30 15:45
    */
    private void init_view() {
        g_cbxCount = ((CheckBox) findViewById(R.id.afl_cbx_count));// CheckBox 能看几次
        g_edtCount = (EditText) findViewById(R.id.afl_edt_count);// EditText 能看几次
        g_txtCountUnlimited = (TextView) findViewById(R.id.afl_txt_count_unlimited);// 能看几次 不限制
        g_lytCount = findViewById(R.id.afl_lyt_count);// 能看几次LinearLayout栏
        g_cbxSingle = (CheckBox) findViewById(R.id.afl_cbx_single_open_time);//  CheckBox 每次能看
        g_edtSingleTime_minite = (EditText) findViewById(R.id.afl_edt_minite);// 每次能看几分钟
        g_edtSingleTime_second = (EditText) findViewById(R.id.afl_edt_second);// 每次能看几秒
        g_txtSingleUnlimited = (TextView) findViewById(R.id.afl_txt_single_unlimited);// 每次能看  不限制
        g_lytSingle = findViewById(R.id.afl_lyt_single_open_time);// 每次能看 LinearLayout栏
        g_cbxData = (CheckBox) findViewById(R.id.afl_cbx_data);// CheckBox 能看多久
        g_txtDataTotal = (TextView) findViewById(R.id.afl_txt_data_total);// 总天数
        g_btnStart = (Button) findViewById(R.id.afl_btn_start);// 能看多久 开始日期
        g_btnEnd = (Button) findViewById(R.id.afl_btn_end);// 能看多久 结束日期
        g_txtDataUnlimited = (TextView) findViewById(R.id.afl_txt_data_unlimited);// 能看多久不限制
        g_lytData = findViewById(R.id.afl_lyt_data);// 能看多久栏
        g_txtFileName = (TextView) findViewById(R.id.afl_txt_name);// 文件名字
        g_edtEmail = (EditText) findViewById(R.id.afl_edt_email);// 邮箱
        g_edtPhone = (EditText) findViewById(R.id.afl_edt_phone);// 手机
        g_edtQQ = (EditText) findViewById(R.id.afl_edt_qq);// QQ
        g_edtRemark = (cn.com.pyc.widget.PycEditText) findViewById(R.id.afl_edt_describe);// 摘要
        g_btnSure = (Button) findViewById(R.id.afl_btn_sure);// 制作
    }
    /**
    * @author 李巷阳
    * @date 2016/11/30 15:45
    */
    private void init_listener() {
        g_cbxCount.setOnCheckedChangeListener(this);
        g_cbxSingle.setOnCheckedChangeListener(this);
        g_cbxData.setOnCheckedChangeListener(this);
        g_btnStart.setOnClickListener(this);
        g_btnEnd.setOnClickListener(this);
        g_btnSure.setOnClickListener(this);
    }

    // 根据intent中的smInfo来初始化界面信息,主要服务于“修改条件”
    /**   
    * @Description: (判断如果是来更新的,就初始化条件选项)
    * @author 李巷阳
    * @date 2016/11/30 16:08 
    */
    private void fillInfo(Intent intent) {
        g_txtFileName.setText(FileUtil.getFileName(filename));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String today = format.format(System.currentTimeMillis());
        g_btnStart.setText(today);
        g_btnEnd.setText(today);

        if (intent != null) {
            if (intent.getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_CHANGE_LIMIT, false)) {
                g_btnSure.setText("更改");
            }
            if (smInfo != null) {
                if (!smInfo.isCountLimit()) {
                    g_cbxCount.setChecked(false);
                } else {
                    g_edtCount.setText(String.valueOf(smInfo.getOpenCount()));
                }
                if (!smInfo.isFreeDataLimit()) {
                    g_cbxData.setChecked(false);
                } else {
                    g_btnStart.setText(smInfo.getStartTime());
                    g_btnEnd.setText(smInfo.getEndTime());
                }

                int singleTime = smInfo.getSingleOpenTime();

                if (singleTime <= 60 && singleTime > 0) {
                    g_edtSingleTime_minite.setText("");
                    g_edtSingleTime_second.setText(singleTime + "");
                } else if (singleTime > 60) {
                    int min = singleTime / 60;
                    int sec = singleTime % 60;

                    g_edtSingleTime_minite.setText(min + "");
                    g_edtSingleTime_second.setText(sec + "");
                } else {
                    g_cbxSingle.setChecked(false); // 0或者不合法数字皆认为是无限制
                }

                String remark = smInfo.getRemark();
                if (!remark.equals(SmInfo.REMARK_DEFAULT)) {
                    g_edtRemark.setText(remark);
                }

                String email = smInfo.getEmail();
                if (!TextUtils.isEmpty(email)) {
                    g_edtEmail.setText(email);
                }

                String phone = smInfo.getPhone();
                if (!TextUtils.isEmpty(phone)) {
                    g_edtPhone.setText(phone);
                }

                String qq = smInfo.getQq();
                if (!TextUtils.isEmpty(qq)) {
                    g_edtQQ.setText(qq);
                }
            }
        }
        calculateTotalDays();
    }

    private void calculateTotalDays() {
        String startTime = g_btnStart.getText().toString();
        String endTime = g_btnEnd.getText().toString();
        long days = DataConvert.transformTime(startTime, endTime, null, null);
        g_txtDataTotal.setText("共" + days + "天");
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 开始时间
            case R.id.afl_btn_start:
                startActivityForResult(new Intent(FreeLimitConditionActivity.this, DateWidget.class), REQUEST_START);
                break;
            // 结束时间
            case R.id.afl_btn_end:
                startActivityForResult(new Intent(FreeLimitConditionActivity.this, DateWidget.class), REQUEST_END);
                break;
            // 制作
            case R.id.afl_btn_sure:
                checkAndCommit();
                break;

            default:
                break;
        }
    }

    /*-
     * 制作的时候对钥匙有要求：
     * 不为空；有昵称
     */
    private boolean isKeyCanMake() {
        UserInfo info = UserDao.getDB(this).getUserInfo();
        // 如果username和userid为空,则进入登陆界面。
        if (info.isKeyNull()) {
            GlobalToast.toastShort(this, "制作需要登录");
            Intent intent = new Intent(this, KeyActivity.class);
            intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_LOGIN);
            startActivity(intent);
            RollBackKey.curRollBackKey = RollBackKey.FromMakeFree;
            return false;
        }
        // 如果昵称为空,就进入名称设置界面。
        if (info.isNickNull()) {
            Intent intent = new Intent(this, KeyActivity.class);
            intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_NICK);
            startActivity(intent);
            RollBackKey.curRollBackKey = RollBackKey.FromMakeFree;
            return false;
        }
        return true;
    }

    // 对限制条件的正确性做排查并提交
    /**
     *
     * @Description: (制作)
     * @author 李巷阳
     * @date 2016/11/30 15:02
     */
    private void checkAndCommit() {
        final boolean numChecked = g_lytCount.getVisibility() == View.VISIBLE;
        final boolean timeChecked = g_lytData.getVisibility() == View.VISIBLE;
        String startTime = g_btnStart.getText().toString();
        String endTime = g_btnEnd.getText().toString();
        String strCount = g_edtCount.getText().toString().trim();
        final String qq = g_edtQQ.getText().toString().trim();
        final String phone = g_edtPhone.getText().toString().trim();
        int count = TextUtils.isEmpty(strCount) ? 0 : Integer.valueOf(strCount);
        boolean correct = true;    // 默认是正确的，和ApplyRightsActivity的区别下
        if (numChecked) {
            if (count <= 0 || count > 9999) {
                correct = false;
                GlobalToast.toastShort(getApplicationContext(), "请输入1-9999之间的整数");
            }
        } else {
            count = 0; // 不限次数
        }

        if (timeChecked) {
            if (DataConvert.transformTime(startTime, endTime, null, null) <= 0) {
                correct = false;
                GlobalToast.toastShort(getApplicationContext(), "结束时间不能早于开始时间");
            }
        } else {
            startTime = ""; // 不限时间
            endTime = "";
        }

        if (!(numChecked || timeChecked)) {
            correct = false;
            GlobalToast.toastShort(getApplicationContext(), "“能看几次”和“能看几天”至少设置一种");
        }

        int singleOpenTime = getSingleOpenTime();
        if (singleOpenTime == -1) {
            correct = false;
        }

        if (TextUtils.isEmpty(qq) && TextUtils.isEmpty(phone)) {
            correct = false;
            GlobalToast.toastShort(getApplicationContext(), "QQ和手机至少填写一种");
        }

        if (correct) {
            SmInfo info = smInfo;
            if (info == null) {
                // 更改条件的话需要保留部分字段，此时info是不为null的
                info = new SmInfo();
            }

            info.setStartTime(startTime);
            info.setEndTime(endTime);
            info.setOpenCount(count);
            info.setSingleOpenTime(singleOpenTime);
            info.setQq(qq);
            info.setPhone(phone);
            info.setRemark(g_edtRemark.getText().toString().trim());    // 这两个为选填项
            info.setEmail(g_edtEmail.getText().toString().trim());
            // 判断是否登陆和是否设置名称
            if (isKeyCanMake()) {
                RollBackKey.curRollBackKey = null;
                Intent i = GlobalIntentKeys.reUseIntent(this, MakeSmFileActivity.class);
                i.putExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO, info);
                startActivity(i);
            }
        }
    }

    /*
     * 这里算出最终的时间，返回值为“秒”，如果不限制则返回0;-1表示需要用户重新输入
     */
    private int getSingleOpenTime() {
        int singleOpenTime = -1; // -1表示数据不正确
        int singleMinite = 0;
        int singleSecond = 0;

        if (g_cbxSingle.isChecked()) {
            String strSingleMinite = g_edtSingleTime_minite.getText().toString().trim();
            String strSingleSecond = g_edtSingleTime_second.getText().toString().trim();

            if (!TextUtils.isEmpty(strSingleMinite)) {
                singleMinite = Integer.parseInt(strSingleMinite);
            } else {
                singleMinite = 0;
            }

            if (!TextUtils.isEmpty(strSingleSecond)) {
                singleSecond = Integer.parseInt(strSingleSecond);
            } else {
                singleSecond = 0;
            }

            if (singleMinite == 0 && singleSecond == 0) {
                GlobalToast.toastShort(getApplicationContext(), "“分”和“秒”不能同时为空！");
            } else if (singleMinite < 0 || singleMinite > 180) {
                GlobalToast.toastShort(getApplicationContext(), "分钟请输入0-180之间的整数");
            } else if (singleSecond < 0 || singleSecond > 60) {
                GlobalToast.toastShort(getApplicationContext(), "秒数请输入0-59之间的整数");
            } else {
                singleOpenTime = singleMinite * 60 + singleSecond;
            }
        } else {
            singleOpenTime = 0;
        }
        return singleOpenTime;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_START:
                    g_btnStart.setText(data.getStringExtra("time"));
                    break;

                case REQUEST_END:
                    g_btnEnd.setText(data.getStringExtra("time"));
                    break;

                default:
                    break;
            }
            calculateTotalDays();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            // 能看几次
            case R.id.afl_cbx_count:
                switchCountVisible(isChecked);
                break;
            // 能看多久
            case R.id.afl_cbx_data:
                switchDataVisible(isChecked);
                break;
            // 每次能看
            case R.id.afl_cbx_single_open_time:
                switchSingleVisible(isChecked);
                break;

            default:
                break;
        }
    }

    private void switchSingleVisible(boolean visible) {
        if (visible) {
            g_lytSingle.setVisibility(View.VISIBLE);
            g_edtSingleTime_minite.requestFocus();
            g_txtSingleUnlimited.setVisibility(View.GONE);
        } else {
            g_lytSingle.setVisibility(View.GONE);
            g_txtSingleUnlimited.setVisibility(View.VISIBLE);
        }
    }

    private void switchDataVisible(boolean visible) {
        if (visible) {
            g_txtDataTotal.setVisibility(View.VISIBLE);
            g_lytData.setVisibility(View.VISIBLE);
            g_txtDataUnlimited.setVisibility(View.GONE);
        } else {
            g_txtDataTotal.setVisibility(View.GONE);
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

    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        switch ((ObTag) data) {
            case Key:
                if (RollBackKey.FromMakeFree.equals(RollBackKey.curRollBackKey)) {
                    checkAndCommit();    // 注意，这里调用方法的全部代码是必要的，因为方法中的sminfo有可能是内部new出来的，不会被存储
                }
                break;
            case Make:
            case ChangeLimit:
                finish();
                break;

            default:
                break;
        }
    }
}
