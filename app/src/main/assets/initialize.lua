local display = Display
local view = view
local isKeepDisplay = false
local Screen = {}

for key, value in pairs(view) do
    if type(value) == "function" then
        if key ~= "newDotMatrix" then
            local oldValue = value
            value = function (self,...)
                if not isKeepDisplay then
                    display:update()
                end
                return oldValue(display,...)
            end
        end
    end
    Screen[key] = value
end

display:initialize(0,0)

function Screen:getBaseWidthHeight()
    return display:getBaseWidthHeight()
end

function Screen:getRotation()
    return display:getRotation()
end

function Screen:getBaseDirection()
    return display:getBaseDirection()
end

function Screen:getWidthHeight()
    return display:getWidthHeight()
end

function Screen:initialize(width,height)
    return display:initialize(width,height)
end

function Screen:update()
    return display:update()
end

function Screen:isChangeDirection()
    return display:isChangeDirection()
end

function Screen:isKeepDisplay()
    return isKeepDisplay
end

function Screen:keepDisplay(is)
    isKeepDisplay = is
end

function Screen:updateAndKeepDisplay()
    isKeepDisplay = true
    display:update()
end

_G.Screen = Screen