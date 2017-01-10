package tv.baokan.baokanandroid.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.app.BaoKanApp;
import tv.baokan.baokanandroid.model.ColumnBean;
import tv.baokan.baokanandroid.ui.fragment.BaseFragment;
import tv.baokan.baokanandroid.ui.fragment.HotFragment;
import tv.baokan.baokanandroid.ui.fragment.NewsFragment;
import tv.baokan.baokanandroid.ui.fragment.NewsListFragment;
import tv.baokan.baokanandroid.ui.fragment.PhotoFragment;
import tv.baokan.baokanandroid.ui.fragment.ProfileFragment;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;

import static tv.baokan.baokanandroid.ui.fragment.NewsFragment.REQUEST_CODE_COLUMN;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10000; // 写入SD卡权限

    private RadioGroup mRgTabbar;              // 底部tabbar
    private List<BaseFragment> mBaseFragments; // fragment集合
    private Fragment mPreviousFragment;        // 上一个显示的fragment
    private int position;                      // 当前选中的tabbarItem位置
    private ProgressDialog mDownloadDialog;    // 加载进度

    private String serverVersion; // 服务器版本号
    private String description;   // 新版本更新描述
    private String apkUrl;        // 新版本apk下载地址

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prepareUI();
        prepareFragments();
        setItemListener();

        // 检查版本更新
        checkVersion();

    }

    /**
     * 检查是否有新版本
     */
    private void checkVersion() {

        NetworkUtils.shared.get(APIs.UPDATE, new HashMap<String, String>(), new NetworkUtils.StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ProgressHUD.showInfo(mContext, "您的网络不给力");
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("err_msg").equals("success")) {
                        JSONObject versionInfo = jsonObject.getJSONObject("data");
                        serverVersion = versionInfo.getString("version");
                        description = versionInfo.getString("description");
                        apkUrl = versionInfo.getString("url");

                        // 更新版本
                        showUpdateDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ProgressHUD.showInfo(mContext, "数据解析异常");
                }
            }
        });

    }

    /**
     * 弹出对话框更新app版本
     */
    protected void showUpdateDialog() {

        // 检查是否是新版本
        String currentVersion = BaoKanApp.app.getVersionName();
        if (currentVersion.equals(serverVersion)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发现新版本:" + serverVersion);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(description);
        builder.setCancelable(false);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 判断是否有写入SD权限
                if (ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // 申请权限
                    ActivityCompat.requestPermissions(mContext,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                } else {
                    // 有写入权限直接下载apk
                    downloadAPK();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 运行时权限请求回调结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadAPK();
                } else {
                    Toast.makeText(getApplicationContext(), "你没有文件写入权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 下载新版本
     */
    protected void downloadAPK() {

        // apk文件保存路径
        String apkPath = null;
        String apkName = "baokan" + serverVersion + ".apk";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            apkPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        if (apkPath == null) {
            ProgressHUD.showInfo(mContext, "您的手机没有SD卡");
            return;
        }

        // 弹出下载进度会话框
        showDownloadDialog();

        // 下载文件
        OkHttpUtils
                .get()
                .url(apkUrl)
                .build()
                .execute(new FileCallBack(apkPath, apkName) {

                    @Override
                    public void onResponse(File arg0, int arg1) {
                        mDownloadDialog.dismiss();
                        // 下载完成安装apk
                        installAPK(arg0.getAbsolutePath());
                    }

                    @Override
                    public void onError(Call arg0, Exception arg1, int arg2) {
                        mDownloadDialog.dismiss();
                        ProgressHUD.showInfo(mContext, "您的网络不给力哦");
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        // 更新下载进度
                        mDownloadDialog.setProgress(Math.round(progress * 100));
                    }

                });

    }

    /**
     * 弹出下载对话框
     */
    public void showDownloadDialog() {
        mDownloadDialog = new ProgressDialog(mContext);
        mDownloadDialog.setIcon(R.mipmap.ic_launcher);
        mDownloadDialog.setTitle("版本更新");
        mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadDialog.setMessage("正在玩命下载中......");
        mDownloadDialog.getWindow().setGravity(Gravity.CENTER);
        mDownloadDialog.setMax(100);
        mDownloadDialog.show();
    }

    /**
     * 安装下载的新版本apk
     *
     * @param apkPath apk存放路径
     */
    private void installAPK(String apkPath) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    // 从其他应用返回回来 会重新添加fragment，这是个bug 得修复
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // super.onSaveInstanceState(outState);
    }

    /**
     * 准备UI
     */
    private void prepareUI() {
        mRgTabbar = (RadioGroup) findViewById(R.id.rg_tabbar);
    }

    /**
     * 准备fragment
     */
    private void prepareFragments() {
        mBaseFragments = new ArrayList<>();
        mBaseFragments.add(new NewsFragment());
        mBaseFragments.add(new PhotoFragment());
        mBaseFragments.add(new HotFragment());
        mBaseFragments.add(new ProfileFragment());
    }

    /**
     * 监听tabbarItem切换事件
     */
    private void setItemListener() {
        mRgTabbar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_news_item:
                        position = 0;
                        break;
                    case R.id.rb_photo_item:
                        position = 1;
                        break;
                    case R.id.rb_hot_item:
                        position = 2;
                        break;
                    case R.id.rb_profile_item:
                        position = 3;
                        break;
                    default:
                        position = 0;
                        break;
                }

                // 切换fragment
                Fragment currentFragment = mBaseFragments.get(position);
                switchFragment(mPreviousFragment, currentFragment);
            }
        });

        // 默认选中第一个item
        mRgTabbar.check(R.id.rb_news_item);
    }

    /**
     * 切换fragment
     *
     * @param from            需要隐藏的fragment
     * @param currentFragment 当前需要显示的fragment
     */
    private void switchFragment(Fragment from, Fragment currentFragment) {
        // 不是重复点击才切换
        if (mPreviousFragment != currentFragment) {
            mPreviousFragment = currentFragment;

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (from != null) {
                transaction.hide(from);
            }
            if (currentFragment.isAdded()) {
                transaction.show(currentFragment);
            } else {
                transaction.add(R.id.fl_main_content, currentFragment);
            }
            transaction.commit();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
        }
        return true;
    }

    // 记录两次点击退出时的第一次有效点击时间
    private long time = 0;

    /**
     * 2秒内连续点击返回2次back才退出app
     */
    private void exit() {
        if (System.currentTimeMillis() - time > 2000) {
            time = System.currentTimeMillis();
            showToast("再次点击将退出");
        } else {
            removeAllActivity();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_COLUMN:
                // 传递给资讯模块，更新栏目数据
                NewsFragment newsFragment = (NewsFragment) mBaseFragments.get(0);
                newsFragment.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

}
