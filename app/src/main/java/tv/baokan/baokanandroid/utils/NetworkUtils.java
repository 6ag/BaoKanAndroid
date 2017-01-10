package tv.baokan.baokanandroid.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.PostFormBuilder;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;


public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    public static final NetworkUtils shared = new NetworkUtils();

    private NetworkUtils() {
    }

    public static abstract class StringCallback {
        public abstract void onError(Call call, Exception e, int id);

        public abstract void onResponse(String response, int id);
    }

    /**
     * get请求
     *
     * @param api        api接口
     * @param parameters 参数
     * @param callback   监听接口
     */
    public void get(String api, HashMap<String, String> parameters, final StringCallback callback) {

        LogUtils.d(TAG, "api = " + api);
        OkHttpUtils
                .get()
                .url(api)
                .params(parameters)
                .build()
                .execute(new com.zhy.http.okhttp.callback.StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onError(call, e, id);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        callback.onResponse(response, id);
                    }
                });

    }

    /**
     * post请求
     *
     * @param api        api接口
     * @param parameters 参数
     * @param callback   监听接口
     */
    public void post(String api, HashMap<String, String> parameters, final StringCallback callback) {

        LogUtils.d(TAG, "api = " + api);
        OkHttpUtils
                .post()
                .url(api)
                .params(parameters)
                .build()
                .execute(new com.zhy.http.okhttp.callback.StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onError(call, e, id);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        callback.onResponse(response, id);
                    }
                });

    }

    /**
     * 是否有网络连接
     *
     * @param context 上下文
     * @return true有网络
     */
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断WiFi是否可用
     *
     * @param context 上下文
     * @return true可用
     */
    public boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断MOBILE网络是否可用
     *
     * @param context 上下文
     * @return true可用
     */
    public boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断网络类型
     *
     * @param context 上下文
     * @return -1：没有网络  1：WIFI网络  2：wap网络  3：net网络
     */
    public static int getNetworkType(Context context) {
        int netType = -1;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
                netType = 3;
            } else {
                netType = 2;
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = 1;
        }
        return netType;
    }

}
