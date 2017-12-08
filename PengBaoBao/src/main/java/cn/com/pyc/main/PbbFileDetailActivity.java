package cn.com.pyc.main;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.qlk.util.base.BaseActivity;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.ColorText;
import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util;
import com.sz.mobilesdk.util.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.bean.event.SeriesPbbFileEvent;
import cn.com.pyc.conn.SmConnect;
import cn.com.pyc.db.SerisesDao;
import cn.com.pyc.db.sm.SmDao;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.receive.TokenInfo;
import cn.com.pyc.sm.SmReaderActivity;
import cn.com.pyc.widget.PycUnderLineOrangeTextView;
import cn.com.pyc.widget.PycUnderLineTextView;
import cn.com.pyc.xcoder.XCoder;
import cn.com.pyc.xcoder.XCoderResult;
import de.greenrobot.event.EventBus;


/**
 * by 熊
 * <p>
 * 混合pbb,suizhi,series后 pbb文件'详细信息'界面（原来条目展开后的界面）
 * <p>
 * 文件详情显示说明：
 * <p>
 * 1，一定要显示的数据：
 * 1.文件制作时间：txtMakeTime（info.getMakeTime()）
 * 2.作者：txtMaker（info.getNick()）
 * 3.作者说明信息：txtRemark(info.getRemark())
 * 4.文件次数信息：txtCounts(info.getOpenCount(),info.getLeftCount())
 * a,如果有次数限制：info.isCountLimit()
 * 1：如果已激活：显示"能看几次：剩余n次，共m次" (info.isNeedApply())
 * 2：如果未激活：显示"能看几次：共m次"
 * b,如果没有次数限制：显示"不限制"
 * 5.文件时间信息：txtDays
 * a,如果是付费文件：info.isPayDataLimit()
 * 1：已激活文件：(info.isNeedApply())
 * a,如果文件版本是 4：info.getFileVersion()
 * 显示的是开始时间和结束时间
 * b,如果文件版本是 2：
 * 显示的是天数或者年数
 * 2：未激活文件：
 * 显示的是"共n天 或者 共n年"
 * <p>
 * b,如果是自由传播文件：info.isFreeDataLimit()
 * 显示相应的时间数
 * 剩余时间的换算在SmInfo类：例：getFreeLeftDays()得到剩余天数
 * <p>
 * <p>
 * 2，分情况显示的数据：
 * 1.每次能看时间：tv_singleTime (info.getSingleOpenTime())
 * 此条只有在数值不为空的时候才显示
 * 2.订单编号：tv_order （info.getOrderNo()）
 * 此条只有在数值不为空的时候才显示
 * 3.系列名称：txtSeriseName （info.getSeriesName()）
 * 此条只有在数值不为空的时候才显示
 * <p>
 * <p>
 * PS：文件字段信息具体请到SmInfo里面查看，每个字段都有相应注释说明
 */

public class PbbFileDetailActivity extends BaseActivity {

    private static final String TAG = "pfd";
    private static final String testID = "pbbandroid0";
    private static final String testPSD = "84n109f3";
    private SmInfo smInfo;
    private String filePath;//文件路径
    private String seriesName;//文件系列名

    private TextView txtMakeTime;//制作时间
    private TextView txtSeriesName;//系列名
    private TextView txtMaker;//制作者
    private TextView txtRemark;//制作者留言信息
    private Button btnRead;//阅读按钮
    private ImageButton imbRefresh;//刷新按钮
    private PycUnderLineOrangeTextView txtShop;//店铺链接按钮
    private SmConnect mSmConnect;

    private TextView txtTitle;//标题
    private View mContentView;
    private View mOperationView;
    private View mEmptyView;
    private TextView mEmptyText;
    private TextView tv_count;//文件阅读次数
    private TextView tv_days;//文件阅读天数
    private TextView tv_days_title;//"能看多久："字样
    private TextView tv_order;//订单编号
    private TextView tv_order_title;//订单编号前面显示的文字
    private TextView tv_singleTime;//每次阅读时间

    private ColorText mNumColor;//控制文本显示颜色

    private ExecHandler mHandler = new ExecHandler(this);

    private static class ExecHandler extends Handler {
        private WeakReference<PbbFileDetailActivity> reference;

