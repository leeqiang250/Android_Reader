package cn.com.pyc.plain;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

import com.qlk.util.base.BaseFragment;
import com.qlk.util.tool.Util.ScreenUtil;

import cn.com.pyc.pbb.R;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.media.MediaActivity;
import cn.com.pyc.plain.record.MusicRecordActivity;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.user.UserInfoActivity;

import static cn.com.pyc.pbb.R.id.fpc_imb_scancode;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (主界面查看本地文件和录制文件两个模块按钮)
 * @date 2016-11-11 下午3:57:57
 */
public class PlainChanelFragment extends BaseFragment implements OnClickListener {

    private ImageButton m_fpc_imb_scancode;
    private ImageButton m_fpc_imb_takephoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_plain_chanel, container, false);
        init_view(v);
        init_listener();
        return v;
    }

    /**
     * @author 李巷阳
     * @date 2016-11-11 下午3:59:06
     */
    private void init_view(View v) {
        m_fpc_imb_scancode = (ImageButton) v.findViewById(fpc_imb_scancode);//	本地文件
        m_fpc_imb_takephoto = (ImageButton) v.findViewById(R.id.fpc_imb_takephoto);    // 录制文件
    }

    /**
     * @author 李巷阳
     * @date 2016-11-11 下午3:59:08
     */
    private void init_listener() {
        m_fpc_imb_scancode.setOnClickListener(this);
        m_fpc_imb_takephoto.setOnClickListener(this);
    }

    /**
     * @author 李巷阳
     * @date 2016-11-11 下午4:00:56
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 本地文件
            case fpc_imb_scancode:
                Query_local_file();
                break;
            // 录制文件
            case R.id.fpc_imb_takephoto:
                Record_local_file();
                break;
            default:
                break;
        }
    }


    /**
     * @Description: (本地文件触发)
     * @author 李巷阳
     * @date 2016/11/29 16:04
     */
    private void Query_local_file() {
        // 判断是否需要验证true:不需要;false:需要.
        // 不需要直接跳转MediaActivity
        if (isVerification()) {
            OpenMediaActivity();
        } else {
            Query_local_file_Verification();
        }
    }

    /**
     * @Description: (录制文件触发)
     * @author 李巷阳
     * @date 2016/11/29 16:05
     */
    private void Record_local_file() {
        // 判断是否需要验证true:不需要;false:需要.
        if (isVerification()) {
            showCameraTakerDialog();
        } else {
            Record_local_file_Verification();
        }
    }


    /**
     * @Description: (打开多媒体列表)
     * @author 李巷阳
     * @date 2016/11/29 16:25
     */
    private void OpenMediaActivity() {
        Intent intent = new Intent(getActivity(), MediaActivity.class);
        // Pbb_Fields.TAG_PLAIN_TOTAL 显示本地数量列表.
        intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, Pbb_Fields.TAG_PLAIN_TOTAL);
        // GlobalIntentKeys.BUNDLE_FLAG_CIPHER 密文环境.
        intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
        intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, true);
        startActivity(intent);
    }

    /**
     * @Description: (用户点击本地文件去验证)
     * @author 李巷阳
     * @date 2016/11/29 16:15
     */
    private void Query_local_file_Verification() {
        // 提示用户验证身体，以及绑定
        final Dialog dialog = new Dialog(getActivity(), R.style.no_frame_small);
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_click_limit, null);
        dialog.setContentView(v);
        dialog.show();
        // 用户不想验证,则跳转MediaActivity。
        v.findViewById(R.id.dcl_btn_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                OpenMediaActivity();
            }
        });
        // 用户同意验证，跳转个人中心去绑定验证。
        v.findViewById(R.id.dcl_btn_goto_check).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                startActivity(new Intent(getActivity(), UserInfoActivity.class));
            }
        });

    }

    /**
     * @Description: (用户点击录制文件去验证)
     * @author 李巷阳
     * @date 2016/11/29 16:22
     */
    private void Record_local_file_Verification() {
        // 提示用户验证身体，以及绑定
        final Dialog dialog = new Dialog(getActivity(), R.style.no_frame_small);
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_click_limit, null);
        dialog.setContentView(v);
        dialog.show();
        // 用户不想验证，继续使用。
        v.findViewById(R.id.dcl_btn_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                showCameraTakerDialog();
            }
        });
        // 用户同意验证，跳转个人中心去绑定验证。
        v.findViewById(R.id.dcl_btn_goto_check).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                startActivity(new Intent(getActivity(), UserInfoActivity.class));
            }
        });
    }

    /**
     *
     * @Description: (判断是否需要验证false:需要, true:不需要)
     *
     * @author 李巷阳
     * @date 2016/11/29 16:10
     */
    /**
     * 需求规定：钥匙未作任何绑定时，密文数量超过10个则需要提示用户验证钥匙
     * 获取密文.pyc文件数量大于10，则提示用户及时验证您的身份，否则卸载/重装后，就无法找回文件。
     */
    private boolean isVerification() {
        UserInfo userInfo = UserDao.getDB(getActivity()).getUserInfo();

        if (userInfo.isEmailBinded() || userInfo.isPhoneBinded() || userInfo.isQqBinded() || GlobalData.getTotalCount(getActivity(), true) < 11) {
            return true;
        } else {
            return false;
        }
    }


    /*-
     * 这个dialog本来想放在CameraTakerActivity中的，但有两处限制：
     * 1.仍以dialog的形式存在于CameraTakerActivity中则背景不能透明，看不到主界面了
     * 2.如果以setContentView的形式虽可以设置Activity的theme成窗口模式，但在随后的
     * 预览界面不能取消theme，还是以窗口形式显示，不美观。
     *
     * 如果能解决这个限制就太好了
     */
    private void showCameraTakerDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.net_connect_dialog_main);
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_camera, null);
        dialog.setContentView(v);
        LayoutParams lay = dialog.getWindow().getAttributes();
        lay.width = ScreenUtil.getScreenWidth(getActivity());
        lay.height = ScreenUtil.getScreenHeight(getActivity());
        dialog.show();
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, true);
                switch (v.getId()) {
                    case R.id.da_btn_music:
                        intent.setClass(getActivity(), MusicRecordActivity.class);
                        break;
                    case R.id.da_btn_video:
                    case R.id.da_btn_photo:
                        intent.setClass(getActivity(), CameraTakerActivity.class);
                        intent.putExtra(GlobalIntentKeys.BUNDLE_OBJECT_MEDIA_TYPE, v.getId() == R.id.da_btn_photo ? GlobalData.Image : GlobalData.Video);
                        break;

                    default:
                        break;
                }
                startActivity(intent);
                dialog.cancel();
            }
        };
        v.findViewById(R.id.da_btn_photo).setOnClickListener(listener);
        v.findViewById(R.id.da_btn_video).setOnClickListener(listener);
        v.findViewById(R.id.da_btn_music).setOnClickListener(listener);
    }
    //    private void clickBranch(final int id) {
    //        UserInfo userInfo = UserDao.getDB(getActivity()).getUserInfo();
    //        // 如果不满足条件,则就让用户去验证身份。
    //        if (userInfo.isEmailBinded() || userInfo.isPhoneBinded() || userInfo.isQqBinded() || GlobalData.getTotalCount(getActivity(), true) < 11) {
    //            // 录制或查看本地文件
    //            clickNormal(id);
    //        } else {
    //            // 提示用户验证身体，以及绑定
    //            is_validate_binding(id);
    //        }
    //    }

    //    private void is_validate_binding(final int id) {
    //        // 提示用户验证身体，以及绑定
    //        final Dialog dialog = new Dialog(getActivity(), R.style.no_frame_small);
    //        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_click_limit, null);
    //        dialog.setContentView(v);
    //        dialog.show();
    //        // 用户不想验证，继续使用。
    //        v.findViewById(R.id.dcl_btn_ok).setOnClickListener(new OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                dialog.cancel();
    //                clickNormal(id);
    //            }
    //        });
    //        // 用户同意验证，跳转个人中心去绑定验证。
    //        v.findViewById(R.id.dcl_btn_goto_check).setOnClickListener(new OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                dialog.cancel();
    //                startActivity(new Intent(getActivity(), UserInfoActivity.class));
    //            }
    //        });
    //    }

    // 按按钮的原有意义进行
    //    private void clickNormal(int id) {
    //        // 获取本地文件
    //        if (id == fpc_imb_scancode) {
    //            OpenMediaActivity();
    //        }
    //        // 录制文件
    //        else if (id == R.id.fpc_imb_takephoto) {
    //            showCameraTakerDialog();
    //        }
    //    }

}
