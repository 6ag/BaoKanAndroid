package tv.baokan.baokanandroid.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import okhttp3.Call;
import tv.baokan.baokanandroid.app.BaoKanApp;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.NetworkUtils;

public class UserBean {

    private static final String TAG = "UserBean";

    // 用户id
    private String userid;

    // token
    private String token;

    // 用户名
    private String username;

    // 昵称
    private String nickname;

    // email
    private String email;

    // 用户组
    private String groupName;

    // 注册时间
    private String registerTime;

    // 头像
    private String avatarUrl;

    // qq
    private String qq;

    // 电话号码
    private String phone;

    // 个性签名
    private String saytext;

    // 积分
    private String points;

    // 防止外界构建默认对象
    private UserBean() {
    }

    public UserBean(JSONObject jsonObject) {
        try {
            userid = jsonObject.getString("id");
            token = jsonObject.getString("token");
            username = jsonObject.getString("username");
            nickname = jsonObject.getString("nickname");
            email = jsonObject.getString("email");
            groupName = jsonObject.getString("groupName");
            registerTime = jsonObject.getString("registerTime");
            avatarUrl = jsonObject.getString("avatarUrl");
            qq = jsonObject.getString("qq");
            phone = jsonObject.getString("phone");
            saytext = jsonObject.getString("saytext");
            points = jsonObject.getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.d(TAG, "数据解析异常");
        }
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(String registerTime) {
        this.registerTime = registerTime;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSaytext() {
        return saytext;
    }

    public void setSaytext(String saytext) {
        this.saytext = saytext;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public static UserBean getUserAccount() {
        return userAccount;
    }

    public static void setUserAccount(UserBean userAccount) {
        UserBean.userAccount = userAccount;
    }

    // 防止重复去操作sp 把对象缓存到内存中
    private static UserBean userAccount = null;

    /**
     * 获取缓存的对象
     *
     * @return 用户信息模型
     */
    public static UserBean shared() {
        if (userAccount == null) {
            userAccount = new UserBean();
            userAccount.decode();
        }
        return userAccount;
    }

    /**
     * 是否已经登录
     *
     * @return true登录
     */
    public static boolean isLogin() {
        return !TextUtils.isEmpty(shared().token);
    }

    /**
     * 退出登录
     */
    public void logout() {
        UserBean.userAccount = null;
        SharedPreferences sp = BaoKanApp.getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    /**
     * 从本地更新用户信息 - 登录成功后保存到偏好设置
     */
    public void updateUserInfoFromLocal() {

        // 移除第三方授权
        Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
        if (weibo.isAuthValid()) {
            weibo.removeAccount(true);
        }
        Platform qq = ShareSDK.getPlatform(QQ.NAME);
        if (qq.isAuthValid()) {
            qq.removeAccount(true);
        }

        // 内存缓存
        UserBean.userAccount = this;
        // 磁盘缓存
        encode();
    }

    // 更新用户信息状态监听
    public static class OnUpdatedUserInfoListener {
        public void onError(String tipString) {
        }

        public void onSuccess(UserBean userBean) {
        }
    }

    /**
     * 从网络更新用户信息
     */
    public static void updateUserInfoFromNetwork(final OnUpdatedUserInfoListener userInfoListener) {
        if (isLogin()) {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("username", shared().username);
            parameters.put("userid", shared().userid);
            parameters.put("token", shared().token);
            NetworkUtils.shared.get(APIs.GET_USERINFO, parameters, new NetworkUtils.StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    userInfoListener.onError("您的网络不给力哦");
                }

                @Override
                public void onResponse(String response, int id) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("err_msg").equals("success")) {
                            UserBean userBean = new UserBean(jsonObject.getJSONObject("data"));
                            // 将登录信息保存到数据库
                            userBean.updateUserInfoFromLocal();
                            userInfoListener.onSuccess(userBean);
                        } else {
                            String info = jsonObject.getString("info");
                            LogUtils.d(TAG, "更新用户信息失败" + info);
                            UserBean.shared().logout();
                            userInfoListener.onError(info);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        userInfoListener.onError("数据解析异常");
                    }
                }
            });
        } else {
            userInfoListener.onError("未登录");
        }
    }

    /**
     * 编码
     */
    private void encode() {
        LogUtils.d(TAG, this.toString());
        SharedPreferences sp = BaoKanApp.getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userid", userid);
        editor.putString("token", token);
        editor.putString("username", username);
        editor.putString("nickname", nickname);
        editor.putString("email", email);
        editor.putString("groupName", groupName);
        editor.putString("registerTime", registerTime);
        editor.putString("avatarUrl", avatarUrl);
        editor.putString("qq", qq);
        editor.putString("phone", phone);
        editor.putString("saytext", saytext);
        editor.putString("points", points);
        editor.apply();
    }

    /**
     * 解码
     */
    private void decode() {
        SharedPreferences sp = BaoKanApp.getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userid = sp.getString("userid", "0");
        token = sp.getString("token", "");
        username = sp.getString("username", "");
        nickname = sp.getString("nickname", "");
        email = sp.getString("email", "");
        groupName = sp.getString("groupName", "");
        registerTime = sp.getString("registerTime", "");
        avatarUrl = sp.getString("avatarUrl", "");
        qq = sp.getString("qq", "");
        phone = sp.getString("phone", "");
        saytext = sp.getString("saytext", "");
        points = sp.getString("points", "");
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "userid='" + userid + '\'' +
                ", token='" + token + '\'' +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", groupName='" + groupName + '\'' +
                ", registerTime='" + registerTime + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", qq='" + qq + '\'' +
                ", phone='" + phone + '\'' +
                ", saytext='" + saytext + '\'' +
                ", points='" + points + '\'' +
                '}';
    }
}
