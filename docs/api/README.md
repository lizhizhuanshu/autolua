# API
**AutoLua** 的**用户界面**代码和**自动化**代码运行在不同的环境中，所以他们有**完全不同**的API，当前**用户界面**的框架使用的是**陌陌开源的UI框架**，可以直接查看他们的[API文档](https://github.com/momotech/MLN/wiki/MLN#api)
## 自动化API
[Display](viewer.lua)提供了**获取屏幕尺寸，找图找色**的功能  
[Input](input.lua)提供了**模拟点击**的功能  
[UI](ui.lua)提供了与**用户界面交互**的功能  
[FloatView](floatView.lua)提供了对**悬浮窗口**一些简单的操作  
[Thread](thread.lua)提供了**sleep**功能  

## 注意
AutoLua在陌陌开源的UI框架之下还添加了一些自己的API