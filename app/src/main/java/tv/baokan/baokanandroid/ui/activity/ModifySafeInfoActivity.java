package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.UserBean;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class ModifySafeInfoActivity extends BaseActivity {

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

    }
}
