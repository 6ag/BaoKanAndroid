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
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.UserBean;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class LoginActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    private static final String TAG = "LoginActivity";

    // 注册的启动请求码
    public static final int REQUEST_CODE_REGISTER = 0;
    public static final int REQUEST_CODE_FORGOT = 1;

    private NavigationViewRed mNavigationViewRed;    // 导航栏
    private EditText mUsernameEditText;               // 账号
    private EditText mPasswordEditText;               // 密码
    private ImageView mShowPassword;                  // 显示密码
    private Button mLoginButton;                      // 登录
    private View mSinaView;                           // 新浪
    private View mQqView;                             // QQ
    private View mRegisterView;                       // 注册
    private View mForgotPasswordView;                 // 忘记密码
    private TextView mAgreementTextView;              // 注册条款

    /**
     * 便捷启动当前activity
     *
     * @param activity 启动当前activity的activity
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mNavigationViewRed = (NavigationViewRed) findViewById(R.id.nav_login);
        mUsernameEditText = (EditText) findViewById(R.id.et_login_username);
        mPasswordEditText = (EditText) findViewById(R.id.et_login_password);
        mShowPassword = (ImageView) findViewById(R.id.iv_login_showpassword);
        mLoginButton = (Button) findViewById(R.id.btn_login_login);
        mSinaView = findViewById(R.id.ll_login_sina);
        mQqView = findViewById(R.id.ll_login_qq);
        mRegisterView = findViewById(R.id.rl_login_register);
        mForgotPasswordView = findViewById(R.id.rl_login_forgotpassword);
        mAgreementTextView = (TextView) findViewById(R.id.tv_login_agreement);

        // 配置导航栏
        mNavigationViewRed.setupNavigationView(true, false, "登录", new NavigationViewRed.OnClickListener() {
            @Override
            public void onBackClick(View v) {
                finish();
            }
        });

        // 监听点击事件
        mShowPassword.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
        mSinaView.setOnClickListener(this);
        mQqView.setOnClickListener(this);
        mRegisterView.setOnClickListener(this);
        mForgotPasswordView.setOnClickListener(this);
        mAgreementTextView.setOnClickListener(this);

        // 改变登录按钮的状态
        loginButtonStateChange();

        // 文本框改变监听
        mUsernameEditText.addTextChangedListener(this);
        mPasswordEditText.addTextChangedListener(this);

        // 自动弹出键盘
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mUsernameEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mUsernameEditText, 0);
            }

        }, 500);
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
        loginButtonStateChange();
    }

    /**
     * 改变登录按钮的状态
     */
    private void loginButtonStateChange() {
        // 账号和密码都不为空才能点击
        if (!TextUtils.isEmpty(mUsernameEditText.getText().toString())
                && !TextUtils.isEmpty(mPasswordEditText.getText().toString())) {
            mLoginButton.setEnabled(true);
        } else {
            mLoginButton.setEnabled(false);
        }
    }

    /**
     * 处理各种点击事件
     *
     * @param v 被点击的view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_login_showpassword:
                showOrHidePassword();
                break;
            case R.id.btn_login_login:
                login();
                break;
            case R.id.ll_login_sina:
                sinaLogin();
                break;
            case R.id.ll_login_qq:
                qqLogin();
                break;
            case R.id.rl_login_register:
                register();
                break;
            case R.id.rl_login_forgotpassword:
                forgotPassword();
                break;
            case R.id.tv_login_agreement:
                readUserAgreement();
                break;
        }
    }

    /**
     * 阅读用户条款
     */
    private void readUserAgreement() {
        AgreementActivity.start(this);
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

    /**
     * 登录
     */
    private void login() {
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        if (TextUtils.isEmpty(username)) {
            ProgressHUD.showInfo(LoginActivity.this, "账号不能为空");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ProgressHUD.showInfo(LoginActivity.this, "密码不能为空");
            return;
        }

        // 登录进度条
        final KProgressHUD hud = ProgressHUD.show(this, "登录中...");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("password", password);

        NetworkUtils.shared.post(APIs.LOGIN, parameters, new NetworkUtils.StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                hud.dismiss();
                ProgressHUD.showInfo(LoginActivity.this, "您的网络不给力哦");
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtils.d(TAG, response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("err_msg").equals("success")) {

                        // 编码登录信息
                        UserBean userBean = new UserBean(jsonObject.getJSONObject("data"));
                        userBean.updateUserInfoFromLocal();

                        // 延迟1秒退出activity
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hud.dismiss();
                                ProgressHUD.showInfo(LoginActivity.this, "登录成功");

                                finish();
                            }
                        }, 1000);

                    } else {
                        hud.dismiss();
                        String info = jsonObject.getJSONObject("data").getString("info");
                        Toast.makeText(LoginActivity.this, info, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hud.dismiss();
                    ProgressHUD.showInfo(LoginActivity.this, "数据解析异常");
                }
            }
        });

    }

    /**
     * 新浪登录
     */
    private void sinaLogin() {

    }

    /**
     * qq登录
     */
    private void qqLogin() {

    }

    /**
     * 注册
     */
    private void register() {
        RegisterActivity.start(this);
    }

    /**
     * 找回密码
     */
    private void forgotPassword() {
        ForgotActivity.start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_REGISTER:
                if (resultCode == RESULT_OK) {
                    String username = data.getStringExtra("username");
                    String password = data.getStringExtra("password");
                    mUsernameEditText.setText(username);
                    mPasswordEditText.setText(password);
                    // 登录
                    login();
                }
                break;
            case REQUEST_CODE_FORGOT:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
        }
    }
}
