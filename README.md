# AutoLua
AutoLua是一个安卓平台的支持模拟点击，找图找色的脚本开发框架，当前使用的是lua5.4，功能类似与触动精灵，按键精灵，autojs等

## 开发环境
* AndroidStudio
* VSCode(非必要)用于开发lua脚本

## 模块说明
* androidlua
    > 1. java对象注册到lua中
    > 2. lua使用java对象
* autolua 核心模块
    > 1. 提供了一个运行在root进程的lua解释器
    > 2. app进程与root进程通信
    > 3. 模拟点击，找图找色等函数
* sample 只是一个简单的示例程序
运行项目，需要一些权限，点击启动按钮左上角会出现控制图标，点击控制图标会执行[lua脚本](sample/src/main/assets/mainTest.lua)你可以修改文件内容再次运行查看效果，最后一定要点击停止服务，一定要点击停止服务，一定要点击停止服务，否则root进程可能会长期驻留在手机后台，只有重启手机才能够清除



## TODO
* 使用lua语言编写UI界面，使用[MLN UI框架](https://github.com/lizhizhuanshu/MLN)
* 完善lua端的API文档，以及一些用的API补充
* android端SDK文档说明？
* 开发一个类似商业项目的运行脚本的客户端
* 在VSCode上实现一个调试器
* 暂时只想到这么多


## 参考项目
[MLN](https://github.com/lizhizhuanshu/MLN)  
[LuaJava](https://github.com/jasonsantos/luajava)  
[LuaScriptCore](https://github.com/vimfung/LuaScriptCore)  

## License
[MIT](LICENSE)

## 帮助我
如果你有什么好的意见或者建议欢迎提交[Issues](https://github.com/lizhizhuanshu/autolua/issues)，或者[Pull request](https://github.com/lizhizhuanshu/autolua/pulls)

## 其他
框架仅仅只是个人非盈利性质的在维护的，所以对于问题的响应可能不会很快
