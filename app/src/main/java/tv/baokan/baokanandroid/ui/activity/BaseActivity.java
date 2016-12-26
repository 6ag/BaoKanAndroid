package tv.baokan.baokanandroid.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

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

}
