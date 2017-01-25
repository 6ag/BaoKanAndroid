package tv.baokan.baokanandroid.ui.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.sharesdk.onekeyshare.OnekeyShare;
import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.adapter.PhotoDetailViewPageAdapter;
import tv.baokan.baokanandroid.cache.NewsDALManager;
import tv.baokan.baokanandroid.model.ArticleDetailBean;
import tv.baokan.baokanandroid.model.UserBean;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.utils.SizeUtils;

public class PhotoDetailActivity extends BaseActivity implements View.OnClickListener {

    private int mPosition = 0;
    private static final String TAG = "PhotoDetailActivity";
    private String classid;                 // 栏目id
    private String id;                      // 文章id
    private ArticleDetailBean detailBean;   // 图库详情模型
    private List<ArticleDetailBean.ArticleDetailPhotoBean> photoBeans; // 图库所有图片模型集合

    private PhotoDetailViewPageAdapter adapter;

    private ViewPager mViewPager;           // 图片载体
    private View mTopLayout;                // 顶部视图
    private TextView mPageTextView;          // 页码
    private TextView mReportTextView;       // 举报

    private ProgressBar mProgressBar;       // 进度圈

    private View mBottomLayout;             // 底部视图
    private NestedScrollView mCaptionScriollView; // 图片文字介绍父视图
    private TextView mCaptionTextView;      // 图片文字介绍
    private ImageButton mBackButton;        // 底部条 返回
    private ImageButton mEditButton;        // 底部条 编辑发布评论信息
    private ImageButton mCommentButton;     // 底部条 评论列表
    private ImageButton mCollectionButton;  // 底部条 收藏
    private ImageButton mShareButton;       // 底部条 分享

    private TextView mPlnumTextView;        // 评论数量

    private AlertDialog commentDialog;      // 评论会话框
    private EditText commentEditText;       // 评论文本框

    /**
     * 便捷启动当前activity
     *
     * @param activity 来源activity
     * @param classid  栏目id
     * @param id       文章id
     */
    public static void start(Activity activity, String classid, String id) {
        Intent intent = new Intent(activity, PhotoDetailActivity.class);
        intent.putExtra("classid_key", classid);
        intent.putExtra("id_key", id);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        prepareUI();
        prepareData();

    }

    /**
     * 准备UI
     */
    private void prepareUI() {
        mViewPager = (ViewPager) findViewById(R.id.vp_photo_detail_viewPager);
        mTopLayout = findViewById(R.id.ll_photo_detail_top_layout);
        mPageTextView = (TextView) findViewById(R.id.tv_photo_detail_page);
        mReportTextView = (TextView) findViewById(R.id.tv_photo_detail_report);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_photo_detail_progressbar);
        mBottomLayout = findViewById(R.id.ll_photo_detail_bottom_layout);
        mCaptionScriollView = (NestedScrollView) findViewById(R.id.nsv_photo_detail_caption_scrollview);
        mCaptionTextView = (TextView) findViewById(R.id.tv_photo_detail_caption);
        mBackButton = (ImageButton) findViewById(R.id.ib_photo_detail_bottom_bar_back);
        mEditButton = (ImageButton) findViewById(R.id.ib_photo_detail_bottom_bar_edit);
        mCommentButton = (ImageButton) findViewById(R.id.ib_photo_detail_bottom_bar_comment);
        mCollectionButton = (ImageButton) findViewById(R.id.ib_photo_detail_bottom_bar_collection);
        mShareButton = (ImageButton) findViewById(R.id.ib_photo_detail_bottom_bar_share);
        mPlnumTextView = (TextView) findViewById(R.id.tv_photo_detail_plnum);