        private ExecHandler(PbbFileDetailActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PbbFileDetailActivity activity = reference.get();
            if (activity == null) return;
            activity.hideBgLoading();
            if (msg.what == 0) {
                activity.mContentView.setVisibility(View.VISIBLE);
                activity.mOperationView.setVisibility(View.VISIBLE);
                activity.checkSeries(activity.smInfo);
                activity.initInfo(activity.smInfo, activity.filePath);
            }
            if (msg.what == -1) {
                activity.mEmptyText.setText("文件解析出现错误！");
                activity.mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pbb_file_details);
        ViewHelp.showAppTintStatusBar(this);
        filePath = getIntent().getStringExtra("pbb_path");
        seriesName = getIntent().getStringExtra("pbb_series_name");
        if (filePath == null) {
            finish();
            return;
        }
        mSmConnect = new SmConnect(this);
        int green = getResources().getColor(R.color.green);
        mNumColor = new ColorText("0123456789-.", green);

        findViewAndSetListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData(filePath, false);
    }

    //刷新数据
    public void refreshData(final String path, final boolean fromUser) {
        if (path == null) return;
        showBgLoading(this);
        Runnable netTask = new Runnable() {
            @Override
            public void run() {
                XCoderResult xr = XCoder.analysisSmFile(path);
                if (xr.succeed()) {
                    smInfo = xr.getSmInfo();
                } else {
                    if (fromUser) {
                        xr.showResult(PbbFileDetailActivity.this);
                    }
                    mHandler.sendEmptyMessage(-1);
                    return;
                }
                if (smInfo != null && smInfo.getFid() > 0) {
                    SmDao.getInstance(PbbFileDetailActivity.this, true).query(smInfo);
                    mSmConnect.getFileInfo(smInfo, false, fromUser, true);
                    //此处也更新或添加一下系列名
                    SerisesDao.getInstance().updateOrInsert(smInfo);
                    //读取数据库一下,主要是次数更新了
                    //SmDao.getInstance(PbbFileDetailActivity.this, true).query(info);
                    mHandler.sendEmptyMessage(0);
                } else {
                    mHandler.sendEmptyMessage(-1);
                }
            }
        };
        GlobalTask.executeBackground(netTask);
//        if (fromUser) {
//            GlobalTask.executeNormalTask(this, netTask);
//        } else {
//            GlobalTask.executeBackground(netTask);
//        }
    }

    //检查系列是否存在
    private void checkSeries(SmInfo smInfo) {
        if (TextUtils.isEmpty(seriesName) && !TextUtils.isEmpty(smInfo.getSeriesName())) {
            EventBus.getDefault().post(new SeriesPbbFileEvent(smInfo));
            GlobalToast.toastLong(this, "文件已迁移至系列《" + smInfo.getSeriesName() + "》中");
            seriesName = smInfo.getSeriesName();
        }
    }

