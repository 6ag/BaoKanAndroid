package tv.baokan.baokanandroid.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.ArticleDetailBean;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.SizeUtils;
import tv.baokan.baokanandroid.utils.StatusUtils;
import tv.baokan.baokanandroid.utils.StreamUtils;


public class NewsDetailActivity extends BaseActivity {

    private static final String TAG = "NewsDetailActivity";

    private String classid;   // 栏目id
    private String id;        // 文章id
    private ScrollView mScrollView;
    private WebView mContentWebView;
    private ImageButton mBackButton;
    private ImageButton mEditButton;
    private ImageButton mFontButton;
    private ImageButton mCollectionButton;
    private ImageButton mShareButton;
    private ArticleDetailBean detailBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 将MIUI/魅族的状态栏改成暗色
        if (!StatusUtils.setMiuiStatusBarDarkMode(this, true)) {
            StatusUtils.setMeizuStatusBarDarkMode(this, true);
        }
        setContentView(R.layout.activity_news_detail);

        prepareUI();
        prepareData();
    }

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

    /**
     * 准备UI
     */
    private void prepareUI() {
        mScrollView = (ScrollView) findViewById(R.id.sv_news_detail_scrollview);
        mContentWebView = (WebView) findViewById(R.id.wv_news_detail_webview);
        mBackButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_back);
        mEditButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_edit);
        mFontButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_font);
        mCollectionButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_share);
    }

    /**
     * 准备数据
     */
    private void prepareData() {

        // 取出启动activity时传递的数据
        Intent intent = getIntent();
        classid = intent.getStringExtra("classid_key");
        id = intent.getStringExtra("id_key");

        WebSettings webSettings = mContentWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

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
                            detailBean = new ArticleDetailBean(jsonObject);
                            setupDetailData();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /**
     * 配置页面数据
     */
    private void setupDetailData() {

        // 加载webView
        setupWebViewData();

        // 更新收藏状态

        // 更新相关链接

    }

    /**
     * 配置webView数据
     */
    private void setupWebViewData() {

        String html = "";
        html += "<div class=\"title\">" + detailBean.getTitle() + "</div>";
        html += "<div class=\"time\">" + detailBean.getBefrom() + "&nbsp;&nbsp;&nbsp;&nbsp;" + detailBean.getNewstime() + "</div>";

        // 新闻正文html
        String tempNewstext = detailBean.getNewstext();

        // 有图片就去加载图片
        if (detailBean.getAllPhotoList().size() > 0) {

            for (int i = 0; i < detailBean.getAllPhotoList().size(); i++) {
                ArticleDetailBean.InsetPhotoBean insetPhotoBean = detailBean.getAllPhotoList().get(i);

                int width = insetPhotoBean.getWidthPixel();
                int height = insetPhotoBean.getHeightPixel();

                int screenWidth = SizeUtils.getScreenWidthDip(this);
                if (width > screenWidth - 40) {
                    float rate = (screenWidth - 40) / (float) width;
                    width = (int) ((float) width * rate);
                    height = (int) ((float) height * rate);
                }

                // 占位图
                String placeholderImage = "file:///android_asset/www/images/loading.jpg";
                // 占位字符串
                String placeholderString = insetPhotoBean.getRef();
                // 图片url
                String imgUrl = insetPhotoBean.getUrl();
                // 图片标签
                String imgTag = "<img onclick='didTappedImage(" + i + ", \"" + imgUrl + "\");' src='" + placeholderImage + "' id='" + imgUrl + "' width='" + width + "' height='" + height + "' />";

                // 将返回的html正文里的图片占位图替换成自定义的img标签
                tempNewstext = tempNewstext.replace(placeholderString, imgTag);

            }

            // 加载图片 - 从缓存中获取图片的本地绝对路径，发送给webView显示
            getImageFromDownloaderOrDiskByImageUrlArray();

        }

        String fontSize = "18";
        String fontName = "";

        html += "<div id=\"content\" style=\"font-size: " + fontSize + "px; font-family: '" + fontName + "';\">" + tempNewstext + "</div>";

        String localHtml = null;
        try {
            InputStream inputStream = getAssets().open("www/html/article.html");
            localHtml = StreamUtils.InputStreanToString(inputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 本地html加载一定要成功
        if (localHtml == null) {
            return;
        }

        html = localHtml.replace("<p>mainnews</p>", html);

        // 加载页面
        mContentWebView.loadDataWithBaseURL("file:///android_asset/www/html/article.html", filterHtml(html), "text/html", "utf-8", null);

    }

    /**
     * 下载或从缓存中获取图片，发送给webView
     */
    private void getImageFromDownloaderOrDiskByImageUrlArray() {

        for (ArticleDetailBean.InsetPhotoBean insetPhotoBean :
                detailBean.getAllPhotoList()) {
            final Uri uri = Uri.parse(insetPhotoBean.getUrl());

            // 判断本地磁盘是否已经缓存



        }

    }

    /**
     * 过滤html
     *
     * @param original html源字符串
     * @return 过滤后的字符串
     */
    private String filterHtml(String original) {
        String tempHtml = original.replace("<p>&nbsp;</p>", "");
        tempHtml = tempHtml.replace("<p>&nbsp;</p>", " style=\"text-indent: 2em;\"");
        return tempHtml;
    }

    // 返回true则是不继续传播事件，自己处理。返回false则系统继续传播处理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 返回时直接处理返回事件
            finish();
            overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
