package tv.baokan.baokanandroid.utils;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.PostFormBuilder;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;


public class NetworkUtils {

    public static final NetworkUtils shared = new NetworkUtils();

    private NetworkUtils() {}

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

        GetBuilder okHttpUtils = OkHttpUtils.get().url(api);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            okHttpUtils.addParams(entry.getKey(), entry.getValue());
        }
        okHttpUtils.build().execute(new com.zhy.http.okhttp.callback.StringCallback() {
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

        PostFormBuilder okHttpUtils = OkHttpUtils.post().url(api);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            okHttpUtils.addParams(entry.getKey(), entry.getValue());
        }
        okHttpUtils.build().execute(new com.zhy.http.okhttp.callback.StringCallback() {
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

}
