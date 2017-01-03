package tv.baokan.baokanandroid.cache;

import org.litepal.crud.DataSupport;

/**
 * 新闻正文数据缓存
 */
public class NewsContentCache extends DataSupport {

    // 文章id
    private String articleid;

    // 分类id
    private String classid;

    // json数据
    private String news;

    public String getArticleid() {
        return articleid;
    }

    public void setArticleid(String articleid) {
        this.articleid = articleid;
    }

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
