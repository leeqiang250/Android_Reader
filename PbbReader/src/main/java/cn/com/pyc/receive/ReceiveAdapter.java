package cn.com.pyc.receive;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.media.ISelection;
import com.qlk.util.tool.ColorText;
import com.qlk.util.tool.ColorText.ColorPair;
import com.qlk.util.tool.DataConvert;
import com.qlk.util.tool.Util.FileUtil;
import com.qlk.util.tool.Util.NetUtil;
import com.sz.mobilesdk.util.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.bean.SmInfo.SucFlag;
import cn.com.pyc.conn.SmConnect;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.sm.SmReaderActivity;
import cn.com.pyc.widget.PycUnderLineTextView;
import cn.com.pyc.xcoder.XCoder;
import cn.com.pyc.xcoder.XCoderResult;


/**
 * It will be extended by some adapters of PengBaoBao.
 * 此为原来文件列表适配器
 * @author QiLiKing 2015-8-3 下午2:25:01
 */
@Deprecated
public class ReceiveAdapter extends BaseExpandableListAdapter implements ISelection {
    private String testID = "pbbandroid0";
    private String testPSD = "84n109f3";
    private String tokenString;

    protected Context mContext;
    private LayoutInflater mInflater;

    protected final ArrayList<String> mPaths;
    public final HashMap<String, SmInfo> mDatas;

    private SmConnect mSmConnect;
    protected ColorText mNumColor;
    protected ColorText searchCT;
    final int green, red, blue;
    private int expandPos = -1;

