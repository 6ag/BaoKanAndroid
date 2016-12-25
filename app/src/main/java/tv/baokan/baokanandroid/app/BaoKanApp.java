package tv.baokan.baokanandroid.app;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;

public class BaoKanApp extends Application {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        Fresco.initialize(this);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }
}
