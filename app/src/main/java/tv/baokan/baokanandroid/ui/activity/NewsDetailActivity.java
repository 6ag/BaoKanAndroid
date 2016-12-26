package tv.baokan.baokanandroid.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.ArticleDetailBean;
import tv.baokan.baokanandroid.model.ArticleListBean;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;

public class NewsDetailActivity extends BaseActivity {

    private static final String TAG = "NewsDetailActivity";

    private String classid;   // 栏目id
    private String id;        // 文章id

    /**
     * 便捷启动当前activity
     *
     * @param context 上下文
     * @param classid 栏目id
     * @param id      文章id
     */
    public static void start(Context context, String classid, String id) {
        Intent intent = new Intent(context, NewsDetailActivity.class);
        intent.putExtra("classid_key", classid);
        intent.putExtra("id_key", id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        // 取出启动activity时传递的数据
        Intent intent = getIntent();
        classid = intent.getStringExtra("classid_key");
        id = intent.getStringExtra("id_key");

        loadNewsDetailFromNetwork();
    }

    /**
     * 加载新闻详情数据
     */
    private void loadNewsDetailFromNetwork() {

        LogUtils.d(TAG, APIs.ARTICLE_DETAIL);
        OkHttpUtils
                .get()
                .url(APIs.ARTICLE_DETAIL)
                .addParams("classid", classid)
                .addParams("id", id)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject jsonObject = new JSONObject(response).getJSONObject("data");
                            ArticleDetailBean articleDetailBean = new ArticleDetailBean(jsonObject);
                            setupData(articleDetailBean);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /**
     * 配置页面数据
     *
     * @param articleDetailBean 文章模型
     */
    private void setupData(ArticleDetailBean articleDetailBean) {

        // 加载webView部分

        // 更新收藏状态

        // 更新相关链接

    }

}