    public ReceiveAdapter(Context context, ArrayList<String> paths, HashMap<String, SmInfo> datas) {
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

        mShopDialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_LIGHT);
        mShopDialog.setMessage("正在获取店铺信息...");
    }

    /**
     * Subclass override this method to indicate the adapter is "Send List" or
     * "Receive List".
     *
     * @return
     */
    protected boolean isReceive() {
        return true;
    }

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
                    mSmConnect.getFileInfo(info, false, fromUser, isReceive());
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

    public final Handler mRefreshHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            notifyDataSetChanged();
        }
    };

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        String path = mPaths.get(groupPosition);
        SmInfo info = mDatas.get(path);
        if (info == null || TextUtils.isEmpty(info.getNick()))        // Batch refresh can't get
        // nick, remark etc.
        {
            refreshSingal(path, false);    // false means that it needn't toast if not succeed.
        }
    }

	/*-*****************************************
     * TODO Search
	 *******************************************/
    /**
     * When "mSearchText" is not empty, then it's in the search mode.
     */
    private String mSearchText;

    public void setSearchText(String searchText) {
        mSearchText = searchText;
    }

    @Override
    public int getGroupCount() {
        return mPaths !=null && !mPaths.isEmpty()? mPaths.size() : 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        //return 1;
        return mPaths !=null && !mPaths.isEmpty()? 1 : 0;
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

	/*-*********************************************************
     * TODO Group
	 ***********************************************************/

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

        // path
        final String filePath = mPaths.get(groupPosition);

        // filename
        String name = FileUtil.getFileName(filePath);
        name = name.substring(0, name.length() - 4);    // remove ".pbb/.pyc"
        if (!TextUtils.isEmpty(mSearchText)) {
            group.txtName.setText(searchCT.getAssignColor(name, new ColorPair(mSearchText, blue)));
        } else {
            group.txtName.setText(name);
        }

        // indicator
        group.imvIndicator.setBackgroundResource(isExpanded ? R.drawable.indicator_down
                : R.drawable.indicator_up);

        // check box
        if (isSelectable) {
            group.cbxCheck.setTag(filePath);
            group.cbxCheck.setChecked(mSelectPaths.contains(filePath));
            group.cbxCheck.setVisibility(View.VISIBLE);
        } else {
            group.cbxCheck.setVisibility(View.GONE);
        }

        // smInfo
        SmInfo info = mDatas.get(filePath);
        if (info == null || info.invalid()) {
            group.imvState.setVisibility(View.INVISIBLE);        //View.INVISIBLE ensure
            // alignment of lines.
            group.txtSummary.setVisibility(View.GONE);        //View.GONE ensure the
            // path-TextView' gravity is center in the parent view,
        } else {
            initGroupInfo(group, info);
        }
        return v;
    }

    /**
     * Initialize summary and indicator.
     *
     * @param group
     * @param info
     */
    protected void initGroupInfo(Group group, SmInfo info) {
        // indicator
        group.imvState.setVisibility(View.VISIBLE);
        group.imvState.setBackgroundResource(getResource(info.getSucOpenFlag()));

        // summary
        group.txtSummary.setVisibility(info.canShowLimit() || info.isNeedApply() ? View.VISIBLE
                : View.GONE);
        group.txtSummary.setText(getSummaryInfo(info));
    }

    /**
     * Get picture resource matches the file state.
     *
     * @param flag
     * @return
     */
    private static int getResource(SucFlag flag) {
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

    private SpannableString getSummaryInfo(SmInfo info) {
        String summary = "";
        if (info.isPayFile() && !info.isNewFile()) {
            if (info.isNeedApply()) {
                return mNumColor.getAllColor("等待激活");
            } else {
                if (info.isCountLimit()) {
                    summary += (info.getOpenCount() - info.getOpenedCount()) + "/"
                            + info.getOpenCount() + " 次";
                } else {
                    summary += "不限次数";
                }

                //				if (!info.isNewFile())
                //				{

                if (info.isPayDataLimit()) {
                    //"Empty" means that this file has not been opened, and now the "remainXXX"
                    // of SmInfo is invalid.
                    if (TextUtils.isEmpty(info.getFirstOpenTime())) {
                        summary += "   剩余 ";
                        if (info.getYears() > 0) {
                            summary += info.getYears() + " 年";
                        }
                        if (info.getDays() > 0) {
                            summary += info.getDays() + " 天";
                        }
                    } else {
                        //Has not out of date
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
                //				}
                //				else
                //				{
                //					if (info.isFreeDataLimit())
                //					{
                //						final long leftDays = info.getFreeLeftDays();
                //						if (leftDays > 0)
                //						{
                //							summary += "   剩余 " + leftDays + " 天";
                //						}
                //						else
                //						{
                //							summary += "   已到期";
                //						}
                //					}
                //					else
                //					{
                //						summary += "   不限天数2";
                //					}
                //				}
            }
        } else {
			/* Free spread file */
            if (info.getOpenCount() == 0) {
                summary += "不限次数";
            } else {
                summary += (info.getOpenCount() - info.getOpenedCount()) + "/"
                        + info.getOpenCount() + " 次";
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
            //Reserve the string like "2015-03-24".
        }

		/* Mark these texts to highlight with different color. */
        return mNumColor.getAssignColor(summary, new ColorPair("已到期", red), new ColorPair("不限天数",
                green), new ColorPair("不限次数", green));
    }

	/*-*********************************************************
	 * child
	 ***********************************************************/

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View v,
                             ViewGroup parent) {
        Child child = null;
        if (v == null) {
            v = mInflater.inflate(R.layout.adapter_sm_child, parent, false);
            child = new Child();
            child.vInfo = v.findViewById(R.id.asc_lyt_info);
            child.vNetError = (ViewStub) v.findViewById(R.id.asc_lyt_net_error);
            child.txtMakeTime = (TextView) v.findViewById(R.id.asc_txt_maketime_content);
            child.txtSeriseName = (TextView) v.findViewById(R.id.asc_txt_serise_name);
            //child.txtClient = (TextView) v.findViewById(R.id.asc_txt_client);
            child.imbRefresh = (ImageButton) v.findViewById(R.id.asc_imb_refresh);
            child.txtMaker = (TextView) v.findViewById(R.id.asc_txt_maker);
            child.txtRemark = (TextView) v.findViewById(R.id.asc_txt_remark);
            child.txtCountContent = (TextView) v.findViewById(R.id.asc_txt_count_content);
            child.txtCountTitle = (TextView) v.findViewById(R.id.asc_txt_count_title);
            child.pbCount = (ProgressBar) v.findViewById(R.id.asc_pb_count);
            child.txtDaysContent = (TextView) v.findViewById(R.id.asc_txt_day_content);
            child.txtDaysTitle = (TextView) v.findViewById(R.id.asc_txt_day_title);
            child.pbDays = (ProgressBar) v.findViewById(R.id.asc_pb_day);
            child.txtSingleOpen = (TextView) v.findViewById(R.id.asc_txt_single_open_time);
            child.btnRead = (Button) v.findViewById(R.id.asc_btn_read);
            child.txtFromTo = (TextView) v.findViewById(R.id.asc_txt_day_from_to);
            child.txtForbid = (TextView) v.findViewById(R.id.asc_txt_abort);
            child.txtShop = (PycUnderLineTextView) v.findViewById(R.id.asc_shop);

            child.txtShop.setVisibility(this.isReceive() ? View.VISIBLE : View.GONE);

            v.setTag(child);
        } else {
            child = (Child) v.getTag();
        }

        final String filePath = mPaths.get(groupPosition);
        SmInfo info = mDatas.get(filePath);
        if (info == null || info.invalid()) {
            showNetErrorView(child, filePath);
        } else {
            initChildInfo(child, info, filePath);
        }

        return v;
    }

    protected void initChildInfo(Child child, final SmInfo info, final String filePath) {

        child.show(child.vInfo);
        child.hide(child.vNetError);

        // make time
        child.txtMakeTime.setText(info.getMakeTime());

        // client
        int client = getClientName(info.getAppType());
        //		child.txtClient.setVisibility(client > 0 ? View.VISIBLE : View.GONE);
        //		child.txtClient.setCompoundDrawablesWithIntrinsicBounds(0, 0, client, 0);

        // series
        child.txtSeriseName.setVisibility(TextUtils.isEmpty(info.getSeriesName()) ? View.GONE
                : View.VISIBLE);
        child.txtSeriseName.setText("系列名称：" + info.getSeriesName());

        // remark
        child.txtMaker.setText("作者 " + info.getNick() + " 说：");
        child.txtRemark.setText(getRemark(info));

        // forbid (only for free file)
        child.txtForbid.setVisibility(info.isMakerAllowed() ? View.GONE : View.VISIBLE);

        // count
        initCount(child, info);

        // day
        initDay(child, info);

        // singleOpen
        initSingleOpen(child, info);

        // read
        child.btnRead.setVisibility(info.canOpen() || info.isPayFile() ? View.VISIBLE : View.GONE);

        child.imbRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSingal(filePath, true);
            }
        });

        child.btnRead.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                TelephonyManager tm = (TelephonyManager) mContext
//                        .getSystemService(Context.TELEPHONY_SERVICE);
//                System.out.println(">>>>DeviceID:" + tm.getDeviceId() + "{"
//                        + tm.getSimSerialNumber() + "}{"
//                        + Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID) + "}");

                Intent intent = new Intent(mContext, SmReaderActivity.class);
                intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, filePath);
                if (mContext instanceof ReceiveActivity) {
                    ((ReceiveActivity) mContext).startActivityForResult(intent,
                            ReceiveActivity.RESULT_READ);
                } else if (mContext instanceof ReceiveSeriesListActivity) {
                    ((ReceiveSeriesListActivity) mContext).startActivityForResult(intent,
                            ReceiveSeriesListActivity.RESULT_READ);
                }
            }
        });

        //浏览店铺
        child.txtShop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (NetUtil.isNetInUse(mContext)) {
                    mShopDialog.show();
                    getSafeUrl(info);
                } else {
                    GlobalToast.toastShort(mContext, "没有可用网络");
                }

            }
        });

    }

    private final ProgressDialog mShopDialog;

    private void getSafeUrl(SmInfo info) {
        //		if (info.getShopUrl() != null)
        //		{
        //			return;
        //		}

        TokenInfo tokenInfo = TokenInfo.getSavedToken(mContext);
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
        //		bundle.putString("logname", "test11");
        //		bundle.putString("grant_type", "client_credentials");

        //请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + tokenInfo.getToken());
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.get(url, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                //mShopDialog.dismiss();
                try {
                    System.out.println(arg0 + "----");
                    JSONObject object = new JSONObject(arg0);
                    JSONObject Result = (JSONObject) object.get("Result");

                    String shopUrl = (String) Result.get("ShopUrl");

                    info.setShopUrl(shopUrl);

                    if (TextUtils.isEmpty(info.getShopUrl())) {
                        View v = ((Activity) mContext).getLayoutInflater().inflate(
                                R.layout.dialog_no_shop, null);

                        final Dialog dialog = new Dialog(mContext, R.style.no_frame_small);
                        dialog.setContentView(v);
                        dialog.show();

                        TextView t = (TextView) v.findViewById(R.id.dd_txt_content);
                        Button b1 = (Button) v.findViewById(R.id.dd_btn_sure);

                        t.setText("作者尚未开设店铺");
                        b1.setOnClickListener(new OnClickListener() {

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
                        (mContext).startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    mShopDialog.dismiss();
                }
            }

            @Override
            public void onFinished() {
                mShopDialog.dismiss();
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

        GlobalHttp.post(url, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                //解析Json
                try {
                    JSONObject object = new JSONObject(arg0);
                    tokenString = (String) object.get("access_token");
                    int timestmp = object.getInt("expires_in");
                    TokenInfo tokenInfo = new TokenInfo();
                    tokenInfo.setToken(tokenString);
                    tokenInfo.setExpires_in(System.currentTimeMillis() + timestmp);
                    TokenInfo.saveToken(mContext, tokenInfo);
                    getShopUrl(info, tokenInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                    mShopDialog.dismiss();
                }
            }

            @Override
            public void onFinished() {
                mShopDialog.dismiss();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
            }

            @Override
            public void onCancelled(CancelledException arg0) {
            }
        });
    }

    private void showNetErrorView(Child child, final String filePath) {
        if (child.btnConnect == null) {
            View netErrorView = child.vNetError.inflate();
            child.btnConnect = (Button) netErrorView.findViewById(R.id.vsn_btn_connect);
        }
        child.btnConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSingal(filePath, true);
            }
        });
        child.show(child.vNetError);
        child.hide(child.vInfo);
    }

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
                    final int totalDays = (int) DataConvert.transformTime(info.getStartTime(),
                            info.getEndTime());
                    final int leftDays = (int) info.getFreeLeftDays();
                    strDays += "剩余 " + leftDays + " 天，共 " + totalDays + " 天";

                    child.pbDays.setMax(totalDays);
                    child.pbDays.setProgress(leftDays);
                    child.txtFromTo.setText(mNumColor.getPartColor("从 " + info.getStartTime()
                            + " 到 " + info.getEndTime()));
                }
            }
        }    // payFile结束
        else {
            if (info.isFreeDataLimit()) {
                child.show(child.pbDays);
                child.show(child.txtFromTo);
                final int totalDays = (int) DataConvert.transformTime(info.getStartTime(),
                        info.getEndTime());
                final int leftDays = (int) info.getFreeLeftDays();
                strDays += "剩余 " + leftDays + " 天，共 " + totalDays + " 天";

                child.pbDays.setMax(totalDays);
                child.pbDays.setProgress(leftDays);
                child.txtFromTo.setText(mNumColor.getPartColor("从 " + info.getStartTime() + " 到 "
                        + info.getEndTime()));
            }
        }

        if (TextUtils.isEmpty(strDays)) {
            child.txtDaysContent.setText(mNumColor.getAllColor("不限制"));
        } else {
            child.txtDaysContent.setText(mNumColor.getPartColor(strDays));
        }
    }

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

    private void initSingleOpen(Child child, SmInfo info) {
        child.hide(child.txtSingleOpen);        // 先隐藏，有值再show
        if (info.isPayFile()) {
            if (!info.isNeedApply() && !TextUtils.isEmpty(info.getFirstOpenTime())) {
                child.show(child.txtSingleOpen);
                child.txtSingleOpen.setText(mNumColor.getPartColor("首次阅读："
                        + info.getFirstOpenTime()));
            }
        } else {
            if (info.getSingleOpenTime() > 0) {
                child.show(child.txtSingleOpen);
                child.txtSingleOpen.setText(mNumColor.getPartColor("每次能看："
                        + DataConvert.toTime(info.getSingleOpenTime())));
            }
        }

    }

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

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    protected class Group {
        public ImageView imvState;
        public TextView txtSummary;
        TextView txtName;
        ImageView imvIndicator;
        CheckBox cbxCheck;
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

        public void show(View v) {
            v.setVisibility(View.VISIBLE);
        }

        public void hide(View v) {
            v.setVisibility(View.GONE);
        }
    }

    private int getClientName(int appType) {
        int client = 0;
        switch (appType) {
            case 18:
                client = R.drawable.pc;
                break;

            case 28:
                client = R.drawable.android;
                break;

            case 29:
                client = R.drawable.wp;
                break;

            case 30:
                client = R.drawable.iphone;
                break;

            default:
                break;
        }
        return client;
    }

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

        mSelectListener.onSelcetChanged(false, mSelectPaths.size(),
                mSelectPaths.size() == mPaths.size());
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

        mSelectListener.onSelcetChanged(false, mSelectPaths.size(),
                mSelectPaths.size() == mPaths.size());
    }

    @Override
    public Collection<String> getSelected() {
        return mSelectPaths;
    }

    private OnCheckedChangeListener checkedChangedListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String path = (String) buttonView.getTag();
            if (isChecked) {
                mSelectPaths.add(path);
            } else {
                mSelectPaths.remove(path);
            }
            mSelectListener.onSelcetChanged(false, mSelectPaths.size(),
                    mSelectPaths.size() == mPaths.size());
        }
    };

    @Override
    public void clearSelected() {
    }

}
