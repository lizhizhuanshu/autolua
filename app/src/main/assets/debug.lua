local debugPrint = ...
local debug = debug
local oldPrint = print
function print(...)
    local t= debug.getinfo(2,"Sl")
    local messageTable = {...}
    for index, value in ipairs(messageTable) do
        messageTable[index] = tostring(value)
    end
    debugPrint(t.source,t.currentline,table.concat(messageTable,"  "))
end