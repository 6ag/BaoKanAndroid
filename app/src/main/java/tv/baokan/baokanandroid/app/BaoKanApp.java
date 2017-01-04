package tv.baokan.baokanandroid.app;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;

import org.litepal.LitePalApplication;

import java.util.ArrayList;
import java.util.List;

public class BaoKanApp extends LitePalApplication {

    // 用于存放所有启动的Activity的集合
    private List<Activity> mActivityList;

    @Override
    public void onCreate() {
        super.onCreate();

        // 存放所有activity的集合
        mActivityList = new ArrayList<>();

        // 渐进式图片
        ImagePipelineConfig config=ImagePipelineConfig.newBuilder(this)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .build();
        Fresco.initialize(this,config);

        // 初始化app异常处理器
//        CrashHandler handler = CrashHandler.getInstance();
//        handler.init(getApplicationContext());

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }

    /**
     * 获取当前应用的版本号
     *
     * @return 版本号
     */
    public String getVersionName() {
        PackageManager pm = getPackageManager();
        // 第一个参数：应用程序的包名
        // 第二个参数：指定信息的标签，0表示获取基础信息，比如包名、版本号。要想获取权限等信息必须要通过标签指定。
        try {
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
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
