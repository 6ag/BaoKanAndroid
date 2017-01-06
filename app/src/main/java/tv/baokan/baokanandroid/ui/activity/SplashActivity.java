package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.UserBean;
import tv.baokan.baokanandroid.utils.ProgressHUD;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 更新用户登录状态
        UserBean.updateUserInfoFromNetwork(new UserBean.OnUpdatedUserInfoListener());

        // 延迟2秒进入主界面
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 2000);

    }
}
