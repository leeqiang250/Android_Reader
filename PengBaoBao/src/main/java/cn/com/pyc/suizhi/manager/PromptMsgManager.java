package cn.com.pyc.suizhi.manager;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.UIHelper;

import java.util.HashMap;
import java.util.Map;

import cn.com.pyc.suizhi.common.Code;

/**
 * 消息弹框提示管理类
 * <p>
 * Created by hudq on 2017/4/5.
 */

public class PromptMsgManager {

    private static Map<String, String> propMaps = new HashMap<>();

    static {
        propMaps.put("13001", "对不起，程序出错了！请把错误码（13001）告知客服尝试解决。");
        //propMaps.put("14001", "对不起，程序出错了！请把错误码（14001）告知客服尝试解决。");
        propMaps.put("14001", "对不起，验证失败，请尝试注销后重新登录账户。");
        //propMaps.put("14002", "对不起，程序出错了！请把错误码（14002）告知客服尝试解决。");
        propMaps.put("14002", "对不起，校验失败！请把错误码（14002）告知客服尝试解决。");

        propMaps.put("15003", "对不起，程序出错了！请把错误码（15003）告知客服尝试解决。");
        propMaps.put("15004", "对不起，程序出错了！请把错误码（15004）告知客服尝试解决。");
        propMaps.put("15006", "对不起，程序出错了！请把错误码（15006）告知客服尝试解决。");
        propMaps.put("15007", "对不起，程序出错了！请把错误码（15007）告知客服尝试解决。");
        propMaps.put("15008", "对不起，程序出错了！请把错误码（15008）告知客服尝试解决。");

        propMaps.put("16001", "您使用的设备数量已超出卖家限制,请登录官网删除旧设备。");
        propMaps.put("16002", "当前版本存在安全隐患，为保障您的权益，请立即升级！");
        //propMaps.put("16003", "您无权打开本文件，请购买后尝试！");
        propMaps.put("16003", "检测到您有分享尚未购买，是否立即购买？");
        propMaps.put("16004", "该文件暂时无法打开，请在卖家允许的时间段内使用。");
        propMaps.put("16005", "对不起，该文件已过期!");
        propMaps.put("16009", "对不起，程序出错了！请把错误码（16009）告知客服尝试解决。");
        propMaps.put("16011", "对不起，程序出错了！请把错误码（16011）告知客服尝试解决。");
        propMaps.put("16012", "该文件已失效，请重新下载！");
        propMaps.put("16014", "卖家禁止此文件在此端使用,请您联系卖家！");
        propMaps.put("19001", "对不起，程序出错了！请把错误码（19001）告知客服尝试解决。");
        propMaps.put("19002", "对不起，程序出错了！请把错误码（19002）告知客服尝试解决。");
    }

    public static void showToast(Context context, String code) {
        Toast.makeText(context, propMaps.get(code), Toast.LENGTH_LONG).show();
    }

    private String posBtnText;
    private String negBtnText;
    private UIHelper.BaseDialogCallBack mCallBack;

    private PromptMsgManager(String posBtnText, String negBtnText, UIHelper.BaseDialogCallBack
            mCallBack) {
        this.posBtnText = posBtnText;
        this.negBtnText = negBtnText;
        if (mCallBack instanceof UIHelper.DialogCallBack) {
            this.mCallBack = ((UIHelper.DialogCallBack) mCallBack);
        } else if (mCallBack instanceof UIHelper.DialogCallBackPat) {
            this.mCallBack = ((UIHelper.DialogCallBackPat) mCallBack);
        } else {
            throw new IllegalArgumentException("illegal args 'mCallback'。 ");
        }
    }


    /**
     * 显示对话框
     *
     * @param ctx
     * @param code
     */
    public void show(Context ctx, String code) {
        SZLog.i("Code: " + code);
        switch (code) {
            case Code._13001:
            case Code._14001:
            case Code._14002:
            case Code._15003:
            case Code._15004:
            case Code._15006:
            case Code._15007:
            case Code._15008:
            case Code._16001:
            case Code._16004:
            case Code._16005:
            case Code._16009:
            case Code._16011:
            case Code._19001:
            case Code._19002:
                showSingleButton(ctx, propMaps.get(code), posBtnText, (UIHelper.DialogCallBack)
                        mCallBack);
                break;

            case Code._16002:
            case Code._16003:
            case Code._16012:
                showDoubleButton(ctx, propMaps.get(code), posBtnText, negBtnText, (UIHelper
                        .DialogCallBackPat) mCallBack);
                break;

            default:
                Log.e("", "illegal args，Undefined 'code'。");
                break;
        }
    }


    private Dialog showSingleButton(Context context,
                                    String contentText,
                                    String posBtnText,
                                    final UIHelper.DialogCallBack callBack) {
        return UIHelper.showSingleCommonDialog(context, "", contentText, posBtnText,
                callBack);
    }

    private Dialog showDoubleButton(Context context,
                                    String contentText,
                                    String posBtnText,
                                    String negBtnText,
                                    final UIHelper.DialogCallBackPat callBack) {
        return UIHelper.showCommonDialog2(context, "", contentText, posBtnText, negBtnText,
                callBack);
    }


    public static class Builder {
        private String posBtnText;
        private String negBtnText;
        private UIHelper.BaseDialogCallBack mCallBack;

        public Builder setPositiveText(String posBtnText) {
            this.posBtnText = posBtnText;
            return this;
        }

        public Builder setNegativeText(String negBtnText) {
            this.negBtnText = negBtnText;
            return this;
        }

        public Builder setDialogCallback(UIHelper.BaseDialogCallBack mCallBack) {
            this.mCallBack = mCallBack;
            return this;
        }

        public PromptMsgManager create() {
            return new PromptMsgManager(posBtnText, negBtnText, mCallBack);
        }
    }
}
