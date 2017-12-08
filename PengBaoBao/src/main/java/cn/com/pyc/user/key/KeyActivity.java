package cn.com.pyc.user.key;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.TextView;

import com.qlk.util.base.BaseFragment.OnChangeFragmentListener;

import java.util.Observable;
import java.util.Stack;

import cn.com.pyc.pbb.R;
import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.user.Pbb_Fields;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (存放登陆, 注册等, fragment的activity容器)
 * @date 2016-11-9 上午10:41:18
 */
public class KeyActivity extends ExtraBaseActivity implements OnChangeFragmentListener {

    private final Stack<String> stack = new Stack<String>();    // 栈在这里挺适合的

    public UserInfo userInfo;
    private String desTag;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_key);
        ViewHelp.showAppTintStatusBar(this);
        init_value();
        onChangeFragment(desTag, getIntent().getExtras()); // 如果是隐私空间的RegisterFragment，则需要bundle
    }

    /**
     * @Description: (初始化值)
     * @author 李巷阳
     * @date 2016-11-9 上午10:46:54
     */
    private void init_value() {
        desTag = getIntent().getStringExtra(Pbb_Fields.TAG_KEY_CURRENT);// 获取要开启fragment的标志。
        if (desTag == null) {
            desTag = Pbb_Fields.TAG_KEY_KEY; // 默认值    已有账号
        }
    }

    @Override
    public void onChangeFragment(String tag, Bundle data) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if (!stack.empty()) {
            ft.hide(fm.findFragmentByTag(stack.peek()));
        }

        Fragment curFragment = fm.findFragmentByTag(tag);
        //		if (curFragment == null)
        //		{
        if (tag.equals(Pbb_Fields.TAG_KEY_LOGIN)) {
            // 登陆界面
            curFragment = new LoginFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_KEY)) {
            // 新用户注册，老用户找回
            curFragment = new KeyFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_NICK)) {
            // 绑定QQ，手动输入昵称
            curFragment = new NickFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_NICK2)) {
            // 输入昵称，继续
            curFragment = new Nick2Fragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_OLD_USER)) {
            // QQ登陆，邮箱登陆
            curFragment = new OldUserFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_PSD)) {
            // 忘记密码
            curFragment = new FindPsdFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_QQ)) {
            // QQ快速登录
            curFragment = new QqFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_QQ_FAILURE)) {
            // 通过邮箱找回
            curFragment = new QqFailureFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_REGISTER)) {
            // 注册
            curFragment = new RegisterFragment2();
        } else {
            throw new NullPointerException("tag is null : " + tag);
        }
        curFragment.setArguments(data); // 主要是ImageSortFragment用到
        //		} else {
        //			//fragment不能重复setArguments
        //			Bundle bundle = curFragment.getArguments();
        //			if (bundle != null) {
        //				bundle.clear();
        //				bundle.putAll(data);
        //			}
        //		}

        if (tag.equals(Pbb_Fields.TAG_KEY_LOGIN)) {
            // 登陆界面
            curFragment = new LoginFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_KEY)) {
            // 新用户注册，老用户找回
            curFragment = new KeyFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_NICK)) {
            // 绑定QQ，手动输入昵称
            curFragment = new NickFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_NICK2)) {
            // 输入昵称，继续
            curFragment = new Nick2Fragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_OLD_USER)) {
            // QQ登陆，邮箱登陆
            curFragment = new OldUserFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_PSD)) {
            // 找回密码
            curFragment = new FindPsdFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_QQ)) {
            // QQ快速登录
            curFragment = new QqFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_QQ_FAILURE)) {
            // 通过邮箱找回
            curFragment = new QqFailureFragment();
        } else if (tag.equals(Pbb_Fields.TAG_KEY_REGISTER)) {
            // 新用户注册
            curFragment = new RegisterFragment2();
        } else {
            throw new NullPointerException("tag is null : " + tag);
        }
        curFragment.setArguments(data); // 主要是ImageSortFragment用到

        ft.add(R.id.ak_lyt_content, curFragment, tag);
        if (!stack.empty()) {
            ft.addToBackStack(null); // 第一个不加入堆栈，返回时就正常了
        }

        ft.commit();

        stack.push(tag);
//        initView(tag);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!stack.empty()) {
            stack.pop();
        }

        if (!stack.empty()) {
//            initView(stack.peek());
        }
    }

    public void setMyTitle(String title) {
        ((TextView) findViewById(R.id.ak_txt_title)).setText(title);
    }

    private void initView(String tag) {
        if (tag.equals(Pbb_Fields.TAG_KEY_KEY)) {
            setMyTitle("已有账号登录");
        } else if (tag.equals(Pbb_Fields.TAG_KEY_LOGIN)) {
            setMyTitle("登录");
        } else if (tag.equals(Pbb_Fields.TAG_KEY_LOGIN)) {
            setMyTitle("邮箱登录");
        } else if (tag.equals(Pbb_Fields.TAG_KEY_NICK) || tag.equals(Pbb_Fields.TAG_KEY_NICK2)) {
            setMyTitle("告诉好友你是谁");
        } else if (tag.equals(Pbb_Fields.TAG_KEY_OLD_USER)) {
            setMyTitle("已有账号登录");
        } else if (tag.equals(Pbb_Fields.TAG_KEY_QQ)) {
            setMyTitle("QQ登录");
        } else if (tag.equals(Pbb_Fields.TAG_KEY_QQ_FAILURE)) {
            setMyTitle("需要注意");
        } else if (tag.equals(Pbb_Fields.TAG_KEY_PSD)) {
            setMyTitle("找回密码");
        }
    }

    /**
     * 如果登陆成功后调用GlobalObserver.getGOb().notifyObservers(ObTag.Key);观察者模式。
     * 此activity继承自BaseActivity,BaseActivity是被观察者。所以会执行update方法。
     *
     * @author 李巷阳
     * @date 2016/12/2 11:12
     */
    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        if (data.equals(ObTag.Key)) {
            finish();
        }
    }

}
