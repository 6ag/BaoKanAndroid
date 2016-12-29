package tv.baokan.baokanandroid.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.ArticleDetailBean;
import tv.baokan.baokanandroid.model.CommentBean;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.DateUtils;
import tv.baokan.baokanandroid.utils.ImageCacheUtils;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.SharedPreferencesUtils;
import tv.baokan.baokanandroid.utils.SizeUtils;
import tv.baokan.baokanandroid.utils.StatusUtils;
import tv.baokan.baokanandroid.utils.StreamUtils;


public class NewsDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "NewsDetailActivity";

    private Context mContext;

    private String classid;                 // 栏目id
    private String id;                      // 文章id
    private ArticleDetailBean detailBean;   // 新闻详情模型
    private List<CommentBean> commentBeanList; // 评论模型集合

    private ProgressBar mProgressBar;       // 进度圈
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

    private LinearLayout mCommentLayout;       // 评论
    private RecyclerView mCommentRecyclerView; // 评论列表
    private CommentRecyclerViewAdapter mCommentRecyclerViewAdapter;
    private Button mMoreCommentButton;          // 更多评论

    private AlertDialog commentDialog;      // 评论会话框
    private EditText commentEditText;       // 评论文本框

    private AlertDialog setFontDialog;      // 设置字体的会话框
    private int fontSize;                   // 修改后的字体大小

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
        // 将MIUI/魅族的状态栏改成暗色
        if (!StatusUtils.setMiuiStatusBarDarkMode(this, true) && !StatusUtils.setMeizuStatusBarDarkMode(this, true)) {
            LogUtils.d(TAG, "修改状态栏没有作用");
        }
        setContentView(R.layout.activity_news_detail);
        mContext = this;

        prepareUI();
        prepareData();

    }

    /**
     * 准备UI
     */
    private void prepareUI() {
        mProgressBar = (ProgressBar) findViewById(R.id.pb_news_detail_progressbar);
        mScrollView = (ScrollView) findViewById(R.id.bsv_news_detail_scrollview);
        mContentWebView = (WebView) findViewById(R.id.wv_news_detail_webview);
        mBackButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_back);
        mEditButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_edit);
        mFontButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_font);
        mCollectionButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_collection);
        mShareButton = (ImageButton) findViewById(R.id.ib_news_detail_bottom_bar_share);
        mLinkLayout = (LinearLayout) findViewById(R.id.ll_news_detail_links);
        mLinkRecyclerView = (RecyclerView) findViewById(R.id.rv_news_detail_links_recyclerview);
        mCommentLayout = (LinearLayout) findViewById(R.id.ll_news_detail_comment);
        mCommentRecyclerView = (RecyclerView) findViewById(R.id.rv_news_detail_comment_recyclerview);
        mMoreCommentButton = (Button) findViewById(R.id.btn_news_detail_comment_more);

        // view硬件加速
        mScrollView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // 新闻正文
        WebSettings webSettings = mContentWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mContentWebView.addJavascriptInterface(new ArticleJavascriptInterface(), "ARTICLE");
        mContentWebView.setWebChromeClient(new WebChromeClient() {
        });
        mContentWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 隐藏加载进度条
                mProgressBar.setVisibility(View.INVISIBLE);
                // 网页加载完成才去加载其他UI
                setupDetailData();
                // 加载网页缓存图片
                getImageFromDownloaderOrDiskByImageUrlArray();
            }

        });

        // 底部工具条按钮点击事件
        mBackButton.setOnClickListener(this);
        mEditButton.setOnClickListener(this);
        mFontButton.setOnClickListener(this);
        mCollectionButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);

        // 相关链接列表
        mLinkRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLinkRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        if (!Fresco.getImagePipeline().isPaused()) {
                            Fresco.getImagePipeline().pause();
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if (Fresco.getImagePipeline().isPaused()) {
                            Fresco.getImagePipeline().resume();
                        }
                        break;
                }
            }
        });

        // 评论列表
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        if (!Fresco.getImagePipeline().isPaused()) {
                            Fresco.getImagePipeline().pause();
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if (Fresco.getImagePipeline().isPaused()) {
                            Fresco.getImagePipeline().resume();
                        }
                        break;
                }
            }
        });

        // 更多评论
        mMoreCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到评论列表

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
                showCommentDialog();
                break;
            case R.id.ib_news_detail_bottom_bar_font:
                showSetFontDialog();
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
     * 弹出评论的会话框
     */
    private void showCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.dialog_comment, null);
        builder.setView(view);
        builder.setCancelable(true);
        commentDialog = builder.create();
        commentDialog.show();

        commentEditText = (EditText) view.findViewById(R.id.et_comment_edittext);
        Button cancelButton = (Button) view.findViewById(R.id.btn_set_font_cancel);
        Button sendButton = (Button) view.findViewById(R.id.btn_set_font_send);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentDialog.dismiss();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = commentEditText.getText().toString();
                if (!TextUtils.isEmpty(comment)) {
                    // 发布评论

                    commentDialog.dismiss();
                } else {
                    Toast.makeText(mContext, "请输入评论内容", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * 弹出显示选择字体的会话框
     */
    private void showSetFontDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.dialog_set_font, null);
        builder.setView(view);
        builder.setCancelable(true);
        setFontDialog = builder.create();
        setFontDialog.show();

        RadioGroup setFontGroup = (RadioGroup) view.findViewById(R.id.rg_set_font_group);
        // 根据缓存的字体选择默认的item
        switch (SharedPreferencesUtils.getInt(mContext, SharedPreferencesUtils.DETAIL_FONT, 18)) {
            case 16:
                setFontGroup.check(R.id.rb_set_font_small);
                break;
            case 18:
                setFontGroup.check(R.id.rb_set_font_middle);
                break;
            case 20:
                setFontGroup.check(R.id.rb_set_font_big);
                break;
            case 22:
                setFontGroup.check(R.id.rb_set_font_verybig);
                break;
        }
        setFontGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 不会直接修改，而是先存储起来，点击确认的时候才修改
                switch (checkedId) {
                    case R.id.rb_set_font_verybig:
                        fontSize = 22;
                        break;
                    case R.id.rb_set_font_big:
                        fontSize = 20;
                        break;
                    case R.id.rb_set_font_middle:
                        fontSize = 18;
                        break;
                    case R.id.rb_set_font_small:
                        fontSize = 16;
                        break;
                }
            }
        });
        Button cancelButton = (Button) view.findViewById(R.id.btn_set_font_cancel);
        Button confirmButton = (Button) view.findViewById(R.id.btn_set_font_confirm);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFontDialog.dismiss();
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 修改字体
                SharedPreferencesUtils.setInt(mContext, SharedPreferencesUtils.DETAIL_FONT, fontSize);
                didChangedFontSize(fontSize);
                setFontDialog.dismiss();
            }
        });
    }

    /**
     * 修改了正文字体大小
     */
    private void didChangedFontSize(int fontSize) {
        mContentWebView.loadUrl("javascript:setFontSize(" + fontSize + ")");
    }

    /**
     * 加载新闻详情数据
     */
    private void loadNewsDetailFromNetwork() {

        OkHttpUtils
                .get()
                .url(APIs.ARTICLE_DETAIL)
                .addParams("classid", classid)
                .addParams("id", id)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(mContext, "您的网络不给力哦", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject jsonObject = new JSONObject(response).getJSONObject("data");
                            detailBean = new ArticleDetailBean(jsonObject);
                            // 加载webView
                            setupWebViewData();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /**
     * 加载最新10条评论数据
     */
    private void loadCommentFromNetwork() {
        OkHttpUtils
                .get()
                .url(APIs.GET_COMMENT)
                .addParams("classid", classid)
                .addParams("id", id)
                .addParams("pageIndex", "1")
                .addParams("pageSize", "10")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(mContext, "您的网络不给力哦", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            List<CommentBean> tempBeanList = new ArrayList<>();
                            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                CommentBean commentBean = new CommentBean(jsonArray.getJSONObject(i));
                                tempBeanList.add(commentBean);
                            }
                            commentBeanList = tempBeanList;

                            // 配置评论数据
                            setupCommentData();
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

        // 加载页面
        mScrollView.setVisibility(View.VISIBLE);

        // 加载相关链接
        if (detailBean.getOtherLinks() != null) {
            mLinkLayout.setVisibility(View.VISIBLE);
            mLinkRecyclerViewAdapter = new LinkRecyclerViewAdapter(detailBean.getOtherLinks(), this);
            mLinkRecyclerView.setAdapter(mLinkRecyclerViewAdapter);
        }

        // 更新收藏状态

        // 页面加载完才去请求评论数据
        loadCommentFromNetwork();
    }

    /**
     * 配置评论数据
     */
    private void setupCommentData() {

        // 加载评论数据
        if (commentBeanList != null && commentBeanList.size() > 0) {
            mCommentLayout.setVisibility(View.VISIBLE);
            mCommentRecyclerViewAdapter = new CommentRecyclerViewAdapter(commentBeanList, this);
            mCommentRecyclerView.setAdapter(mCommentRecyclerViewAdapter);

            // 评论数量不低于10条才显示更多评论
            if (commentBeanList.size() <= 10) {
                mMoreCommentButton.setVisibility(View.GONE);
            } else {
                mMoreCommentButton.setVisibility(View.VISIBLE);
            }

        }
    }

    /**
     * 配置webView数据
     */
    private void setupWebViewData() {

        // 发布时间
        String newstime = newstime = DateUtils.getStringTime(detailBean.getNewstime());;

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
        int fontSize = SharedPreferencesUtils.getInt(mContext, SharedPreferencesUtils.DETAIL_FONT, 18);
        html += "<div id=\"content\" style=\"font-size: " + fontSize + "px;\">" + tempNewstext + "</div>";

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
            final RecyclerView.ViewHolder holder;
            View view;
            if (viewType == LINK_ITEM_TYPE.NO_TITLE_PIC.ordinal()) {
                view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_detail_link_notitlepic, parent, false);
                holder = new NoTitlePicViewHolder(view);
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_detail_link_titlepic, parent, false);
                holder = new TitlePicViewHolder(view);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    // 进入相关链接的正文页面
                    NewsDetailActivity.start(mContext, linkBeanList.get(position).getClassid(), linkBeanList.get(position).getId());
                    NewsDetailActivity.this.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
                }
            });
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
            View itemView;

            LinkBaseViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
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

    // 相关链接适配器
    private class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder> {

        List<CommentBean> commentBeanList;
        Context mContext;

        CommentRecyclerViewAdapter(List<CommentBean> commentBeanList, Context mContext) {
            this.commentBeanList = commentBeanList;
            this.mContext = mContext;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_detail_comment, parent, false);
            ViewHolder holder = new ViewHolder(view);
            holder.starLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "点赞", Toast.LENGTH_SHORT).show();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CommentBean commentBean = commentBeanList.get(position);
            holder.portraitView.setImageURI(commentBean.getUserpic());
            holder.nicknameTextView.setText(commentBean.getPlnickname());
            holder.commentContentTextView.setText(commentBean.getSaytext());
            holder.timeTextView.setText(commentBean.getSaytime());
            holder.starNumTextView.setText(commentBean.getZcnum());
            holder.commentNumTextView.setText(commentBean.getPlstep());
            // 最后一个分割线隐藏
            if (position == commentBeanList.size() - 1) {
                holder.lineView.setVisibility(View.INVISIBLE);
            } else {
                holder.lineView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return commentBeanList.size();
        }

        // 相关链接item基类
        class ViewHolder extends RecyclerView.ViewHolder {

            SimpleDraweeView portraitView;// 头像
            TextView nicknameTextView;    // 昵称
            TextView timeTextView;        // 时间
            TextView starNumTextView;     // 点赞数
            TextView commentNumTextView;  // 楼层
            TextView commentContentTextView; // 评论内容
            LinearLayout starLayout;      // 赞
            View lineView;                // 分割线

            ViewHolder(View itemView) {
                super(itemView);
                portraitView = (SimpleDraweeView) itemView.findViewById(R.id.sdv_cell_news_detail_comment_portrait);
                nicknameTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_name);
                timeTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_time);
                starNumTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_star_num);
                commentNumTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_num);
                commentContentTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_content);
                lineView = itemView.findViewById(R.id.v_cell_news_detail_comment_line);
                starLayout = (LinearLayout) itemView.findViewById(R.id.ll_news_detail_comment_star);
            }
        }

    }

}
