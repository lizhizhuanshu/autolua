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

---@type PixelFormat
PixelFormat={}

---@type LayoutParamsFlags
LayoutParamsFlags = {}



---悬浮窗口对象，用来向屏幕显示一些内容
---@class FloatView
local FloatView = {}

---显示悬浮窗口
function FloatView:show()
    
end

---隐藏悬浮窗口
function FloatView:conceal()
    
end

---销毁悬浮窗口
function FloatView:destroy()
    
end

---获取悬浮窗口左上角的横纵坐标
---@return integer 悬浮窗口的横坐标
---@return integer 悬浮窗口的纵坐标
function FloatView:getXY()
    
end

---将悬浮窗口移动到指定坐标
---@param x integer 横坐标
---@param y integer 纵坐标
function FloatView:setXY(x,y)
    
end

---获取悬浮窗口的宽度和高度
---@return integer 悬浮窗口的宽度
---@return integer 悬浮窗口的高度
function FloatView:getWidthHeight()
    
end


---设置悬浮窗口的宽度和高度
---@param width integer 指定新的悬浮窗口的宽度
---@param height integer 指定新的悬浮窗口的高度
function FloatView:setWidthHeight(width,height)
    
end

---获取悬浮窗口的名字
---@return string
function FloatView:getName()
    
end

---重新加载悬浮窗口的脚本
---@param uri string 表示脚本位置的uri
function FloatView:reload(uri)
    
end


---与用户界面进行交互的对象
---@class UI
UI= {}

---创建一个新的悬浮窗口对象
---@param name string 悬浮窗口得名字，每个悬浮窗口名字必须不同
---@param uri string  悬浮窗口加载脚本得uri
---@param layoutParams LayoutParams 悬浮窗口得布局参数,可以为 nil
---@return FloatView
function UI:newFloatView(name,uri,layoutParams)
    
end

---消耗一个从UI界面发送的信号
---@return ... 可以是多个值，传入多少返回多少
function UI:takeSignal()
    
end

---通过名字获取已经创建的悬浮窗口对象
---@param name string 创建悬浮窗口时传入的名字
---@return FloatView
function UI:getFloatView(name)
    
end

---显示一条简单得消息
---@param message string
---@param time integer 消息显示得时间，当前只能设置0或1，0得时键短，1得时间长
function UI:showMessage(message,time)
    
end

---发送一条信号，这个函数只能在UI界面得脚本中使用
---可以传入多个值，这些值可以完整得被takeSignal接收到
---但是请注意此函数只能传送一些基本类型得值
function UI:sendSignal(...)
    
end

