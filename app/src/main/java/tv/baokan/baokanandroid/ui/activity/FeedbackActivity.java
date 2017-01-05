package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class FeedbackActivity extends BaseActivity implements TextWatcher {

    private NavigationViewRed mNavigationViewRed;     // 导航栏
    private EditText mContentEditText;                // 账号
    private EditText mContactsEditText;               // 密码
    private Button mSubmitButton;                     // 登录

    /**
     * 便捷启动当前activity
     *
     * @param activity 启动当前activity的activity
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, FeedbackActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mNavigationViewRed = (NavigationViewRed) findViewById(R.id.nav_feedback);
        mContentEditText = (EditText) findViewById(R.id.et_feedback_content_edittext);
        mContactsEditText = (EditText) findViewById(R.id.et_feedback_contacts_edittext);
        mSubmitButton = (Button) findViewById(R.id.btn_feedback_submit);

        // 监听文本改变
        mContentEditText.addTextChangedListener(this);
        mContactsEditText.addTextChangedListener(this);

        // 初始化状态
        submitButtonStateChange();

        // 配置导航栏
        mNavigationViewRed.setupNavigationView(true, false, "意见反馈", new NavigationViewRed.OnClickListener() {
            @Override
            public void onBackClick(View v) {
                finish();
            }
        });

        // 提交
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });

    }

    /**
     * 提交意见反馈
     */
    private void submitFeedback() {

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
        submitButtonStateChange();
    }

    /**
     * 改变登录按钮的状态
     */
    private void submitButtonStateChange() {
        // 账号和密码都不为空才能点击
        if (!TextUtils.isEmpty(mContentEditText.getText().toString())
                && !TextUtils.isEmpty(mContactsEditText.getText().toString())) {
            mSubmitButton.setEnabled(true);
        } else {
            mSubmitButton.setEnabled(false);
        }
    }

}
