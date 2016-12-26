package tv.baokan.baokanandroid.model;

import org.json.JSONObject;

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
    public static class InsetPhotoBean {

        // 图片占位字符
        private String ref;

        // 图片描述
        private String caption;

        // 图片url
        private String url;

        // 宽度 像素单位
        private int widthPixel;

        // 高度 像素单位
        private int heightPixel;

        InsetPhotoBean(JSONObject photo) {

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

        public int getWidthPixel() {
            return widthPixel;
        }

        public int getHeightPixel() {
            return heightPixel;
        }
    }

    public static class ArticleDetailPhotoBean {

        // 图片标题
        private String title;

        // 图片描述
        private String caption;

        // 图片url
        private String bigpic;

        ArticleDetailPhotoBean(JSONObject photo) {

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

        // 分类名称
        private String classname;

        ArticleDetailLinkBean(JSONObject linkArticle) {

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

        public String getClassname() {
            return classname;
        }
    }

}
