package tv.baokan.baokanandroid.cache;

import org.litepal.crud.DataSupport;

/**
 * 资讯、图库列表数据缓存 （除了 今日头条分类 的数据）
 * 今日头条分类id 为0 表示默认
 */
public class NewsListOtherCache extends DataSupport {

    // 分类id
    private String classid;

    // json数据
    private String news;

    public String getClassid() {
        return classid;
    }

    public void setClassid(String classid) {
        this.classid = classid;
    }

    public String getNews() {
        return news;
    }

    public void setNews(String news) {
        this.news = news;
    }
}
