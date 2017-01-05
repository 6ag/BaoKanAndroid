package tv.baokan.baokanandroid.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 解决ViewPager大屏手机滑动问题 无需滑动过半才翻页
 */

public class ListViewPager extends ViewPager {

    private float preX;

    public ListViewPager(Context context) {
        super(context);
    }

    public ListViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean res = super.onInterceptTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            preX = ev.getX();
        } else {
            if (Math.abs(ev.getX() - preX) > 4) {
                return true;
            } else {
                preX = ev.getX();
            }
        }
        return res;
    }
}
