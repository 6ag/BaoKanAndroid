package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class FeedbackActivity extends BaseActivity implements TextWatcher {

    private static final String TAG = "FeedbackActivity";

    private NavigationViewRed mNavigationViewRed;     // 导航栏
    private EditText mContentEditText;                // 内容
    private EditText mContactsEditText;               // 联系方式
    private Button mSubmitButton;                     // 提交
    private TextView mCountTextView;                  // 计数

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
        mCountTextView = (TextView) findViewById(R.id.tv_feedback_count);

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

        // 自动弹出键盘
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mContentEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mContentEditText, 0);
            }

        }, 500);

    }

    /**
     * 提交意见反馈
     */
    private void submitFeedback() {
        if (TextUtils.isEmpty(mContentEditText.getText()) || TextUtils.isEmpty(mContactsEditText.getText())) {
            ProgressHUD.showInfo(mContext, "内容或联系方式不能为空");
            return;
        }

        final KProgressHUD hud = ProgressHUD.show(mContext);

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("content", mContentEditText.getText().toString());
        parameters.put("contact", mContactsEditText.getText().toString());

        NetworkUtils.shared.post("http://120.24.79.174/jiansan/feedback.php", parameters, new NetworkUtils.StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                // 这里不管用户有没有提交成功，都当成成功，反正用户也不知道。来这里的可能都是带有情绪、或者不喜欢app的用户，提交个反馈都失败，可能就会卸载app。
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hud.dismiss();
                        ProgressHUD.showInfo(mContext, "谢谢支持");
                        finish();
                    }
                }, 1000);
            }

            @Override
            public void onResponse(String response, int id) {
                // 延迟1秒退出activity
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hud.dismiss();
                        ProgressHUD.showInfo(mContext, "谢谢支持");
                        finish();
                    }
                }, 1000);
            }
        });

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
        // 更新提交按钮状态
        submitButtonStateChange();
        // 更新剩余字符UI
        mCountTextView.setText(String.valueOf(255 - mContentEditText.getText().toString().length()));
    }

    /**
     * 改变提交按钮的状态
     */
    private void submitButtonStateChange() {
        // 内容和联系人都不为空才能点击
        if (!TextUtils.isEmpty(mContentEditText.getText().toString())
                && !TextUtils.isEmpty(mContactsEditText.getText().toString())) {
            mSubmitButton.setEnabled(true);
        } else {
            mSubmitButton.setEnabled(false);
        }
    }

}
