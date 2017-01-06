package tv.baokan.baokanandroid.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ArticleDetailBean {

    // 顶贴数
    private String top;

    // 踩帖数
    private String down;

    // 文章标题
    private String title;

    // 发布时间戳
    private String newstime;

    // 文章内容
    private String newstext;

    // 文章url
    private String titleurl;

    // 文章id
    private String id;

    // 分类id
    private String classid;

    // 评论数量
    private String plnum;

    // 是否已经收藏 1收藏  0未收藏
    private String havefava;

    // 文章简介
    private String smalltext;

    // 标题图片
    private String titlepic;

    // 文章来源
    private String befrom;

    // 所有图片数组
    private List<InsetPhotoBean> allPhotoList;

    // 相关连接
    private List<ArticleDetailLinkBean> otherLinks;

    // 更多图片 - 图库
    private List<ArticleDetailPhotoBean> morePicsList;

    public ArticleDetailBean(JSONObject article) {
        try {
            top = article.getString("top");
            down = article.getString("down");
            title = article.getString("title");
            newstime = article.getString("newstime");
            newstext = article.getString("newstext");
            titleurl = article.getString("titleurl");
            id = article.getString("id");
            classid = article.getString("classid");
            plnum = article.getString("plnum");
            havefava = article.getString("havefava");
            smalltext = article.getString("smalltext");
            titlepic = article.getString("titlepic");
            befrom = article.getString("befrom");

            // 正文插图
            allPhotoList = new ArrayList<>();
            JSONArray allPhotoJsonArray = article.getJSONArray("allphoto");
            for (int i = 0; i < allPhotoJsonArray.length(); i++) {
                allPhotoList.add(new InsetPhotoBean(allPhotoJsonArray.getJSONObject(i)));
            }

            // 相关链接
            otherLinks = new ArrayList<>();
            JSONArray otherLinksJsonArray = article.getJSONArray("otherLink");
            for (int i = 0; i < otherLinksJsonArray.length(); i++) {
                otherLinks.add(new ArticleDetailLinkBean(otherLinksJsonArray.getJSONObject(i)));
            }

            // 图库图片
            morePicsList = new ArrayList<>();
            JSONArray morePicsListJsonArray = article.getJSONArray("morepic");
            for (int i = 0; i < morePicsListJsonArray.length(); i++) {
                morePicsList.add(new ArticleDetailPhotoBean(morePicsListJsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ArticleDetailBean{" +
                "top='" + top + '\'' +
                ", down='" + down + '\'' +
                ", title='" + title + '\'' +
                ", newstime='" + newstime + '\'' +
                ", titleurl='" + titleurl + '\'' +
                ", id='" + id + '\'' +
                ", classid='" + classid + '\'' +
                ", plnum='" + plnum + '\'' +
                ", havefava='" + havefava + '\'' +
                ", smalltext='" + smalltext + '\'' +
                ", titlepic='" + titlepic + '\'' +
                ", befrom='" + befrom + '\'' +
                ", allPhotoList=" + allPhotoList +
                ", otherLinks=" + otherLinks +
                ", morePicsList=" + morePicsList +
                ", newstext='" + newstext + '\'' +
                '}';
    }

    public void setPlnum(String plnum) {
        this.plnum = plnum;
    }

    public String getTop() {
        return top;
    }

    public String getDown() {
        return down;
    }

    public String getTitle() {
        return title;
    }

    public String getNewstime() {
        return newstime;
    }

    public String getNewstext() {
        return newstext;
    }

    public String getTitleurl() {
        return titleurl;
    }

    public String getId() {
        return id;
    }

    public String getClassid() {
        return classid;
    }

    public String getPlnum() {
        return plnum;
    }

    public String getHavefava() {
        return havefava;
    }

    public String getSmalltext() {
        return smalltext;
    }

    public String getTitlepic() {
        return titlepic;
    }

    public String getBefrom() {
        return befrom;
    }

    public List<InsetPhotoBean> getAllPhotoList() {
        return allPhotoList;
    }

    public List<ArticleDetailLinkBean> getOtherLinks() {
        return otherLinks;
    }

    public List<ArticleDetailPhotoBean> getMorePicsList() {
        return morePicsList;
    }

    // 正文插图
    public static class InsetPhotoBean implements Serializable {

        // 图片占位字符
        private String ref;

        // 图片描述
        private String caption;

        // 图片url
        private String url;

        // 宽度 - 这里的单位可以看成dip
        private int widthDip;

        // 高度 - 这里的单位可以看成dip
        private int heightDip;

        InsetPhotoBean(JSONObject photo) {
            try {
                ref = photo.getString("ref");
                caption = photo.getString("caption");
                url = photo.getString("url");
                widthDip = photo.getJSONObject("pixel").getInt("width");
                heightDip = photo.getJSONObject("pixel").getInt("height");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public String getRef() {
            return ref;
        }

        public String getCaption() {
            return caption;
        }

        public String getUrl() {
            return url;
        }

        public int getWidthDip() {
            return widthDip;
        }

        public int getHeightDip() {
            return heightDip;
        }
    }

    public static class ArticleDetailPhotoBean {

        // 图片标题
        private String title;

        // 图片描述
        private String caption;

        // 小图url
        private String smallpic;

        // 图片url
        private String bigpic;

        ArticleDetailPhotoBean(JSONObject photo) {
            try {
                title = photo.getString("title");
                caption = photo.getString("caption");
                smallpic = photo.getString("smallpic");
                bigpic = photo.getString("bigpic");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public String getTitle() {
            return title;
        }

        public String getCaption() {
            return caption;
        }

        public String getBigpic() {
            return bigpic;
        }

        public String getSmallpic() {
            return smallpic;
        }
    }

    public static class ArticleDetailLinkBean {

        // 分类id
        private String classid;

        // 文章id
        private String id;

        // 点击量
        private String onclick;

        // 标题
        private String title;

        private String titlepic;

        // 分类名称
        private String classname;

        ArticleDetailLinkBean(JSONObject linkArticle) {
            try {
                classid = linkArticle.getString("classid");
                id = linkArticle.getString("id");
                onclick = linkArticle.getString("onclick");
                title = linkArticle.getString("title");
                titlepic = linkArticle.getString("titlepic");
                classname = linkArticle.getString("classname");
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

        public String getOnclick() {
            return onclick;
        }

        public String getTitle() {
            return title;
        }

        public String getTitlepic() {
            return titlepic;
        }

        public String getClassname() {
            return classname;
        }
    }

}
