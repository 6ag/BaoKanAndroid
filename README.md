# BaoKanAndroid

## 项目介绍

爆侃网文app的Android版，数据来自 [爆侃网文](http://www.baokan.name) 。

- 项目主结构使用 `RadioGroup + Fragment` 搭建，使用 `TabLayout + ViewPager` 实现顶部标签栏和资讯列表的切换。

- 资讯详情页使用 `ScrollView` 嵌套 `LinearLayout` 实现，正文使用 `WebView` ，利用 `js` 和 `Java` 交互实现了图片本地缓存和图片占位图、正文字体动态修改、图片点击弹出图片浏览器等混编功能。

- 图秀详情页实现了双击图片缩放、单击隐藏UI、图片保存等主流新闻图片浏览器的功能。

- 实现了资讯列表数据缓存、图库列表数据缓存、资讯内容数据缓存、图库内容数据缓存。

- 实现了用户登录、注册、找回密码、资料修改、头像上传、意见反馈、评论文章、收藏文章、分享等常用功能。

- 整个工程代码注释齐全，适合新手参考，也欢迎大神指点。

## Google Play

<a target='_blank' href='http://download.baokan.tv/baokan.apk'>
<img src='https://camo.githubusercontent.com/10fe76c8a0f9b9d8e5bee8170d88a3293449305a/68747470733a2f2f6f776e636c6f75642e6f72672f77702d636f6e74656e742f7468656d65732f6f776e636c6f75646f72676e65772f6173736574732f696d672f636c69656e74732f627574746f6e732f676f6f676c65706c61792e706e67' width='144' height='49' />
</a>

## 开发环境

使用Android Studio 2.2.3开发，最低支持Android 4.4。

推荐大家使用Android studio开发Android App，eclipse已经跟不上时代的发展了。

## iOS端

iOS端使用 `Xcode8.1` + `Swift3.0` 开发，注释也很详细哦，有兴趣的可以看看。并且项目一些技术点介绍的文章也是使用iOS版作示例，不过原理都差不多的。

[点击获取iOS端源码](https://github.com/6ag/BaoKanIOS)

## 各种效果展示

### 栏目管理

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/0.jpg)

### 资讯列表

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/1.jpg)

### 资讯详情 - 正文

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/2.jpg)

### 资讯详情 - 图片浏览器

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/3.jpg)

### 资讯详情 - 相关连接、评论

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/4.jpg)

### 图秀列表

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/5.jpg)

### 图秀详情

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/6.jpg)

### 热门板块

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/7.jpg)

### 个人中心

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/8.jpg)

### 登录、注册、找回密码 - 支持第三方登录

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/9.jpg)

### 资料管理

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/10.jpg)

## 许可

[MIT](https://raw.githubusercontent.com/Finb/V2ex-Swift/master/LICENSE) © [六阿哥](https://github.com/6ag)

