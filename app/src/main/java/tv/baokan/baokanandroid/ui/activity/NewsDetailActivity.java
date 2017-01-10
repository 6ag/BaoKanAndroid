package tv.baokan.baokanandroid.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.cache.NewsDALManager;
import tv.baokan.baokanandroid.model.ArticleDetailBean;
import tv.baokan.baokanandroid.model.CommentBean;
import tv.baokan.baokanandroid.model.UserBean;
import tv.baokan.baokanandroid.adapter.CommentRecyclerViewAdapter;
import tv.baokan.baokanandroid.adapter.LinkRecyclerViewAdapter;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.DateUtils;
import tv.baokan.baokanandroid.utils.FileCacheUtils;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.utils.SharedPreferencesUtils;
import tv.baokan.baokanandroid.utils.SizeUtils;
import tv.baokan.baokanandroid.utils.StatusUtils;
import tv.baokan.baokanandroid.utils.StreamUtils;


public class NewsDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "NewsDetailActivity";

    private String classid;                 // 栏目id
    private String id;                      // 文章id
    private ArticleDetailBean detailBean;   // 新闻详情模型
    private List<CommentBean> commentBeanList; // 评论模型集合

    private ViewGroup mContentView;         // 最外层视图
    private ProgressBar mProgressBar;       // 进度圈
    private ScrollView mScrollView;         // 内容载体 scrollView
    private WebView mContentWebView;        // 正文载体 webView
    private ImageButton mBackButton;        // 底部条 返回
    private ImageButton mEditButton;        // 底部条 编辑发布评论信息
    private ImageButton mFontButton;        // 底部条 设置字体
    private ImageButton mCollectionButton;  // 收藏
    private ImageButton mShareButton;       // 分享

    private View mShareQQButton;          // qq分享
    private View mShareWxButton;          // 微信分享
    private View mSharePyqButton;         // 朋友圈

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
     * @param activity 来源activity
     * @param classid  栏目id
     * @param id       文章id
     */
    public static void start(Activity activity, String classid, String id) {
        Intent intent = new Intent(activity, NewsDetailActivity.class);
        intent.putExtra("classid_key", classid);
        intent.putExtra("id_key", id);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 将MIUI/魅族的状态栏文字图标改成暗色
        if (!StatusUtils.setMiuiStatusBarDarkMode(this, true) && !StatusUtils.setMeizuStatusBarDarkMode(this, true)) {
            LogUtils.d(TAG, "修改状态栏没有作用");
        }
        setContentView(R.layout.activity_news_detail);

        prepareUI();
        prepareData();

    }

    /**
     * 准备UI
     */
    private void prepareUI() {
        mContentView = (ViewGroup) findViewById(R.id.activity_news_detail);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_news_detail_progressbar);
        mScrollView = (ScrollView) findViewById(R.id.bsv_news_detail_scrollview);
        mContentWebView = (WebView) findViewById(R.id.wv_news_detail_webview);
        mShareQQButton = findViewById(R.id.ll_article_content_share_qq);
        mShareWxButton = findViewById(R.id.ll_article_content_share_weixin);
        mSharePyqButton = findViewById(R.id.ll_article_content_share_pyq);
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

        // 新闻正文
        WebSettings webSettings = mContentWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // 开启硬件加速后，webView内容太大会crash 还在寻求最终解决办法
