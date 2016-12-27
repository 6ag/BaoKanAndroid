package tv.baokan.baokanandroid.utils;

import android.content.Context;

public class SizeUtils {

    /**
     * dip单位转px单位
     *
     * @param context  上下文
     * @param dipValue dip数值
     * @return px数值
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px单位转dip单位
     *
     * @param context 上下文
     * @param pxValue px数值
     * @return dip数值
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取屏幕宽度 dip
     *
     * @param context 上下文
     * @return 宽度
     */
    public static int getScreenWidthDip(Context context) {
        return px2dip(context, context.getResources().getDisplayMetrics().widthPixels);
    }

    /**
     * 获取屏幕高度 dip
     *
     * @param context 上下文
     * @return 高度
     */
    public static int getScreenHeightDip(Context context) {
        return px2dip(context, context.getResources().getDisplayMetrics().heightPixels);
    }

    /**
     * 获取屏幕宽度 px
     *
     * @param context 上下文
     * @return 宽度
     */
    public static int getScreenWidthPx(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕高度 px
     *
     * @param context 上下文
     * @return 高度
     */
    public static int getScreenHeightPx(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

}
