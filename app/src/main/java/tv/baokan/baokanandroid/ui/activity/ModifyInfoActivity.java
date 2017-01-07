package tv.baokan.baokanandroid.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.common.file.FileUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.UserBean;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.utils.StreamUtils;
import tv.baokan.baokanandroid.widget.ClearEditText;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class ModifyInfoActivity extends BaseActivity {

    private static final String TAG = "ModifyInfoActivity";

    private NavigationViewRed mNavigationViewRed;
    private View mPortraitLayout;                    // 头像区域
    private SimpleDraweeView mPortraitImageView;     // 头像
    private TextView mUsernameTextView;              // 用户名
    private EditText mNicknameEditText;              // 昵称
    private EditText mPhoneEditText;                 // 联系电话
    private EditText mQQEditText;                    // QQ
    private EditText mSayEditText;                   // 个人签名
    private Button mModifyButton;                    // 修改按钮

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int PERMISSIONS_REQUEST_CAMERA = 2;
    private static final int CHOOSE_PICTURE = 0;
    private static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    private Uri tempUri;

    /**
     * 便捷启动当前activity
     *
     * @param activity 启动当前activity的activity
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, ModifyInfoActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_info);

        prepareUI();
        prepareData();

    }

    /**
     * 准备UI
     */
    private void prepareUI() {
        mNavigationViewRed = (NavigationViewRed) findViewById(R.id.nav_modify_user_info);
        mPortraitLayout = findViewById(R.id.rl_modify_user_info_portrait_layout);
        mPortraitImageView = (SimpleDraweeView) findViewById(R.id.sdv_modify_user_info_portrait);
        mUsernameTextView = (TextView) findViewById(R.id.tv_modify_user_info_username);
        mNicknameEditText = (EditText) findViewById(R.id.et_modify_user_info_nickname);
        mPhoneEditText = (EditText) findViewById(R.id.et_modify_user_info_phone);
        mQQEditText = (EditText) findViewById(R.id.et_modify_user_info_qq);
        mSayEditText = (EditText) findViewById(R.id.et_modify_user_info_say);
        mModifyButton = (Button) findViewById(R.id.btn_modify_user_info_modify);

        mNavigationViewRed.setupNavigationView(true, false, "修改资料", new NavigationViewRed.OnClickListener() {
            @Override
            public void onBackClick(View v) {
                finish();
            }
        });

        // 头像点击
        mPortraitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyUserAvatar();
            }
        });

        // 修改按钮点击事件
        mModifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveModifyInfo();
            }
        });

        // 自动弹出键盘
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//
//            public void run() {
//                InputMethodManager inputManager = (InputMethodManager) mNicknameEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputManager.showSoftInput(mNicknameEditText, 0);
//            }
//
//        }, 500);

    }

    /**
     * 准备数据
     */
    private void prepareData() {
        mPortraitImageView.setImageURI(UserBean.shared().getAvatarUrl());
        mUsernameTextView.setText(UserBean.shared().getUsername());
        mNicknameEditText.setText(UserBean.shared().getNickname());
        mPhoneEditText.setText(UserBean.shared().getPhone());
        mQQEditText.setText(UserBean.shared().getQq());
        mSayEditText.setText(UserBean.shared().getSaytext());
    }

    /**
     * 修改用户头像
     */
    private void modifyUserAvatar() {
        // 判断是否有写入SD权限
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(mContext,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            // 弹出选择图片会话框
            showChoosePicDialog();
        }
    }

    /**
     * 打开相机拍照-先检查相机权限
     */
    private void openTakePhoto() {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mContext,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
        } else {
            takePhoto();
        }
    }

    /**
     * 运行时权限请求回调结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showChoosePicDialog();
                } else {
                    ProgressHUD.showInfo(mContext, "没有文件写入权限");
                }
                break;
            case PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    ProgressHUD.showInfo(mContext, "没有权限使用相机");
                }
                break;
        }
    }

    /**
     * 选择获取图片的方式
     */
    protected void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("上传头像");
        String[] items = {"选择本地照片", "拍照"};
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CHOOSE_PICTURE: // 选择本地照片
                        selectPhoto();
                        break;
                    case TAKE_PICTURE: // 拍照
                        openTakePhoto();
                        break;
                }
            }
        });
        builder.create().show();
    }

    /**
     * 选择本地图片
     */
    private void selectPhoto() {
        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        openAlbumIntent.setType("image/*");
        startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
        overridePendingTransition(R.anim.column_show, R.anim.column_bottom);
    }

    /**
     * 使用相机拍照
     */
    private void takePhoto() {
        // 创建拍照时的临时文件
        File tempFile = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        try {
            if (tempFile.exists()) {
                tempFile.delete();
            }
            tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 24) {
            tempUri = FileProvider.getUriForFile(mContext, "tv.baokan.baokanandroid.cameraalbum.fileprovider", tempFile);
        } else {
            tempUri = Uri.fromFile(tempFile);
        }

        // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        startActivityForResult(openCameraIntent, TAKE_PICTURE);
        overridePendingTransition(R.anim.column_show, R.anim.column_bottom);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PICTURE:
                    startPhotoZoom(tempUri);
                    break;
                case CHOOSE_PICTURE:
                    startPhotoZoom(data.getData());
                    break;
                case CROP_SMALL_PICTURE:
                    if (data != null) {
                        setImageToView(data);
                    }
                    break;
            }
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    protected void startPhotoZoom(Uri uri) {
        if (uri == null) {
            LogUtils.d(TAG, "The uri is not exist.");
            return;
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    /**
     * 保存裁剪之后的图片数据
     */
    protected void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            uploadPic(photo);
        }
    }

    /**
     * 上传图片到服务器
     *
     * @param bitmap 位图
     */
    private void uploadPic(Bitmap bitmap) {
        String imagePath = StreamUtils.savePhoto(bitmap, Environment.getExternalStorageDirectory().getAbsolutePath(), String.valueOf(System.currentTimeMillis()));
        mPortraitImageView.setImageURI("file://" + imagePath);

        final KProgressHUD hud = ProgressHUD.show(mContext, "正在处理");

        LogUtils.d(TAG, "imagePath = " + imagePath);
        if (imagePath != null) {
            OkHttpUtils.post()
                    .addFile("file", "avatar.png", new File(imagePath))
                    .url(APIs.MODIFY_ACCOUNT_INFO)
                    .addParams("username", UserBean.shared().getUsername())
                    .addParams("userid", UserBean.shared().getUserid())
                    .addParams("token", UserBean.shared().getToken())
                    .addParams("action", "UploadAvatar")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            ProgressHUD.showInfo(mContext, "您的网络不给力");
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            LogUtils.d(TAG, response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getString("err_msg").equals("success")) {

                                    // 修改资料成功后需要更新本地用户信息
                                    UserBean.updateUserInfoFromNetwork(new UserBean.OnUpdatedUserInfoListener() {
                                        @Override
                                        public void onSuccess(UserBean userBean) {
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    hud.dismiss();
                                                    ProgressHUD.showInfo(mContext, "修改头像成功");
                                                    finish();
                                                }
                                            }, 1000);
                                        }

                                        @Override
                                        public void onError(String tipString) {
                                            hud.dismiss();
                                            ProgressHUD.showInfo(mContext, tipString);
                                        }
                                    });
                                } else {
                                    hud.dismiss();
                                    ProgressHUD.showInfo(mContext, jsonObject.getString("info"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                hud.dismiss();
                                ProgressHUD.showInfo(mContext, "数据解析异常");
                            }
                        }
                    });
        }
    }

    /**
     * 保存修改信息
     */
    private void saveModifyInfo() {

        final KProgressHUD hud = ProgressHUD.show(mContext, "正在处理");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("username", mUsernameTextView.getText().toString());
        parameters.put("userid", UserBean.shared().getUserid());
        parameters.put("token", UserBean.shared().getToken());
        parameters.put("action", "EditInfo");
        parameters.put("nickname", mNicknameEditText.getText().toString());
        parameters.put("qq", mQQEditText.getText().toString());
        parameters.put("phone", mPhoneEditText.getText().toString());
        parameters.put("saytext", mSayEditText.getText().toString());

        NetworkUtils.shared.post(APIs.MODIFY_ACCOUNT_INFO, parameters, new NetworkUtils.StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                hud.dismiss();
                ProgressHUD.showInfo(mContext, "您的网络不给力哦");
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtils.d(TAG, response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("err_msg").equals("success")) {

                        // 修改资料成功后需要更新本地用户信息
                        UserBean.updateUserInfoFromNetwork(new UserBean.OnUpdatedUserInfoListener() {
                            @Override
                            public void onSuccess(UserBean userBean) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        hud.dismiss();
                                        ProgressHUD.showInfo(mContext, "修改资料成功");
                                        finish();
                                    }
                                }, 1000);
                            }

                            @Override
                            public void onError(String tipString) {
                                hud.dismiss();
                                ProgressHUD.showInfo(mContext, tipString);
                            }
                        });
                    } else {
                        hud.dismiss();
                        ProgressHUD.showInfo(mContext, jsonObject.getString("info"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hud.dismiss();
                    ProgressHUD.showInfo(mContext, "数据解析异常");
                }
            }
        });

    }

}
