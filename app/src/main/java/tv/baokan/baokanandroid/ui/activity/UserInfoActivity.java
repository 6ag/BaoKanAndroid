package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.UserBean;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class UserInfoActivity extends BaseActivity implements View.OnClickListener {

    private NavigationViewRed mNavigationViewRed;
    private SimpleDraweeView mPortraitImageView;
    private TextView mUsernameTextView;
    private TextView mGroupNameTextView;
    private TextView mPointsTextView;
    private View mModifyInfoLayout;
    private View mModifySafeInfoLayout;
    private TextView mRegisterTimeTextView;
    private TextView mMyPointsTextView;
    private TextView mMyGroupNameTextView;
    private Button mLogoutButton;
    private AlertDialog mLogoutDialog;

    /**
     * 便捷启动当前activity
     *
     * @param activity 启动当前activity的activity
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, UserInfoActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        prepareUI();
        prepareData();

    }

    /**
     * 准备UI
     */
    private void prepareUI() {
        mNavigationViewRed = (NavigationViewRed) findViewById(R.id.nav_user_info);
        mPortraitImageView = (SimpleDraweeView) findViewById(R.id.sdv_user_info_portrait);
        mUsernameTextView = (TextView) findViewById(R.id.tv_user_info_username);
        mGroupNameTextView = (TextView) findViewById(R.id.tv_user_info_groupname);
        mPointsTextView = (TextView) findViewById(R.id.tv_user_info_points);
        mModifyInfoLayout = findViewById(R.id.rl_user_info_modify_info_layout);
        mModifySafeInfoLayout = findViewById(R.id.rl_user_info_modify_safe_info_layout);
        mRegisterTimeTextView = (TextView) findViewById(R.id.tv_user_info_registertime);
        mMyPointsTextView = (TextView) findViewById(R.id.tv_user_info_mypoints);
        mMyGroupNameTextView = (TextView) findViewById(R.id.tv_user_info_mygroupname);
        mLogoutButton = (Button) findViewById(R.id.btn_user_info_logout);

        mNavigationViewRed.setupNavigationView(true, false, "我的资料", new NavigationViewRed.OnClickListener() {
            @Override
            public void onBackClick(View v) {
                finish();
            }
        });

        mModifyInfoLayout.setOnClickListener(this);
        mModifySafeInfoLayout.setOnClickListener(this);
        mLogoutButton.setOnClickListener(this);
    }

    /**
     * 准备页面数据
     */
    private void prepareData() {
        mPortraitImageView.setImageURI(UserBean.shared().getAvatarUrl());
        mUsernameTextView.setText(UserBean.shared().getUsername());
        mGroupNameTextView.setText("等级：" + UserBean.shared().getGroupName());
        mPointsTextView.setText(UserBean.shared().getPoints() + "积分");
        mRegisterTimeTextView.setText(UserBean.shared().getRegisterTime());
        mMyPointsTextView.setText(UserBean.shared().getPoints());
        mMyGroupNameTextView.setText(UserBean.shared().getGroupName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_user_info_modify_info_layout:
                ModifyInfoActivity.start(mContext);
                break;
            case R.id.rl_user_info_modify_safe_info_layout:
                ModifySafeInfoActivity.start(mContext);
                break;
            case R.id.btn_user_info_logout:
                showLogoutDialog();
                break;
        }
    }

    /**
     * 询问用户是否要退出
     */
    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle("您确定要退出登录吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLogoutDialog.dismiss();
                logout();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLogoutDialog.dismiss();
            }
        });
        mLogoutDialog = builder.create();
        mLogoutDialog.show();

    }

    /**
     * 注销登录
     */
    private void logout() {
        UserBean.shared().logout();
        ProgressHUD.showInfo(mContext, "退出成功");
        finish();
    }
}
