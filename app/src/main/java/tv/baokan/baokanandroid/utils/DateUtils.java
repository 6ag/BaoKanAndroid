package tv.baokan.baokanandroid.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    /**
     * 将时间戳转换为固定格式的日期字符串
     * @param timestamp 时间戳
     * @return 日期字符串
     */
    public static String timestampToDateString(String timestamp) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = simpleDateFormat.parse(timestamp);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

}
