local screen = require "screen"
local viewer = require "viewer"
screen:initialize(0,0)
print(screen:getWidthHeight())
print(Controller)
print(viewer.getColor(screen,150,150))
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
