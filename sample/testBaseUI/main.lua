local screen = require "screen"
local controller = require "controller"
local viewer = require "viewer"
local display = screen.newDisplay(0,0)
print(display)
print(display:getWidthHeight())
print(viewer.getColor(display,500,500))

print(screen.getWidth(),screen.getHeight())
--print(screen:getWidthHeight())
print(controller)
-- print(viewer.getColor(screen,150,150))
print(LayoutParams)
print(LayoutParams.TYPE_TOAST)
local layout = LayoutParams()
print(layout)

layout.width  = 500
layout.height = 500

local floatView = UI:newFloatView("test","file://view.lua",layout)
print(floatView)
print(layout)
print(floatView:show())
local message = UI:takeSignal()
print(message[0],message[1])
print(type(message[0]))
floatView:conceal()