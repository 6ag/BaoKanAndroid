# BaoKanAndroid

## 项目介绍

爆侃网文app的Android版，数据来自 [爆侃网文](http://www.baokan.name) 。

- 项目主结构使用 `RadioGroup + Fragment` 搭建，使用 `TabLayout + ViewPager` 实现顶部标签栏和资讯列表的切换。

- 资讯详情页使用 `ScrollView` 嵌套 `LinearLayout` 实现，正文使用 `WebView` ，利用 `js` 和 `Java` 交互实现了图片本地缓存和图片占位图、正文字体动态修改、图片点击弹出图片浏览器等混编功能。

- 图秀详情页实现了双击图片缩放、单击隐藏UI、图片保存等主流新闻图片浏览器的功能。

- 实现了资讯列表数据缓存、图库列表数据缓存、资讯内容数据缓存、图库内容数据缓存。

- 实现了用户登录、注册、找回密码、意见反馈、评论文章、收藏文章等常用功能。

- 整个工程代码注释齐全，适合新手参考，也欢迎大神指点。

## 开发环境

使用Android Studio 2.2.3开发，最低支持Android 4.4。

推荐大家使用Android studio开发Android App，eclipse已经跟不上时代的发展了。

## iOS端

iOS端使用 `Xcode8.1` + `Swift3.0` 开发，注释也很详细哦，有兴趣的可以看看。并且项目一些技术点介绍的文章也是使用iOS版作示例，不过原理都差不多的。

[点击获取iOS端源码](https://github.com/6ag/BaoKanIOS)

## 各种效果展示

### 网文资讯

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/1.png)

### 新闻详情

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/2.png)

### 相关链接、评论

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/3.png)

### 图库

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/4.png)

### 图库详情

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/5.png)

### 热门

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/6.png)

### 个人中心

![image](https://github.com/6ag/BaoKanAndroid/blob/master/show/7.png)

## 许可

[MIT](https://raw.githubusercontent.com/Finb/V2ex-Swift/master/LICENSE) © [六阿哥](https://github.com/6ag)

