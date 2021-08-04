---初始化悬浮窗口需要的布局参数对象
---@class LayoutParams
---@field flags integer 窗口的标签
---@field format integer 窗口的颜色渲染模式
---@field gravity integer 吸附参数
---@field width integer 窗口的宽度
---@field height integer 窗口的高度
---@field x integer 窗口的横坐标
---@field y integer 窗口的纵坐标


---布局参数中的flags的几种类型
---@class LayoutParamsFlags
---@field FLAG_KEEP_SCREEN_ON integer 保持屏幕常亮
---@field FLAG_NOT_FOCUSABLE integer 使用这个标签使点击悬浮窗以外的位置可以生效

---布局参数中的format几种类型
---@class PixelFormat
---@field RGB_565 integer
---@field RGB_888 integer
---@field RGBA_8888 integer

---与用户界面进行交互的对象
---@class UI
local M= {}

---创建一个新的悬浮窗口对象
---@param name string 悬浮窗口得名字，每个悬浮窗口名字必须不同
---@param uri string  悬浮窗口加载脚本得uri
---@param layoutParams LayoutParams 悬浮窗口得布局参数,可以为 nil
---@return FloatView
function M:newFloatView(name,uri,layoutParams)
    
end

---消耗一个从UI界面发送的信号
---@return ... 可以是多个值，传入多少返回多少
function M:takeSignal()
    
end

---通过名字获取已经创建的悬浮窗口对象
---@param name string 创建悬浮窗口时传入的名字
---@return FloatView
function M:getFloatView(name)
    
end

---显示一条简单得消息
---@param message string
---@param time integer 消息显示得时间，当前只能设置0或1，0得时键短，1得时间长
function M:showMessage(message,time)
    
end

---发送一条信号，这个函数只能在UI界面得脚本中使用
---可以传入多个值，这些值可以完整得被takeSignal接收到
---但是请注意此函数只能传送一些基本类型得值
function M:sendSignal(...)
    
end

