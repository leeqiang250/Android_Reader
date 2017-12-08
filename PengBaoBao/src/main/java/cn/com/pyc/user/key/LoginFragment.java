package cn.com.pyc.user.key;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlk.util.global.GlobalToast;
import com.sz.help.KeyHelp;
import com.sz.mobilesdk.models.BaseModel;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SecurityUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.tencent.mm.opensdk.utils.Log;
import com.tencent.tauth.Tencent;

import org.xutils.common.Callback;

import cn.com.pyc.base.PbbBaseFragment;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.bean.event.BaseEvent;
import cn.com.pyc.bean.event.ConductUIEvent;
import cn.com.pyc.connect.NetworkRequest;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.model.ClientCredentials_Model;
import cn.com.pyc.model.Userinfo_Model;
import cn.com.pyc.model.Userlogin_Model;
import cn.com.pyc.model.Useruidkey_Model;
import cn.com.pyc.pbb.R;
import cn.com.pyc.suizhi.bean.event.DBMakerEvent;
import cn.com.pyc.suizhi.bean.event.PicEvent;
import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.SZConfig;
import cn.com.pyc.suizhi.common.SZConstant;
import cn.com.pyc.suizhi.manager.HttpEngine;
import cn.com.pyc.suizhi.model.LoginModel;
import cn.com.pyc.suizhi.service.BGOCommandService;
import cn.com.pyc.suizhi.util.DRMUtil;
import cn.com.pyc.suizhi.util.SZAPIUtil;
import cn.com.pyc.suizhi.util.SZPathUtil;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.utils.ClearKeyUtil;
import cn.com.pyc.utils.LayOutUtil;
import cn.com.pyc.utils.SoftKeyBoardListener;
import cn.com.pyc.widget.PycUnderLineOrangeTextView;
import cn.com.pyc.widget.PycUnderLineTextView;
import cn.com.pyc.widget.WidgetTool;
import cn.com.pyc.xcoder.XCoder;
import de.greenrobot.event.EventBus;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (登陆界面)
 * @date 2016-11-9 下午5:11:59
 */
