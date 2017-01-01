package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class RegisterActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    private NavigationViewRed mNavigationViewRed;     // 导航栏
    private EditText mUsernameEditText;               // 账号
    private EditText mPasswordEditText;               // 密码
    private EditText mEmailEditText;                  // 邮箱
    private ImageView mShowPassword;                  // 显示密码
    private Button mRegisterButton;                   // 登录

    /**
     * 便捷启动当前activity
     *
     * @param activity 启动当前activity的activity
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, RegisterActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mNavigationViewRed = (NavigationViewRed) findViewById(R.id.nav_register);
        mUsernameEditText = (EditText) findViewById(R.id.et_register_username);
        mPasswordEditText = (EditText) findViewById(R.id.et_register_password);
        mEmailEditText = (EditText) findViewById(R.id.et_register_email);
        mShowPassword = (ImageView) findViewById(R.id.iv_register_showpassword);
        mRegisterButton = (Button) findViewById(R.id.btn_register_register);

        // 监听点击事件
        mShowPassword.setOnClickListener(this);
        mRegisterButton.setOnClickListener(this);

        // 配置导航栏
        mNavigationViewRed.setupNavigationView(true, false, "注册", new NavigationViewRed.OnClickListener() {
            @Override
            public void onBackClick(View v) {
                finish();
            }
        });

        registerButtonStateChange();

        // 监听文本框改变
        mUsernameEditText.addTextChangedListener(this);
        mPasswordEditText.addTextChangedListener(this);
        mEmailEditText.addTextChangedListener(this);

    }

    /**
     * 改变登录按钮的状态
     */
    private void registerButtonStateChange() {
        if (!TextUtils.isEmpty(mUsernameEditText.getText().toString())
                && !TextUtils.isEmpty(mPasswordEditText.getText().toString())
                && !TextUtils.isEmpty(mEmailEditText.getText().toString())) {
            mRegisterButton.setEnabled(true);
        } else {
            mRegisterButton.setEnabled(false);
        }
    }

    // 监听点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_register_showpassword:
                showOrHidePassword();
                break;
            case R.id.btn_register_register:
                register();
                break;
        }
    }

    /**
     * 注册
     */
    private void register() {

    }

    /**
     * 明文密文切换
     */
    private void showOrHidePassword() {
        if (mShowPassword.getTag() == null) {
            mShowPassword.setTag("show");
            mShowPassword.setImageResource(R.drawable.login_icon_viewcode_selected);
            mPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            mShowPassword.setTag(null);
            mShowPassword.setImageResource(R.drawable.login_icon_viewcode_normal);
            mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    // 下面3个方法是监听文本框改变
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        registerButtonStateChange();
    }
}
