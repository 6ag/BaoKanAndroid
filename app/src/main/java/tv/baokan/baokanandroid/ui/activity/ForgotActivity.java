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

public class ForgotActivity extends BaseActivity implements TextWatcher {

    private NavigationViewRed mNavigationViewRed;     // 导航栏
    private EditText mUsernameEditText;               // 账号
    private EditText mEmailEditText;                  // 邮箱
    private Button mSendButton;                   // 登录

    /**
     * 便捷启动当前activity
     *
     * @param activity 启动当前activity的activity
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, ForgotActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        mNavigationViewRed = (NavigationViewRed) findViewById(R.id.nav_forgot);
        mUsernameEditText = (EditText) findViewById(R.id.et_forgot_username);
        mEmailEditText = (EditText) findViewById(R.id.et_forgot_email);
        mSendButton = (Button) findViewById(R.id.btn_forgot_send);

        // 监听点击事件
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });

        // 配置导航栏
        mNavigationViewRed.setupNavigationView(true, false, "找回密码", new NavigationViewRed.OnClickListener() {
            @Override
            public void onBackClick(View v) {
                finish();
            }
        });

        sendButtonStateChange();

        // 监听文本框改变
        mUsernameEditText.addTextChangedListener(this);
        mEmailEditText.addTextChangedListener(this);

    }

    /**
     * 改变发送按钮的状态
     */
    private void sendButtonStateChange() {
        if (!TextUtils.isEmpty(mUsernameEditText.getText().toString())
                && !TextUtils.isEmpty(mEmailEditText.getText().toString())) {
            mSendButton.setEnabled(true);
        } else {
            mSendButton.setEnabled(false);
        }
    }

    /**
     * 发送邮件
     */
    private void send() {

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
        sendButtonStateChange();
    }
}
