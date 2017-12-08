package cn.com.pyc.base;

import android.widget.ImageView;

import com.qlk.util.base.BaseActivity;
import com.sz.mobilesdk.util.CommonUtil;

import cn.com.pyc.pbb.reader.R;
import cn.jpush.android.api.JPushInterface;

public class PbbBaseActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (getParent() == null){
            JPushInterface.onResume(this);
    }}

    @Override
    protected void onPause() {
        super.onPause();
        if (getParent() == null){
            JPushInterface.onPause(this);
    }}

    /**
     * The environment is PbbReader if return true, otherwise is PengBaoBao
     */
    protected boolean isReaderProject() {
        return getResources().getString(R.string.app_name).equals("PBB Reader");
    }

    /**
     * PengBaoBao运行时，保持左边返回按钮风格统一
     */
    protected void showPBBStyleBackIcon(ImageView leftImage) {
//        HighlightImageView leftImage = ((HighlightImageView) findViewById(R.id.back_img));
        leftImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_back));
        int padding = CommonUtil.dip2px(this, 13f);
        leftImage.setPadding(padding, 0, padding, 0);
    }
}
