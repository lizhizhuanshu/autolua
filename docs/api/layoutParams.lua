---初始化悬浮窗口需要的布局参数对象
---@class LayoutParams
---@field flags integer 窗口的标签
---@field format integer 窗口的颜色渲染模式
---@field Gravity integer 吸附参数
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
---@field RGBX_8888 integer

---@type PixelFormat
PixelFormat={}

---@type LayoutParamsFlags
LayoutParamsFlags = {}