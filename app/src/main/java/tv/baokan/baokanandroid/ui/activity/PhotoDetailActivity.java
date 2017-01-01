package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.ArticleDetailBean;
import tv.baokan.baokanandroid.model.UserBean;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;

public class PhotoDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PhotoDetailActivity";
    private Context mContext;
    private String classid;                 // 栏目id
    private String id;                      // 文章id
    private ArticleDetailBean detailBean;   // 图库详情模型

    private ImageButton mBackButton;        // 底部条 返回
    private ImageButton mEditButton;        // 底部条 编辑发布评论信息
    private ImageButton mCommentButton;     // 底部条 评论列表
    private ImageButton mCollectionButton;  // 底部条 收藏
    private ImageButton mShareButton;       // 底部条 分享

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
        mContext = this;

        prepareUI();
        prepareData();

    }

    /**
     * 准备UI
     */
    private void prepareUI() {
        mBackButton = (ImageButton) findViewById(R.id.ib_photo_detail_bottom_bar_back);
        mEditButton = (ImageButton) findViewById(R.id.ib_photo_detail_bottom_bar_edit);
        mCommentButton = (ImageButton) findViewById(R.id.ib_photo_detail_bottom_bar_comment);
        mCollectionButton = (ImageButton) findViewById(R.id.ib_photo_detail_bottom_bar_collection);
        mShareButton = (ImageButton) findViewById(R.id.ib_photo_detail_bottom_bar_share);

        // 底部工具条按钮点击事件
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

                break;
            case R.id.ib_photo_detail_bottom_bar_collection:
                collectArticle();
                break;
            case R.id.ib_photo_detail_bottom_bar_share:

                break;
        }
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
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("classid", classid);
        parameters.put("id", id);
        if (UserBean.isLogin()) {
            parameters.put("username", UserBean.shared().getUsername());
            parameters.put("userid", String.valueOf(UserBean.shared().getId()));
            parameters.put("token", UserBean.shared().getToken());
        }

        NetworkUtils.shared.get(APIs.ARTICLE_DETAIL, parameters, new NetworkUtils.StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ProgressHUD.showInfo(mContext, "您的网络不给力哦");
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    JSONObject jsonObject = new JSONObject(response).getJSONObject("data");
                    detailBean = new ArticleDetailBean(jsonObject);
                    setupUI();
                } catch (JSONException e) {
                    e.printStackTrace();
                    ProgressHUD.showInfo(mContext, "数据解析异常");
                }
            }
        });
    }

    /**
     * 详情数据加载成功后 - 配置UI
     */
    private void setupUI() {

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

        }, 500);
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
            parameters.put("userid", String.valueOf(UserBean.shared().getId()));
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
            parameters.put("userid", String.valueOf(UserBean.shared().getId()));
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
                        ProgressHUD.showInfo(mContext, tipString);
                        collectionButtonSpringAnimation();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ProgressHUD.showInfo(mContext, "数据解析异常");
                    }
                }
            });
        } else {
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
