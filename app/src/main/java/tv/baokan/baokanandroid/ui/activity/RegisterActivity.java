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
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class RegisterActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    private static final String TAG = "RegisterActivity";

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
        // 注册成功后回调账号密码给来源activity
        activity.startActivityForResult(intent, LoginActivity.REQUEST_CODE_REGISTER);
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
        final String username = mUsernameEditText.getText().toString();
        final String password = mPasswordEditText.getText().toString();
        String email = mEmailEditText.getText().toString();

        if (TextUtils.isEmpty(username)) {
            ProgressHUD.showInfo(this, "账号不能为空");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ProgressHUD.showInfo(this, "密码不能为空");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            ProgressHUD.showInfo(this, "邮箱不能为空");
            return;
        }

        final KProgressHUD hud = ProgressHUD.show(this, "注册中...");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("password", password);
        parameters.put("email", email);

        NetworkUtils.shared.post(APIs.REGISTER, parameters, new NetworkUtils.StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                hud.dismiss();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("err_msg").equals("success")) {

                        // 延迟1秒退出activity
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hud.dismiss();
                                ProgressHUD.showInfo(RegisterActivity.this, "注册成功");

                                // 回调注册的账号密码给登录activity - 需要在finish()前设置
                                Intent intent = new Intent();
                                intent.putExtra("username", username);
                                intent.putExtra("password", password);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }, 1000);
                    } else {
                        hud.dismiss();
                        String info = jsonObject.getString("info");
                        Toast.makeText(RegisterActivity.this, info, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hud.dismiss();
                    ProgressHUD.showInfo(RegisterActivity.this, "数据解析异常");
                }
            }
        });
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
