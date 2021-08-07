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

function Screen:getBaseWidthHeight()
    return display:getBaseWidthHeight()
end

function Screen:getRotation()
    return display:getRotation()
end

function Screen:getBaseDirection()
    return display:getBaseDirection()
end

function Screen:getBaseDensity()
    return display:getBaseDensity()
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


PageSegMode = {
    PSM_OSD_ONLY = 0;
    PSM_AUTO_OSD = 1;
    PSM_AUTO_ONLY = 2;
    PSM_AUTO = 3;
    PSM_SINGLE_COLUMN = 4;
    PSM_SINGLE_BLOCK_VERT_TEXT = 5;
    PSM_SINGLE_BLOCK = 6;
    PSM_SINGLE_LINE = 7;
    PSM_SINGLE_WORD = 8;
    PSM_CIRCLE_WORD = 9;
    PSM_SINGLE_CHAR = 10;
    PSM_SPARSE_TEXT = 11;
    PSM_SPARSE_TEXT_OSD = 12;
    PSM_RAW_LINE = 13;
}



OcrEngineMode ={
    OEM_TESSERACT_ONLY = 0;
    OEM_CUBE_ONLY = 1;
    OEM_TESSERACT_CUBE_COMBINED = 2;
    OEM_DEFAULT = 3;
}


PageIteratorLevel = {
    RIL_BLOCK = 0;
    RIL_PARA = 1;
    RIL_TEXTLINE = 2;
    RIL_WORD = 3;
    RIL_SYMBOL = 4;
}