
local M = {}

---@class ScreenDeviceInfo
---@field id integer 设备的id
---@field width integer 屏幕的宽度
---@field height integer 屏幕的高度



---获取屏幕设备的id
---@return ScreenDeviceInfo
function M.getScreenDeviceInfo()
    
end

---获取手指点击屏幕产生的事件数据
---@param sum integer 事件总数，手指每按下，并抬起算一次
---@return table 所有事件
function M.getTouchEvents(sum)
    
end

---打开输入设备
---@param id integer 设备id
---@return integer 设备句柄
function M.openInputDevice(id)
    
end
---写入输入事件到输入设备
---@param fd integer 输入设备的句柄
---@param type integer 输入事件类型
---@param code integer 输入事件码
---@param value integer 输入事件值
function M.writeInputEvent(fd,type,code,value)
    
end

---关闭输入设备
---@param fd integer 输入设备的句柄
function M.closeInputDevice(fd)
    
end

return M