    public void findViewAndSetListeners() {
        mContentView = findViewById(R.id.apfd_lyt_info);
        mContentView.setVisibility(View.GONE);
        mOperationView = findViewById(R.id.apfd_layout_operation);
        mOperationView.setVisibility(View.GONE);
        mEmptyView = findViewById(R.id.vep_lyt_empty);
        mEmptyView.setVisibility(View.GONE);
        mEmptyText = ((TextView) mEmptyView.findViewById(R.id.vep_txt_prompt));
        txtTitle = (TextView) findViewById(R.id.title_tv);

        txtMakeTime = (TextView) findViewById(R.id.apfd_txt_maketime_content);
        txtMaker = (TextView) findViewById(R.id.apfd_txt_maker);
        txtRemark = (TextView) findViewById(R.id.apfd_txt_remark);
        txtShop = (PycUnderLineOrangeTextView) findViewById(R.id.apfd_shop);
        tv_count = (TextView) findViewById(R.id.tv_count);
        tv_days = (TextView) findViewById(R.id.tv_days);
        tv_days_title = (TextView) findViewById(R.id.tv_days_title);
        txtSeriesName = (TextView) findViewById(R.id.apfd_txt_serise_name);
        tv_order_title = (TextView) findViewById(R.id.apfd_txt_orderno_title);
        tv_order = (TextView) findViewById(R.id.apfd_txt_orderno);
        tv_singleTime = (TextView) findViewById(R.id.tv_singleTime);

        btnRead = (Button) findViewById(R.id.apfd_btn_read);
        imbRefresh = (ImageButton) findViewById(R.id.apfd_imb_refresh);

        findViewById(R.id.back_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //设置信息
    private void initInfo(final SmInfo info, final String filePath) {
        final int totalDays = (int) DataConvert.transformTime(info.getStartTime(), info
                .getEndTime());
        final int leftDays = (int) info.getFreeLeftDays();
//        tv_count.setTextColor(black);
//        tv_days.setTextColor(black);
//        txtTitle.setText(Util.FileUtil.getFileName(filePath));
        String fileName = Util.FileUtil.getFileName(filePath);
        txtTitle.setText(fileName.substring(0, fileName.length() - 4));
        // make time
        txtMakeTime.setText(info.getMakeTime());

        tv_singleTime.setVisibility(info.getSingleOpenTime() > 0 ? View.VISIBLE : View.GONE);
        tv_singleTime.setText(mNumColor.getPartColor("每次能看："
                + DataConvert.toTime(info.getSingleOpenTime())));

        // client
//        int client = getClientName(info.getAppType());

        tv_order_title.setVisibility(TextUtils.isEmpty(info.getOrderNo()) ? View.GONE : View
                .VISIBLE);
        tv_order.setVisibility(TextUtils.isEmpty(info.getOrderNo()) ? View.GONE : View.VISIBLE);
        tv_order.setText("订单编号：" + info.getOrderNo());

        // series
        txtSeriesName.setVisibility(TextUtils.isEmpty(info.getSeriesName()) ? View.GONE : View
                .VISIBLE);
        txtSeriesName.setText("系列名称：" + info.getSeriesName());

        // remark
        txtMaker.setText("作者 " + info.getNick() + " 说：");
        txtRemark.setText(getRemark(info));

        // forbid (only for free file)
//        txtForbid.setVisibility(info.isMakerAllowed() ? View.GONE : View.VISIBLE);

        //System.out.println("---||打印返回文件信息||----" + info.toString());
        // count
        if (info.isCountLimit()) {
            tv_count.setVisibility(View.VISIBLE);
            //已激活
            if (!info.isNeedApply()) {
                String str = "能看几次：剩余 " + (info.getLeftCount()) + " 次，共 " + info.getOpenCount() +
                        " 次";
                // g_txtMoney.setText(new ColorText(".0123456789", green)
                // .getPartColor("帐户余额：" + userInfo.getMoney() + " 元"));
                tv_count.setText(mNumColor.getPartColor(str));
            } else {
                //未激活
//                tv_count.setVisibility(View.GONE);
                tv_count.setText(mNumColor.getPartColor("能看几次：共 " + info.getOpenCount() + "次"));
            }
        } else {
            tv_count.setVisibility(View.VISIBLE);
            tv_count.setText("能看几次：不限制");
        }

        // day
//        initDay(info);
        if (info.isPayDataLimit()) {
            tv_days.setVisibility(View.VISIBLE);
            tv_days_title.setVisibility(View.VISIBLE);
            if (!info.isNeedApply()) {

                //如果fileversion=4 要显示开始时间和结束时间；如果fileversion=2 要显示剩余天数或年数
                if (info.getFileVersion() == 4 && !TextUtils.isEmpty(info.getStartTime())) {
                    tv_days.setText(mNumColor.getPartColor("从" +
                            info.getStartTime() + " 到" + info.getEndTime()));
                } else {
                    if (info.getDays() > 0) {
                        tv_days.setText(mNumColor.getPartColor
                                ("剩余 " + info.getRemainDays() + " 天，共 " + info.getDays() + " 天"));
                    } else if (info.getYears() > 0) {
                        tv_days.setText(mNumColor.getPartColor("剩余 " + info.getYears() + " 年，共 "
                                + info.getYears() + " 年"));
                    } else {
                        tv_days.setText("不限制");
                    }
                }

//                else if (info.getFileVersion() == 2) {
//                    if (info.getDays() > 0) {
//                        tv_days.setText(new ColorText(".-1234567890", green).getPartColor
//                                ("能看多久：剩余 " + info.getRemainDays() + " 天，共 " + info
//                                        .getDays() + " 天"));
//                    } else if (info.getYears() > 0) {
//                        tv_days.setText(new ColorText(".-1234567890", green).getPartColor
//                                ("能看多久：剩余 " + info.getRemainYears() + " 年，共 " + info
//                                        .getYears() + " 年"));
//                    } else {
//                        tv_days.setText("不限制查看天数1");
//                        tv_days.setTextColor(green);
//                    }
//                } else {
//                    tv_days.setText("不限制查看天数2");
//                    tv_days.setTextColor(green);
//                }
            } else {
//                tv_days.setVisibility(View.GONE);
                tv_days.setText(mNumColor.getPartColor("共 " + info.getDays() + "天"));
            }
        } else if (info.isFreeDataLimit()) {
            tv_days.setVisibility(View.VISIBLE);
            tv_days_title.setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(info.getStartTime())) {
                tv_days.setText(mNumColor.getPartColor("剩余" + leftDays
                        + "天，共" + totalDays + "天" + "\n" + "从" +
                        info.getStartTime() + " 到" + info.getEndTime()));
//                tv_days.setText("开始时间：" +
//                        info.getStartTime() + " 结束时间：" + info.getEndTime());
            } else {
                tv_days.setVisibility(View.VISIBLE);
                tv_days.setText("不限制");
            }

            //如果fileversion=4 要显示开始时间和结束时间；如果fileversion=2 要显示剩余天数或年数
//            if (info.getFileVersion() == 4 && !TextUtils.isEmpty(info.getStartTime())) {
//                tv_days.setText(new ColorText(".-1234567890", green).getPartColor("开始时间：" +
//                        info.getStartTime() + " 结束时间：" + info.getEndTime()));
//
//            }else {
//                if (info.getDays() > 0) {
//                    tv_days.setText(new ColorText(".-1234567890", green).getPartColor
//                            ("能看多久：剩余 " + info.getRemainDays() + " 天，共 " + info
//                                    .getDays() + " 天"));
//                } else if (info.getYears() > 0) {
//                    tv_days.setText(new ColorText(".-1234567890", green).getPartColor
//                            ("能看多久：剩余 " + info.getRemainYears() + " 年，共 " + info
//                                    .getYears() + " 年"));
//                } else {
//                    tv_days.setText("能看几天：不限制");
//                }
//            }
        } else {
            tv_days.setVisibility(View.VISIBLE);
            tv_days_title.setVisibility(View.VISIBLE);
            tv_days.setText("不限制");
//            tv_days.setText(mNumColor.getAssignColor("能看多久：不限制",new ColorText.ColorPair("不限制",
// green)));
//            mNumColor.getAssignColor("能看多久：不限制",new ColorText.ColorPair("不限制",green));
        }

        // read
        btnRead.setVisibility(info.canOpen() || info.isPayFile() ? View.VISIBLE : View.GONE);

        imbRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Util.NetUtil.isNetInUse(PbbFileDetailActivity.this)) {
                    GlobalToast.toastShort(PbbFileDetailActivity.this, "没有可用网络");
                    return;
                }
                refreshData(filePath, true);
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PbbFileDetailActivity.this, SmReaderActivity.class);
                intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, filePath);
                startActivity(intent);
            }
        });

        //浏览店铺
        txtShop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (Util.NetUtil.isNetInUse(PbbFileDetailActivity.this)) {
                    //mShopDialog.show();
                    showBgLoading(PbbFileDetailActivity.this);
                    getSafeUrl(info);
                } else {
                    GlobalToast.toastShort(PbbFileDetailActivity.this, "没有可用网络");
                }
            }
        });
    }


