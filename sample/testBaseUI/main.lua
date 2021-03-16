local screen = require "screen"
local controller = require "controller"
local viewer = require "viewer"
local thread = require "thread"

local display = screen.newDisplay(0,0)
print(display)
print(display:getWidthHeight())
local color = viewer.getColor(display,100,100)
print("坐标100，100的bgr颜色是",string.format("%06X",color))

print(screen.getWidth(),screen.getHeight())
print(controller)

---@type LayoutParams
local layoutParams = {
    width = 800,
    height = 800,
    flags = LayoutParamsFlags.FLAG_NOT_FOCUSABLE,
    format = PixelFormat.RGBA_8888,
}

local floatView = UI:newFloatView("test","file://view.lua",layoutParams)
print(floatView)
print("开始延时5秒")
thread.sleep(5000)

print(floatView:show())


print(UI:takeSignal())
floatView:conceal()
