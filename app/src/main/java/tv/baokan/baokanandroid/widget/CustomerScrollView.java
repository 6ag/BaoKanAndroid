package tv.baokan.baokanandroid.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

import tv.baokan.baokanandroid.utils.SizeUtils;

/**
 * 仿ios弹簧效果 scrollview 带阻尼
 *
 * @author sunxiaogang
 *         Create by sxg on 2016/08/26
 */
public class CustomerScrollView extends ScrollView {
    private int MaxY = SizeUtils.getScreenHeightPx(getContext());
    /**
     * 阻尼效果 越小 效果越明显
     */
    private int damp = 2;
    /**
     * 弹簧动画持续时间
     */
    private int ANIMATION_TIME = 1000;
    /**
     * 弹力效果
     */
    private float spring = 2.0f;
    private View mView;
    private Rect mRect = new Rect();
    private float y;

    public CustomerScrollView(Context context) {
        super(context);
    }

    public CustomerScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomerScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            this.mView = getChildAt(0);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mView == null) {
            return super.onTouchEvent(ev);
        } else {

            commOnTouchEvent(ev);

        }
        return super.onTouchEvent(ev);
    }

    private boolean canScroll = false;

    private void commOnTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                y = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (isNeedAnimation()) {
                    animation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final float preY = y;
                float nowY = ev.getY();
                int deltaY = (int) (preY - nowY);
                if (Math.abs(deltaY) < Math.abs(nowY) && (Math.abs(deltaY) + 1) > Math.abs(nowY)) {
                    canScroll = false;
                } else {

                    canScroll = true;
                }
                y = nowY;
                if (isNeedMove() && canScroll) {
                    if (mRect.isEmpty()) {
                        mRect.set(mView.getLeft(), mView.getTop(), mView.getRight(), mView.getBottom());
                    }
                    if (mView.getTop() - deltaY / damp < MaxY && mView.getTop() - deltaY / damp > -MaxY) {
                        mView.layout(mView.getLeft(), mView.getTop() - deltaY / damp, mView.getRight(), mView.getBottom() - deltaY / damp);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 是否需要移动视图
     *
     * @return true则移动
     */
    private boolean isNeedMove() {
        int offset = mView.getMeasuredHeight() - getHeight();
        int scrollY = getScrollY();
        return scrollY == 0 || scrollY == offset;
    }

    private boolean isNeedAnimation() {
        return !mRect.isEmpty();
    }

    private void animation() {
        TranslateAnimation ta = new TranslateAnimation(0, 0, mView.getTop(), mRect.top);
        ta.setDuration(ANIMATION_TIME);
        /**
         * 此处是回弹动画设置
         * 体验效果更佳
         */
        ta.setInterpolator(new DecelerateInterpolator(spring));
        mView.startAnimation(ta);
        mView.layout(mRect.left, mRect.top, mRect.right, mRect.bottom);
        mRect.setEmpty();
        y = 0;
    }

    /**
     * 以下是各种set 和get方法  设置 阻尼系数等
     *
     * @return
     */
    public float getSpring() {
        return spring;
    }

    public void setSpring(float spring) {
        this.spring = spring;
    }

    public int getANIMATION_TIME() {
        return ANIMATION_TIME;
    }

    public void setANIMATION_TIME(int ANIMATION_TIME) {
        this.ANIMATION_TIME = ANIMATION_TIME;
    }

    public int getDamp() {
        return damp;
    }

    public void setDamp(int damp) {
        this.damp = damp;
    }
}