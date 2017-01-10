package tv.baokan.baokanandroid.utils;

public class APIs {

    // 基础url
    public static final String BASE_URL = "http://www.baokan.tv/";

    // 基础url
    public static final String API_URL = BASE_URL + "e/bpi/";

    // 版本更新
    public static final String UPDATE =  API_URL + "update.php";

    // 分类
    public static final String GET_CLASS = API_URL + "getNewsClass.php";

    // 文章列表
    public static final String ARTICLE_LIST = API_URL + "getNewsList.php";

    // 文章详情
    public static final String ARTICLE_DETAIL = API_URL + "getNewsContent.php";

    // 获取评论信息
    public static final String GET_COMMENT = API_URL + "getNewsComment.php";

    // 提交评论
    public static final String SUBMIT_COMMENT = API_URL + "subPlPost.php";

    // 顶贴踩贴
    public static final String TOP_DOWN = API_URL + "DoForPl.php";

    // 注册
    public static final String REGISTER = API_URL + "Register.php";

    // 登录
    public static final String LOGIN = API_URL + "loginReq.php";

    // 获取用户信息
    public static final String GET_USERINFO = API_URL + "checkLoginStamp.php";

    // 获取用户收藏夹
    public static final String GET_USER_FAVA = API_URL + "getUserFava.php";

    // 删除好友、收藏夹
    public static final String DEL_ACTIONS = API_URL + "dellActions.php";

    // 增加删除收藏
    public static final String ADD_DEL_FAVA = API_URL + "addFava.php";

    // 修改账号资料/找回密码
    public static final String MODIFY_ACCOUNT_INFO = API_URL + "publicActions.php";

    // 获取用户评论列表
    public static final String GET_USER_COMMENT = API_URL + "getUserComment.php";

    // 搜索
    public static final String SEARCH = API_URL + "search.php";

    // 搜索关键词列表
    public static final String SEARCH_KEY_LIST = API_URL + "searchKeyboard.php";

    // 更新搜索关键词列表的开关
    public static final String UPDATE_SEARCH_KEY_LIST = API_URL + "updateKeyboard.php";

}
