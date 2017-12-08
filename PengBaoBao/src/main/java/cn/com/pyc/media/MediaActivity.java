package cn.com.pyc.media;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.qlk.util.base.BaseFragment;
import com.qlk.util.base.BaseFragment.OnChangeFragmentListener;
import com.qlk.util.global.GlobalObserver;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;

import java.util.Observable;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.base.ExtraBaseApplication;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.conn.UserConnect;
import cn.com.pyc.conn.UserResult;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.user.InsertPsdActivity;
import cn.com.pyc.user.Pbb_Fields;


/**
 * You can go to any fragment this activity controls, only tell which tag of the
 * tags in the activity by the intent.<br>
 * If the tag is null, it will be set to "TAG_CIPHER_TOTAL" or "TAG_PLAIN_TOTAL"
 * by the intent's key of "GlobalIntentKeys.BUNDLE_FLAG_CIPHER".
 * <p>
 * To call this activity, you should set three items: fragment tag, "isCipher"
 * flag and "isFromSm" flag.
 *
 * @author QiLiKing 2015-8-18 上午11:01:22
 */
/**   
*
* @Description: (此activity包含明文,密文数量列表,以及各个多媒体播放器的跳转中转站。)
* @author 李巷阳
* @date 2016/11/30 11:15 
*/
public class MediaActivity extends ExtraBaseActivity implements OnChangeFragmentListener {

