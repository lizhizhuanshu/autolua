local window = require "window"
local label = Label()
label:setGravity(Gravity.CENTER)
label:text("Hello World!")
label:onClick(function ()
    UI:sendSignal("lizhi",1,2)
end)
---@type window
window:alpha(1)
window:bgColor(Color(0,0,255))
window:addView(label)