        // 监听点击事件
        mReportTextView.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        mEditButton.setOnClickListener(this);
        mCommentButton.setOnClickListener(this);
        mCollectionButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_photo_detail_bottom_bar_back:
                finish();
                break;
            case R.id.ib_photo_detail_bottom_bar_edit:
                showCommentDialog();
                break;
            case R.id.ib_photo_detail_bottom_bar_comment:
                CommentListActivity.start(mContext, classid, id, null, "photo");
                break;
            case R.id.ib_photo_detail_bottom_bar_collection:
                collectArticle();
                break;
            case R.id.ib_photo_detail_bottom_bar_share:
                share();
                break;
            case R.id.tv_photo_detail_report:
                ProgressHUD.showInfo(mContext, "举报成功");
                break;
        }
    }

    /**
     * 分享
     */
    private void share() {
        OnekeyShare oks = new OnekeyShare();
        // 关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网、QQ和QQ空间使用
        oks.setTitle(detailBean.getTitle());
        // titleUrl是标题的网络链接，仅在Linked-in,QQ和QQ空间使用
        oks.setTitleUrl(detailBean.getTitleurl());
        // text是分享文本，所有平台都需要这个字段
        oks.setText(photoBeans.get(mPosition).getCaption());
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        oks.setImageUrl(photoBeans.get(mPosition).getBigpic());
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
     * 准备数据
     */
    private void prepareData() {

        // 取出启动activity时传递的数据
        Intent intent = getIntent();
        classid = intent.getStringExtra("classid_key");
        id = intent.getStringExtra("id_key");

        // 加载网络数据
        loadPhotoDetailFromNetwork();
    }

    /**
     * 加载图片详情数据从网络
     */
    private void loadPhotoDetailFromNetwork() {

        NewsDALManager.shared.loadNewsContent(classid, id, new NewsDALManager.NewsContentCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                detailBean = new ArticleDetailBean(jsonObject);

                // 配置UI
                setupUI(detailBean);
            }

            @Override
            public void onError(String tipString) {
                ProgressHUD.showInfo(mContext, tipString);
            }
        });

    }

    /**
     * 详情数据加载成功后 - 配置UI
     *
     * @param detailBean 详情模型
     */
    private void setupUI(ArticleDetailBean detailBean) {
        photoBeans = detailBean.getMorePicsList();

        // 隐藏进度条
        mProgressBar.setVisibility(View.INVISIBLE);

        // 评论数量
        mPlnumTextView.setText(detailBean.getPlnum());

        // 收藏状态 1已经收藏过
        if (detailBean.getHavefava().equals("1")) {
            mCollectionButton.setImageResource(R.drawable.bottom_bar_collection_selected);
        } else {
            mCollectionButton.setImageResource(R.drawable.bottom_bar_collection_normal1);
        }

        // 组合图片url集合
        List<String> photoList = new ArrayList<>();
        for (ArticleDetailBean.ArticleDetailPhotoBean photoBean :
                photoBeans) {
            photoList.add(photoBean.getBigpic());
        }

        //设置ViewPager
        adapter = new PhotoDetailViewPageAdapter(this, photoList);
        adapter.setOnPhotoTapListener(new PhotoDetailViewPageAdapter.OnPhotoTapListener() {
            @Override
            public void onPhotoTap() {
                // 隐藏/显示 顶部/底部视图
                onPhotoOneTapped();
            }
        });
        mViewPager.setAdapter(adapter);

        // 默认从第一页开始浏览
        mViewPager.setCurrentItem(mPosition);
        onPageChanged(mPosition);

        // 监听viewPager的滚动
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                onPageChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    /**
     * 页码发生改变 更新页码指示器和图片描述文字
     *
     * @param position 页码
     */
    private void onPageChanged(int position) {
        mPageTextView.setText(position + 1 + "/" + photoBeans.size());
        mCaptionTextView.setText(photoBeans.get(position).getCaption());

        // 滚动到最顶部
        mCaptionScriollView.scrollTo(0, 0);

        // 修改文本描述载体scrollView高度
        ViewTreeObserver vto = mCaptionTextView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCaptionTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = mCaptionTextView.getHeight();

                int scrollViewHeight = height + SizeUtils.dip2px(mContext, 45);
                int scrollViewMaxHeight = SizeUtils.dip2px(mContext, 120);

                // 修改文字载体ScrollView的高度 = mCaptionTextView高度 + 35dp，并且现在最大高度为200dip
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mCaptionScriollView.getLayoutParams();
                layoutParams.height = scrollViewHeight < scrollViewMaxHeight ? scrollViewHeight : scrollViewMaxHeight;
                mCaptionScriollView.setLayoutParams(layoutParams);
            }
        });

    }

    // 是否是展开状态 - 默认是展开
    boolean flag = true;

    /**
     * 图片单击手势 隐藏/展开 顶部、底部视图
     */
    private void onPhotoOneTapped() {
        if (flag) {
            flag = false;

            // 隐藏状态栏
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

            // 为了调节隐藏UI的动画和隐藏状态栏动画一致
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 隐藏顶部底部视图
                    setupAnimation(mBottomLayout, R.animator.bottom_hide);
                    setupAnimation(mTopLayout, R.animator.top_hide);
                }
            }, 200);

        } else {
            flag = true;

            // 展开顶部底部视图
            setupAnimation(mBottomLayout, R.animator.bottom_show);
            setupAnimation(mTopLayout, R.animator.top_show);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 显示状态栏
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }, 330);

        }
    }

    /**
     * 给view添加指定属性动画
     *
     * @param target 需要添加动画的对象
     * @param id     动画id
     */
    private void setupAnimation(View target, int id) {
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(this, id);
        set.setTarget(target);
        set.start();
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
                    ProgressHUD.showInfo(mContext, "请输入评论内容");
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

        }, 250);
    }

    /**
     * 发布评论
     *
     * @param comment 评论信息
     */
    private void sendComment(String comment) {

        LogUtils.d(TAG, "评论内容 = " + comment);
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
                ProgressHUD.showInfo(mContext, "您的网络不给力哦");
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtils.d(TAG, response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("err_msg").equals("success")) {
                        ProgressHUD.showInfo(mContext, "评论成功");
                    } else {
                        ProgressHUD.showInfo(mContext, jsonObject.getString("info"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ProgressHUD.showInfo(mContext, "数据解析异常");
                }
            }
        });

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
                    ProgressHUD.showInfo(mContext, "您的网络不给力哦");
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
                                mCollectionButton.setImageResource(R.drawable.bottom_bar_collection_normal1);
                            }
                        }
                        if (tipString.equals("您还没登录!")) {
                            showLoginTipDialog();
                            // 注销本地用户信息
                            UserBean.shared().logout();
                        } else {
                            ProgressHUD.showInfo(mContext, tipString);
                        }
                        collectionButtonSpringAnimation();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ProgressHUD.showInfo(mContext, "数据解析异常");
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
                LoginActivity.start(PhotoDetailActivity.this);
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

}
