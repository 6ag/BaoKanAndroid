package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class AgreementActivity extends BaseActivity {

    private WebView mWebView;
    private NavigationViewRed mNavigationViewRed;     // 导航栏
    private ProgressBar mProgressBar;

    /**
     * 便捷启动当前activity
     *
     * @param activity 启动当前activity的activity
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, AgreementActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        mNavigationViewRed = (NavigationViewRed) findViewById(R.id.nav_agreement);
        mWebView = (WebView) findViewById(R.id.wv_agreement_webview);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_agreement_progressbar);

        mNavigationViewRed.setupNavigationView(true, false, "注册条款", new NavigationViewRed.OnClickListener() {
            @Override
            public void onBackClick(View v) {
                finish();
            }
        });

        mWebView.loadUrl("file:///android_asset/www/html/agreement.html");

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 隐藏加载进度条
                mProgressBar.setVisibility(View.INVISIBLE);
            }

        });
    }

}