//        mContentWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
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
                // 页面滑动到顶部
                mScrollView.fullScroll(ScrollView.FOCUS_UP);
            }

        });

        // 底部工具条按钮点击事件
        mBackButton.setOnClickListener(this);
        mEditButton.setOnClickListener(this);
        mFontButton.setOnClickListener(this);
        mCollectionButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);
        mShareQQButton.setOnClickListener(this);
        mShareWxButton.setOnClickListener(this);
        mSharePyqButton.setOnClickListener(this);

        // 更多评论
        mMoreCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到评论列表
                CommentListActivity.start(mContext, classid, id, commentBeanList, "news");
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
                break;
            case R.id.ib_news_detail_bottom_bar_edit:
                showCommentDialog();
                break;
            case R.id.ib_news_detail_bottom_bar_font:
                showSetFontDialog();
                break;
            case R.id.ib_news_detail_bottom_bar_collection:
                collectArticle();
                break;
            case R.id.ib_news_detail_bottom_bar_share:
                // 弹出分享ui
                showShare(null);
                break;
            case R.id.ll_article_content_share_qq:
                // qq分享
                showShare(ShareSDK.getPlatform(QQ.NAME).getName());
                break;
            case R.id.ll_article_content_share_weixin:
                // 微信分享
                showShare(ShareSDK.getPlatform(Wechat.NAME).getName());
                break;
            case R.id.ll_article_content_share_pyq:
                // 朋友圈分享
                showShare(ShareSDK.getPlatform(WechatMoments.NAME).getName());
                break;
        }
    }

    /**
     * 分享
     */
    private void showShare(String platform) {
        OnekeyShare oks = new OnekeyShare();
        //指定分享的平台，如果为空，还是会调用九宫格的平台列表界面
        if (platform != null) {
            oks.setPlatform(platform);
        }
        // 关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网、QQ和QQ空间使用
        oks.setTitle(detailBean.getTitle());
        // titleUrl是标题的网络链接，仅在Linked-in,QQ和QQ空间使用
        oks.setTitleUrl(detailBean.getTitleurl());
        // text是分享文本，所有平台都需要这个字段
        oks.setText(detailBean.getSmalltext());
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        if (!TextUtils.isEmpty(detailBean.getTitlepic())) {
            oks.setImageUrl(detailBean.getTitlepic());
        } else {
            // 默认图片，放服务器
            oks.setImageUrl("http://www.baokan.tv/d/file/p/2017-01-05/8c81061deb5b31ce6fb8e3a018afe8e5.jpg");
        }
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(detailBean.getTitleurl());
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("爆侃网文");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(detailBean.getTitleurl());
        // 启动分享GUI
        oks.show(mContext);
    }

    /**
     * 收藏文章
     */
    private void collectArticle() {
        if (UserBean.isLogin()) {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("username", UserBean.shared().getUsername());
            parameters.put("userid", UserBean.shared().getUserid());
            parameters.put("token", UserBean.shared().getToken());
            parameters.put("classid", classid);
            parameters.put("id", id);
            NetworkUtils.shared.post(APIs.ADD_DEL_FAVA, parameters, new NetworkUtils.StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    ProgressHUD.showInfo(NewsDetailActivity.this, "您的网络不给力哦");
                }

                @Override
                public void onResponse(String response, int id) {
                    LogUtils.d(TAG, response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String tipString = jsonObject.getString("info");
                        if (jsonObject.getString("err_msg").equals("success")) {
                            if (jsonObject.getJSONObject("result").getInt("status") == 1) {
                                // 收藏成功
                                tipString = "收藏成功";
                                mCollectionButton.setImageResource(R.drawable.bottom_bar_collection_selected);
                            } else {
                                // 取消收藏成功
                                tipString = "取消收藏";
                                mCollectionButton.setImageResource(R.drawable.bottom_bar_collection_normal2);
                            }
                        }
                        if (tipString.equals("您还没登录!")) {
                            showLoginTipDialog();
                            // 注销本地用户信息
                            UserBean.shared().logout();
                        } else {
                            ProgressHUD.showInfo(NewsDetailActivity.this, tipString);
                        }
                        collectionButtonSpringAnimation();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ProgressHUD.showInfo(NewsDetailActivity.this, "数据解析异常");
                    }
                }
            });
        } else {
            showLoginTipDialog();
        }
    }

    /**
     * 显示登录提示会话框
     */
    private void showLoginTipDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("您还未登录");
        builder.setMessage("登录以后才能收藏文章哦！");
        builder.setPositiveButton("登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LoginActivity.start(NewsDetailActivity.this);
            }
        });
        builder.setNegativeButton("以后再说", null);
        builder.show();
    }

    /**
     * 收藏按钮弹簧动画
     */
    private void collectionButtonSpringAnimation() {
        SpringSystem springSystem = SpringSystem.create();
        Spring spring = springSystem.createSpring();
        spring.addListener(new SimpleSpringListener() {

            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                float scale = value * 2f;
                mCollectionButton.setScaleX(scale);
                mCollectionButton.setScaleY(scale);
            }
        });

        spring.setEndValue(0.5);
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
                    sendComment(comment);
                    commentDialog.dismiss();
                } else {
                    ProgressHUD.showInfo(NewsDetailActivity.this, "请输入评论内容");
                }
            }
        });

        // 自动弹出键盘
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                InputMethodManager inputManager = (InputMethodManager) commentEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(commentEditText, 0);
            }

        }, 500);
    }

    /**
     * 发布评论 - 把所有评论信息的用户名都改了。。
     *
     * @param comment 评论信息
     */
    private void sendComment(String comment) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("classid", classid);
        parameters.put("id", id);
        parameters.put("saytext", comment);
        if (UserBean.isLogin()) {
            parameters.put("nomember", "0");
            parameters.put("username", UserBean.shared().getUsername());
            parameters.put("userid", UserBean.shared().getUserid());
            parameters.put("token", UserBean.shared().getToken());
        } else {
            parameters.put("nomember", "1");
        }

        NetworkUtils.shared.post(APIs.SUBMIT_COMMENT, parameters, new NetworkUtils.StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ProgressHUD.showInfo(NewsDetailActivity.this, "您的网络不给力哦");
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtils.d(TAG, response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("err_msg").equals("success")) {
                        // 评论成功后，去重新加载评论信息
                        loadCommentFromNetwork();
                        ProgressHUD.showInfo(NewsDetailActivity.this, "评论成功");
                    } else {
                        ProgressHUD.showInfo(NewsDetailActivity.this, jsonObject.getString("info"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ProgressHUD.showInfo(NewsDetailActivity.this, "数据解析异常");
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

        NewsDALManager.shared.loadNewsContent(classid, id, new NewsDALManager.NewsContentCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                detailBean = new ArticleDetailBean(jsonObject);
                // 加载webView
                setupWebViewData();
            }

            @Override
            public void onError(String tipString) {
                ProgressHUD.showInfo(mContext, tipString);
            }
        });

    }

    /**
     * 加载最新10条评论数据
     */
    private void loadCommentFromNetwork() {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("classid", classid);
        parameters.put("id", id);
        parameters.put("pageIndex", "1");
        parameters.put("pageSize", "10");

        NetworkUtils.shared.get(APIs.GET_COMMENT, parameters, new NetworkUtils.StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ProgressHUD.showInfo(mContext, "您的网络不给力哦");
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
                    ProgressHUD.showInfo(mContext, "数据解析失败");
                }
            }
        });

    }

    /**
     * 配置页面数据
     */
    private void setupDetailData() {

        // webView渲染有点慢，延迟100毫秒显示页面展示数据的UI
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollView.setVisibility(View.VISIBLE);
            }
        }, 100);

        // 1已经收藏过
        if (detailBean.getHavefava().equals("1")) {
            mCollectionButton.setImageResource(R.drawable.bottom_bar_collection_selected);
        } else {
            mCollectionButton.setImageResource(R.drawable.bottom_bar_collection_normal2);
        }

        // 加载相关链接
        if (detailBean.getOtherLinks() != null && detailBean.getOtherLinks().size() > 0) {
            mLinkLayout.setVisibility(View.VISIBLE);
            mLinkRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mLinkRecyclerViewAdapter = new LinkRecyclerViewAdapter(this, detailBean.getOtherLinks());
            mLinkRecyclerView.setAdapter(mLinkRecyclerViewAdapter);
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
        }

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
            mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mCommentRecyclerViewAdapter = new CommentRecyclerViewAdapter(this);
            mCommentRecyclerView.setAdapter(mCommentRecyclerViewAdapter);

            // 更新数据 - 0表示下拉刷新
            mCommentRecyclerViewAdapter.updateData(commentBeanList, 0);

            // 评论数量不低于10条才显示更多评论
            if (commentBeanList.size() < 10) {
                mMoreCommentButton.setVisibility(View.GONE);
            } else {
                mMoreCommentButton.setVisibility(View.VISIBLE);
            }

            // 评论列表滚动事件
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

            // 监听评论里的各种tap事件
            mCommentRecyclerViewAdapter.setOnCommentTapListener(new CommentRecyclerViewAdapter.OnCommentTapListener() {

                // 评论点赞
                @Override
                public void onStarTap(final CommentBean commentBean, final int position) {
                    HashMap<String, String> parameters = new HashMap<>();
                    parameters.put("classid", commentBean.getClassid());
                    parameters.put("id", commentBean.getId());
                    parameters.put("plid", commentBean.getPlid());
                    parameters.put("dopl", "1");
                    parameters.put("action", "DoForPl");

                    NetworkUtils.shared.post(APIs.TOP_DOWN, parameters, new NetworkUtils.StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            ProgressHUD.showInfo(mContext, "您的网络不给力哦");
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getString("err_msg").equals("success")) {
                                    int newZcnum = Integer.valueOf(commentBean.getZcnum()).intValue() + 1;
                                    commentBean.setZcnum(String.valueOf(newZcnum));
                                    commentBean.setStar(true);
                                    mCommentRecyclerViewAdapter.notifyItemChanged(position);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ProgressHUD.showInfo(mContext, "数据解析失败");
                            }
                        }
                    });
                }
            });

        }
    }

    /**
     * 配置webView数据
     */
    private void setupWebViewData() {

        // 发布时间
        String newstime = DateUtils.getStringTime(detailBean.getNewstime());

        String html = "";
        html += "<div class=\"title\">" + detailBean.getTitle() + "</div>\n";
        html += "<div class=\"time\">" + detailBean.getBefrom() + "&nbsp;&nbsp;&nbsp;&nbsp;" + newstime + "</div>\n";

        // 新闻正文html
        String tempNewstext = detailBean.getNewstext();

        // 有图片就去加载图片
        if (detailBean.getAllPhotoList().size() > 0) {

            for (int i = 0; i < detailBean.getAllPhotoList().size(); i++) {
                ArticleDetailBean.InsetPhotoBean insetPhotoBean = detailBean.getAllPhotoList().get(i);

                int width = insetPhotoBean.getWidthDip();
                int height = insetPhotoBean.getHeightDip();

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
            ProgressHUD.showInfo(mContext, "解析数据失败");
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
            FileCacheUtils.checkCacheInDisk(url, new FileCacheUtils.OnCheckCacheInDiskListener() {
                @Override
                public void checkCacheInDisk(boolean isExist, String filePath) {
                    if (isExist && !TextUtils.isEmpty(filePath)) {
                        String sendData = "replaceimage" + url + "~" + filePath;
                        mContentWebView.loadUrl("javascript:replaceContentImage('" + sendData + "');");
                    } else {
                        FileCacheUtils.downloadImage(NewsDetailActivity.this, url, new FileCacheUtils.OnDownloadImageToDiskListener() {
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
         * 让js调用这个方法进行数据回传 - 如果不需要获取图片的坐标和尺寸，直接在上面那个方法操作也行的。我这里暂时写下面吧，后面再做个转场动画
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
                        final int index = jsonObject.getInt("index");
                        int x = Math.round((float) jsonObject.getDouble("x"));
                        int y = Math.round((float) jsonObject.getDouble("y"));
                        int width = Math.round((float) jsonObject.getDouble("width"));
                        int height = Math.round((float) jsonObject.getDouble("height"));
                        String url = jsonObject.getString("url");

                        int imageViewX = SizeUtils.dip2px(mContext, x);
                        int imageViewY = SizeUtils.dip2px(mContext, y + 20) - mScrollView.getScrollY();
                        int imageViewWidth = SizeUtils.dip2px(mContext, width);
                        int imageViewHeight = SizeUtils.dip2px(mContext, height);

                        // 模拟颜色渐变的临时背景
                        final ImageView tempBgView = new ImageView(mContext);
                        RelativeLayout.LayoutParams tempBgViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        tempBgView.setLayoutParams(tempBgViewLayoutParams);
                        tempBgView.setBackgroundColor(mContext.getResources().getColor(R.color.colorPhotoBackground));
                        mContentView.addView(tempBgView);

                        // 创建一个临时图片，覆盖在被点击的正文图片上
                        final SimpleDraweeView tempImageView = new SimpleDraweeView(mContext);
                        tempImageView.setImageURI(url);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(imageViewWidth, imageViewHeight);
                        layoutParams.leftMargin = imageViewX;
                        layoutParams.topMargin = imageViewY;
                        tempImageView.setLayoutParams(layoutParams);
                        mContentView.addView(tempImageView);

                        int screenWidth = SizeUtils.getScreenWidthPx(mContext);
                        int screenHeight = SizeUtils.getScreenHeightPx(mContext);

                        // y方向偏移量达到居中
                        float offestY = (screenHeight - imageViewHeight) * 0.5f - imageViewY;

                        LogUtils.d(TAG, "x = " + imageViewX + " y = " + imageViewY + " offsetY = " + offestY + " width = " + imageViewWidth + " height = " + imageViewHeight);

                        ObjectAnimator translationY = ObjectAnimator.ofFloat(tempImageView, "translationY", offestY);
                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tempImageView, "scaleX", (float) screenWidth / imageViewWidth);
                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tempImageView, "scaleY", (float) screenWidth / imageViewWidth);
                        ObjectAnimator alphaBg = ObjectAnimator.ofFloat(tempBgView, "alpha", 0.0f, 1.0f);
                        AnimatorSet animSet = new AnimatorSet();
                        animSet.play(alphaBg).with(translationY).with(scaleX).with(scaleY);
                        animSet.setDuration(300);
                        animSet.start();
                        animSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // 进入图片浏览器activity - 无动画模式 模拟一个假象
                                PhotoBrowserActivity.start(NewsDetailActivity.this, detailBean.getAllPhotoList(), index);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mContentView.removeView(tempImageView);
                                        mContentView.removeView(tempBgView);
                                    }
                                }, 1000);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
