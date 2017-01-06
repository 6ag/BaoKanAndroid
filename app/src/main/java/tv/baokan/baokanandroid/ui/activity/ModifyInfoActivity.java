package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.UserBean;
import tv.baokan.baokanandroid.widget.ClearEditText;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class ModifyInfoActivity extends BaseActivity {

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

    }

}
