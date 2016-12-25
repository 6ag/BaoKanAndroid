package tv.baokan.baokanandroid.model;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ArticleListBean {

    // 文章分类id
    private String classid;

    // 文章id
    private String id;

    // 文章标题
    private String title;

    // 文章来源
    private String befrom;

    // 点击量
    private String onclick;

    // 评论数
    private String plnum;

    // 创建文章的时间戳
    private String newstime;

    // 标题图片
    private String titlepic;

    // 标题多图
    private String[] morepic;

    public ArticleListBean(JSONObject article) {
        try {
            classid = article.getString("classid");
            id = article.getString("id");
            title = article.getString("title");
            befrom = article.getString("befrom");
            onclick = article.getString("onclick");
            plnum = article.getString("plnum");
            newstime = article.getString("newstime");
            titlepic = article.getString("titlepic");
            JSONArray morepicArray = article.getJSONArray("morepic");
            morepic = new String[morepicArray.length()];
            for (int j = 0; j < morepicArray.length(); j++) {
                morepic[j] = morepicArray.get(j).toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getClassid() {
        return classid;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBefrom() {
        return befrom;
    }

    public String getOnclick() {
        return onclick;
    }

    public String getPlnum() {
        return plnum;
    }

    public String getNewstime() {
        return newstime;
    }

    public String getTitlepic() {
        return titlepic;
    }

    public String[] getMorepic() {
        return morepic;
    }
}
