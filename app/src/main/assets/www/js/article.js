// 图片点击事件
function didTappedImage(index, url) {
    var image = document.getElementById(url);
    var width = image.width;
    var height = image.height;
    var x = image.getBoundingClientRect().left;
    var y = image.getBoundingClientRect().top;
    x = x + document.documentElement.scrollLeft;
    y = y + document.documentElement.scrollTop;

    var json = {'index' : index, 'x' : x, 'y' : y, 'width' : width, 'height' : height, 'url' : url};
    var jsonString = JSON.stringify(json);
    ARTICLE.didTappedImage(jsonString);
}

// 设置字体
function setFontName(name) {
    var content = document.getElementById('content');
    content.style.fontFamily = name;
}

// 设置字体大小
function setFontSize(size) {
    var content = document.getElementById('content');
    content.style.fontSize = size + "px";
}

// 获取网页高度
function getHtmlHeight() {
    return document.body.offsetHeight;
}

// 替换正文图片
function replaceContentImage(message) {
    if (message.match("replaceimage")) {
        var index = message.indexOf("~");
        // 截取占位标识
        var messagereplace = message.substring(0, index);
        // 截取到图片路径
        var messagepath = message.substring(index + 1);
        messagereplace = messagereplace.replace(/replaceimage/, "");


        // 保证只替换一次
        var element = document.getElementById(messagereplace);
        if (element.src.match("loading")) {
            element.src = messagepath;
        }
    }
}