
---@class Display
local M = {}

---屏幕对象，可以获取屏幕数据，找图找色
Display = M

---获取屏幕初始的宽,高
---@return integer 屏幕的宽
---@return integer 屏幕的高
function M:getBaseWidthHeight()
    
end


---获取屏幕的旋转方向
---@return integer
function M:getRotation()
    
end



---获取手机初始状态是横屏还是竖屏
---@return integer
function M:getBaseDirection()
    
end

---获取手机初始屏幕密度
---@return integer
function M:getBaseDensity()
    
end

---获取当前屏幕宽,高
---@return integer 屏幕的宽
---@return integer 屏幕的高
function M:getWidthHeight()
    
end

---初始化虚拟屏幕的宽和高
---@param width integer 屏幕的宽
---@param height integer 屏幕的高
function M:initialize(width,height)
    
end

---更新虚拟屏幕内的颜色数据
function M:update()
    
end

---手机当前方向是否改变
---@return boolean 手机当前方向与初始化的不同返回true，否则返回flase
function M:isChangeDirection()
    
end


