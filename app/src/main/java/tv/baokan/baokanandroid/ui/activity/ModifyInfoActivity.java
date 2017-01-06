package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
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
import tv.baokan.baokanandroid.widget.ClearEditText;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class ModifyInfoActivity extends BaseActivity {

    private static final String TAG = "ModifyInfoActivity";

    private NavigationViewRed mNavigationViewRed;
    private View mPortraitLayout;                    // 头像区域
    private SimpleDraweeView mPortraitImageView;     // 头像
    private TextView mUsernameTextView;              // 用户名
    private EditText mNicknameEditText;              // 昵称
    private EditText mPhoneEditText;                 // 联系电话
    private EditText mQQEditText;                    // QQ
    private EditText mSayEditText;                   // 个人签名
    private Button mModifyButton;                    // 修改按钮

    /**
     * 便捷启动当前activity
     *
     * @param activity 启动当前activity的activity
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, ModifyInfoActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_info);

        prepareUI();
        prepareData();

    }

    /**
     * 准备UI
     */
    private void prepareUI() {
        mNavigationViewRed = (NavigationViewRed) findViewById(R.id.nav_modify_user_info);
        mPortraitLayout = findViewById(R.id.rl_modify_user_info_portrait_layout);
        mPortraitImageView = (SimpleDraweeView) findViewById(R.id.sdv_modify_user_info_portrait);
        mUsernameTextView = (TextView) findViewById(R.id.tv_modify_user_info_username);
        mNicknameEditText = (EditText) findViewById(R.id.et_modify_user_info_nickname);
        mPhoneEditText = (EditText) findViewById(R.id.et_modify_user_info_phone);
        mQQEditText = (EditText) findViewById(R.id.et_modify_user_info_qq);
        mSayEditText = (EditText) findViewById(R.id.et_modify_user_info_say);
        mModifyButton = (Button) findViewById(R.id.btn_modify_user_info_modify);

        mNavigationViewRed.setupNavigationView(true, false, "修改资料", new NavigationViewRed.OnClickListener() {
            @Override
            public void onBackClick(View v) {
                finish();
            }
        });

        // 头像点击
        mPortraitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyUserAvatar();
            }
        });

        // 修改按钮点击事件
        mModifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveModifyInfo();
            }
        });

        // 自动弹出键盘
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mNicknameEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mNicknameEditText, 0);
            }

        }, 500);

    }

    /**
     * 准备数据
     */
    private void prepareData() {
        mPortraitImageView.setImageURI(UserBean.shared().getAvatarUrl());
        mUsernameTextView.setText(UserBean.shared().getUsername());
        mNicknameEditText.setText(UserBean.shared().getNickname());
        mPhoneEditText.setText(UserBean.shared().getPhone());
        mQQEditText.setText(UserBean.shared().getQq());
        mSayEditText.setText(UserBean.shared().getSaytext());
    }

    /**
     * 修改用户头像
     */
    private void modifyUserAvatar() {

    }

    /**
     * 保存修改信息
     */
    private void saveModifyInfo() {

        final KProgressHUD hud = ProgressHUD.show(mContext, "正在处理");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("username", mUsernameTextView.getText().toString());
        parameters.put("userid", UserBean.shared().getUserid());
        parameters.put("token", UserBean.shared().getToken());
        parameters.put("action", "EditInfo");
        parameters.put("nickname", mNicknameEditText.getText().toString());
        parameters.put("qq", mQQEditText.getText().toString());
        parameters.put("phone", mPhoneEditText.getText().toString());
        parameters.put("saytext", mSayEditText.getText().toString());

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
                                        ProgressHUD.showInfo(mContext, "修改资料成功");
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

}
