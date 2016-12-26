package tv.baokan.baokanandroid.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

public class BaoKanApp extends Application {

    // 用于存放所有启动的Activity的集合
    private List<Activity> mActivityList;

    public static float WINDOW_DENSITY; // 屏幕密度 dpi
    public static int WINDOW_WIDTH;     // 屏幕宽度 px
    public static int WINDOW_HEIGHT;    // 屏幕高度 px

    @Override
    public void onCreate() {
        super.onCreate();

        // 存放所有activity的集合
        mActivityList = new ArrayList<>();

        // 初始化Fresco
        Fresco.initialize(this);

        // 初始化app异常处理器
        CrashHandler handler = CrashHandler.getInstance();
        handler.init(getApplicationContext());

        // 获取屏幕尺寸
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        WINDOW_DENSITY = dm.density;
        WINDOW_WIDTH = dm.widthPixels;
        WINDOW_HEIGHT = dm.heightPixels;
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