public class LoginFragment extends PbbBaseFragment implements OnCheckedChangeListener,
        OnClickListener {

    private UserInfo userInfo;
    private EditText g_edtEmail;//用户名邮箱手机号
    private EditText g_edtPsd;//密码

    private PycUnderLineOrangeTextView m_fke_txt_forget;//忘记密码
    private TextView m_fke_btn_login;//登录
    private PycUnderLineOrangeTextView m_fke_txt_rigister;//注册
    private ImageButton m_fke_imb_QQ;//QQ快速登录
    private Activity ctx;
    private UserDao db;
    private ImageView qq_login;//QQ登录
    private Tencent mTencent;
    private RelativeLayout rootLayout;
    private LinearLayout rootLl;
    public static TextView tv_login;
    public static ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_key_email, null);
        init_view(v);
        init_listener();
        ctx = getActivity();
        init_data();
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * @Description: (用一句话描述该文件做什么)
     * @author 李巷阳
     * @date 2016-11-9 下午4:15:58
     */
    private void init_view(View v) {
        qq_login = (ImageView) v.findViewById(R.id.qq_login);
        g_edtEmail = (EditText) v.findViewById(R.id.fke_edt_email);
        g_edtPsd = (EditText) v.findViewById(R.id.fke_edt_psd);
        m_fke_txt_forget = (PycUnderLineOrangeTextView) v.findViewById(R.id.fke_txt_forget);
        m_fke_btn_login = (TextView) v.findViewById(R.id.fke_btn_login);
        m_fke_txt_rigister = (PycUnderLineOrangeTextView) v.findViewById(R.id.fke_txt_rigister);
        m_fke_imb_QQ = (ImageButton) v.findViewById(R.id.fke_imb_QQ);
        ((CheckBox) v.findViewById(R.id.fke_cbx_show)).setOnCheckedChangeListener(this);
        v.findViewById(R.id.fke_edt_email).requestFocus();
        qq_login.setOnClickListener(this);

//        m_fke_btn_login.setClickable();
//        m_fke_btn_login.setOnFocusChangeListener();
        rootLayout = (RelativeLayout) v.findViewById(R.id.root_rl);
        rootLl = (LinearLayout) v.findViewById(R.id.ll_root);
        LinearLayout ll_pbb = (LinearLayout) v.findViewById(R.id.ll_pbb);
        tv_login = (TextView) v.findViewById(R.id.tv_login);
        imageView = (ImageView) v.findViewById(R.id.iv_animator);
        final LayOutUtil layOutUtil = new LayOutUtil(getActivity(),ll_pbb,rootLl);
        SoftKeyBoardListener.setListener(getActivity(), new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                layOutUtil.playUpAnimator(true);
            }
            @Override
            public void keyBoardHide(int height) {
                layOutUtil.playUpAnimator(false);
            }
        });
        g_edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(g_edtEmail.getText().toString().trim()) && !TextUtils.isEmpty(g_edtPsd.getText().toString().trim())) {
                    m_fke_btn_login.setBackground(ctx.getResources().getDrawable(R.drawable.xml_click_btn));
                    m_fke_btn_login.setTextColor(ctx.getResources().getColor(R.color.black));
                }else {
                    m_fke_btn_login.setBackground(ctx.getResources().getDrawable(R.drawable.xml_unclick_btn));
                    m_fke_btn_login.setTextColor(ctx.getResources().getColor(R.color.gray));
                }
            }
        });

        g_edtPsd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(g_edtEmail.getText().toString().trim()) && !TextUtils.isEmpty(g_edtPsd.getText().toString().trim())) {
                    m_fke_btn_login.setBackground(ctx.getResources().getDrawable(R.drawable.xml_click_btn));
                    m_fke_btn_login.setTextColor(ctx.getResources().getColor(R.color.black));
                }else {
                    m_fke_btn_login.setBackground(ctx.getResources().getDrawable(R.drawable.xml_unclick_btn));
                    m_fke_btn_login.setTextColor(ctx.getResources().getColor(R.color.gray));
                }
            }
        });
    }

    /**
     * @Description: (用一句话描述该文件做什么)
     * @author 李巷阳
     * @date 2016-11-9 下午4:15:59
     */
    private void init_listener() {
        m_fke_txt_forget.setOnClickListener(this);
        m_fke_btn_login.setOnClickListener(this);
        m_fke_txt_rigister.setOnClickListener(this);
        m_fke_imb_QQ.setOnClickListener(this);
    }

    /**
     * @Description: (用一句话描述该文件做什么)
     * @author 李巷阳
     * @date 2016-11-9 下午4:16:01
     */
    private void init_data() {
        db = UserDao.getDB(getActivity());
        userInfo = db.getUserInfo();
        ((KeyActivity) getActivity()).showKeyboard();
    }

    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.qq_login:
                //第三方登录QQ
                loginQQ();
                break;
            // 登陆
            case R.id.fke_btn_login:
                loginEmail(getUserName(), getPassWord());
                break;
            // 忘记密码
            case R.id.fke_txt_forget:
                changeFragment(Pbb_Fields.TAG_KEY_PSD);
                break;
            // 新用户注册
            case R.id.fke_txt_rigister:
                // Intent intent = new Intent();
                // intent.putExtra(KeyActivity.TAG_KEY_CURRENT,
                // KeyActivity.TAG_KEY_REGISTER);
                // startActivity(intent, bundle);
                bundle.putString(GlobalIntentKeys.BUNDLE_DATA_TYPE, RegisterFragment2.TYPE_NICK);
                changeFragment(Pbb_Fields.TAG_KEY_REGISTER, bundle);
                break;
            // QQ快速登录
            case R.id.fke_imb_QQ:
                bundle.putString(GlobalIntentKeys.BUNDLE_DATA_TYPE, QqFragment.TYPE_FIND_KEY);
                changeFragment(Pbb_Fields.TAG_KEY_QQ, bundle);
                break;

            default:
                break;
        }
    }

    /**
     * @Params :
     * @Author :songyumei
     * @Date :2017/8/7
     */
    private void loginQQ() {
        //Toast.makeText(getActivity(), "qq登录", Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        bundle.putString(GlobalIntentKeys.BUNDLE_DATA_TYPE, QqFragment.TYPE_FIND_KEY);
        changeFragment(Pbb_Fields.TAG_KEY_QQ, bundle);
    }

    /**
     * @author 李巷阳
     * @date 2016-10-18 下午6:28:23
     */
    public String getPassWord() {
        return g_edtPsd.getText().toString().trim();
    }

    /**
     * @author 李巷阳
     * @date 2016-10-18 下午6:01:33
     */
    public String getUserName() {
        return g_edtEmail.getText().toString().trim();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        WidgetTool.changVisible(g_edtPsd, isChecked);
    }


    //	1. 首先获取客户端得ClientToken的值。(请求头必须有ClientToken)
    //	2. ClientToken + 用户名 + 密码  获取用户信息,保存到数据库中。
    //	3. 用户名 + ClientToken  登陆。
    //  4. 用户名     获取用户信息并保存
    public void loginEmail(final String email, final String psd) {
        if (!CommonUtil.isNetConnect(ctx)) {
            GlobalToast.toastCenter(ctx, ctx.getResources().getString(R.string
                    .pbb_net_disconnected));
            return;
        }

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(psd)) {
            GlobalToast.toastCenter(getActivity(), ctx.getResources().getString(R.string
                    .pbb_login_account_not_empty));
            return;
        }
        showBgLoading(ctx, ctx.getResources().getString(R.string.pbb_login_ing));
        getClientToken(userInfo, email, psd);
    }

    //	1. 首先获取客户端得ClientToken的值。(请求头必须有ClientToken)
    private void getClientToken(final UserInfo userInfo, final String email, final String psd) {
        NetworkRequest.getClientToken(new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(String arg0) {
                try {
                    ClientCredentials_Model mClientCredentials_Model = JSONObject.parseObject
                            (arg0, ClientCredentials_Model.class);
                    String clientToken = mClientCredentials_Model.getAccess_token();
                    logIn(userInfo, email, psd, clientToken);
                } catch (Exception e) {
                    e.printStackTrace();
                    load_error();
                }
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onError() {
                load_error();
            }

            @Override
            public void onCancelled(Exception arg0) {
                hideBgLoading();
            }
        });
    }


    // 2. 通过ClientToken + 用户名 + 密码登陆  获取用户信息,保存到数据库中。
    private void logIn(final UserInfo uinfo, final String email,
                       final String psd, final String clientToken) {
        NetworkRequest.logIn(email, psd, clientToken, new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(String arg0) {
                try {
                    Userlogin_Model mUserlogin_Model = JSONObject.parseObject(arg0,
                            Userlogin_Model.class);
                    String status = mUserlogin_Model.getStatus();
                    if (status.equals("1")) {
                        String Result = mUserlogin_Model.getResult();
                        uinfo.setUserName(Result);
                        getUID(uinfo, clientToken, mUserlogin_Model.getResult());
                    } else if (status.equals("0")) {
                        GlobalToast.toastShort(getActivity(), mUserlogin_Model.getMessage());
                        hideBgLoading();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    load_error();
                }
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onError() {
                load_error();
            }

            @Override
            public void onCancelled(Exception arg0) {
                hideBgLoading();
            }
        });
    }

    // 3. 用户名 + ClientToken  获取UID
    private void getUID(final UserInfo uinfo, final String clientToken, final String loginname) {
        NetworkRequest.getUID(uinfo, clientToken, new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(String arg0) {
                try {
                    Useruidkey_Model mUseruidkey_Model = JSONObject.parseObject(arg0,
                            Useruidkey_Model.class);
                    String status = mUseruidkey_Model.getStatus();
                    if (status.equals("1")) {
                        String Result = mUseruidkey_Model.getResult();
                        byte[] byteResult = Result.getBytes();
                        if (!byteResult.equals(null)) {
                            uinfo.setUid(byteResult);
                            getUserInfo(uinfo, clientToken, loginname);
                        }
                    } else {
                        GlobalToast.toastShort(getActivity(), mUseruidkey_Model.getMessage());
                        hideBgLoading();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    load_error();
                }
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onError() {
                load_error();
            }

            @Override
            public void onCancelled(Exception arg0) {
                hideBgLoading();
            }
        });
    }

    // 4. 获取用户信息并保存
    private void getUserInfo(final UserInfo uinfo, final String clientToken, final String
            loginname) {
        NetworkRequest.getUserInfo(uinfo, clientToken, new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(String arg0) {
                try {
                    Userinfo_Model userLogin = JSONObject.parseObject(arg0, Userinfo_Model.class);
                    String status = userLogin.getStatus();
                    if (status.equals("1")) {
                        suizhiLogin(uinfo, loginname, userLogin);
                    } else if (status.equals("0")) {
                        GlobalToast.toastShort(getActivity(), userLogin.getMessage());
                        hideBgLoading();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    load_error();
                }
            }

            @Override
            public void onFinished() {
                //hideBgLoading();
            }

            @Override
            public void onError() {
                load_error();
            }

            @Override
            public void onCancelled(Exception arg0) {
                hideBgLoading();
            }
        });
    }

    /***
     * 登录
     *
     * @param loginName 输入的用户名
     * @param passWord  密码
     * @param callback  回调函数
     */
    public static void loginV2(String loginName, String passWord,
                               Callback.CommonCallback<String> callback) {
        Bundle bundle = new Bundle();
        bundle.putString("loginName", loginName);
        bundle.putString("password", SecurityUtil.getParamByMD5(loginName, passWord));
        cn.com.pyc.suizhi.manager.HttpEngine.post(SZAPIUtil.getLoginV2Url(), bundle, callback);
    }

    //登录随知服务器，获取token
    private void suizhiLogin(final UserInfo uinfo, final String loginname, final Userinfo_Model
            userLogin) {
        SZConfig.LoginConfig.type = DrmPat.LOGIN_GENERAL;
        loginV2(loginname, SZConfig.PASSWORD,
                new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        LoginModel o = JSON.parseObject(result, LoginModel.class);
                        if (o.isSuccess()) {
                            setUInfoData(uinfo, userLogin);
                            db.saveUserInfo(uinfo);
                            //更新用户头像
                            EventBus.getDefault().post(new PicEvent(BaseEvent.Type.UI_PIC_ICON,
                                    userLogin.getResult().getPicUrl()));

                            //保存随知服务器返回的信息
                            loginSuccess(o.getData());
                        } else {
                            // 失败
                            loginFail(o.getMsg());
                        }
                    }

                    @Override
                    public void onFinished() {
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        if (isAdded()) {
                            loginFail(getString(R.string.login_fail_disconnected));
                        }
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                    }
                });
    }
    // 登录失败
    private void loginFail(String failStr) {
        hideBgLoading();
        UIHelper.showToast(getActivity(), failStr);
    }

    //登录成功,(密码要加密保存？)
    private void loginSuccess(LoginModel.LoginInfo o) {
        SZConstant.setAccountId(o.getAccountId());
        SZConstant.setName(o.getUsername());
        SZConstant.setToken(o.getToken());

        SPUtil.save(KeyHelp.KEY_VISIT_NAME, getUserName()); //登录输入的用户名称
        SPUtil.save(KeyHelp.KEY_VISIT_PWD, getPassWord());

        //注册设备
        registerDeviceInfo();
    }

    /**
     * 注册设备信息
     */
    public void registerDeviceInfo() {
        Bundle bundle = DRMUtil.getEquipmentInfos();
        HttpEngine.post(SZAPIUtil.getEquipmentInfoUrl(), bundle, new Callback
                .CommonCallback<String>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                UIHelper.showToast(ctx, ctx.getString(R.string.register_device_fail));
                //注册设备失败，移除保存的key值
                ClearKeyUtil.removeKey();
                hideBgLoading();
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onSuccess(String result) {
                BaseModel model = JSON.parseObject(result, BaseModel.class);
                if (model == null) {
                    //注册设备失败，移除保存的key值
                    hideBgLoading();
                    ClearKeyUtil.removeKey();
                    UIHelper.showToast(ctx, ctx.getString(R.string.register_device_fail));
                    return;
                }
                if (model.isSuccess()) {
                    //更新发现页
                    EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UPDATE_DISCOVER));
                    // 创建应用文件保存目录
                    SZPathUtil.createFilePath();
                    // 开启服务，创建需要的数据表。
                    BGOCommandService.startBGOService(getActivity(), BGOCommandService.CREATE_DB);
                } else {
                    //注册设备失败，移除保存的key值
                    hideBgLoading();
                    ClearKeyUtil.removeKey();
                    UIHelper.showToast(ctx, model.getMsg());
                }
            }
        });
    }

    //接收来自BGOCommandService服务创建db的通知
    public void onEventMainThread(DBMakerEvent event) {
        boolean isDBMaker = event.isMaker();
        if (isDBMaker) {
            //跳转页面，此时才是真正的登录流程成功！
            hideBgLoading();
            startActivity(new Intent(getActivity(), KeySuccessActivity.class));
        }
    }

    /**
     * 把userinfoModel设置到UserInfo对象中。
     *
     * @author 李巷阳
     * @date 2016/12/6 14:58
     */
    private void setUInfoData(UserInfo uinfo, Userinfo_Model userLogin) {
        uinfo.setUserName(userLogin.getResult().getUserName());
        uinfo.setNick(userLogin.getResult().getUserNick());
        uinfo.setPhone(userLogin.getResult().getMobilePhone());
        if (userLogin.getResult().getMobileStatus().equals("1")) {
            uinfo.setPhoneBinded(true);
        } else if (userLogin.getResult().getMobileStatus().equals("0")) {
            uinfo.setPhoneBinded(false);
        }
        uinfo.setEmail(userLogin.getResult().getEmail());
        if (userLogin.getResult().getEmailStatus().equals("1")) {
            uinfo.setEmailBinded(true);
        } else if (userLogin.getResult().getEmailStatus().equals("0")) {
            uinfo.setEmailBinded(false);
        }
        if (!TextUtils.isEmpty(userLogin.getResult().getQQNick())) {
            uinfo.setQqBinded(true);
            uinfo.setQqBinded(1);
            uinfo.setQqNick(userLogin.getResult().getQQNick());
        } else {
            uinfo.setQqBinded(false);
            uinfo.setQqBinded(0);
        }

        if (userLogin.getResult().getIsEnterpriseChild().equals("1")) {
            uinfo.setStatus(1);
        } else if (userLogin.getResult().getIsEnterpriseChild().equals("0")) {
            uinfo.setStatus(0);
        }

        // 解密服务器返回的密码
        uinfo.setPsd(XCoder.getHttpDecryptText(userLogin.getResult().getPassword()));
    }

    /**
     * @Description: (访问服务器失败)
     * @author 李巷阳
     * @date 2016/11/18 14:38
     */
    private void load_error() {
        hideBgLoading();
        GlobalToast.toastCenter(getActivity(), ctx.getResources().getString(R.string
                .pbb_access_server_failed));
    }
}
