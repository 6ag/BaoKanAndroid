package tv.baokan.baokanandroid.app;

import android.app.Activity;
import android.app.Application;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

public class BaoKanApp extends Application {

    // 用于存放所有启动的Activity的集合
    private List<Activity> mActivityList;

    @Override
    public void onCreate() {
        super.onCreate();

        // 存放所有activity的集合
        mActivityList = new ArrayList<>();

        // 初始化Fresco
        Fresco.initialize(this);

        // 初始化app异常处理器
//        CrashHandler handler = CrashHandler.getInstance();
//        handler.init(getApplicationContext());

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }

    /**
     * 添加Activity
     */
    public void addActivity(Activity activity) {
        if (!mActivityList.contains(activity)) {
            mActivityList.add(activity);
        }
    }

    /**
     * 销毁单个Activity
     */
    public void removeActivity(Activity activity) {
        if (mActivityList.contains(activity)) {
            mActivityList.remove(activity);
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    /**
     * 销毁所有的Activity
     */
    public void removeAllActivity() {
        for (Activity activity : mActivityList) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

}
