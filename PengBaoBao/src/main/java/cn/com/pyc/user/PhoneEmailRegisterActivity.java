package cn.com.pyc.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.pbb.R;
import cn.com.pyc.user.key.KeyActivity;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_CLASS_TEXT;

/**
 * Created by songyumei on 2017/8/8.
 */
@Deprecated
public class PhoneEmailRegisterActivity extends ExtraBaseActivity implements View.OnClickListener {

    private EditText phoneEmail;
    private EditText verificationCode;
    private EditText passwrod;
    private Button getVerificationCode;
    private Button btnRegister;
    private TextView phone_email_Register;
    private TextView login;
    private ImageView qqLogin;
    private boolean isPhoneRegister = true;//默认手机注册

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_email_register);
        initView();
        initValue();
    }

    private void initValue() {
    }

    private void initView() {
        phoneEmail = (EditText) findViewById(R.id.per_edt_phone_email);
        verificationCode = (EditText) findViewById(R.id.per_edt_verification_code);
        passwrod = (EditText) findViewById(R.id.per_edt_password);
        getVerificationCode = (Button) findViewById(R.id.per_btn_verification_code);
        btnRegister = (Button) findViewById(R.id.per_btn_register);
        phone_email_Register = (TextView) findViewById(R.id.per_phoneoremail_register);
        login = (TextView) findViewById(R.id.per_login);
        qqLogin = (ImageView) findViewById(R.id.qq_login);

        getVerificationCode.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        phone_email_Register.setOnClickListener(this);
        login.setOnClickListener(this);
        qqLogin.setOnClickListener(this);

        phoneEmail.setInputType(isPhoneRegister?TYPE_CLASS_NUMBER : TYPE_CLASS_TEXT);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.per_btn_verification_code:
                //获取验证码
                break;
            case R.id.per_btn_register:
                //注册
                break;
            case R.id.per_phoneoremail_register:
                //手机注册或邮箱注册
                isPhoneRegister = !isPhoneRegister;
                phoneEmail.setHint(isPhoneRegister?"手机号":"邮箱");
                phoneEmail.setInputType(isPhoneRegister?TYPE_CLASS_NUMBER : TYPE_CLASS_TEXT);
                phone_email_Register.setText(isPhoneRegister?"使用邮箱注册":"使用手机注册");
                break;
            case R.id.per_login:
                //立即登录
                Intent intent = new Intent(this, KeyActivity.class);
                intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_LOGIN);
                startActivity(intent);
                break;
            case R.id.qq_login:
                //QQ登录
                break;
        }
    }
}