//    private void initDay(SmInfo info) {
//        if (!info.canShowLimit()) {
//            tv_days.setVisibility(View.GONE);
//            return;
//        }
//        tv_days.setVisibility(View.VISIBLE);
//
//        String strDays = "";
//
//        //newFile走自由传播路线
//        if (info.isPayFile() && !info.isNewFile()) {
//            if (info.isPayDataLimit()) {
//
//                if (!info.isNeedApply()) {
//                    if (info.getRemainDays() + info.getRemainYears() > 0)    // 激活后还没查看时，剩余XX是没有值的
//                    {
//                        if (info.getRemainYears() > 0) {
//                            strDays += "剩余 ";
//                            strDays += info.getRemainYears() + " 年，";
//                        }
//                        if (info.getRemainDays() > 0) {
//                            strDays += "剩余 ";
//                            strDays += info.getRemainDays() + " 天，";
//                        }
//                    } else {
//                        if (info.getYears() > 0) {
//                            strDays += "剩余 ";
//                            strDays += info.getYears() + " 年，";
//                        }
//                        if (info.getDays() > 0) {
//                            strDays += "剩余 ";
//                            strDays += info.getDays() + " 天，";
//                        }
//                    }
//                }
//                strDays += "共 ";
//                if (info.getYears() > 0) {
//                    strDays += info.getYears() + " 年 ";
//                }
//                if (info.getDays() > 0) {
//                    strDays += info.getDays() + " 天";
//                }
//            }
//        }    // payFile结束
//
//        if (TextUtils.isEmpty(strDays)) {
//            tv_days.setText(mNumColor.getAllColor("不限制"));
//        } else {
//            tv_days.setText(mNumColor.getPartColor(strDays));
//        }
//    }


    private CharSequence getRemark(SmInfo info) {
        String remark = "";
        if (!TextUtils.isEmpty(info.getQq())) {
            remark += "Q  Q:\t" + info.getQq() + "\n";
        }
        if (!TextUtils.isEmpty(info.getEmail())) {
            remark += "邮箱:\t" + info.getEmail() + "\n";
        }
        if (!TextUtils.isEmpty(info.getPhone())) {
            remark += "手机:\t" + info.getPhone() + "\n";
        }
        if (!TextUtils.isEmpty(info.getRemark())) {
            remark += info.getRemark();
        } else {
            remark += SmInfo.REMARK_DEFAULT;
        }
        return remark;
    }

    //获取店铺链接
    private void getSafeUrl(SmInfo info) {
        TokenInfo tokenInfo = TokenInfo.getSavedToken(PbbFileDetailActivity.this);
        String token = tokenInfo.getValidToken();
        if (token == null) {
            getShopUrlToken(info);
        } else {
            getShopUrl(info, tokenInfo);
        }
    }

    private void getShopUrl(final SmInfo info, TokenInfo tokenInfo) {
        String url = "http://api.pyc.com.cn/api/v1/saleinfo";
        //请求参数
        Bundle bundle = new Bundle();
        bundle.putInt("Fid", info.getFid());

        //请求头
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + tokenInfo.getToken());
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.get(url, bundle, headers, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                //mShopDialog.dismiss();
                try {
                    JSONObject object = new JSONObject(arg0);
                    JSONObject Result = (JSONObject) object.get("Result");

                    String shopUrl = (String) Result.get("ShopUrl");

                    info.setShopUrl(shopUrl);

                    if (TextUtils.isEmpty(info.getShopUrl())) {
                        View v = (PbbFileDetailActivity.this).getLayoutInflater().inflate(
                                R.layout.dialog_no_shop, null);

                        final Dialog dialog = new Dialog(PbbFileDetailActivity.this, R.style
                                .no_frame_small);
                        dialog.setContentView(v);
                        dialog.show();

                        TextView t = (TextView) v.findViewById(R.id.dd_txt_content);
                        Button b1 = (Button) v.findViewById(R.id.dd_btn_sure);

                        t.setText("作者尚未开设店铺");
                        b1.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                    } else {

                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(info.getShopUrl());
                        intent.setData(content_url);
                        (PbbFileDetailActivity.this).startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinished() {
                //mShopDialog.dismiss();
                hideBgLoading();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                //mShopDialog.dismiss();
            }

            @Override
            public void onCancelled(CancelledException arg0) {
            }
        });

    }

    private void getShopUrlToken(final SmInfo info) {
        //从sp取出token
        //如果有token且有效，直接走取url的业务；反之走token业务
        //取完token后，存储，再掉url业务
        String url = "http://login.pyc.com.cn/token";

        //请求参数
        Bundle bundle = new Bundle();
        bundle.putString("grant_type", "client_credentials");

        //请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Basic " + SecurityUtil.encryptBASE64(testID + ":" + testPSD));
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.post(url, bundle, headers, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object = new JSONObject(arg0);
                    String tokenString = (String) object.get("access_token");
                    int timeTmp = object.getInt("expires_in");
                    TokenInfo tokenInfo = new TokenInfo();
                    tokenInfo.setToken(tokenString);
                    tokenInfo.setExpires_in(System.currentTimeMillis() + timeTmp);
                    TokenInfo.saveToken(PbbFileDetailActivity.this, tokenInfo);
                    getShopUrl(info, tokenInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinished() {
                //mShopDialog.dismiss();
                hideBgLoading();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
            }

            @Override
            public void onCancelled(CancelledException arg0) {
            }
        });
    }

}
