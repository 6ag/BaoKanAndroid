package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class ForgotActivity extends BaseActivity implements TextWatcher {

    private static final String TAG = "ForgotActivity";

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
        activity.startActivityForResult(intent, LoginActivity.REQUEST_CODE_FORGOT);
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

        // 自动弹出键盘
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mUsernameEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mUsernameEditText, 0);
            }

        }, 500);
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
        String username = mUsernameEditText.getText().toString();
        String email = mEmailEditText.getText().toString();

        if (TextUtils.isEmpty(username)) {
            ProgressHUD.showInfo(this, "账号不能为空");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            ProgressHUD.showInfo(this, "邮箱不能为空");
            return;
        }

        final KProgressHUD hud = ProgressHUD.show(this, "发送中...");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("action", "SendPassword");
        parameters.put("email", email);

        NetworkUtils.shared.post(APIs.MODIFY_ACCOUNT_INFO, parameters, new NetworkUtils.StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ProgressHUD.showInfo(ForgotActivity.this, "您的网络不给力哦");
            }

            @Override
            public void onResponse(String response, int id) {

                LogUtils.d(TAG, response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String info = jsonObject.getJSONObject("data").getString("info");
                    if (info.equals("邮件已发送，请登录邮箱认证并取回密码")) {

                        // 延迟1秒退出activity
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hud.dismiss();
                                ProgressHUD.showInfo(ForgotActivity.this, "邮件已发送，请登录邮箱认证并取回密码");

                                setResult(RESULT_OK);
                                finish();
                            }
                        }, 1000);
                    } else {
                        hud.dismiss();
                        Toast.makeText(ForgotActivity.this, info, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hud.dismiss();
                    ProgressHUD.showInfo(ForgotActivity.this, "数据解析异常");
                }
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
        sendButtonStateChange();
    }
}