    private String cipher_total_or_plain_total;
    private boolean popBackStack = false;
    private boolean isBackground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_cipher);
        ViewHelp.showAppTintStatusBar(this);
        init_value();
        onChangeFragment(cipher_total_or_plain_total, getIntent().getExtras());
    }

    private void init_value() {
        // 是否是密文环境 true:密文;false:明文。
        boolean isCipher = getIntent().getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
        // 显示密文列表，还是明文列表
        // tag值为 Pbb_Fields.TAG_CIPHER_TOTAL或Pbb_Fields.tag_plain_total
        cipher_total_or_plain_total = getIntent().getStringExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE);
        if (cipher_total_or_plain_total == null) {
            if (isCipher) {
                cipher_total_or_plain_total = Pbb_Fields.TAG_CIPHER_TOTAL;
            } else {
                cipher_total_or_plain_total = Pbb_Fields.TAG_PLAIN_TOTAL;
            }
        }
        // 如果是隐私空间,则需要和服务端比较下密码是否变化,如果变化，则需要重新输入隐私空间密码。
        if (isCipher && cipher_total_or_plain_total.equals(Pbb_Fields.TAG_CIPHER_TOTAL)) {
            // 联网同步服务器密码，查看密码是否变化。
            GlobalTask.executeBackground(psdModifiedTask);
        }
    }


    /**
     * @author 李巷阳
     * @date 2016-11-11 下午5:43:40
     */
    public void onChangeFragment(String tag, Bundle bundle) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment curFragment = null;
        for (String t : Pbb_Fields.TAGS) {
            curFragment = fm.findFragmentByTag(t);
            if (curFragment != null) {
                ft.hide(curFragment);
            }
        }
        curFragment = fm.findFragmentByTag(tag);
        if (curFragment == null) {
            switch (tag) {
                case Pbb_Fields.TAG_CIPHER_TOTAL:// 计算隐私空间的条数
                    curFragment = new CipherTotalFragment();
                    break;
                case Pbb_Fields.TAG_PLAIN_TOTAL:// 计算明文空间的条数
                    curFragment = new PlainTotalFragment();
                    break;
                // 隐私空间的图片
                // 隐私空间的文档
                // 隐私空间的视频
                case Pbb_Fields.TAG_CIPHER_IMAGE:
                case Pbb_Fields.TAG_CIPHER_FILE:
                case Pbb_Fields.TAG_CIPHER_VIDEO:
                    curFragment = new CipherBaseFragment();
                    if (bundle == null) {
                        bundle = new Bundle();
                    }
                    bundle.putString(GlobalIntentKeys.BUNDLE_DATA_TYPE, tag);
                    break;
                // 明文空间的图片
                // 明文空间的文档
                // 明文空间的视频
                case Pbb_Fields.TAG_PLAIN_IMAGE:
                case Pbb_Fields.TAG_PLAIN_FILE:
                case Pbb_Fields.TAG_PLAIN_VIDEO:
                case Pbb_Fields.TAG_PLAIN_MUSIC:
                    curFragment = new PlainBaseFragment();
                    if (bundle == null) {
                        bundle = new Bundle();
                    }
                    bundle.putString(GlobalIntentKeys.BUNDLE_DATA_TYPE, tag);
                    break;
                // 明文文件夹显示界面
                case Pbb_Fields.TAG_PLAIN_IMAGE_SORT:
                    curFragment = new PlainImageSortFragment();
                    break;
                default:
                    break;
            }
            curFragment.setArguments(bundle);
        }

        if (curFragment.isAdded()) {
            ft.show(curFragment);
        } else {
            ft.add(R.id.amc_lyt_content, curFragment, tag);
            if (!tag.equals(cipher_total_or_plain_total)) {
                ft.addToBackStack(Pbb_Fields.BACK_STACK_NAME);
            }
        }

        if (tag.equals(cipher_total_or_plain_total)) {
            popBackStack = true;
            // Here is no method called popBackStackAllowingStateLoss.
            if (!isBackground) {
                getFragmentManager().popBackStack(Pbb_Fields.BACK_STACK_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }

        ft.commitAllowingStateLoss();
    }

    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        switch ((ObTag) data) {
            case Encrypt:
                // 通过内容监听者替换fragment。
                onChangeFragment(Pbb_Fields.TAG_CIPHER_TOTAL, null);
                break;
            case Refresh:
            case Decrypt:
                // 通过内容监听者,刷新UI。
                BaseFragment fragment = null;
                FragmentManager fm = getFragmentManager();
                for (String t : Pbb_Fields.TAGS) {
                    fragment = (BaseFragment) fm.findFragmentByTag(t);
                    if (fragment != null && fragment.isVisible()) {
                        fragment.refreshUI();
                    }
                }
                break;
            case Make:
                // 通过内容监听者关闭此activity。
                finish();
                break;

            default:
                break;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        isBackground = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackground = false;
        if (popBackStack) {
            popBackStack = false;
            getFragmentManager().popBackStack(Pbb_Fields.BACK_STACK_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    /**
     * 需求规定：进入隐私空间后都要同步服务器密码，如果密码变化，则需要重新进入隐私空间
     */
    private Runnable psdModifiedTask = new Runnable() {
        @Override
        public void run() {
            UserInfo userInfo = UserDao.getDB(MediaActivity.this).getUserInfo();
            UserResult ur = new UserConnect(MediaActivity.this).synchronizePsd();
            if (ur.getUserInfo() != null) {
                UserInfo rcvInfo = ur.getUserInfo();
                if (!rcvInfo.getPsd().equals(userInfo.getPsd())) {
                    // 如果初始密码为空，则不用再跳转
                    userInfo.setPsd(rcvInfo.getPsd());
                    if (ExtraBaseApplication.isCurrentFocusInCipherSpace()) {
                        GlobalObserver.getGOb().postNotifyObservers(ObTag.Psd);
                        GlobalToast.toastShort(MediaActivity.this, "密码已改变，需要重新输入");
                        Intent intent = new Intent(MediaActivity.this, InsertPsdActivity.class);
                        intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, Pbb_Fields.TYPE_INSERT_CIPHER);
                        startActivity(intent);
                    }
                }
                userInfo.setMoney(rcvInfo.getMoney());
                UserDao.getDB(MediaActivity.this).saveUserInfo(userInfo);
            }

        }
    };
}
