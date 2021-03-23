Screen:initialize(0,0)
local color = Screen:getColor(100,100)
print("坐标100，100的bgr颜色是",string.format("%06X",color))

print(Screen:getWidthHeight())
print(Controller)

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


Thread:sleep(20000)

print(floatView:show())


print(UI:takeSignal())
floatView:conceal()
