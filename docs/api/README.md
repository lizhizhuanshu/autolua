# API
**AutoLua** 的**用户界面**代码和**自动化**代码运行在不同的环境中，所以他们有**完全不同**的API，当前**用户界面**的框架使用的是**陌陌开源的UI框架**，可以直接查看他们的[API文档](https://github.com/momotech/MLN/wiki/MLN#api)
## 自动化API
[Screen](__Screen.lua)包含了[Display](__Display.lua)和[Viewable](__Viewable.lua)提供了**获取屏幕尺寸，找图找色**的功能  
[Input](__Input.lua)提供了**模拟点击**的功能  
[UI](__UI.lua)提供了与**用户界面交互**的功能  
[FloatView](__FloatView.lua)提供了对**悬浮窗口**一些简单的操作  
[thread](thread.lua)提供了**sleep**,**usleep**功能  
[全局变量](__other.lua)列举出了所有注册到lua中的全局变量
## 注意
AutoLua在陌陌开源的UI框架之下还添加了一些自己的API