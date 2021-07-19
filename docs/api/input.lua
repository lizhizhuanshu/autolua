---控制器对象，负责向手机发送模拟事件
---@class Input
Input = {}

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


input = {}

---@class ScreenDeviceInfo
---@field id integer 设备的id
---@field width integer 屏幕的宽度
---@field height integer 屏幕的高度



---获取屏幕设备的id
---@return ScreenDeviceInfo
function input.getScreenDeviceInfo()
    
end

---获取手指点击屏幕产生的事件数据
---@param sum integer 事件总数，手指每按下，并抬起算一次
---@return table 所有事件
function input.getTouchEvents(sum)
    
end

---打开输入设备
---@param id integer 设备id
---@return integer 设备句柄
function input.openInputDevice(id)
    
end
---写入输入事件到输入设备
---@param fd integer 输入设备的句柄
---@param type integer 输入事件类型
---@param code integer 输入事件码
---@param value integer 输入事件值
function input.writeInputEvent(fd,type,code,value)
    
end

---关闭输入设备
---@param fd integer 输入设备的句柄
function input.closeInputDevice(fd)
    
end