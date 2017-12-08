package cn.com.pyc.sm;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qlk.util.base.BaseActivity;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.media.ISelection;
import com.qlk.util.tool.ColorText;
import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import cn.com.pyc.pbb.R;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.conn.SmConnect;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.media.ExtraDelSndFile;
import cn.com.pyc.web.WebActivity;
import cn.com.pyc.widget.PycUnderLineBlueTextView;
import cn.com.pyc.widget.PycUnderLineTextView;
import cn.com.pyc.xcoder.XCoder;
import cn.com.pyc.xcoder.XCoderResult;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (已经发送的adapter)
 * @date 2016/12/21 11:25
 */
public class SendAdapter extends BaseExpandableListAdapter implements ISelection {

    private String testID = "pbbandroid0";
    private String testPSD = "84n109f3";
    private String tokenString;

    protected Context mContext;
    private LayoutInflater mInflater;
    protected final ArrayList<String> mPaths;
    public final HashMap<String, SmInfo> mDatas;
    protected ColorText mNumColor;
    protected ColorText searchCT;
    final int green, red, blue;
    private int expandPos = -1;
    private SmConnect mSmConnect;
    private ExpandableListView mSmExpandableListView;
    private String mSearchText;

    public SendAdapter(Context context, ExpandableListView SmExpandableListView, ArrayList<String> paths, HashMap<String, SmInfo> datas) {
        mContext = context;
        mPaths = paths;
        mDatas = datas;
        mInflater = LayoutInflater.from(context);
        mSmConnect = new SmConnect(context);
        Resources resources = context.getResources();
        green = resources.getColor(R.color.green);
        red = resources.getColor(R.color.red);
        blue = resources.getColor(android.R.color.holo_orange_dark);
        mNumColor = new ColorText("0123456789-.", green);
        searchCT = new ColorText(null, green);
        mSmExpandableListView = SmExpandableListView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View v, ViewGroup parent) {
        if (isExpanded) {
            expandPos = groupPosition;
        }
        Group group = null;
        if (v == null) {
            v = mInflater.inflate(R.layout.adapter_sm_group, parent, false);
            group = new Group();
            group.imvState = (ImageView) v.findViewById(R.id.asg_imv_state);
            group.txtName = (TextView) v.findViewById(R.id.asg_txt_name);
            group.txtSummary = (TextView) v.findViewById(R.id.asg_txt_summary);
            group.imvIndicator = (ImageView) v.findViewById(R.id.asg_imv_indicator);
            group.cbxCheck = (CheckBox) v.findViewById(R.id.asg_cbx_check);
            group.cbxCheck.setOnCheckedChangeListener(checkedChangedListener);
            v.setTag(group);
        } else {
            group = (Group) v.getTag();
        }

        final String filePath = mPaths.get(groupPosition);// 获取path
        String name = Util.FileUtil.getFileName(filePath);// 截取名字
        name = name.substring(0, name.length() - 4);    // remove ".pbb/.pyc"
        // 如果搜索的字符,在名字中间存在,就高亮显示。
        if (!TextUtils.isEmpty(mSearchText)) {
            group.txtName.setText(searchCT.getAssignColor(name, new ColorText.ColorPair(mSearchText, blue)));
        } else {
            group.txtName.setText(name);
        }
        // 打开或关闭,ExpandableListview的子布局。
        group.imvIndicator.setBackgroundResource(isExpanded ? R.drawable.indicator_down : R.drawable.indicator_up);
        // 判断是否显示或隐藏删除按钮
        if (isSelectable) {
            group.cbxCheck.setTag(filePath);
            group.cbxCheck.setChecked(mSelectPaths.contains(filePath));
            group.cbxCheck.setVisibility(View.VISIBLE);
        } else {
            group.cbxCheck.setVisibility(View.GONE);
        }
        // 获取权限信息。
        SmInfo info = mDatas.get(filePath);
        // 判断权限是否为null
        if (info == null || info.invalid()) {
            group.imvState.setVisibility(View.INVISIBLE);// 显示权限图片。//View.INVISIBLE ensure
            group.txtSummary.setVisibility(View.GONE);// 隐藏已经到期信息。//View.GONE ensure the
        } else {
            // 权限不为null,就把剩余次数和剩余天数,在item中显示出来。
            initGroupInfo(group, info);
        }
        return v;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View v, ViewGroup parent) {
        Child child = null;
        if (v == null) {
            v = mInflater.inflate(R.layout.adapter_sm_child, parent, false);
            child = new Child();
            child.vInfo = v.findViewById(R.id.asc_lyt_info);
            child.vNetError = (ViewStub) v.findViewById(R.id.asc_lyt_net_error);
            child.txtMakeTime = (TextView) v.findViewById(R.id.asc_txt_maketime_content);// 制作时间
            child.txtSeriseName = (TextView) v.findViewById(R.id.asc_txt_serise_name);
            child.imbRefresh = (ImageButton) v.findViewById(R.id.asc_imb_refresh);// 刷新
            child.txtMaker = (TextView) v.findViewById(R.id.asc_txt_maker);
            child.txtRemark = (TextView) v.findViewById(R.id.asc_txt_remark);
            child.txtCountContent = (TextView) v.findViewById(R.id.asc_txt_count_content);
            child.txtCountTitle = (TextView) v.findViewById(R.id.asc_txt_count_title);
            child.pbCount = (ProgressBar) v.findViewById(R.id.asc_pb_count);
            child.txtDaysContent = (TextView) v.findViewById(R.id.asc_txt_day_content);
            child.txtDaysTitle = (TextView) v.findViewById(R.id.asc_txt_day_title);
            child.pbDays = (ProgressBar) v.findViewById(R.id.asc_pb_day);
            child.txtSingleOpen = (TextView) v.findViewById(R.id.asc_txt_single_open_time);
            child.btnRead = (Button) v.findViewById(R.id.asc_btn_read);// read
            child.txtFromTo = (TextView) v.findViewById(R.id.asc_txt_day_from_to);
            child.txtForbid = (TextView) v.findViewById(R.id.asc_txt_abort);// 已被作者终止阅读
            child.txtShop = (PycUnderLineTextView) v.findViewById(R.id.asc_shop);
            child.tv_orderno_title = (TextView) v.findViewById(R.id.asc_txt_orderno_title);// 订单编号
            child.tv_orderno = (PycUnderLineBlueTextView) v.findViewById(R.id.asc_txt_orderno);// 内容
            child.bt_active = (Button) v.findViewById(R.id.asc_btn_active);// 激活与查询
            child.bt_abort = (Button) v.findViewById(R.id.asc_btn_abort);// 终止阅读
            child.bt_modify = (Button) v.findViewById(R.id.asc_btn_modify);// 修改条件
            child.send = (ImageButton) v.findViewById(R.id.asc_imb_send);// 发送分享
            child.txtShop.setVisibility(View.GONE);
            v.setTag(child);
        } else {
            child = (Child) v.getTag();
        }
        final String filePath = mPaths.get(groupPosition);// 获取文件路径
        SmInfo info = mDatas.get(filePath);// 获取文件对应权限集合
        // 权限不为null并且限制条件有效
        if (info != null && info.valid()) {
            child.hide(child.vNetError);
            child.show(child.vInfo);
            initChildInfo(child, info, filePath);
            child.txtMakeTime.setText(info.getMakeTime()); // 设置制作时间
            child.send.setVisibility(View.VISIBLE);// 显示发送按钮
            child.send.setTag(filePath);// 给发送按钮设置tag
            child.btnRead.setVisibility(View.GONE);
            child.txtForbid.setVisibility(View.GONE);
            child.tv_orderno_title.setVisibility(View.GONE);
            child.tv_orderno.setVisibility(View.GONE);
            child.bt_active.setVisibility(View.GONE);
            child.bt_abort.setVisibility(View.GONE);
            child.bt_modify.setVisibility(View.GONE);
            child.txtSeriseName.setVisibility(TextUtils.isEmpty(info.getSeriesName()) ? View.GONE : View.VISIBLE);
            child.txtSeriseName.setText("系列名称：" + info.getSeriesName());
//            child.txtMaker.setText("作者 " + info.getNick() + " 说：");

            child.txtMaker.setText("作者 " + "鹏保宝用户"+ " 说：");
            child.txtRemark.setText(getRemark(info));
            child.txtForbid.setVisibility(info.isMakerAllowed() ? View.GONE : View.VISIBLE);
            initCount(child, info);// 判断是否显示能看几次,如显示，再显示能看的具体次数。
            initDay(child, info);// 判断能看几天是否显示,如显示,再显示具体的天数。
            initSingleOpen(child, info);// 判断每次能看多久。
            // 判断是否付费文件
            if (info.isPayFile()) {
                // 激活与查询
                child.bt_active.setTag(filePath);    // 此处也可不用setTag，直接使用filePath即可
                child.bt_active.setVisibility(View.VISIBLE);
                child.bt_active.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String path = (String) v.getTag();
                        mContext.startActivity(new Intent(mContext, WebActivity.class).putExtra(GlobalIntentKeys.BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.PayRecordSingle).putExtra(GlobalIntentKeys.BUNDLE_DATA_EXTINFO, mDatas.get(path).getFid()));
                    }
                });
            } else {
                // 终止阅读
                child.bt_abort.setText(info.isMakerAllowed() ? "终止阅读" : "取消终止");
                child.bt_abort.setTag(filePath);    // 此处也可不用setTag，直接使用filePath即可
                child.bt_abort.setVisibility(View.VISIBLE);
                child.bt_abort.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String path = (String) v.getTag();
                        stopRead(path);
                    }
                });
                // 修改条件
                child.bt_modify.setTag(filePath);    // 此处也可不用setTag，直接使用filePath即可
                child.bt_modify.setVisibility(View.VISIBLE);
                child.bt_modify.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String path = (String) v.getTag();
                        Intent intent = new Intent(mContext, FreeLimitConditionActivity.class);
                        intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, path);
                        intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CHANGE_LIMIT, true);
                        intent.putExtra(GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO, mDatas.get(path));
                        mContext.startActivity(intent);
                    }
                });
            }
            // 刷新
            child.imbRefresh.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshSingal(filePath, true);
                }
            });
            // 发送分享
            child.send.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExtraDelSndFile.sendFile(mContext, (String) v.getTag());
                }
            });
        } else {
//            showNetErrorView(child, filePath);
            refreshSingal(child , filePath, true);
        }

        return v;
    }

    protected class Group {
        public ImageView imvState;
        public TextView txtSummary;
        TextView txtName;
        ImageView imvIndicator;
        CheckBox cbxCheck;
    }
    /**
    * @Description: (判断每次能看多久。)
    * @author 李巷阳
    * @date 2016/12/21 17:21
    */
    private void initSingleOpen(Child child, SmInfo info) {
        child.hide(child.txtSingleOpen);        // 先隐藏，有值再show
        if (info.isPayFile()) {
            if (!info.isNeedApply() && !TextUtils.isEmpty(info.getFirstOpenTime())) {
                child.show(child.txtSingleOpen);
                child.txtSingleOpen.setText(mNumColor.getPartColor("首次阅读：" + info.getFirstOpenTime()));
            }
        } else {
            if (info.getSingleOpenTime() > 0) {
                child.show(child.txtSingleOpen);
                child.txtSingleOpen.setText(mNumColor.getPartColor("每次能看：" + DataConvert.toTime(info.getSingleOpenTime())));
            }
        }

    }
    /**   
    *
    * @Description: (判断能看几天是否显示,如显示,再显示具体的天数。)
    * @author 李巷阳
    * @date 2016/12/21 17:12 
    */
    private void initDay(Child child, SmInfo info) {
        if (!info.canShowLimit()) {
            child.hide(child.txtDaysContent);
            child.hide(child.txtDaysTitle);
            child.hide(child.pbDays);
            child.hide(child.txtFromTo);
            return;
        }
        child.show(child.txtDaysTitle);
        child.show(child.txtDaysContent);
        child.hide(child.pbDays);
        child.hide(child.txtFromTo);
        String strDays = "";
        //newFile走自由传播路线
        if (info.isPayFile() && !info.isNewFile()) {
            if (info.isPayDataLimit()) {

                if (!info.isNeedApply()) {
                    if (info.getRemainDays() + info.getRemainYears() > 0)    // 激活后还没查看时，剩余XX是没有值的
                    {
                        if (info.getRemainYears() > 0) {
                            strDays += "剩余 ";
                            strDays += info.getRemainYears() + " 年，";
                        }
                        if (info.getRemainDays() > 0) {
                            strDays += "剩余 ";
                            strDays += info.getRemainDays() + " 天，";
                        }
                    } else {
                        if (info.getYears() > 0) {
                            strDays += "剩余 ";
                            strDays += info.getYears() + " 年，";
                        }
                        if (info.getDays() > 0) {
                            strDays += "剩余 ";
                            strDays += info.getDays() + " 天，";
                        }
                    }
                }
                strDays += "共 ";
                if (info.getYears() > 0) {
                    strDays += info.getYears() + " 年 ";
                }
                if (info.getDays() > 0) {
                    strDays += info.getDays() + " 天";
                }
            } else {
                if (info.isFreeDataLimit()) {
                    child.show(child.pbDays);
                    child.show(child.txtFromTo);
                    final int totalDays = (int) DataConvert.transformTime(info.getStartTime(), info.getEndTime());
                    final int leftDays = (int) info.getFreeLeftDays();
                    strDays += "剩余 " + leftDays + " 天，共 " + totalDays + " 天";

                    child.pbDays.setMax(totalDays);
                    child.pbDays.setProgress(leftDays);
                    child.txtFromTo.setText(mNumColor.getPartColor("从 " + info.getStartTime() + " 到 " + info.getEndTime()));
                }
            }
        }    // payFile结束
        else {
            if (info.isFreeDataLimit()) {
                child.show(child.pbDays);
                child.show(child.txtFromTo);
                final int totalDays = (int) DataConvert.transformTime(info.getStartTime(), info.getEndTime());
                final int leftDays = (int) info.getFreeLeftDays();
                strDays += "剩余 " + leftDays + " 天，共 " + totalDays + " 天";

                child.pbDays.setMax(totalDays);
                child.pbDays.setProgress(leftDays);
                child.txtFromTo.setText(mNumColor.getPartColor("从 " + info.getStartTime() + " 到 " + info.getEndTime()));
            }
        }

        if (TextUtils.isEmpty(strDays)) {
            child.txtDaysContent.setText(mNumColor.getAllColor("不限制"));
        } else {
            child.txtDaysContent.setText(mNumColor.getPartColor(strDays));
        }
    }
    /**   
    *
    * @Description: (判断是否显示能看几次,如显示，再显示能看的具体次数。)
    * @author 李巷阳
    * @date 2016/12/21 17:12 
    */
    private void initCount(Child child, SmInfo info) {
        if (!info.canShowLimit()) {
            child.hide(child.txtCountTitle);
            child.hide(child.txtCountContent);
            child.hide(child.pbCount);
            return;
        }

        child.show(child.txtCountTitle);
        child.show(child.txtCountContent);
        child.hide(child.pbCount);

        String strCount = "";

        if (info.isCountLimit()) {
            if (info.isPayFile()) {
                if (!info.isNeedApply()) {
                    strCount += "剩余 " + info.getLeftCount() + " 次，";
                }
            } else {
                strCount += "剩余 " + info.getLeftCount() + " 次，";
                child.show(child.pbCount);
                child.pbCount.setMax(info.getOpenCount());
                child.pbCount.setProgress(info.getLeftCount());
            }

            strCount += "共 " + info.getOpenCount() + " 次";
        }

        if (TextUtils.isEmpty(strCount)) {
            child.txtCountContent.setText(mNumColor.getAllColor("不限制"));
        } else {
            child.txtCountContent.setText(mNumColor.getPartColor(strCount));
        }
    }
    /**   
    *
    * @Description: (在权限内容里显示QQ,手机,邮箱,摘要)
    * @author 李巷阳
    * @date 2016/12/21 17:24 
    */
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

  /**   
  *
  * @Description: (权限不为null,就把剩余次数和剩余天数,在item中显示出来)
  * @author 李巷阳
  * @date 2016/12/21 17:22 
  */
    protected void initGroupInfo(Group group, SmInfo info) {
        group.imvState.setVisibility(View.VISIBLE);// 显示权限图片。
        group.imvState.setBackgroundResource(getResource(info.getSucOpenFlag()));// 设置权限显示的对应图片。
        group.txtSummary.setVisibility(info.canShowLimit() || info.isNeedApply() ? View.VISIBLE : View.GONE);// 在item上显示剩余次数,是否到期。
        group.txtSummary.setText(getSummaryInfo(info));
    }
    /**   
    * @Description: (剩余次数和天数，具体显示内容。)
    * @author 李巷阳
    * @date 2016/12/21 17:24 
    */
    private SpannableString getSummaryInfo(SmInfo info) {
        String summary = "";
        if (info.isPayFile() && !info.isNewFile()) {
            if (info.isNeedApply()) {
                return mNumColor.getAllColor("等待激活");
            } else {
                if (info.isCountLimit()) {
                    summary += (info.getOpenCount() - info.getOpenedCount()) + "/" + info.getOpenCount() + " 次";
                } else {
                    summary += "不限次数";
                }
                // 判断时间是否限制
                if (info.isPayDataLimit()) {
                    if (TextUtils.isEmpty(info.getFirstOpenTime())) {
                        summary += "   剩余 ";
                        if (info.getYears() > 0) {
                            summary += info.getYears() + " 年";
                        }
                        if (info.getDays() > 0) {
                            summary += info.getDays() + " 天";
                        }
                    } else {
                        if (info.getRemainDays() + info.getRemainYears() > 0) {
                            summary += "   剩余 ";
                            if (info.getRemainYears() > 0) {
                                summary += info.getRemainYears() + " 年";
                            }
                            if (info.getRemainDays() > 0) {
                                summary += info.getRemainDays() + " 天";
                            }
                        } else {
                            summary += "   已到期";
                        }
                    }
                } else {
                    summary += "   不限天数";
                }
            }
        } else {
            /* Free spread file */
            if (info.getOpenCount() == 0) {
                summary += "不限次数";
            } else {
                summary += (info.getOpenCount() - info.getOpenedCount()) + "/" + info.getOpenCount() + " 次";
            }

            if (info.isFreeDataLimit()) {
                final long leftDays = info.getFreeLeftDays();
                if (leftDays > 0) {
                    summary += "   剩余 " + leftDays + " 天";
                } else {
                    summary += "   已到期";
                }
            } else {
                summary += "   不限天数";
            }
        }

        if (!TextUtils.isEmpty(info.getFirstOpenTime())) {
            summary += "    " + info.getFirstOpenTime().substring(0, 10).toString();
        }
        return mNumColor.getAssignColor(summary, new ColorText.ColorPair("已到期", red), new ColorText.ColorPair("不限天数", green), new ColorText.ColorPair("不限次数", green));
    }

    /**
     * Get picture resource matches the file state.
     *
     * @param flag
     * @return
     */
    private static int getResource(SmInfo.SucFlag flag) {
        int res = 0;
        switch (flag) {
            case CanOpen:
                res = R.drawable.sm_state1;
                break;

            case NeverOpen:
                res = R.drawable.sm_state2;
                break;

            case LimitOut:
                res = R.drawable.sm_state3;
                break;

            case NeverOpenLimitOut:
                res = R.drawable.sm_state4;
                break;

            default:
                break;
        }
        return res;
    }

    /**   
    * @Description: (如果item对应的权限信息为空,则提示用户重新获取。)
    * @author 李巷阳
    * @date 2016/12/21 17:26 
    */
    private void showNetErrorView(final Child child, final String filePath) {
        if (child.btnConnect == null) {
            View netErrorView = child.vNetError.inflate();
            child.btnConnect = (Button) netErrorView.findViewById(R.id.vsn_btn_connect);
        }
        child.btnConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSingal(child,filePath, true);
            }
        });
        child.show(child.vNetError);
        child.hide(child.vInfo);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
    /**   
    * @Description: (查询本地权限,以及和网络对比,完后显示。)
    * @author 李巷阳
    * @date 2016/12/21 17:27 
    */
    public void refreshSingal(final Child child,final String path, final boolean fromUser) {
        Runnable netTask = new Runnable() {
            @Override
            public void run() {
                SmInfo info = mDatas.get(path);
                if (info == null || info.getFid() <= 0) {
                    XCoderResult xr = XCoder.analysisSmFile(path);
                    if (xr.succeed()) {
                        info = xr.getSmInfo();
                        mDatas.put(path, info);    //replace
                    } else {
                        if (fromUser) {
                            xr.showResult(mContext);
                            showNetErrorView(child, path);
                        }
                        return;
                    }
                }
                if (info != null && info.getFid() > 0) {
                    mSmConnect.getFileInfo(info, false, fromUser, false);
                    mRefreshHandler.sendEmptyMessage(0);
                }
            }
        };

        if (fromUser) {
            GlobalTask.executeNormalTask(mContext, netTask);
        } else {
            GlobalTask.executeBackground(netTask);
        }

    }
    /**
     * @Description: (查询本地权限,以及和网络对比,完后显示。)
     * @author 李巷阳
     * @date 2016/12/21 17:27
     */
    public void refreshSingal(final String path, final boolean fromUser) {
        Runnable netTask = new Runnable() {
            @Override
            public void run() {
                SmInfo info = mDatas.get(path);
                if (info == null || info.getFid() <= 0) {
                    XCoderResult xr = XCoder.analysisSmFile(path);
                    if (xr.succeed()) {
                        info = xr.getSmInfo();
                        mDatas.put(path, info);    //replace
                    } else {
                        if (fromUser) {
                            xr.showResult(mContext);
                        }

                        return;
                    }
                }
                if (info != null && info.getFid() > 0) {
                    mSmConnect.getFileInfo(info, false, fromUser, false);
                    mRefreshHandler.sendEmptyMessage(0);
                }
            }
        };

        if (fromUser) {
            GlobalTask.executeNormalTask(mContext, netTask);
        } else {
            GlobalTask.executeBackground(netTask);
        }

    }
    public void setSearchText(String searchText) {
        mSearchText = searchText;
    }


    protected void initChildInfo(Child child, SmInfo info, String filePath) {
        // 作者说
        child.txtMaker.setText("你对他说：");
        if (info.isPayFile()) {
            if (info.isCountLimit()) {
                child.txtCountContent.setText(searchCT.getPartColor("共 " + info.getOpenCount() + " 次"));
            } else {
                child.txtCountContent.setText(searchCT.getAllColor("不限制"));
            }

            if (info.isPayDataLimit()) {
                String days = "共 ";
                if (info.getYears() > 0) {
                    days += info.getYears() + " 年 ";
                }
                if (info.getDays() > 0) {
                    days += info.getDays() + " 天";
                }
                child.txtDaysContent.setText(searchCT.getPartColor(days));
            } else {
                child.txtDaysContent.setText(searchCT.getAllColor("不限制"));
            }
        } else {
            //            super.initChildInfo(child, info, filePath);
        }

    }

    protected class Child {
        public TextView txtMaker;
        public PycUnderLineTextView txtShop;
        View vInfo; // show sminfo
        ViewStub vNetError;
        TextView txtMakeTime; // The date when the file created.
        //		TextView txtClient; // The Client to create the file.
        TextView txtSeriseName;
        ImageButton imbRefresh;
        TextView txtRemark;
        public TextView txtCountContent;
        TextView txtCountTitle;
        ProgressBar pbCount;
        public TextView txtDaysContent;
        TextView txtDaysTitle;
        TextView txtFromTo;
        TextView txtForbid;
        ProgressBar pbDays;
        TextView txtSingleOpen;
        Button btnRead;
        Button btnConnect;
        TextView tv_orderno_title;
        PycUnderLineBlueTextView tv_orderno;
        Button bt_active;
        Button bt_abort;
        Button bt_modify;
        ImageButton send;

        public void show(View v) {
            v.setVisibility(View.VISIBLE);
        }

        public void hide(View v) {
            v.setVisibility(View.GONE);
        }
    }

    @Override
    public int getGroupCount() {
        return mPaths.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public int getExpandPos() {
        return expandPos;
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
        expandPos = -1;
    }

    // 终止阅读
    private void stopRead(final String path) {
        Runnable netTask = new Runnable() {
            @Override
            public void run() {
                SmInfo info = mDatas.get(path);
                String username = UserDao.getDB(mContext).getUserInfo().getUserName();
                info.setUserName(username);
                if (new SmConnect(mContext).stopRead(info, false, true).succeed()) {
                    BaseActivity.UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        };
        GlobalTask.executeNormalTask(mContext, netTask);
    }

    public void setData(ArrayList<String> mpath) {
        mPaths.clear();
        mPaths.addAll(mpath);
        notifyDataSetChanged();
    }

    public final Handler mRefreshHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            notifyDataSetChanged();
        }
    };

	/*-*********************************************
     * TODO Select
	 ***********************************************/

    private ISelectListener mSelectListener;
    private final HashSet<String> mSelectPaths = new HashSet<>();
    private boolean isSelectable = false;

    @Override
    public void setSelectListener(ISelectListener listener) {
        mSelectListener = listener;
        if (mSelectListener == null) {
            mSelectListener = new ISelectListener() {
                @Override
                public void onSelcetChanged(boolean overflow, int total, boolean allSelected) {
                    /* Do not care the listener's value is null when using. */
                }
            };
        }
    }

    @Override
    public void setSelectable(boolean selectable) {
        this.isSelectable = selectable;
        /* In select mode, user may call setItemSelect(). */
        if (!selectable) {
            mSelectPaths.clear();
        }

        mSelectListener.onSelcetChanged(false, mSelectPaths.size(), mSelectPaths.size() == mPaths.size());
    }

    @Override
    public void clearSelected() {

    }

    @Override
    public boolean isSelecting() {
        return isSelectable;
    }

    @Override
    public void setItemSelected(int position) {
        if (position >= 0) {
            mSelectPaths.add(mPaths.get(position));
        }

        mSelectListener.onSelcetChanged(false, mSelectPaths.size(), mSelectPaths.size() == mPaths.size());
    }

    @Override
    public Collection<String> getSelected() {
        return mSelectPaths;
    }

    private CompoundButton.OnCheckedChangeListener checkedChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String path = (String) buttonView.getTag();
            if (isChecked) {
                mSelectPaths.add(path);
            } else {
                mSelectPaths.remove(path);
            }
            mSelectListener.onSelcetChanged(false, mSelectPaths.size(), mSelectPaths.size() == mPaths.size());
        }
    };

}
