---虚拟屏幕对象，可以通过他获取屏幕颜色数据，和屏幕的宽高，
---旋转方向等，使用此对象前必须初始化
---@class VirtualDisplay:DisplayBuffer
local M = {}


---重置宽高，当宽高都为0时，会根据当前
---屏幕的宽高进行初始化
---@param width integer 虚拟屏幕的宽
---@param height integer 虚拟屏幕的高
function M:reset(width,height)
end

---获取当前虚拟屏幕的宽度和高度
---@return integer 当前虚拟屏幕的宽度
---@return integer 当前虚拟屏幕的高度
function M:getWidthHeight()
end

---检测当前虚拟屏幕的旋转方向是否与初始化的方向一致
---@return boolean 如果方向改变了会返回false，没改变返回true
function M:checkDirection()
end

---更新虚拟屏幕的颜色数据，请注意不调用此函数的话，
---虚拟屏幕的颜色数据是不会改变的
function M:update()
    
end

---销毁当前虚拟的显示
function M:destroy()
    
end