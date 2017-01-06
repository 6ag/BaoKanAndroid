package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

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

public class ModifySafeInfoActivity extends BaseActivity implements TextWatcher {

    private static final String TAG = "ModifySafeInfoActivity";

    private NavigationViewRed mNavigationViewRed;
    private EditText mOldPasswordEditText;           // 老密码
    private EditText mNewPasswordEditText;           // 新密码
    private EditText mConfirmPasswordEditText;       // 确认新密码
    private EditText mEmailEditText;                 // 邮箱
    private Button mModifyButton;                    // 修改按钮

    /**
     * 便捷启动当前activity
     *
     * @param activity 启动当前activity的activity
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, ModifySafeInfoActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_safe_info);

        prepareUI();
        prepareData();

        // 更新按钮状态
        modifyButtonStateChange();

    }

    /**
     * 准备UI
     */
    private void prepareUI() {
        mNavigationViewRed = (NavigationViewRed) findViewById(R.id.nav_safe_modify_user_info);
        mOldPasswordEditText = (EditText) findViewById(R.id.et_safe_modify_user_info_password);
        mNewPasswordEditText = (EditText) findViewById(R.id.et_safe_modify_user_info_new_password);
        mConfirmPasswordEditText = (EditText) findViewById(R.id.et_safe_modify_user_info_confirm_password);
        mEmailEditText = (EditText) findViewById(R.id.et_safe_modify_user_info_email);
        mModifyButton = (Button) findViewById(R.id.btn_safe_modify_user_info_modify);

        mNavigationViewRed.setupNavigationView(true, false, "修改安全信息", new NavigationViewRed.OnClickListener() {
            @Override
            public void onBackClick(View v) {
                finish();
            }
        });

        mModifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveModifyInfo();
            }
        });

        mOldPasswordEditText.addTextChangedListener(this);
        mNewPasswordEditText.addTextChangedListener(this);
        mConfirmPasswordEditText.addTextChangedListener(this);
        mEmailEditText.addTextChangedListener(this);

        // 自动弹出键盘
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mOldPasswordEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mOldPasswordEditText, 0);
            }

        }, 500);

    }

    /**
     * 准备数据
     */
    private void prepareData() {
        mEmailEditText.setText(UserBean.shared().getEmail());
    }

    /**
     * 保存修改信息
     */
    private void saveModifyInfo() {

        final KProgressHUD hud = ProgressHUD.show(mContext, "正在处理");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("username", UserBean.shared().getUsername());
        parameters.put("userid", UserBean.shared().getUserid());
        parameters.put("action", "EditSafeInfo");
        parameters.put("token", UserBean.shared().getToken());
        parameters.put("oldpassword", mOldPasswordEditText.getText().toString());
        parameters.put("password", mNewPasswordEditText.getText().toString());
        parameters.put("repassword", mConfirmPasswordEditText.getText().toString());
        parameters.put("email", mEmailEditText.getText().toString());

        NetworkUtils.shared.post(APIs.MODIFY_ACCOUNT_INFO, parameters, new NetworkUtils.StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                hud.dismiss();
                ProgressHUD.showInfo(mContext, "您的网络不给力哦");
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtils.d(TAG, response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("err_msg").equals("success")) {

                        // 修改资料成功后需要更新本地用户信息
                        UserBean.updateUserInfoFromNetwork(new UserBean.OnUpdatedUserInfoListener() {
                            @Override
                            public void onSuccess(UserBean userBean) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        hud.dismiss();
                                        ProgressHUD.showInfo(mContext, "修改安全信息成功");
                                        finish();
                                    }
                                }, 1000);
                            }

                            @Override
                            public void onError(String tipString) {
                                hud.dismiss();
                                ProgressHUD.showInfo(mContext, tipString);
                            }
                        });
                    } else {
                        hud.dismiss();
                        ProgressHUD.showInfo(mContext, jsonObject.getString("info"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hud.dismiss();
                    ProgressHUD.showInfo(mContext, "数据解析异常");
                }
            }
        });

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        modifyButtonStateChange();
    }

    /**
     * 改变修改按钮的状态
     */
    private void modifyButtonStateChange() {
        if (!TextUtils.isEmpty(mOldPasswordEditText.getText().toString())
                && !TextUtils.isEmpty(mNewPasswordEditText.getText().toString())
                && !TextUtils.isEmpty(mConfirmPasswordEditText.getText().toString())
                && !TextUtils.isEmpty(mEmailEditText.getText().toString())) {
            mModifyButton.setEnabled(true);
        } else {
            mModifyButton.setEnabled(false);
        }
    }
}
