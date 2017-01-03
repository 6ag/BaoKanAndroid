package tv.baokan.baokanandroid.cache;

import org.litepal.crud.DataSupport;

/**
 * 资讯、图库列表数据缓存 (首页数据 也就是 今日头条)
 */

public class NewsListHomeCache extends DataSupport {

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
