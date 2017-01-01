package tv.baokan.baokanandroid.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.app.BaoKanApp;

public class BaseActivity extends AppCompatActivity {

    private BaoKanApp application;
    private BaseActivity mContext;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (application == null) {
            application = (BaoKanApp) getApplication();
        }
        mContext = this;

        // 添加当前activity
        addActivity();

        // 沉浸式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 移除当前activity
        removeActivity();
    }

    /**
     * 添加Activity方法
     */
    public void addActivity() {
        application.addActivity(mContext);
    }

    /**
     * 销毁当个Activity方法
     */
    public void removeActivity() {
        application.removeActivity(mContext);
    }

    /**
     * 销毁所有Activity方法
     */
    public void removeAllActivity() {
        application.removeAllActivity();
    }

    /**
     * 把Toast定义成一个方法  可以重复使用，使用时只需要传入需要提示的内容即可
     * @param text 提示文字
     */
    public void showToast(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 重写finish方法，增加返回动画
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
    }

    // 返回true则是不继续传播事件，自己处理。返回false则系统继续传播处理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
