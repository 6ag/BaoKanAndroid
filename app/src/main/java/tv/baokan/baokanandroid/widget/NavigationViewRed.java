package tv.baokan.baokanandroid.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.baokan.baokanandroid.R;

public class NavigationViewRed extends RelativeLayout implements View.OnClickListener {

    public NavigationViewRed(Context context) {
        this(context, null);
    }

    private ImageView backView;
    private TextView titleView;
    private ImageView rightView;
    private OnClickListener callback;

    // 从布局文件加载会调用
    public NavigationViewRed(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.navigation_view_red, this, true);
        backView = (ImageView) view.findViewById(R.id.iv_nav_back);
        titleView = (TextView) view.findViewById(R.id.tv_nav_title);
        rightView = (ImageView) view.findViewById(R.id.iv_nav_right);
        backView.setOnClickListener(this);
        rightView.setOnClickListener(this);
    }

    /**
     * 配置导航栏
     *
     * @param isShowLeft      是否显示左边按钮
     * @param isShowRight     是否显示右边按钮
     * @param title           标题
     * @param onClickListener 监听器
     */
    public void setupNavigationView(boolean isShowLeft, boolean isShowRight, String title, OnClickListener onClickListener) {
        backView.setVisibility(isShowLeft ? VISIBLE : GONE);
        rightView.setVisibility(isShowRight ? VISIBLE : GONE);
        titleView.setText(title);
        this.callback = onClickListener;
    }

    /**
     * 获取返回按钮
     *
     * @return
     */
    public ImageView getBackView() {
        return backView;
    }

    /**
     * 获取标题控件
     *
     * @return
     */
    public TextView getTitleView() {
        return titleView;
    }

    /**
     * 获取右侧按钮,默认不显示
     *
     * @return
     */
    public ImageView getRightView() {
        return rightView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_nav_back:
                if (callback != null) {
                    callback.onBackClick(backView);
                }
                break;
            case R.id.iv_nav_right:
                if (callback != null) {
                    callback.onRightClick(rightView);
                }
                break;
        }
    }

    // 监听点击事件
    public static class OnClickListener {
        public void onBackClick(View v) {

        }

        public void onRightClick(View v) {

        }
    }

}


