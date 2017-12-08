package cn.com.pyc.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.web.WebActivity;
import cn.com.pyc.widget.HighlightImageView;

public class AboutActivity extends PbbBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_about);
//        if (isReaderProject()) {
//            UIHelper.showTintStatusBar(this, getResources().getColor(R.color.title_top_color));
//        } else {
//            HighlightImageView leftImage = ((HighlightImageView) findViewById(R.id.back_img));
//            showPBBStyleBackIcon(leftImage);
//        }
        ViewHelp.showAppTintStatusBar(this);
        HighlightImageView leftImage = ((HighlightImageView) findViewById(R.id.back_img));
        showPBBStyleBackIcon(leftImage);

        ((TextView) findViewById(R.id.title_tv)).setText("关 于");

        findViewById(R.id.back_img).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        findViewById(R.id.asa_imb_phone1).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_DIAL);
//                intent.setData(Uri.parse("tel:13811965182"));
//                startActivity(Intent.createChooser(intent, null));
//            }
//        });
//        findViewById(R.id.asa_imb_phone2).setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_DIAL);
//                intent.setData(Uri.parse("tel:13811977882"));
//                startActivity(Intent.createChooser(intent, null));
//            }
//        });
        findViewById(R.id.asa_txt_privacy).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(AboutActivity.this, WebActivity.class).putExtra(
                        GlobalIntentKeys.BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.Privacy));
            }
        });

//        ((TextView) findViewById(R.id.asa_txt_version)).setText(getString(R.string.app_version));
        ((TextView) findViewById(R.id.asa_txt_version)).setText(getString(R.string
                .version_lable));
    }

}
