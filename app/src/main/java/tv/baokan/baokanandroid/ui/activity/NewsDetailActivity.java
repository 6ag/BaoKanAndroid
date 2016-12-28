package tv.baokan.baokanandroid.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.ArticleDetailBean;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.DateUtils;
import tv.baokan.baokanandroid.utils.ImageCacheUtils;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.SizeUtils;
import tv.baokan.baokanandroid.utils.StatusUtils;
import tv.baokan.baokanandroid.utils.StreamUtils;


public class NewsDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "NewsDetailActivity";

    private String classid;                 // 栏目id
    private String id;                      // 文章id
    private ArticleDetailBean detailBean;   // 新闻详情模型

    private ScrollView mScrollView;         // 内容载体 scrollView
    private WebView mContentWebView;        // 正文载体 webView
    private ImageButton mBackButton;        // 底部条 返回
    private ImageButton mEditButton;        // 底部条 编辑发布评论信息
    private ImageButton mFontButton;        // 底部条 设置字体
    private ImageButton mCollectionButton;  // 收藏
    private ImageButton mShareButton;       // 分享

    private LinearLayout mLinkLayout;       // 相关阅读
    private RecyclerView mLinkRecyclerView; // 相关阅读列表
    private LinkRecyclerViewAdapter mLinkRecyclerViewAdapter;

    RelativeLayout mFontBar;                // 设置字体的布局载体
    BottomSheetBehavior mFontBarBehavior;   // 设置字体的行为

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
        mScrollView = (ScrollView) findViewById(R.id.bsv_news_detail_scrollview);
        mContentWebView = (WebView) findViewById(R.id.wv_news_detail_webview);
        mBackButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_back);
        mEditButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_edit);
        mFontButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_font);
        mCollectionButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_collection);
        mShareButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_share);
        mLinkLayout = (LinearLayout) findViewById(R.id.ll_news_detail_links);
        mLinkRecyclerView = (RecyclerView) findViewById(R.id.rv_news_detail_links_recyclerview);

        // 底部工具条按钮点击事件
        mBackButton.setOnClickListener(this);
        mEditButton.setOnClickListener(this);
        mFontButton.setOnClickListener(this);
        mCollectionButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);

        // 底部字体设置视图
        mFontBar = (RelativeLayout) findViewById(R.id.rl_news_detail_bottom_font_bar);
        mFontBarBehavior = BottomSheetBehavior.from(mFontBar);
        mFontBarBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        // 相关链接列表
        mLinkRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLinkRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

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
        mContentWebView.addJavascriptInterface(new ArticleJavascriptInterface(), "ARTICLE");
        mContentWebView.setWebChromeClient(new WebChromeClient() {
        });
        mContentWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                getImageFromDownloaderOrDiskByImageUrlArray();
            }

        });

        // 加载网络数据
        loadNewsDetailFromNetwork();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_news_detail_bottom_bar_back:
                finish();
                overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
                break;
            case R.id.ib_news_detail_bottom_bar_edit:
                Toast.makeText(this, "弹出评论", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ib_news_detail_bottom_bar_font:
                // 弹出修改字体的视图
                mFontBarBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case R.id.ib_news_detail_bottom_bar_collection:
                Toast.makeText(this, "收藏", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ib_news_detail_bottom_bar_share:
                Toast.makeText(this, "弹出分享", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 修改了正文字体大小
     */
    private void didChangedFontSize(int fontSize) {
        mContentWebView.loadUrl("javascript:setFontSize(" + fontSize + ")");
    }

    /**
     * 修改了正文字体
     */
    private void didChangedFontName(String fontName) {
        mContentWebView.loadUrl("javascript:setFontName('" + fontName + "');");
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

        // 更新相关链接
        if (detailBean.getOtherLinks() != null) {
            mLinkRecyclerViewAdapter = new LinkRecyclerViewAdapter(detailBean.getOtherLinks(), this);
            mLinkRecyclerView.setAdapter(mLinkRecyclerViewAdapter);
        }

        // 更新收藏状态

    }

    /**
     * 配置webView数据
     */
    private void setupWebViewData() {

        // 发布时间
        String newstime = "";
        try {
            newstime = DateUtils.timestampToDateString(detailBean.getNewstime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String html = "";
        html += "<div class=\"title\">" + detailBean.getTitle() + "</div>\n";
        html += "<div class=\"time\">" + detailBean.getBefrom() + "&nbsp;&nbsp;&nbsp;&nbsp;" + newstime + "</div>\n";

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
                String imgTag = "<img onclick='ARTICLE.didTappedImage(" + i + ", \"" + imgUrl + "\");' src='" + placeholderImage + "' id='" + imgUrl + "' width='" + width + "' height='" + height + "' />";

                // 将返回的html正文里的图片占位图替换成自定义的img标签
                tempNewstext = tempNewstext.replace(placeholderString, imgTag);

            }

        }

        // 从本地缓存获取字体大小和字体名称
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

        // 将拼接好的正文html插入本地网页模板
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

            // 插图的url
            final String url = insetPhotoBean.getUrl();

            // 判断本地磁盘是否已经缓存
            ImageCacheUtils.checkCacheInDisk(url, new ImageCacheUtils.OnCheckCacheInDiskListener() {
                @Override
                public void checkCacheInDisk(boolean isExist, String filePath) {
                    if (isExist && filePath != null) {
                        String sendData = "replaceimage" + url + "~" + filePath;
                        mContentWebView.loadUrl("javascript:replaceContentImage('" + sendData + "');");
                    } else {
                        ImageCacheUtils.downloadImage(NewsDetailActivity.this, url, new ImageCacheUtils.OnDownloadImageToDiskListener() {
                            @Override
                            public void downloadFinished(boolean success, final String filePath) {
                                if (!success) {
                                    LogUtils.d(TAG, "下载文件不成功 url = " + url);
                                    return;
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String sendData = "replaceimage" + url + "~" + filePath;
                                        mContentWebView.loadUrl("javascript:replaceContentImage('" + sendData + "');");
                                    }
                                });

                            }
                        });
                    }
                }
            });

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
            finish();
            overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // java调用js需要在主线程调用
    private final class ArticleJavascriptInterface {

        /**
         * 正文图片点击事件，在这里调用js内的方法 获取被点击图片的各种信息 (4.4后还有个方法可以直接获取到js的执行返回结果)
         *
         * @param index 第几张图片被点击
         * @param url   图片的url
         */
        @JavascriptInterface
        public void didTappedImage(final int index, final String url) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContentWebView.loadUrl("javascript:didTappedImage(" + index + ", \"" + url + "\")");
                }
            });
        }

        /**
         * 让js调用这个方法进行数据回传
         *
         * @param json 回传的图片信息json数据
         */
        @JavascriptInterface
        public void didTappedImage(final String json) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogUtils.d(TAG, json);
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        int index = jsonObject.getInt("index");
                        int x = Math.round((float) jsonObject.getDouble("x"));
                        int y = Math.round((float) jsonObject.getDouble("y"));
                        int width = Math.round((float) jsonObject.getDouble("width"));
                        int height = Math.round((float) jsonObject.getDouble("height"));
                        String url = jsonObject.getString("url");
                        Toast.makeText(NewsDetailActivity.this, url, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    // 相关链接item类型枚举
    enum LINK_ITEM_TYPE {
        NO_TITLE_PIC, // 无图
        TITLE_PIC     // 有图
    }

    // 相关链接适配器
    private class LinkRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ArticleDetailBean.ArticleDetailLinkBean> linkBeanList;
        Context mContext;

        LinkRecyclerViewAdapter(List<ArticleDetailBean.ArticleDetailLinkBean> linkBeanList, Context mContext) {
            this.linkBeanList = linkBeanList;
            this.mContext = mContext;
        }

        @Override
        public int getItemViewType(int position) {
            if (linkBeanList.get(position).getTitlepic() == null) {
                return LINK_ITEM_TYPE.NO_TITLE_PIC.ordinal();
            } else {
                return LINK_ITEM_TYPE.TITLE_PIC.ordinal();
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder;
            View view;
            if (viewType == LINK_ITEM_TYPE.NO_TITLE_PIC.ordinal()) {
                view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_detail_link_notitlepic, parent, false);
                holder = new NoTitlePicViewHolder(view);
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_detail_link_titlepic, parent, false);
                holder = new TitlePicViewHolder(view);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ArticleDetailBean.ArticleDetailLinkBean linkBean = linkBeanList.get(position);
            LinkBaseViewHolder baseViewHolder = (LinkBaseViewHolder) holder;
            baseViewHolder.titleTextView.setText(linkBean.getTitle());
            baseViewHolder.classNameTextView.setText(linkBean.getClassname());
            baseViewHolder.onclickTextView.setText(linkBean.getOnclick());
            if (holder instanceof TitlePicViewHolder) {
                TitlePicViewHolder titlePicViewHolder = (TitlePicViewHolder) holder;
                titlePicViewHolder.titlePicView.setImageURI(linkBean.getTitlepic());
            }
            // 最后一个分割线隐藏
            if (position == linkBeanList.size() - 1) {
                baseViewHolder.lineView.setVisibility(View.INVISIBLE);
            } else {
                baseViewHolder.lineView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return linkBeanList.size();
        }

        // 相关链接item基类
        class LinkBaseViewHolder extends RecyclerView.ViewHolder {

            TextView titleTextView;
            TextView classNameTextView;
            TextView onclickTextView;
            View lineView;

            LinkBaseViewHolder(View itemView) {
                super(itemView);
                titleTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_link_title);
                classNameTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_link_classname);
                onclickTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_link_onclick);
                lineView = itemView.findViewById(R.id.v_cell_news_detail_link_line);
            }
        }

        // 无图的item
        class NoTitlePicViewHolder extends LinkBaseViewHolder {

            NoTitlePicViewHolder(View itemView) {
                super(itemView);
            }
        }

        // 有图的item
        class TitlePicViewHolder extends LinkBaseViewHolder {

            SimpleDraweeView titlePicView;

            TitlePicViewHolder(View itemView) {
                super(itemView);
                titlePicView = (SimpleDraweeView) itemView.findViewById(R.id.sdv_cell_news_detail_link_pic);
            }
        }

    }

}
