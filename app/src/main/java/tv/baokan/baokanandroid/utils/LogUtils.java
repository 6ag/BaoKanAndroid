package tv.baokan.baokanandroid.utils;

import android.util.Log;

public class LogUtils {

    private static final int VERBOSE = 1;
    private static final int DEBUG = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;
    private static final int NOTHING = 6;
    public static int level = VERBOSE;

    /**
     * 最低级的日志
     *
     * @param tag tag标签
     * @param msg 日志信息
     */
    public static void v(String tag, String msg) {
        if (level <= VERBOSE) {
            Log.v(tag, msg);
        }
    }

    /**
     * debug级别日志，调试用
     *
     * @param tag tag标签
     * @param msg 日志信息
     */
    public static void d(String tag, String msg) {
        if (level <= DEBUG) {
            Log.v(tag, msg);
        }
    }

    /**
     * info级别日志，重要信息
     *
     * @param tag tag标签
     * @param msg 日志信息
     */
    public static void i(String tag, String msg) {
        if (level <= INFO) {
            Log.v(tag, msg);
        }
    }

    /**
     * warn级别日志，特别需要注意的提示
     *
     * @param tag tag标签
     * @param msg 日志信息
     */
    public static void w(String tag, String msg) {
        if (level <= WARN) {
            Log.v(tag, msg);
        }
    }

    /**
     * error级别日志，这个一般在catch块里用
     *
     * @param tag tag标签
     * @param msg 日志信息
     */
    public static void e(String tag, String msg) {
        if (level <= ERROR) {
            Log.v(tag, msg);
        }
    }

}