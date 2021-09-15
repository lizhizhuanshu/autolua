---控制器对象，负责向手机发送模拟事件
---@class Input
local Input = {}

---按下手指
---@param x number 手指所在点的横坐标
---@param y number 手指所在点的纵坐标
---@param major number 手指与屏幕接触面的主轴尺寸
---@param minor number 手指与屏幕接触面的副轴尺寸
---@param pressure number 手指按压屏幕的强度
---@return integer 返回手指的id
function Input:touchDown(x,y,major,minor,pressure)
end

---移动手指
---@param pointerID integer 手指的id，通过touchDown得到
---@param x number 手指所在点的横坐标
---@param y number 手指所在点的纵坐标
---@param major number 手指与屏幕接触面的主轴尺寸
---@param minor number 手指与屏幕接触面的副轴尺寸
---@param pressure number 手指按压屏幕的强度
function Input:touchMove(pointerID,x,y,major,minor,pressure)
    
end

---抬起手指
---@param pointerID integer 手指的id，通过touchDown得到
function Input:touchUp(pointerID)
    
end

function Input:injectKeyEvent(action,code,repeat,metaState)
    
end
